# Interfaz (fases F3, F4 y F5)

*Se lee en F3, F4 y en las pantallas de F5. Compose con Material 3. Reglas 4 y 7 del mandato.*

## 1. Antes de una pantalla nueva: 2-3 opciones

Un pedido de diseño abierto (tablero, panel, editor) se resuelve con 2-3 opciones **vistas en el teléfono** (o en el emulador) antes de programar la definitiva: una maqueta mínima por opción (un `@Composable` con datos falsos), captura de cada una y AskUserQuestion con la más barata incluida. La elección va a una D-## (L11, L18). Una pantalla de prueba que solo abre un extra (p. ej. `--es enlace eco`) no pide opciones, pero sí su captura (M-078). Si el pedido trae una palabra con dos lecturas (p. ej. «horizontal y vertical»: ¿la orientación de la lista o su lugar alrededor del tablero?), cada lectura lleva su maqueta en la misma captura, o se pregunta antes de dibujar (FC.2, M-101).

Las maquetas van siempre en `app/src/main/kotlin/com/jacck/mono/demo/Maquetas.kt` (`@Composable fun Maqueta(letra: String)`, extra `maqueta` de `MainActivity`), con su commit: la pantalla siguiente sobrescribe ese archivo con Write (si después de leerlo lo cambiaste con Bash, antes un `Read` con `limit: 5`: Write lo exige, M-100) y, tras la elección, queda el esqueleto vacío. Así no hay archivos que el autor tenga que borrar (M-029).

La maqueta cabe en una pantalla (adb no desliza, D-23); si no, `maqueta=<paso><opción>` (p. ej. `FB.4c`) pone esa opción arriba y salen juntas con `pantallas maqueta=FB.4 maqueta=FB.4c`. Datos falsos que cumplen los `require` de lo que dibujan (índices válidos): si no, la app se cierra y se gasta una instalación (M-055). Un valor de extra no lleva comas (`pantallas` separa por comas): listas con `+` (`quitar=13+14`, M-068).

## 2. Después de cambiar una pantalla: captura

- `python3 .claude/skills/agente-mono/scripts/telefono.py instalar` y después `telefono.py captura --salida <scratchpad>`; se lee la captura antes de decir que algo se ve bien.
- **Lo que tiene variantes, una captura de cada una** (M-120): casillas (propiedad, estación, impuesto, carta, esquina: `casilla=1 casilla=5 casilla=4 casilla=2 casilla=0` en el Clásico) y jugadores (propio en su turno, otro, en quiebra).
- Lo que juzga el autor (si se ve bien, si se entiende) no lo declaras tú: le muestras la captura (SendUserFile si está disponible) y preguntas.
- **Lo que va al autor sale del APK instalado:** si cambias algo visible después de capturar (un texto, un color), reinstala y recaptura esa pantalla antes de enviarla; el pie de SendUserFile dice solo lo que se ve en la imagen (M-049).
- **Abrir con un extra** (maqueta o prueba): `telefono.py adb -- shell am start -S -n com.jacck.mono/.MainActivity --es maqueta A` (`-S` cierra antes la app; `--ez`/`--ei` para booleanos o enteros, `--el semilla 1`: es Long y con `--ei` se ignora; `pantallas semilla=1` ya lo hace bien). Varias pantallas en una orden: `telefono.py pantallas - maqueta=A fase=compra propiedades=true,hoja=true --salida capturas/<paso>.png` instala, abre cada una (`-` = sin extras, el menú), espera 6 s, la primera 12 s (con menos, un diálogo sale a medio aparecer y la primera tras instalar, en blanco: M-080), captura y las une; se lee esa sola imagen y va al autor con SendUserFile (M-035, M-048). No se escribe código de Pillow para unir capturas. Los extras de partida abren con el aviso «Lo que pasó» encima del tablero (M-046). adb no toca, no desliza ni manda teclas en el Redmi (`input tap`, `input keyevent`, también «atrás» → `INJECT_EVENTS`, `redmi.md`; M-103): un diálogo que solo sale con un toque se abre con un extra de prueba que lo deja abierto (`hoja`, `fase`, `seguro`), no con taps (M-096). Un gesto que adb no hace (deslizar, pulsación larga) deja un `Log.i` con lo que hizo y sus ms: el log prueba la prueba del autor (M-121).
- **El autor puede tener el Redmi en la mano** (sobre todo tras pedir un cambio): antes de `instalar`, `pantallas`, `grabar` o un `am start`, una línea («voy a usar el Redmi unos N s: no lo toques»). Una partida abierta con extras no se guarda (solo la primera, D-63): no se le deja al autor para jugar (M-104).
- **Animación o video para el autor:** antes, `grabar.md` (grabar, extras que esperan a los dados, fotogramas de un tramo; M-110).
- Lo que va bajo la fila de íconos se mira también durante un pago: `grabar.md` (M-117).
- Si algo falla en el teléfono: `telefono.py log -n 60` (solo la etiqueta `Mono` y los errores fatales), nunca el logcat completo.

### Pruebas en el Redmi

Prueba del autor en el Redmi (o el teléfono no responde): `redmi.md`.

## 3. Reglas de la interfaz

- La API del motor y los componentes de la app (`Chiva.kt`, `board/BoardView.kt`) se leen por firmas, no enteros: `grep -nE -A3 '^\s*(fun|data class|class|sealed interface|data object|object) ' <Archivo>.kt` (M-024, M-067).
- Código de la app en `app/src/main/kotlin/com/jacck/mono/` (`board/`, `game/`, `demo/`), pruebas en `app/src/test/kotlin/…`; no hay `java/` (M-030).
- **Qué acción vale y cuánto cuesta:** `Engine.tryApply(config, state, action)` (null si no vale) y la cifra de su evento, como `propertyMoves` (D-24); para habilitar botones no se leen los cuerpos de las reglas del motor (M-027).
- **Botones con dinero:** verbo + cifra con el signo del dinero (− pagas, + recibes): `Construir −$50`, `Vender +$25`; nunca el signo pegado a un ícono («+🏠 $50» se leyó como cobrar; el autor, 2026-10-06; M-026).
- **Lo que la app escribe fuera de pantalla** (archivos; después Bluetooth) deja un `Log.i` desde el primer commit, y antes de pedir la prueba se comprueba: `telefono.py adb -- shell run-as com.jacck.mono ls -la files` (M-032). La evidencia de una prueba del autor sale primero de un archivo (`partida.json`): el búfer `main` del Redmi guarda ~35 s si `instalar` no lo agrandó a 4 MiB (lo dice su salida, M-066).
- **Pantalla que sale del menú y vuelve** (editores de F4): el menú va dentro de `rememberSaveableStateHolder().SaveableStateProvider("menu")`; sin eso, `rememberSaveable` pierde nombres y personajes al volver (F4.1, D-39; M-061).
- La interfaz no decide reglas: llama al motor con una acción y dibuja el estado y los eventos que devuelve. Si la interfaz necesita algo que el motor no da, primero va al motor con su prueba (regla 4).
- Tablero en anillo para cualquier N (D-05): la geometría (posición de cada casilla en el borde) es una función pura con su prueba en `app/src/test` (N = 16, 40 y 48: extremos de D-09 y el Clásico; M-021).
- Todo se ve en vertical, sin girar el teléfono.
- **Textos nuevos en `strings.xml`:** prefijo propio de la pantalla, comprobado antes con `grep -c 'name="<prefijo>_' app/src/main/res/values/strings.xml` (los `rule_*` son del validador); un texto que explica una regla se escribe desde la línea **Regla:** de su ficha R-##, no de memoria ni solo del KDoc (M-063).
- Un ViewModel por pantalla que guarda el estado del motor; el guardado de partidas y tableros (F3.6, F4.4) en archivos JSON de la app.
- **Ícono de la app, letra o imagen de terceros:** `arte.md` §4.
- El nombre de una prueba entre comillas invertidas no admite `. ; [ ] / < > : \` (en la JVM no compila): separa con comas o guiones (M-102).

