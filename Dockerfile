# syntax=docker/dockerfile:1
#
# Imagen de RentaHerramientas en dos etapas:
#   1. build   → compila el proyecto multimódulo con Maven + JDK 21 y separa el jar en capas.
#   2. runtime → solo JRE 21, usuario sin privilegios y healthcheck sobre /actuator/health.
#
# Uso normal: docker compose up --build   (ver DESPLIEGUE.md)

# ============================================================== 1. build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
COPY domain/pom.xml domain/
COPY application/pom.xml application/
COPY infrastructure/pom.xml infrastructure/
COPY frontend/pom.xml frontend/
COPY bootstrap/pom.xml bootstrap/
COPY domain/src domain/src
COPY application/src application/src
COPY infrastructure/src infrastructure/src
COPY frontend/src frontend/src
COPY bootstrap/src bootstrap/src

# Caché de dependencias: el repositorio local de Maven (/root/.m2) vive en una caché
# de BuildKit que sobrevive entre builds, así solo se descarga lo que sea nuevo.
# El jar se separa en capas (dependencias / cargador / módulos / aplicación).
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests package \
 && cp bootstrap/target/bootstrap-*.jar app.jar \
 && java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extraido

# ============================================================== 2. runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuario sin privilegios y directorio de imágenes subidas (se monta como volumen).
RUN groupadd --system --gid 10001 renta \
 && useradd --system --uid 10001 --gid renta --home-dir /app --shell /usr/sbin/nologin renta \
 && mkdir -p /uploads \
 && chown renta:renta /uploads

# Capas de la menos a la más cambiante: dependencias, cargador de Spring Boot,
# módulos SNAPSHOT del proyecto y, por último, las clases de la aplicación.
COPY --from=build --chown=renta:renta /build/extraido/dependencies/ ./
COPY --from=build --chown=renta:renta /build/extraido/spring-boot-loader/ ./
COPY --from=build --chown=renta:renta /build/extraido/snapshot-dependencies/ ./
COPY --from=build --chown=renta:renta /build/extraido/application/ ./

USER renta

ENV SPRING_PROFILES_ACTIVE=dev \
    UPLOADS_DIR=/uploads \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Duser.timezone=America/Bogota"

EXPOSE 8080
VOLUME ["/uploads"]

# start-period: margen para que Flyway migre la base en el primer arranque.
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=5 \
    CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
