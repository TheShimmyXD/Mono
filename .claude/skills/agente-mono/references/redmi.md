# Redmi: pruebas del autor y problemas

*Se lee antes de una prueba del autor en el Redmi, o si `telefono.py dispositivos`, `instalar` o `captura` no responden como se espera (M-056, M-070).*

## 1. Pruebas del autor

- **adb no puede tocar la pantalla:** `input tap` da `INJECT_EVENTS` (haría falta «Depuración USB (ajustes de seguridad)»). Las partidas de prueba las juega el autor; tú instalas, le dices hasta dónde jugar y capturas (D-23, M-023). Antes de automatizar algo por adb, prueba un solo paso a mano y lee su stderr.
- **Estado avanzado sin tocar:** para capturar una pantalla que necesita media partida, un extra de prueba en `MainActivity` que prepara el estado o abre la pantalla (`propiedades`, `hoja`; D-24), anotado en su KDoc y en ESTADO (M-028).
- **`instalar` cierra la app, pero `files/partida.json` sigue:** «Seguir la partida» abre la de antes de instalar. La prueba del autor pide «Partida nueva» con los toques desde el menú, y la evidencia compara la hora de `partida.json` con la de la instalación (M-058). La pregunta del resultado trae los fallos probables como opciones («no salió X», «se cerró», «salió distinto»; M-033). Si no cuadra, antes de buscar el fallo: la hora de su respuesta (¿alcanzó a hacerlo?) y el log desde `MainActivity creada`; `--------- beginning of main` lo imprime logcat siempre, no es rotación (M-031).
- **Un *Terminado* con prueba del autor:** antes, el comando que la comprueba va a ESTADO (p. ej. `telefono.py adb -- shell run-as com.jacck.mono cat files/partida.json | grep token`); busca sin distinguir mayúsculas (`grep -i`) y, si puede, por lo que el autor no teclea (campo, índice), no por el texto exacto que le pediste escribir (M-060). La primera pregunta es «¿Ya la jugaste?» (Ya / Después), no la aprobación; con «Ya», se corre ese comando y solo entonces Aprobar / Con un cambio / Todavía no. Sin evidencia, sin `[x]` (M-052).
- **El guion habla con lo que ve el autor:** casillas por su nombre (el editor numera «Casilla N» = índice + 1), botones por su texto; nunca índices internos. Si la prueba tiene preparación (editar un tablero) y luego algo largo (jugar), se comprueba la preparación en su archivo antes de lo largo: `run-as com.jacck.mono cat files/tableros/t<n>.json`, comparado con el preset por nombre y campo a campo (las cartas repiten nombre; F4.5, M-071).
- **Un error del log se fecha** (`telefono.py log` lleva la hora en cada línea) antes de contárselo al autor: el búfer de 4 MiB guarda horas, también cierres de compilaciones viejas (M-072).

## 2. Puesta a punto (F0.4, 2026-10-06)

Si `telefono.py dispositivos` da una pista en vez de la serie, el autor lo arregla así (una vez por equipo y teléfono):
- `no permissions`: regla udev con sudo, en su terminal: `/etc/udev/rules.d/51-android.rules` con `SUBSYSTEM=="usb", ATTR{idVendor}=="18d1", MODE="0666"` y lo mismo con `2717` (Xiaomi); después `sudo udevadm control --reload-rules && sudo udevadm trigger`, `adb kill-server` y reconectar el cable.
- `unauthorized`: aceptar «¿Permitir depuración USB?» en el teléfono.
- Pantalla apagada (HyperOS la apaga a los ~10 min y adb no puede encenderla): `captura` lo detecta y no guarda nada; se pide al autor que desbloquee el Redmi (M-020).
- `INSTALL_FAILED_USER_RESTRICTED`: HyperOS pide «Instalar vía USB» en Opciones de desarrollador y tocar Instalar en el teléfono a tiempo.
- **Sin `svc power stayon`** (el autor, 2026-10-06): pide el desbloqueo justo antes y agrupa `instalar` + capturas en la misma llamada.
- Sin el Redmi conectado: `telefono.py emulador` (AVD `Medium_Phone`) y se espera a que `telefono.py dispositivos` lo liste.
