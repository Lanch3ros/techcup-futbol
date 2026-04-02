# Roadmap: 100% Test Coverage (feature/full-test-coverage)

Este documento define las fases para alcanzar el 100% de cobertura de código en el proyecto TechCup Fútbol según el reporte de JaCoCo. Se mantendrá el enfoque de pruebas rápidas (Mockito) donde sea posible.

## Fase 1: Capa de Servicios (Core Logic)
**Objetivo:** Elevar `com.example.core.service` de 35% a 100%.
* **Paso 1.1:** Ejecutar `mvn clean test jacoco:report` y revisar `target/site/jacoco/com.example.core.service/index.html` para identificar las clases específicas con líneas rojas o amarillas (ramas parciales).
* **Paso 1.2:** Crear pruebas para todos los caminos de error (excepciones `BusinessRuleException` y `ResourceNotFoundException`).
* **Paso 1.3:** Crear pruebas para ramas condicionales no cubiertas (e.g., validaciones de roles, estados alternativos, cálculo de estadísticas complejas).
* **Paso 1.4:** Verificar que `CustomUserDetailsService` y `JwtService` tengan pruebas exhaustivas para generación y validación de tokens.

## Fase 2: Capa de Controladores (REST API)
**Objetivo:** Elevar `com.example.controller` de 12% a 100%.
* **Paso 2.1:** Implementar pruebas unitarias para cada controlador usando instanciación directa e inyección de *mocks* de los servicios (sin levantar el contexto de Spring).
* **Paso 2.2:** Validar que los controladores retornen los códigos HTTP correctos (200, 201) según los DTOs de respuesta esperados.
* **Paso 2.3:** Simular el comportamiento de `SecurityContextHolder` en las pruebas de controladores que extraen información del usuario autenticado.

## Fase 3: Configuración y Seguridad
**Objetivo:** Elevar `com.example.config` de 0% a 100%.
* **Paso 3.1:** Crear pruebas para `JwtAuthenticationFilter`. Validar el comportamiento con un token válido, un token inválido/expirado, y peticiones sin cabecera *Authorization*.
* **Paso 3.2:** Probar la instanciación de beans en `SecurityConfig` y `SwaggerConfig` mediante la carga de un contexto de aplicación parcial o validando los métodos de configuración directamente.

## Fase 4: Modelos y Clase Principal
**Objetivo:** Cubrir líneas residuales en entidades y main.
* **Paso 4.1:** Escribir pruebas para los métodos de dominio dentro de `com.example.core.model` (si existe lógica más allá de *getters/setters* de Lombok).
* **Paso 4.2:** Escribir una prueba de humo (*smoke test*) para la clase principal anotada con `@SpringBootApplication` que verifique la carga del contexto.
* **Paso 4.3:** Ejecutar la suite completa y confirmar 100% global.