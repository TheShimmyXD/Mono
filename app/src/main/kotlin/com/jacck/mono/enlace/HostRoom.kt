package com.jacck.mono.enlace

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jacck.mono.LOG_TAG
import com.jacck.mono.engine.Result
import com.jacck.mono.engine.link.Host
import com.jacck.mono.engine.link.Message
import com.jacck.mono.engine.model.Action
import com.jacck.mono.game.Remote
import java.io.InputStream
import java.io.OutputStream

/**
 * La sala del anfitrión (F5.4, D-51): la partida de verdad ([host]), el servidor de la red local y el
 * buzón hacia el invitado. Vive en el `GameViewModel` y no en la pantalla, para seguir abierta al pasar
 * de la sala a la partida y al girar el teléfono. Lo que muestra la sala son estados de Compose (la
 * pantalla se redibuja sola al cambiar) y solo se tocan en el hilo principal.
 */
class HostRoom(context: Context, private val host: Host) : Remote {

    private val app = context.applicationContext
    val label: String = Settings.Global.getString(app.contentResolver, Settings.Global.DEVICE_NAME) ?: Build.MODEL

    var event by mutableStateOf<LinkEvent?>(null)
        private set
    var port by mutableStateOf<Int?>(null)
        private set
    var guest by mutableStateOf("?")
        private set
    var since by mutableStateOf(0L)
        private set
    var cuts by mutableStateOf(0)
        private set
    var messages by mutableStateOf(0)
        private set

    /** Llegó el primer saludo del invitado: la partida se abre (y no se cierra si después se corta). */
    var started by mutableStateOf(false)
        private set

    override val seats: Set<Int> get() = host.guestSeats
    override val connected: Boolean get() = event is LinkEvent.Connected

    private val main = Handler(Looper.getMainLooper())
    @Volatile private var outbox: Outbox? = null
    private var server: LanServer? = null
    private var listener: (Int, Result) -> Unit = { _, _ -> }

    /** Abre la sala en la red local; la segunda vez no hace nada. */
    fun start() {
        if (server != null) return
        server = LanServer(app, label, ::onEvent, ::session).also { it.start() }
    }

    override fun play(action: Action): Pair<Int, Result> = synchronized(host) {
        val result = playLocal(host, action) { outbox?.send(it) }
        host.last to result
    }

    override fun listen(onApplied: (Int, Result) -> Unit) {
        listener = onApplied
    }

    /** Cierra el servidor; la sesión abierta termina sola al cerrarse su conexión. */
    override fun close() {
        server?.stop()
        server = null
    }

    private fun onEvent(e: LinkEvent) {
        Log.i(LOG_TAG, "anfitrión: $e")
        main.post {
            if (e is LinkEvent.Listening) port = e.port
            // Tras un corte (F5.5) el invitado vuelve con `Resync`, sin `Hello`: conserva su nombre.
            if (e is LinkEvent.Connected) { since = System.currentTimeMillis(); messages = 0 }
            if (e is LinkEvent.Closed && event is LinkEvent.Connected) {
                cuts++
                Log.i(LOG_TAG, "anfitrión: corte $cuts tras ${clock((System.currentTimeMillis() - since) / 1000)}")
            }
            event = e
        }
    }

    /**
     * Un invitado conectado (hilo de su sesión): lee con [hostLoop] y escribe por su propio [Outbox].
     * La conexión que vuelve tras un corte reemplaza a la vieja (`acceptLoop`, F5.5).
     */
    private fun session(input: InputStream, output: OutputStream) {
        Outbox(output).use { box ->
            outbox = box
            val (read, bad) = hostLoop(
                host, input, box::send,
                onMessage = { m ->
                    if (m is Message.Hello) Log.i(LOG_TAG, "anfitrión: hola de ${m.name}, protocolo ${m.version}")
                    main.post {
                        messages++
                        if (m is Message.Hello) { guest = m.name; started = true }
                    }
                },
                onApplied = { n, result -> main.post { listener(n, result) } },
            )
            if (outbox === box) outbox = null // una conexión nueva ya puede tener su propio buzón
            Log.i(LOG_TAG, "anfitrión: fin de la sesión, $read mensajes ($bad sin entender), ${box.written} enviados")
        }
    }
}
