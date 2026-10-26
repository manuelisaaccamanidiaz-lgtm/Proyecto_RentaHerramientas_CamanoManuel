# 🔧 Compilación

Guía para compilar RentaHerramientas, ejecutar sus pruebas y generar el artefacto.
Para levantar la aplicación con Docker ver [DESPLIEGUE.md](DESPLIEGUE.md).

## 1. Requisitos

| Herramienta | Versión | Para qué | Comprobar |
|---|---|---|---|
| JDK | **21** (o superior compilando con `--release 21`) | Compilar y ejecutar | `java -version` |
| Maven | **3.9+** | Construir el proyecto multimódulo | `mvn -v` |
| Docker Desktop / Engine | 24+ con Compose v2 | PostgreSQL local, pruebas de integración (Testcontainers) e imagen de la app | `docker version` · `docker compose version` |
| Git | cualquiera | Clonar el repositorio | `git --version` |

> 💡 El proyecto fija `java.version=21`: Maven compila con `--release 21`, así que un
> JDK 22+ también sirve para compilar. El jar resultante corre en cualquier JRE 21+.

## 2. Estructura del build

```
proyecto-rentaherramientas/         POM padre (hereda de spring-boot-starter-parent 4.0.8)
├── domain/          → domain-1.0-SNAPSHOT.jar          Java puro
├── application/     → application-1.0-SNAPSHOT.jar     casos de uso
├── infrastructure/  → infrastructure-1.0-SNAPSHOT.jar  adaptadores Spring
├── frontend/        → frontend-1.0-SNAPSHOT.jar        solo recursos estáticos
└── bootstrap/       → bootstrap-1.0-SNAPSHOT.jar       ⭐ jar ejecutable (Spring Boot)
```

Maven construye los módulos en ese orden (reactor). El único artefacto que se despliega es
`bootstrap/target/bootstrap-1.0-SNAPSHOT.jar`, que incluye a los demás.

## 3. Compilar

```bash
mvn clean compile                 # compila los 5 módulos
mvn clean install -DskipTests     # compila, empaqueta e instala en ~/.m2 (sin pruebas)
```

## 4. Ejecutar las pruebas

| Comando | Qué ejecuta | ¿Necesita Docker? |
|---|---|---|
| `mvn test` | 99 pruebas **unitarias** (surefire): dominio y casos de uso | No |
| `mvn verify` | Unitarias + 5 de **integración** (failsafe, clases `*IntegrationTest`) | **Sí** |
| `mvn -pl bootstrap -am verify -Dtest=none -Dsurefire.failIfNoSpecifiedTests=false` | Solo integración | Sí |
| `mvn -pl domain test -Dtest=ReservaTest` | Una clase concreta | No |

Las pruebas de integración levantan un `postgres:17` con **Testcontainers**; Spring Boot
toma la conexión con `@ServiceConnection`. No usan tu base local ni tu `.env`.

Reportes:

- Unitarias: `domain/target/surefire-reports/`, `application/target/surefire-reports/`
- Integración: `bootstrap/target/failsafe-reports/`

## 5. Generar el artefacto

```bash
mvn clean verify                  # recomendado: empaqueta y pasa TODAS las pruebas
# o, más rápido:
mvn clean package -DskipTests
```

Resultado: `bootstrap/target/bootstrap-1.0-SNAPSHOT.jar` (jar ejecutable, ~70 MB con
Tomcat embebido). Ejecutarlo desde la raíz del proyecto:

```bash
java -jar bootstrap/target/bootstrap-1.0-SNAPSHOT.jar
# con otro perfil:
java -jar bootstrap/target/bootstrap-1.0-SNAPSHOT.jar --spring.profiles.active=prod
```

Necesita una base PostgreSQL accesible (por ejemplo `docker compose up -d postgres`) y las
variables del `.env`. Para la imagen Docker no hace falta compilar antes: el `Dockerfile`
compila dentro de su etapa de build.

## 6. Solución de problemas comunes

### 🚫 Puerto 8080 ocupado

Síntoma: `Web server failed to start. Port 8080 was already in use.`

```powershell
# Windows: ver qué proceso usa el puerto y detenerlo
Get-NetTCPConnection -LocalPort 8080 -State Listen | Select-Object OwningProcess
Stop-Process -Id <PID>
```

```bash
# Linux / macOS
lsof -i :8080        # luego: kill <PID>
```

Suele ser otra instancia de la app o el contenedor `rentaherramientas-app`
(`docker compose stop app`). Alternativas: arrancar en otro puerto con
`--server.port=8081`, o en Docker cambiar `APP_PORT=8081` en `.env`.

### 🐳 Docker apagado

Síntomas:

- `docker compose`: `Cannot connect to the Docker daemon` /
  `open //./pipe/dockerDesktopLinuxEngine: The system cannot find the file specified`.
- `mvn verify`: `IllegalStateException: Could not find a valid Docker environment`
  en las clases `*IntegrationTest`.

Solución: abre **Docker Desktop** y espera a que indique *Engine running*
(`docker info` debe responder). Si solo necesitas compilar, usa `mvn test` o
`-DskipTests`, que no requieren Docker.

### ☕ Java distinto de 21

Síntomas:

- Con JDK 17 o anterior: `error: release version 21 not supported` o
  `invalid target release: 21`.
- `mvn -v` muestra otro `Java version` que `java -version`: Maven usa el JDK de `JAVA_HOME`.

Solución: instala un JDK 21 (por ejemplo Eclipse Temurin 21) y apunta `JAVA_HOME` a él:

```powershell
# Windows (sesión actual)
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21..."
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

```bash
# Linux / macOS
export JAVA_HOME=/ruta/al/jdk-21 && export PATH="$JAVA_HOME/bin:$PATH"
```

Comprueba que `mvn -v` muestre `Java version: 21` (o superior).

### 🧮 Checksum de Flyway

Síntoma al arrancar:

```
Validate failed: Migrations have failed validation
Migration checksum mismatch for migration version 3
-> Applied to database : 123456789
-> Resolved locally    : 987654321
```

Causa: se **editó** un archivo `V<n>__*.sql` que ya estaba aplicado en esa base. Flyway
guarda el checksum de cada migración en `flyway_schema_history` y no permite cambiarlas.

Solución:

- ✅ **Lo correcto:** no modificar migraciones aplicadas; crear una nueva (`V7__…sql`) con el cambio.
- 🧹 **En desarrollo**, si quieres empezar de cero (borra todos los datos):
  `docker compose down -v && docker compose up --build` (o `docker compose up -d postgres`
  para la ejecución sin Docker de la app).
- 🔁 Si el cambio fue solo de formato o comentarios y la base es de desarrollo, se puede
  ejecutar `flyway repair` (o borrar la base) para recalcular los checksums. **Nunca** en producción.

### Otros

| Síntoma | Causa probable | Solución |
|---|---|---|
| `Connection to localhost:5432 refused` | PostgreSQL no está corriendo | `docker compose up -d postgres` y revisar `POSTGRES_*` en `.env` |
| `password authentication failed for user` | El volumen se creó con otra contraseña | Usar la contraseña original o `docker compose down -v` (borra datos) |
| `app.jwt.secret (JWT_SECRET) debe tener al menos 32 bytes` | `JWT_SECRET` vacío o corto | Generar uno con `openssl rand -base64 48` |
| `Could not resolve placeholder 'POSTGRES_HOST'` en prod | Falta una variable (prod no tiene valores por defecto) | Definir todas las variables de entorno |
| El IDE no encuentra el `.env` | Directorio de trabajo distinto a la raíz | Ejecutar desde la raíz del proyecto o desde `bootstrap/` |
