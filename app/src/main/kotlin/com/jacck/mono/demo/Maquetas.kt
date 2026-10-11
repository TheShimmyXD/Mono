package com.jacck.mono.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.BotonChiva
import com.jacck.mono.Calcomania
import com.jacck.mono.Chiva
import com.jacck.mono.PantallaChiva
import com.jacck.mono.board.Icon
import com.jacck.mono.board.IconImage
import com.jacck.mono.board.Medallon
import com.jacck.mono.board.Personajes
import com.jacck.mono.board.PlayerColors

/**
 * Maquetas de la pantalla que se está diseñando (M-029), con datos falsos; se abren con el extra
 * `maqueta`. FH.2 (D-90): el historial de partidas, de tres formas: A una tarjeta por partida,
 * B lista compacta de una línea, C resumen de victorias arriba y tarjetas cortas debajo.
 */
@Composable
fun Maqueta(letra: String) {
    val look = letra.last().uppercaseChar().takeIf { it in "ABC" } ?: 'A'
    PantallaChiva {
        Column(Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Historial", fontSize = 30.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text(mapOf('A' to "A · Una tarjeta por partida", 'B' to "B · Lista compacta", 'C' to "C · Resumen arriba").getValue(look), fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            when (look) {
                'A' -> Partidas.forEach { Tarjeta(it) }
                'B' -> Compacta()
                else -> { Resumen(); Partidas.forEach { Corta(it) } }
            }
            Spacer(Modifier.weight(1f))
            BotonChiva("Volver al menú", {}, principal = false)
        }
    }
}

/** Jugador falso: nombre, color y personaje (su índice), y si lo juega la máquina o está en otro teléfono. */
private data class Quien(val nombre: String, val k: Int, val maquina: Boolean = false, val remoto: Boolean = false)

private data class Partida(val fecha: String, val tablero: String, val jugadores: List<Quien>, val ganador: Int?, val fin: String, val tiempo: String, val turnos: Int)

private val Andres = Quien("Andrés", 0)
private val Santi = Quien("Santi", 1)
private val Partidas = listOf(
    Partida("Sáb 11 oct", "Clásico", listOf(Andres, Santi, Quien("Máquina 1", 2, maquina = true), Quien("Laura", 3, remoto = true)), 0, "Ganó Andrés", "1 h 12 min", 86),
    Partida("Vie 10 oct", "Tío Rico", listOf(Andres, Santi), 1, "Ganó Santi", "48 min", 203),
    Partida("Jue 9 oct", "Mi Bogotá", listOf(Andres, Quien("Máquina 1", 1, maquina = true), Quien("Máquina 2", 2, maquina = true)), 2, "Se acabó el tiempo: ganó Máquina 2", "2 h 05 min", 140),
    Partida("Mié 8 oct", "Clásico", listOf(Andres, Santi, Quien("Caro", 2), Quien("Dani", 3)), null, "Sin terminar", "25 min", 40),
)

@Composable
private fun Ficha(q: Quien, ganador: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Medallon(Personajes[q.k].second, PlayerColors[q.k], if (ganador) 40.dp else 32.dp)
        Text(q.nombre, fontSize = 12.sp, maxLines = 1)
        when {
            q.maquina -> Text("máquina", fontSize = 10.sp, color = Chiva.Tinta.copy(alpha = 0.6f))
            q.remoto -> IconImage(Icon.ENLACE, 14.dp)
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
        Row(verticalAlignment = Alignment.Bottom) { p.jugadores.forEachIndexed { i, q -> Ficha(q, i == p.ganador) } }
        Text(p.fin, fontSize = 15.sp, color = if (p.ganador == null) Chiva.Tinta.copy(alpha = 0.6f) else Chiva.Verde)
    }
}

@Composable
private fun Compacta() = Calcomania {
    Row(Modifier.background(Chiva.Ocre).padding(horizontal = 8.dp, vertical = 4.dp)) {
        listOf("Fecha" to 1.1f, "Ganó" to 1.3f, "Tiempo" to 1f, "Turnos" to 0.7f).forEach { (t, w) -> Text(t, fontSize = 13.sp, modifier = Modifier.weight(w)) }
    }
    Partidas.forEachIndexed { n, p ->
        if (n > 0) HorizontalDivider(color = Chiva.Tinta.copy(alpha = 0.3f))
        Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1.1f)) { Text(p.fecha, fontSize = 13.sp); Text(p.tablero, fontSize = 11.sp, color = Chiva.Tinta.copy(alpha = 0.6f)) }
            Row(Modifier.weight(1.3f), verticalAlignment = Alignment.CenterVertically) {
                p.ganador?.let { g -> val q = p.jugadores[g]; Medallon(Personajes[q.k].second, PlayerColors[q.k], 24.dp); Spacer(Modifier.width(4.dp)); Text(q.nombre, fontSize = 13.sp, maxLines = 1) }
                    ?: Text("Sin terminar", fontSize = 13.sp, color = Chiva.Tinta.copy(alpha = 0.6f))
            }
            Text(p.tiempo, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text("${p.turnos}", fontSize = 13.sp, modifier = Modifier.weight(0.7f))
        }
    }
    Text("Toca una partida para ver quiénes jugaron", fontSize = 12.sp, color = Chiva.Tinta.copy(alpha = 0.6f), modifier = Modifier.padding(8.dp))
}

@Composable
private fun Resumen() = Calcomania {
    Column(Modifier.background(Chiva.Azul).padding(10.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("4 partidas · 4 h 30 min jugadas", color = Color.White, fontSize = 17.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf(Andres to 1, Santi to 1, Quien("Máquina 2", 2) to 1).forEach { (q, v) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Medallon(Personajes[q.k].second, PlayerColors[q.k], 30.dp)
                    Text(" $v", color = Color.White, fontSize = 18.sp)
                }
            }
        }
        Text("victorias", color = Color.White, fontSize = 12.sp)
    }
}

@Composable
private fun Corta(p: Partida) = Calcomania {
    Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("${p.fecha} · ${p.tablero}", fontSize = 14.sp)
            Text(p.fin, fontSize = 13.sp, color = if (p.ganador == null) Chiva.Tinta.copy(alpha = 0.6f) else Chiva.Verde)
            Text("${p.jugadores.size} jugadores · ${p.tiempo} · ${p.turnos} turnos", fontSize = 12.sp, color = Chiva.Tinta.copy(alpha = 0.7f))
        }
        p.ganador?.let { g -> val q = p.jugadores[g]; Medallon(Personajes[q.k].second, PlayerColors[q.k], 36.dp) }
    }
}
