package com.jacck.mono.demo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.PantallaChiva
import com.jacck.mono.board.Board
import com.jacck.mono.board.PlayerToken
import com.jacck.mono.board.RingGrid
import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin

/**
 * Maquetas de FC.1 (D-58): tres formas de mover la ficha por el tablero del Clásico, en bucle: saca 7
 * y avanza; después una carta la lleva a la casilla 24. Se abren con el extra `maqueta` (A, B o C):
 * A, saltito por casilla (180 ms); B, se desliza por el borde (110 ms por casilla); C, aparece casilla
 * por casilla sin pasar por en medio (160 ms). Los datos son de una partida nueva (semilla 1).
 */
@Composable
fun Maqueta(letra: String) {
    val config = remember { Preset.CLASSIC.load() }
    val state = remember { Engine.newGame(config, listOf("Ana", "Botty 1"), 1L).state }
    val n = config.squares.size
    val (titulo, msPorCasilla) = when (letra) {
        "B" -> "B · Se desliza por el borde" to 110
        "C" -> "C · Casilla por casilla, sin pasar por en medio" to 160
        else -> "A · Saltito en cada casilla" to 180
    }
    val pos = remember { Animatable(0f) } // casilla, con decimales mientras camina
    var rotulo by remember { mutableStateOf("") }
    var quieta by remember { mutableStateOf(true) }

    suspend fun caminar(desde: Int, pasos: Int) {
        quieta = false
        when (letra) {
            "B" -> pos.animateTo(desde + pasos.toFloat(), tween(msPorCasilla * pasos, easing = FastOutSlowInEasing))
            "C" -> repeat(pasos) { delay(msPorCasilla.toLong()); pos.snapTo(desde + it + 1f) }
            else -> repeat(pasos) { pos.animateTo(desde + it + 1f, tween(msPorCasilla, easing = LinearEasing)) }
        }
        pos.snapTo(((desde + pasos) % n).toFloat())
        quieta = true
    }

    LaunchedEffect(letra) {
        while (true) {
            pos.snapTo(0f)
            rotulo = "Ana va a tirar"
            delay(1200)
            rotulo = "Ana sacó 3 + 4 = 7"
            caminar(0, 7)
            rotulo = "Ana cae en ${config.squares[7].name}"
            delay(1200)
            rotulo = "Carta: avanza hasta ${config.squares[24].name}"
            delay(900)
            caminar(7, 17)
            rotulo = "Ana llega a ${config.squares[24].name}"
            delay(2000)
        }
    }

    PantallaChiva {
        BoxWithConstraints(Modifier.fillMaxSize().padding(4.dp)) {
            val grid = remember(n, maxWidth, maxHeight) { RingGrid.fit(n, maxWidth.value, maxHeight.value) }
            val cw = maxWidth / grid.cols
            val ch = maxHeight / grid.rows
            val casilla = floor(pos.value).toInt()
            Board(config, state, Modifier.fillMaxSize(), highlight = if (quieta) casilla % n else null, showTokens = false) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(titulo, fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Text("$msPorCasilla ms por casilla", fontSize = 15.sp)
                    Text(rotulo, fontSize = 17.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 18.dp))
                }
            }
            // La otra ficha, quieta en la salida.
            val size = minOf(30.dp, cw * 0.48f)
            val salida = grid.cellOf(0)
            PlayerToken(state, 1, size, Modifier.offset(cw * salida.col + cw - size - 2.dp, ch * salida.row + ch - size - 6.dp))
            // La que camina: entre el centro de abajo de una casilla y el de la siguiente.
            val a = grid.cellOf(casilla % n)
            val b = grid.cellOf((casilla + 1) % n)
            val f = pos.value - casilla
            val x = cw * (a.col + (b.col - a.col) * f) + 2.dp
            val y = ch * (a.row + (b.row - a.row) * f) + ch - size - 6.dp
            val alto = if (letra != "B" && letra != "C") sin(PI * f).toFloat() else 0f // A: sube y baja en cada casilla
            PlayerToken(state, 0, size, Modifier.offset(x, y - size * 0.9f * alto).scale(1f + 0.25f * alto))
        }
    }
}
