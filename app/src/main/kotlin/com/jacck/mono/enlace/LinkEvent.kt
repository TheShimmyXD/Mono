package com.jacck.mono.enlace

/** Lo que cuenta un servidor del enlace (la sala en la red local) a su pantalla. */
sealed interface LinkEvent {
    /** Esperando a un invitado en [port]. */
    data class Listening(val port: Int) : LinkEvent
    /** Llegó un invitado; [replaced]: su conexión reemplaza a otra que seguía abierta (vuelve tras un corte, F5.5). */
    data class Connected(val peer: String, val replaced: Boolean = false) : LinkEvent
    data class Closed(val reason: String?) : LinkEvent
}
