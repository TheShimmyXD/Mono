package com.jacck.mono.terminal

import com.jacck.mono.engine.link.Guest
import com.jacck.mono.engine.link.Message
import com.jacck.mono.engine.link.actor
import com.jacck.mono.engine.link.decode
import com.jacck.mono.engine.link.encode
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.TurnPhase
import java.io.BufferedReader
import java.io.IOException
import java.io.Writer
import java.net.SocketTimeoutException
import kotlin.random.Random

/**
 * Cómo terminó la sesión: si la partida acabó, cuántas acciones propuso, resúmenes que no cuadraron,
 * el `Bye` y, si se cortó la red (F5.5), por qué (`lost`): entonces vale la pena volver a conectar.
 */
data class GuestResult(val over: Boolean, val proposals: Int, val mismatches: Int, val closed: String?, val lost: String? = null)

/**
 * El PC como invitado (F5.4, D-46, D-50): saluda, recibe la partida y, cada vez que le toca a uno de
 * sus jugadores, propone lo que diga [choose]. Las acciones del anfitrión las aplica [guest] y compara
 * su resumen. Si [input] no trae nada en el tiempo del socket, pide lo que falta (`Guest.timeout`);
 * el anfitrión siempre contesta (`Alive`), así que dos esperas seguidas sin nada son una red caída
 * (F5.5, D-74). Mientras recibe, cada [beatMs] manda un `Resync` como latido, para que la sala no
 * lo dé por perdido. Empieza con `Guest.resume` (saludo, o lo que falta tras un corte). [say] recibe
 * una línea por acción aplicada y otra por turno terminado. Acaba con la partida, un `Bye` o el corte.
 */
fun guestSession(guest: Guest, input: BufferedReader, output: Writer, seed: Long, say: (String) -> Unit = {}, beatMs: Long = 5_000): GuestResult {
    val random = Random(seed)
    var proposals = 0
    var sentAt = System.currentTimeMillis()
    var silent = 0
    fun send(messages: List<Message>) {
        if (messages.isEmpty()) return
        for (m in messages) output.write(encode(m) + "\n")
        output.flush()
        sentAt = System.currentTimeMillis()
    }
    fun result(lost: String? = null) = GuestResult(guest.state?.phase is TurnPhase.Over, proposals, guest.mismatches, guest.closed, lost)
    try {
        send(listOf(guest.resume()))
    } catch (e: IOException) {
        return result(e.message ?: "no se pudo escribir")
    }
    while (true) {
        val config = guest.config
        val state = guest.state
        if (state?.phase is TurnPhase.Over) break
        if (config != null && state != null && guest.pending == null) {
            choose(config, state, guest.seats, random)?.let { guest.propose(it) }?.let {
                try { send(listOf(it)) } catch (e: IOException) { return result(e.message) }
                proposals++
            }
        }
        val line = try {
            input.readLine()
        } catch (_: SocketTimeoutException) {
            if (++silent >= 2) return result("sin respuesta del anfitrión")
            try { send(guest.timeout()) } catch (e: IOException) { return result(e.message) }
            continue
        } catch (e: IOException) {
            return result(e.message ?: "conexión rota")
        } ?: return result("el anfitrión cerró la conexión")
        silent = 0
        val message = try { decode(line) } catch (_: IllegalArgumentException) { continue }
        if (message is Message.Applied && state != null && message.n == guest.last + 1) {
            val who = state.players[actor(state, message.action)].name
            say("#${message.n} $who: ${message.action}")
        }
        try {
            send(guest.receive(message))
            if (System.currentTimeMillis() - sentAt >= beatMs) send(listOf(Message.Resync(guest.last)))
        } catch (e: IOException) {
            return result(e.message)
        }
        if (message is Message.Applied && message.action == Action.EndTurn) {
            guest.state?.let { s -> say("  turno ${s.turn}: " + s.players.joinToString(" · ") { p -> if (p.bankrupt) "${p.name} quebró" else "${p.name} $${p.money}" }) }
        }
        if (message is Message.Bye) break
    }
    return result()
}

/**
 * El PC en la sala con reconexión (F5.5, D-74): abre una conexión con [connect] y juega con
 * [guestSession]; si se corta, avisa con [say] y vuelve a intentar cada [retryMs] hasta [giveUpMs]
 * sin lograrlo. `connect` devuelve el lector y el escritor de una conexión nueva, o null si no pudo.
 * Devuelve el resultado de la última sesión (con las propuestas de todas) y cuántas veces volvió.
 */
fun guestLink(
    guest: Guest,
    seed: Long,
    connect: () -> Pair<BufferedReader, Writer>?,
    say: (String) -> Unit = {},
    beatMs: Long = 5_000,
    retryMs: Long = 2_000,
    giveUpMs: Long = 300_000,
): Pair<GuestResult, Int> {
    var proposals = 0
    var returns = 0
    var session = 0
    var cut: GuestResult? = null // la última sesión que se cortó
    var cutAt = 0L
    while (true) {
        val link = connect()
        if (link == null) {
            val last = cut ?: return GuestResult(false, proposals, guest.mismatches, guest.closed, "no pude conectar") to returns
            if (System.currentTimeMillis() - cutAt > giveUpMs) return last.copy(proposals = proposals) to returns
            Thread.sleep(retryMs)
            continue
        }
        if (cut != null) {
            returns++
            say("De vuelta tras %.1f s (reconexión %d).".format((System.currentTimeMillis() - cutAt) / 1000.0, returns))
        }
        val result = guestSession(guest, link.first, link.second, seed + session++, say, beatMs)
        proposals += result.proposals
        val lost = result.lost ?: return result.copy(proposals = proposals) to returns
        say("Se cortó la conexión ($lost) en la acción ${guest.last}; vuelvo a intentar cada %.1f s.".format(retryMs / 1000.0))
        cut = result
        cutAt = System.currentTimeMillis()
        Thread.sleep(retryMs)
    }
}
