package com.jacck.mono.game

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jacck.mono.LOG_TAG
import com.jacck.mono.board.artKey
import com.jacck.mono.engine.ConfigError
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.RuleOptions
import com.jacck.mono.engine.validate
import com.jacck.mono.engine.withRules
import com.jacck.mono.engine.withSquare

/**
 * El editor de casillas y reglas (F4.1, F4.2; D-39, D-40): guarda el tablero que se edita, la ficha
 * de la casilla elegida y las reglas escritas. Una casilla o las reglas solo se guardan si el
 * validador del motor no encuentra errores nuevos (F2.7). [preset] son las reglas de la caja, para
 * marcar «antes $200» en lo cambiado.
 */
class EditorViewModel(start: GameConfig, val preset: RuleOptions = start.rules) : ViewModel() {
    var config: GameConfig by mutableStateOf(start)
        private set
    var selected: Int? by mutableStateOf(null)
        private set
    var draft: SquareDraft? by mutableStateOf(null)
        private set

    /** Errores del último «Guardar»: los del validador que la edición añade. */
    var errors: List<ConfigError> by mutableStateOf(emptyList())
        private set

    /** El último «Guardar» tenía una cifra vacía. */
    var blank: Boolean by mutableStateOf(false)
        private set

    /** Hay algo escrito que no se ha guardado. */
    val changed: Boolean get() = selected?.let { draft != SquareDraft.of(config.squares[it]) } ?: false

    /** Pestaña «Reglas» (true) o «Casillas» del editor, y el tema elegido dentro de «Reglas». */
    var showRules: Boolean by mutableStateOf(false)
    var ruleTab: RuleTab by mutableStateOf(RuleTab.DINERO)

    /** Las reglas escritas (con − / + y Sí / No), aún sin guardar. */
    var rules: RuleOptions by mutableStateOf(start.rules)
        private set

    /** Errores del último «Guardar reglas». */
    var ruleErrors: List<ConfigError> by mutableStateOf(emptyList())
        private set

    val rulesChanged: Boolean get() = rules != config.rules

    fun editRules(next: RuleOptions) {
        rules = next
    }

    fun saveRules() {
        val next = config.withRules(rules)
        ruleErrors = validate(next) - validate(config).toSet()
        if (ruleErrors.isEmpty()) {
            config = next
            Log.i(LOG_TAG, "editor: reglas guardadas: salario ${rules.salary}, dinero inicial ${rules.startingMoney}, cambiadas ${changedRules(preset, rules)}")
        }
    }

    fun select(square: Int) {
        selected = square
        draft = SquareDraft.of(config.squares[square])
        errors = emptyList()
        blank = false
    }

    fun edit(next: SquareDraft) {
        draft = next
    }

    fun save() {
        val i = selected ?: return
        val square = draft?.applyTo(config.squares[i], ::artKey)
        blank = square == null
        if (square == null) return
        val next = config.withSquare(i, square)
        errors = validate(next) - validate(config).toSet()
        if (errors.isEmpty()) {
            config = next
            // La ficha queda como lo guardado («050» → «50», espacios fuera): «Guardar» se apaga.
            draft = SquareDraft.of(square)
            Log.i(LOG_TAG, "editor: casilla $i guardada: $square")
        }
    }
}

/** Cuántas filas del editor de reglas difieren entre [a] y [b] (para el log). */
fun changedRules(a: RuleOptions, b: RuleOptions): Int = RULE_ROWS.count { row ->
    when (row) {
        is NumberRow -> row.get(a) != row.get(b)
        is ChoiceRow -> row.choices.any { it.isOn(a) != it.isOn(b) }
    }
}
