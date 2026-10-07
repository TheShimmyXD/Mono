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
- **Elección:** pendiente del autor.
- **Cómo se revierte:** quitar la sección «personajes» de `arte.py` y correrlo; `Maquetas.kt` vuelve a `dab05d0`.
- **Estado:** propuesta.
