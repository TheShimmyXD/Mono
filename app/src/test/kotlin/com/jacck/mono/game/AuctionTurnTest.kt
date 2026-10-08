package com.jacck.mono.game

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.PlayerDebt
import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AuctionTurnTest {

    private fun auction(bidders: List<Int>, leader: Int? = null) =
        TurnPhase.Auction(square = 6, bidders = bidders, highestBid = if (leader == null) 0 else 50, highestBidder = leader)

    @Test
    fun `sin pujas empieza el primero de la lista`() {
        assertEquals(2, nextBidder(auction(listOf(2, 3, 0, 1))))
    }

    @Test
    fun `después de una puja sigue el de la derecha`() {
        assertEquals(3, nextBidder(auction(listOf(2, 3, 0, 1), leader = 2)))
    }

    @Test
    fun `al final de la lista vuelve al principio`() {
        assertEquals(2, nextBidder(auction(listOf(2, 3, 0, 1), leader = 1)))
    }

    @Test
    fun `quien pasa sale y la ronda sigue con el siguiente`() {
        // 2 puja, 3 pasa (sale de la lista): le toca a 0.
        assertEquals(0, nextBidder(auction(listOf(2, 0, 1), leader = 2)))
    }

    @Test
    fun `F5-4 decide el del turno, el siguiente postor o el primer deudor`() {
        val state = Engine.newGame(Preset.CLASSIC.load(), listOf("Ana", "Beto", "Caro"), seed = 3).state.copy(current = 1)
        assertEquals(1, decider(state))
        assertEquals(0, decider(state.copy(phase = auction(listOf(2, 0, 1), leader = 2))))
        val debts = listOf(PlayerDebt(debtor = 2, creditor = 1, amount = 40), PlayerDebt(debtor = 0, creditor = 1, amount = 40))
        assertEquals(2, decider(state.copy(phase = TurnPhase.Debt(debts, TurnPhase.EndOfTurn))))
    }
}
