package com.jacck.mono.engine

import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.Card
import com.jacck.mono.engine.model.CardEffect
import com.jacck.mono.engine.model.Deck
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/** Cárcel, dobles seguidos y Parada Libre (F2.4) en `jailBoard`: Cárcel en 4, «Váyase» en 13. */
class JailTest {

    private val config = jailBoard().copy(cards = listOf(Card(Deck.A, "Salir libre", CardEffect.GetOutOfJail)))

    /** Ana en la Cárcel con `turns` turnos dentro y `cards` cartas; le toca tirar. */
    private fun jailed(turns: Int = 0, cards: List<Int> = emptyList(), holdings: Map<Int, Holding> = emptyMap()): GameState {
        val state = twoPlayers(config, holdings, position = 4)
        val players = state.players.toMutableList()
        players[0] = players[0].copy(jailTurns = turns, jailCards = cards)
        return state.copy(players = players, decks = emptyMap())
    }

    @Test
    fun `R-09 con el tercer doble seguido no avanza y va a la Carcel`() {
        val state = twoPlayers(config).copy(doublesInRow = 2)
        val (after, events) = Engine.roll(config, state, Dice(3, 3))
        val ana = after.players[0]
        assertEquals(4, ana.position)
        assertEquals(0, ana.jailTurns)
        assertEquals(1500, ana.money)
        assertEquals(TurnPhase.EndOfTurn, after.phase)
        assertTrue(Event.SentToJail(0, JailCause.DOUBLES) in events)
        assertTrue(events.none { it is Event.Moved })
    }

    @Test
    fun `R-20 Vayase a la Carcel no cobra el sueldo y termina el turno aunque haya dobles`() {
        // 11 + 2 = 13 «Váyase»: va a 4 dando la vuelta por la salida, sin cobrar $200.
        val (after, events) = Engine.roll(config, twoPlayers(config, position = 11), Dice(1, 1))
        val ana = after.players[0]
        assertEquals(4, ana.position)
        assertEquals(0, ana.jailTurns)
        assertEquals(1500, ana.money)
        assertEquals(TurnPhase.EndOfTurn, after.phase)
        assertEquals(0, after.doublesInRow)
        assertTrue(events.none { it is Event.RollAgain || it is Event.SalaryPaid })
    }

    @Test
    fun `R-21 caer en la Carcel es de visita y el turno siguiente es normal`() {
        val (visit, _) = Engine.roll(config, twoPlayers(config, position = 1), Dice(1, 2))
        assertEquals(4, visit.players[0].position)
        assertNull(visit.players[0].jailTurns)
        assertEquals(1500, visit.players[0].money)
        val (next, _) = Engine.roll(config, visit.copy(phase = TurnPhase.Roll), Dice(2, 4))
        assertEquals(10, next.players[0].position)
    }

    @Test
    fun `R-22 con dobles sale, avanza lo que marcan y no vuelve a tirar`() {
        val (after, events) = Engine.roll(config, jailed(), Dice(3, 3))
        val ana = after.players[0]
        assertNull(ana.jailTurns)
        assertEquals(10, ana.position)
        assertEquals(1500, ana.money)
        assertEquals(TurnPhase.EndOfTurn, after.phase)
        assertTrue(Event.LeftJail(0, JailExit.DOUBLES) in events)
        assertTrue(events.none { it is Event.RollAgain })
    }

    @Test
    fun `R-22 sin dobles se queda y suma un turno dentro`() {
        val (after, events) = Engine.roll(config, jailed(), Dice(1, 2))
        assertEquals(4, after.players[0].position)
        assertEquals(1, after.players[0].jailTurns)
        assertEquals(TurnPhase.EndOfTurn, after.phase)
        assertTrue(Event.StayedInJail(0, 1) in events)
    }

    @Test
    fun `R-22 paga la multa de $50 antes de tirar y luego tira normal`() {
        val (free, events) = Engine.apply(config, jailed(turns = 1), Action.PayJailFine)
        assertEquals(1450, free.players[0].money)
        assertNull(free.players[0].jailTurns)
        assertEquals(TurnPhase.Roll, free.phase)
        assertEquals(listOf(Event.LeftJail(0, JailExit.FINE, 50)), events)
        // Ya libre, los dobles dan otra tirada (R-09).
        val (after, _) = Engine.roll(config, free, Dice(3, 3))
        assertEquals(10, after.players[0].position)
        assertEquals(TurnPhase.Roll, after.phase)
    }

    @Test
    fun `R-22 en el tercer turno no se paga antes de tirar`() {
        assertThrows<IllegalActionException> { Engine.apply(config, jailed(turns = 2), Action.PayJailFine) }
    }

    @Test
    fun `R-22 en el tercer turno sin dobles paga $50, sale y avanza lo que marco`() {
        val (after, events) = Engine.roll(config, jailed(turns = 2), Dice(2, 4))
        val ana = after.players[0]
        assertNull(ana.jailTurns)
        assertEquals(10, ana.position)
        assertEquals(1450, ana.money)
        assertTrue(Event.LeftJail(0, JailExit.LAST_TURN, 50) in events)
    }

    @Test
    fun `R-22 sale con la carta Salir libre de la Carcel`() {
        val (free, _) = Engine.apply(config, jailed(cards = listOf(0)), Action.UseJailCard)
        val ana = free.players[0]
        assertNull(ana.jailTurns)
        assertEquals(emptyList<Int>(), ana.jailCards)
        assertEquals(1500, ana.money)
        assertThrows<IllegalActionException> { Engine.apply(config, jailed(), Action.UseJailCard) }
    }

    @Test
    fun `R-23 desde la Carcel cobra alquiler y construye`() {
        val blue = mapOf(6 to Holding(0), 8 to Holding(0), 9 to Holding(0))
        // Beto cae en Azul 1 (6): alquiler 6 × 2 por el grupo completo (R-16).
        val bettosTurn = jailed(holdings = blue).let { s ->
            s.copy(current = 1, players = s.players.toMutableList().also { it[1] = it[1].copy(position = 3) })
        }
        val (paid, _) = Engine.roll(config, bettosTurn, Dice(1, 2))
        assertEquals(1512, paid.players[0].money)
        // En su turno, aún dentro, Ana construye una casa de $50 en Azul 1.
        val (built, _) = Engine.apply(config, jailed(holdings = blue), Action.Build(6))
        assertEquals(1, built.holdings.getValue(6).houses)
        assertEquals(1450, built.players[0].money)
        assertEquals(0, built.players[0].jailTurns)
    }

    @Test
    fun `R-24 Parada Libre no da dinero ni nada`() {
        val (after, events) = Engine.roll(config, twoPlayers(config, position = 7), Dice(1, 2))
        assertEquals(10, after.players[0].position)
        assertEquals(1500, after.players[0].money)
        assertEquals(0, after.pot)
        assertEquals(TurnPhase.EndOfTurn, after.phase)
        assertEquals(2, events.size, "solo la tirada y el movimiento")
        assertFalse(after.players[0].jailTurns != null)
    }
}
