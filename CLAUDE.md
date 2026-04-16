# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
**TechCup Fútbol** is a Spring Boot backend for managing a semi-annual engineering football tournament at Escuela Colombiana de Ingeniería Julio Garavito. It handles team registrations, player management, payment verification, match tracking, standings, and JWT-based authentication.

## Build & Run Commands
```bash
mvn clean install                           # Compile and resolve dependencies
mvn spring-boot:run -Dmaven.test.skip=true  # Start HTTPS server at https://localhost:8443
mvn clean test                              # Run all tests (509 tests, all green)
mvn test -Dtest=PlayerServiceTest           # Run a single test class
mvn clean test jacoco:report                # Tests + JaCoCo HTML report in target/site/jacoco/
```

Swagger UI: `https://localhost:8443/swagger-ui.html`

**Prerequisites:** Java 21, Maven 3.8+, Docker (Colima on macOS) running PostgreSQL 16.

```bash
docker compose up -d   # Start PostgreSQL 16 on host port 5433
```

Environment variables (all have development defaults in `application.yaml`):
- `DB_URL` → `jdbc:postgresql://localhost:5433/techcup`
- `DB_USERNAME` / `DB_PASSWORD` → `techcup`
- `JWT_SECRET` → Base64-encoded 256-bit key (development default provided)
- `SSL_KEY_STORE_PASSWORD` → `techcup123` (PKCS12 keystore in `src/main/resources/keystore.p12`)
- `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` → Google OAuth2 credentials (use `dev-client-id` placeholder for local dev)

## Architecture

Layered: **Controller → Service → Repository → Model**, cross-cutting concerns in `core/`.

```
src/main/java/com/example/
├── config/          # SecurityConfig, JwtAuthenticationFilter, SwaggerConfig, DatabaseSeeder, CorsConfig
├── controller/      # REST controllers (all prefixed /api/v1) + DTOs + Mappers + GlobalExceptionHandler
├── core/
│   ├── service/     # Business logic + JwtService + CustomUserDetailsService
│   ├── model/       # JPA entities
│   ├── factory/     # PlayerFactory per user type (Factory Method)
│   ├── validator/   # Email validators per user type (Strategy)
│   └── exception/   # ResourceNotFoundException, BusinessRuleException
└── repository/      # Spring Data JPA repositories
```

## Domain Model & Persistence

**User hierarchy** — `SINGLE_TABLE` inheritance in the `users` table:
- Discriminator column: `user_type` = `STUDENT | TEACHER | GRADUATE | RELATIVE | ADMIN | ORGANIZER | REFEREE`
- Abstract `User` base class; concrete subtypes: `StudentPlayer`, `TeacherPlayer`, `GraduatePlayer`, `RelativePlayer`, `StaffPlayer`, `AdminUser`, `OrganizerUser`, `RefereeUser`
- `UserRepository extends JpaRepository<User, Long>` — returns `User`, cast to `Player` in services when needed

**Uniqueness constraints on `users` table** (added Sprint 3):
- `email` → `@Column(unique = true, nullable = false)` — every user must have a unique non-null email
- `identification` → `@Column(unique = true)` — nullable (system users ADMIN/ORGANIZER/REFEREE may not have a cedula), but must be unique when present
- `licenseNumber` → `@Column(unique = true)` — enforced for `RefereeUser` only

**`password` field** — annotated `@JsonProperty(access = WRITE_ONLY)`: deserialized from JSON but never serialized into responses. Passwords are never exposed via any REST endpoint.

**`Team.players`** is `@Transient` — never JPA-mapped. Always load players via `userRepository.findByTeamId(teamId)`.

**`Invitation`** entity (`invitations` table) — persists each team→player invite with status `PENDING | ACCEPTED | REJECTED`. Created in `TeamService.sendInvitation()`, processed in `PlayerService.respondToInvitation()`.

**Key relationships:**
- `Tournament.registeredTeams` → `@ManyToMany @JoinTable(name="tournament_teams")`
- `Tournament.matches` → `@OneToMany @JoinColumn(name="tournament_id")`
- `Match.homeTeam / awayTeam` → `@ManyToOne`
- `Match.events / lineups` → `@Transient` (loaded separately)
- `RefereeUser.assignedMatchIds` → `@ElementCollection @CollectionTable(name="referee_matches")`

**`ddl-auto: update`** — Hibernate auto-creates/updates all tables on startup.

## Database Seeder

`DatabaseSeeder` (`config/DatabaseSeeder.java`) implements `CommandLineRunner` and runs at startup. It inserts three system users the first time the app starts against an empty database (idempotent — checks for `admin@techcup.edu.co` before inserting):

| Email                        | Type            | Role          | Discriminator |
|------------------------------|-----------------|---------------|---------------|
| `admin@techcup.edu.co`       | `AdminUser`     | `ADMIN`       | `ADMIN`       |
| `organizador@techcup.edu.co` | `OrganizerUser` | `ORGANIZADOR` | `ORGANIZER`   |
| `arbitro@techcup.edu.co`     | `RefereeUser`   | `ARBITRO`     | `REFEREE`     |

Default password for all: `Admin123*` (BCrypt-encoded at startup).

## Security (JWT + HTTPS + CORS + OAuth2)

### HTTPS
The server runs exclusively on **port 8443** with TLS. The PKCS12 keystore is at `src/main/resources/keystore.p12`. All `curl` calls and HTTP Client requests must use `-k` / `https://` to accept the self-signed certificate in development.

### JWT
HTTP Basic is **disabled**. All protected endpoints require a Bearer JWT.

**Authentication flow:**
1. `POST /api/v1/auth/login` → `AuthController` → `AuthenticationManager` (BCrypt verify) → `JwtService.generateToken()` → returns `{ token, type: "Bearer", email }`
2. Every subsequent request: `JwtAuthenticationFilter` extracts the token, validates via `JwtService.isTokenValid()`, loads user via `CustomUserDetailsService.loadUserByUsername(email)`, sets `SecurityContextHolder`

**`JwtService`** — HS256, **1 hour expiry** (`app.jwt.expiration-ms: 3600000`). Token includes a `"roles"` claim. `isTokenValid()` catches `JwtException` and returns `false` instead of propagating.

### Google OAuth2
`POST /api/v1/auth/google` (public) — accepts a Google ID token and returns a TechCup JWT. Configured via `spring.security.oauth2.client.registration.google` in `application.yaml`. Use `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` env vars in production.

### CORS
`CorsConfig` (`config/CorsConfig.java`) permits:
- Origins: `http(s)://localhost:*`, `http(s)://127.0.0.1:*`
- Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
- Headers: all (`*`), credentials allowed

### CustomUserDetailsService
Looks up the user by `email` in `UserRepository`. Role resolution: uses `user.getRole()` if non-blank; otherwise resolves by type — `AdminUser` → `ROLE_ADMIN`, `OrganizerUser` → `ROLE_ORGANIZADOR`, `RefereeUser` → `ROLE_ARBITRO`, anything else → `ROLE_JUGADOR`.

**Public endpoints:** Swagger UI, `POST /api/v1/auth/login`, `POST /api/v1/auth/google`, `POST /api/v1/players/register`, `POST /api/v1/referees`, GET tournaments/matches/stats

**RBAC matrix:**

| Role          | Key permissions                                                             |
|---------------|-----------------------------------------------------------------------------|
| `ADMIN`       | Everything                                                                  |
| `ORGANIZADOR` | Tournament CRUD, match creation, referee assignment, payment approve/reject |
| `ARBITRO`     | Match status, result, events                                                |
| `CAPITAN`     | Team creation, lineup, invitations, payment upload, tournament inscription  |
| `JUGADOR`     | Read-only authenticated access                                              |

## Player Registration (`PlayerService.registerPlayer`)

### `PlayerRegistrationRequest` fields (Sprint 3 additions)
- `name` OR `firstName` + `lastName` — either form accepted (cross-field `@AssertTrue isNameProvided()`)
- `age` (`Integer`) — converted to `birthDate` by `normalizeIncomingRegistration()`
- `skillLevel` — stored as `certificationLevel` for referees
- `securityRole` — internal field; set by normalizer for CAPTAIN type
- `jerseyNumber` changed from `int` to `Integer` (nullable; normalizer defaults to `10` if absent)
- `position` pattern is now **case-insensitive**: `(?i)^(Portero|Defensa|Volante|Delantero)$`

### `normalizeIncomingRegistration()` (Sprint 3)
Runs at the top of `registerPlayer()` to align front-end payloads with the factory/seeder contracts:
- `CAPTAIN` / `CAPITAN` / `CAPTAN` userType → `userType = STUDENT` + `securityRole = CAPITAN`
- `REFEREE` / `ARBITRO` userType → `userType = REFEREE` (routed to `registerRefereeUser()`)
- `PLAYER` / `JUGADOR` userType → `userType = STUDENT`
- Derives `name` from `firstName + " " + lastName` if `name` is blank
- Normalizes `position` to title-case (`delantero` → `Delantero`)
- Converts `age` to `birthDate` (`LocalDate.now().minusYears(age)`)
- Defaults `jerseyNumber` to `10` if `null`

### Duplicate-registration guard (Sprint 3)
Before `userRepository.save()`, `registerPlayer()` checks:
```java
if (userRepository.existsByEmail(data.getEmail()))
    throw new BusinessRuleException("El correo '...' ya está registrado.");
if (identification != null && !identification.isBlank() && userRepository.existsByIdentification(identification))
    throw new BusinessRuleException("La identificación '...' ya está registrada.");
```
Both throw `BusinessRuleException` → **409 Conflict**. The DB unique constraint is a safety net for race conditions.

`registerPlayer()` now returns `User` (not `Player`) to support the `RefereeUser` path.

### Referee registration path
When `userType = REFEREE`, `registerRefereeUser()` is called instead of the factory chain:
- Validates email + identification uniqueness and non-blank license
- Also checks `existsByLicenseNumber(license)` → 409 if duplicate
- Creates a `RefereeUser` directly, sets `role = "ARBITRO"`, encodes password, saves

### Password encoding
`passwordEncoder.encode()` is called **after** the factory builds the entity and **before** `userRepository.save()`. Factories set plaintext; encoding always happens in the service.

## Key Business Rules

- **RN-03-4**: `>50%` of team players must belong to an engineering `Program` (SISTEMAS, IA, CIBERSEGURIDAD, ESTADISTICA). Maestría programs do **not** count. Enforced in `TeamService.validateEngineeringProgramComposition()`, triggered on `configureLineup()`.
- **RN-09-2**: Each finished match in which a team had zero cards awards +1 FairPlay point. Computed in `StatsService.calculateFairPlayPoints()` and added to base points in `getTeamStats()` / `getTournamentStandings()`.
- **RN-11-3**: Accepting an invitation auto-rejects all other `PENDING` invitations for the same player via `invitationRepository.saveAll()` in `PlayerService.processInvitationResponse()`.
- **RN-08-1**: `MatchService.registerResult()` throws `BusinessRuleException` if match status ≠ `"Finalizado"`.
- **GAP-13**: `MatchService.registerResult()` updates both teams' stats after saving the match result: `matchesPlayed`, `matchesWon/Lost/Drawn`, `goalsFor`, `goalsAgainst`, `goalDifference`, `points` (3/1/0).
- **Plantilla congelada**: `TeamService.sendInvitation()` and `removePlayer()` throw `BusinessRuleException` if the team's tournament is `"En progreso"`.
- **Brackets (6.11)**: `TournamentService.generateQuarterFinals()` ranks top-8 teams by points → goal difference → goals scored, then pairs: 1v8, 2v7, 3v6, 4v5. Guards: tournament must be `"En progreso"`, no existing QF matches, ≥8 registered teams.
- **Match states**: valid transitions are `Programado → En Curso → Finalizado`. The string must match exactly — `"En Curso"` not `"En progreso"`.
- **Lineup minimum**: `LineupRequest.startingPlayersIds` has `@Size(min=7, max=11)` — at least 7 starters required per team.

## Statistics Engine (Phase 4)

- **GAP-14** `StatsService.getTournamentStandings()` — filters to only registered teams of the tournament via `tournament.getRegisteredTeams()`.
- **GAP-15** `StatsService.getTopScorersByTournament()` — scopes events to match IDs from `tournament.getMatches()` using `matchEventRepository.findByMatchIdIn()`.
- **GAP-16** `StatsService.getPlayerStats()` — computes `matchesPlayed` as distinct finished matches where the player had at least one event; resolves `teamName` via `teamRepository.findById(player.getTeamId())`.

`StatsService` constructor: `StatsService(TeamRepository, MatchRepository, MatchEventRepository, UserRepository, TournamentRepository)` — 5 args.

## ProfileDTO

`ProfileDTO` is a Java record in `controller/dto/response/`. It **includes `id` as its first field** (added to enable player ID capture for fichajes/lineup flows):

```java
public record ProfileDTO(Long id, String fullName, String email, String userType,
    String profilePhoto, Integer jerseyNumber, String position, String identification,
    String gender, LocalDate birthDate, Program program, Long teamId, Integer semester) {}
```

`PlayerMapper` passes `user.getId()` as the first argument. All test constructors for `ProfileDTO` use `null` as first arg.

## Automated QA — Bash Scripts

Four bash scripts in the project root (use `python3` for JSON parsing; `jq` not required):

| Script                          | Coverage                                                                    |
|---------------------------------|-----------------------------------------------------------------------------|
| `test_api_flow.sh`              | Login, public/protected GET/POST tournaments                                |
| `test_rbac_flow.sh`             | 16 cross-role RBAC tests (ORGANIZADOR, ÁRBITRO, ADMIN)                      |
| `test_player_registration.sh`   | Strategy pattern: 5 happy paths + 5 unhappy paths per user type             |
| `test_e2e_section6.sh`          | §6.1–6.12 E2E tournament lifecycle (22/24 passing; 2 WIP)                   |
| `test_full_match_simulation.sh` | **52/52** — full match: 14 players, 2 teams, payments, lineup, match, stats |

`test_full_match_simulation.sh` validates the stats engine end-to-end: Equipo A = **4 pts** (3 victoria + 1 FairPlay RN-09-2), Equipo B = **1 pt** (0 derrota + 1 FairPlay).

## Automated QA — IntelliJ HTTP Client (`api-tests/`)

```
api-tests/
├── http-client.env.json          # Environments: "local" (https://localhost:8443/api/v1), "staging"
├── 01_flujo_torneo_completo.http # 59 requests covering §6.1–§6.12 end-to-end
└── payment.jpg                   # Minimal valid JPEG for payment upload tests
```

**Key patterns used:**
- `client.global.set("org_token", response.body.token)` — capture JWT after each login
- `Authorization: Bearer {{org_token}}` — inject token in subsequent requests
- Single `GET /players` with a JS handler that maps emails → IDs for all 14 players at once
- `{{player_a1_id}}` interpolated inside JSON body arrays (lineup)
- `< ./payment.jpg` — multipart file upload

**Running the `.http` file twice** now returns **409 Conflict** for duplicate emails/identifications (Sprint 3 validation), not a 500 crash.

## Testing Conventions

**509 tests, all green. JaCoCo coverage: 100% instructions, 100% lines, 100% methods, 100% classes, 99.2% branches.**

The 3 missed branches are accepted structural false negatives:

| Class                         | Branch                                        | Reason                                                                 |
|-------------------------------|-----------------------------------------------|------------------------------------------------------------------------|
| `JwtService#isTokenValid`     | `isTokenExpired() = true`                     | JJWT throws `ExpiredJwtException` before the boolean can return `true` |
| `TeamService#isValidProgram`  | non-null, non-engineering, non-master program | No `Program` enum value meets this condition                           |
| `TeamService#isMasterProgram` | false path                                    | Same reason — impossible with current enum values                      |

**Test layers and strategies:**

- **Service layer** — pure Mockito, no Spring context. Manual constructor injection:
  ```java
  repo = mock(SomeRepository.class);
  service = new SomeService(repo, ...);
  when(repo.findById(1L)).thenReturn(Optional.of(entity));
  ```
- **Controller layer** — pure Mockito, direct `new` instantiation + injected service mocks. No `@WebMvcTest`. Call controller methods directly and assert `ResponseEntity` status/body.
- **Config layer** — mixed:
    - `JwtAuthenticationFilter`: pure Mockito + `MockHttpServletRequest` / `MockHttpServletResponse`
    - `SwaggerConfig`: direct instantiation, call `customOpenAPI()` and assert OpenAPI structure
    - `SecurityConfig`: `@SpringBootTest(webEnvironment = NONE) @ActiveProfiles("test")` — beans are autowired from a real (but DB-free) Spring context
- **Model layer** — direct instantiation. Private `@PrePersist` methods invoked via Java reflection (`getDeclaredMethod` + `setAccessible(true)`). Default interface methods covered via anonymous implementations that do not extend `User`.
- **Application smoke test** — `TechcupFutbolApplicationTests`: `@SpringBootTest(webEnvironment = NONE) @ActiveProfiles("test")` for `contextLoads()`; `mockStatic(SpringApplication.class)` to cover `main()` without starting a second context.

`@Value` fields in `JwtService` are injected in tests via `ReflectionTestUtils.setField()`.

When adding a dependency to a service constructor, update **all** test files that instantiate that service directly — they will fail to compile otherwise. Current multi-dependency services:
- `PlayerService(UserRepository, PasswordEncoder, InvitationRepository)`
- `TeamService(TeamRepository, UserRepository, InvitationRepository, TournamentRepository)`
- `StatsService(TeamRepository, MatchRepository, MatchEventRepository, UserRepository, TournamentRepository)`

**Mockito defaults for new `existsBy*` methods:** boolean methods return `false` by default, so existing `registerPlayer` tests pass without explicit stubs for the duplicate-check calls. Only add `when(repo.existsByEmail(...)).thenReturn(true)` when testing the duplicate-rejection path.

**Jackson configuration note:** Spring Boot 4.x uses Jackson 3.x (`tools.jackson`). `ACCEPT_CASE_INSENSITIVE_ENUMS` is a `MapperFeature` (not `DeserializationFeature`) in Jackson 3.x. The correct `application.yaml` key is `spring.jackson.mapper.accept-case-insensitive-enums: true`.

## CI/CD

Three GitHub Actions workflows:

### `.github/workflows/maven.yml` — CI (build & test)
- Triggers on push to `main` / `develop` / `feat/**` and PRs to `main` / `develop`
- Spins up `postgres:16` as a service container (port 5433) with **hardcoded** credentials (`techcup`/`techcup`) — do NOT use secrets for the CI test database; it's ephemeral and this was the fix for the `password authentication failed` error
- Runs `mvn clean test jacoco:report` — secrets injected: `JWT_SECRET`, `SSL_KEY_STORE_PASSWORD`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- Uploads JaCoCo HTML report as artifact (7-day retention)
- Runs SonarCloud analysis via `SonarSource/sonarcloud-github-action@master` — **note: free plan only analyzes `main`; `develop` shows "Not analyzed"**
- On `develop` or `main`: builds and pushes Docker image to ACR tagged with `github.sha` and `latest`

### `.github/workflows/deploy-qa.yml` — Deploy to QA
- Triggers via `workflow_run` on CI completing successfully on `develop`
- Deploys image `ACR/techcup-backend:${{ github.event.workflow_run.head_sha }}` to App Service `techcup-backend-qa-1`
- Requires GitHub Secret: `AZURE_WEBAPP_PUBLISH_PROFILE_QA`
- Runs health check against `https://techcup-backend-qa-1-gva9hqfdeqard9bf.centralus-01.azurewebsites.net/swagger-ui.html`

### `.github/workflows/deploy-prod.yml` — Deploy to PROD
- Triggers via `workflow_run` on CI completing successfully on `main`
- Uses GitHub Environment `production` (branch protection on `main` enforces 3-reviewer PRs)
- Deploys to App Service `techcup-backend-prod-1`
- Requires GitHub Secret: `AZURE_WEBAPP_PUBLISH_PROFILE_PROD`
- Runs health check against `https://techcup-backend-prod-1-awagabefhwadb2g9.centralus-01.azurewebsites.net/swagger-ui.html`

### Azure infrastructure (Sprint 4)
- ACR: `techcupacr.azurecr.io`
- QA DB: `techcup-db-qa.postgres.database.azure.com` / user `techcup_qa`
- PROD DB: `techcup-db-prod.postgres.database.azure.com` / user `techcup_prod`
- QA App Service: `techcup-backend-qa-1` → `https://techcup-backend-qa-1-gva9hqfdeqard9bf.centralus-01.azurewebsites.net`
- PROD App Service: `techcup-backend-prod-1` → `https://techcup-backend-prod-1-awagabefhwadb2g9.centralus-01.azurewebsites.net`

### Docker local run
```bash
docker compose up -d postgres          # only the DB (for Maven dev)
docker compose up --build              # full stack: postgres + app on port 8443
cp .env.example .env                   # fill in local values before running full stack
```

## Error Handling

Global `@ControllerAdvice` (`GlobalExceptionHandler`) maps:

| Exception                                                   | HTTP | Notes                                                             |
|-------------------------------------------------------------|------|-------------------------------------------------------------------|
| `ResourceNotFoundException`                                 | 404  | Message passed through to client                                  |
| `BusinessRuleException`                                     | 409  | Message passed through to client                                  |
| `DataIntegrityViolationException`                           | 409  | DB-level unique constraint violation (safety net)                 |
| `MethodArgumentNotValidException`                           | 400  | Returns all field messages joined by `"; "`                       |
| `MultipartException` / `MissingServletRequestPartException` | 400  | Guides client to use `multipart/form-data` with `playerData` part |
| `HttpMediaTypeNotSupportedException`                        | 415  | Wrong Content-Type for the endpoint                               |
| `HttpMessageNotReadableException`                           | 400  | Malformed JSON body                                               |
| `Exception` (catch-all)                                     | 500  | Generic — logged with full stack trace                            |

All error responses use `ErrorResponse { status, error, message, path, timestamp }`.
