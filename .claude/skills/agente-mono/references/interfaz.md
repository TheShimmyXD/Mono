# Interfaz (fases F3, F4 y F5)

*Se lee en F3, F4 y en las pantallas de F5. Compose con Material 3. Reglas 4 y 7 del mandato.*

## 1. Antes de una pantalla nueva: 2-3 opciones

Un pedido de diseño abierto (tablero, panel, editor) se resuelve con 2-3 opciones **vistas en el teléfono** (o en el emulador) antes de programar la definitiva: una maqueta mínima por opción (un `@Composable` con datos falsos), captura de cada una y AskUserQuestion con la más barata incluida. La elección va a una D-## (L11, L18). Una pantalla de prueba que solo abre un extra (p. ej. `--es enlace eco`) no pide opciones, pero sí su captura (M-078). Si el pedido trae una palabra con dos lecturas (p. ej. «horizontal y vertical»: ¿la orientación de la lista o su lugar alrededor del tablero?), cada lectura lleva su maqueta en la misma captura, o se pregunta antes de dibujar (FC.2, M-101).

Las maquetas van siempre en `app/src/main/kotlin/com/jacck/mono/demo/Maquetas.kt` (`@Composable fun Maqueta(letra: String)`, extra `maqueta` de `MainActivity`), con su commit: la pantalla siguiente sobrescribe ese archivo con Write (si después de leerlo lo cambiaste con Bash, antes un `Read` con `limit: 5`: Write lo exige, M-100) y, tras la elección, queda el esqueleto vacío. Así no hay archivos que el autor tenga que borrar (M-029).

La maqueta cabe en una pantalla (adb no desliza, D-23); si no, `maqueta=<paso><opción>` (p. ej. `FB.4c`) pone esa opción arriba y salen juntas con `pantallas maqueta=FB.4 maqueta=FB.4c`. Datos falsos que cumplen los `require` de lo que dibujan (índices válidos): si no, la app se cierra y se gasta una instalación (M-055). Un valor de extra no lleva comas (`pantallas` separa por comas): listas con `+` (`quitar=13+14`, M-068).

## 2. Después de cambiar una pantalla: captura

- `python3 .claude/skills/agente-mono/scripts/telefono.py instalar` y después `telefono.py captura --salida <scratchpad>`; se lee la captura antes de decir que algo se ve bien.
- Lo que juzga el autor (si se ve bien, si se entiende) no lo declaras tú: le muestras la captura (SendUserFile si está disponible) y preguntas.
- **Lo que va al autor sale del APK instalado:** si cambias algo visible después de capturar (un texto, un color), reinstala y recaptura esa pantalla antes de enviarla; el pie de SendUserFile dice solo lo que se ve en la imagen (M-049).
- **Abrir con un extra** (maqueta o prueba): `telefono.py adb -- shell am start -S -n com.jacck.mono/.MainActivity --es maqueta A` (`-S` cierra antes la app; `--ez`/`--ei` para booleanos o enteros). Varias pantallas en una orden: `telefono.py pantallas - maqueta=A fase=compra propiedades=true,hoja=true --salida capturas/<paso>.png` instala, abre cada una (`-` = sin extras, el menú), espera 6 s, la primera 12 s (con menos, un diálogo sale a medio aparecer y la primera tras instalar, en blanco: M-080), captura y las une; se lee esa sola imagen y va al autor con SendUserFile (M-035, M-048). No se escribe código de Pillow para unir capturas. Los extras de partida abren con el aviso «Lo que pasó» encima del tablero (M-046). adb no toca ni desliza en el Redmi (`input tap` → `INJECT_EVENTS`, `redmi.md`): un diálogo que solo sale con un toque se abre con un extra de prueba que lo deja abierto (`hoja`, `fase`, `seguro`), no con taps (M-096).
- **Animación:** `telefono.py grabar <extras> --segundos N --salida capturas/<paso>.mp4` instala, abre, graba y deja al lado `<paso>_hoja.png` (hasta 24 fotogramas); se lee la hoja antes de enviar el `.mp4`. La velocidad se mide en el log, no en el video (D-59). Sin guiones de `screenrecord` ni Pillow sueltos (M-099).
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

