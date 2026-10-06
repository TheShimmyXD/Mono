package com.jacck.mono.engine

import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.RuleOptions
import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/** Subasta (F2.6) en `propertyBoard`: Ana cae en Rojo 2 (3, $60) y no la compra. */
class AuctionTest {

    private fun declined(rules: RuleOptions = testRules()) =
        propertyBoard(rules).let { config ->
            config to Engine.apply(config, twoPlayers(config, position = 3, phase = TurnPhase.Buy(3)), Action.Decline).state
        }

    @Test
    fun `R-12 si no la compra se subasta y puede ganarla quien la rechazo`() {
        val (config, auction) = declined()
        assertEquals(TurnPhase.Auction(3, listOf(0, 1), highestBid = 0, highestBidder = null), auction.phase)
        val anaBids = Engine.apply(config, auction, Action.Bid(0, 10)).state
        val betoBids = Engine.apply(config, anaBids, Action.Bid(1, 25)).state
        val anaAgain = Engine.apply(config, betoBids, Action.Bid(0, 40)).state
        val (done, events) = Engine.apply(config, anaAgain, Action.PassBid(1))
        assertEquals(Holding(0), done.holdings[3])
        assertEquals(listOf(1460, 1500), done.players.map { it.money })
        assertEquals(TurnPhase.EndOfTurn, done.phase)
        assertEquals(Event.AuctionWon(0, 3, 40), events.last())
    }

    @Test
    fun `R-12 la subasta empieza en cualquier precio y cada puja supera la anterior`() {
        val (config, auction) = declined()
        val one = Engine.apply(config, auction, Action.Bid(1, 1)).state
        assertThrows<IllegalActionException> { Engine.apply(config, one, Action.Bid(0, 1)) }
        assertThrows<IllegalActionException> { Engine.apply(config, one, Action.PassBid(1)) }
        assertThrows<IllegalActionException> { Engine.apply(config, one, Action.Bid(0, 1501)) }
    }

    @Test
    fun `R-44 Tio Rico subasta con base en el precio menos $200`() {
        val config = propertyBoard(tioRicoRules())
        val expensive = config.copy(squares = config.squares.toMutableList().also {
            it[3] = (it[3] as Property).copy(price = 1000)
        })
        val auction = Engine.apply(expensive, twoPlayers(expensive, position = 3, phase = TurnPhase.Buy(3)), Action.Decline).state
        assertThrows<IllegalActionException> { Engine.apply(expensive, auction, Action.Bid(1, 799)) }
        val bid = Engine.apply(expensive, auction, Action.Bid(1, 800)).state
        val done = Engine.apply(expensive, bid, Action.PassBid(0)).state
        assertEquals(Holding(1), done.holdings[3])
        assertEquals(26400 - 800, done.players[1].money)
    }
}
