# Gradle y motor (fases F0 y F2)

*Se lee en F0 y F2. El autor conoce poco Kotlin y Android (P5): cada decisión de Kotlin, Gradle o Android se explica en una línea al tomarla.*

## 1. Entorno (H5)

- `java` y `adb` no están en el PATH. Gradle usa `JAVA_HOME` del JBR de Android Studio (`[cierre] entorno` de `mono.toml`); `cierre_paso.py` y `telefono.py` ya lo ponen. A mano: `JAVA_HOME=~/android-studio/jbr ./gradlew ...` desde la raíz.
- Gradle: siempre `--console=plain -q` y la salida acotada (`| tail -20`). El primer `./gradlew` descarga Gradle y dependencias: puede tardar minutos (`timeout: 600000`); si falla por red, se dice al autor y se espera.
- Versiones en `gradle/libs.versions.toml` (catálogo); D-## con AGP, Kotlin, Compose BOM, `minSdk` y `targetSdk` en F0.2. Instalado: SDK 37 (platforms), build-tools 36, Android Studio 2026.2.1.
- Si crear el proyecto a mano da problemas de versiones, la alternativa barata es que el autor lo cree en Android Studio («Empty Activity», Kotlin, Compose) en la raíz y el agente añade `engine` (ofrecerla con AskUserQuestion).

## 2. Capas (D-02)

`engine/` es un módulo `kotlin("jvm")`: configuración, estado, acciones y reglas. No importa `android.*`, `androidx.*` ni nada de la interfaz; no lee archivos ni la hora; no usa `Random` sin semilla. `app/` (Android) lo usa; el guardado y el Bluetooth viven en `app/`.

## 3. Motor determinista (D-03)

- Estado inmutable (`data class` con `val` y listas inmutables) + `fun apply(state, action): Result` que devuelve el estado nuevo y los eventos (lo que la interfaz anima o anuncia).
- El azar entra con una semilla: dados y barajado salen de un generador guardado en el estado, así una partida guardada o enviada por Bluetooth sigue igual.
- Serialización con `kotlinx.serialization` (JSON) para tableros, presets y partidas.
- Ningún número del juego (precios, salario, multas) en el código: vienen de la configuración, y la configuración de un preset que cita su R-## (F2.8).

## 4. Pruebas

- JUnit en `engine/src/test/kotlin`, una clase por tema y **una prueba por R-##**, con el ID en el nombre: ``fun `R-07 cobra el salario al pasar por la salida`()``.
- Escenario pequeño y explícito: tablero de pocas casillas, dados fijados (generador falso o semilla con resultado conocido), lo que se espera escrito con números.
- La prueba compara con lo que dice la ficha R-## (la fuente), no con lo que devuelve otra función del motor.
- Invariantes en F2.9: el dinero total (jugadores + banco) se conserva; nadie queda con dinero negativo sin estar en quiebra; la partida termina o llega al tope de turnos. Tiempo de las 1000 partidas medido y anotado en ESTADO.
- Antes de programar una tarea, sus fichas en una tanda: `for r in 19 20 21; do awk -v id="### R-$r " 'index($0,id)==1{p=1;print;next} p&&/^##/{p=0} p' REGLAS.md; done`.
- Antes del punto de control de una tarea del motor, cruza los R-## de sus fichas con `grep -rhoE 'fun `R-[0-9]+' engine/src/test | sort -u`; la que falte, su prueba (también la regla que solo vive en una opción del modelo).
- `cierre_paso.py` corre `:engine:test` y `:app:assembleDebug`; el resumen dice cuántas pruebas pasaron (`engine/build/test-results`, sin abrir `build/` entero: `grep -h -o 'tests="[0-9]*"' engine/build/test-results/test/*.xml`).
- **Pregunta de balance o duración** (dinero inicial, tamaño, jugadores): antes de preguntar al autor, mídela con `simulate` en una prueba temporal `engine/src/test/kotlin/com/jacck/mono/engine/TmpMedidaTest.kt` (40 semillas, `println` con un prefijo que se filtra con `grep`), creada y borrada en la misma orden, que termina con `git status --short` (excepción a los scripts en el scratchpad: tiene que compilar con el motor). Las cifras van a la pregunta y a su D-## (D-43, M-073).
- **Partidas automáticas enteras en una prueba** (`TerminalTest`, enlace): sin tratos el Clásico no termina (más de 20 000 acciones, D-50); usa $300 al empezar, salario $0 y un tope ≤ 5000 acciones, y corre primero una semilla con el tiempo medido antes del bucle (M-085).

- **Tarea grande** (más de ~6 R-##): pártela en sub-pasos (a, b, c), cada uno con `cierre_paso.py` y su commit; la casilla se marca con el último; ESTADO dice solo qué sub-pasos faltan (hashes y capturas, a SESIONES). Entre llamadas el árbol compila: el modelo nuevo va en la misma tanda que el código que lo usa. (M-088)
- El nombre de una prueba entre comillas invertidas no admite `. ; [ ] / < > : \` (en la JVM no compila): separa con comas o guiones (M-102).

## 5. Estilo

- Identificadores en inglés; comentarios y KDoc en español, cortos (la convención de `python-programmer`, reglas 1 y 10, llevada a Kotlin; Azorian A13).
- Archivos ≤ 400 líneas (dividir antes de 600). Textos que ve el jugador, en español, en `app/src/main/res/values/strings.xml`, nunca en `engine` (el motor devuelve eventos, no frases).
- KDoc de una regla: qué hace y su R-##: `/** Cobra el salario al pasar por la salida (R-07). */`.
