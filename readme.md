Test

Synchronisation incrémentale vs complète

Privilégier l'incrémental (récupérer uniquement les données modifiées depuis le dernier run via un timestamp/delta) plutôt qu'un full systématique
Stocker la date du dernier import réussi pour servir de point de départ
Prévoir une stratégie de resynchronisation complète périodique ou manuelle (pour rattraper les écarts)

Cohérence des données

Validation des données reçues (formats, champs obligatoires, types) avant insertion
Gestion des conflits si la BDD peut aussi être modifiée par ailleurs
Stratégie en cas de données partielles (l'API ne renvoie qu'une partie du dataset attendu)


Récupération depuis l'API

Gestion de la pagination (récupérer toutes les pages, pas seulement la première)
Timeouts de connexion et de lecture configurés explicitement
Politique de retry avec backoff exponentiel sur erreurs transitoires (5xx, timeouts), mais pas sur les erreurs 4xx*

Robustesse et reprise

Gestion transactionnelle : que se passe-t-il en cas d'échec au milieu du traitement (rollback partiel, reprise)
Skip/retry policy Spring Batch pour isoler les enregistrements en erreur sans faire échouer tout le batch
Restartabilité du job (Spring Batch permet de reprendre un job échoué là où il s'est arrêté via le JobRepository)
Éviter les exécutions concurrentes (verrou/lock si un run précédent n'est pas terminé)

Cohérence des données

Validation des données reçues (formats, champs obligatoires, types) avant insertion
Gestion des conflits si la BDD peut aussi être modifiée par ailleurs
Stratégie en cas de donné







Je travaille sous Windows avec WSL et un devcontainer en Docker rootless. Je veux ouvrir mon container depuis Windows Terminal en un clic, sans manipulations dans PowerShell.

Fais trois choses.

Premièrement, crée dans WSL un script appelé claude-container, placé dans le dossier bin de mon home. Ce script règle les variables d’environnement pour Docker rootless, démarre le devcontainer de ce projet avec le CLI devcontainer s’il n’est pas lancé, puis exécute dedans la commande passée en argument, ou un bash par défaut. Installe le CLI devcontainer s’il manque.

Deuxièmement, ajoute dans le fichier settings de Windows Terminal deux profils. Le premier, Container Claude, ouvre un bash dans le container. Le second, Historique Claude, lance claude avec l’option resume dans le container. Trouve toi-même le nom de ma distribution WSL et le chemin du fichier settings, et fais une sauvegarde de ce fichier avant de le modifier.

Troisièmement, ajoute un volume dans le devcontainer.json pour que le dossier point claude de l’utilisateur du container survive aux rebuilds.

Avant de modifier quoi que ce soit, montre-moi ton plan. À la fin, teste le script et dis-moi comment vérifier que les deux profils marchent.
