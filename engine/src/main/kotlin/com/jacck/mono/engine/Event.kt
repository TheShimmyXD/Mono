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

    /** Sacó dobles y vuelve a tirar (R-09, R-43). */
    data class RollAgain(val player: Int) : Event

    /** El turno pasó a `player` (R-06). */
    data class TurnPassed(val player: Int) : Event
}
