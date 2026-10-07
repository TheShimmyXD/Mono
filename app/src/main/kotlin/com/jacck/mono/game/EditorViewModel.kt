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
import com.jacck.mono.engine.withSquareAdded
import com.jacck.mono.engine.withSquareRemoved

/**
 * El editor de casillas, tamaño y reglas (F4.1..F4.3; D-39..D-41): guarda el tablero que se edita, la
 * ficha de la casilla elegida y las reglas escritas. Quitar y añadir casillas se aplica al instante,
 * aunque el tablero quede inválido: sus errores ([boardErrors]) se ven en rojo y el menú no deja empezar. Una casilla o las reglas solo se guardan si el
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

    /** Los errores del tablero entero (F4.3): no vacío, no se puede jugar. */
    val boardErrors: List<ConfigError> get() = validate(config)

    /** Tipo de la casilla que añade «Añadir después» (F4.3, D-41). */
    var newKind: NewKind by mutableStateOf(NewKind.PROPERTY)

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

    /** Quita la casilla elegida y elige la de antes (D-41); la salida y la Cárcel no se quitan. */
    fun remove() {
        val i = selected?.takeIf { removable(config, it) } ?: return
        val name = config.squares[i].name
        config = config.withSquareRemoved(i)
        select(i - 1)
        Log.i(LOG_TAG, "editor: casilla $i quitada ($name): ${config.squares.size} casillas, ${config.cards.size} cartas")
    }

    /** Añade después de la elegida una casilla de tipo [newKind] (la copia de `newSquare`) y la elige. */
    fun add(name: String) {
        val i = selected ?: return
        val square = newSquare(config, newKind, i, name) ?: return
        config = config.withSquareAdded(i + 1, square)
        select(i + 1)
        Log.i(LOG_TAG, "editor: casilla ${i + 1} añadida ($newKind): ${config.squares.size} casillas")
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
