# DIAGRAMAS DE ARQUITECTURA

---

Link diagramas: https://lucid.app/lucidchart/3777f7f9-49cb-4f47-859d-86e581460502/edit?viewport_loc=-1363%2C-885%2C3299%2C1490%2C0_0&invitationId=inv_96e5594f-9313-43cc-99e2-2ea8478b8063

## Diagrama De Clases

**Patrónes utilizados**

**Factory Method - PlayerFactory**
- ¿Por qué lo elegimos?
  - El sistema tiene cinco tipos de participantes Estudiante, Graduado, Profesor, Personal Administrativo y Familiar que 
  comparten atributos comunes como nombre, correo y posición de juego, pero tienen diferencias concretas en cómo se crean
  y validan. El Estudiante y el Graduado se registran con correo institucional, el Familiar con Gmail, el Profesor tiene 
  departamento y cargo.

- ¿Cómo ayuda a resolver el problema del sistema?
  - Factory Method centraliza la creación de cada tipo de Jugador en su propia fábrica. Cuando llega una solicitud de 
  registro al PlayerController, este simplemente delega a la PlayerFactory correspondiente según el userType recibido, 
  y esa fábrica construye el objeto correcto con sus validaciones propias.

**Strategy - EmailValidator**
- ¿Por qué lo eligieron?
  - Porque no todos los jugadores usan el mismo tipo de correo. Un estudiante debe registrarse con correo institucional, 
  un familiar con Gmail, un administrativo con su correo de la universidad. Si no usáramos este patrón, tendríamos que 
  escribir la misma lógica de validación repetida en cada tipo de jugador, y si algo cambia habría que buscarla y 
  modificarla en varios lugares al mismo tiempo.
- ¿Cómo ayuda a resolver el problema del sistema?
  - Cada regla de validación vive en su propia clase. Cuando se registra un jugador, el sistema simplemente escoge el 
  validador que le corresponde según su tipo y lo aplica. Si mañana la universidad cambia su dominio de correo, 
  solo se toca una clase. Si se agrega un nuevo tipo de jugador, solo se crea un validador nuevo sin tocar nada más.


**Command - MatchCommand**
- ¿Por qué lo eligieron?
  - Porque el árbitro puede equivocarse. Si registra un gol que no era o una tarjeta al jugador incorrecto, necesita poder corregirlo. 
  Sin este patrón no habría forma ordenada de deshacer una acción ya ejecutada, y tampoco habría registro de todo lo que pasó 
  durante el partido.
- ¿Cómo ayuda a resolver el problema del sistema?
  - Cada acción del árbitro se guarda como un objeto independiente antes de ejecutarse. Ese objeto recuerda cómo estaba el 
  partido antes del cambio, así que si algo estuvo mal simplemente se deshace y el partido vuelve al estado anterior. 
  Además, como todas las acciones quedan guardadas en orden, al final del partido existe un historial completo de todo 
  lo que el árbitro registró y corrigió.



## Diagrama De Componentes General y Especifico

**General**

![componentesgeneral.png](../images/componentesgeneral.png)

**Específico**

![componentesespecifico.png](../images/componentesespecifico.png)
