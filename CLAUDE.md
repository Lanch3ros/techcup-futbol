# CLAUDE.md
This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
**TechCup Fútbol** is a Spring Boot backend for managing a semi-annual engineering football tournament at Escuela Colombiana de Ingeniería Julio Garavito. It handles team registrations, player management, payment verification, match tracking, and standings calculation.

## Build & Run Commands
```bash
mvn clean install        # Compile and resolve dependencies
mvn spring-boot:run      # Start server at localhost:8080
mvn test                 # Run all tests with JaCoCo coverage
mvn test -Dtest=PlayerServiceTest  # Run a single test class
```

Swagger UI is available at `http://localhost:8080/swagger-ui.html` once running.

Prerequisites: Java 21, Maven 3.8+, PostgreSQL 16 running in Docker (Host port **5433** → Container 5432).

## Architecture & Infrastructure
Layered architecture: **Controller → Service → Repository → Model**, with cross-cutting concerns in `core/`.

- **Database:** PostgreSQL 16 running in Docker.
- **Port Mapping:** Host **5433** -> Container 5432 (to avoid conflicts with local DBs).
- **ORM:** Spring Data JPA with Hibernate. `ddl-auto: update`.
```
src/main/java/com/example/
├── controller/          # REST controllers, all prefixed /api/v1
├── core/
│   ├── service/         # Business logic
│   ├── model/           # Domain entities
│   ├── factory/         # PlayerFactory per player type (Factory Method pattern)
│   ├── validator/       # Email validators per player type (Strategy pattern)
│   └── exception/       # ResourceNotFoundException, BusinessRuleException
├── repository/          # Spring Data JPA repositories
└── config/              # SwaggerConfig, SecurityConfig
```

## Domain Model & Persistence

- **User Hierarchy:** Uses `SINGLE_TABLE` inheritance strategy in the `users` table.
    - Discriminator column: `user_type` (STUDENT, TEACHER, GRADUATE, etc.).
    - Abstract `User` class implements `Player` interface.
- **Key Entities:** `Team`, `Player`, `Match`, `Tournament`, `Payment`, `Referee`, `MatchEvent`.
- **Relationships:**
    - `Team` ↔ `Player` (@OneToMany/ManyToOne)
    - `Tournament` ↔ `Team` (@ManyToMany via join table)
    - `Match` ↔ `Team` (home/away associations)

### Design Patterns in Use
- **Factory Method** (`core/factory/`): `PlayerFactory` dispatches to type-specific factories (e.g., `StudentFactory`, `TeacherFactory`) based on the registration request.
- **Strategy** (`core/validator/`): `EmailValidator` interface with implementations like `StudentEmailValidator` (@mail.escuelaing.edu.co) and `GmailValidator` (@gmail.com).
- **Command** (`MatchCommand`): Match events (goals, cards) are encapsulated as commands, enabling an audit trail.
- **Mapper**: `PlayerMapper`, `TeamMapper`, `TournamentMapper`, `LineupMapper` translate between domain models and DTOs.

## Security & RBAC
Implemented via `SecurityConfig` with RBAC (Role-Based Access Control).

- **Roles:** ADMIN, ORGANIZADOR, ARBITRO, CAPITAN, JUGADOR.
- **Current State:** HTTP Basic enabled. User persistence in DB is the next implementation step (replacing in-memory users).

### Error Handling
Global `@ControllerAdvice` maps:
- `ResourceNotFoundException` → 404
- `BusinessRuleException` → 409
- `MethodArgumentNotValidException` → 400
- Unhandled exceptions → 500

All errors return a standard `ErrorResponse { httpStatus, statusCode, message, path }`.

## Testing & Quality
- **Total Tests:** 119 tests (All Green ✅).
- **Coverage:** 28.8% Total / **30.5%** in `core.service`.
- **Stabilization:** All tests use Mocks for `JpaRepository` and support the JPA entity model.

## API Endpoints Summary

| Controller | Base Path | Key Features |
|---|---|---|
| PlayerController | `/api/v1/players` | Registration, Profile, Program validation |
| TeamController | `/api/v1/teams` | Team creation, >50% Engineering rule |
| TournamentController | `/api/v1/tournaments` | Seeding (1v8, 2v7…), Brackets generation |
| MatchController | `/api/v1/matches` | Status updates, Result registration (FINISHED guard) |
| PaymentController | `/api/v1/payments` | Multipart upload (JPG, PNG, PDF), FairPlay bonus |
