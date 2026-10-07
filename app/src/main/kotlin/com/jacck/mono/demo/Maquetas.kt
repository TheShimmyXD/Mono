package com.jacck.mono.demo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.Calcomania
import com.jacck.mono.Chiva
import com.jacck.mono.PantallaChiva
import com.jacck.mono.R
import com.jacck.mono.board.PlayerColors

/**
 * Maquetas de la pantalla que se está diseñando (M-029), con datos falsos; se abren con el extra
 * `maqueta`. FB.3: los 8 personajes de `arte.py` y tres formas de ponerlos en una casilla del
 * tablero (A en fila, B grandes encima, C medallones encimados), con 1 a 4 fichas en la misma casilla.
 */
@Composable
fun Maqueta(letra: String) {
    PantallaChiva {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Calcomania {
                Titulo("Personajes ($letra)")
                Personajes.chunked(4).forEachIndexed { fila, cuatro ->
                    Row(Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        cuatro.forEachIndexed { i, pj -> Disco(pj, PlayerColors[(fila * 4 + i) % PlayerColors.size], 64.dp) }
                    }
                }
            }
            Opcion("A · En fila, sobre su color") { n -> FilaA(n) }
            Opcion("B · Grandes, encima de la casilla") { n -> EncimaB(n) }
            Opcion("C · Medallones encimados abajo") { n -> MedallonesC(n) }
        }
    }
}

private val Personajes = listOf(
    R.drawable.pj_mono, R.drawable.pj_chiva, R.drawable.pj_sombrero, R.drawable.pj_colibri,
    R.drawable.pj_perro, R.drawable.pj_arepa, R.drawable.pj_tinto, R.drawable.pj_guacamaya,
)
private val Ancho = 54.dp
private val Alto = 66.dp

@Composable
private fun Titulo(texto: String) =
    Text(texto, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))

/** Cuatro casillas de tamaño real con 1, 2, 3 y 4 fichas, dibujadas por [fichas]. */
@Composable
private fun Opcion(titulo: String, fichas: @Composable (Int) -> Unit) {
    Calcomania {
        Titulo(titulo)
        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("Chapinero" to Color(0xFF8E5A2B), "Usaquén" to Color(0xFF38C6F4), "Niza" to Color(0xFFE5007E), "Rosales" to Color(0xFFF4A700))
                .forEachIndexed { k, (nombre, grupo) ->
                    Box(Modifier.size(Ancho, Alto).background(Color.White).border(1.5.dp, Chiva.Tinta)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Box(Modifier.fillMaxWidth().height(Ancho / 6).background(grupo).border(0.75.dp, Chiva.Tinta))
                            Text(nombre, fontSize = 8.sp, maxLines = 1)
                            Text("$${100 + 20 * k}", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        fichas(k + 1)
                    }
                }
        }
    }
}

/** Personaje sobre un disco de color con contorno de tinta. */
@Composable
private fun Disco(pj: Int, color: Color, tam: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(tam).clip(CircleShape).background(color).border(1.dp, Chiva.Tinta, CircleShape)) {
        Image(painterResource(pj), null, Modifier.size(tam).padding(tam / 10))
    }
}

/** A: como la ficha de hoy, en la fila de abajo, pero con el personaje sobre su color. */
@Composable
private fun FilaA(n: Int) {
    val tam = minOf(18.dp, (Ancho - 6.dp) / n)
    Box(Modifier.size(Ancho, Alto)) {
        Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp)) {
            repeat(n) { Disco(Personajes[it], PlayerColors[it], tam, Modifier.padding(horizontal = 0.5.dp)) }
        }
    }
}

/** B: personajes grandes escalonados sobre la casilla, cada uno parado en una base de su color. */
@Composable
private fun EncimaB(n: Int) {
    val tam = Ancho * 0.6f
    val paso = if (n > 1) (Ancho - tam) / (n - 1) else 0.dp
    Box(Modifier.size(Ancho, Alto)) {
        repeat(n) {
            Box(Modifier.align(Alignment.BottomStart).offset(x = paso * it, y = -(2.dp + 4.dp * (n - 1 - it))).size(tam)) {
                Box(Modifier.align(Alignment.BottomCenter).size(tam * 0.85f, tam * 0.3f).background(PlayerColors[it], CircleShape).border(1.dp, Chiva.Tinta, CircleShape))
                Image(painterResource(Personajes[it]), null, Modifier.size(tam).padding(bottom = tam * 0.08f))
            }
        }
    }
}

/** C: medallones blancos con aro del color del jugador, encimados abajo como avatares. */
@Composable
private fun MedallonesC(n: Int) {
    val tam = 26.dp
    val paso = if (n > 1) minOf(tam, (Ancho - 4.dp - tam) / (n - 1)) else 0.dp
    Box(Modifier.size(Ancho, Alto)) {
        repeat(n) {
            Box(
                Modifier.align(Alignment.BottomStart).offset(x = 2.dp + paso * it, y = -2.dp).size(tam).clip(CircleShape)
                    .background(Color.White).border(2.5.dp, PlayerColors[it], CircleShape).border(0.75.dp, Chiva.Tinta, CircleShape),
            ) {
                Image(painterResource(Personajes[it]), null, Modifier.size(tam).padding(3.dp))
            }
        }
    }
}
