# 🗺️ Roadmap de Desarrollo: TechCup Fútbol

## Feedback de Auditoría (Ajuste de Contexto)
- **Aciertos:** Detección de `program` (RN-03-4), FairPlay (RN-09-2) y brackets (6.11).
- **Corrección:** El "Carné Universitario" NO es un requerimiento. Usamos correo institucional e identificación.

## Fase 1-3: Modelos, Lógica y Seguridad Base (COMPLETADO ✅)
- [x] Modelos JPA, Enums y validaciones de programa.
- [x] Lógica de Brackets, FairPlay y estados de partido.
- [x] Seguridad inicial (RBAC) y carga de archivos (Multipart).

## Fase 4: Infraestructura y Calidad (COMPLETADO ✅)
- [x] Docker Compose (PostgreSQL 16 en puerto 5433).
- [x] Migración total a JPA (Estrategia SINGLE_TABLE).
- [x] Estabilización de tests (92 tests) y cobertura >30% en servicios.

## Fase 5: Profesionalización y Seguridad (EN PROGRESO 🚀)
*Objetivo: Cumplir requerimientos de seguridad y externalización de credenciales.*
- [x] **Paso 5.1: Encriptación y Variables:** Implementar BCrypt en PlayerService y mover credenciales a variables de entorno en `.yaml` y `docker-compose`.
- [x] **Paso 5.2: Auth Persistente:** Implementar `UserDetailsService` para cargar usuarios desde la DB (reemplazar memoria).
- [x] **Paso 5.3: CI/CD:** Configurar GitHub Actions con los Secrets definidos en el repo.

## Fase 6: Funcionalidad Final e Invitaciones
- [x] **RF-11: Gestión de Invitaciones:** Endpoint para Aceptar/Rechazar con persistencia real.
- [x] **Integridad (RN-11-3):** Lógica de rechazo automático de invitaciones paralelas.

## Fase 7: Seguridad Avanzada (JWT) y Cleanup (EN PROGRESO 🚀)
- [ ] **Paso 7.1: El Generador (JwtService):** Crear la lógica para firmar y validar tokens con una Secret Key segura.
- [ ] **Paso 7.2: El Guardián (JwtAuthenticationFilter):** Implementar el filtro que intercepta cada petición y extrae el "brazalete" (token).
- [ ] **Paso 7.3: El Punto de Acceso (AuthController):** Crear el endpoint de Login real que genera el token tras validar con BCrypt.
- [ ] **Paso 7.4: Integración Final:** Ajustar `SecurityConfig` para desactivar Basic Auth y activar la protección por JWT.