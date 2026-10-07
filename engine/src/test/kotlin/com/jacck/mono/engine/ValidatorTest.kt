package com.jacck.mono.engine

import com.jacck.mono.engine.model.Card
import com.jacck.mono.engine.model.CardEffect
import com.jacck.mono.engine.model.CardSquare
import com.jacck.mono.engine.model.ColorGroup
import com.jacck.mono.engine.model.Deck
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GoToJail
import com.jacck.mono.engine.model.Jail
import com.jacck.mono.engine.model.MortgageValue
import com.jacck.mono.engine.model.NearestKind
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Rest
import com.jacck.mono.engine.model.Square
import com.jacck.mono.engine.model.Start
import com.jacck.mono.engine.model.Station
import com.jacck.mono.engine.model.Tax
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Validador de la configuración (F2.7, D-09, D-16): una prueba por error. `jailBoard()` es válido:
 * 16 casillas, salida en 0, rojo 1, 3; azul 6, 8, 9; trenes 5 y 15; servicios 7 y 12;
 * impuesto en 2, Cárcel en 4, Parada Libre en 10 y «Váyase a la Cárcel» en 13.
 */
class ValidatorTest {

    private val valid = jailBoard()

    private fun GameConfig.with(vararg changes: Pair<Int, Square>) =
        copy(squares = squares.toMutableList().apply { changes.forEach { (i, sq) -> this[i] = sq } })

    private fun assertHas(expected: ConfigError, config: GameConfig) {
        val errors = validate(config)
        assertTrue(expected in errors, "esperaba $expected en $errors")
    }

    @Test
    fun `F2-7 el tablero de prueba no tiene errores`() {
        assertEquals(emptyList<ConfigError>(), validate(valid))
    }

    @Test
    fun `F2-7 N de 16 a 48 y multiplo de 4`() {
        assertHas(ConfigError.BoardSize(18), valid.copy(squares = valid.squares + Rest("A") + Rest("B")))
        assertHas(ConfigError.BoardSize(52), valid.copy(squares = valid.squares + List(36) { Rest("R$it") }))
        assertEquals(emptyList<ConfigError>(), validate(valid.copy(squares = valid.squares + List(4) { Rest("R$it") })))
    }

    @Test
    fun `F2-7 una sola salida y en la casilla 0`() {
        assertHas(ConfigError.StartMisplaced, valid.with(0 to Rest("Nada"), 10 to Start("Salida")))
        assertHas(ConfigError.StartMisplaced, valid.with(10 to Start("Otra salida")))
    }

    @Test
    fun `F2-7 sin grupos de color`() {
        assertHas(ConfigError.NoGroups, valid.copy(groups = emptyList()))
    }

    @Test
    fun `F2-7 dos grupos con el mismo id`() {
        assertHas(ConfigError.DuplicateGroup("rojo"), valid.copy(groups = valid.groups + ColorGroup("rojo", "Otro rojo", "#FF0000")))
    }

    @Test
    fun `F2-7 grupo vacio o de mas de 4`() {
        assertHas(ConfigError.GroupSize("verde", 0), valid.copy(groups = valid.groups + ColorGroup("verde", "Verde", "#388E3C")))
        val blue = valid.squares[6] as Property
        assertHas(ConfigError.GroupSize("azul", 5), valid.with(10 to blue.copy(name = "Azul 4"), 11 to blue.copy(name = "Azul 5")))
    }

    @Test
    fun `F2-7 propiedad de un grupo que no existe`() {
        assertHas(ConfigError.UnknownGroup(1, "gris"), valid.with(1 to (valid.squares[1] as Property).copy(group = "gris")))
    }

    @Test
    fun `F2-7 nombre de 1 a 24 caracteres`() {
        assertHas(ConfigError.NameLength(10), valid.with(10 to Rest("")))
        assertHas(ConfigError.NameLength(11), valid.with(11 to Rest("x".repeat(25))))
    }

    @Test
    fun `F2-7 precios negativos y porcentajes de mas`() {
        assertHas(ConfigError.SquareAmount(1, SquareField.PRICE), valid.with(1 to (valid.squares[1] as Property).copy(price = -1)))
        assertHas(ConfigError.SquareAmount(3, SquareField.RENT), valid.with(3 to (valid.squares[3] as Property).copy(rents = listOf(4, 20, 60, 180, 320, 100_000))))
        assertHas(ConfigError.SquareAmount(2, SquareField.TAX_PERCENT), valid.with(2 to Tax("Impuesto", fixed = 200, percent = 101)))
    }

    @Test
    fun `F2-7 falta la cifra que la regla deja a cada casilla`() {
        val noMortgage = valid.with(5 to (valid.squares[5] as Station).copy(mortgage = null))
        assertHas(ConfigError.MissingAmount(5, SquareField.MORTGAGE), noMortgage)
        // Con la hipoteca = mitad del total (R-49) no hace falta.
        val half = noMortgage.copy(rules = noMortgage.rules.copy(mortgageValue = MortgageValue.HALF_TOTAL))
        assertEquals(emptyList<ConfigError>(), validate(half))
        assertHas(ConfigError.MissingAmount(6, SquareField.HOUSE_PRICE), valid.with(6 to (valid.squares[6] as Property).copy(housePrice = null)))
    }

    @Test
    fun `F2-7 alquileres de mas o de menos`() {
        // Con 3 casas máximo, un solar lleva 5 alquileres (base, 1..3 casas, hotel).
        assertHas(ConfigError.RentCount(1, 5, 6), valid.copy(rules = valid.rules.copy(maxHouses = 3)))
        // Dos trenes en el tablero: cada uno necesita al menos 2 alquileres.
        assertHas(ConfigError.RentCount(5, 2, 1), valid.with(5 to Station("Tren 1", 200, listOf(25), mortgage = 100)))
    }

    @Test
    fun `F2-7 opciones fuera de rango`() {
        fun rules(change: (com.jacck.mono.engine.model.RuleOptions) -> com.jacck.mono.engine.model.RuleOptions) = valid.copy(rules = change(valid.rules))
        assertHas(ConfigError.RuleRange(RuleField.SALARY, 0, 99_999), rules { it.copy(salary = -1) })
        assertHas(ConfigError.RuleRange(RuleField.DOUBLES_TO_JAIL, 2, 5), rules { it.copy(doublesToJail = 1) })
        assertHas(ConfigError.RuleRange(RuleField.MAX_HOUSES, 1, 4), rules { it.copy(maxHouses = 5) })
        assertHas(ConfigError.RuleRange(RuleField.PLAYERS, 2, 6), rules { it.copy(maxPlayers = 7) })
        assertHas(ConfigError.RuleRange(RuleField.BANKRUPTCY_INTEREST, 0, 100), rules { it.copy(bankruptcyInterest = 101) })
        // 9 Escrituras para 6 jugadores: alcanza para 1 a cada uno, no para 2.
        assertHas(ConfigError.RuleRange(RuleField.STARTING_DEEDS, 0, 1), rules { it.copy(startingDeeds = 2) })
    }

    @Test
    fun `F2-7 opciones que no casan`() {
        val r = valid.rules
        assertHas(ConfigError.Incoherent(Incoherence.PLAYERS_ORDER), valid.copy(rules = r.copy(minPlayers = 5, maxPlayers = 3)))
        assertHas(ConfigError.Incoherent(Incoherence.RENT_MUST_BE_CLAIMED), valid.copy(rules = r.copy(rentMustBeClaimed = true)))
        assertHas(ConfigError.Incoherent(Incoherence.FREE_PARKING_POT), valid.copy(rules = r.copy(freeParkingPot = true)))
        // Sin Cárcel, con casillas de Cárcel en el tablero.
        assertHas(ConfigError.Incoherent(Incoherence.JAIL_DISABLED_BUT_USED), valid.copy(rules = r.copy(jail = false, doublesToJail = 0)))
        assertHas(ConfigError.Incoherent(Incoherence.JAIL_SQUARE_COUNT), valid.with(4 to Rest("Sin Cárcel"), 13 to Rest("Nada")))
        assertHas(ConfigError.Incoherent(Incoherence.JAIL_SQUARE_COUNT), valid.with(11 to Jail("Otra Cárcel")))
        // Sin Cárcel y sin sus casillas, Tío Rico es válido en esto.
        val noJail = valid.with(4 to Rest("Nada"), 13 to Rest("Nada")).copy(rules = r.copy(jail = false, doublesToJail = 0))
        assertEquals(emptyList<ConfigError>(), validate(noJail))
        assertHas(ConfigError.Incoherent(Incoherence.JAIL_DISABLED_BUT_USED), noJail.with(13 to GoToJail("Váyase")))
    }

    @Test
    fun `F2-7 casilla de carta sin cartas en su mazo`() {
        val cards = valid.with(10 to CardSquare("Casualidad", Deck.A), 11 to CardSquare("Arca", Deck.B))
            .copy(cards = listOf(Card(Deck.A, "Cobre", CardEffect.Collect(50))))
        assertEquals(listOf<ConfigError>(ConfigError.EmptyDeck(11, Deck.B)), validate(cards))
    }

    @Test
    fun `F2-7 cartas imposibles en este tablero`() {
        val board = valid.with(10 to CardSquare("Casualidad", Deck.A))
        fun card(effect: CardEffect) = board.copy(cards = listOf(Card(Deck.A, "Carta", effect)))
        assertHas(ConfigError.BadCard(0, CardProblem.MOVE_OUTSIDE_BOARD), card(CardEffect.MoveTo(16)))
        assertHas(ConfigError.BadCard(0, CardProblem.MOVE_TO_CARD), card(CardEffect.MoveTo(10)))
        assertHas(ConfigError.BadCard(0, CardProblem.AMOUNT), card(CardEffect.Pay(-50)))
        val noStations = board.with(5 to Rest("Nada"), 15 to Rest("Nada"))
        assertHas(ConfigError.BadCard(0, CardProblem.NO_NEAREST), noStations.copy(cards = listOf(Card(Deck.A, "Tren", CardEffect.MoveToNearest(NearestKind.STATION, rentFactor = 2)))))
        assertEquals(emptyList<ConfigError>(), validate(card(CardEffect.MoveToNearest(NearestKind.UTILITY, diceMultiplier = 10))))
    }

    @Test
    fun `R-17 reclamar el alquiler no lo tiene el motor`() {
        assertHas(ConfigError.Incoherent(Incoherence.RENT_MUST_BE_CLAIMED), valid.copy(rules = valid.rules.copy(rentMustBeClaimed = true)))
    }
}
