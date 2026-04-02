# Roadmap: Global Case-Insensitivity (feature/case-insensitive-validation)

Este documento define las fases estrictas para implementar validaciones insensibles a mayúsculas y minúsculas en TechCup Fútbol. Ejecutar en orden secuencial.

## Fase 1: Configuración de Deserialización (Jackson)
**Objetivo:** Permitir que los endpoints REST acepten Enums en cualquier formato de mayúsculas/minúsculas.
* **Paso 1.1:** Abrir `src/main/resources/application.yaml`.
* **Paso 1.2:** Localizar el bloque de configuración de `spring: jackson:`.
* **Paso 1.3:** Añadir la propiedad `deserialization.accept-case-insensitive-enums: true`.

## Fase 2: Capa de Persistencia (Repositories)
**Objetivo:** Modificar las consultas a PostgreSQL para ignorar el case al buscar por estados de texto.
* **Paso 2.1:** Escanear `src/main/java/com/example/repository/` en busca de consultas por estado (ej. `TournamentRepository`).
* **Paso 2.2:** Renombrar los *Query Methods* derivados que involucren texto de estados para incluir el sufijo `IgnoreCase` (ej. cambiar `findByStatus` a `findByStatusIgnoreCase`).
* **Paso 2.3:** Modificar las consultas explícitas (`@Query`) donde se comparen cadenas de estado o reglas de negocio para usar la función SQL `LOWER()` en ambos lados de la igualdad (ej. `LOWER(t.status) = LOWER(:status)`).

## Fase 3: Capa de Negocio (Services)
**Objetivo:** Reemplazar validaciones en memoria estrictas por comparaciones tolerantes al case.
* **Paso 3.1:** Localizar `MatchService.java`. Modificar el método `registerResult()` (Regla RN-08-1) para que la validación de estado use `equalsIgnoreCase("Finalizado")`.
* **Paso 3.2:** Localizar `TournamentService.java`. Modificar el método `generateQuarterFinals()` (Brackets 6.11) para que la validación de estado use `equalsIgnoreCase("En progreso")`.
* **Paso 3.3:** Escanear globalmente el directorio `src/main/java/com/example/core/service/` buscando métodos `.equals()` aplicados a variables `String` que representen estados o roles. Reemplazarlos por `.equalsIgnoreCase()`.

## Fase 4: Suite de Pruebas (Tests)
**Objetivo:** Garantizar que los cambios no rompan las reglas de negocio y validen la nueva flexibilidad de case.
* **Paso 4.1:** Escanear `src/test/java/com/example/core/service/`.
* **Paso 4.2:** Identificar pruebas relacionadas con `MatchService`, `TournamentService` y repositorios modificados en fases anteriores.
* **Paso 4.3:** Actualizar los *mocks* y datos de entrada (*hardcoded*) en las pruebas para inyectar deliberadamente variaciones de case (ej. pasar "FINALIZADO" o "en progreso") y asegurar que el comportamiento esperado se mantiene.
* **Paso 4.4:** Ejecutar el comando `mvn clean test`.
* **Paso 4.5:** Confirmar que las 147 pruebas finalizan en verde. Si hay fallos, iterar la corrección limitándose al archivo fallido.