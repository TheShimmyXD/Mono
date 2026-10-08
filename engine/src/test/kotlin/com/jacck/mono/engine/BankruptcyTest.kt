package com.jacck.mono.engine

import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.EndCondition
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.PlayerDebt
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Deudas, quiebra y fin de la partida (F2.6) en `propertyBoard`: rojo 1, 3 (precio 60, casa y
 * hotel 50, hipoteca 30); Azul 3 en 9 (hipoteca 55); Tren 1 en 5 y Tren 2 en 15 (200, hipoteca 100).
 */
class BankruptcyTest {

    private val config = propertyBoard()

    /** Ana (0) juega; Ana debe `owed` a `creditor` y tiene `money`; `bankrupt` ya quebraron. */
    private fun inDebt(
        config: GameConfig,
        count: Int,
        money: Int,
        creditor: Int?,
        owed: Int,
        holdings: Map<Int, Holding>,
        bankrupt: Set<Int> = emptySet(),
    ): GameState {
        val names = listOf("Ana", "Beto", "Cata", "Dani").take(count)
        val state = Engine.newGame(config, names, seed = 1L).state
        val players = state.players.mapIndexed { i, p ->
            if (i == 0) p.copy(money = money) else p.copy(bankrupt = i in bankrupt)
        }
        val phase = TurnPhase.Debt(listOf(PlayerDebt(0, creditor, owed)), TurnPhase.EndOfTurn)
        return state.copy(players = players, current = 0, holdings = holdings, phase = phase)
    }

    private val redHouses = mapOf(1 to Holding(0, houses = 1), 3 to Holding(0, houses = 1), 9 to Holding(0, mortgaged = true))

    @Test
    fun `R-34 sin alcanzar queda en deuda y sale hipotecando`() {
        // Ana con $10 cae en Tren 1 de Beto: alquiler 25 → −15.
        val state = twoPlayers(config, mapOf(5 to Holding(1), 1 to Holding(0)))
        val poor = state.copy(players = state.players.toMutableList().also { it[0] = it[0].copy(money = 10) })
        val (owing, events) = Engine.roll(config, poor, Dice(2, 3))
        assertEquals(-15, owing.players[0].money)
        assertEquals(TurnPhase.Debt(listOf(PlayerDebt(0, 1, 25)), TurnPhase.EndOfTurn), owing.phase)
        assertTrue(Event.InDebt(0, 1) in events)
        // Hipotecando Rojo 1 junta 30: al hipotecar sale de la deuda (quebrar también puede, D-56).
        assertThrows<IllegalActionException> { Engine.apply(config, owing, Action.EndTurn) }
        val (paid, _) = Engine.apply(config, owing, Action.Mortgage(1))
        assertEquals(15, paid.players[0].money)
        assertEquals(TurnPhase.EndOfTurn, paid.phase)
    }

    @Test
    fun `R-34 quiebra ante otro jugador - edificios a la mitad e hipotecadas con 10 %`() {
        // Ana: −200 tras pagar 300 a Beto; junta como mucho 25 + 25 + 30 + 30 = 110.
        val state = inDebt(config, 3, money = -200, creditor = 1, owed = 300, holdings = redHouses)
            .let { s -> s.copy(players = s.players.toMutableList().also { it[0] = it[0].copy(jailCards = listOf(0)) }) }
        val (after, events) = Engine.apply(config, state, Action.DeclareBankruptcy)
        // Beto: 1500 + 50 de las casas − 6 de interés (55 × 10 % = 5,5 ↑) − 200 del saldo de Ana.
        assertEquals(1344, after.players[1].money)
        assertEquals(mapOf(1 to Holding(1), 3 to Holding(1), 9 to Holding(1, mortgaged = true, feePaid = true)), after.holdings)
        assertEquals(state.bankHouses + 2, after.bankHouses)
        assertEquals(listOf(0), after.players[1].jailCards)
        assertTrue(after.players[0].bankrupt)
        assertEquals(0, after.players[0].money)
        // Quebró quien jugaba: el turno pasa a Beto.
        assertEquals(listOf(Event.Bankrupt(0, 1), Event.TurnPassed(1)), events)
        assertEquals(1, after.current)
        assertEquals(TurnPhase.Roll, after.phase)
    }

    @Test
    fun `R-34 la hipotecada recibida se levanta sin interes solo en ese turno`() {
        val state = inDebt(config, 3, money = -200, creditor = 1, owed = 300, holdings = redHouses)
        val (received, _) = Engine.apply(config, state, Action.DeclareBankruptcy)
        val (now, _) = Engine.apply(config, received, Action.Unmortgage(9))
        assertEquals(received.players[1].money - 55, now.players[1].money)
        // Si la deja para un turno posterior paga el interés otra vez: 55 + 6.
        val later = received.copy(holdings = received.holdings + (9 to Holding(1, mortgaged = true)))
        val (paid, _) = Engine.apply(config, later, Action.Unmortgage(9))
        assertEquals(received.players[1].money - 61, paid.players[1].money)
    }

    @Test
    fun `R-35 quiebra ante el Banco - edificios al Banco y subasta de lo demas`() {
        val state = inDebt(config, 3, money = -200, creditor = null, owed = 0, holdings = redHouses)
        val (after, events) = Engine.apply(config, state, Action.DeclareBankruptcy)
        assertEquals(state.bankHouses + 2, after.bankHouses)
        assertEquals(emptyMap<Int, Holding>(), after.holdings)
        assertEquals(TurnPhase.Auction(1, listOf(1, 2), 0, null, queue = listOf(3, 9), then = TurnPhase.Roll), after.phase)
        assertEquals(listOf(Event.Bankrupt(0, null), Event.TurnPassed(1), Event.AuctionStarted(1, 1)), events)
        // Beto gana Rojo 1 por 40 y sigue Rojo 2.
        val (bid, _) = Engine.apply(config, after, Action.Bid(1, 40))
        val (won, _) = Engine.apply(config, bid, Action.PassBid(2))
        assertEquals(Holding(1), won.holdings[1])
        assertEquals(3, (won.phase as TurnPhase.Auction).square)
    }

    @Test
    fun `D-56 en su turno quiebra cuando quiere - todo al Banco y subasta`() {
        // Ana, sin deber nada y con $500, antes de tirar.
        val start = inDebt(config, 3, money = 500, creditor = null, owed = 0, holdings = redHouses)
        val state = start.copy(phase = TurnPhase.Roll)
        val (after, events) = Engine.apply(config, state, Action.DeclareBankruptcy)
        assertTrue(after.players[0].bankrupt)
        assertEquals(0, after.players[0].money)
        assertEquals(state.bankHouses + 2, after.bankHouses)
        assertEquals(TurnPhase.Auction(1, listOf(1, 2), 0, null, queue = listOf(3, 9), then = TurnPhase.Roll), after.phase)
        assertEquals(listOf(Event.Bankrupt(0, null), Event.TurnPassed(1), Event.AuctionStarted(1, 1)), events)
        // También después de tirar.
        val (late, _) = Engine.apply(config, state.copy(phase = TurnPhase.EndOfTurn), Action.DeclareBankruptcy)
        assertTrue(late.players[0].bankrupt)
    }

    @Test
    fun `D-56 en deuda quiebra aunque alcance hipotecando - todo al acreedor`() {
        // Ana debe 25 a Beto con −15; hipotecando Rojo 1 juntaría 30.
        val state = inDebt(config, 3, money = -15, creditor = 1, owed = 25, holdings = mapOf(1 to Holding(0)))
        val (after, events) = Engine.apply(config, state, Action.DeclareBankruptcy)
        assertEquals(Holding(1), after.holdings[1])
        assertEquals(1500 - 15, after.players[1].money)
        assertEquals(listOf(Event.Bankrupt(0, 1), Event.TurnPassed(1)), events)
    }

    @Test
    fun `D-56 la quiebra voluntaria con dos jugadores termina la partida y no vale en turno ajeno`() {
        val state = twoPlayers(config, mapOf(1 to Holding(0)))
        val (after, events) = Engine.apply(config, state, Action.DeclareBankruptcy)
        assertEquals(TurnPhase.Over(listOf(1)), after.phase)
        assertEquals(listOf(Event.Bankrupt(0, null), Event.GameOver(listOf(1))), events)
        // En la subasta puja cada uno, no es el turno de nadie: no se quiebra.
        val auction = state.copy(phase = TurnPhase.Auction(1, listOf(0, 1), 0, null))
        assertThrows<IllegalActionException> { Engine.apply(config, auction, Action.DeclareBankruptcy) }
    }

    @Test
    fun `R-35 gana el ultimo jugador que queda`() {
        val state = inDebt(config, 2, money = -200, creditor = null, owed = 0, holdings = redHouses)
        val (after, events) = Engine.apply(config, state, Action.DeclareBankruptcy)
        assertEquals(TurnPhase.Over(listOf(1)), after.phase)
        assertEquals(Event.GameOver(listOf(1)), events.last())
        assertThrows<IllegalActionException> { Engine.apply(config, after, Action.Roll) }
    }

    @Test
    fun `R-37 cada jugador recibe y paga dos Escrituras al empezar`() {
        val short = propertyBoard(testRules().copy(endCondition = EndCondition.SECOND_BANKRUPTCY, startingDeeds = 2))
        val (state, events) = Engine.newGame(short, listOf("Ana", "Beto", "Cata"), seed = 7L)
        val dealt = events.filterIsInstance<Event.DeedDealt>()
        assertEquals(6, dealt.size)
        assertEquals(6, dealt.map { it.square }.toSet().size)
        for (player in 0..2) {
            val mine = dealt.filter { it.player == player }
            assertEquals(2, mine.size)
            assertEquals(1500 - mine.sumOf { (short.squares[it.square] as OwnableSquare).price }, state.players[player].money)
            assertTrue(mine.all { state.holdings[it.square] == Holding(player) })
        }
        assertEquals(state, Engine.newGame(short, listOf("Ana", "Beto", "Cata"), seed = 7L).state)
    }

    @Test
    fun `R-38 en el juego corto el hotel va sobre tres casas y vuelve a la mitad`() {
        val short = propertyBoard(testRules().copy(endCondition = EndCondition.SECOND_BANKRUPTCY, maxHouses = 3))
        val state = twoPlayers(short, mapOf(1 to Holding(0, houses = 3), 3 to Holding(0, houses = 3)))
        val (after, events) = Engine.apply(short, state, Action.Build(1))
        assertEquals(Holding(0, hotel = true), after.holdings[1])
        assertEquals(state.bankHouses + 3, after.bankHouses)
        assertEquals(Event.HotelBuilt(0, 1, 50), events.single())
        // Devolución: la mitad de hotel + 3 casas = (50 + 150) / 2.
        assertEquals(100, buildingsRefund(short, short.squares[1] as Property, after.holdings.getValue(1)))
    }

    @Test
    fun `R-39 en la segunda quiebra todo pasa entero y gana el mas rico`() {
        val short = propertyBoard(testRules().copy(endCondition = EndCondition.SECOND_BANKRUPTCY))
        // Dani ya quebró. Ana (−400, debe 500 a Beto) junta como mucho 125 + 125 + 30 + 30 = 310.
        val hotels = mapOf(1 to Holding(0, hotel = true), 3 to Holding(0, hotel = true), 15 to Holding(2, mortgaged = true))
        val state = inDebt(short, 4, money = -400, creditor = 1, owed = 500, holdings = hotels, bankrupt = setOf(3))
        val (after, events) = Engine.apply(short, state, Action.DeclareBankruptcy)
        assertEquals(Holding(1, hotel = true), after.holdings[1])
        assertEquals(state.bankHotels, after.bankHotels)
        // Beto: 1500 − 400 + 2 × (60 + 50 + 4 × 50) = 1720. Cata: 1500 + 200 / 2 = 1600.
        assertEquals(1720, wealth(short, after, 1))
        assertEquals(1600, wealth(short, after, 2))
        assertEquals(TurnPhase.Over(listOf(1)), after.phase)
        assertEquals(listOf(Event.Bankrupt(0, 1), Event.GameOver(listOf(1))), events)
    }

    @Test
    fun `R-40 al acabar el tiempo gana el mas rico y el empate lo desempata el efectivo`() {
        val timed = propertyBoard(testRules().copy(endCondition = EndCondition.TIME_LIMIT))
        // Ana 1500 en efectivo; Beto 1440 + Rojo 1 (60) = 1500: empatan y Ana tiene más efectivo.
        val base = twoPlayers(timed, mapOf(1 to Holding(1)))
        val tied = base.copy(players = base.players.toMutableList().also { it[1] = it[1].copy(money = 1440) })
        assertEquals(TurnPhase.Over(listOf(0)), Engine.apply(timed, tied, Action.TimeUp).state.phase)
        // Mismo patrimonio y mismo efectivo: ganan los dos (D-15).
        val equal = twoPlayers(timed)
        assertEquals(TurnPhase.Over(listOf(0, 1)), Engine.apply(timed, equal, Action.TimeUp).state.phase)
    }

    @Test
    fun `R-40 sin limite de tiempo no se acaba por tiempo`() {
        assertThrows<IllegalActionException> { Engine.apply(config, twoPlayers(config), Action.TimeUp) }
    }
}
