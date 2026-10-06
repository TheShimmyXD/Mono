package com.jacck.mono.engine

/**
 * Lo que pasó al aplicar una acción, en orden: la interfaz lo anima o lo anuncia (con sus
 * textos en `strings.xml`). El motor no devuelve frases.
 */
sealed interface Event {

    /** Una ronda de la tirada inicial: jugador → dados (R-06, R-43). */
    data class StartRolled(val rolls: Map<Int, Dice>) : Event

    /** Quién empieza la partida (R-06, R-43). */
    data class FirstPlayer(val player: Int) : Event

    /** Tirada de quien juega (R-07). */
    data class DiceRolled(val player: Int, val dice: Dice) : Event

    /** La ficha avanzó de `from` a `to`, índices del anillo (R-07). */
    data class Moved(val player: Int, val from: Int, val to: Int) : Event

    /** El Banco pagó el sueldo por caer en la salida o pasar por ella (R-10, R-45). */
    data class SalaryPaid(val player: Int, val amount: Int) : Event

    /** Compró al Banco la casilla `square` (R-11). */
    data class Bought(val player: Int, val square: Int, val price: Int) : Event

    /** No compró; sigue del Banco (D-08) hasta que haya subasta (F2.6). */
    data class Declined(val player: Int, val square: Int) : Event

    /** `payer` pagó a `owner` el alquiler de `square` (R-13..R-16, R-48). */
    data class RentPaid(val payer: Int, val owner: Int, val square: Int, val amount: Int) : Event

    /** Casa número `houses` en `square` (R-25, R-46). */
    data class HouseBuilt(val player: Int, val square: Int, val houses: Int, val price: Int) : Event

    /** Hotel o castillo en `square` (R-27, R-47). */
    data class HotelBuilt(val player: Int, val square: Int, val price: Int) : Event

    /** Pagó al Banco el impuesto de `square` (R-19, R-52..R-54). */
    data class TaxPaid(val player: Int, val square: Int, val amount: Int) : Event

    /** Robó la carta `card` (índice de `GameConfig.cards`) (R-18). */
    data class CardDrawn(val player: Int, val card: Int) : Event

    /** Pago por una carta; `null` es el Banco (D-14). */
    data class CardPayment(val from: Int?, val to: Int?, val amount: Int) : Event

    /** Fue a la Cárcel, sin cobrar el sueldo (R-20). */
    data class SentToJail(val player: Int, val cause: JailCause) : Event

    /** Sigue en la Cárcel; lleva `turns` turnos dentro (R-22). */
    data class StayedInJail(val player: Int, val turns: Int) : Event

    /** Salió de la Cárcel; `fine` es la multa pagada, 0 si no pagó (R-22). */
    data class LeftJail(val player: Int, val way: JailExit, val fine: Int = 0) : Event

    /** Hipotecó `square` y cobró `amount` (R-31, R-49). */
    data class Mortgaged(val player: Int, val square: Int, val amount: Int) : Event

    /** Levantó la hipoteca de `square` pagando `amount` (R-32, R-50). */
    data class Unmortgaged(val player: Int, val square: Int, val amount: Int) : Event

    /** Vendió al Banco una casa o el hotel de `square` y cobró `amount` (R-30). */
    data class BuildingSold(val player: Int, val square: Int, val amount: Int) : Event

    /** Sacó dobles y vuelve a tirar (R-09, R-43). */
    data class RollAgain(val player: Int) : Event

    /** El turno pasó a `player` (R-06). */
    data class TurnPassed(val player: Int) : Event
}

/** Por qué fue a la Cárcel (R-20): la casilla, la carta (F2.5) o los dobles seguidos (R-09). */
enum class JailCause { SQUARE, CARD, DOUBLES }

/** Cómo salió (R-22): dobles, carta, multa antes de tirar o multa forzada en el último turno. */
enum class JailExit { DOUBLES, CARD, FINE, LAST_TURN }
