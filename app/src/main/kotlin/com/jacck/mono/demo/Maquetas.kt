package com.jacck.mono.demo

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.BotonChiva
import com.jacck.mono.Calcomania
import com.jacck.mono.Chiva
import com.jacck.mono.PantallaChiva
import com.jacck.mono.board.Icon
import com.jacck.mono.board.IconImage
import com.jacck.mono.board.Personajes
import com.jacck.mono.board.PlayerColors

/**
 * Maquetas de la pantalla que se está diseñando (M-029), con datos falsos; se abren con el extra
 * `maqueta`. FH.2 (D-90): el historial de partidas, opción A del autor: una tarjeta por partida,
 * medallones iguales con insignia arriba a la derecha (robot o enlace), el ganador con cromo dorado
 * que se mueve detrás de su personaje y los demás en blanco y negro.
 */
@Composable
fun Maqueta(letra: String) {
    PantallaChiva {
        Column(Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Historial", fontSize = 30.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Partidas.forEach { Tarjeta(it) }
            Spacer(Modifier.weight(1f))
            BotonChiva("Volver al menú", {}, principal = false)
        }
    }
}

/** Jugador falso: nombre, color y personaje (su índice), y si lo juega la máquina o está en otro teléfono. */
private data class Quien(val nombre: String, val k: Int, val maquina: Boolean = false, val remoto: Boolean = false)

/** [fin] solo cuando no basta el ganador dorado: por tiempo o sin terminar. */
private data class Partida(val fecha: String, val tablero: String, val jugadores: List<Quien>, val ganador: Int?, val fin: String?, val tiempo: String, val turnos: Int)

private val Andres = Quien("Andrés", 0)
private val Santi = Quien("Santi", 1)
private val Partidas = listOf(
    Partida("Sáb 11 oct", "Clásico", listOf(Andres, Santi, Quien("Máquina 1", 2, maquina = true), Quien("Laura", 3, remoto = true)), 0, null, "1 h 12 min", 86),
    Partida("Vie 10 oct", "Tío Rico", listOf(Andres, Santi), 1, null, "48 min", 203),
    Partida("Jue 9 oct", "Mi Bogotá", listOf(Andres, Quien("Máquina 1", 1, maquina = true), Quien("Máquina 2", 2, maquina = true)), 2, "Se acabó el tiempo", "2 h 05 min", 140),
    Partida("Mié 8 oct", "Clásico", listOf(Andres, Santi, Quien("Caro", 2), Quien("Dani", 3)), null, "Sin terminar", "25 min", 40),
)

/** Cromo dorado: tonos de oro con un brillo claro en medio, en espejo para que el barrido no tenga corte. */
private val Oro = listOf(Color(0xFFA67C00), Color(0xFFE8B923), Color(0xFFFFF4C2), Color(0xFFE8B923), Color(0xFFA67C00))

/** Medallón del historial: como [Medallon], con fondo de cromo que barre si ganó, o el personaje en gris si no. */
@Composable
private fun MedallonHistorial(q: Quien, estado: Boolean?, size: Dp) {
    val brillo by rememberInfiniteTransition(label = "cromo").animateFloat(
        0f, 1f, infiniteRepeatable(tween(2200, easing = LinearEasing)), label = "cromo",
    )
    Box(Modifier.size(size + 8.dp)) {
        Box(
            Modifier.align(Alignment.BottomStart).size(size).clip(CircleShape)
                .drawBehind {
                    if (estado == true) {
                        val w = this.size.width
                        val x = -w + 2 * w * brillo
                        drawRect(Brush.linearGradient(Oro, Offset(x, 0f), Offset(x + w, this.size.height), TileMode.Mirror))
                    } else drawRect(Color.White)
                }
                .border(size / 10, PlayerColors[q.k], CircleShape).border(size / 35, Chiva.Tinta, CircleShape),
        ) {
            Image(
                painterResource(Personajes[q.k].second), null, Modifier.size(size).padding(size / 8),
                colorFilter = if (estado == false) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
            )
        }
        val insignia = if (q.maquina) Icon.MAQUINA else if (q.remoto) Icon.ENLACE else null
        insignia?.let {
            Box(
                Modifier.align(Alignment.TopEnd).size(20.dp).clip(CircleShape).background(Color.White).border(1.5.dp, Chiva.Tinta, CircleShape),
                contentAlignment = Alignment.Center,
            ) { IconImage(it, 14.dp) }
        }
    }
}

@Composable
private fun Tarjeta(p: Partida) = Calcomania {
    Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(p.fecha, fontSize = 15.sp)
            Text(" · ${p.tablero}", fontSize = 15.sp, modifier = Modifier.weight(1f))
            Text("${p.tiempo} · ${p.turnos} turnos", fontSize = 13.sp, color = Chiva.Tinta.copy(alpha = 0.7f))
        }
        Row {
            p.jugadores.forEachIndexed { i, q ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
                    // Sin ganador (sin terminar), todos a color.
                    MedallonHistorial(q, p.ganador?.let { it == i }, 40.dp)
                    Text(q.nombre, fontSize = 12.sp, maxLines = 1)
                }
            }
        }
        p.fin?.let { Text(it, fontSize = 14.sp, color = Chiva.Tinta.copy(alpha = 0.6f)) }
    }
}

