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
| `develop` | Integración. Al hacer push, el CI corre y si pasa → deploy automático a QA. |
| `main` | Producción. Solo se actualiza por PR con mínimo 3 revisores → deploy automático a PROD. |

### Pipeline automático
1. **Push a `develop`** → GitHub Actions corre `mvn clean test jacoco:report` + build Docker + push a ACR + deploy a QA
2. **Merge a `main`** → mismo CI + deploy a PROD

### Ambientes
| Ambiente | URL | Swagger |
|----------|-----|---------|
| Local | `https://localhost:8443` | `https://localhost:8443/swagger-ui.html` |
| QA | `https://techcup-backend-qa-1-gva9hqfdeqard9bf.centralus-01.azurewebsites.net` | `https://techcup-backend-qa-1-gva9hqfdeqard9bf.centralus-01.azurewebsites.net/swagger-ui.html` |
| PROD | `https://techcup-backend-prod-1-awagabefhwadb2g9.centralus-01.azurewebsites.net` | `https://techcup-backend-prod-1-awagabefhwadb2g9.centralus-01.azurewebsites.net/swagger-ui.html` |

### Apagar/encender servidores Azure (para ahorrar costos)
Cuando no estés trabajando activamente en QA o PROD, puedes pausar los App Services desde el portal:

1. Ve a [portal.azure.com](https://portal.azure.com) → grupo de recursos `techcup-rg`
2. Entra a `techcup-backend-qa-1` o `techcup-backend-prod-1`
3. Haz clic en **Stop** para pausar (no genera costo de cómputo)
4. Haz clic en **Start** para reanudar cuando lo necesites

> Los servidores de base de datos (`techcup-db-qa`, `techcup-db-prod`) también pueden pausarse desde su panel en Azure Portal.
