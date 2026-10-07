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
- **Dinero inicial de Tío Rico** (R-42; billetes leídos por el autor, 2026-10-06): $100, $200, $500, $1000, $2000 y $5000; 3 de cada uno → 3 × 8800 = $26 400.
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

### D-11 · Turnos, dados y sueldo en el motor

- **Pregunta:** ¿cómo se tira, se mueve y se cobra en `engine`, y cómo se prueba con dados fijados?
- **Opciones:** dados falsos inyectados en todo el motor · la tirada como dato (`Engine.roll(config, state, dice)`) y `apply(Roll)` que la saca del generador del estado.
- **Elección:** `Engine` es un `object` (una sola instancia, sin estado propio) con funciones puras: `newGame`, `apply`, `roll` (dados conocidos: sirve a las pruebas y, si un día se quiere, a jugar con dados físicos) y `firstPlayer` (recibe los dados como `Iterator`). La tirada inicial usa los dos dados en los dos presets (R-43 dice «el dado», pero Tío Rico trae dos, R-41). Empieza el mayor y el turno sigue el orden de la lista desde él (fila 3 de Diferencias). El sueldo se paga una vez por cada paso por la salida (R-10), aunque la salida no esté en la casilla 0; estar en la salida y salir de ella no cobra. Con dobles y `doublesRollAgain` vuelve a tirar sin pasar por «fin de turno». Una acción fuera de fase lanza `IllegalActionException`.
- **Pendiente:** la Cárcel por dobles seguidos (R-09) va con la Cárcel en F2.4; el efecto de la casilla (R-08) en F2.3..F2.5; construir entre dos tiradas de dobles, cuando haya casas (F2.3).
- **Por qué:** la tirada como dato hace las pruebas exactas sin trucos de semilla, y el azar sigue saliendo solo de `GameState.random` (D-03).
- **Cómo se revierte:** cambiar `Engine.roll` y `advance`; las pruebas de `MovementTest` dicen qué se rompe.
- **Estado:** vigente.

### D-12 · Comprar, alquiler y construir

- **Pregunta:** lo que los reglamentos no fijan para F2.3: ¿se construye en una hipotecada?, ¿dónde construye Tío Rico «al llegar»?, ¿cuándo se construye en Clásico?
- **Opciones:** preguntadas al autor con AskUserQuestion (2026-10-06).
- **Elección (del autor):** nunca se construye en una hipotecada, en ningún preset (R-49 lo dice para Tío Rico; Monopoly calla). Con `buildOnlyWhenLanding` (Tío Rico, R-46/R-47) casas y castillo **solo en la propiedad donde cayó**, y solo en ese turno. En Clásico se construye **solo en el propio turno** (R-25 dice «en cualquier momento»), antes o después de tirar.
- **Del agente:** el hotel pide `maxHouses` en el propio solar y, con `evenBuild`, en todo el grupo (R-27); sin `evenBuild`, solo en el propio (R-47). Ferrocarril y servicio cobran lo impreso según cuántos tiene el dueño (R-13, D-10); el servicio, con los dados de la tirada que lo trajo. Si no alcanza para el alquiler, el saldo queda negativo hasta la quiebra (F2.6). `houseStock`/`hotelStock` = 0 no descuenta del Banco.
- **Pendiente:** subasta al rechazar (R-12, R-44) y vender edificios parejo (R-26, R-30) en F2.6; subasta por escasez (R-28) y `rentMustBeClaimed` = sí (R-17) sin implementar: ningún preset los usa; el validador (F2.7) los rechaza mientras tanto.
- **Por qué:** las tres elecciones del autor; lo demás es la lectura literal de cada ficha.
- **Cómo se revierte:** `Building.kt` y `Landing.kt`; las pruebas de `BuildTest` y `BuyRentTest` dicen qué se rompe.
- **Estado:** vigente.

### D-13 · Cárcel, impuestos y Parada Libre

- **Pregunta:** lo que R-09, R-19..R-24 y R-52..R-54 no fijan para F2.4.
- **Opciones:** lectura literal de cada ficha (elegida) o reglas caseras habituales (pagar la multa también en el 3.er turno, bote en Parada Libre).
- **Elección (del agente, aprobada por el autor el 2026-10-06):** la multa voluntaria solo antes de tirar en los `jailMaxTurns` − 1 primeros turnos dentro (R-22 «cualquiera de sus dos turnos siguientes»); en el último se tira y, sin dobles, se paga a la fuerza aunque el saldo quede negativo (F2.6). Tras pagar o usar la carta se tira normal, con otra tirada si hay dobles. Los dobles que sacan de la Cárcel no dan otra tirada (R-22). Ir a la Cárcel pone `doublesInRow` = 0 y termina el turno (R-20). Patrimonio de R-19: efectivo + precio impreso de las casillas + casas a su precio + hotel a su precio y, con `hotelReturnsHouses`, sus `maxHouses` casas. El impuesto va al Banco (R-52 «sin decir»).
- **Pendiente:** `freeParkingPot` = sí sin implementar (ningún preset lo usa; D-08); la carta vuelve debajo de su mazo en F2.5; el validador (F2.7) rechaza `doublesToJail` > 0 o una casilla `goToJail` sin `jail` y sin casilla Cárcel (el motor lanza `IllegalStateException`).
- **Por qué:** «Nada se supone de las reglas»: donde la ficha dice algo, se sigue al pie de la letra.
- **Cómo se revierte:** `Jail.kt` y `Taxes.kt`; `JailTest` y `TaxTest` dicen qué se rompe.
- **Estado:** vigente.

### D-14 · Cartas: efectos y mazos

- **Pregunta:** qué efectos puede tener una carta (R-18 no lo dice: está en las barajas) y lo que el motor necesita para robarlas.
- **Opciones:** los del juego clásico, un mínimo (cobrar, pagar, ir a, Cárcel, salir libre) o sacarlos de las barajas del autor.
- **Elección (del autor, 2026-10-06; F2.5 aprobada):** los del juego clásico: `Collect`, `Pay`, `MoveTo`, `MoveBy` (negativo = retrocede), `GoToJail`, `GetOutOfJail`, `Repairs` (por casa y por hotel), `CollectFromEach`, `PayEach` y `MoveToNearest` (ferrocarril con `rentFactor`; servicio con `diceMultiplier`, que tira los dados de nuevo con la semilla). El texto de cada carta lo escribe el autor en el preset (F2.8).
- **Del agente:** `GameConfig.cards` (sin barajar) y `GameState.decks` (orden de cada mazo, barajado en `newGame` con la semilla, Fisher-Yates). La carta vuelve debajo antes de cumplirse; «Salir libre» se guarda en `PlayerState.jailCards` (índices, para saber a qué mazo vuelve). `MoveTo` siempre avanza (si ya está ahí, vuelta entera) y cobra el sueldo al pasar la salida; retroceder no lo cobra. Mazo vacío → no pasa nada. Pagos de cartas con el Banco o entre jugadores pueden dejar el saldo negativo (F2.6).
- **Pendiente:** el validador (F2.7) rechaza una casilla de carta sin cartas en su mazo, `MoveTo` fuera del tablero o a una casilla de carta, y `MoveToNearest` sin casillas de ese tipo.
- **Por qué:** elección del autor; lo demás sigue R-18 al pie de la letra y D-03 (azar con semilla).
- **Cómo se revierte:** `Cards.kt` y `model/Card.kt`; `CardTest` dice qué se rompe.
- **Estado:** vigente.

### D-15 · Hipotecas, subasta, quiebra y fin

- **Pregunta:** lo que R-12, R-26, R-30..R-32, R-34, R-35 y R-37..R-40 no fijan para F2.6.
- **Opciones:** lectura literal de cada ficha completada con lo mínimo para que el motor no se trabe (elegida) o reglas caseras (subastar al quebrar ante un jugador, préstamos del Banco).
- **Del autor (2026-10-06):** `bankruptcyInterest` = 10 en los dos juegos (fila 35 de `## Diferencias`); en el recuento (R-39, R-40) el empate lo desempata el efectivo y, si sigue, ganan todos los empatados (`TurnPhase.Over(winners)`).
- **Del agente:** no se construye con una hipotecada en el grupo y en una hipotecada no se venden edificios. Puja en la subasta todo el que sigue en juego desde quien juega, incluido quien la rechazó. Quien queda con saldo negativo entra en `TurnPhase.Debt` con quien le cobró por última vez en la acción (alquiler o carta) o con el Banco; mientras, solo vende, hipoteca o se declara en quiebra, y solo puede quebrar si ni vendiendo todo ni hipotecando alcanza. El saldo negativo se descuenta al acreedor hasta lo que cobró en esa acción; el resto lo pone el Banco. Quien recibe una hipotecada paga ya el interés y puede levantarla sin más interés, aunque no sea su turno, hasta que el turno vuelva a pasar (`feePaid`). Ante el Banco, los edificios vuelven a sus existencias, las casillas se subastan sin hipoteca en orden de casilla empezando por el siguiente jugador y las cartas «Salir libre» van debajo de su mazo. En la 2.ª quiebra (R-39) no se cobra interés. Las Escrituras del juego corto o con tiempo (R-37, R-40) se reparten de una en una desde quien empieza. El fin por tiempo lo manda la app con `Action.TimeUp`.
- **Pendiente:** negocios entre jugadores (R-29, R-33, R-51); subasta por escasez de edificios (R-28).
- **Por qué:** elecciones del autor; lo demás sigue cada ficha al pie de la letra y evita que la partida se trabe o cree dinero.
- **Cómo se revierte:** `Mortgage.kt`, `Auction.kt` y `Bankruptcy.kt`; `MortgageTest`, `AuctionTest` y `BankruptcyTest` dicen qué se rompe.
- **Estado:** vigente.

### D-16 · Validador de la configuración

- **Pregunta:** dónde viven los mensajes en español del validador (F2.7) y qué rechaza además de los rangos de D-09.
- **Opciones:** frases en español dentro del motor (lo más corto, pero rompe `motor.md` §5 y D-02) o errores tipados en el motor y frases en `strings.xml` de la app (elegida).
- **Elección (del agente):** `validate(config)` en `engine/…/Validator.kt` devuelve todos los errores como `ConfigError` (14 tipos, con `SquareField`, `RuleField`, `Incoherence` y `CardProblem`); `app/…/ConfigErrorText.kt` los convierte en frases de `strings.xml` con un `when` que cubre todos los casos (un error sin mensaje no compila). Además de D-09 rechaza: `rentMustBeClaimed` y `freeParkingPot` = sí (el motor no los tiene, D-12, D-13); casillas, cartas o dobles que mandan a la Cárcel sin `jail`; con `jail`, distinto de una casilla Cárcel; casilla de carta con su mazo vacío, `MoveTo` fuera del tablero o a una casilla de carta, `MoveToNearest` sin casillas de ese tipo (D-14); alquileres: `maxHouses` + 2 en cada solar y al menos uno por ferrocarril o servicio del tablero; `startingDeeds` × `maxPlayers` más que las casillas comprables. Sin rango en D-09 y fijado aquí: factores de las cartas desde 1. La subasta por escasez (R-28) no es una opción: no hay nada que rechazar; el motor no la hace.
- **Por qué:** `motor.md` §5 (el motor devuelve datos, no frases) y una sola fuente de textos para cuando llegue la traducción o el editor (F4).
- **Cómo se revierte:** `Validator.kt` y `ConfigErrorText.kt`; `ValidatorTest` dice qué se rompe.
- **Estado:** vigente.

### D-17 · Precios de las casillas y texto de las cartas

- **Pregunta:** de dónde salen los precios, alquileres e hipotecas de cada casilla y el texto de las cartas de los presets (F2.8): no están en los reglamentos.
- **Opciones:** que los dé el autor desde sus juegos o que los balancee el agente.
- **Elección (del autor, 2026-10-06):** los balancea el agente, porque el autor no tiene los juegos a mano; el texto de las cartas también lo escribe el agente, con los efectos clásicos de D-14. En F2.8 cada cifra de los presets cita esta ficha (lo que no viene de una R-##), y el método de balanceo se anota aquí al hacerlo.
- **Por qué:** decisión del autor; los valores se pueden cambiar en el editor (F4).
- **Cómo se revierte:** se cambian los JSON de los presets; el validador y las pruebas de F2.8 dicen si siguen siendo válidos.
- **Método (F2.8):** Clásico = cifras de la edición clásica (calles, ferrocarriles $200 con [25, 50, 100, 200], servicios $150 con ×4/×10, hipoteca impresa = 1/2, hotel al precio de la casa, 16 + 16 cartas), porque usan la misma escala que $1500 de R-03 y $200 de R-10; el segundo impuesto («Impuesto de lujo», $100 fijo) no está en el reglamento y sale de aquí. Tío Rico = clásico × 10 (el sueldo de R-45 es 10 × el de R-10): la propiedad k (0..31) toma la calle clásica `round(k × 21 / 31)`, precio y alquileres × 10, sin la renta de 4 casas (castillo = hotel × 10); casa y castillo, los fijos de R-46 y R-47; 11 + 11 cartas (R-41) con los efectos clásicos × 10, sin las de Cárcel. Generado con un script del agente; el JSON es la fuente.
- **Estado:** vigente.

### D-18 · Tablero y nombres de los presets

- **Pregunta:** qué casilla va en cada posición y cómo se llaman (ningún reglamento trae el tablero) y cómo se nombran si algún día se publica (P3).
- **Opciones:** nombres propios inventados · calles reales de Monopoly · provisionales; Tío Rico de 44 casillas, de 44 con estaciones o de 40 como el Clásico.
- **Elección (del autor, 2026-10-06):** nombres propios: Clásico con barrios y calles de Bogotá, Tío Rico con pueblos y ciudades de Colombia; mazos «Casualidad»/«Arca Comunal» y «Lotería»/«Sorpresa» (R-18), Tierras con su nombre de R-52..R-54. Clásico: el esqueleto clásico de 40 (8 grupos, 4 estaciones, 2 servicios, Contribuciones en 4, Cárcel en 10). Tío Rico: 44 casillas (11 por lado) = Estación Santa Fe + 32 propiedades en 8 grupos de 4 (los 32 títulos de R-41) + 3 Tierras (14, 33, 39) + 6 de cartas + 2 descansos (11, 22). Los presets siguen llamándose «Clásico» y «Tío Rico».
- **Del agente:** `Preset.load()` en `engine/…/Presets.kt` lee `engine/src/main/resources/presets/<id>.json`; sin Cárcel, Tío Rico lleva `jailFine` 0 y `jailMaxTurns` 3 (no se usan).
- **Por qué:** sin arte ni nombres de terceros (riesgo de marcas del plan); 32 títulos solo caben con N = 44 (múltiplo de 4, D-09).
- **Cómo se revierte:** cambiar el JSON; `PresetTest` dice qué deja de cumplir.
- **Estado:** vigente.

### D-19 · Simulador de partidas (F2.9)

- **Pregunta:** cómo jugar 1000 partidas sin interfaz y qué se comprueba.
- **Opciones:** bot que conoce las reglas · bot que prueba acciones al azar y el motor dice cuál vale (esta) · solo tiradas y fin de turno.
- **Elección (del agente):** `engine/src/test/…/Simulator.kt`: en cada fase el bot prueba acciones al azar y toma la primera que no lanza `IllegalActionException`; cualquier otra excepción es un fallo. Compra 7 de cada 10, puja al azar, construye o levanta hipotecas hasta 3 veces por turno, en la deuda vende o hipoteca y si no alcanza quiebra. `ringBoard` saca de cada preset tableros de N = 16..48 (quita casillas al azar, nunca la salida ni la Cárcel, o añade descansos; quita las cartas que se quedan sin destino). Invariantes en cada acción: el saldo de cada jugador cambia lo que dicen los eventos (en una quiebra, sin cifra en su evento, se audita a los demás y el quebrado queda en $0); fuera de una deuda nadie en juego tiene saldo negativo; casas y hoteles del Banco + los del tablero = existencias (R-28). Tope: 1000 turnos. Sin negocios: `Trade` aún no está en el motor.
- **Medido (2026-10-06):** 1000 partidas en 32 s, 1 398 476 acciones, 0 excepciones; 598 terminan y 402 llegan al tope; Clásico 352/500 (203 turnos de media), Tío Rico 246/500 (440).
- **Por qué:** sin duplicar las reglas en el bot; el motor no cambia; la prueba corre con `:engine:test` (+32 s al cierre) para que una regresión salga en cada paso.
- **Cómo se revierte:** borrar `Simulator.kt` y `SimulatorTest.kt`.
- **Estado:** vigente.


### D-20 · Forma del tablero en la pantalla (F3.1)

- **Pregunta:** cómo dibujar el anillo de N casillas en el Redmi en vertical (≈ 393 × 873 dp).
- **Opciones (maquetas con el preset Clásico, `capturas/F3.1_maqueta_{A,B,C}.png`):** A cuadrado 11 × 11 bajo el ancho, con el panel debajo (casillas de ~36 dp, solo precio o ícono) · B rectángulo alto 7 × 15 que llena la pantalla, con el panel dentro del anillo (esta) · C anillo circular con la ficha de la casilla en el centro.
- **Elección (del autor, 2026-10-06):** B. Las casillas llevan la franja del grupo, el nombre (2 líneas) y el precio; las especiales, un ícono; el dueño, una franja abajo; las fichas, puntos de color; la casilla en turno con borde grueso. Salida abajo a la derecha, avanza hacia la izquierda (como el cartón). En el centro: dados, ficha de la casilla, jugadores y botones.
- **Para F3.2:** columnas y filas salen de N con una función pura (perímetro 2·(c+f)−4 = N, casillas lo más cuadradas posible para el alto y ancho disponibles), con su prueba para N = 20, 40 y 60. Con N = 60, unos 9 × 23 (≈ 43 × 37 dp).
- **Por qué:** es la única de las tres en la que se leen los nombres y aprovecha todo el alto del teléfono.
- **Cómo se revierte:** cambiar la función de geometría por la del cuadrado (A) o el círculo (C); las maquetas están en `app/…/maquetas/Maquetas.kt` (se borra en F3.2).
- **Estado:** vigente.

### D-21 · Tablero desde la configuración (F3.2)

- **Pregunta:** cómo repartir N casillas en el rectángulo de D-20 y cómo ver el tablero antes de que haya partida.
- **Elección (del agente; el rango de N, del autor):** `RingGrid.fit(n, ancho, alto)` (`app/…/board/RingGrid.kt`) prueba todas las columnas de 5 (centro de 3 casillas para el panel) a (N+4)/2 − 3 y se queda con las casillas más cuadradas; en el Redmi (393 × 840 dp) da 5 × 5 con N = 16, 7 × 15 con 40 y 8 × 18 con 48. Letra = ancho/7 (7-12 sp) y fichas de hasta 12 dp. Propiedad sin dueño: nombre y precio; con dueño: casas 🏠, hotel 🏨 o «Hipotecada» (atenuada) y franja del color del dueño; las demás, un ícono. `demo/DemoGame.kt` arma el Clásico (40), el Tío Rico (44) o el Clásico recortado o con descansos para otro N, con dueños y fichas puestos a mano; se elige con `--ei n` y `--ei jugadores`. Pruebas de la app con JUnit 5 (`testOptions.unitTests`), como el motor.
- **Rango de N (del autor, 2026-10-06):** el *Terminado* de F3.2 pasa de N = 20, 40, 60 a 16, 40, 48, los extremos de D-09; el motor no cambia.
- **Por qué:** una función pura con prueba para cualquier N (D-05) y sin tocar el motor para mostrar el tablero.
- **Cómo se revierte:** `RingGrid.MIN_COLS` y el criterio de `fit`; `DemoGame.kt` se va cuando F3.3 tenga partida de verdad.
- **Estado:** vigente.
