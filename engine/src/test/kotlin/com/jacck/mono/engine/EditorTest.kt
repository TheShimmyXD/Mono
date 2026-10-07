package com.jacck.mono.engine

import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.Property
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/** Editor de casillas y reglas (F4.1, F4.2; D-39, D-40) en `propertyBoard`: lo editado es lo que cobra el motor. */
class EditorTest {

    private val board = propertyBoard()
    private val red2 = board.squares[3] as Property

    @Test
    fun `R-13 una casilla editada cobra su alquiler nuevo`() {
        val edited = board.withSquare(3, red2.copy(name = "Las Cruces", rents = listOf(50, 100, 200, 300, 400, 500)))
        val result = Engine.roll(edited, twoPlayers(edited, mapOf(3 to Holding(1))), Dice(1, 2))
        assertEquals(listOf(1450, 1550), result.state.players.map { it.money })
        assertTrue(Event.RentPaid(0, 1, 3, 50) in result.events)
    }

    @Test
    fun `R-11 una casilla editada se compra a su precio nuevo`() {
        val edited = board.withSquare(3, red2.copy(price = 90))
        val landed = Engine.roll(edited, twoPlayers(edited), Dice(1, 2)).state
        val bought = Engine.apply(edited, landed, Action.Buy)
        assertEquals(listOf(Event.Bought(0, 3, 90)), bought.events)
    }

    @Test
    fun `withSquare cambia solo esa casilla y guarda la clave del dibujo`() {
        val edited = board.withSquare(3, red2.copy(name = "Otra", art = "rojo_2"))
        assertEquals(board.squares.filterIndexed { i, _ -> i != 3 }, edited.squares.filterIndexed { i, _ -> i != 3 })
        assertEquals("rojo_2", edited.squares[3].art)
        assertEquals("Rojo 2", board.squares[3].name)
    }

    @Test
    fun `D-09 una edicion fuera de rango la marca el validador`() {
        val edited = board.withSquare(3, red2.copy(name = "", rents = listOf(4, 20)))
        val errors = validate(edited)
        assertTrue(ConfigError.NameLength(3) in errors)
        assertTrue(errors.any { it is ConfigError.RentCount && it.square == 3 })
    }

    @Test
    fun `R-10 el Clasico con el salario editado cobra el nuevo al pasar por la salida`() {
        val clasico = Preset.CLASSIC.load()
        val edited = clasico.withRules(clasico.rules.copy(salary = 300))
        val result = Engine.roll(edited, twoPlayers(edited, position = 38), Dice(1, 2))
        assertEquals(1, result.state.players[0].position)
        assertEquals(1800, result.state.players[0].money)
        assertEquals(listOf(Event.SalaryPaid(0, 300)), result.events.filterIsInstance<Event.SalaryPaid>())
        assertEquals(clasico.squares, edited.squares)
    }

    @Test
    fun `D-09 una regla editada fuera de rango la marca el validador`() {
        val clasico = Preset.CLASSIC.load()
        val edited = clasico.withRules(clasico.rules.copy(salary = 100_000, maxHouses = 5))
        val errors = validate(edited)
        assertTrue(errors.any { it is ConfigError.RuleRange && it.field == RuleField.SALARY })
        assertTrue(errors.any { it is ConfigError.RuleRange && it.field == RuleField.MAX_HOUSES })
    }

    /** El Clásico de 24: sin las cuatro últimas casillas de cada lado (6..9, 16..19, 26..29, 36..39). */
    private fun clasico24() = (39 downTo 1).filter { it % 10 >= 6 }.fold(Preset.CLASSIC.load()) { c, i -> c.withSquareRemoved(i) }

    /** Cada carta «Avance hasta…» nombra la casilla a la que lleva («Viaje hasta la Estación…»). */
    private fun cardsNameTheirSquare(config: com.jacck.mono.engine.model.GameConfig) = config.cards.all { c ->
        val move = c.effect as? com.jacck.mono.engine.model.CardEffect.MoveTo ?: return@all true
        config.squares[move.square].name in c.text
    }

    @Test
    fun `F4-3 el Clasico de 24 casillas es valido y se juega hasta la quiebra`() {
        val board = clasico24()
        assertEquals(24, board.squares.size)
        assertEquals(emptyList<ConfigError>(), validate(board))
        assertEquals(listOf("marron", "rosado", "rojo", "verde"), board.groups.map { it.id })
        // Sin tratos (el motor no tiene `Trade`) casi nadie junta un grupo: de 40 partidas de 1500
        // turnos terminan 13 (con 6000, 14; medido en F4.3). Jugable sí: ninguna invariante se rompe.
        val results = (0 until 40).map { simulate(board, players = 2 + it % 3, seed = it.toLong(), maxTurns = 1500) }
        println("F4.3 Clásico 24: terminan ${results.count { it.over }} de 40, turnos medios ${results.filter { it.over }.map { it.turns }.average().toInt()}")
        assertTrue(results.count { it.over && it.bankruptcies >= 1 } >= 10, "terminan ${results.count { it.over }} de 40")
    }

    @Test
    fun `F4-3 al quitar casillas las cartas siguen a la suya y la de Zona T sale del mazo`() {
        val clasico = Preset.CLASSIC.load()
        val board = clasico24()
        assertTrue(cardsNameTheirSquare(clasico) && cardsNameTheirSquare(board))
        assertEquals(clasico.cards.size - 1, board.cards.size)
        assertTrue(board.cards.none { "Zona T" in it.text })
    }

    @Test
    fun `R-10 en el Clasico de 24 se cobra el salario al pasar por la salida`() {
        val board = clasico24()
        val result = Engine.roll(board, twoPlayers(board, position = 22), Dice(1, 2))
        assertEquals(1, result.state.players[0].position)
        assertEquals(listOf(Event.SalaryPaid(0, 200)), result.events.filterIsInstance<Event.SalaryPaid>())
    }

    @Test
    fun `F4-3 insertar corre las casillas y las cartas y el tamano invalido lo marca el validador`() {
        val clasico = Preset.CLASSIC.load()
        val added = clasico.withSquareAdded(1, red2.copy(group = "marron", name = "Casilla nueva"))
        assertEquals(41, added.squares.size)
        assertEquals("Casilla nueva", added.squares[1].name)
        assertEquals(clasico.squares.drop(1), added.squares.drop(2))
        assertTrue(cardsNameTheirSquare(added))
        assertTrue(ConfigError.BoardSize(41) in validate(added))
        assertEquals(clasico, added.withSquareRemoved(1))
    }

    @Test
    fun `F4-3 la salida no se quita ni se inserta antes de ella`() {
        assertThrows<IllegalArgumentException> { board.withSquareRemoved(0) }
        assertThrows<IllegalArgumentException> { board.withSquareAdded(0, red2) }
        assertThrows<IllegalArgumentException> { board.withSquareRemoved(16) }
    }

    @Test
    fun `withSquare fuera del tablero es un error`() {
        assertThrows<IllegalArgumentException> { board.withSquare(16, red2) }
    }
}
