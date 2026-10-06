package com.jacck.mono.engine

import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.Card
import com.jacck.mono.engine.model.CardEffect
import com.jacck.mono.engine.model.CardSquare
import com.jacck.mono.engine.model.Deck
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.NearestKind
import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Cartas (F2.5): R-18 y los efectos de D-14, en `jailBoard` con un mazo A en 11 y uno B en 14.
 * Cada prueba fija el orden del mazo; Ana parte de 8 y saca 1 + 2 → cae en 11.
 */
class CardTest {

    private fun board(cards: List<Card>): GameConfig {
        val base = jailBoard()
        val squares = base.squares.toMutableList().apply {
            this[11] = CardSquare("Casualidad", Deck.A)
            this[14] = CardSquare("Arca Comunal", Deck.B)
        }
        return base.copy(squares = squares, cards = cards)
    }

    /** Mazo A con `effects` en ese orden, Ana en 8 con `holdings`; devuelve config y estado. */
    private fun deckA(vararg effects: CardEffect, holdings: Map<Int, Holding> = emptyMap()): Pair<GameConfig, GameState> {
        val config = board(effects.mapIndexed { i, e -> Card(Deck.A, "Carta $i", e) })
        val state = twoPlayers(config, holdings, position = 8).copy(decks = mapOf(Deck.A to effects.indices.toList()))
        return config to state
    }

    private fun draw(vararg effects: CardEffect, holdings: Map<Int, Holding> = emptyMap()): Result {
        val (config, state) = deckA(*effects, holdings = holdings)
        return Engine.roll(config, state, Dice(1, 2))
    }

    @Test
    fun `R-18 roba la de encima, cumple lo impreso y la devuelve debajo`() {
        val (after, events) = draw(CardEffect.Collect(10), CardEffect.Pay(20), CardEffect.Collect(30))
        assertEquals(1510, after.players[0].money)
        assertEquals(listOf(1, 2, 0), after.decks[Deck.A])
        assertTrue(Event.CardDrawn(0, 0) in events)
        assertEquals(TurnPhase.EndOfTurn, after.phase)
    }

    @Test
    fun `R-18 Salir libre de la Carcel se guarda y al usarla vuelve debajo`() {
        val (config, state) = deckA(CardEffect.GetOutOfJail, CardEffect.Collect(10))
        val (kept, _) = Engine.roll(config, state, Dice(1, 2))
        assertEquals(listOf(0), kept.players[0].jailCards)
        assertEquals(listOf(1), kept.decks[Deck.A])
        val inJail = kept.copy(
            phase = TurnPhase.Roll,
            players = kept.players.toMutableList().also { it[0] = it[0].copy(position = 4, jailTurns = 0) },
        )
        val (free, _) = Engine.apply(config, inJail, Action.UseJailCard)
        assertEquals(emptyList<Int>(), free.players[0].jailCards)
        assertEquals(listOf(1, 0), free.decks[Deck.A])
    }

    @Test
    fun `R-18 cada mazo se baraja al empezar con la semilla de la partida (D-03)`() {
        val cards = List(10) { Card(Deck.A, "A$it", CardEffect.Collect(it)) } +
            List(6) { Card(Deck.B, "B$it", CardEffect.Pay(it)) }
        val config = board(cards)
        fun decks(seed: Long) = Engine.newGame(config, listOf("Ana", "Beto"), seed).state.decks
        assertEquals(decks(7L), decks(7L))
        assertNotEquals(decks(7L), decks(8L))
        assertEquals((0..9).toSet(), decks(7L).getValue(Deck.A).toSet())
        assertEquals((10..15).toSet(), decks(7L).getValue(Deck.B).toSet())
    }

    @Test
    fun `R-20 la carta Vayase a la Carcel manda directo, sin sueldo`() {
        val (after, events) = draw(CardEffect.GoToJail)
        assertEquals(4, after.players[0].position)
        assertEquals(0, after.players[0].jailTurns)
        assertEquals(1500, after.players[0].money)
        assertEquals(TurnPhase.EndOfTurn, after.phase)
        assertTrue(Event.SentToJail(0, JailCause.CARD) in events)
    }

    @Test
    fun `D-14 paga al Banco`() {
        assertEquals(1485, draw(CardEffect.Pay(15)).state.players[0].money)
    }

    @Test
    fun `D-14 avanzar hasta una casilla cobra el sueldo al pasar la salida y la ofrece`() {
        val (after, _) = draw(CardEffect.MoveTo(1))
        assertEquals(1, after.players[0].position)
        assertEquals(1700, after.players[0].money)
        assertEquals(TurnPhase.Buy(1), after.phase)
    }

    @Test
    fun `D-14 retroceder no cobra el sueldo aunque cruce la salida`() {
        // 11 − 13 = −2 → casilla 14 (de B, mazo vacío: nada).
        val (after, _) = draw(CardEffect.MoveBy(-13))
        assertEquals(14, after.players[0].position)
        assertEquals(1500, after.players[0].money)
    }

    @Test
    fun `D-14 avanzar N cumple la casilla de destino`() {
        // 11 + 2 = 13 «Váyase a la Cárcel».
        val (after, _) = draw(CardEffect.MoveBy(2))
        assertEquals(4, after.players[0].position)
        assertEquals(0, after.players[0].jailTurns)
    }

    @Test
    fun `D-14 reparaciones por casa y por hotel`() {
        val holdings = mapOf(3 to Holding(0, hotel = true), 6 to Holding(0, houses = 2), 9 to Holding(0, houses = 3))
        // 5 casas × 25 + 1 hotel × 100 = 225.
        assertEquals(1500 - 225, draw(CardEffect.Repairs(25, 100), holdings = holdings).state.players[0].money)
    }

    @Test
    fun `D-14 cobrar de cada jugador y pagar a cada uno`() {
        val collected = draw(CardEffect.CollectFromEach(50)).state.players
        assertEquals(listOf(1550, 1450), collected.map { it.money })
        val paid = draw(CardEffect.PayEach(50)).state.players
        assertEquals(listOf(1450, 1550), paid.map { it.money })
    }

    @Test
    fun `D-14 al ferrocarril mas cercano paga el doble del alquiler`() {
        // 11 → Tren 2 (15), de Beto con un solo ferrocarril: 25 × 2.
        val (after, _) = draw(CardEffect.MoveToNearest(NearestKind.STATION, rentFactor = 2), holdings = mapOf(15 to Holding(1)))
        assertEquals(15, after.players[0].position)
        assertEquals(listOf(1450, 1550), after.players.map { it.money })
        val free = draw(CardEffect.MoveToNearest(NearestKind.STATION, rentFactor = 2)).state
        assertEquals(TurnPhase.Buy(15), free.phase)
    }

    @Test
    fun `D-14 al servicio mas cercano tira los dados y paga 10 veces`() {
        val (config, state) = deckA(CardEffect.MoveToNearest(NearestKind.UTILITY, diceMultiplier = 10), holdings = mapOf(12 to Holding(1)))
        val thrown = state.random.rollDice().first
        val (after, _) = Engine.roll(config, state, Dice(1, 2))
        assertEquals(12, after.players[0].position)
        assertEquals(1500 - 10 * thrown.total, after.players[0].money)
        assertEquals(1500 + 10 * thrown.total, after.players[1].money)
    }
}
