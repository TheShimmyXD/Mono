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

### D-06 · Versiones y paquete de la app

- **Pregunta:** ¿con qué versiones se crea el proyecto Gradle y cómo se llama el paquete?
- **Opciones:** crearlo a mano con las últimas estables · que el autor lo cree en Android Studio («Empty Activity») y el agente añada `engine`.
- **Elección:** a mano, con las estables publicadas el 2026-10-06 (catálogo `gradle/libs.versions.toml`): Gradle 9.8.0 (wrapper, sha256 verificado), AGP 9.4.1 (trae Kotlin integrado: sin plugin `kotlin-android`), Kotlin 2.4.20, Compose BOM 2026.09.00, activity-compose 1.13.0, JUnit 6.1.3. `compileSdk` y `targetSdk` 37 (el SDK instalado), `minSdk` 26 (Android 8: el Redmi y casi cualquier teléfono de los amigos). Bytecode Java 17 en los dos módulos. Paquete `com.jacck.mono` (namespace y applicationId).
- **Por qué:** sin pasos del autor y con todo en el catálogo; Java 17 lo compila el JBR 25 de Android Studio y lo acepta Android.
- **Cómo se revierte:** versiones: una línea del catálogo. El applicationId se puede cambiar libremente hasta publicar (P3); después ya no.
- **Estado:** vigente.

### D-07 · Hook `pre-commit` en `.githooks/`

- **Pregunta:** ¿dónde vive el hook que rechaza un ESTADO sobre el tope, y qué corre?
- **Opciones:** `.git/hooks/pre-commit` (fuera de Git) · `.githooks/pre-commit` en Git + `core.hooksPath` · llamar a `cierre_paso.py` entero desde el hook.
- **Elección:** `.githooks/pre-commit` en Git, activado con `git config core.hooksPath .githooks`; solo mide ESTADO (misma cuenta que `cierre_paso.py`), sin Gradle.
- **Por qué:** sobrevive a un clon nuevo; un commit no espera 10 s de Gradle (eso ya lo hace `cierre_paso.py` antes del commit). `cierre_paso.py` no tiene una opción para medir solo ESTADO (anotado para el observador).
- **Cómo se revierte:** `git config --unset core.hooksPath` y borrar `.githooks/`. En un clon nuevo hay que repetir el `git config`.
- **Estado:** vigente.

### D-08 · Lo que los reglamentos no dicen (tabla de Diferencias)

- **Pregunta:** ¿qué valor llevan las filas ◇ de `REGLAS.md` `## Diferencias`, donde un reglamento calla o es ambiguo?
- **Opciones:** propuestas del agente en la tabla, con alternativas preguntadas por AskUserQuestion para «par» (R-43), los derechos (R-51) y el dinero inicial de Tío Rico (R-42).
- **Elección (aprobada por el autor el 2026-10-06):** empate en la tirada inicial → repiten solo los empatados (R-06); «par» = dobles, otra tirada sin tope (R-43); nadie puja → sigue del Banco (R-12); el 10 % se redondea hacia arriba, a $1 (R-32); Tío Rico no construye parejo (R-46); el castillo devuelve las 3 casas al Banco (R-47); ×2 con castillo en todas las del grupo (R-48); Tío Rico: 30 casas y 10 castillos, con la escasez de R-28; ambos venden edificios al Banco a 1/2 (R-30); derechos de $200 por cosa vendida, los paga el vendedor al Banco (R-51); el Banco nunca se arruina (R-05); alquiler automático (R-17); sin bote en Parada Libre (R-24); recuento del juego con tiempo = el de R-39 (R-40); el dinero de las Tierras va al Banco (R-52); 2-6 jugadores en los dos presets (D-05).
- **Pendiente:** dinero inicial de Tío Rico = 3 × (suma de las 6 denominaciones); el autor da el valor de los billetes (no bloquea hasta F2.8).
- **Por qué:** cada valor es la lectura más cercana al texto o la que no añade reglas; todas son opciones, así que se cambian en el editor (F4.2).
- **Cómo se revierte:** cambiar el valor en el preset (F2.8) y aquí.
- **Estado:** vigente.

### D-09 · Qué es editable y sus rangos

- **Pregunta:** ¿qué puede cambiar el autor en el editor (F4) y entre qué límites? El validador del motor (F2.7) rechaza lo que salga de aquí.
- **Opciones:** todo libre · campos fijos con rangos (esta) · solo los presets.
- **Elección (aprobada por el autor el 2026-10-06):**
  - **Tablero:** anillo de N casillas, N de 16 a 48 y múltiplo de 4 (un cuadrado con 4 esquinas; el Monopoly tiene 40). Exactamente una `Start` en la posición 0. Grupos de color de 1 a 4 propiedades; al menos un grupo.
  - **Tipos de casilla:** `Start`, `Property` (con grupo de color), `Station` y `Utility` (sin casas), `Tax`, `Card` (mazo A o B), `Jail`, `GoToJail` y `Rest` (descanso). Con `jail = no`, no puede haber `Jail` ni `GoToJail`.
  - **Campos de cada casilla:** nombre de 1 a 24 caracteres; precio, alquiler base, alquiler con 1..`maxHouses` casas y con hotel, precio de la casa y del hotel (cuando `housePrice`/`hotelPrice` = por casilla) y valor de hipoteca (cuando `mortgageValue` = impreso): enteros de 0 a 99 999. `Tax`: `fixed` 0-99 999, `percent` 0-100, `perHotel` 0-99 999.
  - **Opciones de reglas** (`REGLAS.md` `## Diferencias`): las 34. Dinero (`startingMoney`, `salary`, `jailFine`, `auctionBase`, `unmortgageFee` fijo, `tradeFee`) de 0 a 99 999; porcentajes de 0 a 100; `maxHouses` 1-4; `doublesToJail` 0 (sin tope) o 2-5; `jailMaxTurns` 1-5; `houseStock` 0 (sin límite) o 1-99 y `hotelStock` 0 o 1-99; sí/no en el resto.
  - **Jugadores:** 2-6 (D-05).
- **Por qué:** cada rango cubre los dos reglamentos con margen (el máximo leído es $2000 de sueldo y de la Tierra de la Frontera) y deja fuera los tableros que no caben en 393 dp de ancho (48 casillas → 13 por lado ≈ 30 dp cada una).
- **Cómo se revierte:** son constantes del validador (F2.7); cambiar aquí y allí.
- **Estado:** vigente.

### D-10 · Modelo del motor y JSON

- **Pregunta:** ¿cómo se guardan tablero, partida y acciones, y con qué librería se pasan a JSON?
- **Opciones:** `kotlinx.serialization` (plugin del compilador, sin reflexión; ya en `motor.md` §3) · Gson o Moshi (reflexión, peor con clases `sealed`) · JSON a mano.
- **Elección:** `kotlinx-serialization-json` 1.11.0 (la última estable; 1.12.0 aún es RC) con el plugin `org.jetbrains.kotlin.plugin.serialization` de Kotlin 2.4.20. Modelo en `engine/…/model/`: `GameConfig` (grupos, casillas `sealed` con `"type"` en el JSON, `RuleOptions` con un campo por fila de `## Diferencias` y sin valores por defecto), `GameState` aparte (jugadores, `holdings` por índice de casilla, fase del turno `sealed`, `GameRandom`), `Action` `sealed`. Cifras enteras en $; porcentajes enteros. JSON estricto: una clave desconocida es un error.
- **Por qué:** es la librería oficial de Kotlin y funciona igual en `engine` (JVM) y en `app` (Android); separar configuración y estado permite cargar un preset sin partida y enviar solo el estado por Bluetooth.
- **Cómo se revierte:** el modelo no depende del formato; cambiar `MonoJson` y quitar las anotaciones.
- **Estado:** vigente.
