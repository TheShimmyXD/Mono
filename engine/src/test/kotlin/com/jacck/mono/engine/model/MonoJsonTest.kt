package com.jacck.mono.engine.model

import com.jacck.mono.engine.GameRandom
import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/** Ida y vuelta JSON del modelo (F2.1, D-10): lo que se guarda es lo que se carga. */
class MonoJsonTest {

    // Opciones con los valores de la columna «Tío Rico» de `## Diferencias` (salvo el
    // dinero inicial, pendiente en D-08): ejercitan los campos null y los no nulos.
    private val rules = RuleOptions(
        minPlayers = 2, maxPlayers = 6, startingMoney = 15000, salary = 2000,
        startTieRule = StartTieRule.REROLL_TIED, doublesRollAgain = true, doublesToJail = 0,
        jail = false, jailFine = 0, jailMaxTurns = 3, auctionPriceDiscount = 200,
        unsoldStaysWithBank = true, rentWhileMortgaged = true,
        mortgageValue = MortgageValue.HALF_TOTAL, mortgageWithBuildings = true,
        unmortgageFee = Fee(fixed = 200), percentRounding = Rounding.UP,
        buildOnlyWhenLanding = true, maxHouses = 3, evenBuild = false, housePrice = 1000,
        hotelPrice = 2000, hotelReturnsHouses = true, houseStock = 30, hotelStock = 10,
        groupDoubleRent = true, hotelGroupDoubleRent = true, sellBuildingsToBank = true,
        tradeBuildings = true, tradeFee = 200, mortgagedTradeInterest = 0,
        bankUnlimited = true, rentMustBeClaimed = false, freeParkingPot = false,
        endCondition = EndCondition.LAST_STANDING, startingDeeds = 0,
    )

    // Un tablero pequeño con los 9 tipos de casilla; las cifras son de prueba, no de un preset.
    private val config = GameConfig(
        name = "Prueba",
        groups = listOf(ColorGroup("rojo", "Rojo", "#D32F2F")),
        squares = listOf(
            Start("Salida"),
            Property("Uno", "rojo", price = 600, rents = listOf(20, 100, 300, 900, 1600)),
            CardSquare("Lotería", Deck.A),
            Property("Dos", "rojo", 800, listOf(40, 200, 600, 1800, 2500), housePrice = 500, mortgage = 300),
            Tax("Tierra del Futuro", fixed = 1500, perHotel = 200),
            Station("Tren", 2000, listOf(250, 500, 1000, 2000)),
            Utility("Luz", 1500, listOf(4, 10)),
            Jail("Cárcel"),
            GoToJail("Váyase a la Cárcel"),
            Rest("Parada Libre"),
        ),
        rules = rules,
    )

    private val state = GameState(
        players = listOf(
            PlayerState("Ana", money = 13400, position = 3),
            PlayerState("Beto", money = 9000, position = 7, jailTurns = 1, jailCards = 1),
            PlayerState("Caro", money = 0, position = 0, bankrupt = true),
        ),
        holdings = mapOf(1 to Holding(owner = 0, houses = 2), 3 to Holding(0, hotel = true, mortgaged = true)),
        current = 1,
        phase = TurnPhase.Auction(square = 5, bidders = listOf(0, 1), highestBid = 1900, highestBidder = 0),
        doublesInRow = 2,
        bankHouses = 28,
        bankHotels = 9,
        pot = 0,
        turn = 41,
        random = GameRandom(-7L),
    )

    @Test
    fun `F2-1 ida y vuelta de un tablero`() {
        val text = MonoJson.encodeConfig(config)
        val back = MonoJson.decodeConfig(text)
        assertEquals(config, back)
        assertEquals(text, MonoJson.encodeConfig(back))
        assertTrue(text.contains("\"type\": \"goToJail\""), "cada casilla lleva su tipo")
    }

    @Test
    fun `F2-1 ida y vuelta de una partida`() {
        val text = MonoJson.encodeState(state)
        val back = MonoJson.decodeState(text)
        assertEquals(state, back)
        assertEquals(text, MonoJson.encodeState(back))
        assertEquals(state.random.nextInt(6), back.random.nextInt(6), "el azar sigue igual")
    }

    @Test
    fun `F2-1 ida y vuelta de cada fase y cada accion`() {
        val phases = listOf(TurnPhase.Roll, TurnPhase.Buy(3), TurnPhase.TaxChoice(2), TurnPhase.EndOfTurn, TurnPhase.Over(0))
        for (phase in phases) {
            val withPhase = state.copy(phase = phase)
            assertEquals(withPhase, MonoJson.decodeState(MonoJson.encodeState(withPhase)))
        }
        val actions = listOf(
            Action.Roll, Action.Buy, Action.Decline, Action.Bid(1, 2100), Action.PassBid(0),
            Action.Build(1), Action.SellBuilding(1), Action.Mortgage(3), Action.Unmortgage(3),
            Action.PayJailFine, Action.UseJailCard, Action.PayTax(percent = true), Action.Trade(0, 1, giveSquares = listOf(1), takeMoney = 900),
            Action.EndTurn,
        )
        for (action in actions) {
            assertEquals(action, MonoJson.decodeAction(MonoJson.encodeAction(action)))
        }
    }

    @Test
    fun `F2-1 una clave desconocida es un error`() {
        val text = MonoJson.encodeAction(Action.Build(1)).replace("\"square\"", "\"casilla\"")
        assertThrows<SerializationException> { MonoJson.decodeAction(text) }
    }
}
