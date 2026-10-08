package com.jacck.mono.game

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.minimumBid
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.PlayerDebt
import com.jacck.mono.engine.model.Tax
import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** El letrero del turno (FD.4, D-69, D-70) en el Clásico: Ana (0) y Beto (1), le toca a Ana. */
class TurnBarTest {

    private val config = Preset.CLASSIC.load()
    private val start = Engine.newGame(config, listOf("Ana", "Beto"), seed = 1).state.copy(current = 0)
    private fun GameState.money(player: Int, amount: Int) =
        copy(players = players.mapIndexed { k, p -> if (k == player) p.copy(money = amount) else p })
    private fun kinds(bar: TurnBar?) = bar!!.buttons.map { it.kind }

    @Test
    fun `FD-4 antes de tirar, Tirar los dados, y al terminar, Terminar turno`() {
        val roll = turnBar(config, start)!!
        assertEquals(BarTitle.Turn(0), roll.title)
        assertEquals(listOf(BarButton(BarKind.ROLL, Action.Roll)), roll.buttons)
        assertEquals(listOf(BarKind.END_TURN), kinds(turnBar(config, start.copy(phase = TurnPhase.EndOfTurn))))
    }

    @Test
    fun `FD-4 en una casilla del Banco, Comprar con su precio y No comprar en ocre`() {
        val price = (config.squares[1] as OwnableSquare).price
        val bar = turnBar(config, start.copy(phase = TurnPhase.Buy(1)))!!
        assertEquals(BarTitle.Buy(1), bar.title)
        assertEquals(
            listOf(BarButton(BarKind.BUY, Action.Buy, price), BarButton(BarKind.DECLINE, Action.Decline, principal = false)),
            bar.buttons,
        )
        // Sin dinero, el motor no deja comprar: el botón sale apagado.
        assertFalse(turnBar(config, start.copy(phase = TurnPhase.Buy(1)).money(0, price - 1))!!.buttons[0].enabled)
    }

    @Test
    fun `FD-4 en la subasta, tres pujas y Pasar para quien tiene el turno de pujar`() {
        val base = minimumBid(config, 1)
        val auction = TurnPhase.Auction(1, listOf(1, 0), highestBid = 0, highestBidder = null)
        val bar = turnBar(config, start.copy(phase = auction))!!
        assertEquals(BarTitle.Auction(1, 1), bar.title)
        assertEquals(listOf(base, base + 10, base + 50, null), bar.buttons.map { it.amount })
        assertEquals(Action.Bid(1, base), bar.buttons[0].action)
        assertEquals(Action.PassBid(1), bar.buttons.last().action)
        // Con una puja de Beto, le toca a Ana y las pujas suben 10, 50 y 100.
        val led = turnBar(config, start.copy(phase = auction.copy(highestBid = 80, highestBidder = 1)))!!
        assertEquals(BarTitle.Auction(1, 0), led.title)
        assertEquals(listOf(90, 130, 180, null), led.buttons.map { it.amount })
    }

    @Test
    fun `FD-4 en la Carcel, tirar dobles y la multa, y la carta solo si la tiene`() {
        val jailed = start.copy(players = start.players.mapIndexed { k, p -> if (k == 0) p.copy(jailTurns = 0) else p })
        val bar = turnBar(config, jailed)!!
        assertEquals(BarTitle.Jail(0, 1, config.rules.jailMaxTurns), bar.title)
        assertEquals(listOf(BarKind.JAIL_ROLL, BarKind.JAIL_FINE), kinds(bar))
        assertEquals(config.rules.jailFine, bar.buttons[1].amount)
        val card = config.cards.indices.first()
        val withCard = jailed.copy(players = jailed.players.mapIndexed { k, p -> if (k == 0) p.copy(jailCards = listOf(card)) else p })
        assertEquals(listOf(BarKind.JAIL_ROLL, BarKind.JAIL_FINE, BarKind.JAIL_CARD), kinds(turnBar(config, withCard)))
    }

    @Test
    fun `FD-4 en un impuesto con porcentaje, la cifra fija o el porcentaje`() {
        val square = config.squares.indexOfFirst { it is Tax && it.percent > 0 }
        val tax = config.squares[square] as Tax
        val bar = turnBar(config, start.copy(phase = TurnPhase.TaxChoice(square)))!!
        assertEquals(BarTitle.Tax(square), bar.title)
        assertEquals(listOf(tax.fixed, tax.percent), bar.buttons.map { it.amount })
        assertEquals(listOf(Action.PayTax(percent = false), Action.PayTax(percent = true)), bar.buttons.map { it.action })
    }

    @Test
    fun `FD-4 en una deuda, Vender o hipotecar si tiene que, y Quebrar`() {
        val debt = TurnPhase.Debt(listOf(PlayerDebt(1, 0, 40)), TurnPhase.EndOfTurn)
        val owing = start.copy(phase = debt).money(1, -40)
        val withLand = turnBar(config, owing.copy(holdings = mapOf(1 to Holding(1))))!!
        assertEquals(BarTitle.Debt(1), withLand.title)
        assertEquals(listOf(BarKind.SELL_OR_MORTGAGE, BarKind.BANKRUPTCY), kinds(withLand))
        assertTrue(withLand.buttons[0].enabled)
        // Sin nada que vender, solo queda quebrar: ese es el principal.
        val bare = turnBar(config, owing)!!
        assertFalse(bare.buttons[0].enabled)
        assertTrue(bare.buttons[1].principal)
        assertTrue(canManage(owing, 1, localTurn = true))
        assertFalse(canManage(owing, 0, localTurn = true))
    }

    @Test
    fun `FD-4 la maquina juega sin botones, despues espera Seguir, y el otro telefono se espera`() {
        assertEquals(TurnBar(BarTitle.Machine(0), emptyList()), turnBar(config, start, bots = setOf(0)))
        assertEquals(TurnBar(BarTitle.MachinePlayed(0), listOf(BarButton(BarKind.NEXT))), turnBar(config, start, bots = setOf(0), held = 0))
        assertEquals(TurnBar(BarTitle.Waiting(0), emptyList()), turnBar(config, start, remote = setOf(0)))
        assertNull(turnBar(config, start.copy(phase = TurnPhase.Over(listOf(0)))))
    }
}
