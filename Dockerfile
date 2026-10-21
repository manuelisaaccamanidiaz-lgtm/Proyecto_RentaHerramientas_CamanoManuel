# syntax=docker/dockerfile:1
#
# La línea anterior es una "directiva del parser" (no un comentario normal): debe ser la
# PRIMERA línea del archivo. Pide a BuildKit la sintaxis estable más reciente del Dockerfile,
# necesaria para RUN --mount=type=cache (más abajo).
#
# Imagen de RentaHerramientas en dos etapas:
#   1. build   → compila el proyecto multimódulo con Maven + JDK 21 y separa el jar en capas.
#   2. runtime → solo JRE 21, usuario sin privilegios y healthcheck sobre /actuator/health.
#
# Multi-stage: la etapa build (Maven, JDK, código fuente, caché) se descarta al final; la
# imagen publicada solo contiene lo que se copia explícitamente con COPY --from=build.
# Resultado: imagen más pequeña y sin herramientas de compilación ni código fuente.
#
# Uso normal: docker compose up --build   (ver DESPLIEGUE.md)
#
# Nota: en un Dockerfile los comentarios solo pueden ir en líneas completas que empiecen
# por "#"; un "#" al final de una instrucción se toma como parte de ella.

# ============================================================== 1. build
# "AS build" da nombre a la etapa para poder copiar desde ella en la etapa 2.
# La imagen tiene variante arm64, así que compila de forma nativa en Apple Silicon.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Se copian los POM de cada módulo y luego el código fuente. Lo que no aparece aquí
# (target/, .env, docs, .git…) tampoco llega al contexto de build gracias a .dockerignore.
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
#
# Detalle de cada parte:
#   --mount=type=cache   requiere BuildKit (activo por defecto en Docker Desktop y en
#                        "docker compose" v2). La caché NO queda dentro de la imagen.
#   mvn -B -ntp          -B: modo batch (sin preguntas interactivas); -ntp: no imprime el
#                        progreso de cada descarga, para un log más limpio.
#   -DskipTests          las pruebas no se ejecutan aquí: las de integración necesitan
#                        Testcontainers (Docker dentro del build), se corren con mvn verify.
#   -Djarmode=tools ... extract --layers --launcher
#                        herramienta de Spring Boot que descomprime el jar en carpetas
#                        (una por capa) y deja listo el lanzador JarLauncher.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests package \
 && cp bootstrap/target/bootstrap-*.jar app.jar \
 && java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extraido

# ============================================================== 2. runtime
# Solo el JRE (sin compilador ni Maven): menos tamaño y menos superficie de ataque.
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuario sin privilegios y directorio de imágenes subidas (se monta como volumen).
# uid/gid fijos (10001) para que los permisos del volumen sean predecibles; nologin
# impide abrir una shell interactiva con ese usuario. /uploads se crea y se le asigna
# al usuario antes de declararlo VOLUME, así el volumen nuevo hereda ese propietario.
RUN groupadd --system --gid 10001 renta \
 && useradd --system --uid 10001 --gid renta --home-dir /app --shell /usr/sbin/nologin renta \
 && mkdir -p /uploads \
 && chown renta:renta /uploads

# Capas de la menos a la más cambiante: dependencias, cargador de Spring Boot,
# módulos SNAPSHOT del proyecto y, por último, las clases de la aplicación.
# Docker reutiliza una capa si no cambió: al modificar solo el código de la app se
# reconstruye y se transfiere únicamente la última capa, no las dependencias.
# --chown evita un RUN chown posterior (que duplicaría el tamaño de esas capas).
COPY --from=build --chown=renta:renta /build/extraido/dependencies/ ./
COPY --from=build --chown=renta:renta /build/extraido/spring-boot-loader/ ./
COPY --from=build --chown=renta:renta /build/extraido/snapshot-dependencies/ ./
COPY --from=build --chown=renta:renta /build/extraido/application/ ./

# A partir de aquí todo (incluido el proceso Java) se ejecuta sin privilegios de root.
USER renta

# Valores por defecto; docker-compose.yml los sobrescribe con lo que venga del .env.
#   -XX:MaxRAMPercentage=75   la JVM usa como máximo el 75 % de la memoria asignada al
#                             contenedor (no de la del equipo anfitrión).
#   -Duser.timezone           zona horaria de Colombia para fechas de reservas y facturas.
# JAVA_TOOL_OPTIONS la lee la JVM automáticamente, sin cambiar el ENTRYPOINT.
ENV SPRING_PROFILES_ACTIVE=dev \
    UPLOADS_DIR=/uploads \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Duser.timezone=America/Bogota"

# EXPOSE solo documenta el puerto interno; quien lo publica en el host es "ports" en
# docker-compose.yml (APP_PORT).
EXPOSE 8080
# Declara /uploads como volumen: las imágenes subidas no se pierden al recrear el
# contenedor. En compose se monta el volumen con nombre uploads_data.
VOLUME ["/uploads"]

# start-period: margen para que Flyway migre la base en el primer arranque.
# Docker ejecuta este comando dentro del contenedor cada 10 s: si curl falla 5 veces
# seguidas el contenedor pasa a "unhealthy". -f hace que curl falle con respuestas HTTP de
# error (p. ej. 503 cuando la app está DOWN); -sS lo deja silencioso salvo errores.
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=5 \
    CMD curl -fsS http://localhost:8080/actuator/health || exit 1

# Forma "exec" (array JSON): Java es el proceso principal (PID 1) y recibe directamente
# la señal de "docker compose down", lo que permite un apagado ordenado. JarLauncher
# arranca la aplicación a partir de las capas extraídas en /app.
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
