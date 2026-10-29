# Contrat du service SOAP `ClientService`


> structures de réponse, règles de validation, erreurs SOAP (Fault) et du WSDL.
> Exemples d'exécution réels : [`tests-live-partie3.md`](tests-live-partie3.md).

## 1. Accès au service

| Élément | Valeur |
|---|---|
| Endpoint SOAP | `POST http://localhost:8080/services/clients` |
| WSDL (publié) | `http://localhost:8080/services/clients?wsdl` |
| WSDL (copie versionnée) | [`ClientService.wsdl`](ClientService.wsdl) |
| Content-Type | `text/xml; charset=utf-8` |
| Encodage des échanges | UTF-8 (noms accentués supportés) |

Caractéristiques du contrat :

- **Style** : document/literal **bare** — chaque opération consomme un élément
  requête et produit un élément réponse (ex. `getClientDetailsRequest` /
  `getClientDetailsResponse`) ;
- **Espace de noms** : `http://ws.soapclients.ibam.com/` ;
- **Transport** : HTTP POST (SOAP 1.1) ;
- Le WSDL est **publié automatiquement** par Apache CXF à partir des annotations
  JAX-WS/JAXB du code (approche *code-first*) : le code est la source de vérité,
  la copie archivée sert de référence stable pour la documentation et SoapUI.

## 2. Type commun : `ClientDetails`

Représentation d'un client dans toutes les réponses.

| Champ | Type XML | Présence | Description |
|---|---|---|---|
| `id` | `xs:int` | requis | Identifiant technique du client |
| `nom` | `xs:string` | requis | Nom (≤ 100 caractères) |
| `prenom` | `xs:string` | requis | Prénom (≤ 100 caractères) |
| `email` | `xs:string` | requis | Adresse e-mail, unique dans le système |
| `telephone` | `xs:string` | optionnel | Numéro au format libre (≤ 20 caractères) |
| `ville` | `xs:string` | optionnel | Ville (≤ 80 caractères) |
| `pays` | `xs:string` | optionnel | Pays (≤ 80 caractères) |
| `statut` | `xs:string` | requis | `ACTIF` ou `INACTIF` |
| `dateCreation` | `xs:dateTime` | requis | ISO-8601 (ex. `2026-10-28T23:42:25`) |
| `dateModification` | `xs:dateTime` | requis | ISO-8601, actualisée à chaque modification |

## 3. Opérations

### 3.1 `getClientDetails` — consulter un client

Retourne la fiche complète du client identifié par `id`.

**Requête** (`getClientDetailsRequest`) :

| Paramètre | Type | Présence | Règle |
|---|---|---|---|
| `id` | `xs:int` | requis | Entier strictement positif |

**Réponse** (`getClientDetailsResponse`) :

| Champ | Type | Description |
|---|---|---|
| `client` | `ClientDetails` | Fiche complète du client |

**Erreurs possibles** : `DONNEES_INVALIDES` (identifiant absent ou ≤ 0),
`CLIENT_INTROUVABLE`, `ERREUR_INTERNE`.

### 3.2 `searchClients` — rechercher des clients

Recherche paginée avec filtres **optionnels** combinés par « ET ».

**Requête** (`searchClientsRequest`) :

| Paramètre | Type | Présence | Sémantique |
|---|---|---|---|
| `nom` | `xs:string` | optionnel | Contient (insensible à la casse) |
| `ville` | `xs:string` | optionnel | Contient (insensible à la casse) |
| `statut` | `xs:string` | optionnel | Égalité : `ACTIF` ou `INACTIF` (casse indifférente) |
| `page` | `xs:int` | optionnel | Numéro de page, ≥ 0, **défaut 0** |
| `taillePage` | `xs:int` | optionnel | Taille de page, 1 à 100, **défaut 20** (bornée à 100) |

**Réponse** (`searchClientsResponse`) :

| Champ | Type | Description |
|---|---|---|
| `client` | `ClientDetails` (0..n) | Page de résultats, triée par `id` croissant |
| `total` | `xs:long` | Nombre total de correspondances, toutes pages confondues |
| `page` | `xs:int` | Numéro de la page renvoyée |
| `taillePage` | `xs:int` | Taille effectivement appliquée |

Cas particuliers : une recherche **sans résultat n'est pas une erreur** (liste vide,
`total = 0`) ; sans aucun filtre, retourne l'ensemble des clients paginés.

**Erreurs possibles** : `DONNEES_INVALIDES` (statut hors énumération), `ERREUR_INTERNE`.

### 3.3 `updateClient` — mettre à jour un client

Mise à jour **partielle** : seuls les champs fournis sont modifiés.

**Requête** (`updateClientRequest`) :

| Paramètre | Type | Présence | Sémantique |
|---|---|---|---|
| `id` | `xs:int` | requis | Identifie le client à modifier (entier > 0) |
| `nom` | `xs:string` | optionnel | `null` (champ absent) = inchangé |
| `prenom` | `xs:string` | optionnel | idem |
| `email` | `xs:string` | optionnel | Validé + contrôle d'unicité s'il change |
| `telephone` | `xs:string` | optionnel | idem |
| `ville` | `xs:string` | optionnel | idem |
| `pays` | `xs:string` | optionnel | idem |
| `statut` | `xs:string` | optionnel | `ACTIF` / `INACTIF`, normalisé en majuscules |

**Réponse** (`updateClientResponse`) :

| Champ | Type | Description |
|---|---|---|
| `client` | `ClientDetails` | État complet **après** modification (`dateModification` actualisée) |

Sémantique « champ absent = inchangé » : envoyer une valeur **vide** (`""` ou
espaces) est une **erreur** (on ne peut pas vider un champ via ce contrat) ; pour
ne pas modifier un champ, omettre l'élément.

**Erreurs possibles** : `DONNEES_INVALIDES` (toutes les violations listées),
`CLIENT_INTROUVABLE`, `ERREUR_INTERNE`.

## 4. Règles de validation

Appliquées côté serveur (`ClientService`) — le client ne peut pas se reposer sur
une validation côté appelant.

| Donnée | Règle | En cas de violation |
|---|---|---|
| `id` (toutes opérations) | Obligatoire, entier strictement positif | Fault `DONNEES_INVALIDES` |
| `nom`, `prenom` (update) | Si fournis : non vides après trim, ≤ 100 caractères | Fault listant le champ |
| `email` (update) | Si fourni : respecte `^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)+$`, ≤ 150 caractères, **unique** (comparaison insensible à la casse, hors client courant) | Fault listant le champ |
| `telephone` (update) | Si fourni : non vide, ≤ 20 caractères | Fault listant le champ |
| `ville`, `pays` (update) | Si fournis : non vides, ≤ 80 caractères | Fault listant le champ |
| `statut` (update + search) | `ACTIF` ou `INACTIF`, insensible à la casse | Fault listant le champ |
| Pagination (search) | `page` ≥ 0 (défaut 0) ; `taillePage` 1..100 (défaut 20) | Valeurs normalisées silencieusement |
| Recherche texte | Les jokers SQL (`%`, `_`, `\`) saisis sont neutralisés | La recherche reste littérale |

Toutes les violations détectées en une passe sont renvoyées **dans une seule
Fault** (éléments `erreur` répétés), pour que l'appelant corrige tout d'un coup.

## 5. Erreurs SOAP (Faults)

Toute erreur métier est transportée par une Fault SOAP dont le bloc `detail`
contient un élément `serviceFault` :

```xml
<soap:Fault>
  <faultcode>soap:Server</faultcode>
  <faultstring>Aucun client ne correspond à l'identifiant 999</faultstring>
  <detail>
    <ns2:serviceFault xmlns:ns2="http://ws.soapclients.ibam.com/">
      <code>CLIENT_INTROUVABLE</code>
      <message>Aucun client ne correspond à l'identifiant 999</message>
      <erreur>…détail champ par champ (validation)…</erreur>
    </ns2:serviceFault>
  </detail>
</soap:Fault>
```

Codes fonctionnels **stables** (à utiliser côté client pour la gestion d'erreurs) :

| Code | Signification | Quand |
|---|---|---|
| `CLIENT_INTROUVABLE` | Aucun client pour l'identifiant fourni | `getClientDetails`, `updateClient` |
| `DONNEES_INVALIDES` | Paramètre(s) en violation des règles ci-dessus | les trois opérations |
| `ERREUR_INTERNE` | Incident inattendu côté serveur | les trois opérations |

Choix de sécurité (OWASP — gestion sécurisée des erreurs) : les Faults
`ERREUR_INTERNE` sont volontairement **génériques** (aucune stack trace, aucun
détail d'infrastructure divulgué) ; le détail technique est journalisé côté
serveur uniquement.

## 6. Le WSDL, pièce par pièce

Copie archivée : [`ClientService.wsdl`](ClientService.wsdl) (publiée en direct
sur `…?wsdl`). Structure :

| Section WSDL | Contenu |
|---|---|
| `wsdl:types` (schéma XML) | Éléments globaux `getClientDetailsRequest/Response`, `searchClientsRequest/Response`, `updateClientRequest/Response`, `serviceFault` ; types complexes `ClientDetails`, `ServiceFault` et les types de requêtes/réponses |
| `wsdl:message` | Un message par requête et par réponse, plus un message par Fault : `ClientIntrouvableException`, `DonneesInvalidesException`, `ErreurInterneException` (tous portant l'élément `serviceFault`) |
| `wsdl:portType` `ClientService` | Les 3 opérations avec leurs entrées/sorties/Faults |
| `wsdl:binding` | `ClientServiceSoapBinding` : `document` / `literal` (SOAP 1.1) |
| `wsdl:service` `ClientService` | Port `ClientServiceWsImplPort` → `/services/clients` |

Consommer le service : créer un projet SoapUI à partir de **l'URL du WSDL**
(fichier ou `http://localhost:8080/services/clients?wsdl`) ; SoapUI génère les
enveloppes de chaque opération, Faults comprises. Exemples prêts à rejouer :
[`tests-live-partie3.md`](tests-live-partie3.md).

## 7. Limites et garanties opérationnelles

- Charge : pagination plafonnée à 100 éléments par page ; pool de connexions
  dimensionné (HikariCP, 10 connexions max).
- Requêtes exclusivement **paramétrées** (JPA/Criteria) : aucune concaténation SQL.
- Parseur XML entrant durci : DTD et entités externes interdites (anti-XXE),
  limites de profondeur/taille (anti-bombe XML).
- Opérations **idempotentes** en lecture (`getClientDetails`, `searchClients`) ;
  `updateClient` est réexécutable (dernier état écrit conservé).
