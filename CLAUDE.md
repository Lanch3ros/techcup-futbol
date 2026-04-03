# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
**TechCup Fútbol** is a Spring Boot backend for managing a semi-annual engineering football tournament at Escuela Colombiana de Ingeniería Julio Garavito. It handles team registrations, player management, payment verification, match tracking, standings, and JWT-based authentication.

## Build & Run Commands
```bash
mvn clean install                        # Compile and resolve dependencies
mvn spring-boot:run -Dmaven.test.skip=true  # Start server at localhost:8080 (skips test compilation)
mvn clean test                           # Run all tests (498 tests, all green)
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
├── config/          # SecurityConfig, JwtAuthenticationFilter, SwaggerConfig, DatabaseSeeder
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
- Discriminator column: `user_type` = `STUDENT | TEACHER | GRADUATE | RELATIVE | ADMIN | ORGANIZER | REFEREE`
- Abstract `User` base class; concrete subtypes: `StudentPlayer`, `TeacherPlayer`, `GraduatePlayer`, `RelativePlayer`, `StaffPlayer`, `AdminUser`, `OrganizerUser`, `RefereeUser`
- `UserRepository extends JpaRepository<User, Long>` — returns `User`, cast to `Player` in services when needed

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

| Email | Type | Role | Discriminator |
|-------|------|------|---------------|
| `admin@techcup.edu.co` | `AdminUser` | `ADMIN` | `ADMIN` |
| `organizador@techcup.edu.co` | `OrganizerUser` | `ORGANIZADOR` | `ORGANIZER` |
| `arbitro@techcup.edu.co` | `RefereeUser` | `ARBITRO` | `REFEREE` |

Default password for all: `Admin123*` (BCrypt-encoded at startup).

## Security (JWT)

HTTP Basic is **disabled**. All protected endpoints require a Bearer JWT.

**Authentication flow:**
1. `POST /api/v1/auth/login` → `AuthController` → `AuthenticationManager` (BCrypt verify) → `JwtService.generateToken()` → returns `{ token, type: "Bearer", email }`
2. Every subsequent request: `JwtAuthenticationFilter` (runs before `UsernamePasswordAuthenticationFilter`) extracts the token, validates via `JwtService.isTokenValid()`, loads user via `CustomUserDetailsService.loadUserByUsername(email)`, sets `SecurityContextHolder`

**`CustomUserDetailsService`** looks up the user by `email` in `UserRepository`. Role resolution: uses `user.getRole()` if non-blank; otherwise resolves by type — `AdminUser` → `ROLE_ADMIN`, `OrganizerUser` → `ROLE_ORGANIZADOR`, `RefereeUser` → `ROLE_ARBITRO`, anything else → `ROLE_JUGADOR`.

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
- **RN-11-3**: Accepting an invitation auto-rejects all other `PENDING` invitations for the same player via `invitationRepository.saveAll()` in `PlayerService.respondToInvitation()`.
- **RN-08-1**: `MatchService.registerResult()` throws `BusinessRuleException` if match status ≠ `"Finalizado"`.
- **GAP-13**: `MatchService.registerResult()` updates both teams' stats after saving the match result: `matchesPlayed`, `matchesWon/Lost/Drawn`, `goalsFor`, `goalsAgainst`, `goalDifference`, `points` (3/1/0).
- **Plantilla congelada**: `TeamService.sendInvitation()` and `removePlayer()` throw `BusinessRuleException` if the team's tournament is `"En progreso"`.
- **Brackets (6.11)**: `TournamentService.generateQuarterFinals()` ranks top-8 teams by points → goal difference → goals scored, then pairs: 1v8, 2v7, 3v6, 4v5. Guards: tournament must be `"En progreso"`, no existing QF matches, ≥8 registered teams.

## Statistics Engine (Phase 4)

- **GAP-14** `StatsService.getTournamentStandings()` — filters to only registered teams of the tournament via `tournament.getRegisteredTeams()`.
- **GAP-15** `StatsService.getTopScorersByTournament()` — scopes events to match IDs from `tournament.getMatches()` using `matchEventRepository.findByMatchIdIn()`.
- **GAP-16** `StatsService.getPlayerStats()` — computes `matchesPlayed` as distinct finished matches where the player had at least one event; resolves `teamName` via `teamRepository.findById(player.getTeamId())`.

`StatsService` constructor: `StatsService(TeamRepository, MatchRepository, MatchEventRepository, UserRepository, TournamentRepository)` — 5 args.

## Password & Registration

`PlayerService.registerPlayer()` calls `passwordEncoder.encode()` on the password **after** the factory builds the entity and **before** `userRepository.save()`. Factories set the plaintext password; encoding always happens in the service layer.

## Testing Conventions

**498 tests, all green. JaCoCo coverage: 100% instructions, 100% lines, 100% methods, 100% classes, 99.2% branches (356/359).**

The 3 missed branches are accepted structural false negatives:

| Class | Branch | Reason |
|-------|--------|--------|
| `JwtService#isTokenValid` | `isTokenExpired() = true` | JJWT throws `ExpiredJwtException` before the boolean can return `true` |
| `TeamService#isValidProgram` | non-null, non-engineering, non-master program | No `Program` enum value meets this condition |
| `TeamService#isMasterProgram` | false path | Same reason — impossible with current enum values |

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
