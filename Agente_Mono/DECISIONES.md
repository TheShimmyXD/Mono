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

### D-22 · Panel del turno: diálogos (F3.3)

- **Pregunta:** cómo se ve el centro del tablero durante el turno y dónde se toman las decisiones (comprar, impuesto, Cárcel, subasta, deuda).
- **Elección (del autor, 2026-10-06):** maqueta **B** de tres vistas en el Redmi (`capturas/F3.3_maqueta_{A,B,C}.png`; A = dados, escritura y botones fijos en el centro; C = bitácora y barra de decisión): el centro muestra el turno, los dados, los jugadores y el botón de la acción principal; cada decisión del motor (`TurnPhase.Buy`, `TaxChoice`, …) sale en un `AlertDialog` encima del tablero con la escritura de la casilla.
- **Por qué:** el centro queda limpio y cada decisión nueva es otro diálogo, sin pelear por espacio (lo pide la tarea: «con diálogos»).
- **Cómo se revierte:** el contenido del diálogo es el mismo `Composable` que iría fijo en el centro (opción A).
- **Estado:** vigente.

### D-23 · Partida jugable en un teléfono (F3.3)

- **Pregunta:** cómo conectar la pantalla con el motor y resolver lo que el motor deja abierto en un solo teléfono.
- **Elección (del agente):** `game/GameViewModel.kt` (dependencia `androidx.lifecycle:lifecycle-viewmodel-compose` 2.9.4, la misma versión que ya traía `activity-compose`) guarda `GameConfig` y `GameState` y pasa cada botón a `Engine.apply`; si el motor rechaza la acción, se muestra su motivo («El motor no lo permite: …») y no cambia nada. «Lo que pasó» (D-22) sale solo cuando hay algún evento notable (`isNotable`: sueldo, alquiler, impuesto, carta, Cárcel, subasta cerrada, deuda, quiebra, fin, dobles); los textos de cada evento, en `strings.xml` (`ev_*`). Subasta: puja el siguiente de la lista después de quien va ganando (`nextBidder`, 4 pruebas en `app/src/test`), con botones de base, +10, +50 o +100 que no pasen de su dinero. Para la escritura y la base de la subasta, `mortgageValue` y `minimumBid` del motor se hacen públicas (sin cambiar reglas). `MainActivity` arranca el Clásico con 4 jugadores; extras `jugadores`, `tio_rico`, `semilla` (la misma semilla y los mismos toques repiten la partida) y `n` (tablero de muestra de F3.2).
- **Pruebas en el Redmi:** HyperOS rechaza `adb shell input tap` (`INJECT_EVENTS`) sin «Depuración USB (ajustes de seguridad)»; la vuelta la juega el autor y el agente captura.
- **Por qué:** la interfaz no decide reglas (D-02); el orden de pujas es solo de quién tiene el teléfono, no una regla (R-12 deja pujar a cualquiera).
- **Cómo se revierte:** `nextBidder` y `isNotable` son funciones sueltas en `app/…/game/`.
- **Estado:** vigente.

### D-24 · Panel de propiedades: hoja desde abajo (F3.4)

- **Pregunta:** cómo construye, vende, hipoteca y levanta quien juega desde la partida.
- **Opciones vistas en el Redmi:** A escritura al tocar una casilla, B hoja «Mis propiedades» que sube desde abajo, C pantalla de jugadores con pestañas (`capturas/F3.4_maqueta_{A,B,C}.png`).
- **Elección (del autor):** B. Botón «🏠 Mis propiedades» en el centro durante `Roll` y `EndOfTurn`; la hoja (`ModalBottomSheet` de Material 3) lista las casillas de quien juega en orden del anillo, con franja, edificios y un botón por jugada válida, con su cifra; el signo es solo del dinero (− pagas, + recibes): `Construir −$50` (`Hotel −$X`), `Vender +$25`, `Hipotecar +$30`, `Levantar −$110` (el autor, al ver que «+🏠 $50» se leía como cobrar).
- **Cómo sabe la interfaz qué vale (del agente):** `Engine.tryApply` (nuevo, público) prueba la acción sin tocar el estado y devuelve el resultado o null; `propertyMoves` (`app/…/game/PropertyMoves.kt`, 5 pruebas) toma la cifra del evento del motor. Ninguna regla ni precio se calcula en la interfaz (D-02). En la deuda sigue el diálogo de F3.3.
- **Extras de prueba:** `propiedades` (`demo/SampleProperties.kt`: quien empieza tiene los marrones con 2 casas, los celestes y la Sabana hipotecada; solo para el Clásico) y `hoja` (la abre al arrancar), porque adb no toca la pantalla (D-23).
- **Cómo se revierte:** la hoja es `PropertiesSheet`/`PropertyRow` en `GameScreen.kt`; `tryApply` no cambia `apply`.
- **Estado:** vigente.

### D-25 · Menú de nueva partida: todo en una pantalla (F3.5)

- **Pregunta:** cómo se elige preset, cuántos juegan y sus nombres antes de la partida.
- **Opciones vistas en el Redmi:** A todo en una pantalla, B asistente en 3 pasos, C mesa de jugadores con tarjetas para añadir y quitar (`capturas/F3.5_maqueta_{A,B,C}.png`).
- **Elección (del autor):** A. `NewGameScreen` (`app/…/game/`): botones Clásico / Tío Rico, botones 2..6 (3 de entrada), un campo por jugador con su color de ficha y «Empezar».
- **Nombres (del agente; no son reglas del juego):** campo vacío = nombre de muestra de esa posición (Ana, Beto…, en gris); se recortan los espacios; 12 caracteres como máximo (caben en el panel); dos nombres iguales, sin distinguir mayúsculas, se marcan «Ese nombre ya está» y no dejan empezar. `playerNames`/`repeatedNames` en `NewGameForm.kt` (5 pruebas).
- **Arranque:** `MainActivity` abre el menú; con cualquiera de los extras de prueba (`jugadores`, `tio_rico`, `semilla`, `propiedades`, `hoja`) va directo a la partida como antes, para adb. Lo elegido sobrevive a que Android recree la pantalla (`rememberSaveable`). No hay vuelta al menú desde la partida (llega con el guardado, F3.6).
- **Cómo se revierte:** quitar la rama del menú en `MainActivity`; el motor no cambia.
- **Estado:** vigente.

### D-26 · Guardar y retomar: botón en el menú (F3.6)

- **Pregunta:** qué pasa al abrir Mono con una partida a medias.
- **Opciones:** botón «Seguir la partida» en el menú, abrir la partida directo, o una lista de varias guardadas.
- **Elección (del autor):** botón en el menú, arriba: «Seguir la partida» con los nombres y el turno; debajo, la nueva partida como en D-25.
- **Cómo (del agente):** `SavedGame(config, state)` en el motor (`MonoJson.encodeSaved`/`decodeSaved`; `SavedGameTest`: 40 jugadas, guardar, cargar y las 40 siguientes iguales, en los dos presets). La configuración va entera, no el nombre del preset, para que sirva con los tableros del editor (F4). La app guarda un solo `partida.json` en su carpeta privada tras cada jugada (`SaveFile`, 4 pruebas): escribe a un temporal y lo renombra (cerrar a mitad no deja un archivo cortado); una partida terminada lo borra; un archivo dañado se ignora. Empezar una partida nueva reemplaza la guardada. Los avisos de «Lo que pasó» y los últimos dados no se guardan: al seguir, la pantalla abre en la fase en que quedó.
- **Extras de prueba:** las partidas abiertas con extras no se guardan, para no pisar la del autor.
- **Cómo se revierte:** quitar `onState` en `MainActivity` y el botón del menú.
- **Estado:** vigente.

### D-27 · Fase FA «Arte y acabado» antes de F4

- **Pregunta:** el autor pide quitar los emojis, abrir una tarjeta al tocar una casilla y dar arte propio a cada lugar de Colombia: dónde va en la hoja de ruta y quién hace el arte.
- **Opciones:** ahora antes de F4 · tras el hito F4.5 · solo estilo, íconos y tarjeta ahora; arte dibujado por el agente en vector · imágenes que trae el autor · mezcla.
- **Elección (del autor, 2026-10-06):** fase FA ahora, antes de F4; el arte lo dibuja el agente en vector.
- **Del agente:** la fase se llama FA (con letra) para no renumerar F4 y F5, que ya se citan en fichas y referencias. Fuente de cada dibujo en SVG dentro del repo; en la app, VectorDrawable (el formato vectorial de Android: nítido a cualquier tamaño y liviano). Dibujo propio y estilizado, sin calcar fotos ni logos.
- **Cómo se revierte:** quitar la sección FA del plan; los emojis siguen en el historial de Git.
- **Estado:** vigente.

### D-28 · Estilo de arte: «arte de chiva» (FA.1)

- **Pregunta:** con qué estilo se dibujan los lugares, los íconos y el acabado del tablero.
- **Opciones (vistas en el Redmi, `capturas/FA.1_estilos.png`):** A postal plana (formas planas sin contorno, tonos suaves) · B estampilla grabada (tinta sobre crema, rayado, acento del grupo) · C arte de chiva (contorno negro grueso, colores saturados, franjas de chiva).
- **Elección (del autor, 2026-10-06):** C.
- **Del agente:**
  - *Paleta:* cielo `#38C6F4`, sol `#FFC21A`, cerros `#3DB54A`/`#1E9E8C`, suelo `#FFB238`, muro `#FFFFFF` con sombra `#CFE0FF`, techo `#E63946`, puerta `#5B2A86`, ventana `#1D7BEF`, adorno `#E5007E`, ocre `#FFD60A`, piedra `#F7D9A8`, ladrillo `#E4572E`, aguja `#7B61D9`, hojas `#2BB04A` (la tabla completa, `PALETA` en `arte/arte.py`).
  - *Trazo:* contorno `#1B1B1B` de 1,5 en cada forma; detalles en línea de 1,0-1,4; sol con 12 rayos.
  - *Encuadre:* lienzo 200 × 140 (10:7); cielo, fondo (cerros o mar), el monumento del lugar al centro, casas a los lados, suelo abajo; franjas de chiva de 9 arriba y abajo (dientes rojo, amarillo, azul, verde y magenta sobre negro). En una casilla del tablero (~36 dp) la escena no se lee: ahí va un recorte o solo el color del grupo (se decide en FA.3).
  - *Flujo:* `arte/arte.py` guarda cada lugar como formas con un papel (cielo, muro, techo…) y escribe `arte/svg/<id>.svg` (fuente que se ve en cualquier visor) y `app/src/main/res/drawable/arte_<id>.xml` (VectorDrawable: vectorial, nítido a cualquier tamaño). No se editan a mano: se cambia `arte.py` y se regenera. `[cierre]` corre `arte.py --revisar` (sale 1 si algo no está al día).
- **Cómo se revierte:** cambiar `PALETA`, `CONTORNO` y `franjas()` en `arte.py` y regenerar; los estilos A y B siguen en el commit `270656e` (`demo/Maquetas.kt`).
- **Estado:** vigente.

### D-29 · Íconos propios en vez de emojis (FA.2)

- **Pregunta:** cómo se dibujan y se eligen los íconos de casillas, edificios, dados y turno.
- **Del agente:** 23 íconos en `arte/arte.py` (lienzo 24 × 24, sin franjas, contorno 1,1, paleta de D-28): casa, hotel, 6 caras del dado, salida, cárcel, «váyase» (patrulla verde y blanca), estación, energía, acueducto, impuesto, Casualidad, Arca Comunal, Lotería, Sorpresa, Parada Libre, Mirador, Hamaca y flecha de turno. Salen como `ic_<id>.xml` y se pintan con `Image` (respeta sus colores; `Icon` de Material los teñiría de un color).
  - *Qué ícono:* por tipo de casilla (`board/Icons.kt`, `Square.icon()`); servicios, mazos y descansos se distinguen por el nombre (sin tildes ni mayúsculas), y un nombre que no se reconoce cae en el ícono general del tipo: el editor (F4) puede renombrar sin romper nada. `IconTest`: los dos presets y los nombres desconocidos.
  - *Tamaños:* en el tablero, la mitad del ancho de la casilla (≤ 32 dp); casas y hotel, 1,3 × la letra; dados, 52 dp. En la escritura, «Con 2 casas» y «Con hotel» en texto (plurales de Android).
- **Cómo se revierte:** `git revert` del commit de FA.2; los emojis vuelven con él.
- **Estado:** vigente.

### D-30 · Carta de la casilla al tocarla (FA.3)

- **Pregunta:** qué se abre al tocar una casilla del tablero.
- **Opciones (maquetas en el Redmi, `capturas/FA.3_maquetas.png`):** A hoja inferior · B carta de chiva centrada · C ficha compacta.
- **Elección (del autor, 2026-10-06):** B.
- **Del agente:** `board/SquareCard.kt` (un `Dialog`: se cierra tocando fuera). Franjas de chiva, nombre en la franja del grupo (negra si no tiene), arte del lugar o, sin arte, su ícono sobre cielo; alquileres con la fila de lo construido resaltada; «Cobra ahora» sale de `rentDue` del motor (incluye el doble del grupo completo); hipoteca de `mortgageValue`; dueño con su ficha, casas o «Hipotecada»; libre: «la vende el Banco». Impuestos: lo que se paga. Mientras la carta está abierta, los diálogos del turno esperan (vuelven al cerrarla).
  - *Arte por lugar:* `arte/arte.py` escribe también `board/ArteLugares.kt` (clave = nombre sin tildes, minúsculas y `_`, `artKey`); FA.4 y FA.5 solo añaden lugares. `ArtKeyTest`.
  - *Extra de prueba:* `casilla` (índice) abre la carta al arrancar.
- **Cómo se revierte:** quitar `onSquare` del `Board` y la carta de `GameScreen`.
- **Estado:** vigente.

### D-31 · El arte de Tío Rico solo en la carta (FA.4)

- **Pregunta:** el *Terminado* de FA.4 dice «arte en el tablero y en la tarjeta»; en una casilla de ~36 dp la escena no se lee.
- **Opciones:** A el arte se ve al tocar la casilla (carta de D-30), el tablero sigue con nombre y precio · B recorte del monumento de fondo en cada casilla (maquetas).
- **Elección (del autor, 2026-10-06):** A. A mitad de FA.4 (15 lugares en `capturas/FA.4a_` a `FA.4d_cartas.png`) aprobó también el estilo para los grupos que faltan.
- **Cómo se revierte:** maquetas de B y un fondo por casilla en `Board`.
- **Estado:** vigente; vale también para FA.5.

### D-32 · Fase FB «Interfaz de chiva, personajes y logo» antes de F4

- **Pregunta:** el autor pide que la interfaz siga el estilo artístico, un logo para la app y poder escoger su jugador (hoy cada jugador es un círculo de color; el nombre ya se escribe en el menú y Ana, Beto… solo salen si se deja vacío).
- **Opciones:** jugador = personaje dibujado · personaje y color · solo un nombre más claro; la fase antes de F4 · tras el hito F4 · tras F5; el logo con el nombre «Mono» · sin texto hasta decidir el nombre.
- **Elección (del autor, 2026-10-07):** personaje dibujado (8, sin repetir); fase FB antes de F4; logo sin texto, el nombre visible sigue pendiente (P6).
- **Del agente:** con letra (FB) para no renumerar F4 y F5. Antes de F4 porque el editor nace con el estilo nuevo y no hay que retocarlo. Personajes e ícono salen de `arte/arte.py`, como el resto del arte (D-28); el ícono es adaptativo (capa de fondo y de frente: el formato que Android recorta en círculo o en gota según el teléfono).
- **Cómo se revierte:** quitar la sección FB del plan; la interfaz anterior sigue en el historial de Git.
- **Estado:** vigente.

### D-33 · Estilo de la interfaz: «Chiva de fiesta» (FB.1)

- **Pregunta:** cómo se ven las pantallas (colores, letra, botones, franjas y diálogos) para que sigan el arte de chiva (D-28).
- **Opciones (vistas en el Redmi, `capturas/FB.1_estilos.png`; código en `b91f161`, `demo/Maquetas.kt`):** A Chiva de fiesta (todo cargado) · B Carrocería azul (fondo azul, Lilita solo en títulos y botones) · C Sobria con acentos (el tablero de hoy con contorno negro; chiva solo en panel y diálogos).
- **Elección (del autor, 2026-10-07):** A.
- **Del agente (el tema para FB.2):**
  - *Colores:* pantalla sol `#FFC21A`; casillas y paneles blancos; tinta `#1B1B1B` para contornos y texto; casilla de turno `#FFE08A`; botón principal techo `#E63946` con texto blanco, secundario ocre `#FFD60A` con texto tinta; error en techo. Fichas de jugador, como hoy hasta FB.3-FB.4.
  - *Letra:* Lilita One (`res/font/lilita_one.ttf`, licencia OFL en `arte/letra/OFL_LilitaOne.txt`; trae tildes, ñ, ¿ y ¡) en todo: títulos, botones, nombres y cifras. Un `Typography` de Compose con esa familia, así todo `Text` la hereda.
  - *Contorno y sombra:* casilla 1,5 dp (3 dp la de turno); panel 2,5 dp con esquinas de 12 dp; botón 2 dp y esquinas de 10 dp; paneles, botones y diálogos con sombra negra sólida desplazada (4, 3 y 5 dp), como una calcomanía.
  - *Franjas:* de chiva (dientes rojo, amarillo, azul, verde y magenta sobre tinta) de 12 dp arriba y abajo de la pantalla y de 10 dp en la cabecera de cada diálogo; la de la carta (`SquareCard.kt`) pasa a ser la misma pieza.
  - *Diálogos:* carta blanca con contorno de 3 dp y esquinas de 16 dp, franja arriba, título centrado, banda del grupo con el nombre en mayúsculas, cifras en filas y dos botones lado a lado (secundario a la izquierda, principal a la derecha; texto en una línea, 16 sp: «Comprar −$240» cabe, M-026).
  - *Cómo:* un `ChivaTheme` (colores de Material 3 + `Typography` con Lilita) en `MainActivity` y piezas comunes (`FranjaChiva`, `Calcomania`, `BotonChiva`) en un archivo propio de la app; las pantallas usan esas piezas, no colores sueltos.
- **Cómo se revierte:** volver a `MaterialTheme` sin parámetros en `MainActivity` y a los componentes de Material; las opciones B y C siguen en `b91f161`.
- **Estado:** vigente.

### D-34 · Diálogos de turno como cartas de chiva (FB.2b)

- **Pregunta:** cómo aplicar D-33 a los 7 diálogos de turno (avisos, compra, subasta, Cárcel, impuesto, deuda y quiebra, fin) y cómo capturarlos sin jugar.
- **Del agente:**
  - *Una pieza:* `DialogoChiva` en `Chiva.kt`: `Calcomania` (contorno 3 dp, esquinas 16 dp, sombra 5 dp), franja de 8 dp arriba y abajo (la de la carta, `SquareCard.kt`), título blanco en una banda de color, cuerpo con scroll y botones `BotonChiva` debajo. No se cierra tocando fuera: el motor espera una decisión. La escritura de la compra deja la `Card` de Material por un contorno de tinta de 2 dp.
  - *Botones apilados, no lado a lado* (cambia lo dicho en D-33): a media anchura no caben «Pagar el 10 % de lo que tiene» ni «Hipotecar La Perseverancia (+$50)»; apilados, el principal (rojo) va primero salvo en la deuda, donde la quiebra va al final.
  - *Color de la banda por tipo:* techo rojo para decidir (compra, impuesto, deuda), azul para «Lo que pasó», magenta la subasta, tinta la Cárcel y verde el fin (colores de la franja, `Chiva.Azul`, `Magenta`, `Verde`).
  - *Texto de botón:* sigue en una línea; si no cabe, `autoSize` baja la letra de 16 a 11 sp («Vender un edificio de Calle del Embudo» se cortaba). «Comprar $60» pasa a «Comprar −$60» (M-026).
  - *Extra de prueba `fase`* (`compra`, `subasta`, `carcel`, `impuesto`, `deuda`, `fin`; `demo/SamplePhases.kt`, prueba `SamplePhasesTest` en los dos presets): abre la partida en ese diálogo sin el aviso inicial; la subasta sale del motor (rechazar y pujar el mínimo). Como `propiedades` y `hoja` (D-24), no guarda la partida.
  - *Menú y «Mis propiedades» (FB.2c):* el menú en `PantallaChiva` con «Juego» y «Jugadores» en paneles `Calcomania`, opciones con `OpcionChiva` (roja la elegida, blanca las demás), «Seguir la partida» en una calcomanía azul, campos con contorno de tinta y nombre de muestra en tinta al 40 %. La hoja con fondo sol, cabecera roja con el dinero y cada propiedad en su tarjeta con las jugadas debajo, a lo ancho (rojo si pagas, ocre si recibes).
- **Capturas:** `capturas/FB.2b_dialogos.png`, `capturas/FB.2c_menu_hoja.png`.
- **Cómo se revierte:** los diálogos, el menú y la hoja vuelven a los componentes de Material desde `2eb9a48`.
- **Estado:** vigente.

### D-35 · Personajes en estilo chiva y cómo van en la casilla (FB.3)

- **Pregunta:** qué 8 personajes y cómo se ven en una casilla del tablero (hoy la ficha es un círculo de color de 12 dp como máximo, en casillas de 49 a 78 dp).
- **Del autor:** los 8 de la «mezcla»: mono, chiva, sombrero vueltiao, colibrí, perro criollo, arepa, tinto y guacamaya (los 6 del plan más tinto y guacamaya).
- **Del agente:**
  - *Lienzo propio de 48 × 48, sin fondo*, contorno 1,4 y la paleta de D-28 (`arte.py`, sección «personajes», `PERSONAJES`); salen como `arte/svg/pj_<id>.svg` y `res/drawable/pj_<id>.xml` (ids `mono`, `chiva`, `sombrero`, `colibri`, `perro`, `arepa`, `tinto`, `guacamaya`). Sin fondo para que el color del jugador lo ponga la ficha, no el dibujo: los colores de jugador siguen marcando al dueño de cada propiedad.
  - *Ojos con brillo* (`brillos`, un trazo blanco de largo casi cero) en mono, perro y arepa.
  - *Tres opciones en la maqueta* (extra `maqueta`, `demo/Maquetas.kt`), con 1 a 4 fichas en casillas de 54 × 66 dp: **A** en la fila de abajo como hoy, cada personaje sobre un disco de su color (18 dp, 12 dp con 4); **B** grandes (60 % del ancho) escalonados encima de la casilla, parados en una base de su color; **C** medallones blancos de 26 dp con aro de su color, encimados abajo.
- **Captura:** `capturas/FB.3_personajes.png`.
- **Elección del autor:** C, medallones blancos con aro del color del jugador, encimados abajo (26 dp; FB.4 los lleva al tablero, el panel y los diálogos).
- **Cómo se revierte:** quitar la sección «personajes» de `arte.py` y correrlo; `Maquetas.kt` vuelve a `dab05d0`.
- **Estado:** vigente.

### D-36 · Escoger personaje en el menú y guardarlo con el jugador (FB.4)

- **Pregunta:** cómo escoge cada jugador su personaje y dónde se guarda.
- **Del autor:** opción **B** de la maqueta (`capturas/FB.4_opciones.png`): los 8 bajo cada nombre; y el personaje guardado en el jugador.
- **Del agente:**
  - *Motor:* `PlayerState.token: String? = null` (id de `arte.py`) y `Engine.newGame(…, tokens)`; el motor no lo usa para ninguna regla. Una partida guardada antes no trae la clave y carga con null (kotlinx-serialization usa el valor por defecto); pruebas en `SavedGameTest`. En F5 viaja solo con el estado.
  - *Menú:* cada jugador empieza con el personaje de su posición (Ana el mono, Beto la chiva…); tocar uno libre lo escoge; los de otros salen atenuados con el aro de su dueño y no se pueden tocar. `playerTokens` (`NewGameForm.kt`) garantiza que no se repitan, también al subir cuántos juegan (el posterior toma el primero libre).
  - *Lista y dibujos:* `board/Personajes.kt` lo genera `arte.py` (orden = el de `PERSONAJES`); `personaje(token, k)` da el dibujo, y sin personaje o con un id desconocido, el de la posición `k`.
  - *Dónde se ve:* `PlayerToken` (medallón de D-35) reemplaza a `Token` en el tablero (encimados abajo, hasta 30 dp), el panel de jugadores (20 dp), «Mis propiedades» (30 dp) y la carta (dueño, 24 dp). Los diálogos de turno no tenían círculo; no se les añadió nada.
- **Capturas:** `capturas/FB.4_opciones.png` (maqueta), `capturas/FB.4_pantallas.png` (menú, partida y hoja).
- **Cómo se revierte:** `Token` y el menú sin personajes están en `29e7480`; el campo `token` puede quedarse (null no cambia nada).
- **Estado:** vigente.

### D-37 · Jugar contra la máquina queda como idea, después del hito (FB.4)

- **Pregunta:** al probar FB.4, el autor notó que no puede decir quién es él y quién es «la IA».
- **Hecho:** no hay IA; los 2-6 jugadores son personas que se pasan el teléfono (pasar y jugar), y `PLAN_MONO.md` §1 deja «jugar contra la máquina» fuera de la v1.
- **Del autor:** aprobar FB.4 tal cual y anotar el jugador automático como idea para después del hito F4.5, sin programarlo (opciones descartadas: una línea aclaratoria en el menú; una fase FC de IA).
- **Si se retoma:** fase con letra (D-27): jugador automático en `engine` con semilla (D-03) que elige entre las jugadas legales, y en el menú, persona o máquina por jugador; el campo iría en `PlayerState` como `token` (D-36).
- **Estado:** vigente.

### D-38 · Logo e ícono de la app: la chiva (FB.5)

- **Pregunta:** qué logo sin texto (P6 sigue abierta) lleva el ícono de Mono.
- **Del autor:** opción **B**, la chiva de frente sobre rombos (`capturas/FB.5_logos.png`; descartadas A, el mono en un sol, y C, un dado de colores; las tres en `00e8e3e`).
- **Del agente:**
  - *Lienzo:* 108 × 108, el del ícono adaptativo de Android: fondo que llena todo (verde con rombos amarillos, rojos y blancos) y frente que cabe en el círculo central de 66 (la chiva de `pj_chiva` × 1,4 con `escalar`); contorno 2,4 y líneas 1,8. El lanzador recorta las 72 del centro con la forma que quiera (HyperOS: cuadrado redondeado).
  - *Salidas de `arte.py`:* `logo_chiva_fondo.xml`, `logo_chiva_frente.xml`, `arte/svg/logo_chiva.svg` y, por `LOGO_APP`, `res/mipmap-anydpi-v26/ic_launcher.xml`; el manifiesto usa `@mipmap/ic_launcher` como `icon` y `roundIcon`. Con minSdk 26 no hacen falta PNG por densidad.
  - *Amarillo:* `bolt`, no `sun`: `trazos` le pone rayos a la primera forma `sun`.
  - Sin capa monocroma (íconos temáticos de Android 13): el lanzador usa el de color. Se añade si el autor lo pide.
- **Captura:** `capturas/FB.5_lanzador.png` (lanzador del Redmi).
- **Cómo se revierte:** quitar `icon`/`roundIcon` del manifiesto (vuelve el ícono de Android); otro logo = otra función en `LOGOS` y cambiar `LOGO_APP`.
- **Estado:** vigente.


### D-39 · Editor de casillas: tablero + ficha (F4.1)

- **Pregunta:** cómo se ve y cómo funciona el editor de casillas (nombre, precio, grupo, alquileres).
- **Del autor:** opción **B**, el tablero en anillo arriba (se toca la casilla) y su ficha debajo (`capturas/F4.1_maquetas.png`; descartadas A, lista + ficha, y C, la escritura con − / +; las tres en `15b5882`).
- **Del agente:**
  - *Motor:* `GameConfig.withSquare(i, casilla)` (`engine/Editor.kt`), puro y sin validar; el ViewModel llama después a `validate` y solo guarda si la edición no añade errores (los ya presentes del preset no bloquean). `EditorTest`: el alquiler editado es el que cobra R-13 y el precio el que paga R-11.
  - *Arte:* cada casilla lleva `art: String?` (null = la clave sale del nombre, como hasta FA). Al renombrar una casilla sin clave, el editor le pone la de su nombre de antes (`SquareDraft.applyTo`), así «Las Cruces» renombrada conserva su dibujo. Los JSON viejos cargan igual (por defecto null).
  - *Qué se edita:* propiedad (nombre, precio, grupo, 6 alquileres), ferrocarril (nombre, precio, 4 alquileres), servicio (nombre y precio; los multiplicadores de dados no), impuesto (nombre y valor fijo), las demás solo el nombre. Cifras: solo dígitos, hasta 5; nombre hasta 24 (`MAX_SQUARE_NAME`, el del validador, ahora público).
  - *Dónde vive:* botón «Editar casillas» del menú, para el juego elegido; «Listo» o atrás vuelve. El tablero editado vive mientras la app esté abierta (`rememberSaveable`, JSON por preset) y se usa al «Empezar»; guardarlo en archivo es F4.4. La partida guardada ya lleva su configuración, así que «Seguir la partida» conserva lo editado.
  - *Extra de prueba:* `--ei editor <casilla>` abre el editor del Clásico con esa casilla elegida.
- **Captura:** `capturas/F4.1_editor.png`.
- **Cómo se revierte:** quitar el botón del menú y `editing` en `MainActivity`; `art` y `withSquare` no estorban.
- **Estado:** vigente.

### D-40 · Editor de reglas: pestañas por tema, dentro del editor (F4.2)

- **Pregunta:** cómo se ve el editor de reglas y desde dónde se entra.
- **Del autor:** maqueta **B**, pestañas por tema con una calcomanía por regla y una línea que la explica (`capturas/F4.2_maquetas.png`; descartadas A, lista larga, y C, seis fichas grandes y «Más reglas»; las tres en `fd493f7`). Se entra por una pestaña «Casillas | Reglas» dentro del editor, no por otro botón del menú (que pasa a «Editar casillas y reglas»).
- **Del agente:**
  - *Motor:* `GameConfig.withRules(reglas)` (`engine/Editor.kt`); el ViewModel valida como en D-39 (solo se guarda si no añade errores). `EditorTest`: el Clásico con salario $300 cobra $300 al pasar la Salida (R-10); salario 100 000 y 5 casas los marca el validador.
  - *Qué se edita* (`game/RuleRows.kt`, 28 filas en 6 temas: Dinero, Dados, Casas, Alquiler, Hipotecas, Fin): las opciones de F1.3 que el juego ya usa. Fuera: jugadores (se eligen en el menú), tratos entre jugadores (`tradeBuildings`, `tradeFee`, `mortgagedTradeInterest`: el motor aún no tiene `Trade`), fin por tiempo (no hay reloj), reclamar el alquiler y el bote de Parada Libre (el validador los rechaza, D-12, D-13), `bankUnlimited` y `unsoldStaysWithBank` (el motor no los distingue) y si hay Cárcel (depende del tablero; F4.3).
  - *Cifras con − / +:* dinero de $10 bajo $100, de $50 bajo $1000, de $100 bajo $10 000 y de $1000 desde ahí; porcentajes de 5 en 5; conteos de 1 en 1; los dobles a la Cárcel saltan entre Nunca, 2..5. Precio fijo de casa y hotel y la subasta: «De cada casilla / Fijo» (o «Cualquier precio / Precio −»), que es el null de `RuleOptions`. Los rangos los pone el validador (D-09).
  - *Marcas:* lo que difiere de la caja dice «antes $200» en rojo (`preset` en el ViewModel). Textos `rules_*` en `strings.xml` (los `rule_*` son del validador).
  - *Extra de prueba:* `--es reglas <tema>` abre la pestaña Reglas en ese tema.
- **Captura:** `capturas/F4.2_editor.png`.
- **Cómo se revierte:** quitar la fila «Casillas | Reglas» de `EditorScreen` y `RulesPane`; `withRules` no estorba.
- **Estado:** vigente.

### D-41 · Tamaño del mapa: quitar y añadir desde la ficha (F4.3)

- **Pregunta:** cómo se añaden y quitan casillas en el editor, y qué pasa con lo que depende de su posición.
- **Del autor:** maqueta **A**, «Quitar esta casilla» y «Añadir después» (con su tipo) en la ficha de la casilla tocada; el error de tamaño en rojo (`capturas/F4.3_maquetas.png`; descartadas B, pestaña «Tamaño» de 4 en 4, y C, lista con ✕; las tres en `4fca620`). Pidió además que el texto de las celdas no se vea «tan espichado».
- **Del agente:**
  - *Motor:* `withSquareAdded(i, casilla)` y `withSquareRemoved(i)` (`engine/Editor.kt`), puros y sin validar. Las cartas «Avance hasta…» siguen a su casilla; la que lleva a una casilla quitada sale del mazo (su texto la nombra); un grupo que queda sin propiedades se borra. La salida (0) no se quita ni se inserta antes de ella. `EditorTest` +5: el Clásico sin las 4 últimas de cada lado (24 casillas) es válido, cobra el salario (R-10) y de 40 partidas simuladas de 1500 turnos terminan en quiebra 13 (con 6000, 14): sin tratos entre jugadores casi nadie junta un grupo.
  - *Anillo impar:* al quitar de una en una se pasa por 39; `RingGrid.fit` dibuja n + 1 y deja vacía la celda de antes de la salida (antes cerraba la app).
  - *Interfaz* (`game/NewSquare.kt`, `EditorScreen.Tamano`): quitar y añadir se aplican al instante, aunque el tablero quede inválido; sus errores salen en rojo sobre la ficha y el menú no deja «Empezar» ese juego (`menu_broken`). Tipos para añadir: Propiedad, Tren, Carta e Impuesto; la nueva copia la más cercana de su tipo hacia atrás (cifras, grupo, mazo) y propiedad o tren se llaman «Casilla nueva». La Cárcel tampoco se quita (no se repone). Un quinto tren lo marca el validador (falta su alquiler; ningún reglamento lo da).
  - *Celdas* (lo pedido por el autor): la letra del nombre baja de medio en medio punto, hasta 5.5 sp, hasta que cada palabra quepa entera en dos líneas (`NombreCasilla`), y el tablero del editor es 0.8 de alto por ancho (40 casillas: 10 × 12 de 38 dp, antes 11 × 11 de 35 dp). «Perseverancia» con 38 casillas aún sale con «…».
  - *Extras de prueba:* `--es quitar 13+14` (casillas ya quitadas) y `--ez abajo true` (abre desplazado hasta la ficha).
- **Captura:** `capturas/F4.3_editor.png`, `capturas/F4.3_ficha.png`.
- **Cómo se revierte:** quitar los botones de la ficha; `withSquareAdded`/`withSquareRemoved` no estorban.
- **Estado:** vigente.

### D-42 · Tableros propios: lista en el menú y un JSON por tablero (F4.4)

- **Pregunta:** dónde se eligen, nombran, duplican y borran los tableros editados, y cómo sobreviven a cerrar la app.
- **Del autor:** maqueta **A**, la tarjeta «Juego» del menú lista los originales y los propios (★) con sus casillas; con uno propio elegido, su nombre se cambia ahí mismo y salen Editar / Duplicar / Borrar (`capturas/F4.4_maquetas.png`; descartadas B, pantalla «Mis tableros», y C, nombre en el editor; las tres en `7d8399d`).
- **Del agente:**
  - *Archivos* (`game/BoardShelf.kt`): un JSON por tablero en `files/tableros/t<n>.json`, con su nombre en `GameConfig.name`; se escribe a un temporal y se renombra, como `partida.json` (D-26). Uno dañado se salta. Ids `t1`, `t2`… en orden de creación. `BoardShelfTest` 6 pruebas.
  - *Los originales no se tocan:* editar el Clásico o Tío Rico y volver con cambios guarda una copia «Clásico copia» (después «copia 2»…) que queda elegida; sin cambios no se guarda nada. Duplicar hace lo mismo con cualquiera. Nombre de hasta 20 letras; vacío, se queda el de antes; se guarda al escribir.
  - *Borrar* pide confirmación (`DialogoChiva`, «Borrar» / «Dejarlo»); la partida guardada no depende del tablero (lleva su propia copia en `partida.json`).
  - Un tablero propio con errores se guarda igual, sale en rojo y no deja «Empezar» (como F4.3).
  - El editor se abre cada vez desde lo guardado (clave `editor-<tablero>-<veces>`); antes reusaba el ViewModel de la vez anterior.
  - *Extra de prueba:* `--es tablero t1` abre el menú con ese tablero elegido.
- **Captura:** `capturas/F4.4_menu.png`, `capturas/F4.4_propio.png`.
- **Cómo se revierte:** volver a las dos fichas Clásico / Tío Rico; los archivos de `tableros/` no estorban.
- **Estado:** vigente.

### D-43 · Hito «Primera quiebra»: Clásico de 32, 2 jugadores, sin tratos (F4.5)

- **Pregunta:** qué tablero, cuántos jugadores y si hacen falta tratos (`Trade`) para que la partida del hito termine (duda abierta en D-41).
- **Medido (sesión 27, prueba temporal con `simulate`, 40 semillas, tope de 1500 turnos de jugador):** el dinero inicial no acorta las partidas (Clásico de 24 con 4 jugadores: terminan 3, 3, 2, 5 y 6 de 40 con $1500, 1000, 700, 500 y 300). Por tablero, con $1500, terminan (mediana de turnos): de 24, 24/40 (106) con 2, 12/40 (159) con 3 y 3/40 (126) con 4; de 32 (Clásico sin las casillas 2, 4, 7, 17, 22, 33, 36 y 38: cartas e impuestos; válido), 36/40 (142), 22/40 (188) y 19/40 (246); de 40, 36/40 (168), 22/40 (239) y 18/40 (267). Sin tratos casi nadie completa un grupo, y sin grupo no se construye.
- **Del autor:** Clásico de 32, 2 jugadores (la primera quiebra cierra la partida), sin tratos; los tratos quedan para después del hito.
- **Del agente:** no hace falta código; el autor quita las 8 casillas en el editor y cambia el nombre y el precio de una propiedad.
- **Resultado (2026-10-07, aprobado por el autor):** «Clásico copia» de 32 casillas, 2 jugadores; en el turno 123 Beto cayó en La Soledad, debía $750 de alquiler y quebró (`GameOver(winners=[0])`, log 17:57). Se quitaron Las Cruces, Contribuciones, La Perseverancia, Chapinero, Estación del Sur, Estación del Oriente, Usaquén e Impuesto de lujo (el agente dio números de casilla en vez de nombres). No quedó ninguna casilla con nombre o precio cambiado: el autor no tocó «Guardar casilla». Aprobó así, porque cambiar nombre y precio ya estaba aprobado en el Redmi en F4.1.
- **Cómo se revierte:** otro tablero o más jugadores; los tratos, como fase con letra (D-27).
- **Estado:** vigente.

### D-44 · El PC hace de segundo jugador de pruebas por Bluetooth (F5)

- **Pregunta:** cómo probar F5 sin un segundo teléfono (P4) y sin descargar nada nuevo.
- **Comprobado (sesión 27):** el PC tiene adaptador Bluetooth (apagado: `bluetoothctl show` → `Powered: no`) y el Python del sistema trae `socket.AF_BLUETOOTH` y `BTPROTO_RFCOMM`. La imagen del emulador (`android-37.0`) ya está, pero su Bluetooth es virtual y no conecta con un teléfono real (por confirmar en la documentación de Android en F5.1).
- **Del autor:** el PC como segundo jugador de pruebas. Los *Terminado* de F5.3, F5.4 y F5.5 pasan a Redmi ↔ PC; F5.6 (hito «Enlace») sigue con el teléfono de un amigo.
- **Consecuencia:** el transporte es Bluetooth clásico RFCOMM (Nearby Connections solo funciona entre Androids). F5.1 sigue pidiendo su prueba de concepto y su D-## aprobada. Para F5.4, el motor (Kotlin puro, D-02) corre en el PC como jugador de terminal, unido al Bluetooth por un puente en Python. *(Cambiado por D-50: con la red local, el motor se une por TCP directo, sin puente.)*
- **Cómo se revierte:** devolver los *Terminado* al teléfono de un amigo; lo del PC queda como herramienta de pruebas.
- **Estado:** vigente.

### D-45 · Transporte de F5: Bluetooth clásico RFCOMM, el teléfono anfitrión hace de servidor

- **Pregunta (F5.1):** con qué transporte y con qué papeles hablan los dos aparatos.
- **Comprobado en la documentación (2026-10-07):** `bt-permissions` (Android 12+: `BLUETOOTH_CONNECT` para hablar con emparejados, `BLUETOOTH_SCAN` solo para buscar, `BLUETOOTH_ADVERTISE` solo para hacerse visible; `BLUETOOTH` y `BLUETOOTH_ADMIN` con `maxSdkVersion="30"`) y `connect-bluetooth-devices` (`accept` y `connect` bloquean: hilo propio; un cliente por canal). Fuentes del SDK 37: `listenUsingRfcommWithServiceRecord` y `getBondedDevices` piden `BLUETOOTH_CONNECT`; `cancelDiscovery`, `BLUETOOTH_SCAN`; `isEnabled` y `createRfcommSocketToServiceRecord`, ninguno.
- **Decisión:** RFCOMM seguro (cifrado y con emparejamiento) con un UUID propio (`1a80cf3d-…`, `enlace/EcoServer.kt`). El anfitrión (el que decide en F5.2) es el servidor: escucha con `listenUsingRfcommWithServiceRecord("Mono", UUID)` y el invitado se conecta. Así la app solo pide «Dispositivos cercanos» (`BLUETOOTH_CONNECT`); buscar teléfonos sin emparejar (`BLUETOOTH_SCAN`) queda para F5.3 si hace falta. Nearby Connections queda fuera (D-44: no habla con el PC).
- **Prueba de concepto (medida):** pantalla `--es enlace eco` en el Redmi + `python3 pc/eco.py` en el PC (canal buscado con `sdptool browse`: cambia al reabrir, 5 y luego 6). Conexión en 105 ms; 20/20 y 200/200 ecos correctos, ida y vuelta mediana 10-11 ms (máx 66 y 120 ms); UTF-8 («ñ») intacto. Tras cerrarse el PC, el Redmi vuelve a escuchar a los 2 s. Pruebas: `EchoLoopTest` (2) y `pc/test_eco.py` (4).
- **Tropiezo:** el PC tenía al Redmi como emparejado pero el Redmi ya no al PC: `Connection refused`. Se arregló quitando el emparejamiento en el PC (`bluetoothctl remove`) y emparejando de nuevo desde el Redmi.
- **Cómo se revierte:** el transporte queda detrás del bucle de mensajes (`echoLoop` no sabe de Bluetooth); cambiar a otro es reescribir `EcoServer`.
- **Estado:** reemplazada por D-48 en la partida (2026-10-07): el Bluetooth queda solo en la prueba del eco.

### D-46 · Protocolo de la partida en enlace: el anfitrión decide y numera, el invitado aplica y compara

- **Pregunta (F5.2):** cómo quedan iguales las dos copias del motor aunque se pierdan o desordenen mensajes.
- **Decisión:** `engine/.../link/` (Kotlin puro, para que el PC lo use en F5.4). Mensajes JSON compactos, uno por línea, con `PROTOCOL_VERSION = 1`: `Hello`, `Snapshot` (tablero + estado + jugadores del invitado), `Propose(acción, after)`, `Applied(n, acción, resumen)`, `Rejected`, `Resync(after, full)`, `Bye`. El anfitrión (`Host`) aplica, numera y guarda el registro; una propuesta hecha sin ver lo último no se aplica: se le reenvía lo que falta. El invitado (`Guest`) guarda las adelantadas, ignora las repetidas, aplica en orden y compara el resumen (FNV-1a de 64 bits del JSON del estado); si no cuadra pide la partida entera. Sin noticias en un rato, `timeout()` pide lo que falta. Los dados no viajan: el azar va en `GameState.random` (D-03). Quién decide una acción: `actor()` (pujador, deudor o quien juega); `TimeUp` solo lo manda el anfitrión; `Trade` se rechaza hasta tener motor.
- **Medido (`LinkTest`, 5 pruebas):** 1000 partidas (Clásico y Tío Rico, 16-48 casillas, 2-6 jugadores, tope 150 turnos) por un transporte falso que pierde 5 %, duplica 3 % y adelanta 10 %: las 1000 acaban con el mismo estado y resumen en los dos lados, 0 resúmenes distintos; 453 215 acciones, 1 237 749 mensajes, 62 055 perdidos, 28 851 pedidos de reenvío; 66 s.
- **Cómo se revierte:** el protocolo no toca el motor; se cambia en `link/` subiendo `PROTOCOL_VERSION`.
- **Estado:** vigente (aprobada por el autor, 2026-10-07).

### D-47 · Crear y unirse a una partida por Bluetooth: cada jugador elige «Aquí» u «Otro teléfono»; unirse, a un emparejado

- **Pregunta (F5.3):** cómo se arma en la app una partida entre dos teléfonos y cómo encuentra el invitado al anfitrión.
- **Opciones vistas en el Redmi** (`capturas/F5.3_maquetas.png`, commit `2d2005a`): A sala de espera (el amigo toma siempre el último jugador), B en «Jugadores» cada uno marca «Aquí» u «📶 Otro teléfono», C unirse buscando teléfonos cercanos sin emparejar (un permiso más, `BLUETOOTH_SCAN`).
- **Del autor (2026-10-07):** B para crear y, para unirse, solo los teléfonos ya emparejados (sin C).
- **Decisión:** con alguno en «Otro teléfono», «Empezar» pasa a «Esperar al otro teléfono» y abre la sala (`enlace/HostScreen.kt`): crea la partida con `Engine.newGame`, la pone en un `Host` con esos asientos (D-46) y la atiende por RFCOMM (`RfcommServer`, `hostLoop`). Hace falta al menos uno «Aquí». La sala no deja apagar la pantalla (`keepScreenOn`): HyperOS corta el Bluetooth en segundo plano. El permiso y el encendido del Bluetooth van en `BluetoothGate`, que comparte con la prueba del eco. Para probar sin tocar la pantalla: `--es enlace sala` (Clásico, el último de `jugadores` allá). En el PC, `pc/invitado.py` hace de invitado (`hello`, y cada 10 s `resync` con la partida entera para medir).
- **Cómo se revierte:** quitar el interruptor del menú; la sala y el bucle no tocan el motor.
- **Transporte:** la sala pasó de RFCOMM a la red local (D-48); el menú, `hostLoop` y `guestLoop` no cambian.
- **Estado:** vigente (aprobada por el autor, 2026-10-07).

### D-48 · Transporte de la partida: la red local (Wi-Fi o punto de acceso), TCP y anuncio NSD; el Bluetooth queda de prueba

- **Pregunta (F5.3, del autor):** el Bluetooth comparte la radio con los auriculares y obliga a emparejar una vez por amigo; ¿hay algo más cómodo cerca, con o sin internet?
- **Opciones:** red local con TCP y NSD; Nearby Connections (solo entre Android, sin el PC de D-44, depende de Play Services); Bluetooth sin emparejar (dos permisos más); servidor en internet (descartado: publicar y mantener algo, y sin internet no va).
- **Del autor (2026-10-07):** la red local. Los dos en el mismo Wi-Fi o, si no hay, el anfitrión enciende su punto de acceso y el invitado se conecta (funciona sin internet). Dos teléfonos solo con datos móviles no se ven.
- **Decisión:** la sala abre un `ServerSocket` en un puerto libre y lo anuncia por NSD (mDNS) como `_mono._tcp` con el nombre del teléfono (`enlace/LanServer.kt`); muestra además su dirección (`En la red: 10.0.1.192:46199`) por si la búsqueda falla. `LanGate` espera a tener una IPv4 local (descarta datos móviles `ccmni`/`rmnet`, VPN y bucle; `lanAddresses`, probada con las interfaces reales del Redmi) y pide `ACCESS_LOCAL_NETWORK` solo desde Android 17 (API 37): con `targetSdk 37` hace falta para hablar con la red local y anunciar por NSD lo exige siempre (`NsdManager.java` del SDK 37, líneas 129-130 y 362). El Redmi es Android 16 (SDK 36): no lo pide. `pc/invitado.py` busca la sala con `avahi-browse -rpt _mono._tcp` (o `--ip`/`--puerto`; `--bt` para el Bluetooth).
- **Medido (Redmi y PC en el mismo Wi-Fi, 1 min):** el PC encontró la sala solo (8,4 s con la búsqueda); 6/6 pedidos de la partida entera, ida y vuelta mediana 34 ms (mín 25, máx 58) frente a 539 ms por Bluetooth (10 min, D-45).
- **Medido (10 min por Wi-Fi):** 10,0 min sin cortes, 59/59 pedidos bien, ida y vuelta mediana 49 ms (mín 18, un pico de 3737 ms en el minuto 9).
- **Por medir (F5.4):** la sala con el punto de acceso del Redmi encendido (que el anuncio NSD salga por esa interfaz).
- **Cómo se revierte:** `HostLink` vuelve a crear `RfcommServer` en vez de `LanServer` (los dos dan los mismos `LinkEvent`).
- **Estado:** vigente (aprobada por el autor, 2026-10-07).

### D-49 · F5.9: limpiar el Bluetooth antes de F5.4

- **Pregunta (del autor, 2026-10-07):** con la partida en la red local (D-48), ¿sigue el Bluetooth en la app? Quedan la prueba del eco (`RfcommServer`, `BluetoothGate`, `EcoServer`, `EcoScreen`: 234 líneas), el permiso `BLUETOOTH_CONNECT`, `pc/eco.py` y `invitado.py --bt`.
- **Del autor:** quitarlo en un paso antes de F5.4, con la interfaz: lo que en pantalla, textos y permisos habla de Bluetooth (no un repaso del resto de la app).
- **Decisión:** casilla **F5.9** escrita entre F5.3 y F5.4 (el siguiente número libre: `tablero.py` solo cuenta `F5.<número>` y F5.4-F5.8 ya se citan en fichas y referencias; D-27). `MONO_UUID` y `SERVICE_NAME` se van con `RfcommServer.kt`; lo que se reutiliza (`LinkEvent`, `hostLoop`, `guestLoop`) se queda. Los archivos los borra el autor con `! git rm` (regla de la skill).
- **Del autor (2026-10-07, al aprobar F5.9):** el plan pasa a «por la red local» en el hito F5.6, F5.7 y la tabla de riesgos; las maquetas de F5.3 (`demo/Maquetas.kt`) se quedan como registro. Hecho en `9fcece6` (21 archivos, −472 líneas).
- **Cómo se revierte:** el Bluetooth queda en la historia de Git (`6765db8` y anteriores).
- **Estado:** vigente (pedida por el autor, 2026-10-07).

### D-50 · F5.4: el jugador del PC es un programa de consola en Kotlin, con el mismo motor, y juega solo

- **Pregunta:** cómo juega el PC su parte en F5.4 (D-44 preveía un puente en Python hacia el Bluetooth).
- **Opciones:** Kotlin automático y manual · Kotlin solo automático · ampliar `pc/invitado.py` sin motor (más barato, pero no compara el resumen del estado).
- **Del autor (2026-10-07):** Kotlin, solo automático.
- **Decisión:** módulo Gradle `:terminal` (Kotlin/JVM con el plugin `application` de Gradle, sin dependencias nuevas) que usa `Guest` del motor por TCP directo a la sala (busca con `avahi-browse`, o `--ip`/`--puerto`). `choose` compra siempre que puede, construye una de cada tres veces al terminar el turno, pasa en las subastas y en una deuda vende, hipoteca o quiebra; su azar es suyo, con `--semilla` (D-03). Lanzador: `./gradlew :terminal:installDist` → `terminal/build/install/mono-pc/bin/mono-pc`.
- **Medido (`TerminalTest`, localhost):** 10 partidas de 2 jugadores y 5 de 4 terminadas, 0 resúmenes distintos. Con el Clásico normal no terminan (más de 20 000 acciones: sin tratos nadie completa un grupo, D-43); con $300 al empezar y salario $0, 2 jugadores: 49-840 acciones, mediana 120 (25 turnos), unas 40 del teléfono.
- **Cómo se revierte:** quitar `:terminal` de `settings.gradle.kts`; el protocolo no cambia.
- **Estado:** vigente.

### D-51 · F5.4b: la sala vive en el ViewModel de la partida y escribe por un buzón con hilo propio

- **Pregunta:** dónde vive el servidor de la sala para que no se cierre al pasar de la sala a la partida, y cómo manda el teléfono sus jugadas sin usar la red desde el hilo principal (Android lo prohíbe).
- **Decisión:** `enlace/HostRoom.kt` tiene el `Host`, el `LanServer` y el `Outbox` de la conexión abierta; va dentro del `GameViewModel` (`remote`, interfaz `game/Remote.kt`), que sobrevive al cambio de pantalla y al giro. `enlace/LinkedGame` muestra la sala hasta el `Hello` y después la partida. `Outbox`: cola con un hilo que escribe en orden; `send` no espera. Las jugadas de aquí (`playLocal`) y las del invitado (`hostLoop`) pasan por el mismo candado del `Host`, y cada una lleva su número para que el ViewModel no retroceda si una llega tarde. `Host.events` guarda los eventos de la última jugada (para los avisos). En los turnos del otro teléfono (`decider`: postor, deudor o el del turno) el centro dice «Esperando a X 📶» sin botones ni diálogos; si se cortó, lo dice. Las jugadas del otro teléfono van todas a «Lo que pasó» (una compra no es notable en un solo teléfono, D-22, pero aquí no se vio). La partida enlazada no se guarda ni se reinicia (sin botón «Nueva partida» al final); salir de la sala la cierra.
- **Medido (`OutboxTest`, localhost):** 400 jugadas, 201 del teléfono y 199 del invitado a la vez, 0 resúmenes distintos (4,4 s). Sin reloj, el invitado se quedaba con una propuesta cruzada sin respuesta: el PC ya pide lo que falta con `Guest.timeout()`.
- **Cómo se revierte:** `HostScreen` vuelve a crear su `Host` y `LanServer` (commit `6ceb104`).
- **Estado:** vigente.

### D-52 · F5.8 «Jugar contra la máquina» sube justo detrás de F5.4 y deja de ser extra

- **Del autor (2026-10-07):** moverla como paso inmediatamente siguiente; deja de ser extra; *Terminado* con «máquina más lista» (opciones: partida en el Redmi con la lógica de `mono-pc` · solo en el motor, 100 partidas en la JVM · máquina más lista).
- **Decisión:** en `PLAN_MONO.md`, F5.8 va entre F5.4 y F5.5, sin renumerar (como F5.9, D-27). Un jugador del menú se marca «🤖 Máquina» y juega solo; puja en subastas y construye con criterio. *Terminado:* una partida corta ($300, salario $0) del autor contra la máquina, terminada en el Redmi con captura, y la máquina lista gana al menos 3 de 10 partidas simuladas contra la simple (la de `mono-pc`, D-50).
- **Cómo se revierte:** devolver la casilla al final de F5 con «(Extra)».
- **Estado:** vigente.

### D-53 · K18: sin emojis en la app; «📶» y «★» pasan a íconos dibujados

- **Del autor (2026-10-07, K18, M-087):** ningún emoji en textos, `strings.xml`, logs ni comentarios de `app/`; lo que haría un emoji es un ícono en el estilo de la app.
- **Decisión:** dos íconos 24 × 24 nuevos en `arte/arte.py` (`ICONOS`): `enlace` (cuatro barras azules `sea` que suben, lo del otro teléfono) y `propio` (estrella amarilla `bolt`, tablero propio); salen como `ic_enlace.xml` e `ic_propio.xml` y en Kotlin como `Icon.ENLACE` e `Icon.PROPIO`. `OpcionChiva` gana `icono` (como `BotonChiva`), y en un texto el ícono va al lado en un `Row`: botón «Unirme», ficha «Otro teléfono», sala («Otro teléfono: X»), «Esperando a X» y la nota de la copia («la de la estrella»). Se quita `menu_board_own` (era `menu_board` con «★»). El log dice «otro teléfono». `sin_emojis.py app/src/main`: 12 líneas → 0. Captura: `capturas/k18_sin_emojis.png` (menú, sala, unirme).
- **Cómo se revierte:** devolver los símbolos a `strings.xml` (lo prohíbe K18).
- **Estado:** vigente.

### D-54 · F5.8a: la máquina vive en el motor (`bot/Bots.kt`), simple y lista, y solo elige jugadas que el motor acepta

- **Del autor (2026-10-07):** F5.8 en sub-pasos a (motor), b (app), c (partida en el Redmi); en el plan, «Máquina» con un ícono dibujado (K18).
- **Decisión:** `fun interface Bot` en `engine/.../bot/Bots.kt`, sin Android y con su `Random` con semilla (D-02, D-03). `SimpleBot` es la de `mono-pc` (D-50), movida tal cual; `:terminal` la usa (sus 15 partidas siguen en verde). `SmartBot` guarda una reserva de $50: compra si le queda la reserva o si completa un grupo; en la subasta puja (mayor + 5 % del precio, desde la base) hasta el precio, o ×1,5 si completa o bloquea un grupo; construye parejo y sin bajar de la reserva; levanta hipotecas con $100 de holgura; en el impuesto paga lo menos; en una deuda hipoteca lo suelto, luego vende casas (desde la que más tiene), luego hipoteca lo de grupos y al final quiebra. No son reglas: todo pasa por `Engine.tryApply`, así que no llevan R-##.
- **Medido:** `BotsTest`: 10 partidas cortas ($300, salario $0, 2 jugadores, asientos alternados, semillas 1-10): la lista gana 10/10, todas terminan, turnos 17-73 (mediana 28,5). La ventaja grande es la subasta: la simple siempre pasa.
- **Cómo se revierte:** `mono-pc` vuelve a su `choose` propio y se borra `bot/`.
- **Estado:** vigente.

### D-55 · F5.8b: «Máquina» es el robot junto al nombre (maqueta B) y la juega el `GameViewModel` con pausa

- **Del autor (2026-10-07):** de las maquetas A (tercera opción «Aquí · Otro · Máquina»), B (botón con el robot junto al nombre) y C (contador «Máquinas»), eligió la **B** (`capturas/f5_8b_maquetas.png`).
- **Decisión:** ícono `maquina` en `arte.py` (cabeza de robot, `Icon.MAQUINA`, K18). En el menú, el robot apagado (35 %) junto a cada nombre; prendido, cambia «Aquí / Otro teléfono» por «Juega la máquina»; «Empezar» pide que alguien juegue aquí. `game/Machine.kt` (sin Android): si `decider` es de `bots`, la jugada de `SmartBot` (o la de `SimpleBot` si la lista no tiene). El `GameViewModel` la juega con `viewModelScope` tras `pause` = 900 ms, una a la vez; lo que hizo va entero a «Lo que pasó» (como lo del otro teléfono, D-51) y en su turno no hay botones, sino el robot y «Juega X». `SavedGame.bots` guarda quién es máquina (una partida vieja carga sin máquinas); también vale en la sala del enlace. Extra de prueba `maquina` (`--es maquina 1+2`).
- **Medido:** `MachineTest`: 5 partidas cortas de 3 máquinas terminan; en el Redmi (`--es maquina 1 --el semilla 7`), Beto tiró, sacó carta, compró la Estación de la Sabana y pasó el turno, con 903 ms entre jugadas (log) (`capturas/f5_8b_maquina_juega.png`).
- **Ojo:** el aviso «Lo que pasó» tapa el letrero «Juega X» casi siempre (la máquina sigue jugando debajo); se juzga en F5.8c.
- **Cómo se revierte:** se quitan `bots` del `GameViewModel`, del menú y de `SavedGame`, y `Machine.kt`.
- **Estado:** vigente.

### D-56 · Quiebra voluntaria: en tu turno, cuando quieras, con «¿Seguro?»

- **Del autor (2026-10-07, tras F5.8c):** «que si me da gana pueda declararme en quiebra, eso sí, que haya un mensaje que pregunte si estoy seguro». Eligió: en tu turno, siempre (antes o después de tirar y en una deuda, aunque aún alcance hipotecando); si debes a un jugador, todo a él; si no, al Banco. El pedido de no mostrar «Comprar» sin dinero lo retiró («déjalo como está»).
- **Decisión:** `declareBankruptcy` vale en `Debt` (el primer deudor) y en `Roll`, `Buy`, `EndOfTurn` y `TaxChoice` (quien juega, sin deuda: ante el Banco); ya no exige que ni vendiendo ni hipotecando alcance (R-34 lo pedía: el reglamento queda en su ficha, el motor sigue al autor). Ante un jugador, lo de R-34; ante el Banco, lo de R-35 (casas al Banco, subasta de lo demás, el dinero se pierde). En una subasta no vale. Se quita `raisable`, que solo servía a ese chequeo. En la app: «Declararme en quiebra» al final de «Mis propiedades» y el botón de la deuda; ambos abren «¿Seguro que X se declara en quiebra?» con a quién va lo suyo, «Sí, me declaro en quiebra» y «No, sigo jugando». Extra de prueba `seguro` (true). La máquina y el simulador solo quiebran cuando ya no pueden vender ni hipotecar (la quiebra va última en su lista).
- **Medido:** `BankruptcyTest`, 3 pruebas D-56 (en su turno al Banco con subasta; en deuda al acreedor aunque alcance; con dos, termina, y en subasta no vale). Motor 187 pruebas en verde. Capturas: `capturas/d56_quiebra.png` (deuda) y `capturas/d56_seguro.png` (las dos confirmaciones).
- **Cómo se revierte:** `declareBankruptcy` vuelve a pedir `Debt` y el chequeo de lo que se puede juntar; se quitan el botón de la hoja y `ConfirmBankruptcy`.
- **Estado:** vigente.

### D-57 · Menú: la máquina se llama «Botty N», de 2 a 4 jugadores y abre en 2

- **Del autor (2026-10-08):** «si se selecciona el robot, haz que los nombres sean Botty 1, Botty 2, etc.; la cantidad máxima de jugadores debe ser 4; por defecto debe ser 2 jugadores, no 3».
- **Decisión:** `playerNames` recibe los puestos de la máquina y los nombra `Botty k` (k = 1, 2… en el orden de la mesa), sin importar lo escrito; en el menú, con el robot, el campo muestra ese nombre y no se escribe (lo escrito vuelve al quitar el robot). `MAX_PLAYERS = 4` y `DEFAULT_PLAYERS = 2` en `NewGameForm.kt`; el extra `jugadores` también se limita a 4 y el extra `maquina` usa los mismos nombres. El límite es solo de la app: el motor sigue aceptando de 2 a 6 (D-05) y sus pruebas no cambian. Texto en `menu_bot_name` («Botty %1$d»). Al aprobar, el autor pidió dos puntos en la tirada inicial («Para empezar: Ana: 3, Botty 1: 6…», `EventText.kt`), para que el número del nombre no se pegue al del dado.
- **Medido:** `NewGameFormTest`, 2 pruebas D-57; app 78 pruebas en verde, motor 187. Capturas `capturas/d57_botty.png` (menú con 2 · 3 · 4 y el 2 elegido; partida con Botty 1 y Botty 2) y `capturas/d57_botty_tirada.png` (con los dos puntos).
- **Cómo se revierte:** `MAX_PLAYERS = 6`, `DEFAULT_PLAYERS = 3` y `playerNames` sin `bots`.
- **Estado:** vigente (aprobada, 2026-10-08).

### D-58 · Fase FC: fichas que caminan y jugadores por su ícono, antes de F5.5

- **Del autor (2026-10-08):** «lo que sigue es añadir animaciones a la pieza que se mueva, es muy difícil de seguir el juego cuando las piezas solo se teletransportan»; «que se muestren solo los íconos de los jugadores; si doy click en ellos puedo ver su nombre, su dinero, un botón de "propiedades" donde pueda ver las propiedades del jugador»; «cuando doy click […] debería ver marcadas en él las propiedades del jugador al que estoy seleccionando, lo mismo aplica para mí, por ende el botón de "Mis propiedades" desaparecería. Anota esto al plan.»
- **Decisión:** fase con letra FC (D-27) entre F4 y F5, así es la siguiente; F5.5 espera. Tres tareas: FC.1 animación (2-3 formas grabadas antes), FC.2 maquetas del panel de íconos y su tarjeta, FC.3 la tarjeta, la hoja de cualquier jugador y las casillas marcadas en el tablero; «Mis propiedades» se quita y lo que tenía (construir, hipotecar, quiebra de D-56) pasa a la hoja propia. Se leyó «el logo» como el ícono del jugador.
- **Cómo se revierte:** se quita la sección FC del plan y F5.5 vuelve a ser la siguiente.
- **Estado:** vigente.

### D-59 · FC.1: la ficha camina con un saltito por casilla (maqueta A)

- **Del autor (2026-10-08):** de 3 maquetas grabadas en el Redmi (`capturas/fc1_A.mp4`, `fc1_B.mp4`, `fc1_C.mp4`: A saltito, B deslizarse, C casilla por casilla sin pasar por en medio), «quiero el saltito».
- **Decisión:** `game/Walk.kt` saca de los eventos de cada jugada los recorridos (`walks`, puro y probado): un `Moved` avanza casilla por casilla (o retrocede si la carta era de retroceder, D-14) y un `SentToJail` salta derecho a la Cárcel (`jump`, 500 ms). Saltito de 180 ms por casilla (`HOP_MS`), y un recorrido no pasa de 2,7 s (`WALK_MAX_MS`: una carta que da la vuelta va más rápido). El `GameViewModel` guarda la cola (`walking`); `GameScreen` anima el primero (seno: sube y baja; la ficha crece un poco en el aire) y, mientras camina, no abre «Lo que pasó» ni las decisiones, y la máquina espera a que llegue. Quien tiene un recorrido en cola espera en la casilla de donde sale. Vale también en la partida enlazada (las jugadas del otro lado pasan por `show`). El motor no cambia.
- **Medido:** `WalkTest`, 4 pruebas D-59; app 82 en verde, motor 187. En el Redmi (partida Botty 1 contra Botty 2, semilla 7, `capturas/fc1_partida.mp4`), el log: 184-191 ms por salto con los dados, una carta de 38 saltos en 2748 ms (72 ms por salto) y el salto a la Cárcel en 527 ms.
- **Cómo se revierte:** `walking` vacío (sin `walks` en `show`) y `Board` sin `hops`.
- **Estado:** vigente.

### D-60 · FC.2: íconos en fila arriba con el dinero, que se oculta con una pulsación larga; la tarjeta va en el centro

- **Del autor (2026-10-08):** vio dos vueltas de maquetas en el Redmi (`capturas/fc2_maquetas.png`: fila y columna, con y sin dinero, tarjeta en diálogo; `capturas/fc2_maquetas2.png`: fila pegada arriba, dos a cada lado, tarjeta en el centro). Eligió: «fusiona A y B: el programa arranca mostrando, como en B, pero si mantengo presionado en alguno de los íconos se oculta el dinero; si mantengo presionado cuando está oculto, se muestra». Para la tarjeta, «tarjeta en el centro» (E, sin oscurecer el tablero).
- **Decisión:** en la partida, los íconos de los jugadores van en fila arriba del centro del tablero, pegados a la fila de casillas de arriba (46 dp; el de quien juega, 15 % más grande, con fondo y aro); debajo de cada uno, su dinero. Una pulsación larga en cualquier ícono oculta o muestra el dinero de todos (se lee así: «se oculta el dinero»; si el autor lo quiere por jugador, se cambia en FC.3). Un toque abre la tarjeta de ese jugador en el centro, en lugar del turno: medallón, nombre, dinero, «Propiedades (n)» y «Cerrar»; mientras está abierta, sus casillas llevan un marco de su color (4 dp). Sin nombres a la vista en el panel. Se aplica en FC.3.
- **Cómo se revierte:** vuelve `PlayersPanel` (nombre y dinero en lista) dentro de la calcomanía del turno.
- **Estado:** vigente.

### D-61 · FC.3: la tarjeta reemplaza al turno; la hoja es de cualquiera y solo la propia, en tu turno, tiene jugadas

- **Contexto:** D-60 fijó la fila, la tarjeta y el marco; faltaba cómo convive con el turno y con «Mis propiedades».
- **Decisión:** un toque en un ícono abre su tarjeta en el centro en lugar del turno (sin «Tirar» a la vista hasta «Cerrar»); tocar el mismo ícono la cierra. «Propiedades (n)» abre la hoja de ese jugador; lleva jugadas y «Declararme en quiebra» solo si `canManage` (es quien juega, su turno es de este teléfono y la fase es tirar o terminar, como antes en «Mis propiedades»); si no, solo se mira. El dinero oculto (pulsación larga) vale para todos y se recuerda al girar la pantalla. Se quitan «Mis propiedades» y `PlayersPanel` (la demo `n` usa `PlayersRow`). Extra `tarjeta=k` abre la tarjeta de k sin el aviso «Lo que pasó»; con `hoja=true`, la hoja de k. La muestra `propiedades` le da además 11, 13 y 15 al siguiente jugador.
- **Cómo se revierte:** `Center` vuelve a llevar «Mis propiedades» y la hoja vuelve a ser solo de quien juega.
- **Estado:** vigente.

### D-62 · Las casillas del jugador elegido respiran con una máscara de su color

- **Del autor (2026-10-08, al aprobar FC.3):** «que las casillas parpadearan con el color correspondiente … una máscara de un tono traslucido del color del jugador y un efecto de breathing (lento) … se agrandan y vuelven al tamaño original para distinguir mejor».
- **Decisión:** mientras la tarjeta está abierta, cada casilla del jugador se dibuja otra vez encima del tablero (para que la vecina no la tape al crecer), con su marco de 4 dp (D-60) y una máscara de su color que va del 20 % al 40 % de opacidad, y crece hasta un 12 % (`BREATH_SCALE`) en 1400 ms (`BREATH_MS`) y vuelve en otros tantos, con aceleración suave. Cifras elegidas por el agente; las ajusta el autor al verlo.
- **Cómo se revierte:** se quita el bloque `marks` de `Board` y vuelve el marco quieto de D-60.
- **Estado:** vigente.
