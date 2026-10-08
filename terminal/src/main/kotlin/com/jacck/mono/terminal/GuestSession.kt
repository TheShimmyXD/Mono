package com.jacck.mono.terminal

import com.jacck.mono.engine.link.Guest
import com.jacck.mono.engine.link.Message
import com.jacck.mono.engine.link.actor
import com.jacck.mono.engine.link.decode
import com.jacck.mono.engine.link.encode
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.TurnPhase
import java.io.BufferedReader
import java.io.Writer
import java.net.SocketTimeoutException
import kotlin.random.Random

/** Cómo terminó la sesión: si la partida acabó, cuántas acciones propuso, resúmenes que no cuadraron y el `Bye`. */
data class GuestResult(val over: Boolean, val proposals: Int, val mismatches: Int, val closed: String?)

/**
 * El PC como invitado (F5.4, D-46, D-50): saluda, recibe la partida y, cada vez que le toca a uno de
 * sus jugadores, propone lo que diga [choose]. Las acciones del anfitrión las aplica [guest] y compara
 * su resumen. Si [input] no trae nada en el tiempo del socket, pide lo que falta (`Guest.timeout`).
 * [say] recibe una línea por acción aplicada y otra por turno terminado. Acaba con la partida, un
 * `Bye` o el fin de la conexión.
 */
fun guestSession(guest: Guest, input: BufferedReader, output: Writer, seed: Long, say: (String) -> Unit = {}): GuestResult {
    val random = Random(seed)
    var proposals = 0
    fun send(messages: List<Message>) {
        if (messages.isEmpty()) return
        for (m in messages) output.write(encode(m) + "\n")
        output.flush()
    }
    send(listOf(guest.hello()))
    while (true) {
        val config = guest.config
        val state = guest.state
        if (state?.phase is TurnPhase.Over) break
        if (config != null && state != null && guest.pending == null) {
            choose(config, state, guest.seats, random)?.let { guest.propose(it) }?.let {
                send(listOf(it))
                proposals++
            }
        }
        val line = try {
            input.readLine()
        } catch (_: SocketTimeoutException) {
            send(guest.timeout())
            continue
        } ?: break
        val message = try { decode(line) } catch (_: IllegalArgumentException) { continue }
        if (message is Message.Applied && state != null && message.n == guest.last + 1) {
            val who = state.players[actor(state, message.action)].name
            say("#${message.n} $who: ${message.action}")
        }
        send(guest.receive(message))
        if (message is Message.Applied && message.action == Action.EndTurn) {
            guest.state?.let { s -> say("  turno ${s.turn}: " + s.players.joinToString(" · ") { p -> if (p.bankrupt) "${p.name} quebró" else "${p.name} $${p.money}" }) }
        }
        if (message is Message.Bye) break
    }
    return GuestResult(guest.state?.phase is TurnPhase.Over, proposals, guest.mismatches, guest.closed)
}
