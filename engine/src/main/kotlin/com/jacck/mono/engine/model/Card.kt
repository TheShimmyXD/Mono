package com.jacck.mono.engine.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Una carta de un mazo (R-18): `text` es lo que lee el jugador (lo escribe el autor, F2.8) y
 * `effect`, lo que hace el motor. Los efectos son los del juego clásico (D-14).
 */
@Serializable
data class Card(val deck: Deck, val text: String, val effect: CardEffect)

/** Lo que cumple quien roba la carta (D-14). En JSON lleva `"type"`. */
@Serializable
sealed interface CardEffect {

    /** Cobra `amount` del Banco. */
    @Serializable
    @SerialName("collect")
    data class Collect(val amount: Int) : CardEffect

    /** Paga `amount` al Banco. */
    @Serializable
    @SerialName("pay")
    data class Pay(val amount: Int) : CardEffect

    /** Avanza hasta `square` y cumple lo de la casilla; cobra el sueldo si pasa por la salida (R-10). */
    @Serializable
    @SerialName("moveTo")
    data class MoveTo(val square: Int) : CardEffect

    /** Avanza `steps` (o retrocede, si es negativo, sin cobrar el sueldo) y cumple lo de la casilla. */
    @Serializable
    @SerialName("moveBy")
    data class MoveBy(val steps: Int) : CardEffect

    /** «Váyase a la Cárcel» (R-20). */
    @Serializable
    @SerialName("goToJail")
    data object GoToJail : CardEffect

    /** «Salir libre de la Cárcel»: se guarda hasta usarla (R-18, R-22). */
    @Serializable
    @SerialName("getOutOfJail")
    data object GetOutOfJail : CardEffect

    /** Reparaciones: paga al Banco por cada casa y cada hotel o castillo suyo. */
    @Serializable
    @SerialName("repairs")
    data class Repairs(val perHouse: Int, val perHotel: Int) : CardEffect

    /** Cobra `amount` de cada uno de los demás jugadores. */
    @Serializable
    @SerialName("collectFromEach")
    data class CollectFromEach(val amount: Int) : CardEffect

    /** Paga `amount` a cada uno de los demás jugadores. */
    @Serializable
    @SerialName("payEach")
    data class PayEach(val amount: Int) : CardEffect

    /**
     * Avanza al ferrocarril (`STATION`) o servicio (`UTILITY`) más cercano. Si es de otro,
     * paga el alquiler × `rentFactor` (ferrocarril) o tira los dados y paga el total ×
     * `diceMultiplier` (servicio, si no es null). Del Banco, lo puede comprar.
     */
    @Serializable
    @SerialName("moveToNearest")
    data class MoveToNearest(
        val kind: NearestKind,
        val rentFactor: Int = 1,
        val diceMultiplier: Int? = null,
    ) : CardEffect
}

/** Tipo de casilla al que manda `MoveToNearest`. */
@Serializable
enum class NearestKind { STATION, UTILITY }
