package com.jacck.mono.enlace

/** Lo que cuenta un servidor del enlace (RFCOMM o red local) a su pantalla. */
sealed interface LinkEvent {
    /** Esperando a un invitado; [port] en la red local (null por Bluetooth). */
    data class Listening(val port: Int? = null) : LinkEvent
    data class Connected(val peer: String) : LinkEvent
    data class Closed(val reason: String?) : LinkEvent
}
