# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
**TechCup Fútbol** is a Spring Boot backend for managing a semi-annual engineering football tournament at Escuela Colombiana de Ingeniería Julio Garavito. It handles team registrations, player management, payment verification, match tracking, standings, and JWT-based authentication.

## Build & Run Commands
```bash
mvn clean install                        # Compile and resolve dependencies
mvn spring-boot:run -Dmaven.test.skip=true  # Start server at localhost:8080 (skips test compilation)
mvn clean test                           # Run all tests (437 tests, all green)
mvn test -Dtest=PlayerServiceTest        # Run a single test class
mvn clean test jacoco:report             # Tests + JaCoCo HTML report in target/site/jacoco/
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

**Prerequisites:** Java 21, Maven 3.8+, Docker (Colima on macOS) running PostgreSQL 16.

```bash
docker compose up -d   # Start PostgreSQL 16 on host port 5433
```

Environment variables (all have development defaults in `application.yaml`):
- `DB_URL` → `jdbc:postgresql://localhost:5433/techcup`
- `DB_USERNAME` / `DB_PASSWORD` → `techcup`
- `JWT_SECRET` → Base64-encoded 256-bit key (development default provided)

## Architecture

Layered: **Controller → Service → Repository → Model**, cross-cutting concerns in `core/`.

```
src/main/java/com/example/
├── config/          # SecurityConfig, JwtAuthenticationFilter, SwaggerConfig
├── controller/      # REST controllers (all prefixed /api/v1) + DTOs + Mappers
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
- Discriminator column: `user_type` = `STUDENT | TEACHER | GRADUATE | RELATIVE | ADMIN`
- Abstract `User implements Player`; concrete subtypes: `StudentPlayer`, `TeacherPlayer`, `GraduatePlayer`, `RelativePlayer`, `AdminPlayer`
- `PlayerRepository extends JpaRepository<User, Long>` — returns `User`, cast to `Player` in services when needed

**`Team.players`** is `@Transient` — never JPA-mapped. Always load players via `playerRepository.findByTeamId(teamId)`.

**`Invitation`** entity (`invitations` table) — persists each team→player invite with status `PENDING | ACCEPTED | REJECTED`. Created in `TeamService.sendInvitation()`, processed in `PlayerService.processInvitationResponse()`.

**Key relationships:**
- `Tournament.registeredTeams` → `@ManyToMany @JoinTable(name="tournament_teams")`
- `Match.homeTeam / awayTeam` → `@ManyToOne`
- `Match.events / lineups` → `@Transient` (loaded separately)
- `Referee.assignedMatchIds` → `@ElementCollection @CollectionTable(name="referee_matches")`

**`ddl-auto: update`** — Hibernate auto-creates/updates all tables on startup.

## Security (JWT)

HTTP Basic is **disabled**. All protected endpoints require a Bearer JWT.

**Authentication flow:**
1. `POST /api/v1/auth/login` → `AuthController` → `AuthenticationManager` (BCrypt verify) → `JwtService.generateToken()` → returns `{ token, type: "Bearer", email }`
2. Every subsequent request: `JwtAuthenticationFilter` (runs before `UsernamePasswordAuthenticationFilter`) extracts the token, validates via `JwtService.isTokenValid()`, loads user via `CustomUserDetailsService.loadUserByUsername(email)`, sets `SecurityContextHolder`

**`CustomUserDetailsService`** looks up the user by `email` in `PlayerRepository`. Role resolution: uses `user.getRole()` if set; otherwise `ADMIN` type → `ROLE_ADMIN`, anything else → `ROLE_JUGADOR`.

**`JwtService`** — HS256, 24h expiry. `@Value` fields `app.jwt.secret` and `app.jwt.expiration-ms`. Token includes a `"roles"` claim (list of Spring Security authority strings). `isTokenValid()` catches `JwtException` internally and returns `false` instead of propagating.

**Public endpoints:** Swagger UI, `POST /api/v1/auth/login`, `POST /api/v1/players/register`

**RBAC matrix:**

| Role | Key permissions |
|---|---|
| `ADMIN` | Everything |
| `ORGANIZADOR` | Tournament CRUD, match creation, referee assignment, payment approve/reject |
| `ARBITRO` | Match status, result, events |
| `CAPITAN` | Team creation, lineup, invitations, payment upload |
| `JUGADOR` | Read-only authenticated access |

## Key Business Rules

- **RN-03-4**: `>50%` of team players must belong to an engineering `Program` (SISTEMAS, IA, CIBERSEGURIDAD, ESTADISTICA). Maestría programs do **not** count. Enforced in `TeamService.validateEngineeringProgramComposition()`, triggered on `configureLineup()`.
- **RN-09-2**: Each finished match in which a team had zero cards awards +1 FairPlay point. Computed in `StatsService.calculateFairPlayPoints()` and added to base points in `getTeamStats()` / `getTournamentStandings()`.
- **RN-11-3**: Accepting an invitation auto-rejects all other `PENDING` invitations for the same player via `invitationRepository.saveAll()` in `PlayerService.processInvitationResponse()`.
- **RN-08-1**: `MatchService.registerResult()` throws `BusinessRuleException` if match status ≠ `"Finalizado"`.
- **Brackets (6.11)**: `TournamentService.generateQuarterFinals()` ranks top-8 teams by points → goal difference → goals scored, then pairs: 1v8, 2v7, 3v6, 4v5. Guards: tournament must be `"En progreso"`, no existing QF matches, ≥8 registered teams.

## Password & Registration

`PlayerService.registerPlayer()` calls `passwordEncoder.encode()` on the password **after** the factory builds the entity and **before** `playerRepository.save()`. Factories set the plaintext password; encoding always happens in the service layer.

## Testing Conventions

**437 tests, all green. JaCoCo coverage: 100% instructions, 100% lines, 100% methods, 100% classes, 99.6% branches.**

The single missed branch is a structural false negative in `JwtService#isTokenValid`: the `isTokenExpired() = true` path is unreachable because JJWT throws `ExpiredJwtException` before that boolean can return `true`. This is accepted as unavoidable.

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
- `PlayerService(PlayerRepository, PasswordEncoder, InvitationRepository)`
- `TeamService(TeamRepository, PlayerRepository, InvitationRepository)`

**Jackson configuration note:** Spring Boot 4.x uses Jackson 3.x (`tools.jackson`). `ACCEPT_CASE_INSENSITIVE_ENUMS` is a `MapperFeature` (not `DeserializationFeature`) in Jackson 3.x. The correct `application.yaml` key is `spring.jackson.mapper.accept-case-insensitive-enums: true`.

## CI/CD

GitHub Actions workflow at `.github/workflows/maven.yml`:
- Triggers on push to `main` / `feat/**` and PRs to `main`
- Spins up `postgres:16` as a service container (port 5433), waits for health-check
- Runs `mvn clean test` with secrets `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` injected as env vars
- Required GitHub secret value for `DB_URL`: `jdbc:postgresql://localhost:5433/techcup`

## Error Handling

Global `@ControllerAdvice` (`GlobalExceptionHandler`) maps:
- `ResourceNotFoundException` → 404
- `BusinessRuleException` → 409
- `MethodArgumentNotValidException` → 400
- Unhandled `Exception` → 500

All error responses: `ErrorResponse { status, message, path }`.
