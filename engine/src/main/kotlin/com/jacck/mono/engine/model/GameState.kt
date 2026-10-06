package com.jacck.mono.engine.model

import com.jacck.mono.engine.GameRandom
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Estado de una partida en un instante (D-03): inmutable y completo, azar incluido, para
 * guardarlo o enviarlo por Bluetooth y seguir igual. La configuración va aparte (`GameConfig`).
 */
@Serializable
data class GameState(
    val players: List<PlayerState>,
    /** Casillas con dueño, por índice en el anillo; las que no están son del Banco. */
    val holdings: Map<Int, Holding>,
    /** Índice en `players` de quien juega. */
    val current: Int,
    val phase: TurnPhase,
    /** Dobles seguidos de quien juega (R-09). */
    val doublesInRow: Int,
    /** Casas y hoteles que le quedan al Banco (R-28). */
    val bankHouses: Int,
    val bankHotels: Int,
    /** Bote de Parada Libre (`freeParkingPot`, R-24). */
    val pot: Int,
    /** Turnos jugados, para el tope del simulador (F2.9). */
    val turn: Int,
    val random: GameRandom,
)

/** Un jugador. `jailTurns` = turnos ya pasados en la Cárcel; null si está libre (R-22). */
@Serializable
data class PlayerState(
    val name: String,
    val money: Int,
    val position: Int,
    val jailTurns: Int? = null,
    /** Cartas «Salir libre de la Cárcel» guardadas (R-18). */
    val jailCards: Int = 0,
    val bankrupt: Boolean = false,
)

/** Dueño (índice en `players`) y lo que hay en una casilla (R-25, R-27, R-31). */
@Serializable
data class Holding(
    val owner: Int,
    val houses: Int = 0,
    val hotel: Boolean = false,
    val mortgaged: Boolean = false,
)

/** En qué punto del turno está la partida: dice qué acciones son válidas. */
@Serializable
sealed interface TurnPhase {

    /** Quien juega debe tirar los dados (o salir de la Cárcel). */
    @Serializable
    @SerialName("roll")
    data object Roll : TurnPhase

    /** Cayó en una casilla del Banco: compra o rechaza (R-11). */
    @Serializable
    @SerialName("buy")
    data class Buy(val square: Int) : TurnPhase

    /** Subasta en curso (R-12, R-44); `bidders` = quienes aún pujan, en orden. */
    @Serializable
    @SerialName("auction")
    data class Auction(
        val square: Int,
        val bidders: List<Int>,
        val highestBid: Int,
        val highestBidder: Int?,
    ) : TurnPhase

    /** Ya movió y resolvió la casilla: puede construir, negociar o terminar. */
    @Serializable
    @SerialName("endOfTurn")
    data object EndOfTurn : TurnPhase

    /** Partida terminada (R-35, R-39, R-40). */
    @Serializable
    @SerialName("over")
    data class Over(val winner: Int) : TurnPhase
}
