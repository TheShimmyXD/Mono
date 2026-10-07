package com.jacck.mono.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.Calcomania
import com.jacck.mono.Chiva
import com.jacck.mono.PantallaChiva
import com.jacck.mono.board.Medallon
import com.jacck.mono.board.Personajes
import com.jacck.mono.board.PlayerColors

/**
 * Maquetas de la pantalla que se está diseñando (M-029), con datos falsos; se abren con el extra
 * `maqueta`. FB.4: tres formas de escoger personaje en el menú, con Ana (mono), Beto (chiva) y
 * Caro (el primero libre, el sombrero). La de FB.3 (personajes en la casilla) está en `9089e37`.
 */
@Composable
fun Maqueta(letra: String) {
    PantallaChiva {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Escoger personaje ($letra)", fontSize = 22.sp)
            val opciones = listOf<@Composable () -> Unit>({ OpcionA() }, { OpcionB() }, { OpcionC() })
            // `FB.4c` pone la C arriba: adb no puede deslizar la pantalla (D-23).
            (if (letra.endsWith("c")) listOf(2, 0, 1) else listOf(0, 1, 2)).forEach { opciones[it]() }
        }
    }
}

private val Nombres = listOf("Ana", "Beto", "Caro")

/** Personaje de cada jugador (índice en `Personajes`); Caro no ha escogido: tiene el primero libre. */
private val Elegidos = listOf(0, 1, 2)

@Composable
private fun Campo(nombre: String) {
    Box(Modifier.fillMaxWidth().height(40.dp).background(Color.White).border(1.5.dp, Chiva.Tinta, RoundedCornerShape(4.dp)).padding(horizontal = 12.dp)) {
        Text(nombre, fontSize = 17.sp, modifier = Modifier.align(Alignment.CenterStart))
    }
}

/** Los 8 en 2 filas de 4: los tomados por otros, atenuados con el aro de su dueño. */
@Composable
private fun Rejilla(jugador: Int, tam: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Personajes.withIndex().chunked(4).forEach { fila ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                fila.forEach { (k, par) ->
                    val dueno = Elegidos.indexOf(k)
                    Medallon(
                        par.second, if (dueno >= 0) PlayerColors[dueno] else PlayerColors[jugador], tam.dp,
                        Modifier.alpha(if (dueno >= 0 && dueno != jugador) 0.3f else 1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun OpcionA() {
    Calcomania {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("A · Se toca el medallón", fontSize = 17.sp)
            Nombres.forEachIndexed { i, nombre ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Medallon(Personajes[Elegidos[i]].second, PlayerColors[i], 44.dp)
                    Campo(nombre)
                }
            }
            Text("Al tocar el de Caro:", fontSize = 13.sp)
            Box(Modifier.fillMaxWidth().border(2.dp, Chiva.Tinta, RoundedCornerShape(12.dp)).padding(8.dp)) {
                Rejilla(jugador = 2, tam = 52)
            }
        }
    }
}

@Composable
private fun OpcionB() {
    Calcomania {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("B · Los 8 bajo cada nombre", fontSize = 17.sp)
            Nombres.forEachIndexed { i, nombre ->
                Campo(nombre)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Personajes.forEachIndexed { k, (_, pj) ->
                        val dueno = Elegidos.indexOf(k)
                        Medallon(
                            pj, if (dueno >= 0) PlayerColors[dueno] else Color.LightGray, if (dueno == i) 38.dp else 32.dp,
                            Modifier.alpha(if (dueno >= 0 && dueno != i) 0.3f else 1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OpcionC() {
    Calcomania {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("C · Paso aparte, tras «Empezar»", fontSize = 17.sp)
            Text("Caro, escoge tu personaje", fontSize = 20.sp, color = PlayerColors[2])
            Rejilla(jugador = 2, tam = 70)
        }
    }
}
