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

- Android 12+: permisos `BLUETOOTH_CONNECT` y `BLUETOOTH_SCAN` (y `BLUETOOTH_ADVERTISE` si el teléfono se hace visible) pedidos en tiempo de ejecución, con un texto que diga para qué.
- HyperOS (Xiaomi) cierra conexiones en segundo plano: la partida va en primer plano; si hace falta, guiar al autor a quitar el ahorro de batería para Mono, con capturas.
- Reconexión: el invitado pide el estado desde el último número de acción recibido; si no cuadra, el anfitrión envía el estado completo.
- Prueba real: el autor no tiene un segundo Android (P4). Las pruebas con dos teléfonos se agendan con el autor (el teléfono de un amigo con la app instalada por `adb` o un APK que el autor le pasa); mientras tanto, todo lo que se pueda se prueba en la JVM.
