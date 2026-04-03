# Roadmap: Refactorización y Remediación de Gaps (refactor/user-hierarchy-and-gaps)

Este plan prioriza la corrección de la jerarquía de usuarios antes de implementar las brechas de requerimientos del documento TechCup.pdf.

## Fase 0: Reestructuración de la Jerarquía de Usuarios (CRÍTICO)
**Objetivo:** Separar la identidad (User) del comportamiento de juego (Player).
* **Paso 0.1:** Modificar `User.java`. Eliminar la implementación de `Player`. Asegurar que sea la base `@Inheritance(strategy = SINGLE_TABLE)`.
* **Paso 0.2:** Renombrar `PlayerRepository` a `UserRepository`. Este será el origen de datos para `CustomUserDetailsService`.
* **Paso 0.3:** Crear/Ajustar la interfaz `Player` con los métodos deportivos.
* **Paso 0.4:** Refactorizar las clases de jugadores (`StudentPlayer`, `TeacherPlayer`, etc.) para que extiendan de `User` e implementen `Player`.
* **Paso 0.5:** Crear `AdminUser` y `OrganizerUser` extendiendo de `User` (sin implementar `Player`).
* **Paso 0.6:** Migrar la lógica de `Referee` a una nueva entidad `RefereeUser` que extienda de `User`. Esto permite que el árbitro pueda autenticarse en el sistema.

## Fase 1: Dominio y Perfil (GAP-01, GAP-03, GAP-04, GAP-05)
* **Paso 1.1:** Crear `StaffUser` (Personal Administrativo que juega). Implementar su validador de email y añadirlo al `PlayerFactory`.
* **Paso 1.2:** Añadir a `User` los campos `birthDate`, `gender` e `identification`. Añadir `semester` solo a `StudentUser`.
* **Paso 1.3:** Actualizar `ProfileDTO` para exponer todos los campos del perfil deportivo y personal.
* **Paso 1.4:** Añadir `shieldUrl` a `Team` y crear su endpoint de actualización.
* **Paso 1.5:** Forzar la validación de email en `PlayerService.registerPlayer()` usando el Strategy pattern existente.

## Fase 2: Reglas de Equipos y Alineaciones (GAP-06 al GAP-12)
* **Paso 2.1:** Ajustar validación de programas para que el 100% de los jugadores pertenezcan a programas válidos (Ingeniería o Maestrías específicas).
* **Paso 2.2:** Validar que un jugador no tenga equipo (`teamId == null`) antes de enviarle una invitación.
* **Paso 2.3:** Implementar persistencia de suplentes (`reservePlayerIds`) en las alineaciones.
* **Paso 2.4:** Bloquear cambios en equipos (eliminar/invitar) si el torneo ya está "En progreso" (Congelamiento de plantilla).

## Fase 3: Flujo de Torneos y Pagos (GAP-02, GAP-08, GAP-09)
* **Paso 3.1:** Crear endpoints `POST /start` y `/finish` para torneos con validaciones de transición de estado.
* **Paso 3.2:** Habilitar el estado "En revisión" para pagos mediante un endpoint dedicado.
* **Paso 3.3:** Impedir la inscripción de un equipo al torneo si no tiene un pago en estado "Aprobado".

## Fase 4: Motor de Estadísticas y Posiciones (GAP-13 al GAP-16)
* **Paso 4.1:** Implementar el cálculo de estadísticas (PJ, PG, PE, PP, GF, GC, Puntos) en `MatchService.registerResult()`.
* **Paso 4.2:** Corregir `StatsService` para que los standings y goleadores se filtren estrictamente por el `tournamentId`.
* **Paso 4.3:** Completar el cálculo de `matchesPlayed` y `teamName` en las estadísticas individuales del jugador.