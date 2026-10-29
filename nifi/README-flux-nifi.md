# Flux NiFi — Extraction, validation et chargement des clients

Ce document décrit précisément le flux NiFi de l'Exercice 2 (étape 2 du travail demandé).
Il permet de **reconstruire le flux à l'identique en une dizaine de minutes**, quelle que
soit la version de NiFi : chaque processeur, chaque propriété et chaque connexion y sont
listés avec leurs valeurs exactes.

## Vue d'ensemble du flux

```
fichier CSV                                    MySQL
(clients_a_charger.csv)                       (table clients)
        │                                          ▲
        ▼                                          │ INSERT (par lots)
 ┌─────────────┐   ┌───────────────┐  valides  ┌───┴──────────────┐
 │   GetFile   │──►│  QueryRecord  │──────────►│ PutDatabaseRecord │
 │ (extraction)│   │ (validation   │           │  (chargement)     │
 └─────────────┘   │  par requête  │           └───┬──────────────┘
                   │  SQL)         │            success    failure/retry
                   └──────┬────────┘                │           │
                     invalides                ┌─────▼────┐ ┌────▼─────┐
                          │                   │ PutFile  │ │ PutFile  │
                          ▼                   │ traités  │ │ erreurs  │
                   ┌─────────────┐            └──────────┘ └──────────┘
                   │  PutFile    │
                   │  rejetés    │
                   └─────────────┘
```

**Rôle de chaque étape**

| Étape | Processeur | Rôle |
|---|---|---|
| 1. Extraction | `GetFile` | Surveille le répertoire d'entrée et consomme le CSV des nouveaux clients |
| 2. Validation | `QueryRecord` | Deux requêtes SQL partitionnent les enregistrements : `valides` / `invalides` |
| 3. Chargement | `PutDatabaseRecord` | Insère les enregistrements valides dans MySQL via des requêtes **paramétrées** (aucune concaténation SQL, donc aucune injection possible) |
| Traçabilité | `PutFile` ×3 | Archivage des fichiers traités, rejets de validation, erreurs de chargement |

## Données d'entrée

Fichier : `nifi/data/input/clients_a_charger.csv` — 16 enregistrements :

- **12 lignes valides** (identifiants 21 à 32) : elles doivent se retrouver dans la table `clients` ;
- **4 lignes invalides** (identifiants 33 à 36) : elles doivent être rejetées —

| id | Motif de rejet |
|---|---|
| 33 | e-mail sans `@` (`samba.ndiaye#example.com`) |
| 34 | champ `nom` vide |
| 35 | `statut` = `SUSPENDU` (valeurs admises : `ACTIF`, `INACTIF`) |
| 36 | champ `prenom` vide |

## Règles de validation (extraites de la requête SQL)

Champ par champ :

| Champ | Règle |
|---|---|
| `nom`, `prenom` | obligatoires, non vides |
| `email` | obligatoire, doit contenir `@` puis `.` |
| `statut` | obligatoire, énumération `ACTIF` / `INACTIF` |

## Services de contrôleur à créer

Créer les trois services (Configuration du groupe de processus → « Controller Services » → « + »).

### 1. `CSVReader` (lecteur d'enregistrements)

| Propriété | Valeur |
|---|---|
| Name | `CSVReader` |
| Schema Access Strategy | `Infer Schema` |
| CSV Format | `Default` (la 1ʳᵉ ligne est l'en-tête) |
| Value Delimiter | `,` |

### 2. `CSVRecordSetWriter` (writer)

| Propriété | Valeur |
|---|---|
| Name | `CSVRecordSetWriter` |
| Schema Write Strategy | `No Schema` |
| CSV Format | `Default` |
| Value Delimiter | `,` |
| Include Header | `true` |

### 3. `DBCPConnectionPool` (connexion MySQL)

| Propriété | Valeur |
|---|---|
| Name | `DBCP_MySQL` |
| Database Connection URL | `jdbc:mysql://mysql:3306/clientsdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` *(voir note)* |
| Database Driver Class Name | `com.mysql.cj.jdbc.Driver` |
| Database User | `soapapp` |
| Password | `soappass` |

> **Note URL** : `mysql` est le nom du service dans `docker-compose` (Partie 4). Si MySQL
> tourne sur la machine hôte et NiFi dans Docker, utiliser `host.docker.internal` ; si NiFi
> tourne hors Docker avec MySQL local, utiliser `localhost`.
>
> **Pilote JDBC obligatoire** : copier `mysql-connector-j-8.x.jar` dans le répertoire
> `lib/` de NiFi puis redémarrer (voir `docs/installation-nifi.md`).

## Processeurs : propriétés exactes

### 1. `GetFile` — *1. Extraction (CSV clients)*

| Propriété | Valeur |
|---|---|
| Input Directory | répertoire `nifi/data/input` du dépôt (chemin absolu recommandé) |
| Keep Source File | `false` |

Relations : `success` → QueryRecord.

### 2. `QueryRecord` — *2. Validation (SQL)*

| Propriété | Valeur |
|---|---|
| Record Reader | `CSVReader` |
| Record Writer | `CSVRecordSetWriter` |

Propriétés dynamiques (bouton « + » en bas de l'onglet Properties) — le **nom de la
propriété devient la relation de sortie** :

| Nom de propriété | Valeur (requête SQL) |
|---|---|
| `valides` | `SELECT * FROM FLOWFILE WHERE nom <> '' AND prenom <> '' AND email LIKE '%@%.%' AND statut IN ('ACTIF', 'INACTIF')` |
| `invalides` | `SELECT * FROM FLOWFILE WHERE NOT (nom <> '' AND prenom <> '' AND email LIKE '%@%.%' AND statut IN ('ACTIF', 'INACTIF'))` |

Relations : `valides` → PutDatabaseRecord ; `invalides` → PutFile rejetés ;
`original` et `failure` → auto-terminés.

### 3. `PutDatabaseRecord` — *3. Chargement (MySQL)*

| Propriété | Valeur |
|---|---|
| Record Reader | `CSVReader` |
| Statement Type | `insert` |
| Table Name | `clients` |
| DBCP Connection Pool | `DBCP_MySQL` |
| Translate Field Names | `false` (les colonnes du CSV portent déjà les noms des colonnes SQL) |
| Unmatched Column Behavior | `ignore` |

Relations : `success` → PutFile traités ; `failure` et `retry` → PutFile erreurs.

### 4. `PutFile` — *4a. Archivage (traités)*

| Propriété | Valeur |
|---|---|
| Directory | `nifi/data/traites` |
| Conflict Resolution Strategy | `replace` |

### 5. `PutFile` — *4b. Rejets (validation)*

| Propriété | Valeur |
|---|---|
| Directory | `nifi/data/rejetes` |
| Conflict Resolution Strategy | `replace` |

### 6. `PutFile` — *4c. Erreurs (chargement)*

| Propriété | Valeur |
|---|---|
| Directory | `nifi/data/erreurs` |
| Conflict Resolution Strategy | `replace` |

## Procédure de test du flux

1. Démarrer MySQL puis exécuter `db/01_schema.sql` et `db/02_donnees_test.sql`
   (la table contient 20 clients) ;
2. Vérifier que `mysql-connector-j-8.x.jar` est présent dans `lib/` de NiFi ;
3. Activer les trois services de contrôleur (Enable) ;
4. Lancer les processeurs (Play) ;
5. Copier (ou glisser) `clients_a_charger.csv` dans `nifi/data/input` ;
6. Vérifier les résultats attendus (ci-dessous) ;
7. Arrêter les processeurs après la démonstration.

## Résultats attendus

| Contrôle | Résultat attendu |
|---|---|
| `SELECT COUNT(*) FROM clients;` | **32** (20 du script SQL + 12 chargées par NiFi) |
| `SELECT * FROM clients WHERE id IN (33,34,35,36);` | **0 ligne** (rejetées) |
| Répertoire `nifi/data/traites` | 1 fichier CSV contenant les 12 enregistrements valides |
| Répertoire `nifi/data/rejetes` | 1 fichier CSV contenant les 4 enregistrements invalides |
| Répertoire `nifi/data/erreurs` | vide (sauf problème MySQL : pilote absent, base arrêtée…) |

## Dépannage rapide

| Symptôme | Cause probable / correction |
|---|---|
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | Pilote absent de `lib/` — copier le JAR et redémarrer NiFi |
| `Access denied for user 'soapapp'` | Identifiants DBCP ≠ ceux de `db/01_schema.sql` |
| PutDatabaseRecord en `failure`, erreurs « Incorrect string value » | Jeu de caractères : vérifier que la base est en `utf8mb4` |
| QueryRecord : `invalides` vide alors que des lignes sont fausses | Vérifier que le `CSVReader` est en `Infer Schema` + `Default` (en-tête) et le writer en `Include Header = true` |
| Rien ne se passe | Chemin du répertoire `Input Directory` incorrect (chemin absolu recommandé) |

## Exporter le flux en template (artefact versionné dans le dépôt)

Une fois le flux construit et testé, exporter un template NiFi — c'est le livrable
« les workflows » du dépôt Git :

1. Sélectionner tous les processeurs et connexions (rectangle de sélection ou Ctrl+A) ;
2. Bouton **Create Template** du panneau Operate → nom : `flux_clients_etl` ;
3. Panneau Operate → **Download Template** → enregistrer le XML dans `nifi/` ;
4. Committer le fichier `nifi/flux_clients_etl.xml`.

Ce template s'importe sur toute instance NiFi 1.x (Upload Template, puis glisser l'icône
Template sur le canevas) : il ne reste qu'à ajuster les chemins de répertoires et l'URL
JDBC du service `DBCP_MySQL`. Sur NiFi 2.x (templates retirés), reconstruire le flux
avec les tableaux ci-dessus.

