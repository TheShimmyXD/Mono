# Grabar una animación en el Redmi

*Se lee antes de escribir un extra `fase` o de `telefono.py grabar` o `fotogramas`: una animación nueva (recorridos, vuelos, pagos) o un video para el autor (M-110, M-116).*

## 1. Grabar

- `telefono.py grabar <extras> --segundos N --salida capturas/<paso>.mp4` instala, empieza a grabar, abre la app 1 s después (así sale la primera jugada, M-111), y deja al lado `<paso>_hoja.png` (hasta 24 fotogramas); se lee la hoja antes de enviar el `.mp4`. La velocidad se mide en el log, no en el video (D-59).
- Los extras van en un argumento con comas (`jugadores=2,semilla=7,maquina=0-1`); listas con `-` o `+`. Si el autor toca algo mientras graba: pregunta cerrada («¿Listo? Al responder abro la app y grabo N s»), con los gestos en orden y cuánto dura, y `grabar` justo tras su respuesta, con `--segundos` ≥ 60. Con un aviso en el chat, el video sale vacío (M-108, M-119).
- Un video de más de 30 MiB no sube a SendUserFile: va su `<paso>_envio.mp4` (M-107). `ffmpeg` sale de `[herramientas]` en `mono.toml`; sin guiones de `screenrecord`, ffmpeg ni Pillow sueltos (M-099).
- Si el autor jugó hace menos de 3 min (su partida escribe «guardada: turno» en el log), `instalar`, `pantallas`, `grabar` y `cartas` paran con un AVISO: se le pregunta antes y solo con su permiso se repite con `--ya` (M-112).

## 2. Que pase lo que se quiere grabar, sin tocar

- **La máquina juega sola** (`maquina=0-1`); con `pausa=<ms>` espera eso antes de cada jugada, para que se vea lo que hay en pantalla antes (FD.2, D-66).
- **Lo que depende de los dados:** un valor de `fase` que deja la ficha a la distancia de los próximos dados de la casilla buscada (el azar del motor es puro: `random.rollDice()` no lo gasta; `landingOn` en `demo/SamplePhases.kt`), con su prueba en `SamplePhasesTest` para los dos presets. Hoy: `alquiler` (cae en una propiedad de otro) y `carta` (carta «cobra a cada jugador» encima del mazo).
- `oculto=true` abre con el dinero oculto (la pulsación larga que adb no hace).

## 3. Ver una animación corta

- `telefono.py fotogramas <video> --desde S --segundos N [--fps 8] --salida <png>`, con S = hora del log − la de inicio que imprime `grabar` (M-108). `--segundos` admite decimales.
- Para mirar un detalle (la fila de íconos, un billete): `--alto 0.3` deja la franja de arriba de cada fotograma y `--columnas 2` los hace 4 veces más grandes; la hoja sigue en 2160 px (M-113).
