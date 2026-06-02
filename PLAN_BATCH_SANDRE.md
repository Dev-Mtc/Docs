# Batch Spring de synchronisation des référentiels SANDRE

> Document de spécification destiné à Claude Code pour générer l'intégralité du projet.

---

## 1. Objectif

Créer un **batch Java (Spring Boot + Spring Batch)** qui synchronise quotidiennement plusieurs référentiels SANDRE depuis l'API REST officielle. Le batch est lancé par un **ordonnanceur externe** (CRON configuré sur le serveur via une variable d'environnement), s'exécute **une seule fois**, puis se termine en remontant un **code de sortie** reflétant son statut (0 = succès, ≠0 = échec).

Le batch ne récupère **que les modifications et ajouts depuis sa dernière exécution réussie**, grâce au paramètre `derniereDateDeMAJ` exposé par l'API.

---

## 2. API SANDRE

### Format d'URL (confirmé par la documentation officielle)

```
https://api.sandre.eaufrance.fr/referentiels/v1/[référentiel].[format]?{outputSchema=schéma}&{compress=true}&{filter=filtre}&{derniereDateDeMAJ=date}&{limit=limite}&{offset=offset}
```

- `[référentiel]` : **obligatoire** (ex : code du référentiel)
- `[format]` : **obligatoire** → utiliser `json`
- `derniereDateDeMAJ` : **filtre incrémental** → ne renvoie que les éléments modifiés/ajoutés depuis cette date
- `limit` / `offset` : **pagination**
- `filter` : filtre champ/opérateur/valeur (optionnel, non utilisé ici)
- `outputSchema`, `compress` : optionnels

L'API **n'a pas de limite de nombre d'appels**.

### Référentiels à synchroniser

| Référentiel | Description |
|-------------|-------------|
| Paramètre   | Nomenclature des paramètres |
| Méthode     | Nomenclature des méthodes |
| Cours d'eau | Référentiel des cours d'eau |
| Obstacle    | Référentiel des obstacles à l'écoulement |
| Intervenant | Référentiel des intervenants |

> ⚠️ Les **codes exacts** de référentiel dans l'URL et le **nom des clés naturelles** (codes identifiants type `CdParametre`, `CdMethode`, etc.) doivent être déterminés en interrogeant réellement l'API (un appel `limit=1` par référentiel) et en inspectant le JSON renvoyé. Ne pas inventer la structure : la déduire de la réponse réelle.

---

## 3. Décision d'architecture FONDAMENTALE

**Mode de synchronisation = UPSERT incrémental (insert/update par clé naturelle SANDRE), JAMAIS un truncate global.**

Raison : l'API en mode delta (`derniereDateDeMAJ`) ne renvoie **que** les éléments modifiés/ajoutés. Un `deleteAll` effacerait tous les enregistrements non modifiés que l'API ne renvoie pas.

- **Premier run** (aucun historique de succès pour le référentiel) → synchro **complète**, sans `derniereDateDeMAJ`.
- **Runs suivants** → `derniereDateDeMAJ` = date de la dernière synchro réussie du référentiel → **upsert** des seuls éléments reçus.

---

## 4. Stack technique

- **Java 21**, **Spring Boot 3.x**, **Maven**, packaging **JAR exécutable**.
- Dépendances :
  - `spring-boot-starter-batch`
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-webflux` (WebClient pour appeler l'API)
  - `postgresql`
  - `net.logstash.logback:logstash-logback-encoder` (logs JSON pour Kibana)
- Base de données : **PostgreSQL**.

---

## 5. Ordonnancement (CRON externe — PAS de `@Scheduled`)

Le batch **ne doit pas** utiliser `@Scheduled` ni de scheduler interne. Il est lancé par un ordonnanceur externe dont la planification provient d'une **variable d'environnement serveur**.

`main()` doit terminer le process avec le code de sortie du batch :

```java
@SpringBootApplication
@EnableBatchProcessing
public class SandreSyncApplication {
    public static void main(String[] args) {
        System.exit(SpringApplication.exit(
            SpringApplication.run(SandreSyncApplication.class, args)));
    }
}
```

Exemple de configuration serveur (crontab) à documenter dans le README, où `CRON_SANDRE` est la variable d'env :

```bash
# /etc/cron.d/sandre-sync
${CRON_SANDRE} appuser /usr/bin/java -jar /opt/sandre/sandre-sync-batch.jar >> /var/log/sandre/run.log 2>&1
```

---

## 6. Table d'historique (schéma `public`)

Destinée à la **supervision par l'exploitation BRGM**. Une ligne par référentiel et par exécution.

```sql
CREATE TABLE public.sync_history (
    id                BIGSERIAL PRIMARY KEY,
    job_execution_id  BIGINT,
    referentiel       VARCHAR(50)  NOT NULL,   -- PARAMETRE, METHODE, COURS_EAU, OBSTACLE, INTERVENANT
    statut            VARCHAR(20)  NOT NULL,   -- STARTED, SUCCESS, FAILED
    started_at        TIMESTAMP    NOT NULL,
    finished_at       TIMESTAMP,
    nb_recus          INTEGER,                 -- nb d'éléments reçus de l'API
    nb_upsert         INTEGER,                 -- nb d'éléments insérés/mis à jour
    derniere_date_maj TIMESTAMP,               -- valeur passée à derniereDateDeMAJ pour ce run
    message_erreur    TEXT,
    duree_ms          BIGINT
);

CREATE INDEX idx_sync_history_ref_date ON public.sync_history(referentiel, started_at DESC);
```

---

## 7. Composants à créer

### 7.1 Client API (`SandreApiClient`)
- WebClient configuré sur la base URL.
- Méthode générique `recuperer(referentiel, derniereDateDeMAJ)` qui **gère la pagination** : boucle sur `offset` par pas de `limit` (taille de page configurable, ex. 1000) jusqu'à une page incomplète ou vide.
- Mapping JSON → DTO par référentiel.
- Timeout configuré. En cas d'API indisponible : remonter une exception explicite (loggée + historisée).

### 7.2 Modèle de données (JPA)
- Une entité par référentiel, identifiée par sa **clé naturelle SANDRE** (sert à l'upsert).
- Entité `SyncHistory` mappée sur `public.sync_history`.

### 7.3 Service d'historique (`SyncHistoryService`)
- `start(referentiel)` → crée une ligne `STARTED`.
- `success(hist, nbRecus, nbUpsert)` / `failed(hist, message)`.
- `derniereDateMajReussie(referentiel)` → date de la dernière synchro `SUCCESS` (ou vide → premier run).

### 7.4 Job & Steps Spring Batch
- `Job` nommé `sandreSyncJob` enchaînant **un Step par référentiel**, séquentiellement.
- Chaque Step = Tasklet générique `AbstractReferentielTasklet<T>` paramétré par référentiel :
  1. `start` historique
  2. lit `derniereDateMajReussie`
  3. appelle l'API (avec pagination)
  4. **upsert** par clé naturelle, en **transaction par référentiel**
  5. `success` ou `failed` historique
- **Stratégie d'erreur** : si un référentiel échoue, son Step est marqué `FAILED` et historisé, **mais le Job poursuit les référentiels suivants**. Le Job global se termine en `FAILED` (code de sortie ≠ 0) si au moins un Step a échoué.

### 7.5 Logs (fichier JSON → Kibana)
- `logback-spring.xml` avec `RollingFileAppender` + `LogstashEncoder` (format JSON).
- Champ fixe `application=sandre-sync-batch`, rotation quotidienne, rétention ~30 jours.
- Logguer : début/fin de chaque référentiel, nb d'éléments, durée, et toute erreur (ex. API indisponible) avec stacktrace.
- Le fichier JSON sera collecté ultérieurement par un agent (Filebeat/Fluent Bit) vers le puits de logs : **aucun code spécifique au puits à ajouter côté batch**.

### 7.6 README
- Build (`mvn clean package`).
- Variables d'environnement (datasource PostgreSQL, base URL API, page size).
- Exemple de configuration CRON serveur via variable d'env (crontab + exemple Kubernetes CronJob).
- Signification des codes de sortie.

---

## 8. Décisions par défaut retenues (ne pas redemander)

1. **Échec d'un référentiel** → le job **poursuit** les autres référentiels ; statut final `FAILED` si au moins un échec.
2. **Format API** → `json`.
3. **Clés naturelles / codes de référentiel** → à **déduire de la réponse réelle de l'API** (appel `limit=1` par référentiel), pas à inventer.
4. **Taille de page de pagination** → 1000 (configurable via `application.yml`).
5. **Premier run** → synchro complète sans `derniereDateDeMAJ`.

---

## 9. Livrables attendus

- Projet Maven complet, compilable (`mvn clean package` sans erreur).
- Script SQL de création de `public.sync_history`.
- `application.yml` paramétrable par variables d'environnement.
- `logback-spring.xml` au format JSON.
- README complet.
