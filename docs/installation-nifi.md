# Installation d'Apache NiFi (procédure distincte)

Le sujet exige une procédure spécifique pour les outils à installation particulière.
Deux options : **Docker (recommandé)** ou installation locale.

> Prérequis mémoire : NiFi a besoin d'au moins **2 Go de RAM** disponibles pour la JVM.
> Adapter `-Xms/-Xmx` dans `conf/bootstrap.conf` si nécessaire.

## Option A — Docker (recommandé)

```bash
docker run --name nifi -d \
  -p 8443:8443 \
  -e NIFI_WEB_HTTPS_PORT=8443 \
  -v "$(pwd)/nifi/data:/opt/nifi/nifi-current/data/clients" \
  -v "$(pwd)/nifi/lib:/opt/nifi/nifi-current/lib_extra" \
  apache/nifi:1.28.1
```

- Interface : `https://localhost:8443/nifi`
- Identifiants générés au premier démarrage : `docker logs nifi 2>&1 | grep -i "generated"`
  (lignes `Username:` / `Password:`)
- Le pilote MySQL et le répertoire `data/` du dépôt sont montés dans le conteneur.

Dans ce mode, pour le service `DBCP_MySQL` et le répertoire d'entrée :

- Input Directory : `/opt/nifi/nifi-current/data/clients/input`
- URL JDBC : `jdbc:mysql://mysql:3306/clientsdb?...` si NiFi est attaché au réseau du
  compose (Partie 4), sinon `host.docker.internal:3306`.

## Option B — Installation locale

1. Télécharger NiFi 1.x (ex. `nifi-1.28.1-bin.zip`) sur https://nifi.apache.org/download ;
2. Décompresser, puis vérifier `conf/bootstrap.conf` (mémoire JVM ≥ 2 Go) ;
3. Démarrer :
   - Linux/macOS : `bin/nifi.sh start`
   - Windows : `bin\nifi.cmd start` (ou `run` pour la console) ;
4. Interface : `https://localhost:8443/nifi` — identifiants auto-générés dans
   `logs/nifi-app.log` (chercher `Generated Authentication Token` / `Username/Password`) ;
5. Arrêt : `bin/nifi.sh stop`.

## Pilote JDBC MySQL (obligatoire dans les deux options)

`PutDatabaseRecord` a besoin du pilote MySQL dans le classpath de NiFi :

1. Télécharger `mysql-connector-j-8.4.0.jar` (ou toute version 8.x) :
   https://dev.mysql.com/downloads/connector/j/ → « Platform Independent » ;
2. Copier le JAR dans le répertoire `lib/` de NiFi (Option B) ou dans le répertoire monté
   `nifi/lib/` du dépôt (Option A — le montage l'expose à NiFi) ;
3. Redémarrer NiFi.

## Construction du flux

Une fois NiFi démarré : suivre `nifi/README-flux-nifi.md` — tous les processeurs,
services et propriétés y sont décrits avec leurs valeurs exactes.
