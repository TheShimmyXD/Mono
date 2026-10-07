# Enlace por Bluetooth (fase F5)

*Se lee en F5, después de que el autor aprobara el hito «Primera quiebra». Lo que aquí se dice se confirma en la documentación de Android del momento antes de programar (regla 1: nada se supone del sistema).*

## 1. Primero la decisión (F5.1)

Opciones para la D-##, con prueba de concepto de cada una antes de elegir:
- **Bluetooth clásico (RFCOMM)**: lo que pidió el autor; sin servicios de Google; emparejamiento del sistema.
- **Nearby Connections** (Google Play services): usa Bluetooth y wifi; descubre y conecta con menos código; depende de Play services en los dos teléfonos.
La pregunta al autor lleva qué cuesta cada una en tareas y qué ve él al conectar.

## 2. Protocolo (F5.2, antes de tocar el Bluetooth)

- El anfitrión decide: tira los dados con la semilla de la partida, aplica la acción y envía `(número, acción)`; el invitado aplica la misma acción y compara un resumen (hash) del estado.
- El invitado solo propone acciones en su turno; el anfitrión las valida con el motor.
- Mensajes en JSON con `kotlinx.serialization`, con versión del protocolo.
- Se prueba en la JVM con un transporte falso (cola en memoria, con pérdidas y desorden simulados): dos motores idénticos tras 1000 partidas. Esto no necesita un segundo teléfono (P4).

## 3. Android (F5.3-F5.5)

- Android 12+: permisos `BLUETOOTH_CONNECT` y `BLUETOOTH_SCAN` (y `BLUETOOTH_ADVERTISE` si el teléfono se hace visible) pedidos en tiempo de ejecución, con un texto que diga para qué. El permiso exacto de cada método sale de las fuentes del SDK, no de la referencia web (que solo devuelve el menú, M-076): `grep -B4 'public .*listenUsingRfcommWithServiceRecord(' ~/Android/Sdk/sources/android-37.0/android/bluetooth/BluetoothAdapter.java` → `@RequiresPermission(...)`.
- HyperOS (Xiaomi) cierra conexiones en segundo plano: la partida va en primer plano; si hace falta, guiar al autor a quitar el ahorro de batería para Mono, con capturas.
- Reconexión: el invitado pide el estado desde el último número de acción recibido; si no cuadra, el anfitrión envía el estado completo.
- Prueba real (D-44): F5.3-F5.5 son Redmi ↔ PC; solo el hito F5.6 es con el teléfono de un amigo (app por `adb` o un APK que el autor le pasa). Lo demás, en la JVM (`LinkTest`).
- **PC ↔ Redmi** (D-45, M-077): el Bluetooth del PC lo enciende el autor (`! rfkill unblock bluetooth && bluetoothctl power on`). El canal RFCOMM cambia al reabrir la app: se busca por SDP (`pc/eco.py` lo hace). `Connection refused` con el registro de Mono visible en `sdptool browse` = emparejado solo de un lado: si `telefono.py adb -- shell dumpsys bluetooth_manager | grep -c <MAC del PC>` da 0, el autor quita el Redmi en el PC (`bluetoothctl remove <MAC>`) y empareja desde el Redmi.
