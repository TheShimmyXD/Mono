package com.jacck.mono.engine.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Lo que un jugador puede pedir al motor. Las acciones sin `player` las hace quien juega;
 * el motor (F2.2..F2.6) dice si son válidas en la fase actual. Son lo que viaja por Bluetooth.
 * `square` es siempre el índice de la casilla en el anillo.
 */
@Serializable
sealed interface Action {

    /** Tirar los dados (R-07). */
    @Serializable
    @SerialName("roll")
    data object Roll : Action

    /** Comprar la casilla al precio impreso (R-11). */
    @Serializable
    @SerialName("buy")
    data object Buy : Action

    /** No comprarla: se subasta o sigue del Banco (R-12, R-44). */
    @Serializable
    @SerialName("decline")
    data object Decline : Action

    /** Pujar en la subasta (R-12). */
    @Serializable
    @SerialName("bid")
    data class Bid(val player: Int, val amount: Int) : Action

    /** Retirarse de la subasta. */
    @Serializable
    @SerialName("passBid")
    data class PassBid(val player: Int) : Action

    /** Comprar una casa, o el hotel si ya tiene `maxHouses` (R-25, R-27, R-46, R-47). */
    @Serializable
    @SerialName("build")
    data class Build(val square: Int) : Action

    /** Vender una casa o el hotel al Banco (R-30). */
    @Serializable
    @SerialName("sellBuilding")
    data class SellBuilding(val square: Int) : Action

    /** Hipotecar (R-31, R-49). */
    @Serializable
    @SerialName("mortgage")
    data class Mortgage(val square: Int) : Action

    /** Levantar la hipoteca (R-32, R-50). */
    @Serializable
    @SerialName("unmortgage")
    data class Unmortgage(val square: Int) : Action

    /** Pagar la multa para salir de la Cárcel (R-22). */
    @Serializable
    @SerialName("payJailFine")
    data object PayJailFine : Action

    /** Pagar el impuesto: el `percent` % del patrimonio o la cifra fija (R-19). */
    @Serializable
    @SerialName("payTax")
    data class PayTax(val percent: Boolean) : Action

    /** Usar la carta «Salir libre de la Cárcel» (R-18, R-22). */
    @Serializable
    @SerialName("useJailCard")
    data object UseJailCard : Action

    /**
     * Negocio entre dos jugadores (R-29, R-33, R-51): `from` da `giveSquares`, `giveMoney`
     * y `giveJailCards`; `to` da `takeSquares` y `takeMoney`.
     */
    @Serializable
    @SerialName("trade")
    data class Trade(
        val from: Int,
        val to: Int,
        val giveSquares: List<Int> = emptyList(),
        val takeSquares: List<Int> = emptyList(),
        val giveMoney: Int = 0,
        val takeMoney: Int = 0,
        val giveJailCards: Int = 0,
    ) : Action

    /** Declararse en quiebra, si ni vendiendo ni hipotecando alcanza (R-34, R-35). */
    @Serializable
    @SerialName("declareBankruptcy")
    data object DeclareBankruptcy : Action

    /** Se acabó el tiempo del juego con límite: gana el más rico (R-40). Lo manda la app. */
    @Serializable
    @SerialName("timeUp")
    data object TimeUp : Action

    /** Terminar el turno. */
    @Serializable
    @SerialName("endTurn")
    data object EndTurn : Action
}
