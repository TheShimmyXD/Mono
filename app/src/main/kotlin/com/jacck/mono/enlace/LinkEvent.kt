package com.jacck.mono.enlace

/** Lo que cuenta un servidor del enlace (la sala en la red local) a su pantalla. */
sealed interface LinkEvent {
    /** Esperando a un invitado en [port]. */
    data class Listening(val port: Int) : LinkEvent
    data class Connected(val peer: String) : LinkEvent
    data class Closed(val reason: String?) : LinkEvent
}
