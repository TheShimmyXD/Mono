package com.jacck.mono.engine

import com.jacck.mono.engine.model.EndCondition
import com.jacck.mono.engine.model.Fee
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.MortgageValue
import com.jacck.mono.engine.model.Rest
import com.jacck.mono.engine.model.Rounding
import com.jacck.mono.engine.model.RuleOptions
import com.jacck.mono.engine.model.Start
import com.jacck.mono.engine.model.StartTieRule

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

/** Anillo de `size` casillas sin efecto, con la salida en `start` (para mover y cobrar). */
fun ringBoard(size: Int = 16, start: Int = 0, rules: RuleOptions = testRules()) = GameConfig(
    name = "Anillo",
    groups = emptyList(),
    squares = List(size) { if (it == start) Start("Salida") else Rest("Casilla $it") },
    rules = rules,
)
