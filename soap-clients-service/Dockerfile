# =====================================================================
# Image du service SOAP — build multi-étapes (reproductible)
#   1. compilation avec Maven officiel (JDK 17)
#   2. image d'exécution minimale (JRE 17), utilisateur non-root
# =====================================================================

# ---------- Étape 1 : build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copie du seul pom.xml : la couche de dépendances est mise en cache
# et ne se reconstruit que si le pom change.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src src
RUN mvn -B -q clean package -DskipTests

# ---------- Étape 2 : exécution ----------
FROM eclipse-temurin:17-jre-jammy

# curl est utilisé par le healthcheck Docker.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /build/target/*.jar app.jar

# Principe du moindre privilège : le service ne tourne pas en root.
RUN useradd --system --create-home --shell /usr/sbin/nologin appuser
USER appuser

EXPOSE 8080

# MaxRAMPercentage : la JVM respecte la mémoire allouée au conteneur.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
