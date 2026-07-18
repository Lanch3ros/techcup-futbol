# Guía de Operaciones Técnicas (Terminal) - Equipo TechCup Fútbol

## 1. Inicio de Jornada (Levantar Entorno)
Abramos la terminal en la raíz del proyecto y ejecutemos estos comandos en orden para preparar el entorno:

1. **Levantar máquina virtual (Colima):**
   `colima start --cpu 2 --memory 4`

2. **Iniciar base de datos (manteniendo datos previos):**
   `docker compose up -d postgres`

3. **Iniciar el backend:**
   `mvn spring-boot:run -Dmaven.test.skip=true`

(La API quedará expuesta en `https://localhost:8443` y la documentación en `https://localhost:8443/swagger-ui.html`)

> **Nota:** El `docker compose up -d` sin argumentos también levanta el servicio `app` (imagen Docker). Para desarrollo local con Maven, usa `docker compose up -d postgres` para iniciar solo la base de datos.

---

## 2. Desarrollo y Control de Calidad
Para no detener el servidor, abramos una **segunda pestaña** en la terminal y utilicemos estos comandos según las necesidades de integración:

* **Compilar e instalar dependencias:** `mvn clean install`
* **Ejecutar suite de pruebas (509 tests):** `mvn clean test`
* **Generar reporte de cobertura (JaCoCo):** `mvn clean test jacoco:report`
* **Ver reporte de cobertura:** Abrir `target/site/jacoco/index.html` en el navegador

---

## 3. Fin de Jornada (Liberar Recursos)
Sigamos este orden estricto al terminar de trabajar para no dejar procesos "zombies" consumiendo batería o RAM en nuestros equipos locales:

1. **Detener el backend:** Vamos a la terminal donde corre Spring Boot y presionamos `Ctrl + C`.
2. **Apagar contenedores (SIN borrar datos):** `docker compose stop`
3. **Apagar motor de virtualización:** `colima stop`

---

## 4. Mantenimiento (Borrón y Cuenta Nueva)
Usemos esta secuencia únicamente cuando necesitemos purgar toda la base de datos y empezar con un entorno en blanco para evitar colisiones de datos.

1. Detenemos el backend con `Ctrl + C`.
2. Destruimos la base de datos y su volumen de persistencia: `docker compose down -v`
3. Levantamos una base de datos nueva y limpia: `docker compose up -d postgres`
4. Iniciamos el backend para que Hibernate vuelva a crear las tablas vacías automáticamente: `mvn spring-boot:run -Dmaven.test.skip=true`

---

## 5. Flujo de Ramas y CI/CD (Sprint 4)

El proyecto sigue el siguiente flujo de despliegue automático:

```
feat/** ──► develop ──► main
               │           │
               ▼           ▼
              QA          PROD
```

### Ramas
| Rama | Propósito |
|------|-----------|
| `feat/**` | Desarrollo de nuevas funcionalidades. El CI corre tests automáticamente. |
| `develop` | Integración. El CI corre tests en cada push. (El deploy automático a QA en Azure quedó inactivo.) |
| `main` | Producción. Cada push dispara el redeploy automático en Railway. |

### Pipeline automático

> **Migración (julio 2026):** el deploy en Azure quedó inactivo (créditos agotados). Producción corre en **Railway** (backend + PostgreSQL) y **Vercel** (frontend).

1. **Push a `main`** → GitHub Actions corre `mvn clean test jacoco:report` (CI) y, en paralelo, Railway detecta el push y redespliega el backend desde el `Dockerfile`
2. **Push a `main` del frontend** → Vercel redespliega automáticamente
3. Los workflows `deploy-qa.yml` / `deploy-prod.yml` (Azure) quedaron obsoletos — se conservan solo como referencia

### Ambientes
| Ambiente | URL | Swagger |
|----------|-----|---------|
| Local | `https://localhost:8443` | `https://localhost:8443/swagger-ui.html` |
| PROD (Railway) | `https://techcup-futbol-production.up.railway.app` | `https://techcup-futbol-production.up.railway.app/swagger-ui/index.html` |
| Frontend PROD (Vercel) | `https://techcup-futbol-fronted.vercel.app` | — |
| QA | _Pendiente de montar en Railway (segunda DB + segundo servicio backend)_ | — |

### Gestión de servicios en Railway
1. Entra a [railway.app](https://railway.app) → proyecto TechCup
2. El servicio backend y la base PostgreSQL se administran desde el dashboard (variables de entorno, logs, redeploys manuales)
3. Railway redespliega automáticamente en cada push a `main` de `Lanch3ros/techcup-futbol`

### Credenciales demo (producción)
| Rol | Email | Contraseña |
|-----|-------|------------|
| Organizador | `organizador@techcup.edu.co` | `Admin123*` |
| Admin | `admin@techcup.edu.co` | `Admin123*` |
| Árbitro | `arbitro@techcup.edu.co` | `Admin123*` |
| Jugador | `jugador.stats.a@mail.escuelaing.edu.co` | `Techcup123*` |
