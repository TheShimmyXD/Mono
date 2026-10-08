package com.jacck.mono.engine.link

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.link.Message.Applied
import com.jacck.mono.engine.link.Message.Bye
import com.jacck.mono.engine.link.Message.Hello
import com.jacck.mono.engine.link.Message.Propose
import com.jacck.mono.engine.link.Message.Rejected
import com.jacck.mono.engine.link.Message.Resync
import com.jacck.mono.engine.link.Message.Snapshot

/**
 * El anfitrión (F5.2, D-46): tiene la partida de verdad y el registro de todas las acciones, para
 * reenviar las que el invitado no recibió. `guestSeats` = jugadores que maneja el invitado.
 * No sabe de sockets: recibe mensajes y devuelve los que hay que enviar.
 */
class Host(val config: GameConfig, start: GameState, val guestSeats: Set<Int>) {

    var state: GameState = start
        private set
    private val log = mutableListOf<Applied>()

    /** Número de la última acción aplicada (0 al empezar). */
    val last: Int get() = log.size

    /**
     * Aplica una acción de un jugador del anfitrión (o `TimeUp` de la app) y devuelve el mensaje
     * para el invitado. Lanza `IllegalActionException` si el motor no la acepta.
     */
    fun play(action: Action): Applied {
        require(action == Action.TimeUp || actor(state, action) !in guestSeats) { "la decide el invitado: $action" }
        return record(action)
    }

    fun receive(message: Message): List<Message> = when (message) {
        is Hello -> if (message.version == PROTOCOL_VERSION) listOf(snapshot())
        else listOf(Bye("versión del protocolo ${message.version}; el anfitrión usa $PROTOCOL_VERSION"))
        // Una propuesta vieja (repetida o hecha sin ver lo último) no se aplica: se le manda lo que le falta.
        is Propose -> if (message.after != last) resend(message.after) else listOf(judge(message))
        is Resync -> if (message.full) listOf(snapshot()) else resend(message.after)
        else -> emptyList()
    }

    fun snapshot() = Snapshot(PROTOCOL_VERSION, config, state, guestSeats, last)

    private fun judge(p: Propose): Message {
        if (p.action == Action.TimeUp || actor(state, p.action) !in guestSeats) return Rejected(p.after, "no le toca al invitado")
        return try {
            record(p.action)
        } catch (e: IllegalStateException) { // IllegalActionException
            Rejected(p.after, e.message ?: "acción no válida")
        } catch (e: UnsupportedOperationException) { // Trade, aún sin motor
            Rejected(p.after, e.message ?: "acción no disponible")
        }
    }

    private fun record(action: Action): Applied {
        state = Engine.apply(config, state, action).state
        return Applied(last + 1, action, digest(state)).also { log += it }
    }

    private fun resend(after: Int): List<Message> = if (after in 0..last) log.subList(after, last).toList() else listOf(snapshot())
}

/**
 * El invitado (F5.2, D-46): aplica las acciones del anfitrión en orden. Las que llegan adelantadas
 * esperan en `ahead`; las repetidas se ignoran; si el resumen no cuadra pide la partida entera.
 * Si no llega nada en un rato, la app llama a [timeout].
 */
class Guest(private val name: String) {

    var config: GameConfig? = null
        private set
    var state: GameState? = null
        private set
    var seats: Set<Int> = emptySet()
        private set
    var last = -1
        private set

    /** Propuesta enviada que aún no tiene respuesta: no se propone otra. */
    var pending: Propose? = null
        private set
    var closed: String? = null
        private set

    /** Resúmenes que no cuadraron (debe quedar en 0). */
    var mismatches = 0
        private set
    private val ahead = sortedMapOf<Int, Applied>()

    fun hello() = Hello(PROTOCOL_VERSION, name)

    fun receive(message: Message): List<Message> {
        when (message) {
            is Snapshot -> {
                config = message.config; state = message.state; seats = message.seats; last = message.last
                pending = null
                ahead.headMap(last + 1).clear()
            }
            is Applied -> if (message.n > last) {
                ahead[message.n] = message
                return applyReady()
            }
            is Rejected -> if (message.after == pending?.after) pending = null
            is Bye -> closed = message.reason
            else -> Unit
        }
        return emptyList()
    }

    /**
     * Propone una acción de uno de sus jugadores, si está al día y sin otra esperando; antes la
     * prueba con su copia del motor para no mandar algo que no vale. Devuelve el mensaje o null.
     */
    fun propose(action: Action): Propose? {
        val c = config ?: return null
        val s = state ?: return null
        if (pending != null || ahead.isNotEmpty() || actor(s, action) !in seats) return null
        Engine.tryApply(c, s, action) ?: return null
        return Propose(action, last).also { pending = it }
    }

    /** Nada llegó en un rato: pide lo que falta (o todo, si aún no tiene partida) y olvida la propuesta. */
    fun timeout(): List<Message> {
        pending = null
        return listOf(Resync(last, full = state == null))
    }

    private fun applyReady(): List<Message> {
        val c = config ?: return listOf(Resync(last, full = true))
        while (true) {
            val next = ahead.remove(last + 1) ?: break
            val after = try { Engine.apply(c, state!!, next.action).state } catch (_: IllegalStateException) { null }
            if (after == null || digest(after) != next.digest) {
                mismatches++
                ahead.clear()
                return listOf(Resync(last, full = true))
            }
            state = after
            last = next.n
            pending = null
        }
        ahead.headMap(last + 1).clear()
        return emptyList()
    }
}
