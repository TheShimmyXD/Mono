package com.jacck.mono.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.BotonChiva
import com.jacck.mono.Calcomania
import com.jacck.mono.Chiva
import com.jacck.mono.PantallaChiva
import com.jacck.mono.board.Board
import com.jacck.mono.board.PlayersRow
import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset

/**
 * Maquetas de la pantalla que se está diseñando (M-029), con datos falsos; se abren con el extra
 * `maqueta`. Ahora: la botonera del turno (FD.4, D-67), bajo los íconos y a todo el ancho del centro,
 * en 4 situaciones (tirar, comprar, subasta, «Seguir»): A franja roja de una pieza en segmentos,
 * B botones de chiva en fila, C letrero blanco con los botones dentro.
 */
@Composable
fun Maqueta(letra: String) {
    val config = remember { Preset.CLASSIC.load() }
    val state = remember { Engine.newGame(config, listOf("Andrés", "Botty 2"), 7).state }
    // Cada situación: su nombre y sus botones (texto, principal).
    val situaciones = listOf(
        "Tirar" to listOf("Tirar los dados" to true),
        "Comprar" to listOf("Comprar −$220" to true, "No comprar" to false),
        "Subasta" to listOf("$220" to true, "$230" to true, "$270" to true, "Pasar" to false),
        "Máquina" to listOf("Seguir" to true),
    )
    PantallaChiva {
        Board(config, state, Modifier.fillMaxSize().padding(4.dp), highlight = null) {
            Column(Modifier.fillMaxSize()) {
                PlayersRow(state, showMoney = true)
                Text("Opción $letra", Modifier.padding(top = 4.dp), fontSize = 18.sp)
                Column(Modifier.fillMaxWidth().padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    situaciones.forEach { (nombre, botones) ->
                        Text(nombre, fontSize = 12.sp, color = Chiva.Tinta.copy(alpha = 0.6f))
                        when (letra) {
                            "A" -> FranjaA(botones)
                            "B" -> FilaB(botones)
                            else -> LetreroC(nombre, botones)
                        }
                    }
                }
            }
        }
    }
}

/** A: una franja roja de una pieza, partida en segmentos con una raya; el secundario, en ocre. */
@Composable
private fun FranjaA(botones: List<Pair<String, Boolean>>) {
    val forma = RoundedCornerShape(10.dp)
    Box(Modifier.fillMaxWidth().height(48.dp)) {
        Box(Modifier.fillMaxSize().offset(3.dp, 3.dp).background(Chiva.Tinta, forma))
        Row(Modifier.fillMaxSize().clip(forma).border(2.dp, Chiva.Tinta, forma)) {
            botones.forEachIndexed { i, (texto, principal) ->
                if (i > 0) Box(Modifier.width(2.dp).fillMaxHeight().background(Chiva.Tinta))
                Segmento(texto, principal)
            }
        }
    }
}

@Composable
private fun RowScope.Segmento(texto: String, principal: Boolean) {
    Box(
        Modifier.weight(if (texto.length > 6) 2f else 1f).fillMaxHeight().background(if (principal) Chiva.Techo else Chiva.Ocre),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            texto, color = if (principal) Color.White else Chiva.Tinta, maxLines = 1, softWrap = false,
            autoSize = TextAutoSize.StepBased(minFontSize = 11.sp, maxFontSize = 17.sp), modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

/** B: los botones de chiva de hoy, uno al lado del otro, cada uno con su sombra. */
@Composable
private fun FilaB(botones: List<Pair<String, Boolean>>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        botones.forEach { (texto, principal) ->
            BotonChiva(texto, {}, Modifier.weight(if (texto.length > 6) 2f else 1f), principal = principal)
        }
    }
}

/** C: un letrero blanco como el del turno, con lo que toca arriba y los botones dentro. */
@Composable
private fun LetreroC(nombre: String, botones: List<Pair<String, Boolean>>) {
    val titulo = when (nombre) {
        "Tirar" -> "Te toca, Andrés"
        "Comprar" -> "¿Compras Calle 19?"
        "Subasta" -> "Subasta de Calle 19: puja Andrés"
        else -> "Botty 2 jugó"
    }
    Calcomania {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(titulo, fontSize = 15.sp, maxLines = 1)
            FilaB(botones)
        }
    }
}
