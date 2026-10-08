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
import androidx.lifecycle.ViewModel
import com.jacck.mono.LOG_TAG
import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Result
import com.jacck.mono.engine.link.Guest
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.game.Remote
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.concurrent.thread

/** En qué va la conexión del invitado. */
sealed interface Join {
    data object Choosing : Join
    data class Connecting(val peer: String) : Join
    data class Connected(val peer: String) : Join
    /** Se cortó y vuelve a intentar cada 2 s (F5.5, D-74). */
    data class Lost(val peer: String, val reason: String) : Join
    data class Closed(val peer: String, val reason: String?) : Join
}

/** La partida tal como llegó en el primer `Snapshot`: con ella se abre el tablero. [mine] = jugadores de este teléfono. */
data class GuestStart(val config: GameConfig, val state: GameState, val mine: Set<Int>)

/**
 * El invitado en un teléfono (F5.10, D-76): la conexión con la sala, su `Guest` y la reconexión de
 * D-74, del lado del `GameViewModel` como [Remote]. Es un ViewModel para seguir vivo al pasar de
 * «Unirme» al tablero y al girar el teléfono. Las jugadas de aquí se proponen y vuelven, aplicadas por
 * el anfitrión, por [listen] con las del otro teléfono: [play] devuelve null. Los estados de Compose
 * solo se tocan en el hilo principal.
 */
class GuestRoom(context: Context) : ViewModel(), Remote {

    val label: String = Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME) ?: Build.MODEL

    var status by mutableStateOf<Join>(Join.Choosing)
        private set
    var start by mutableStateOf<GuestStart?>(null)
        private set
    var cuts by mutableStateOf(0)
        private set

    @Volatile override var seats: Set<Int> = emptySet()
        private set
    override val connected: Boolean get() = status is Join.Connected

    private val main = Handler(Looper.getMainLooper())
    private val guest: Guest = Guest(label) { n, result ->
        // En el hilo de la conexión, dentro de `guest.receive`: config y asientos ya están puestos.
        val config = guest.config!!
        val mine = guest.seats
        main.post { arrive(n, result, config, mine) }
    }
    private var listener: ((Int, Result) -> Unit)? = null
    private val early = mutableListOf<Pair<Int, Result>>() // lo que llega antes de que el tablero escuche
    @Volatile private var outbox: Outbox? = null
    @Volatile private var socket: Socket? = null
    @Volatile private var closed = false
    private var link: Thread? = null

    /** Se conecta con la sala de [peer] en [ip]:[port] y la atiende hasta el final; tras un corte vuelve a la misma dirección. */
    fun connect(peer: String, ip: String, port: Int) {
        if (link?.isAlive == true) return
        status = Join.Connecting(peer)
        Log.i(LOG_TAG, "invitado: conectando con $peer en $ip:$port")
        link = thread(name = "mono-invitado") { serve(peer, InetSocketAddress(ip, port)) }
    }

    /** No se pudo ni empezar a conectar con [peer] (la búsqueda no dio su dirección). */
    fun failed(peer: String, reason: String) {
        if (link?.isAlive != true) status = Join.Closed(peer, reason)
    }

    /** Vuelve a la lista de salas tras una conexión fallida. */
    fun choose() {
        if (start == null && link?.isAlive != true) status = Join.Choosing
    }

    /**
     * Propone una jugada de este teléfono (F5.10): antes la prueba con la copia del motor, para que un
     * botón que ya no vale dé su motivo como sin enlace (`IllegalActionException`). Si aún espera la
     * respuesta a otra, no la manda; sin conexión tampoco la propone, para que no quede esperando una
     * respuesta que no llegará y el toque de después del corte valga. Siempre null: vuelve por [listen].
     */
    override fun play(action: Action): Pair<Int, Result>? {
        val box = outbox
        val proposal = synchronized(guest) {
            val c = guest.config
            val s = guest.state
            if (c != null && s != null) Engine.apply(c, s, action)
            if (box == null) null else guest.propose(action)
        }
        when {
            box == null -> Log.i(LOG_TAG, "invitado: $action sin conexión; se toca otra vez al volver")
            proposal == null -> Log.i(LOG_TAG, "invitado: $action no se propone (espera respuesta)")
            else -> box.send(proposal)
        }
        return null
    }

    override fun listen(onApplied: (Int, Result) -> Unit) {
        listener = onApplied
        early.forEach { (n, r) -> onApplied(n, r) }
        early.clear()
    }

    override fun close() {
        if (closed) return
        closed = true
        socket?.close()
        Log.i(LOG_TAG, "invitado: se sale de la partida")
    }

    override fun onCleared() = close()

    private fun arrive(n: Int, result: Result, config: GameConfig, mine: Set<Int>) {
        if (start == null) {
            seats = result.state.players.indices.toSet() - mine
            val names = result.state.players.map { it.name }
            Log.i(LOG_TAG, "invitado: partida ${config.name}, ${config.squares.size} casillas, aquí ${mine.sorted().map(names::get)}, acción $n")
            start = GuestStart(config, result.state, mine)
            return
        }
        listener?.invoke(n, result) ?: early.add(n to result)
    }

    /** Hilo de la conexión: una sesión con [guestLoop] por conexión, y tras un corte, otra cada 2 s hasta 5 min (D-74). */
    private fun serve(peer: String, address: InetSocketAddress) {
        var lostAt = 0L
        while (!closed) {
            val s = Socket()
            socket = s
            try {
                s.connect(address, 8000)
                s.tcpNoDelay = true
                s.soTimeout = HOST_SILENCE_MS
            } catch (e: IOException) {
                s.close()
                val reason = e.message ?: "no pude conectar"
                if (closed || guest.state == null || System.currentTimeMillis() - lostAt > GIVE_UP_MS) {
                    Log.i(LOG_TAG, "invitado: no se pudo con $peer: $reason")
                    main.post { status = Join.Closed(peer, reason) }
                    return
                }
                Thread.sleep(RETRY_MS)
                continue
            }
            if (lostAt > 0) Log.i(LOG_TAG, "invitado: de vuelta tras %.1f s".format((System.currentTimeMillis() - lostAt) / 1000.0))
            main.post { status = Join.Connected(peer) }
            val reason = Outbox(s.getOutputStream()).use { box ->
                outbox = box
                val r = try { guestLoop(guest, s.getInputStream(), box::send) } catch (e: IOException) { e.message ?: "conexión rota" }
                outbox = null
                r
            }
            s.close()
            if (reason == null || closed) {
                Log.i(LOG_TAG, "invitado: fin de la sesión en la acción ${guest.last}, ${guest.mismatches} resúmenes distintos (${guest.closed ?: "partida terminada o salida"})")
                main.post { status = Join.Closed(peer, guest.closed) }
                return
            }
            lostAt = System.currentTimeMillis()
            Log.i(LOG_TAG, "invitado: se cortó ($reason) en la acción ${guest.last}; vuelvo a intentar")
            main.post { cuts++; status = Join.Lost(peer, reason) }
            Thread.sleep(RETRY_MS)
        }
    }

    private companion object {
        const val RETRY_MS = 2_000L
        const val GIVE_UP_MS = 300_000L
    }
}
