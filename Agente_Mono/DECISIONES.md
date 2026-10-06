# Decisiones — Mono

*Una ficha por decisión no trivial: pregunta · opciones · elección · por qué · cómo se revierte · estado. Se leen por encabezado: `sed -n '/^### D-03 /,/^### D-04 /p'`.*

### D-01 · Pila

- **Pregunta:** ¿con qué se hace el juego en Android Studio?
- **Opciones:** Kotlin + Jetpack Compose · Flutter · Godot o Unity · Java + vistas XML.
- **Elección:** Kotlin + Jetpack Compose.
- **Por qué:** lo nativo de Android hoy; el Bluetooth se hace con la API del sistema y el motor en Kotlin puro se prueba en la JVM. Elegida por el autor (2026-10-06).
- **Cómo se revierte:** el motor (`engine`) no depende de Compose; cambiar la interfaz no toca las reglas.
- **Estado:** vigente.

### D-02 · Dos módulos: `engine` y `app`

- **Pregunta:** ¿cómo se separan las reglas de la interfaz?
- **Opciones:** un solo módulo Android · `engine` (Kotlin/JVM, sin Android) + `app` (Android).
- **Elección:** dos módulos; `engine` no importa nada de `android.*` ni `androidx.*`.
- **Por qué:** las reglas se prueban en segundos sin teléfono y no se mezclan con la interfaz (Azorian D-04: núcleo puro).
- **Cómo se revierte:** mover el código de `engine` a `app` (no previsto).
- **Estado:** vigente.

### D-03 · Motor determinista

- **Pregunta:** ¿cómo se representa la partida?
- **Opciones:** objetos mutables con azar libre · estado inmutable + acción → estado nuevo, con azar por semilla inyectada.
- **Elección:** estado inmutable y serializable; dados y barajado con una semilla que entra desde afuera.
- **Por qué:** cualquier partida se reproduce en una prueba, y en F5 el anfitrión decide y los dos teléfonos aplican las mismas acciones y llegan al mismo estado.
- **Cómo se revierte:** costoso después de F2; se decide ahora.
- **Estado:** vigente.

### D-04 · Reglas con fuente

- **Pregunta:** ¿cómo se garantiza que el motor no invente reglas?
- **Opciones:** reglas de memoria · catálogo con fuente.
- **Elección:** `REGLAS.md` con fichas R-## (texto breve, cifras, reglamento y página o recorte). Donde Tío Rico y Monopoly difieren, una opción de configuración con su valor en cada preset.
- **Por qué:** regla 1 del mandato (autor, 2026-10-06). Los reglamentos son escaneos: se transcriben una vez.
- **Cómo se revierte:** no se revierte; una regla sin fuente es una D-## del autor.
- **Estado:** vigente.

### D-05 · Jugadores y tablero

- **Pregunta:** ¿cuántos jugadores y qué forma de tablero?
- **Opciones:** 2-6 en anillo de N casillas · 2-8 en anillo · 2-6 con formas fijas.
- **Elección:** 2-6 jugadores; tablero en anillo de N casillas (rango de N en F1.4). Primero en un teléfono; Bluetooth después del hito «Primera quiebra».
- **Por qué:** autor (2026-10-06, P1-P2): 8 fichas no caben bien en la pantalla de un teléfono.
- **Cómo se revierte:** el máximo de jugadores es un número de la configuración.
- **Estado:** vigente.
