package com.jacck.mono.game

import com.jacck.mono.R
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.EndCondition
import com.jacck.mono.engine.validate
import com.jacck.mono.engine.withRules
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Las filas del editor de reglas (F4.2, D-40): pestañas, − / + y que cada fila escriba su opción. */
class RuleRowsTest {

    private val clasico = Preset.CLASSIC.load().rules
    private val tioRico = Preset.TIO_RICO.load().rules
    private fun number(label: Int) = RULE_ROWS.filterIsInstance<NumberRow>().single { it.label == label }

    @Test
    fun `cada pestana tiene reglas y ninguna se repite`() {
        assertEquals(RuleTab.entries.toSet(), RULE_ROWS.map { it.tab }.toSet())
        assertEquals(RULE_ROWS.size, RULE_ROWS.map { it.label }.toSet().size)
    }

    @Test
    fun `R-10 el salario sube y baja de 50 en 50 bajo $1000`() {
        val salary = number(R.string.rules_salary)
        val up = salary.step(clasico, up = true)
        assertEquals(250, up.salary)
        assertEquals(200, salary.step(up, up = false).salary)
        assertEquals(clasico.copy(salary = 250), up)
    }

    @Test
    fun `el paso del dinero crece con la cifra`() {
        val money = number(R.string.rules_starting_money)
        assertEquals(listOf(60, 300, 1100, 27_400), listOf(50, 250, 1000, 26_400).map { money.step(it, up = true) })
        assertEquals(listOf(40, 950, 9900, 10_000), listOf(50, 1000, 10_000, 11_000).map { money.step(it, up = false) })
        assertEquals(0, money.step(0, up = false))
        assertEquals(99_999, money.step(99_999, up = true))
    }

    @Test
    fun `R-09 los dobles a la Carcel saltan de nunca a 2`() {
        val doubles = number(R.string.rules_doubles_jail)
        assertEquals(listOf(2, 0, 0, 4, 5), listOf(3, 2, 0, 3, 5).zip(listOf(false, false, false, true, true)).map { (v, up) -> doubles.step(v, up) })
    }

    @Test
    fun `R-22 los turnos en la Carcel no salen de 1 a 5`() {
        val turns = number(R.string.rules_jail_turns)
        assertEquals(1, turns.step(1, up = false))
        assertEquals(5, turns.step(5, up = true))
    }

    @Test
    fun `R-25 el precio fijo de la casa se apaga y se enciende`() {
        val house = number(R.string.rules_house_price)
        val optional = requireNotNull(house.optional)
        assertNull(house.get(clasico))
        val fixed = house.set(clasico, optional.fallback)
        assertEquals(100, fixed.housePrice)
        assertEquals(150, house.step(fixed, up = true).housePrice)
        assertEquals(clasico, house.step(clasico, up = true))
        assertEquals(1000, house.get(tioRico))
    }

    @Test
    fun `R-39 elegir segunda quiebra cambia como termina`() {
        val end = RULE_ROWS.filterIsInstance<ChoiceRow>().single { it.label == R.string.rules_end }
        assertEquals(listOf(true, false), end.choices.map { it.isOn(clasico) })
        val second = end.choices[1].apply(clasico)
        assertEquals(EndCondition.SECOND_BANKRUPTCY, second.endCondition)
        assertEquals(listOf(false, true), end.choices.map { it.isOn(second) })
    }

    @Test
    fun `cada fila escribe solo su opcion y deja los presets validos`() {
        for (base in listOf(clasico, tioRico)) for (row in RULE_ROWS) {
            when (row) {
                is NumberRow -> assertEquals(base, row.set(base, row.get(base)), "fila ${row.label}")
                is ChoiceRow -> {
                    assertEquals(1, row.choices.count { it.isOn(base) }, "fila ${row.label}")
                    row.choices.forEach { c -> assertTrue(c.isOn(c.apply(base))) }
                }
            }
        }
        val config = Preset.CLASSIC.load()
        assertEquals(validate(config), validate(config.withRules(number(R.string.rules_salary).step(clasico, up = true))))
    }
}
