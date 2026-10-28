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

## Installation

```bash
# 1. Base de données (création du schéma + jeu de données fictives)
mysql -u root -p < db/01_schema.sql
mysql -u root -p < db/02_donnees_test.sql

# 2. Environnement complet ( fourni à la partie Docker Compose)
docker compose up -d          # démarrera MySQL, Prometheus et Grafana

# 3. Service SOAP
cd soap-clients-service
./mvnw spring-boot:run        # démarre le service (WSDL à la partie 3)
```

Le flux ETL NiFi (extraction → validation → chargement) est documenté pas à pas dans
[`nifi/README-flux-nifi.md`](nifi/README-flux-nifi.md) — installation de NiFi :
[`docs/installation-nifi.md`](docs/installation-nifi.md).

## Utilisation

- WSDL : `http://localhost:8080/services/clients?wsdl` (publié à la partie 3)
- Santé : `http://localhost:8080/actuator/health`
- Métriques : `http://localhost:8080/actuator/prometheus`

## Suivi d'avancement

- [x] **Partie 1** — Squelette Maven, configuration Spring Boot + CXF, architecture
- [x] **Partie 2** — Base de données : scripts SQL + jeu de données + flux NiFi
- [ ] **Partie 3** — Service SOAP : `getClientDetails`, `searchClients`, `updateClient`, Faults
- [ ] **Partie 4** — Docker Compose : MySQL, Prometheus, Grafana (installation reproductible)
- [ ] **Partie 5** — Documentation WSDL, contrats, règles de validation, erreurs SOAP
- [ ] **Partie 6** — Métriques + tableau de bord Grafana
- [ ] **Partie 7** — Tests SoapUI (nominaux + erreurs) et scénario bout en bout
