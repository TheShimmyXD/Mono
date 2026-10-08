package com.jacck.mono.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.BotonChiva
import com.jacck.mono.Calcomania
import com.jacck.mono.Chiva
import com.jacck.mono.PantallaChiva
import com.jacck.mono.board.Board
import com.jacck.mono.board.Icon
import com.jacck.mono.board.IconImage
import com.jacck.mono.board.PlayerToken
import com.jacck.mono.board.PlayersRow
import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.Property

/**
 * Maquetas de la pantalla que se está diseñando (M-029), con datos falsos; se abren con el extra
 * `maqueta`. Ahora: la ventana con lo que pasa (FD.5, D-67), bajo el letrero del turno, tras tres
 * turnos falsos: A bitácora corrida (lo nuevo arriba), B solo la última jugada con dados grandes,
 * C una tarjeta por turno (el actual entero, el anterior atenuado).
 */
@Composable
fun Maqueta(letra: String) {
    val config = remember { Preset.CLASSIC.load() }
    val state = remember { Engine.newGame(config, listOf("Andrés", "Botty 2", "Botty 3"), 7).state }
    val calles = config.squares.filterIsInstance<Property>().map { it.name }
    // Tres turnos, del más viejo al más nuevo: quién, sus dados y lo que pasó.
    val turnos = listOf(
        Turno(2, 2 to 1, listOf("Botty 3 cae en ${calles[1]}", "Botty 3 paga $8 de alquiler a Andrés")),
        Turno(0, 4 to 3, listOf("Andrés cae en ${calles[4]}", "Andrés compra ${calles[4]} por $120")),
        Turno(1, 6 to 6, listOf("Botty 2 pasa por la Salida y cobra $200", "Botty 2 cae en ${calles[6]}", "Botty 2 paga $10 de alquiler a Andrés", "Sacó dobles: tira otra vez")),
    )
    PantallaChiva {
        Board(config, state, Modifier.fillMaxSize().padding(4.dp), highlight = null) {
            Column(Modifier.fillMaxSize()) {
                PlayersRow(state, showMoney = true)
                Calcomania(Modifier.fillMaxWidth().padding(top = 26.dp)) {
                    Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Opción $letra · Botty 2 jugó", fontSize = 15.sp)
                        BotonChiva("Seguir", {})
                    }
                }
                Spacer(Modifier.padding(top = 8.dp))
                Ventana(Modifier.weight(1f)) {
                    when (letra) {
                        "A" -> BitacoraA(state, turnos)
                        "B" -> UltimaB(turnos.last())
                        else -> PorTurnoC(state, turnos)
                    }
                }
            }
        }
    }
}

private data class Turno(val jugador: Int, val dados: Pair<Int, Int>, val lineas: List<String>)

/** La ventana azul de la imagen del autor (D-67): calcomanía con una franja azul de título. */
@Composable
private fun Ventana(modifier: Modifier, contenido: @Composable () -> Unit) {
    Calcomania(modifier.fillMaxWidth()) {
        Text("Lo que pasa", Modifier.fillMaxWidth().background(Chiva.Azul).padding(horizontal = 10.dp, vertical = 4.dp), color = Color.White, fontSize = 15.sp)
        Column(Modifier.fillMaxSize().clipToBounds().padding(8.dp)) { contenido() }
    }
}

@Composable
private fun Dados(d: Pair<Int, Int>, tam: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconImage(Icon.dado(d.first), tam.dp)
        IconImage(Icon.dado(d.second), tam.dp)
        Text("= ${d.first + d.second}", fontSize = (tam / 2).sp)
    }
}

/** A: todas las líneas seguidas, lo más nuevo arriba; cada una con el medallón de quien la hizo. */
@Composable
private fun BitacoraA(state: GameState, turnos: List<Turno>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        turnos.asReversed().forEachIndexed { i, t ->
            val opaco = if (i == 0) 1f else 0.55f
            Row(Modifier.alpha(opaco), verticalAlignment = Alignment.CenterVertically) {
                PlayerToken(state, t.jugador, 20.dp)
                Spacer(Modifier.width(6.dp))
                Dados(t.dados, 20)
            }
            t.lineas.forEach { l ->
                Row(Modifier.alpha(opaco), verticalAlignment = Alignment.CenterVertically) {
                    PlayerToken(state, t.jugador, 20.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(l, fontSize = 14.sp)
                }
            }
        }
    }
}

/** B: solo la última jugada, con los dados grandes; la siguiente la reemplaza. */
@Composable
private fun UltimaB(t: Turno) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Dados(t.dados, 48)
        t.lineas.forEach { Text(it, Modifier.fillMaxWidth(), fontSize = 15.sp) }
    }
}

/** C: una tarjeta por turno: el actual entero arriba y el anterior debajo, atenuado. */
@Composable
private fun PorTurnoC(state: GameState, turnos: List<Turno>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        turnos.asReversed().take(2).forEachIndexed { i, t ->
            Calcomania(Modifier.fillMaxWidth().alpha(if (i == 0) 1f else 0.5f), sombra = 2.dp, borde = 1.5.dp) {
                Row(Modifier.fillMaxWidth().background(Chiva.Turno).padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    PlayerToken(state, t.jugador, 24.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(state.players[t.jugador].name, Modifier.weight(1f), fontSize = 15.sp)
                    Dados(t.dados, 24)
                }
                Column(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    t.lineas.forEach { Text("• $it", fontSize = 14.sp) }
                }
            }
        }
    }
}
