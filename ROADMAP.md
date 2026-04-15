# ROADMAP Sprint 4 — Backend TechCup Fútbol

Fases ordenadas por dependencias: cada fase asume que la anterior está completa.

---

## Fase 1 — Infraestructura Azure (prerequisito de todo lo demás)

Antes de poder configurar secretos ni workflows de CD, necesitas los recursos de Azure creados.

### Paso 1.1 — [MANUAL] Crear grupo de recursos en Azure

1. Entra a [portal.azure.com](https://portal.azure.com)
2. Busca **Resource groups** → **Create**
3. Nombre sugerido: `techcup-rg`
4. Región: `East US` o la más cercana disponible
5. Crea el grupo

### Paso 1.2 — [MANUAL] Crear Azure Container Registry (ACR)

1. Busca **Container registries** → **Create**
2. Resource group: `techcup-rg`
3. Registry name: `techcupacr` (debe ser único globalmente, todo minúsculas)
4. SKU: `Basic`
5. Una vez creado, ve a **Access keys** y activa **Admin user**
6. Anota: `Login server`, `Username`, `Password` (los necesitas en Fase 2)

### Paso 1.3 — [MANUAL] Crear Azure Database for PostgreSQL — Ambiente QA

1. Busca **Azure Database for PostgreSQL** → **Create** → **Flexible server**
2. Resource group: `techcup-rg`
3. Server name: `techcup-db-qa`
4. PostgreSQL version: `16`
5. Admin username: `techcup_qa`
6. Password: elige una segura y anótala
7. En **Networking**: selecciona **Public access**, agrega tu IP actual y marca **Allow public access from any Azure service**
8. Database name: `techcup`
9. Anota el hostname completo: `techcup-db-qa.postgres.database.azure.com`

### Paso 1.4 — [MANUAL] Crear Azure Database for PostgreSQL — Ambiente PROD

Repite el Paso 1.3 con:
- Server name: `techcup-db-prod`
- Admin username: `techcup_prod`
- Usa una password diferente a QA

### Paso 1.5 — [MANUAL] Crear Azure App Service — Ambiente QA

1. Busca **App Services** → **Create** → **Web App**
2. Resource group: `techcup-rg`
3. Name: `techcup-backend-qa` (esto define la URL: `https://techcup-backend-qa.azurewebsites.net`)
4. Publish: **Docker Container**
5. OS: **Linux**
6. Region: la misma que usaste antes
7. Plan: `B1` (Basic) es suficiente para QA
8. En **Docker**: Single Container, selecciona **Azure Container Registry**, apunta al ACR creado en Paso 1.2

### Paso 1.6 — [MANUAL] Crear Azure App Service — Ambiente PROD

Repite el Paso 1.5 con:
- Name: `techcup-backend-prod`
- Plan: `B2` o superior para producción

---

## Fase 2 — Configuración de Secretos

Con los recursos creados, ahora puedes registrar las credenciales en GitHub.

### Paso 2.1 — [MANUAL] Configurar GitHub Secrets

Ve a tu repositorio en GitHub → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**.

Crea los siguientes secretos:

| Secret | Valor |
|--------|-------|
| `DB_URL` | `jdbc:postgresql://localhost:5433/techcup` (ya existe — para CI con Postgres local) |
| `DB_USERNAME` | `techcup` (ya existe) |
| `DB_PASSWORD` | `techcup` (ya existe) |
| `QA_DB_URL` | `jdbc:postgresql://techcup-db-qa.postgres.database.azure.com:5432/techcup?sslmode=require` |
| `QA_DB_USERNAME` | `techcup_qa` |
| `QA_DB_PASSWORD` | La password que elegiste en Paso 1.3 |
| `PROD_DB_URL` | `jdbc:postgresql://techcup-db-prod.postgres.database.azure.com:5432/techcup?sslmode=require` |
| `PROD_DB_USERNAME` | `techcup_prod` |
| `PROD_DB_PASSWORD` | La password que elegiste en Paso 1.4 |
| `ACR_LOGIN_SERVER` | `techcupacr.azurecr.io` |
| `ACR_USERNAME` | El username del ACR (Paso 1.2) |
| `ACR_PASSWORD` | La password del ACR (Paso 1.2) |
| `JWT_SECRET` | El mismo valor Base64 que usas en desarrollo |
| `SSL_KEY_STORE_PASSWORD` | `techcup123` |
| `GOOGLE_CLIENT_ID` | Tu Google Client ID de producción |
| `GOOGLE_CLIENT_SECRET` | Tu Google Client Secret de producción |

### Paso 2.2 — [MANUAL] Configurar GitHub Environments para aprobaciones en PROD

1. Ve a **Settings** → **Environments** → **New environment**
2. Nombre: `production`
3. Activa **Required reviewers** y agrega mínimo 3 miembros del equipo
4. Esto hará que el deploy a PROD espere aprobación manual

---

## Fase 3 — Dockerización del Backend

Con los secretos listos, creamos los archivos Docker. Estos pasos los ejecutamos juntos en código.

### Paso 3.1 — Crear `Dockerfile` para el backend

Crear el archivo `Dockerfile` en la raíz del proyecto con multi-stage build:
- Stage 1 (`build`): usa `maven:3.9-eclipse-temurin-21` para compilar con `mvn clean package -DskipTests`
- Stage 2 (`runtime`): usa `eclipse-temurin:21-jre-alpine`, copia el JAR del stage anterior
- Expone el puerto `8443`
- Lee variables de entorno: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `SSL_KEY_STORE_PASSWORD`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- El keystore `keystore.p12` debe copiarse en el stage de runtime

### Paso 3.2 — Actualizar `docker-compose.yml` para incluir la app

El `docker-compose.yml` actual solo levanta PostgreSQL. Agregar un servicio `app` que:
- Dependa de `postgres`
- Use `build: .` (Dockerfile local)
- Inyecte todas las variables de entorno desde un archivo `.env`
- Mapee el puerto `8443:8443`
- Comparta la red con postgres

### Paso 3.3 — Crear `.env.example` y agregar `.env` a `.gitignore`

Crear `.env.example` con los nombres de las variables sin valores sensibles, para documentar qué se necesita. Verificar que `.env` esté en `.gitignore` para no exponer credenciales.

### Paso 3.4 — Verificar levantamiento local con Docker

```bash
cp .env.example .env        # completar con valores locales
docker compose up --build   # levantar postgres + app
curl -k https://localhost:8443/swagger-ui.html
```

---

## Fase 4 — Pipeline de Integración Continua (CI actualizado)

Con Docker funcionando localmente, expandimos el workflow de GitHub Actions existente.

### Paso 4.1 — Actualizar `.github/workflows/maven.yml`

El workflow actual solo corre tests en `main` y `feat/**`. Necesita:
- Agregar trigger en rama `develop` (ambiente QA)
- Agregar paso de generación del reporte JaCoCo (`mvn jacoco:report`)
- Agregar paso de upload del reporte JaCoCo como artefacto de GitHub Actions
- Agregar paso de build Docker (`docker build -t ...`)
- Agregar paso de login al ACR y push de la imagen (solo en `develop` y `main`)
- Versionar la imagen con el SHA del commit: `techcupacr.azurecr.io/techcup-backend:${{ github.sha }}`

### Paso 4.2 — Crear workflow de análisis estático con SonarQube

Agregar paso en el CI que ejecute SonarQube Scanner después de los tests. Requiere el Paso 4.3.

### Paso 4.3 — [MANUAL] Configurar SonarQube Cloud

1. Ve a [sonarcloud.io](https://sonarcloud.io) e inicia sesión con GitHub
2. Crea una nueva organización vinculada a tu cuenta/org de GitHub
3. Importa el repositorio `techcup-futbol`
4. SonarCloud generará un `SONAR_TOKEN`
5. Agrega ese token como GitHub Secret con el nombre `SONAR_TOKEN`
6. Anota el `sonar.organization` y `sonar.projectKey` que se generan

---

## Fase 5 — Pipeline de Despliegue a QA

### Paso 5.1 — Crear `.github/workflows/deploy-qa.yml`

Workflow separado que se activa automáticamente cuando el CI en `develop` pasa exitosamente:
- Trigger: `workflow_run` en `develop` con status `completed`
- Descarga la imagen del ACR con el SHA correspondiente
- Hace deploy a Azure App Service QA usando `azure/webapps-deploy@v3`
- Inyecta los secrets de QA como App Settings del App Service
- Ejecuta un health check básico: `curl https://techcup-backend-qa.azurewebsites.net/swagger-ui.html`

### Paso 5.2 — [MANUAL] Configurar credenciales de Azure en GitHub Actions

Para que los workflows puedan hacer deploy en App Service necesitas:
1. En Azure Portal → **App Service `techcup-backend-qa`** → **Get publish profile**
2. Descarga el archivo `.PublishSettings`
3. Copia todo el contenido del archivo
4. Agrégalo como GitHub Secret: `AZURE_WEBAPP_PUBLISH_PROFILE_QA`
5. Repite para `techcup-backend-prod` → Secret: `AZURE_WEBAPP_PUBLISH_PROFILE_PROD`

### Paso 5.3 — [MANUAL] Configurar variables de entorno en Azure App Service QA

1. Ve al App Service `techcup-backend-qa` → **Configuration** → **Application settings**
2. Agrega cada variable de entorno de QA:
   - `DB_URL` → valor de `QA_DB_URL`
   - `DB_USERNAME` → valor de `QA_DB_USERNAME`
   - `DB_PASSWORD` → valor de `QA_DB_PASSWORD`
   - `JWT_SECRET`, `SSL_KEY_STORE_PASSWORD`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
3. Guarda y reinicia el servicio

---

## Fase 6 — Pipeline de Despliegue a Producción

### Paso 6.1 — Crear `.github/workflows/deploy-prod.yml`

Workflow que se activa en merge a `main`:
- Trigger: `push` a `main`
- Usa el environment `production` (configurado en Paso 2.2) para forzar aprobación manual de 3 miembros
- Etiqueta la imagen Docker con el tag `latest` y con el tag de versión
- Push al ACR
- Deploy a Azure App Service PROD
- Health check post-deploy

### Paso 6.2 — [MANUAL] Configurar variables de entorno en Azure App Service PROD

Igual que el Paso 5.3 pero en `techcup-backend-prod` con los valores de PROD.

---

## Fase 7 — Análisis de Código con SonarQube

### Paso 7.1 — Agregar configuración de SonarQube al proyecto

Crear `sonar-project.properties` en la raíz con:
- `sonar.organization` y `sonar.projectKey` (obtenidos en Paso 4.3)
- `sonar.sources=src/main/java`
- `sonar.tests=src/test/java`
- `sonar.java.coveragePlugin=jacoco`
- `sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml`

### Paso 7.2 — Agregar paso de análisis en el CI

En `maven.yml`, después de correr los tests y generar el reporte JaCoCo, agregar el paso de SonarScanner usando la GitHub Action oficial `SonarSource/sonarcloud-github-action@master`.

### Paso 7.3 — Verificar calificativos en SonarCloud

Una vez ejecutado el primer análisis:
- Revisar que `Reliability`, `Security`, `Maintainability` sean calificativo **B o superior**
- Si alguno está en C o D, corregir los issues que SonarCloud señale antes de continuar

---

## Fase 8 — Actualización del README

### Paso 8.1 — Actualizar `README.md` con URLs de despliegue

Agregar sección de **Despliegues** al README con:
- URL QA: `https://techcup-backend-qa.azurewebsites.net`
- URL PROD: `https://techcup-backend-prod.azurewebsites.net`
- URL Swagger QA: `https://techcup-backend-qa.azurewebsites.net/swagger-ui.html`
- Instrucciones para construir y levantar con Docker localmente

---

## Resumen de dependencias

```
Fase 1 (Azure)
    └─► Fase 2 (Secrets)
            ├─► Fase 3 (Docker local)
            │       └─► Fase 4 (CI actualizado)
            │               ├─► Fase 5 (Deploy QA)
            │               └─► Fase 6 (Deploy PROD)
            └─► Fase 7 (SonarQube)
                        └─► Fase 8 (README)
```

## Estado actual

| Componente                         | Estado                    |
|------------------------------------|---------------------------|
| CI básico (tests en main/feat)     | ✅ Ya implementado         |
| PostgreSQL en docker-compose       | ✅ Ya implementado         |
| Dockerfile del backend             | ❌ Pendiente (Fase 3)      |
| App en docker-compose              | ❌ Pendiente (Fase 3)      |
| CI con JaCoCo + Docker build       | ❌ Pendiente (Fase 4)      |
| Trigger en rama `develop`          | ❌ Pendiente (Fase 4)      |
| SonarQube                          | ❌ Pendiente (Fases 4 y 7) |
| Deploy a QA en Azure               | ❌ Pendiente (Fase 5)      |
| Deploy a PROD en Azure             | ❌ Pendiente (Fase 6)      |
| GitHub Environments + aprobaciones | ❌ Pendiente (Paso 2.2)    |
| README con URLs de despliegue      | ❌ Pendiente (Fase 8)      |
