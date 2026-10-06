package com.jacck.mono.engine

import com.jacck.mono.engine.model.ColorGroup
import com.jacck.mono.engine.model.EndCondition
import com.jacck.mono.engine.model.Fee
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.GoToJail
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.Jail
import com.jacck.mono.engine.model.MortgageValue
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Rest
import com.jacck.mono.engine.model.Rounding
import com.jacck.mono.engine.model.RuleOptions
import com.jacck.mono.engine.model.Square
import com.jacck.mono.engine.model.Start
import com.jacck.mono.engine.model.StartTieRule
import com.jacck.mono.engine.model.Station
import com.jacck.mono.engine.model.Tax
import com.jacck.mono.engine.model.TurnPhase
import com.jacck.mono.engine.model.Utility

/**
 * Opciones con los valores de la columna «Clásico» de `REGLAS.md` `## Diferencias`; cada
 * prueba cambia solo lo que mira. No son el preset (F2.8).
 */
fun testRules(
    startingMoney: Int = 1500,
    salary: Int = 200,
    startTieRule: StartTieRule = StartTieRule.REROLL_TIED,
    doublesRollAgain: Boolean = true,
    doublesToJail: Int = 3,
) = RuleOptions(
    minPlayers = 2, maxPlayers = 6, startingMoney = startingMoney, salary = salary,
    startTieRule = startTieRule, doublesRollAgain = doublesRollAgain, doublesToJail = doublesToJail,
    jail = true, jailFine = 50, jailMaxTurns = 3, auctionPriceDiscount = null,
    unsoldStaysWithBank = true, rentWhileMortgaged = false,
    mortgageValue = MortgageValue.PRINTED, mortgageWithBuildings = false,
    unmortgageFee = Fee(percent = 10), percentRounding = Rounding.UP,
    buildOnlyWhenLanding = false, maxHouses = 4, evenBuild = true, housePrice = null,
    hotelPrice = null, hotelReturnsHouses = true, houseStock = 32, hotelStock = 12,
    groupDoubleRent = true, hotelGroupDoubleRent = false, sellBuildingsToBank = true,
    tradeBuildings = false, tradeFee = 0, mortgagedTradeInterest = 10,
    bankUnlimited = true, rentMustBeClaimed = false, freeParkingPot = false,
    endCondition = EndCondition.LAST_STANDING, startingDeeds = 0,
)

/**
 * Anillo de `size` casillas sin efecto, con la salida en `start` (para mover y cobrar) y, si
 * las reglas tienen Cárcel, la Cárcel a un cuarto de vuelta (de visita no hace nada, R-21).
 */
fun ringBoard(size: Int = 16, start: Int = 0, rules: RuleOptions = testRules()): GameConfig {
    val jail = if (rules.jail) (start + size / 4) % size else -1
    return GameConfig(
        name = "Anillo",
        groups = emptyList(),
        squares = List(size) {
            when (it) {
                start -> Start("Salida")
                jail -> Jail("Cárcel")
                else -> Rest("Casilla $it")
            }
        },
        rules = rules,
    )
}

/** Columna «Tío Rico» de `## Diferencias` para lo de F2.3..F2.6 (sin el preset, F2.8). */
fun tioRicoRules() = testRules(startingMoney = 26400, salary = 2000, doublesToJail = 0).copy(
    jail = false, auctionPriceDiscount = 200, rentWhileMortgaged = true,
    mortgageValue = MortgageValue.HALF_TOTAL, mortgageWithBuildings = true, unmortgageFee = Fee(fixed = 200),
    buildOnlyWhenLanding = true, maxHouses = 3,
    evenBuild = false, housePrice = 1000, hotelPrice = 2000, houseStock = 30, hotelStock = 10,
    hotelGroupDoubleRent = true,
)

/**
 * 16 casillas con dos grupos (rojo: 1, 3; azul: 6, 8, 9), ferrocarriles en 5 y 15 y servicios
 * en 7 y 12; hipoteca impresa = mitad del precio (Azul 3: 55). Cifras de prueba, no de un preset.
 */
fun propertyBoard(rules: RuleOptions = testRules()): GameConfig {
    fun red(name: String, rents: List<Int>) = Property(name, "rojo", 60, rents, housePrice = 50, hotelPrice = 50, mortgage = 30)
    fun blue(name: String, price: Int, rents: List<Int>, mortgage: Int = price / 2) =
        Property(name, "azul", price, rents, housePrice = 50, hotelPrice = 50, mortgage = mortgage)
    val squares = List(16) { Rest("Casilla $it") }.toMutableList<Square>()
    squares[0] = Start("Salida")
    squares[1] = red("Rojo 1", listOf(2, 10, 30, 90, 160, 250))
    squares[3] = red("Rojo 2", listOf(4, 20, 60, 180, 320, 450))
    squares[5] = Station("Tren 1", 200, listOf(25, 50, 100, 200), mortgage = 100)
    squares[6] = blue("Azul 1", 100, listOf(6, 30, 90, 270, 400, 550))
    squares[7] = Utility("Luz", 150, listOf(4, 10), mortgage = 75)
    squares[8] = blue("Azul 2", 100, listOf(6, 30, 90, 270, 400, 550))
    squares[9] = blue("Azul 3", 120, listOf(8, 40, 100, 300, 450, 600), mortgage = 55)
    squares[12] = Utility("Agua", 150, listOf(4, 10), mortgage = 75)
    squares[15] = Station("Tren 2", 200, listOf(25, 50, 100, 200), mortgage = 100)
    return GameConfig(
        name = "Propiedades",
        groups = listOf(ColorGroup("rojo", "Rojo", "#D32F2F"), ColorGroup("azul", "Azul", "#1976D2")),
        squares = squares,
        rules = rules,
    )
}

/**
 * `propertyBoard` con impuesto de $200 o 10 % en 2, Cárcel en 4, Parada Libre en 10 y
 * «Váyase a la Cárcel» en 13 (F2.4).
 */
fun jailBoard(rules: RuleOptions = testRules()): GameConfig = propertyBoard(rules).withSquares(
    2 to Tax("Impuesto", fixed = 200, percent = 10),
    4 to Jail("Cárcel"),
    10 to Rest("Parada Libre"),
    13 to GoToJail("Váyase a la Cárcel"),
)

/** `propertyBoard` de Tío Rico con las tres Tierras en 2, 4 y 10 (R-52..R-54). */
fun tierrasBoard(): GameConfig = propertyBoard(tioRicoRules()).withSquares(
    2 to Tax("Tierra del Futuro", fixed = 1500, perHotel = 200),
    4 to Tax("Tierra de la Aventura", fixed = 1800),
    10 to Tax("Tierra de la Frontera", fixed = 2000),
)

private fun GameConfig.withSquares(vararg changes: Pair<Int, Square>) =
    copy(squares = squares.toMutableList().apply { changes.forEach { (i, sq) -> this[i] = sq } })

/** Partida de Ana (0) y Beto (1) en `config`: juega Ana, en `position`, con esas propiedades. */
fun twoPlayers(
    config: GameConfig,
    holdings: Map<Int, Holding> = emptyMap(),
    position: Int = 0,
    phase: TurnPhase = TurnPhase.Roll,
): GameState {
    val state = Engine.newGame(config, listOf("Ana", "Beto"), seed = 1L).state
    val players = state.players.toMutableList().also { it[0] = it[0].copy(position = position) }
    return state.copy(players = players, current = 0, holdings = holdings, phase = phase)
}
