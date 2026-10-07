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
 * Servicio de Mono por Bluetooth clásico (RFCOMM, D-45). El teléfono anfitrión es el servidor, así
 * que solo pide `BLUETOOTH_CONNECT`: `listenUsingRfcommWithServiceRecord` lo exige y no hace falta
 * buscar dispositivos (`BLUETOOTH_SCAN`), según las fuentes del SDK 37.
 */
val MONO_UUID: UUID = UUID.fromString("1a80cf3d-efe2-4ea4-98d1-6c4976e1d3c9")

/** Nombre del registro SDP: con él el PC (`pc/eco.py`, `pc/invitado.py`) encuentra el canal RFCOMM. */
const val SERVICE_NAME = "Mono"

/**
 * Servidor en un hilo propio (`accept` y `readLine` bloquean y no pueden ir en el hilo de la
 * pantalla): escucha, atiende a un cliente con [session] hasta que cierra y vuelve a escuchar, hasta [stop].
 */
class RfcommServer(
    private val adapter: BluetoothAdapter,
    private val onEvent: (Event) -> Unit,
    private val session: (InputStream, OutputStream) -> Unit,
) {

    sealed interface Event {
        data object Listening : Event
        data class Connected(val peer: String) : Event
        data class Closed(val reason: String?) : Event
    }

    @Volatile private var server: BluetoothServerSocket? = null
    @Volatile private var socket: BluetoothSocket? = null
    @Volatile private var stopped = false

    @SuppressLint("MissingPermission") // BluetoothGate solo lo arranca con BLUETOOTH_CONNECT concedido
    fun start() = thread(name = "mono-rfcomm") {
        while (!stopped) {
            try {
                val listening = adapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, MONO_UUID)
                server = listening
                onEvent(Event.Listening)
                val s = listening.accept()
                listening.close() // RFCOMM atiende un cliente por canal
                socket = s
                onEvent(Event.Connected(s.remoteDevice.name ?: s.remoteDevice.address))
                session(s.inputStream, s.outputStream)
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
