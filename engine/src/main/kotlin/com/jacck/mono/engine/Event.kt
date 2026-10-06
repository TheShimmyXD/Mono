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

    /** Sacó dobles y vuelve a tirar (R-09, R-43). */
    data class RollAgain(val player: Int) : Event

    /** El turno pasó a `player` (R-06). */
    data class TurnPassed(val player: Int) : Event
}
