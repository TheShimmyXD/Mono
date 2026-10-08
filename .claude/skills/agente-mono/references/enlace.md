# Enlace entre teléfonos (fase F5)

*Se lee en F5, después de que el autor aprobara el hito «Primera quiebra». Lo que aquí se dice se confirma en la documentación de Android del momento antes de programar (regla 1: nada se supone del sistema).*

## 1. Primero la decisión (F5.1)

Opciones para la D-##, con prueba de concepto de cada una antes de elegir:
- **Bluetooth clásico (RFCOMM)**: lo que pidió el autor; sin servicios de Google; emparejamiento del sistema.
- **Nearby Connections** (Google Play services): usa Bluetooth y wifi; descubre y conecta con menos código; depende de Play services en los dos teléfonos.
La pregunta al autor lleva qué cuesta cada una en tareas y qué ve él al conectar.

## 2. Protocolo (F5.2, antes de tocar el transporte)

- El anfitrión decide: tira los dados con la semilla de la partida, aplica la acción y envía `(número, acción)`; el invitado aplica la misma acción y compara un resumen (hash) del estado.
- El invitado solo propone acciones en su turno; el anfitrión las valida con el motor.
- Mensajes en JSON con `kotlinx.serialization`, con versión del protocolo.
- Se prueba en la JVM con un transporte falso (cola en memoria, con pérdidas y desorden simulados): dos motores idénticos tras 1000 partidas. Esto no necesita un segundo teléfono (P4).

## 3. Android (F5.3-F5.5): la red local (D-48)

- **Transporte:** TCP en la red local (mismo Wi-Fi, o el punto de acceso del anfitrión, sin internet si hace falta); la sala (`enlace/LanServer.kt`) se anuncia por NSD como `_mono._tcp` y muestra su dirección por si la búsqueda falla. Dos teléfonos solo con datos móviles no se ven. `hostLoop`/`guestLoop` no saben del transporte.
- **Permisos:** el exacto de cada API sale de las fuentes del SDK, no de la referencia web (M-076): `grep -n 'ACCESS_LOCAL_NETWORK\|MulticastLock' ~/Android/Sdk/sources/android-37.0/android/net/nsd/NsdManager.java`. `ACCESS_LOCAL_NETWORK` hace falta desde Android 17 (API 37) con `targetSdk 37` (el Redmi es Android 16: no lo pide); el `MulticastLock` para buscar, hasta Android 13 sin «T extensions 7».
- **Red local o datos:** `telefono.py adb -- shell ip -br addr`; en el Redmi `ccmni*` son datos móviles (`lanAddresses` los descarta).
- HyperOS (Xiaomi) cierra conexiones en segundo plano: sala y «Unirme» dejan la pantalla encendida; si hace falta, guiar al autor a quitar el ahorro de batería para Mono, con capturas.
- Reconexión: el invitado pide el estado desde el último número de acción recibido; si no cuadra, el anfitrión envía el estado completo.
- **Prueba real (D-44):** F5.3-F5.5 son Redmi ↔ PC en el mismo Wi-Fi: `python3 pc/invitado.py --minutos 10` busca la sala con `avahi-browse -rpt _mono._tcp` (o `--ip`/`--puerto`). Solo el hito F5.6 es con el teléfono de un amigo (app por `adb` o un APK que el autor le pasa). Lo demás, en la JVM (`LinkTest`, `HostLoopTest`, `GuestLoopTest`).
- **Partida con `mono-pc` y su captura** (M-091): guion en el scratchpad que hace `telefono.py adb -- shell am start -n com.jacck.mono/.MainActivity --es enlace sala --ei jugadores 2 --el semilla N`, espera 8 s y `captura` (la sala); lanza `terminal/build/install/mono-pc/bin/mono-pc --espera 2 --semilla M` (con `JAVA_HOME`) en segundo plano, espera 12 s y `captura` (la partida); termina `mono-pc` (la partida queda en «se cortó») y `log -n 25`. Mismas semillas = misma partida, para comparar antes y después de un cambio. `--espera 2`: una propuesta del PC que cruza con una jugada del teléfono se repite a los 2 s, no a los 10. Las capturas se miran con `Read` antes de mostrarlas. Si el paso cambió una pantalla que solo sale con el enlace («Esperando a X», lo que llega del otro lado), esa captura de los 12 s va en el mismo paso, antes del punto de control; en una partida del autor, además, una `captura` en un turno del PC (M-093).
- **Partida del autor:** se le dice cuánto dura con la última partida real como referencia (F5.4c: $300 y salario $0, 260 jugadas suyas y 7,4 min), no con máquinas contra máquinas, que no declinan ni pasan en la subasta como él (M-094).
- **Pruebas largas** (minutos): con `run_in_background` y `python3 -u … > <scratchpad>/x.txt` (sin `-u` la salida se queda en el búfer hasta el final, M-079), mientras se programa el sub-paso siguiente; no se instala en el teléfono hasta que terminen (instalar cierra la sala).
- **Bluetooth** (D-45, solo la prueba `--es enlace eco` con `pc/eco.py`): el PC lo enciende el autor (`! rfkill unblock bluetooth && bluetoothctl power on`); `Connection refused` con el registro de Mono en `sdptool browse` = emparejado de un solo lado (M-077).
