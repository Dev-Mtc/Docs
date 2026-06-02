# sandre-sync-batch

Batch Spring Boot 3 / Spring Batch chargé de synchroniser **incrémentalement** plusieurs
référentiels SANDRE (https://api.sandre.eaufrance.fr) dans une base PostgreSQL locale.

## Référentiels synchronisés

| Nom        | Code API | Clé naturelle                | Date MAJ utilisée               |
|------------|----------|------------------------------|----------------------------------|
| Paramètre  | `par`    | `CdParametre`                | `DateMajParametre`              |
| Méthode    | `met`    | `CdMethode`                  | `DateMajMethode`                |
| Cours d'eau| `cea`    | `CdEntiteHydrographique`     | `DateMajEntiteHydrographique`   |
| Obstacle   | `obs`    | `CdObstEcoul`                | `DateMAJObstEcoul`              |
| Intervenant| `int`    | `CdIntervenant`              | `DateMajIntervenant`            |

> Ces codes et clés ont été déduits d'appels réels à l'API (`?limit=1`) — ils
> reflètent la structure JSON effectivement renvoyée par SANDRE.

## Stratégie de synchronisation

- **Upsert incrémental par clé naturelle**, jamais de `TRUNCATE` ni `DELETE` global.
- **Premier run** d'un référentiel (aucune entrée `SUCCESS` en historique) :
  appel sans `derniereDateDeMAJ` → synchro complète.
- **Runs suivants** : `derniereDateDeMAJ` = date de démarrage du dernier `SUCCESS` pour
  ce référentiel → upsert des seuls éléments retournés.
- Pagination : taille de page configurable (défaut 1000), boucle sur `offset` jusqu'à
  obtenir une page incomplète.

## Stratégie d'erreur

- Un référentiel qui échoue n'interrompt **pas** le job : son `Step` est marqué `FAILED`
  dans l'historique, et les référentiels suivants sont quand même synchronisés.
- Si **au moins un** référentiel a échoué, le statut global du Job est dégradé en
  `FAILED` (par `JobCompletionListener`) → **code de sortie ≠ 0**, exploitable par
  l'ordonnanceur externe.

| Code de sortie | Signification                                |
|----------------|----------------------------------------------|
| `0`            | Tous les référentiels synchronisés `SUCCESS` |
| `1` (≠0)       | Au moins un référentiel `FAILED`             |

## Build

```bash
mvn clean package
```

Produit `target/sandre-sync-batch.jar`, exécutable directement (`java -jar ...`).

## Préparation de la base

PostgreSQL ≥ 12 (le batch utilise du `JSONB`). Créer le schéma :

```bash
psql -h <host> -U <user> -d <db> -f src/main/resources/db/schema.sql
```

Le batch n'effectue **aucun DDL** (`spring.jpa.hibernate.ddl-auto=validate`). Les
tables techniques de Spring Batch sont créées au premier lancement
(`spring.batch.jdbc.initialize-schema=always`).

## Variables d'environnement

| Variable                  | Défaut                                                  | Rôle                                    |
|---------------------------|---------------------------------------------------------|-----------------------------------------|
| `DB_URL`                  | `jdbc:postgresql://localhost:5432/sandre`               | URL JDBC PostgreSQL                     |
| `DB_USER`                 | `sandre`                                                | Utilisateur PostgreSQL                  |
| `DB_PASSWORD`             | `sandre`                                                | Mot de passe                            |
| `DB_POOL_MAX`             | `5`                                                     | Taille max pool HikariCP                |
| `SANDRE_BASE_URL`         | `https://api.sandre.eaufrance.fr/referentiels/v1`       | URL racine API SANDRE                   |
| `SANDRE_PAGE_SIZE`        | `1000`                                                  | Taille de page (paramètre `limit`)      |
| `SANDRE_TIMEOUT`          | `60s`                                                   | Timeout connexion/lecture WebClient     |
| `SANDRE_MAX_IN_MEMORY_MB` | `64`                                                    | Buffer max de la réponse JSON           |
| `LOG_DIR`                 | `/var/log/sandre`                                       | Dossier des logs JSON                   |
| `LOG_BASENAME`            | `sandre-sync-batch`                                     | Préfixe du fichier de log               |
| `LOG_MAX_HISTORY`         | `30`                                                    | Rétention (jours) des logs JSON         |
| `LOG_LEVEL_APP`           | `INFO`                                                  | Niveau de log de `fr.brgm.sandre`       |
| `CRON_SANDRE`             | _(côté serveur uniquement)_                             | Planification cron (cf. ci-dessous)     |

## Ordonnancement (CRON externe)

Le batch **n'a aucun scheduler interne** (pas de `@Scheduled`). Il est lancé par un
ordonnanceur externe, dont la planification est portée par la variable d'environnement
serveur `CRON_SANDRE`.

### Exemple `/etc/cron.d/sandre-sync` (Linux)

```bash
# La planification provient de la variable d'environnement CRON_SANDRE
# (à définir dans /etc/environment ou via Ansible/Chef, ex: CRON_SANDRE="0 3 * * *")
${CRON_SANDRE} appuser /usr/bin/java -jar /opt/sandre/sandre-sync-batch.jar >> /var/log/sandre/run.log 2>&1
```

### Exemple Kubernetes `CronJob`

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: sandre-sync-batch
spec:
  schedule: "${CRON_SANDRE}"   # ex. "0 3 * * *"
  concurrencyPolicy: Forbid
  successfulJobsHistoryLimit: 3
  failedJobsHistoryLimit: 5
  jobTemplate:
    spec:
      backoffLimit: 0
      template:
        spec:
          restartPolicy: Never
          containers:
            - name: sandre-sync
              image: registry.brgm.fr/sandre/sandre-sync-batch:1.0.0
              envFrom:
                - secretRef: { name: sandre-db-credentials }
              env:
                - { name: DB_URL,           value: "jdbc:postgresql://pg/sandre" }
                - { name: SANDRE_PAGE_SIZE, value: "1000" }
              volumeMounts:
                - { name: logs, mountPath: /var/log/sandre }
          volumes:
            - name: logs
              persistentVolumeClaim: { claimName: sandre-logs }
```

## Logs

Sortie JSON via `LogstashEncoder` dans `${LOG_DIR}/${LOG_BASENAME}.json`, avec rotation
quotidienne et compression gzip. Champ fixe `application=sandre-sync-batch` pour le
filtrage Kibana. **Aucun code spécifique au puits de logs côté batch** : un agent
(Filebeat / Fluent Bit) doit collecter le fichier vers Elasticsearch.

## Supervision

Une ligne par référentiel et par exécution dans `public.sync_history` :

```sql
SELECT started_at, referentiel, statut, nb_recus, nb_upsert, duree_ms, message_erreur
FROM   public.sync_history
ORDER  BY started_at DESC
LIMIT  20;
```

## Stack technique

- Java 21, Spring Boot 3.3, Spring Batch 5
- Spring Data JPA + Hibernate 6 (colonne `jsonb` via `@JdbcTypeCode(SqlTypes.JSON)`)
- WebFlux WebClient (réactif, timeouts configurables)
- PostgreSQL (driver)
- Logstash Logback Encoder
