# Guía de Operaciones Técnicas (Terminal) - Equipo TechCup Fútbol

## 1. Inicio de Jornada (Levantar Entorno)
Abramos la terminal en la raíz del proyecto y ejecutemos estos comandos en orden para preparar el entorno:

1. **Levantar máquina virtual (Colima):**
   `colima start --cpu 2 --memory 4

2. **Iniciar base de datos (manteniendo datos previos):**
   `docker compose up -d`

3. **Iniciar el backend:**
   `mvn spring-boot:run -Dmaven.test.skip=true`

(La API quedará expuesta en http://localhost:8080 y la documentación en http://localhost:8080/swagger-ui.html)

## 2. Desarrollo y Control de Calidad
Para no detener el servidor, abramos una **segunda pestaña** en la terminal y utilicemos estos comandos según las necesidades de integración:

* **Compilar e instalar dependencias:** `mvn clean install`
* **Ejecutar suite de pruebas (437 tests):** `mvn clean test`
* **Generar reporte de cobertura (JaCoCo):** `mvn clean test jacoco:report`

## 3. Fin de Jornada (Liberar Recursos)
Sigamos este orden estricto al terminar de trabajar para no dejar procesos "zombies" consumiendo batería o RAM en nuestros equipos locales:

1. **Detener el backend:** Vamos a la terminal donde corre Spring Boot y presionamos `Ctrl + C`.
2. **Apagar contenedores (SIN borrar datos):** `docker compose stop`
3. **Apagar motor de virtualización:** `colima stop`

## 4. Mantenimiento (Borrón y Cuenta Nueva)
Usemos esta secuencia únicamente cuando necesitemos purgar toda la base de datos y empezar con un entorno en blanco para evitar colisiones de datos.

1. Detenemos el backend con `Ctrl + C`.
2. Destruimos la base de datos y su volumen de persistencia: `docker compose down -v`
3. Levantamos una base de datos nueva y limpia: `docker compose up -d`
4. Iniciamos el backend para que Hibernate vuelva a crear las tablas vacías automáticamente: `mvn spring-boot:run -Dmaven.test.skip=true`