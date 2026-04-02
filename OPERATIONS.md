# Guía de Operaciones Técnicas - TechCup Fútbol

## Inicio de Jornada (Levantar Entorno)
1. **Levantar Virtualización (Colima):** Ejecuta en la terminal: `colima start --cpu 2 --memory 4`
2. **Iniciar Base de Datos:** Ejecuta en la terminal: `docker compose up -d`
3. **Verificar Conexión:** Abre el panel **Database** en IntelliJ y refresca `techcup@localhost`.
4. **Ejecutar Backend:** En IntelliJ, abre la clase principal (`TechcupFutbolApplication.java`) y haz clic en el **botón verde de Play (Run)** ubicado en el margen izquierdo junto a la definición de la clase, o usa el botón de Play en la barra de herramientas superior.

## Desarrollo y Control de Calidad
* **Limpieza y Compilación:** Ejecuta en la terminal: `mvn clean install`
* **Ejecutar Pruebas:** Haz clic en el botón verde de Play junto a la clase de prueba o ejecuta en la terminal: `mvn clean test`
* **Reporte de Cobertura (JaCoCo):** Ejecuta en la terminal: `mvn clean test jacoco:report`
* **Acceso API (Swagger UI):** Navega a `http://localhost:8080/swagger-ui.html` (requiere que el backend esté en ejecución).

## Fin de Jornada (Liberar Recursos)
1. **Detener Aplicación:** En IntelliJ, haz clic en el **botón rojo de Stop (cuadrado)** situado en la ventana de la consola (Run tool window).
2. **Apagar Contenedores (Preservando Datos):** Ejecuta en la terminal: `docker compose stop`
3. **Apagar Virtualización:** Ejecuta en la terminal: `colima stop`

## Mantenimiento y Limpieza Profunda
* **Borrar Base de Datos y Volúmenes (Pérdida de datos):** Ejecuta en la terminal: `docker compose down -v`
* **Resetear Tablas vía Hibernate:** Cambia el valor a `spring.jpa.hibernate.ddl-auto: create` en el archivo `application.yaml` y vuelve a arrancar el backend desde IntelliJ. Devuelve el valor a `update` al terminar.