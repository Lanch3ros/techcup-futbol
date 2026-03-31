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

Prerequisites: Java 21, Maven 3.8+, PostgreSQL (not yet required — repositories use in-memory storage).

## Architecture

Layered architecture: **Controller → Service → Repository → Model**, with cross-cutting concerns in `core/`.

```
src/main/java/com/example/
├── controller/          # 7 REST controllers, all prefixed /api/v1
├── core/
│   ├── service/         # Business logic (7 services)
│   ├── model/           # Domain entities (14 classes)
│   ├── factory/         # PlayerFactory per player type (Factory Method pattern)
│   ├── validator/       # Email validators per player type (Strategy pattern)
│   └── exception/       # ResourceNotFoundException, BusinessRuleException
├── repository/          # In-memory HashMap repositories (7 total)
└── config/              # SwaggerConfig
```

### Domain Model

`Player` is an interface implemented by `StudentPlayer`, `TeacherPlayer`, `GraduatePlayer`, `RelativePlayer`, and `AdminPlayer` — all extending the abstract `User` class. Player type affects email validation rules and registration logic.

Key aggregates: `Tournament` → `Match` → `MatchEvent`, `Team` → `Player`, `Team` → `Payment`.

### Design Patterns in Use

- **Factory Method** (`core/factory/`): `PlayerFactory` dispatches to type-specific factories (e.g., `StudentFactory`, `TeacherFactory`) based on the registration request.
- **Strategy** (`core/validator/`): `EmailValidator` interface with implementations like `StudentEmailValidator` (@mail.escuelaing.edu.co) and `GmailValidator` (@gmail.com).
- **Command** (`MatchCommand`): Match events (goals, cards) are encapsulated as commands, enabling an audit trail.
- **Mapper**: `PlayerMapper`, `TeamMapper`, `TournamentMapper`, `LineupMapper` translate between domain models and DTOs.

### Repository Layer

All repositories use in-memory `HashMap<Long, Entity>` storage with auto-incrementing IDs. JPA dependencies (`spring-boot-starter-data-jpa`, PostgreSQL driver) are present but not yet wired — migration requires adding `@Entity`/`@Repository` JPA annotations.

### Security

Security dependencies are present (`spring-security`, `oauth2-client`, JJWT 0.11.5) but **no SecurityConfig exists** — all endpoints are currently unsecured. Planned: OAuth2/Google for authentication, JWT for stateless API sessions, RBAC with roles JUGADOR, CAPITAN, ORGANIZADOR, ARBITRO, ADMIN.

### Error Handling

Global `@ControllerAdvice` maps:
- `ResourceNotFoundException` → 404
- `BusinessRuleException` → 409
- `MethodArgumentNotValidException` → 400
- Unhandled exceptions → 500

All errors return a standard `ErrorResponse { httpStatus, statusCode, message, path }`.

## API Endpoints Summary

| Controller | Base Path | Responsibilities |
|---|---|---|
| PlayerController | `/api/v1/players` | Register, profile, availability, invitations |
| TeamController | `/api/v1/teams` | Create team, roster, lineup, invitations |
| TournamentController | `/api/v1/tournaments` | Lifecycle, standings, match generation |
| MatchController | `/api/v1/matches` | Create, results, events, lineups |
| PaymentController | `/api/v1/payments` | Upload, approve/reject workflow |
| RefereeController | `/api/v1/referees` | Register, assignment, schedule |
| StatsController | `/api/v1/stats` | Standings, top scorers |
