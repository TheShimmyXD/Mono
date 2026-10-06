package com.jacck.mono.engine

import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.Rounding
import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Impuesto de Monopoly (R-19) en `jailBoard` y las Tierras de Tío Rico (R-52..R-54). */
class TaxTest {

    private val config = jailBoard()

    @Test
    fun `R-19 elige pagar $200 y, con dobles, vuelve a tirar`() {
        val (choice, _) = Engine.roll(config, twoPlayers(config), Dice(1, 1))
        assertEquals(TurnPhase.TaxChoice(2), choice.phase)
        assertEquals(1500, choice.players[0].money)
        val (paid, events) = Engine.apply(config, choice, Action.PayTax(percent = false))
        assertEquals(1300, paid.players[0].money)
        assertTrue(Event.TaxPaid(0, 2, 200) in events)
        assertEquals(TurnPhase.Roll, paid.phase)
    }

    @Test
    fun `R-19 elige el 10 % del patrimonio, redondeado hacia arriba (D-08)`() {
        // Efectivo 1505 + Rojo 1 hipotecada 60 + Rojo 2 60 con hotel (50 + 4 casas de 50)
        // + Azul 1 100 con 2 casas de 50 = 2075 → 10 % = 207,5 → $208.
        val holdings = mapOf(
            1 to Holding(0, mortgaged = true),
            3 to Holding(0, hotel = true),
            6 to Holding(0, houses = 2),
        )
        val start = twoPlayers(config, holdings).let { s ->
            s.copy(players = s.players.toMutableList().also { it[0] = it[0].copy(money = 1505) })
        }
        assertEquals(2075, netWorth(config, start, 0))
        val (choice, _) = Engine.roll(config, start, Dice(1, 1))
        val (paid, _) = Engine.apply(config, choice, Action.PayTax(percent = true))
        assertEquals(1505 - 208, paid.players[0].money)
        assertEquals(207, percentOf(2075, 10, Rounding.DOWN))
        assertEquals(208, percentOf(2075, 10, Rounding.NEAREST))
    }

    @Test
    fun `R-52 Tierra del Futuro cobra $1500 mas $200 por castillo`() {
        val tierras = tierrasBoard()
        val castles = mapOf(1 to Holding(0, hotel = true), 3 to Holding(0, hotel = true), 6 to Holding(0, houses = 3))
        val (after, events) = Engine.roll(tierras, twoPlayers(tierras, castles), Dice(1, 1))
        assertEquals(26400 - 1500 - 2 * 200, after.players[0].money)
        assertTrue(Event.TaxPaid(0, 2, 1900) in events)
    }

    @Test
    fun `R-53 Tierra de la Aventura cobra $1800`() {
        val tierras = tierrasBoard()
        val (after, _) = Engine.roll(tierras, twoPlayers(tierras, position = 1), Dice(1, 2))
        assertEquals(4, after.players[0].position)
        assertEquals(26400 - 1800, after.players[0].money)
        assertEquals(TurnPhase.EndOfTurn, after.phase)
    }

    @Test
    fun `R-54 Tierra de la Frontera cobra $2000`() {
        val tierras = tierrasBoard()
        val (after, _) = Engine.roll(tierras, twoPlayers(tierras, position = 7), Dice(1, 2))
        assertEquals(10, after.players[0].position)
        assertEquals(26400 - 2000, after.players[0].money)
    }
}
