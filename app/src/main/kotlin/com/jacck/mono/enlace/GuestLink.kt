package com.jacck.mono.enlace

import com.jacck.mono.engine.link.Guest
import com.jacck.mono.engine.link.Message
import com.jacck.mono.engine.link.decode
import com.jacck.mono.engine.model.TurnPhase
import java.io.IOException
import java.io.InputStream
import java.net.SocketTimeoutException

/**
 * Silencio del anfitrión con el que el invitado pide lo que falta (`Guest.timeout`, F5.10, D-76): el
 * anfitrión siempre contesta, así que dos seguidos sin nada son una red caída (D-74). Va por debajo
 * de los 30 s de [GUEST_SILENCE_MS] para que la sala no lo dé por perdido mientras se piensa una jugada.
 */
const val HOST_SILENCE_MS = 10_000

/**
 * Una conexión del invitado (F5.3, F5.10, D-46, D-76): empieza con `Guest.resume` (el saludo, o lo
 * que falta tras un corte, D-74), le da a [guest] cada línea de JSON de [input] y manda sus respuestas
 * con [send] (el [Outbox]). Si [input] no trae nada en el tiempo del socket, pide lo que falta; dos
 * esperas seguidas sin nada cortan. Mientras recibe, cada [beatMs] sin mandar nada va un `Resync`
 * como latido. Una línea que no se entiende se salta. [onMessage] recibe cada mensaje entendido, ya
 * aplicado. Las jugadas de aquí las propone la pantalla con el mismo [guest] (`GuestRoom.play`).
 * No sabe de sockets, para probarlo en la JVM. Devuelve null si terminó (`Bye` o fin de la partida)
 * o el motivo del corte, y entonces vale la pena volver a conectar.
 */
fun guestLoop(
    guest: Guest,
    input: InputStream,
    send: (Message) -> Unit,
    onMessage: (Message) -> Unit = {},
    beatMs: Long = 5_000,
    clock: () -> Long = System::currentTimeMillis,
): String? {
    val reader = input.bufferedReader(Charsets.UTF_8)
    var sentAt = clock()
    fun reply(messages: List<Message>) {
        if (messages.isEmpty()) return
        messages.forEach(send)
        sentAt = clock()
    }
    reply(listOf(synchronized(guest) { guest.resume() }))
    var silent = 0
    while (true) {
        val line = try {
            reader.readLine()
        } catch (_: SocketTimeoutException) {
            if (++silent >= 2) return "sin respuesta del anfitrión"
            reply(synchronized(guest) { guest.timeout() })
            continue
        } catch (e: IOException) {
            return e.message ?: "conexión rota"
        } ?: return "el anfitrión cerró la conexión"
        silent = 0
        val message = try { decode(line) } catch (_: IllegalArgumentException) { continue }
        // La pantalla también propone con `guest` (GuestRoom.play): un solo hilo a la vez.
        val replies = synchronized(guest) { guest.receive(message) }
        onMessage(message)
        if (message is Message.Bye) return null
        reply(replies)
        if (clock() - sentAt >= beatMs) reply(listOf(Message.Resync(guest.last)))
        if (guest.state?.phase is TurnPhase.Over) return null
    }
}
