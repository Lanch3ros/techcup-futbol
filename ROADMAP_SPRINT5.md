# ROADMAP Sprint 5 — Pruebas Funcionales HTTP por Funcionalidad

Basado en el requerimiento del Sprint 5: cada funcionalidad del backend debe tener una prueba funcional con cuatro flujos:
1. **Flujo Básico** — happy path completo
2. **Errores de Validación de Input** — campos inválidos, faltantes o con formato incorrecto
3. **Errores de Validaciones de Negocio** — reglas del dominio violadas (409, 403)
4. **Evidencia de Persistencia con Logging** — GET después de cada operación para confirmar que el estado quedó guardado en la BD

Archivo de referencia de estilo: `api-tests/01_flujo_torneo_completo.http`
Directorio destino: `api-tests/`

---

## Convenciones de estilo (basadas en el archivo existente)

- Secciones con cabeceras ASCII y comentarios en español
- Variables: `{actor}_token`, `{recurso}_id`, `{tipo}_{letra}_id`
- Response handlers JavaScript: `client.global.set(...)` + `client.log(...)`
- Separador de requests: `###`
- Ambiente: `{{host}}` → `https://localhost:8443/api/v1`
- Flujos de error no capturan variables (no tienen response handler)

---

## Fase 1 — `02_autenticacion.http`

**Funcionalidad:** Autenticación JWT nativa y Google OAuth2.

### Endpoints involucrados
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/google`

### Flujos a cubrir

#### Flujo Básico
1. Login exitoso como ADMIN (`admin@techcup.edu.co` / `Admin123*`) → capturar `adm_token`
2. Login exitoso como ORGANIZADOR (`organizador@techcup.edu.co`) → capturar `org_token`
3. Login exitoso como ÁRBITRO (`arbitro@techcup.edu.co`) → capturar `arb_token`
4. Verificar que el token funciona: `GET /api/v1/players` con `Authorization: Bearer {{adm_token}}`

#### Errores de Validación de Input
- Login con `email` vacío → 400
- Login con `password` vacío → 400
- Login con JSON malformado → 400
- `POST /auth/google` con `idToken` vacío → 400

#### Errores de Validaciones de Negocio
- Login con email correcto pero password incorrecto → 401
- Login con email que no existe → 401
- Request a endpoint protegido sin token → 403
- Request a endpoint protegido con token malformado → 403
- Request a endpoint protegido con token expirado (token inventado con formato válido pero firma inválida) → 403

#### Evidencia de Persistencia
- Después del login exitoso: `GET /api/v1/players` usando el token obtenido → confirma que el JWT generado es válido y el usuario existe en BD

### Reglas de negocio relevantes
- Contraseña verificada con BCrypt
- JWT HS256 con TTL 1 hora
- Sin HTTP Basic — solo Bearer JWT
- Google OAuth2: si el usuario no existe, se crea como `RelativePlayer` con rol `JUGADOR`

---

## Fase 2 — `03_registro_jugadores.http`

**Funcionalidad:** Registro de jugadores de todos los tipos (Strategy + Factory Method) y árbitros.

### Endpoints involucrados
- `POST /api/v1/players/register` (JSON y multipart)
- `POST /api/v1/referees`
- `GET /api/v1/players`
- `GET /api/v1/players/{id}`

### Setup requerido
- Login inicial para capturar `adm_token` y `org_token`

### Flujos a cubrir

#### Flujo Básico
1. Registrar `StudentPlayer` (STUDENT) → capturar ID por email
2. Registrar `StudentPlayer` como CAPTAIN (userType=CAPITAN) → capturar ID
3. Registrar `TeacherPlayer` (TEACHER)
4. Registrar `GraduatePlayer` (GRADUATE)
5. Registrar `RelativePlayer` (RELATIVE) — email Gmail
6. Registrar `RefereeUser` vía `POST /referees` con licencia
7. `GET /api/v1/players` → mapear emails a IDs con response handler JavaScript
8. `GET /api/v1/players/{id}` → verificar campos del perfil

#### Errores de Validación de Input
- `name` vacío y sin `firstName`/`lastName` → 400 (`isNameProvided()` falla)
- `email` con formato inválido → 400
- `position` con valor fuera del patrón (`(?i)^(Portero|Defensa|Volante|Delantero)$`) → 400
- `userType` vacío o nulo → 400
- Referee sin `licenseNumber` → 400/409
- `jerseyNumber` negativo (si aplica validación) → 400

#### Errores de Validaciones de Negocio
- Registrar jugador con email ya existente → 409 (`"El correo '...' ya está registrado."`)
- Registrar jugador con identificación ya existente → 409
- Referee con licencia duplicada → 409
- StudentPlayer con email de dominio incorrecto (ej. `@gmail.com`) → 400
- TeacherPlayer con email de `@mail.escuelaing.edu.co` (dominio de estudiante) → 400

#### Evidencia de Persistencia
- Después de cada registro exitoso: `GET /api/v1/players/{id}` para confirmar que los campos (name, email, userType, position, jerseyNumber) quedaron guardados correctamente

### Reglas de negocio relevantes
- `CAPTAIN`/`CAPITAN`/`CAPTAN` → normaliza a `STUDENT` + `securityRole=CAPITAN`
- `PLAYER`/`JUGADOR` → normaliza a `STUDENT`
- `age` → convierte a `birthDate`
- `jerseyNumber` null → default 10
- `position` → normaliza a title-case
- Validación de dominio de email por tipo (Strategy)
- Password encoding después de factory, antes de save

---

## Fase 3 — `04_gestion_equipos.http`

**Funcionalidad:** Creación de equipos, invitaciones, respuesta a invitaciones, alineación y eliminación de jugadores.

### Endpoints involucrados
- `POST /api/v1/teams`
- `GET /api/v1/teams`, `GET /api/v1/teams/{id}`
- `GET /api/v1/teams/{id}/players`
- `POST /api/v1/teams/{id}/invitations`
- `PATCH /api/v1/players/invitations/{invitationId}`
- `DELETE /api/v1/teams/{id}/players/{playerId}`
- `PUT /api/v1/teams/{id}/lineup`
- `GET /api/v1/teams/{id}/lineup`

### Setup requerido
- Login de CAPITÁN y ADMIN
- Al menos 11 jugadores registrados (7 titulares mínimo + reservas)

### Flujos a cubrir

#### Flujo Básico
1. Crear equipo → capturar `team_id`
2. `GET /teams` → confirmar creación
3. Enviar invitación a jugador → capturar `invitation_id`
4. Jugador acepta invitación (`ACCEPTED`)
5. Repetir invitación y aceptación hasta tener ≥7 jugadores
6. Verificar que jugadores secundarios reciben rechazo automático (RN-11-3)
7. `PUT /teams/{id}/lineup` con 7-11 titulares y reservas válidos
8. `GET /teams/{id}/lineup` → confirmar alineación

#### Errores de Validación de Input
- Crear equipo sin `name` → 400
- `PUT /lineup` con menos de 7 titulares (`@Size(min=7)`) → 400
- `PUT /lineup` con más de 11 titulares → 400
- Respuesta a invitación con `action` inválido (ni `ACCEPTED` ni `REJECTED`) → 400/409

#### Errores de Validaciones de Negocio
- Invitar a jugador que ya tiene equipo → 409
- Invitar a jugador cuando ya tiene invitación `PENDING` del mismo equipo → 409
- Invitar más de 12 jugadores al equipo → 409
- `PUT /lineup` con jugadores que no pertenecen al equipo → 409
- `PUT /lineup` con menos del 50% de jugadores de programas de ingeniería (RN-03-4: SISTEMAS, IA, CIBERSEGURIDAD, ESTADISTICA) → 409
- Eliminar jugador de equipo cuyo torneo está `"En progreso"` (plantilla congelada) → 409
- Enviar invitación cuando el torneo está `"En progreso"` → 409

#### Evidencia de Persistencia
- `GET /teams/{id}/players` después de cada invitación aceptada → confirma que el jugador aparece en el equipo
- `GET /teams/{id}/lineup` después de configurar alineación → confirma titulares y reservas guardados

### Reglas de negocio relevantes
- RN-03-4: >50% titulares deben ser de programas de ingeniería (no cuentan maestrías)
- RN-11-3: Aceptar una invitación auto-rechaza todas las demás `PENDING` del mismo jugador
- Plantilla congelada cuando el torneo del equipo está `"En progreso"`
- Máximo 12 jugadores por equipo

---

## Fase 4 — `05_pagos.http`

**Funcionalidad:** Subida de comprobante de pago, ciclo de aprobación/rechazo/revisión.

### Endpoints involucrados
- `POST /api/v1/payments/upload` (multipart)
- `POST /api/v1/payments` (JSON)
- `GET /api/v1/payments`
- `GET /api/v1/payments/{id}`
- `GET /api/v1/payments/team/{teamId}`
- `PATCH /api/v1/payments/{id}/approve`
- `PATCH /api/v1/payments/{id}/reject`
- `PATCH /api/v1/payments/{id}/review`

### Setup requerido
- Login de CAPITÁN y ORGANIZADOR
- Equipo creado → `team_id`
- Archivo `payment.jpg` en `api-tests/`

### Flujos a cubrir

#### Flujo Básico
1. `POST /payments/upload` con `payment.jpg` (multipart) → capturar `payment_id`
2. `GET /payments/team/{teamId}` → confirmar pago asociado al equipo
3. `PATCH /payments/{id}/review` → estado pasa a `"En revisión"`
4. `GET /payments/{id}` → confirmar estado
5. `PATCH /payments/{id}/approve` con `approvedBy` → estado pasa a `"Aprobado"`
6. `GET /payments/{id}` → confirmar estado final
7. Flujo alternativo: `PATCH /payments/{id}/reject` con `comments` → estado `"Rechazado"`

#### Errores de Validación de Input
- `POST /payments/upload` sin archivo → 400
- `POST /payments/upload` con archivo de tipo inválido (ej. `.txt`) → 400
- `PATCH /payments/{id}/approve` con `approvedBy` vacío → 400/409
- `PATCH /payments/{id}/reject` sin `comments` → 400/409

#### Errores de Validaciones de Negocio
- `PATCH /payments/{id}/approve` sobre pago ya aprobado → 409
- `PATCH /payments/{id}/reject` sobre pago ya aprobado → 409
- `PATCH /payments/{id}/review` sobre pago que no está en `"Pendiente"` → 409
- `GET /payments/{id}` con ID inexistente → 404

#### Evidencia de Persistencia
- `GET /payments/{id}` después de cada cambio de estado → confirma transición correcta
- `GET /payments/team/{teamId}` → confirma asociación equipo↔pago

### Reglas de negocio relevantes
- Solo archivos con extensión y content-type válidos (imagen)
- El pago aprobado es requisito para inscribir equipo al torneo
- Estados válidos: `Pendiente → En revisión → Aprobado / Rechazado`

---

## Fase 5 — `06_gestion_torneos.http`

**Funcionalidad:** Ciclo de vida del torneo: creación, inscripción de equipos, generación de partidos, cuartos de final, inicio y fin.

### Endpoints involucrados
- `POST /api/v1/tournaments`
- `GET /api/v1/tournaments`, `GET /api/v1/tournaments/{id}`
- `GET /api/v1/tournaments/{id}/teams`
- `GET /api/v1/tournaments/{id}/standings`
- `GET /api/v1/tournaments/{id}/bracket`
- `POST /api/v1/tournaments/{id}/teams/{teamId}`
- `POST /api/v1/tournaments/{id}/generate-matches`
- `POST /api/v1/tournaments/{id}/generate-quarter-finals`
- `POST /api/v1/tournaments/{id}/start`
- `POST /api/v1/tournaments/{id}/finish`
- `PATCH /api/v1/tournaments/{id}/status`

### Setup requerido
- Login ORGANIZADOR
- ≥2 equipos con pago aprobado (≥8 para cuartos de final)

### Flujos a cubrir

#### Flujo Básico
1. `POST /tournaments` → capturar `tournament_id`
2. `GET /tournaments` → confirmar creación
3. `POST /tournaments/{id}/teams/{teamId}` para inscribir equipos
4. `POST /tournaments/{id}/start` → estado pasa a `"En progreso"`
5. `POST /tournaments/{id}/generate-matches` → genera fixture round-robin
6. `GET /tournaments/{id}/teams` → lista equipos inscritos
7. `POST /tournaments/{id}/generate-quarter-finals` (con ≥8 equipos con puntos) → genera brackets
8. `GET /tournaments/{id}/bracket` → confirmar estructura
9. `POST /tournaments/{id}/finish` → estado pasa a `"Finalizado"`

#### Errores de Validación de Input
- Crear torneo sin `name` → 400
- Crear torneo con `endDate` anterior a `startDate` → 409
- `PATCH /tournaments/{id}/status` con estado inválido → 409

#### Errores de Validaciones de Negocio
- Inscribir equipo sin pago aprobado → 409
- Inscribir equipo ya inscrito → 409
- `POST /generate-matches` con menos de 2 equipos → 409
- `POST /generate-quarter-finals` con menos de 8 equipos → 409
- `POST /generate-quarter-finals` cuando ya existen partidos de cuartos → 409
- `POST /generate-quarter-finals` en torneo que no está `"En progreso"` → 409
- Inscribir equipo en torneo que no está `"Activo"` → 409
- Transición de estado inválida (ej. `"Finalizado"` → `"Activo"`) → 409

#### Evidencia de Persistencia
- `GET /tournaments/{id}` después de cada cambio de estado
- `GET /tournaments/{id}/teams` después de cada inscripción
- `GET /tournaments/{id}/standings` después de generar partidos

### Reglas de negocio relevantes
- Estados válidos: `Activo → En progreso → Finalizado`
- Inscripción solo en estado `"Activo"`
- Generación de partidos requiere `"En progreso"`
- Cuartos: top-8 por puntos → diferencia de goles → goles anotados; pares: 1v8, 2v7, 3v6, 4v5
- Cuartos: no duplicar si ya existen; mínimo 8 equipos registrados

---

## Fase 6 — `07_gestion_partidos.http`

**Funcionalidad:** Cambios de estado de partidos, registro de resultado, eventos (goles/tarjetas), asignación de árbitro y consulta de alineaciones.

### Endpoints involucrados
- `POST /api/v1/matches`
- `GET /api/v1/matches`, `GET /api/v1/matches/{id}`
- `PATCH /api/v1/matches/{id}/status`
- `PATCH /api/v1/matches/{id}/result`
- `POST /api/v1/matches/{id}/events`
- `GET /api/v1/matches/{id}/events`
- `GET /api/v1/matches/{id}/lineups`
- `PATCH /api/v1/matches/{id}/referee`

### Setup requerido
- Login ORGANIZADOR, ÁRBITRO
- Torneo `"En progreso"`, al menos 1 partido generado → `match_id`
- Árbitro creado → `referee_id`
- Dos equipos con jugadores y alineaciones configuradas

### Flujos a cubrir

#### Flujo Básico
1. `PATCH /matches/{id}/referee` → asignar árbitro al partido
2. `GET /matches/{id}` → confirmar árbitro asignado
3. `PATCH /matches/{id}/status` con `{ "status": "En Curso" }`
4. `POST /matches/{id}/events` → registrar gol (tipo `"Gol"`, playerId, minuto)
5. `POST /matches/{id}/events` → registrar tarjeta amarilla
6. `GET /matches/{id}/events` → confirmar eventos registrados
7. `PATCH /matches/{id}/status` con `{ "status": "Finalizado" }`
8. `PATCH /matches/{id}/result` con marcador final (`homeScore`, `awayScore`)
9. `GET /matches/{id}/lineups` → confirmar alineaciones

#### Errores de Validación de Input
- `PATCH /matches/{id}/status` con `status` vacío → 400
- `POST /matches/{id}/events` sin `playerId` → 400
- `POST /matches/{id}/events` sin `eventType` → 400
- `PATCH /matches/{id}/result` sin `homeScore` o `awayScore` → 400

#### Errores de Validaciones de Negocio
- `PATCH /matches/{id}/result` cuando estado ≠ `"Finalizado"` (RN-08-1) → 409
- `PATCH /matches/{id}/status` con transición inválida (`"Finalizado"` → `"En Curso"`) → 409
- `PATCH /matches/{id}/status` con `"En progreso"` en lugar de `"En Curso"` (string exacto) → 409
- Crear partido con `homeTeamId == awayTeamId` → 409
- `PATCH /matches/{id}/referee` con `refereeId` inexistente → 404

#### Evidencia de Persistencia
- `GET /matches/{id}` después de cada cambio de estado → confirma transición
- `GET /matches/{id}/events` después de cada evento → confirma que goles y tarjetas persisten
- `GET /stats/teams/{teamId}` después de registrar resultado → confirma actualización de puntos (GAP-13: 3/1/0)

### Reglas de negocio relevantes
- RN-08-1: Resultado solo registrable en estado `"Finalizado"`
- GAP-13: `registerResult()` actualiza stats de ambos equipos (matchesPlayed, goalsFor, goalsAgainst, points)
- RN-09-2: Si un equipo termina un partido sin tarjetas → +1 FairPlay point
- Transiciones válidas: `Programado → En Curso → Finalizado`
- String exacto: `"En Curso"` (no `"En progreso"`)

---

## Fase 7 — `08_arbitros.http`

**Funcionalidad:** Registro de árbitros, consulta y partidos asignados.

### Endpoints involucrados
- `POST /api/v1/referees`
- `GET /api/v1/referees`
- `GET /api/v1/referees/{id}`
- `GET /api/v1/referees/{id}/matches`

### Setup requerido
- Login ADMIN u ORGANIZADOR

### Flujos a cubrir

#### Flujo Básico
1. `POST /referees` con nombre, email, identificación, licencia → capturar `referee_id`
2. `GET /referees` → confirmar árbitro en lista
3. `GET /referees/{id}` → verificar todos los campos
4. Asignar árbitro a un partido (`PATCH /matches/{id}/referee`)
5. `GET /referees/{id}/matches` → confirmar que el partido aparece en la lista

#### Errores de Validación de Input
- `POST /referees` sin `licenseNumber` → 400/409
- `POST /referees` sin `email` → 400
- `POST /referees` sin `name` → 400

#### Errores de Validaciones de Negocio
- `POST /referees` con email ya registrado → 409
- `POST /referees` con identificación ya registrada → 409
- `POST /referees` con `licenseNumber` duplicado → 409
- `GET /referees/{id}` con ID inexistente → 404

#### Evidencia de Persistencia
- `GET /referees/{id}` después del registro → confirma todos los campos guardados
- `GET /referees/{id}/matches` después de asignación → confirma relación árbitro↔partido

### Reglas de negocio relevantes
- `licenseNumber` único por árbitro (`@Column(unique=true)`)
- Email e identificación únicos en tabla `users`
- `role = "ARBITRO"`, discriminador `REFEREE`

---

## Fase 8 — `09_estadisticas.http`

**Funcionalidad:** Estadísticas de jugadores, equipos, goleadores y tabla de posiciones del torneo.

### Endpoints involucrados
- `GET /api/v1/stats/top-scorers`
- `GET /api/v1/stats/tournaments/{tournamentId}/top-scorers`
- `GET /api/v1/stats/players/{id}`
- `GET /api/v1/stats/teams/{id}`
- `GET /api/v1/tournaments/{id}/standings`

### Setup requerido
- Torneo con al menos 2 partidos `"Finalizado"` con resultados y eventos registrados
- Jugadores con goles registrados en `match_events`

### Flujos a cubrir

#### Flujo Básico
1. `GET /stats/top-scorers` → lista global de goleadores (incluye todos los torneos)
2. `GET /stats/tournaments/{tournamentId}/top-scorers` → goleadores del torneo específico
3. `GET /stats/players/{id}` → stats de un jugador concreto (`matchesPlayed`, `goals`, `yellowCards`, `redCards`, `teamName`)
4. `GET /stats/teams/{id}` → stats de un equipo (`points`, `matchesPlayed`, `goalsFor`, `goalsAgainst`, `goalDifference`, `fairPlayPoints`)
5. `GET /tournaments/{id}/standings` → tabla de posiciones completa del torneo ordenada

#### Errores de Validación de Input
- N/A (todos son GETs con path variables numéricas — los errores son de negocio)

#### Errores de Validaciones de Negocio
- `GET /stats/players/{id}` con ID inexistente → 404
- `GET /stats/teams/{id}` con ID inexistente → 404
- `GET /stats/tournaments/{id}/top-scorers` con torneo inexistente → 404

#### Evidencia de Persistencia
- Registrar un gol nuevo en un partido `"En Curso"` y luego finalizarlo
- Ejecutar `GET /stats/players/{playerId}` → confirma que el gol se refleja en `goals`
- Ejecutar `GET /stats/teams/{teamId}` → confirma `points` y `fairPlayPoints` actualizados (RN-09-2)
- `GET /tournaments/{id}/standings` → confirma orden correcto por puntos → diferencia de goles → goles anotados

### Reglas de negocio relevantes
- GAP-14: `getTournamentStandings()` filtra solo equipos inscritos en el torneo
- GAP-15: `getTopScorersByTournament()` filtra eventos solo de partidos del torneo
- GAP-16: `getPlayerStats()` — `matchesPlayed` = partidos `"Finalizado"` donde el jugador tuvo al menos un evento
- RN-09-2: +1 FairPlay por partido finalizado sin tarjetas
- Orden tabla: puntos → diferencia de goles → goles anotados

---

## Resumen de archivos a crear

| # | Archivo | Funcionalidad | Endpoints principales |
|---|---------|---------------|----------------------|
| 1 | `02_autenticacion.http` | JWT login + Google OAuth2 | `/auth/login`, `/auth/google` |
| 2 | `03_registro_jugadores.http` | Registro todos los tipos + árbitros | `/players/register`, `/referees` |
| 3 | `04_gestion_equipos.http` | CRUD equipos + invitaciones + alineación | `/teams/**`, `/players/invitations/**` |
| 4 | `05_pagos.http` | Upload + aprobación/rechazo pagos | `/payments/**` |
| 5 | `06_gestion_torneos.http` | Ciclo de vida torneo + brackets | `/tournaments/**` |
| 6 | `07_gestion_partidos.http` | Estado + resultado + eventos partido | `/matches/**` |
| 7 | `08_arbitros.http` | CRUD árbitros + asignación | `/referees/**` |
| 8 | `09_estadisticas.http` | Stats jugadores + equipos + tabla | `/stats/**` |

**Total: 8 archivos nuevos** (más el existente `01_flujo_torneo_completo.http`)

## Orden de implementación recomendado

```
02_autenticacion (independiente)
    └─► 03_registro_jugadores (necesita tokens)
            └─► 07_arbitros (necesita admin token)
            └─► 04_equipos (necesita jugadores registrados)
                    └─► 05_pagos (necesita equipos)
                            └─► 06_torneos (necesita equipos con pago aprobado)
                                    └─► 07_partidos (necesita torneo en progreso)
                                            └─► 09_estadisticas (necesita partidos finalizados)
```
