# Tests du service SOAP — résultats obtenus (Partie 3)

> Environnement de test : MySQL 8 (Docker, initialisé par `db/*.sql` — 20 clients),
> service démarré via `./mvnw spring-boot:run`, requêtes envoyées avec `curl`
> sur `http://localhost:8080/services/clients`. Le WSDL publié est archivé dans
> [`ClientService.wsdl`](ClientService.wsdl).

Résumé — **7/7 scénarios réussis** :

| # | Scénario | Résultat attendu | Résultat obtenu |
|---|---|---|---|
| 1 | `getClientDetails` id=1 | Fiche complète du client 1 | ✅ 200 — fiche avec dates ISO-8601 |
| 2 | `getClientDetails` id=999 | Fault `CLIENT_INTROUVABLE` | ✅ Fault + `<detail>` `serviceFault` |
| 3 | `searchClients` nom=« ndiaye », taille 5 | 1 client, pagination | ✅ total=1, page=0, taillePage=5 |
| 4 | `searchClients` statut=« SUSPENDU » | Fault `DONNEES_INVALIDES` | ✅ Fault « valeurs admises ACTIF, INACTIF » |
| 5 | `updateClient` id=5 (ville + statut minuscule) | MAJ partielle, statut normalisé | ✅ ville=Marrakech, statut=INACTIF, `dateModification` actualisée, autres champs inchangés |
| 6 | `updateClient` id=5 (email invalide + nom vide) | Fault listant **chaque** champ en erreur | ✅ Fault avec 2 `<erreur>` détaillées |
| 7 | `updateClient` id=5 (email déjà utilisé) | Fault unicité | ✅ Fault « déjà utilisée par un autre client » |

## Enveloppes SOAP utilisées

### 1. getClientDetails — cas nominal (id = 1)

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ws="http://ws.soapclients.ibam.com/">
  <soapenv:Body>
    <ws:getClientDetailsRequest><id>1</id></ws:getClientDetailsRequest>
  </soapenv:Body>
</soapenv:Envelope>
```

Réponse (extrait) :

```xml
<ns2:getClientDetailsResponse xmlns:ns2="http://ws.soapclients.ibam.com/">
  <client>
    <id>1</id>
    <nom>Ndiaye</nom>
    <prenom>Amadou</prenom>
    <email>amadou.ndiaye@example.com</email>
    <telephone>+221 77 101 01 01</telephone>
    <ville>Dakar</ville>
    <pays>Sénégal</pays>
    <statut>ACTIF</statut>
    <dateCreation>2026-10-28T23:42:25.945762</dateCreation>
    <dateModification>2026-10-28T23:42:25.945762</dateModification>
  </client>
</ns2:getClientDetailsResponse>
```

### 2. getClientDetails — client introuvable (id = 999)

Requête : `<ws:getClientDetailsRequest><id>999</id></ws:getClientDetailsRequest>`

Réponse :

```xml
<soap:Fault>
  <faultcode>soap:Server</faultcode>
  <faultstring>Aucun client ne correspond à l'identifiant 999</faultstring>
  <detail>
    <ns2:serviceFault xmlns:ns2="http://ws.soapclients.ibam.com/">
      <code>CLIENT_INTROUVABLE</code>
      <message>Aucun client ne correspond à l'identifiant 999</message>
    </ns2:serviceFault>
  </detail>
</soap:Fault>
```

### 3. searchClients — recherche par nom, paginée

```xml
<ws:searchClientsRequest>
  <nom>ndiaye</nom>
  <taillePage>5</taillePage>
</ws:searchClientsRequest>
```

Réponse (extrait) : client n°1 (Ndiaye Amadou) + `total=1`, `page=0`, `taillePage=5`.

### 4. searchClients — statut hors énumération

```xml
<ws:searchClientsRequest><statut>SUSPENDU</statut></ws:searchClientsRequest>
```

Réponse : Fault `DONNEES_INVALIDES` — « Statut invalide 'SUSPENDU' : valeurs admises ACTIF, INACTIF ».

### 5. updateClient — mise à jour partielle + normalisation

```xml
<ws:updateClientRequest>
  <id>5</id>
  <ville>Marrakech</ville>
  <statut>inactif</statut>
</ws:updateClientRequest>
```

Réponse (extrait) : `ville=Marrakech`, `statut=INACTIF` (normalisé en majuscules),
`nom` inchangé, `dateModification` postérieure à `dateCreation`.

### 6. updateClient — plusieurs erreurs de validation d'un coup

```xml
<ws:updateClientRequest>
  <id>5</id>
  <email>mauvais-format</email>
  <nom></nom>
</ws:updateClientRequest>
```

Réponse : Fault `DONNEES_INVALIDES` avec le détail **champ par champ** :

```xml
<erreur>nom : une valeur vide n'est pas admise (omettre le champ pour laisser inchangé)</erreur>
<erreur>email : le format 'mauvais-format' n'est pas une adresse valide</erreur>
```

### 7. updateClient — unicité de l'e-mail

```xml
<ws:updateClientRequest>
  <id>5</id>
  <email>fatou.diallo@example.com</email>
</ws:updateClientRequest>
```

Réponse : Fault `DONNEES_INVALIDES` — « l'adresse 'fatou.diallo@example.com' est déjà
utilisée par un autre client ».

## Reproduire ces tests

```bash
# 1. MySQL (initialisé automatiquement par db/*.sql au premier démarrage)
docker run -d --name clientsdb -e MYSQL_ROOT_PASSWORD=rootpass \
  -e MYSQL_DATABASE=clientsdb -e MYSQL_USER=soapapp -e MYSQL_PASSWORD=soappass \
  -p 3306:3306 -v "$(pwd)/db:/docker-entrypoint-initdb.d:ro" mysql:8

# 2. Service
cd soap-clients-service && ./mvnw spring-boot:run

# 3. Vérifier le WSDL puis envoyer les enveloppes ci-dessus
curl -s "http://localhost:8080/services/clients?wsdl"
curl -s -X POST http://localhost:8080/services/clients \
  -H "Content-Type: text/xml; charset=utf-8" --data @ma-requete.xml
```

Les mêmes scénarios sont rejoués dans SoapUI — projet fourni à la partie 7.
