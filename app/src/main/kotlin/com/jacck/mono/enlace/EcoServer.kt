package com.jacck.mono.enlace

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.concurrent.thread

/**
 * Prueba de concepto de F5.1 (D-45): el teléfono escucha por Bluetooth clásico (RFCOMM) y devuelve
 * cada línea que recibe; el PC se conecta con `pc/eco.py`. El teléfono es el servidor (el anfitrión
 * de F5.2), así que solo pide `BLUETOOTH_CONNECT`: `listenUsingRfcommWithServiceRecord` lo exige y
 * no hace falta buscar dispositivos (`BLUETOOTH_SCAN`), según las fuentes del SDK 37.
 */
val MONO_UUID: UUID = UUID.fromString("1a80cf3d-efe2-4ea4-98d1-6c4976e1d3c9")

/** Nombre del registro SDP: con él `pc/eco.py` encuentra el canal RFCOMM. */
const val SERVICE_NAME = "Mono"

/** Primera línea que manda el teléfono al conectar; el PC la usa para saber que llegó a Mono. */
const val GREETING = "MONO eco 1"

/**
 * Manda [GREETING] y responde `eco <n>: <línea>` a cada línea de [input] hasta que el otro lado
 * cierra. Devuelve cuántas líneas contestó. No sabe de Bluetooth, para probarlo en la JVM.
 */
fun echoLoop(input: InputStream, output: OutputStream, onMessage: (Int, String) -> Unit = { _, _ -> }): Int {
    val reader = input.bufferedReader(Charsets.UTF_8)
    val writer = output.bufferedWriter(Charsets.UTF_8)
    writer.write("$GREETING\n")
    writer.flush()
    var n = 0
    while (true) {
        val line = reader.readLine() ?: break
        n++
        writer.write("eco $n: $line\n")
        writer.flush()
        onMessage(n, line)
    }
    return n
}

/** Servidor de eco en un hilo propio: `accept` y `readLine` bloquean y no pueden ir en el hilo de la pantalla. */
class EcoServer(private val adapter: BluetoothAdapter, private val onEvent: (Event) -> Unit) {

    sealed interface Event {
        data object Listening : Event
        data class Connected(val peer: String) : Event
        data class Message(val n: Int, val text: String) : Event
        data class Closed(val reason: String?) : Event
    }

    @Volatile private var server: BluetoothServerSocket? = null
    @Volatile private var socket: BluetoothSocket? = null
    @Volatile private var stopped = false

    /** Escucha, atiende a un cliente y vuelve a escuchar, hasta [stop]. */
    @SuppressLint("MissingPermission") // EcoScreen solo lo arranca con BLUETOOTH_CONNECT concedido
    fun start() = thread(name = "mono-eco") {
        while (!stopped) {
            try {
                val listening = adapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, MONO_UUID)
                server = listening
                onEvent(Event.Listening)
                val s = listening.accept()
                listening.close() // RFCOMM atiende un cliente por canal
                socket = s
                onEvent(Event.Connected(s.remoteDevice.name ?: s.remoteDevice.address))
                echoLoop(s.inputStream, s.outputStream) { n, line -> onEvent(Event.Message(n, line)) }
                onEvent(Event.Closed(null))
            } catch (e: IOException) {
                if (stopped) break
                onEvent(Event.Closed(e.message))
                Thread.sleep(2000) // p. ej. Bluetooth apagado: no reintentar sin pausa
            } finally {
                socket?.close()
                socket = null
            }
        }
    }

    fun stop() {
        stopped = true
        server?.close()
        socket?.close()
    }
}
