package com.jacck.mono.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.PantallaChiva
import com.jacck.mono.board.Board
import com.jacck.mono.board.PlayerColors
import com.jacck.mono.board.freeSquares
import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.Holding

/**
 * Maquetas de la pantalla que se está diseñando (M-029), con datos falsos; se abren con el extra
 * `maqueta`. FH.1 (D-90): Ana elegida, sus casillas en su color y las libres en blanco, de tres formas:
 * A marco blanco con contorno de tinta, B lo demás atenuado, C la casilla libre lavada de blanco.
 */
@Composable
fun Maqueta(letra: String) {
    val look = letra.last().uppercaseChar().takeIf { it in "ABC" } ?: 'A'
    val config = remember { Preset.CLASSIC.load() }
    val state = remember {
        Engine.newGame(config, listOf("Ana", "Beto", "Caro", "Dani"), 1L).state.copy(
            holdings = mapOf(
                6 to Holding(0), 8 to Holding(0, houses = 1), 9 to Holding(0), 25 to Holding(0),
                1 to Holding(1), 3 to Holding(1), 21 to Holding(2), 23 to Holding(2, houses = 2), 24 to Holding(2),
                12 to Holding(3), 37 to Holding(3, mortgaged = true),
            ),
        )
    }
    val title = mapOf('A' to "A · Marco blanco", 'B' to "B · Lo demás atenuado", 'C' to "C · Libres lavadas de blanco")
    PantallaChiva {
        Board(
            config, state, Modifier.fillMaxSize().padding(4.dp), highlight = null,
            marks = state.holdings.filterValues { it.owner == 0 }.keys, markColor = PlayerColors[0],
            free = freeSquares(config, state), freeLook = look,
        ) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title.getValue(look), fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("Ana elegida: sus casillas en su color; las del Banco, en blanco", fontSize = 15.sp, textAlign = TextAlign.Center)
            }
        }
    }
}
