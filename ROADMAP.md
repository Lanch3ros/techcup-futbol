# 🗺️ Roadmap de Desarrollo: TechCup Fútbol

## 1. Feedback de Auditoría (Ajuste de Contexto)
Claude, toma nota de los siguientes puntos sobre el reporte anterior:
- **Aciertos:** Excelente detección de la falta del campo `program` (RN-03-4), la ausencia del punto FairPlay (RN-09-2) y la falta de lógica para brackets eliminatorios (6.11).
- **Corrección (Hallucinación):** El "Carné Universitario" **NO** es un requerimiento oficial. La sección 6.1 del PDF trata sobre "Registro de Torneo". Ignora cualquier validación de carné físico/digital; nos basaremos únicamente en el **correo institucional** y el campo **identificación**.

## 2. Fase 1: Base de Datos y Modelos (Prioridad Alta)
Objetivo: Ajustar los modelos para que soporten las reglas de negocio del PDF.
- [ ] Agregar campo `program` (Enum: Sistemas, IA, Ciberseguridad, Estadística) a `Player`.
- [ ] Agregar campo `identification` y `password` a los modelos de usuario.
- [ ] Implementar validación en `TeamService` para que >50% de jugadores cumplan con el programa académico (RN-03-4).

## 3. Fase 2: Lógica de Torneo y Estadísticas
Objetivo: Corregir el cálculo de puntos y la progresión del torneo.
- [ ] Actualizar `StatsService` para incluir el +1 punto por FairPlay (sin tarjetas) [RN-09-2].
- [ ] Implementar el algoritmo de generación de llaves (Cuartos, Semis, Final) en `TournamentService` [6.11].
- [ ] Validar que el marcador solo se registre si el partido está en estado `FINISHED` [RN-08-1].

## 4. Fase 3: Seguridad y Archivos
Objetivo: Proteger el sistema y permitir carga de evidencias.
- [ ] Crear `SecurityConfig` para restringir endpoints por rol (ADMIN, ORGANIZADOR, CAPITAN).
- [ ] Implementar el endpoint real para subida de comprobantes de pago (MultipartFile) [RF-06].