# Interfaz (fases F3 y F4)

*Se lee en F3 y F4. Compose con Material 3. Reglas 4 y 7 del mandato.*

## 1. Antes de una pantalla nueva: 2-3 opciones

Un pedido de diseño abierto (tablero, panel, editor) se resuelve con 2-3 opciones **vistas en el teléfono** (o en el emulador) antes de programar la definitiva: una maqueta mínima por opción (un `@Composable` con datos falsos), captura de cada una y AskUserQuestion con la más barata incluida. La elección va a una D-## (L11, L18).

## 2. Después de cambiar una pantalla: captura

- `python3 .claude/skills/agente-mono/scripts/telefono.py instalar` y después `telefono.py captura --salida <scratchpad>`; se lee la captura antes de decir que algo se ve bien.
- Lo que juzga el autor (si se ve bien, si se entiende) no lo declaras tú: le muestras la captura (SendUserFile si está disponible) y preguntas.
- Si algo falla en el teléfono: `telefono.py log -n 60` (solo la etiqueta `Mono` y los errores fatales), nunca el logcat completo.
- Sin el Redmi conectado: `telefono.py emulador` (AVD `Medium_Phone`) y se espera a que `telefono.py dispositivos` lo liste.

## 3. Reglas de la interfaz

- La interfaz no decide reglas: llama al motor con una acción y dibuja el estado y los eventos que devuelve. Si la interfaz necesita algo que el motor no da, primero va al motor con su prueba (regla 4).
- Tablero en anillo para cualquier N (D-05): la geometría (posición de cada casilla en el borde) es una función pura con su prueba en `app/src/test` (N = 20, 40, 60).
- Pantalla del Redmi: se mide con `adb shell wm size` y `wm density` en F0.4 y se anota en ESTADO; todo se ve en vertical, sin girar el teléfono. Textos en `strings.xml`.
- Un ViewModel por pantalla que guarda el estado del motor; el guardado de partidas y tableros (F3.6, F4.4) en archivos JSON de la app.

## 4. El hito (F4.5)

Se prepara con el autor: qué tablero edita, cuántos juegan y cuánto dura (una partida corta con poco dinero inicial es válida si es una opción del editor). Lo aprueba él con AskUserQuestion; no se marca `[x]` sin eso. Nada de F5 antes.
