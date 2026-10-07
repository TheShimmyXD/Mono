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
import com.jacck.mono.engine.validate
import com.jacck.mono.engine.withSquare

/**
 * El editor de casillas (F4.1, D-39): guarda el tablero que se edita y la ficha de la casilla
 * elegida. Una casilla solo se guarda si el validador del motor no encuentra errores nuevos (F2.7).
 */
class EditorViewModel(start: GameConfig) : ViewModel() {
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
