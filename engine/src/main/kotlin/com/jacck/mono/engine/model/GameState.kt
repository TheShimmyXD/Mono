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
    /** Orden de cada mazo, de arriba abajo, con índices de `GameConfig.cards` (R-18). */
    val decks: Map<Deck, List<Int>> = emptyMap(),
)

/** Un jugador. `jailTurns` = turnos ya pasados en la Cárcel; null si está libre (R-22). */
@Serializable
data class PlayerState(
    val name: String,
    val money: Int,
    val position: Int,
    val jailTurns: Int? = null,
    /** Cartas «Salir libre de la Cárcel» guardadas, índices de `GameConfig.cards` (R-18). */
    val jailCards: List<Int> = emptyList(),
    val bankrupt: Boolean = false,
)

/**
 * `debtor` debe a `creditor` (índice en `players`); null = al Banco (R-34, R-35). `amount` = lo que
 * le pagó en la acción que lo dejó en negativo: tope de lo que devuelve el acreedor si quiebra (D-15).
 */
@Serializable
data class PlayerDebt(val debtor: Int, val creditor: Int?, val amount: Int = 0)

/** Dueño (índice en `players`) y lo que hay en una casilla (R-25, R-27, R-31). */
@Serializable
data class Holding(
    val owner: Int,
    val houses: Int = 0,
    val hotel: Boolean = false,
    val mortgaged: Boolean = false,
    /** Ya pagó en este turno el interés de una hipotecada recibida: levantarla cuesta solo el valor (R-34). */
    val feePaid: Boolean = false,
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

    /**
     * Subasta en curso (R-12, R-44); `bidders` = quienes aún pujan, en orden. `queue` = las
     * que se subastan después (R-35); `then` = la fase al terminar, null = sigue el turno.
     */
    @Serializable
    @SerialName("auction")
    data class Auction(
        val square: Int,
        val bidders: List<Int>,
        val highestBid: Int,
        val highestBidder: Int?,
        val queue: List<Int> = emptyList(),
        val then: TurnPhase? = null,
    ) : TurnPhase

    /** Cayó en un impuesto con porcentaje: elige cómo pagarlo (R-19). */
    @Serializable
    @SerialName("taxChoice")
    data class TaxChoice(val square: Int) : TurnPhase

    /** Ya movió y resolvió la casilla: puede construir, negociar o terminar. */
    @Serializable
    @SerialName("endOfTurn")
    data object EndOfTurn : TurnPhase

    /**
     * Alguien quedó con saldo negativo: `debts[0]` vende, hipoteca o se declara en quiebra
     * (R-34, R-35); los demás esperan. Pagadas todas, sigue `resume`.
     */
    @Serializable
    @SerialName("debt")
    data class Debt(val debts: List<PlayerDebt>, val resume: TurnPhase) : TurnPhase

    /** Partida terminada; varios ganadores si empatan en el recuento (R-35, R-39, R-40, D-15). */
    @Serializable
    @SerialName("over")
    data class Over(val winners: List<Int>) : TurnPhase
}
