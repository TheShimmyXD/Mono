# Interfaz (fases F3 y F4)

*Se lee en F3 y F4. Compose con Material 3. Reglas 4 y 7 del mandato.*

## 1. Antes de una pantalla nueva: 2-3 opciones

Un pedido de diseño abierto (tablero, panel, editor) se resuelve con 2-3 opciones **vistas en el teléfono** (o en el emulador) antes de programar la definitiva: una maqueta mínima por opción (un `@Composable` con datos falsos), captura de cada una y AskUserQuestion con la más barata incluida. La elección va a una D-## (L11, L18).

Las maquetas van siempre en `app/src/main/kotlin/com/jacck/mono/demo/Maquetas.kt` (`@Composable fun Maqueta(letra: String)`, extra `maqueta` de `MainActivity`), con su commit: la pantalla siguiente sobrescribe ese archivo con Write y, tras la elección, queda el esqueleto vacío. Así no hay archivos que el autor tenga que borrar (M-029).

La maqueta cabe en una pantalla (adb no desliza, D-23); si no, `maqueta=<paso><opción>` (p. ej. `FB.4c`) pone esa opción arriba y salen juntas con `pantallas maqueta=FB.4 maqueta=FB.4c`. Datos falsos con índices válidos: un −1 cierra la app y gasta una instalación (M-055).

## 2. Después de cambiar una pantalla: captura

- `python3 .claude/skills/agente-mono/scripts/telefono.py instalar` y después `telefono.py captura --salida <scratchpad>`; se lee la captura antes de decir que algo se ve bien.
- Lo que juzga el autor (si se ve bien, si se entiende) no lo declaras tú: le muestras la captura (SendUserFile si está disponible) y preguntas.
- **Lo que va al autor sale del APK instalado:** si cambias algo visible después de capturar (un texto, un color), reinstala y recaptura esa pantalla antes de enviarla; el pie de SendUserFile dice solo lo que se ve en la imagen (M-049).
- **Abrir con un extra** (maqueta o prueba): `telefono.py adb -- shell am start -S -n com.jacck.mono/.MainActivity --es maqueta A` (`-S` cierra antes la app; `--ez`/`--ei` para booleanos o enteros). Varias pantallas en una orden: `telefono.py pantallas - maqueta=A fase=compra propiedades=true,hoja=true --salida capturas/<paso>.png` instala, abre cada una (`-` = sin extras, el menú), espera 6 s (con menos, un diálogo sale a medio aparecer), captura y las une; se lee esa sola imagen y va al autor con SendUserFile (M-035, M-048). No se escribe código de Pillow para unir capturas. Los extras de partida abren con el aviso «Lo que pasó» encima del tablero (M-046).
- Si algo falla en el teléfono: `telefono.py log -n 60` (solo la etiqueta `Mono` y los errores fatales), nunca el logcat completo.
- Sin el Redmi conectado: `telefono.py emulador` (AVD `Medium_Phone`) y se espera a que `telefono.py dispositivos` lo liste.

### Redmi listo (F0.4, 2026-10-06)

Si `telefono.py dispositivos` da una pista en vez de la serie, el autor lo arregla así (una vez por equipo y teléfono):
- `no permissions`: regla udev con sudo, en su terminal: `/etc/udev/rules.d/51-android.rules` con `SUBSYSTEM=="usb", ATTR{idVendor}=="18d1", MODE="0666"` y lo mismo con `2717` (Xiaomi); después `sudo udevadm control --reload-rules && sudo udevadm trigger`, `adb kill-server` y reconectar el cable.
- `unauthorized`: aceptar «¿Permitir depuración USB?» en el teléfono.
- Pantalla apagada (HyperOS la apaga a los ~10 min y adb no puede encenderla): `captura` lo detecta y no guarda nada; se pide al autor que desbloquee el Redmi (M-020).
- `INSTALL_FAILED_USER_RESTRICTED`: HyperOS pide «Instalar vía USB» en Opciones de desarrollador y tocar Instalar en el teléfono a tiempo.
- **adb no puede tocar la pantalla:** `input tap` da `INJECT_EVENTS` (haría falta «Depuración USB (ajustes de seguridad)»). Las partidas de prueba las juega el autor; tú instalas, le dices hasta dónde jugar y capturas (D-23, M-023). Antes de automatizar algo por adb, prueba un solo paso a mano y lee su stderr.
- **Estado avanzado sin tocar:** para capturar una pantalla que necesita media partida, un extra de prueba en `MainActivity` que prepara el estado o abre la pantalla (`propiedades`, `hoja`; D-24), anotado en su KDoc y en ESTADO (M-028).
- **`instalar` cierra la partida abierta** (la de un APK anterior no vuelve): la prueba del autor se hace en lo recién instalado, y se le dice. La pregunta del resultado trae los fallos probables como opciones («no salió X», «se cerró», «salió distinto»; M-033). Si no cuadra, antes de buscar el fallo: la hora de su respuesta (¿alcanzó a hacerlo?) y el log desde `MainActivity creada`; `--------- beginning of main` lo imprime logcat siempre, no es rotación (M-031).
- **Un *Terminado* con prueba del autor:** antes, el comando que la comprueba va a ESTADO (p. ej. `telefono.py adb -- shell run-as com.jacck.mono cat files/partida.json | grep token`). La primera pregunta es «¿Ya la jugaste?» (Ya / Después), no la aprobación; con «Ya», se corre ese comando y solo entonces Aprobar / Con un cambio / Todavía no. Sin evidencia, sin `[x]` (M-052).
- **Sin `svc power stayon`** (el autor, 2026-10-06): pide el desbloqueo justo antes y agrupa `instalar` + capturas en la misma llamada.

## 3. Reglas de la interfaz

- La API del motor se lee por firmas, no entera: `grep -nE '^\s*(fun|data class|class|sealed interface|data object|object) ' engine/src/main/kotlin/com/jacck/mono/engine/<Archivo>.kt` (M-024).
- Código de la app en `app/src/main/kotlin/com/jacck/mono/` (`board/`, `game/`, `demo/`), pruebas en `app/src/test/kotlin/…`; no hay `java/` (M-030).
- **Qué acción vale y cuánto cuesta:** `Engine.tryApply(config, state, action)` (null si no vale) y la cifra de su evento, como `propertyMoves` (D-24); para habilitar botones no se leen los cuerpos de las reglas del motor (M-027).
- **Botones con dinero:** verbo + cifra con el signo del dinero (− pagas, + recibes): `Construir −$50`, `Vender +$25`; nunca el signo pegado a un ícono («+🏠 $50» se leyó como cobrar; el autor, 2026-10-06; M-026).
- **Lo que la app escribe fuera de pantalla** (archivos; después Bluetooth) deja un `Log.i` desde el primer commit, y antes de pedir la prueba se comprueba: `telefono.py adb -- shell run-as com.jacck.mono ls -la files` (M-032).
- La interfaz no decide reglas: llama al motor con una acción y dibuja el estado y los eventos que devuelve. Si la interfaz necesita algo que el motor no da, primero va al motor con su prueba (regla 4).
- Tablero en anillo para cualquier N (D-05): la geometría (posición de cada casilla en el borde) es una función pura con su prueba en `app/src/test` (N = 16, 40 y 48: extremos de D-09 y el Clásico; M-021).
- Pantalla del Redmi: se mide con `adb shell wm size` y `wm density` en F0.4 y se anota en ESTADO; todo se ve en vertical, sin girar el teléfono. Textos en `strings.xml`.
- Un ViewModel por pantalla que guarda el estado del motor; el guardado de partidas y tableros (F3.6, F4.4) en archivos JSON de la app.
- **Letra o imagen de terceros:** solo con licencia libre (OFL, CC0, Apache); se baja a una carpeta propia del scratchpad junto con su licencia, se comprueba que tenga tildes, ñ, ¿ y ¡ (`~/.cache/mono-arte/bin/python -I -c` con `ImageFont.truetype(ttf, 40)`: un glifo que falta da los mismos `bytes(f.getmask(ch))` que `'\uE000'`; no hay `fontTools`), la licencia queda en `arte/<tipo>/` y se cita en su D-## (D-33, M-047).

## 4. El hito (F4.5)

Se prepara con el autor: qué tablero edita, cuántos juegan y cuánto dura (una partida corta con poco dinero inicial es válida si es una opción del editor). Lo aprueba él con AskUserQuestion; no se marca `[x]` sin eso. Nada de F5 antes.
