# Exercice 2 — Service SOAP de clients

> Projet « Développement à base de composants et services web » — Java, Apache CXF, NiFi, Grafana.

Service SOAP de gestion de clients exposant les opérations **`getClientDetails`**, **`searchClients`** et **`updateClient`**, alimenté par un flux ETL NiFi (extraction → validation → chargement) et supervisé par Prometheus / Grafana.

## Architecture

```
┌──────────────┐   fichier de    ┌─────────────────────┐     SQL      ┌────────────────┐
│ Jeu de       │   démonstration │        NiFi         │  (chargement │     MySQL      │
│ données      │ ──────────────► │ extraction →        │ ──────────►  │  table         │
│ clients      │                 │ validation →        │  par lots)   │  clients       │
└──────────────┘                 │ chargement          │              └───────┬────────┘
                                 └─────────────────────┘                      │ JDBC (JPA /
                                                                              HikariCP)
                  /services/clients?wsdl                             ┌─────────▼──────────┐
┌──────────────┐  getClientDetails / searchClients /  updateClient   │   Spring Boot 3    │
│ SoapUI ou    │ ◄─────────────────────────────────────────────────► │  + Apache CXF      │
│ client SOAP  │            réponses SOAP / Faults                   │  (JAX-WS)          │
└──────────────┘                                                    └─────────┬──────────┘
                                                                              │ /actuator/prometheus
┌──────────────┐                                                      ┌───────▼──────────┐
│   Grafana    │ ◄───────────── collecte (scrape) ──────────────────  │    Prometheus    │
│ (dashboard)  │                                                      └──────────────────┘
└──────────────┘
```

## Stack technique

| Composant | Technologie | Rôle |
|---|---|---|
| Service SOAP | Java 17, Spring Boot 3.3, Apache CXF 4 (JAX-WS) | Publication du WSDL et des 3 opérations |
| Persistance | Spring Data JPA, MySQL 8, HikariCP | Stockage de la table `clients` |
| ETL | Apache NiFi | Extraction, validation et chargement des données clients |
| Observabilité | Spring Actuator, Micrometer, Prometheus, Grafana | Collecte de métriques et tableau de bord |
| Tests | SoapUI, JUnit 5 | Cas nominaux, erreurs SOAP, bout en bout |

## Structure du dépôt

```
DevComposant/
├── soap-clients-service/          # Application Java (Spring Boot + CXF)
│   └── src/main/java/com/ibam/soapclients/
│       ├── SoapClientsApplication.java
│       ├── config/                # Configuration CXF (durcissement XML, bus)
│       ├── model/                 # Entité JPA Client
│       ├── repository/            # Accès aux données (Spring Data JPA)
│       ├── service/               # Logique métier + validation
│       ├── ws/                    # Contrat SOAP (SEI), implémentation, Faults
│       └── exception/             # Exceptions métier
├── nifi/                          # Flux NiFi : doc etapes + données d'entrée/sortie
├── prometheus/                    # Configuration Prometheus
├── grafana/                       # Provisioning + tableau de bord
├── soapui/                        # Projet SoapUI (tests)
├── docs/                          # Documentation WSDL, contrats, présentation
├── db/                            # Scripts SQL (création + jeu de données)
├── docker-compose.yml             # Environnement complet reproductible
└── README.md
```

## Prérequis

- **JDK 17 ou supérieur** (le Maven Wrapper télécharge Maven automatiquement, aucune installation Maven requise)
- **Docker + Docker Compose** (MySQL, Prometheus, Grafana)
- **Apache NiFi** (installation dédiée, procédure dans `docs/`) et **SoapUI** pour les tests

## Installation (machine neuve : une seule commande)

```bash
git clone <url-du-depot> && cd DevComposant
docker compose up -d --build
```

Au premier démarrage, MySQL exécute automatiquement `db/01_schema.sql` (schéma,
compte applicatif) et `db/02_donnees_test.sql` (20 clients fictifs), puis le
service SOAP démarre une fois la base saine. C'est tout.

| Service | URL | Identifiants |
|---|---|---|
| Service SOAP (WSDL) | http://localhost:8080/services/clients?wsdl | — |
| Santé / métriques | http://localhost:8080/actuator/health · /actuator/prometheus | — |
| Prometheus | http://localhost:9090 (Status → Targets) | — |
| Grafana | http://localhost:3001 (le port 3000 était occupé sur la machine de développement) | admin / admin |
| MySQL | localhost:3306 — base `clientsdb` | soapapp / soappass (root : rootpass) |

Commandes utiles :

```bash
docker compose ps        # état + healthchecks
docker compose logs -f app   # journaux du service
docker compose down      # arrêt (données conservées dans les volumes)
docker compose down -v   # arrêt + remise à zéro (les scripts réinitialisent tout)
```

Mode développeur (service hors Docker, infra dans Docker) :

```bash
docker compose up -d mysql prometheus grafana   # sans l'app
cd soap-clients-service && ./mvnw spring-boot:run
# NB : dans prometheus/prometheus.yml, cibler host.docker.internal:8080 au lieu de app:8080
```

Le flux ETL NiFi (extraction → validation → chargement) est documenté pas à pas dans
[`nifi/README-flux-nifi.md`](nifi/README-flux-nifi.md) — installation de NiFi :
[`docs/installation-nifi.md`](docs/installation-nifi.md) (il accède à MySQL via
`localhost:3306`, port publié par le compose).

## Utilisation

- **WSDL du service** : `http://localhost:8080/services/clients?wsdl` (copie archivée : `docs/ClientService.wsdl`)
- **Contrat complet documenté** : [`docs/contrat-ws.md`](docs/contrat-ws.md) — paramètres, structures de réponse, règles de validation, erreurs SOAP (Fault), lecture du WSDL
- Opérations : `getClientDetails`, `searchClients` (nom / ville / statut + pagination), `updateClient` (mise à jour partielle)
- Santé : `http://localhost:8080/actuator/health`
- Métriques : `http://localhost:8080/actuator/prometheus`
- Scénarios de test exécutés avec succès : [`docs/tests-live-partie3.md`](docs/tests-live-partie3.md)

## Suivi d'avancement

- [x] **Partie 1** — Squelette Maven, configuration Spring Boot + CXF, architecture
- [x] **Partie 2** — Base de données : scripts SQL + jeu de données + flux NiFi
- [x] **Partie 3** — Service SOAP : `getClientDetails`, `searchClients`, `updateClient`, Faults
- [x] **Partie 4** — Docker Compose : MySQL, service, Prometheus, Grafana (installation reproductible)
- [x] **Partie 5** — Documentation WSDL, contrats, règles de validation, erreurs SOAP : [`docs/contrat-ws.md`](docs/contrat-ws.md)
- [ ] **Partie 6** — Métriques + tableau de bord Grafana
- [ ] **Partie 7** — Tests SoapUI (nominaux + erreurs) et scénario bout en bout
