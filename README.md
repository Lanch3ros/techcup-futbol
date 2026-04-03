# **| JAVABURGUERS |**

![LandingPageImage.png](docs/images/LandingPageImage.png)

### NOMBRES DE INTEGRANTES:
- Andres Camilo Vivas Baquero
- Dana Valeria Leal Guzmán
- Daniel Julian Peña Bonilla
- Jose Luis Lancheros Ayora
- Juan Sebastian Murcia Yanquen

## TECHCUP FUTBOL
Plataforma web centralizada para la gestión integral del torneo semestral de fútbol de los programas de ingeniería
de la Escuela Colombiana de Ingeniería Julio Garavito. Este sistema reemplaza los procesos manuales mediante la
automatización de inscripciones, administración de equipos, verificación de pagos y cálculo de estadísticas en tiempo real.

---

## Instrucciones de Ejecución

### Prerrequisitos
* Java 21
* Maven 3.8+
* Docker (Para levantar PostgreSQL 16)

### Pasos para ejecutar localmente
1. Clonar el repositorio:
   `git clone https://github.com/Lanch3ros/techcup-futbol.git`
2. Navegar a la carpeta del proyecto:
   `cd techcup-futbol`
3. Levantar la base de datos PostgreSQL:
   `docker compose up -d`
4. Ejecutar la suite de pruebas (verificación de integridad con +480 tests):
   `mvn clean test jacoco:report`
5. Ejecutar la aplicación Spring Boot:
   `mvn spring-boot:run -Dmaven.test.skip=true`
6. La aplicación estará disponible en `http://localhost:8080`
7. Para visualizar la documentación interactiva (Swagger / OpenAPI 3.1), ingresa a:
   `http://localhost:8080/swagger-ui.html`

---

# ÍNDICE
### 0. PRESENTACIONES SPRINT
* **Sprint 1:** [Enlace a Canva](https://www.canva.com/design/DAHDIhwNdzU/ynjiJ__QOQWReNaZfXhO7Q/edit?utm_content=DAHDIhwNdzU&utm_campaign=designshare&utm_medium=link2&utm_source=sharebutton)
* **Sprint 2:** [Enlace a Canva](https://www.canva.com/design/DAHEoyICPoE/jg6A0KOsso8ERnJbRn0hRw/edit?utm_content=DAHEoyICPoE&utm_campaign=designshare&utm_medium=link2&utm_source=sharebutton)
* **Sprint 3:** [Enlace a Canva](https://www.canva.com/design/DAHFSF0epuE/R3Pq2PrtoQJfLQqHlH7F8Q/edit?utm_content=DAHFSF0epuE&utm_campaign=designshare&utm_medium=link2&utm_source=sharebutton)

---

### 1. ARQUITECTURA Y PATRONES DE DISEÑO

Para la construcción del core de la aplicación, el equipo analizó e implementó los siguientes patrones de diseño y arquitectura, garantizando escalabilidad y cumplimiento de las reglas de negocio de TechCup.

**Arquitectura en Capas (MVC adaptado a REST API)**
- **¿Por qué lo elegimos?** Es el estándar de la industria para aplicaciones web con Spring Boot, permitiendo separar responsabilidades (Separation of Concerns).
- **¿Cómo ayuda a resolver el problema?** Aísla la capa de presentación (REST Controllers) de la lógica de negocio (Services) y del acceso a datos (Repositories). Esto nos permite validar tokens JWT en los controladores sin acoplar la lógica matemática del cálculo de estadísticas o la generación de llaves de los torneos.

**Factory Method - `PlayerFactory`**
- **¿Por qué lo elegimos?** El sistema maneja múltiples actores (`StudentUser`, `GraduateUser`, `TeacherUser`, `RelativeUser`, `StaffUser`). Todos comparten atributos básicos (nombre, correo), pero difieren en su creación y validación.
- **¿Cómo ayuda a resolver el problema?** Centraliza la lógica de instanciación. Cuando un usuario envía un JSON al endpoint de registro, el controlador delega a la fábrica correspondiente. Esto nos permite separar estructuralmente la "Identidad" (Clase base `User` para login) del "Comportamiento" (Interfaz `Player` para jugar), evitando que administradores o árbitros hereden propiedades innecesarias como el número dorsal.

**Strategy - Validador de Correos (`EmailValidator`)**
- **¿Por qué lo elegimos?** Existen reglas estrictas de registro: los estudiantes y graduados usan el dominio `@mail.escuelaing.edu.co`. Los profesores y administrativos(staff) deben usar el dominio `@escuelaing.edu.co` y los familiares usan Gmail.
- **¿Cómo ayuda a resolver el problema?** Encapsulamos cada regla de validación en su propia clase. Antes de persistir el usuario en la base de datos, el `PlayerService` invoca dinámicamente la estrategia correspondiente. Si la universidad cambia su dominio en el futuro, solo se modifica una clase concreta sin alterar la lógica global.

---

### 2. DIAGRAMAS

#### 2.1 DIAGRAMA DE CONTEXTO DEL SISTEMA
Representa cómo interactúa TECHCUP FÚTBOL con los actores externos. Su propósito es mostrar los límites del sistema.

![DiagramaContexto.png](docs/images/ContextDiagram.png)

* **Jugadores / Capitanes:** Interactúan para registrar perfiles, aceptar invitaciones, pagar inscripciones y armar alineaciones.
* **Personal de Gestión (Organizador, Administrador):** Controlan el ciclo de vida del torneo y la seguridad.
* **Árbitros:** Reportan resultados, tarjetas y faltas.
* **Sistema externo:** Sistema de correo electrónico (SMTP) para notificaciones y File Storage (S3) para fotos de perfil y comprobantes.

#### 2.2 DIAGRAMA DE CLASES (Core de Negocio)
Enfocado en las entidades de negocio y la aplicación de los patrones. Destaca la jerarquía de herencia con estrategia `SINGLE_TABLE`:
Una clase base abstracta `User` (para el sistema de seguridad JWT) de la cual extienden todos los usuarios. Los actores que participan en los partidos implementan adicionalmente la interfaz `Player`, mientras que los usuarios de gestión (`AdminUser`, `OrganizerUser`, `RefereeUser`) solo extienden de `User`.

![ClassDiagram.png](docs/images/ClassDiagram.png)

#### 2.3 DIAGRAMA DE SECUENCIA

* **Flujo 1:** Registro de Jugador

![SequenceDiagramFlujo1RegistroJugador.png](docs/images/SequenceDiagramFlujo1RegistroJugador.png)

Para mas detalle ir a la ruta `docs/uml/SequenceDiagramFlujo1RegistroJugador.pdf` o darle click a
[Registro de Jugador](docs/uml/SequenceDiagramFlujo1RegistroJugador.pdf)

* **Flujo 2:** Inscripción de Equipo a Torneo

![SequenceDiagramFlujo2InscripcionEquipoTorneo.png](docs/images/SequenceDiagramFlujo2InscripcionEquipoTorneo.png)

Para mas detalle ir a la ruta `docs/uml/SequenceDiagramFlujo2InscripcionEquipoTorneo.pdf` o darle click a
[Inscripción de Equipo a Torneo](docs/uml/SequenceDiagramFlujo2InscripcionEquipoTorneo.pdf)

#### 2.4 DIAGRAMAS DE COMPONENTES
**Diagrama de Componentes General (Vista Macro)**
Muestra los bloques tecnológicos principales: La SPA en React interactuando vía JSON/HTTP con el API en Spring Boot, el cual se conecta de manera segura a PostgreSQL 16.

![componentesgeneral.png](docs/images/componentesgeneral.png)


**Diagrama de Componentes Específico (Arquitectura Interna)**
Detalla las capas del backend:
1. `Config`: Seguridad (Filtros JWT), Base de datos (Seeder) y Swagger.
2. `Controller`: Exposición de endpoints REST y manejo de DTOs.
3. `Service`: Lógica central (ej. `MatchService`, `TournamentService`, `StatsService`).
4. `Repository`: Interfaces de Spring Data JPA (ej. `UserRepository`).

![componentesespecifico.png](docs/images/componentesespecifico.png)


#### 2.5 DIAGRAMA ER (ENTIDAD-RELACIÓN)
Representa el modelo físico en PostgreSQL:
- **`users`**: Centraliza todas las credenciales mediante la columna discriminadora `user_type`.
- **`teams` / `tournaments`**: Relación Mucho-a-Mucho mediante la tabla intermedia `tournament_teams`.
- La alineación de equipos y suplentes (`startingPlayerIds`, `reservePlayerIds`) se persiste eficientemente mediante `@ElementCollection`.

![ERDiagram.png](docs/uml/REDiagram.png)


---

### 3. SEGURIDAD Y CONTROL DE ACCESO (RBAC)
El sistema implementa seguridad *Stateless* utilizando **JSON Web Tokens (JWT)**.
* Todo endpoint (excepto login y registro) es interceptado por un `JwtAuthenticationFilter`.
* Se cuenta con un Seeder idempotente que garantiza la existencia de perfiles maestros al iniciar la BD:
    * `admin@techcup.edu.co` (ROLE_ADMIN)
    * `organizador@techcup.edu.co` (ROLE_ORGANIZADOR)
    * `arbitro@techcup.edu.co` (ROLE_ARBITRO)

---

### 4. CALIDAD Y DEUDA TÉCNICA (TESTING)

Para garantizar el control de la deuda técnica, el proyecto cuenta con un entorno de validación robusto y escaneos de calidad automatizados.

* **Pruebas Unitarias:** 498 tests ejecutados mediante JUnit y Mockito.
* **Cobertura de Código (JaCoCo):** El sistema mantiene un **97.7% de cobertura** en Líneas, Clases y Métodos.
* **Análisis Estático (SonarQube):** Superamos el *Quality Gate* del Sprint 3 resolviendo todas las deudas técnicas y garantizando la máxima confiabilidad de la API. Las métricas finales del código nuevo (New Code) son:
    * **Fiabilidad (Reliability):** A (0 Bugs)
    * **Seguridad (Security):** A (0 Vulnerabilidades)
    * **Mantenibilidad (Maintainability):** A (0 Code Smells)
    * **Hotspots de Seguridad:** 0% Riesgo (100% Revisados)

> **Nota Técnica:** Las excepciones de seguridad (como la desactivación intencional del CSRF) están debidamente justificadas mediante anotaciones `// NOSONAR` debido a la naturaleza Stateless (JWT) de la arquitectura.
![JacocoCoverage.png](docs/jacoco/JacocoCoverage.png)

![SonarqubeReport.png](docs/images/SonarqubeReport.png)

### 5. ANÁLISIS DE REQUERIMIENTOS Y GESTIÓN
Ir a la ruta `docs/requirements/RequirementsAnalisis.pdf` o darle click a
[Requirements Analisis](docs/requirements/RequirementsAnalisis.pdf)

* **Gestión en Jira:** Todo el Product Backlog, Épicas e Historias de Usuario están trazadas en nuestro board ágil.
  ![JiraSprint3.png](docs/images/Jira/JiraSprint3.png)

