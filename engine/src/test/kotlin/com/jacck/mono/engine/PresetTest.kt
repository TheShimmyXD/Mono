package com.jacck.mono.engine

import com.jacck.mono.engine.model.CardEffect
import com.jacck.mono.engine.model.CardSquare
import com.jacck.mono.engine.model.Deck
import com.jacck.mono.engine.model.EndCondition
import com.jacck.mono.engine.model.Fee
import com.jacck.mono.engine.model.GoToJail
import com.jacck.mono.engine.model.Jail
import com.jacck.mono.engine.model.MonoJson
import com.jacck.mono.engine.model.MortgageValue
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Rounding
import com.jacck.mono.engine.model.RuleOptions
import com.jacck.mono.engine.model.Start
import com.jacck.mono.engine.model.StartTieRule
import com.jacck.mono.engine.model.Station
import com.jacck.mono.engine.model.Tax
import com.jacck.mono.engine.model.Utility
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

/**
 * Presets (F2.8, D-17, D-18): cada opción con el valor de su fila de `REGLAS.md`
 * `## Diferencias` y su R-##; las casillas que fija un reglamento, con la suya.
 */
class PresetTest {

    private val classic = Preset.CLASSIC.load()
    private val tioRico = Preset.TIO_RICO.load()

    /** Fila de `## Diferencias`: su número, sus R-##, la opción y el valor en cada preset. */
    private class Row(val row: Int, val rules: String, val classic: Any?, val tioRico: Any?, val get: (RuleOptions) -> Any?)

    private val rows = listOf(
        Row(1, "R-03 R-42", 1500, 26_400) { it.startingMoney },
        Row(2, "R-10 R-45", 200, 2000) { it.salary },
        Row(4, "R-06 R-43", StartTieRule.REROLL_TIED, StartTieRule.REROLL_TIED) { it.startTieRule },
        Row(5, "R-09 R-43", true, true) { it.doublesRollAgain },
        Row(5, "R-09 R-43", 3, 0) { it.doublesToJail },
        Row(6, "R-20 R-23", true, false) { it.jail },
        Row(7, "R-22", 50, 0) { it.jailFine },
        Row(7, "R-22", 3, 3) { it.jailMaxTurns },
        Row(8, "R-12 R-44", null, 200) { it.auctionPriceDiscount },
        Row(9, "R-12", true, true) { it.unsoldStaysWithBank },
        Row(10, "R-14 R-49", false, true) { it.rentWhileMortgaged },
        Row(11, "R-31 R-49", MortgageValue.PRINTED, MortgageValue.HALF_TOTAL) { it.mortgageValue },
        Row(12, "R-31 R-49", false, true) { it.mortgageWithBuildings },
        Row(13, "R-32 R-50", Fee(percent = 10), Fee(fixed = 200)) { it.unmortgageFee },
        Row(14, "R-32", Rounding.UP, Rounding.UP) { it.percentRounding },
        Row(15, "R-25 R-46", false, true) { it.buildOnlyWhenLanding },
        Row(16, "R-26 R-46", 4, 3) { it.maxHouses },
        Row(17, "R-26 R-46", true, false) { it.evenBuild },
        Row(18, "R-25 R-46", null, 1000) { it.housePrice },
        Row(19, "R-27 R-47", null, 2000) { it.hotelPrice },
        Row(19, "R-27 R-47", true, true) { it.hotelReturnsHouses },
        Row(20, "R-02 R-28 R-41", 32, 30) { it.houseStock },
        Row(20, "R-02 R-28 R-41", 12, 10) { it.hotelStock },
        Row(21, "R-16", true, true) { it.groupDoubleRent },
        Row(22, "R-48", false, true) { it.hotelGroupDoubleRent },
        Row(23, "R-30", true, true) { it.sellBuildingsToBank },
        Row(24, "R-29 R-51", false, true) { it.tradeBuildings },
        Row(25, "R-51", 0, 200) { it.tradeFee },
        Row(26, "R-33 R-51", 10, 0) { it.mortgagedTradeInterest },
        Row(27, "R-05", true, true) { it.bankUnlimited },
        Row(28, "R-17", false, false) { it.rentMustBeClaimed },
        Row(29, "R-24", false, false) { it.freeParkingPot },
        Row(30, "R-35 R-39 R-40", EndCondition.LAST_STANDING, EndCondition.LAST_STANDING) { it.endCondition },
        Row(31, "R-37 R-40", 0, 0) { it.startingDeeds },
        Row(34, "R-02 R-41 D-05", 2, 2) { it.minPlayers },
        Row(34, "R-02 R-41 D-05", 6, 6) { it.maxPlayers },
        Row(35, "R-34 D-15", 10, 10) { it.bankruptcyInterest },
    )

    @TestFactory
    fun `F2-8 cada opcion es la de su fila de Diferencias`() = rows.map { r ->
        DynamicTest.dynamicTest("fila ${r.row} · ${r.rules}") {
            assertEquals(r.classic, r.get(classic.rules), "Clásico, fila ${r.row} (${r.rules})")
            assertEquals(r.tioRico, r.get(tioRico.rules), "Tío Rico, fila ${r.row} (${r.rules})")
        }
    }

    @Test
    fun `F2-8 los presets pasan el validador`() {
        assertEquals(emptyList<ConfigError>(), validate(classic))
        assertEquals(emptyList<ConfigError>(), validate(tioRico))
    }

    @Test
    fun `F2-8 ida y vuelta JSON sin perder nada`() {
        assertEquals(classic, MonoJson.decodeConfig(MonoJson.encodeConfig(classic)))
        assertEquals(tioRico, MonoJson.decodeConfig(MonoJson.encodeConfig(tioRico)))
    }

    @Test
    fun `R-07 R-42 la salida es GO o la Estacion Santa Fe`() {
        assertEquals(Start("Salida"), classic.squares[0])
        assertEquals(Start("Estación Santa Fe"), tioRico.squares[0])
    }

    @Test
    fun `R-19 contribuciones de 200 o 10 por ciento`() {
        assertEquals(Tax("Contribuciones", fixed = 200, percent = 10), classic.squares[4])
    }

    @Test
    fun `R-52 R-53 R-54 las tres Tierras`() {
        val taxes = tioRico.squares.filterIsInstance<Tax>()
        assertEquals(
            listOf(
                Tax("Tierra del Futuro", fixed = 1500, perHotel = 200),
                Tax("Tierra de la Aventura", fixed = 1800),
                Tax("Tierra de la Frontera", fixed = 2000),
            ),
            taxes,
        )
    }

    @Test
    fun `R-41 Tio Rico 32 titulos 22 tarjetas y sin Carcel`() {
        assertEquals(44, tioRico.squares.size)
        assertEquals(32, tioRico.squares.count { it is OwnableSquare })
        assertEquals(22, tioRico.cards.size)
        assertTrue(tioRico.squares.none { it is Jail || it is GoToJail })
    }

    @Test
    fun `R-02 R-18 Clasico 40 casillas 28 titulos y Salir libre en cada mazo`() {
        assertEquals(40, classic.squares.size)
        assertEquals(listOf(22, 4, 2), listOf(classic.squares.count { it is Property }, classic.squares.count { it is Station }, classic.squares.count { it is Utility }))
        for (deck in Deck.entries) {
            assertEquals(1, classic.cards.count { it.deck == deck && it.effect == CardEffect.GetOutOfJail })
            assertTrue(classic.squares.any { it is CardSquare && it.deck == deck })
        }
    }

    @Test
    fun `D-18 cada preset arranca una partida de 6 con su dinero inicial`() {
        for (config in listOf(classic, tioRico)) {
            val state = Engine.newGame(config, List(6) { "J$it" }, seed = 7L).state
            assertEquals(List(6) { config.rules.startingMoney }, state.players.map { it.money })
        }
    }
}
