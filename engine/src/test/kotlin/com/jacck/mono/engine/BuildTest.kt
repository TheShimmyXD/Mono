package com.jacck.mono.engine

import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/** Casas, hoteles y castillos (F2.3) en el grupo rojo de `propertyBoard` (casillas 1 y 3). */
class BuildTest {

    private val classic = propertyBoard()
    private val tioRico = propertyBoard(tioRicoRules())

    private fun build(config: GameConfig, state: GameState, square: Int) =
        Engine.apply(config, state, Action.Build(square)).state

    private fun reds(first: Holding, second: Holding = Holding(0)) = mapOf(1 to first, 3 to second)

    @Test
    fun `R-25 sin el grupo completo no se construye`() {
        val state = twoPlayers(classic, mapOf(1 to Holding(0), 3 to Holding(1)))
        assertThrows<IllegalActionException> { build(classic, state, 1) }
    }

    @Test
    fun `R-25 la casa cuesta lo de la Escritura y sube el alquiler`() {
        val built = build(classic, twoPlayers(classic, reds(Holding(0))), 1)
        assertEquals(1, built.holdings.getValue(1).houses)
        assertEquals(1450, built.players[0].money)
        assertEquals(31, built.bankHouses)
        assertEquals(10, rentDue(classic, built, 1, Dice(1, 2)))
    }

    @Test
    fun `R-26 se construye parejo`() {
        val one = build(classic, twoPlayers(classic, reds(Holding(0))), 1)
        assertThrows<IllegalActionException> { build(classic, one, 1) }
        val two = build(classic, build(classic, one, 3), 1)
        assertEquals(2, two.holdings.getValue(1).houses)
        assertEquals(1, two.holdings.getValue(3).houses)
    }

    @Test
    fun `R-27 hotel con 4 casas en cada solar, devuelve las 4 y uno por solar`() {
        val state = twoPlayers(classic, reds(Holding(0, houses = 4), Holding(0, houses = 4))).copy(bankHouses = 24)
        val hotel = build(classic, state, 1)
        assertEquals(Holding(0, houses = 0, hotel = true), hotel.holdings[1])
        assertEquals(1450, hotel.players[0].money)
        assertEquals(28, hotel.bankHouses)
        assertEquals(11, hotel.bankHotels)
        assertThrows<IllegalActionException> { build(classic, hotel, 1) }
        val uneven = twoPlayers(classic, reds(Holding(0, houses = 4), Holding(0, houses = 3)))
        assertThrows<IllegalActionException> { build(classic, uneven, 1) }
    }

    @Test
    fun `R-28 sin casas en el Banco no se construye`() {
        val state = twoPlayers(classic, reds(Holding(0))).copy(bankHouses = 0)
        assertThrows<IllegalActionException> { build(classic, state, 1) }
    }

    @Test
    fun `D-12 no se construye en una hipotecada ni a mitad de una compra`() {
        val mortgaged = twoPlayers(classic, reds(Holding(0, mortgaged = true)))
        assertThrows<IllegalActionException> { build(classic, mortgaged, 1) }
        val buying = twoPlayers(classic, reds(Holding(0)), position = 6, phase = TurnPhase.Buy(6))
        assertThrows<IllegalActionException> { build(classic, buying, 1) }
    }

    @Test
    fun `R-46 Tio Rico construye sin parejo hasta 3 casas de 1000 solo en la que cayo (D-12)`() {
        val landed = twoPlayers(tioRico, reds(Holding(0)), position = 1, phase = TurnPhase.EndOfTurn)
        var state = landed
        repeat(3) { state = build(tioRico, state, 1) }
        assertEquals(3, state.holdings.getValue(1).houses)
        assertEquals(0, state.holdings.getValue(3).houses)
        assertEquals(26400 - 3000, state.players[0].money)
        assertThrows<IllegalActionException> { build(tioRico, landed, 3) }
        val nextTurn = landed.copy(phase = TurnPhase.Roll, doublesInRow = 0)
        assertThrows<IllegalActionException> { build(tioRico, nextTurn, 1) }
    }

    @Test
    fun `R-47 castillo con las 3 casas de la propia y 2000`() {
        val state = twoPlayers(tioRico, reds(Holding(0, houses = 3)), position = 1, phase = TurnPhase.EndOfTurn)
            .copy(bankHouses = 27)
        val castle = build(tioRico, state, 1)
        assertEquals(Holding(0, houses = 0, hotel = true), castle.holdings[1])
        assertEquals(26400 - 2000, castle.players[0].money)
        assertEquals(30, castle.bankHouses)
        assertEquals(9, castle.bankHotels)
    }
}
