# Scénario fonctionnel de bout en bout (livrable « tests obligatoires »)

> Le sujet exige « au minimum un scénario fonctionnel réussi, un test de bout en
> bout de la chaîne principale du projet ». Ce document est le **script de la
> démonstration** : il enchaîne la chaîne complète
> **fichier CSV → NiFi (extraction/validation/chargement) → MySQL → service SOAP →
> Grafana**, avec les résultats attendus à chaque étape. Idéal pour la soutenance.

## Préparation (machine propre)

```bash
git clone <url-du-depot> && cd DevComposant
docker compose down -v 2>/dev/null; docker compose up -d --build
```

Au premier démarrage : MySQL s'initialise seul (schéma + **20 clients**), le service
SOAP démarre, Prometheus scrape, Grafana provisionne source + dashboard.

Point de contrôle n° 0 :

| Contrôle | Attendu |
|---|---|
| `docker compose ps` | 4 services, MySQL et app `healthy` |
| `curl http://localhost:8080/actuator/health` | `{"status":"UP"}` |
| Dashboard Grafana — panneau « Clients en base » | **20** |

## Étape 1 — Chargement ETL par NiFi (extraction → validation → chargement)

Dans NiFi (préalablement installé — `docs/installation-nifi.md`), lancer le flux
documenté dans [`nifi/README-flux-nifi.md`](../nifi/README-flux-nifi.md), puis déposer
`nifi/data/input/clients_a_charger.csv` (16 enregistrements : 12 valides, 4 invalides).

| Contrôle | Attendu |
|---|---|
| `SELECT COUNT(*) FROM clients;` | **32** (20 + 12 chargées) |
| `SELECT id FROM clients WHERE id IN (33,34,35,36);` | **0 ligne** (rejets) |
| Répertoire `nifi/data/rejetes` | fichier contenant les **4** enregistrements invalides |
| Répertoire `nifi/data/traites` | fichier des **12** valides |
| Dashboard Grafana — « Clients en base » | passe de 20 à **32** (< 30 s) |

C'est la preuve de la chaîne **NiFi → MySQL → Grafana**.

## Étape 2 — Consommation du service SOAP (SoapUI)

Importer [`soapui/exo2-clients-soapui-project.xml`](../soapui/exo2-clients-soapui-project.xml)
et exécuter les deux suites — procédure détaillée : [`soapui/README-soapui.md`](../soapui/README-soapui.md).

| Contrôle | Attendu |
|---|---|
| Suite « Cas nominaux » (3 cas) | ✅ 3/3 verts (consultation, recherche paginée, mise à jour partielle) |
| Suite « Cas d'erreurs » (4 cas) | ✅ 4/4 verts (Faults `CLIENT_INTROUVABLE` et `DONNEES_INVALIDES` vérifiées par XPath) |
| Vérification d'effet en base | le client 5 a bien `ville = Marrakech`, `statut = INACTIF` |

## Étape 3 — Supervision (Grafana, en direct pendant la démo)

Dashboard : `http://localhost:3001/d/soap-clients` (rafraîchissement 5 s).

Lancer une suite SoapUI en boucle (ou rejouer quelques requêtes) et montrer :

| Panneau | Ce qui bouge |
|---|---|
| « Appels SOAP par seconde et par opération » | courbes getClientDetails / searchClients / updateClient |
| « Faults SOAP (total) » et « Taux de Faults » | augmentation lors de la suite d'erreurs |
| « Requêtes HTTP par statut » | série HTTP 500 = les Faults, HTTP 200 = les succès |
| « Latence HTTP p95 » | temps de réponse du 95ᵉ centile |
| « Santé du service » | EN LIGNE tant que le service tourne (couper `docker stop clients-soap` pour montrer le passage HORS LIGNE, puis `docker start clients-soap`) |

## Conclusion du scénario

La chaîne principale est démontrée de bout en bout : **données brutes → ETL avec
rejets contrôlés → stockage → service SOAP contractuel (avec erreurs métier
typées) → supervision temps réel**. Les mêmes étapes sont reproductibles sur une
machine neuve en suivant le README principal (une commande `docker compose up -d --build`).

## Réinitialisation entre deux démonstrations

```bash
docker compose down -v && docker compose up -d   # base réinitialisée (20 clients), pile redémarrée
```
