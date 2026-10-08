package com.jacck.mono.demo

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.BotonChiva
import com.jacck.mono.Calcomania
import com.jacck.mono.Chiva
import com.jacck.mono.OpcionChiva
import com.jacck.mono.PantallaChiva
import com.jacck.mono.board.Icon
import com.jacck.mono.board.IconImage

/**
 * Maquetas de la pantalla que se está diseñando (M-029), con datos falsos; se abren con el extra
 * `maqueta`. F5.8b, dónde se marca un jugador «Máquina» en el menú: A una tercera opción junto a
 * «Aquí» y «Otro», B un botón con el robot al lado del nombre, C un contador «Máquinas» debajo de
 * «Jugadores» (los últimos pasan a máquina).
 */
@Composable
fun Maqueta(letra: String) {
    PantallaChiva {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (letra) {
                "A" -> MaquetaA()
                "B" -> MaquetaB()
                else -> MaquetaC()
            }
        }
    }
}

@Composable
private fun Titulo(texto: String) =
    Text(texto, fontSize = 30.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

@Composable
private fun Nota(texto: String) = Text(texto, fontSize = 14.sp, color = Chiva.Tinta.copy(alpha = 0.7f))

@Composable
private fun Nombre(texto: String, modifier: Modifier = Modifier.fillMaxWidth()) =
    OutlinedTextField(texto, {}, singleLine = true, modifier = modifier)

private val jugadores = listOf("Ana" to 0, "Beto" to 2, "Caro" to 1) // 0 aquí, 1 otro teléfono, 2 máquina

/** A: tres opciones en la fila de cada jugador. */
@Composable
private fun MaquetaA() {
    Titulo("Nueva partida")
    Calcomania {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Jugadores", fontSize = 18.sp)
            jugadores.forEach { (n, donde) ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Nombre(n)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OpcionChiva("Aquí", donde == 0, {}, Modifier.weight(1f))
                        OpcionChiva("Otro", donde == 1, {}, Modifier.weight(1f), Icon.ENLACE)
                        OpcionChiva("Máquina", donde == 2, {}, Modifier.weight(1.3f), Icon.MAQUINA)
                    }
                }
            }
        }
    }
    BotonChiva("Esperar al otro teléfono", {})
}

/** B: la fila de hoy y, junto al nombre, un botón con el robot que vuelve máquina al jugador. */
@Composable
private fun MaquetaB() {
    Titulo("Nueva partida")
    Calcomania {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Jugadores", fontSize = 18.sp)
            jugadores.forEach { (n, donde) ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Nombre(n, Modifier.weight(1f))
                        val forma = RoundedCornerShape(10.dp)
                        Box(
                            Modifier.size(52.dp).border(2.dp, Chiva.Tinta, forma).alpha(if (donde == 2) 1f else 0.35f),
                            contentAlignment = Alignment.Center,
                        ) { IconImage(Icon.MAQUINA, 34.dp) }
                    }
                    if (donde == 2) Nota("Juega la máquina")
                    else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OpcionChiva("Aquí", donde == 0, {}, Modifier.weight(1f))
                        OpcionChiva("Otro teléfono", donde == 1, {}, Modifier.weight(1.4f), Icon.ENLACE)
                    }
                }
            }
        }
    }
    BotonChiva("Esperar al otro teléfono", {})
}

/** C: cuántas máquinas, debajo de cuántos jugadores; las últimas filas son máquinas. */
@Composable
private fun MaquetaC() {
    Titulo("Nueva partida")
    Calcomania {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Jugadores", fontSize = 18.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (2..6).forEach { OpcionChiva("$it", it == 3, {}, Modifier.weight(1f)) }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconImage(Icon.MAQUINA, 24.dp)
                Text("Máquinas", fontSize = 18.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (0..2).forEach { OpcionChiva("$it", it == 1, {}, Modifier.weight(1f)) }
            }
            listOf("Ana" to 0, "Caro" to 1).forEach { (n, donde) ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Nombre(n)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OpcionChiva("Aquí", donde == 0, {}, Modifier.weight(1f))
                        OpcionChiva("Otro teléfono", donde == 1, {}, Modifier.weight(1.4f), Icon.ENLACE)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconImage(Icon.MAQUINA, 28.dp)
                Text("Máquina 1", fontSize = 18.sp)
            }
        }
    }
    BotonChiva("Esperar al otro teléfono", {})
}
