# Tests SoapUI — Exercice 2 (étape 7 du travail demandé)

Projet SoapUI versionné : **[`exo2-clients-soapui-project.xml`](exo2-clients-soapui-project.xml)**
— 2 suites, 7 cas de test, assertions automatiques (réponse SOAP, présence/absence de
Fault, XPath sur les champs). Tous les scénarios ont été préalablement validés en
exécution réelle : voir [`docs/tests-live-partie3.md`](../docs/tests-live-partie3.md).

## Importer et exécuter (2 minutes)

1. Démarrer l'environnement : `docker compose up -d --build` (le service doit être
   joignable sur `http://localhost:8080/services/clients`) ;
2. Ouvrir SoapUI → **File → Import Project** → sélectionner
   `soapui/exo2-clients-soapui-project.xml` ;
3. Double-clic sur une suite → onglet **TestSuite** → bouton **Run** (vert) ;
4. Tous les cas doivent passer (icone verte). Relancer autant de fois que voulu :
   les scénarios sont réexécutables (l'`updateClient` de test réapplique les mêmes
   valeurs).

> SoapUI relit le WSDL à l'import : le service doit être **démarré**. Si l'import
> signalait un souci de format selon votre version de SoapUI, reconstruire le projet
> en 5 minutes avec la procédure manuelle ci-dessous — les enveloppes et les
> assertions attendues y sont données intégralement.

## Suites et résultats attendus

### Suite « Cas nominaux »

| Cas de test | Requête | Assertions | Résultat attendu |
|---|---|---|---|
| `getClientDetails id=1 (succes)` | `<id>1</id>` | nom = `Ndiaye`, statut = `ACTIF`, pas de Fault | ✅ fiche complète du client 1 |
| `searchClients nom=ndiaye` | nom + taillePage=5 | total = `1`, pas de Fault | ✅ 1 client, pagination cohérente |
| `updateClient id=5 (maj partielle)` | ville=`Marrakech`, statut=`inactif` | ville = `Marrakech`, statut = `INACTIF` (normalisé) | ✅ champs absents inchangés |

### Suite « Cas d'erreurs (Faults) »

| Cas de test | Requête | Assertions | Résultat attendu |
|---|---|---|---|
| `getClientDetails id=999` | id inexistant | Fault + code = `CLIENT_INTROUVABLE` | ✅ Fault avec `serviceFault` |
| `searchClients statut=SUSPENDU` | statut hors enum | Fault + code = `DONNEES_INVALIDES` | ✅ message « valeurs admises ACTIF, INACTIF » |
| `updateClient donnees invalides` | email=`mauvais-format`, nom vide | Fault + **2** éléments `erreur` | ✅ toutes les violations listées d'un coup |
| `updateClient email deja utilise` | email du client 2 | Fault + faultstring « déjà utilisée par un autre client » | ✅ unicité vérifiée en base |

## Procédure manuelle équivalente (si l'import posait problème)

1. SoapUI → **File → New SOAP Project** → Initial WSDL :
   `http://localhost:8080/services/clients?wsdl` (cocher « Create TestSuite ») ;
2. Pour chaque cas ci-dessus : ouvrir la requête de l'opération, coller l'enveloppe
   correspondante (elles figurent toutes dans
   [`docs/tests-live-partie3.md`](../docs/tests-live-partie3.md)), puis cliquer **Send** ;
3. Ajouter les assertions : clic droit sur le nœud de la requête → **Add Assertion** →
   « SOAP Response », « Not SOAP Fault » (nominaux) ou « SOAP Fault » (erreurs), puis
   « XPath Match » avec les chemins du tableau ci-dessus ;
4. Sauvegarder le projet dans `soapui/` pour le dépôt Git.
