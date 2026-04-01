# 🗺️ Roadmap de Desarrollo: TechCup Fútbol

## Feedback de Auditoría (Ajuste de Contexto)
- **Aciertos:** Detección de `program` (RN-03-4), FairPlay (RN-09-2) y brackets (6.11).
- **Corrección:** El "Carné Universitario" NO es un requerimiento. Usamos correo institucional e identificación.

## Fase 1: Base de Datos y Modelos (COMPLETADO ✅)
- [x] Agregar campo `program` (Enum) y Maestrías.
- [x] Agregar campo `identification` y `password` a User.
- [x] Validación de >50% de programas de ingeniería (RN-03-4).

## Fase 2: Lógica de Torneo y Estadísticas (COMPLETADO ✅)
- [x] Punto FairPlay (+1) en StatsService [RN-09-2].
- [x] Algoritmo de Brackets (1v8, 2v7, 3v6, 4v5) [6.11].
- [x] Validación de estado FINISHED para resultados [RN-08-1].

## Fase 3: Seguridad y Archivos (COMPLETADO ✅)
- [x] SecurityConfig base con RBAC (Roles).
- [x] Endpoint MultipartFile para comprobantes de pago (JPG, PNG, PDF) [RF-06].

## Fase 4: Infraestructura y Persistencia Real (EN PROGRESO 🚀)
**Nota técnica:** El entorno usa Colima (x86_64) en Mac Intel.
- [x] Crear `docker-compose.yml` para PostgreSQL 16 (Puerto 5433).
- [x] Migrar modelos de Java a Entidades `@Entity` (JPA).
- [x] Implementar `JpaRepository` para reemplazar los HashMaps actuales.
- [ ] Persistencia de Usuarios: Mover usuarios de memoria a la base de datos con `UserDetailsService`.
- [ ] Implementar Gestión de Invitaciones (Aceptar/Rechazar) con persistencia [RF-11].

## Fase 4.1: Estabilización y Refactorización de Tests (COMPLETADO ✅)
- [x] Actualizar constructores de modelos en `src/test/java`.
- [x] Refactorizar mocks de Repositorios (ahora interfaces JPA).
- [x] Asegurar `mvn test` en verde (119 tests pasando).

## Fase 4.2: Recuperación de Cobertura (COMPLETADO ✅)
- [x] Lograr >30% de cobertura en `com.example.core.service`.
- [x] Crear tests para la lógica de FairPlay (RN-09-2).
- [x] Crear tests para la generación de Brackets (6.11).
- [x] Asegurar que `TeamService` valide correctamente la regla del 50% (RN-03-4).