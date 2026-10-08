package com.jacck.mono.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.BotonChiva
import com.jacck.mono.Calcomania
import com.jacck.mono.Chiva
import com.jacck.mono.PantallaChiva
import com.jacck.mono.board.Board
import com.jacck.mono.board.BuildingIcons
import com.jacck.mono.board.Icon
import com.jacck.mono.board.IconImage
import com.jacck.mono.board.PlayerColors
import com.jacck.mono.board.PlayerToken
import com.jacck.mono.board.PlayersRow
import com.jacck.mono.board.SquareFace
import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Property
import com.jacck.mono.game.money
import com.jacck.mono.game.propertyMoves

/**
 * Maquetas de la pantalla que se está diseñando (M-029), con datos falsos; se abren con el extra
 * `maqueta`. Ahora: los detalles en la ventana y el deslizar para volver (FD.6, D-67). Cada opción,
 * con la casilla (`A`, `B`, `C`) y con el jugador (`A2`, `B2`, `C2`): A la ventana cambia a lo que
 * se tocó; B el detalle es una carta encima de «Lo que pasa», que asoma detrás; C las cartas se
 * apilan (lo último tocado arriba) y cada deslizar quita una.
 */
@Composable
fun Maqueta(letra: String) {
    val config = remember { Preset.CLASSIC.load() }
    val state = remember { withSampleProperties(Engine.newGame(config, listOf("Andrés", "Botty 2", "Botty 3"), 7).state) }
    val jugador = letra.endsWith("2")
    val opcion = letra.take(1)
    val suyas = state.holdings.filterValues { it.owner == state.current }.keys
    PantallaChiva {
        Board(
            config, state, Modifier.fillMaxSize().padding(4.dp), highlight = null,
            marks = if (jugador) suyas else emptySet(), markColor = PlayerColors[state.current],
        ) {
            Column(Modifier.fillMaxSize()) {
                PlayersRow(state, showMoney = true)
                Calcomania(Modifier.fillMaxWidth().padding(top = 26.dp)) {
                    Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Opción $letra · Turno de ${state.players[state.current].name}", fontSize = 15.sp)
                        BotonChiva("Tirar los dados", {})
                    }
                }
                Box(Modifier.weight(1f).fillMaxWidth().padding(top = 10.dp)) {
                    val detalle: @Composable () -> Unit = { if (jugador) Jugador(config, state) else SquareFace(config, state, 1) }
                    when (opcion) {
                        "A" -> CambiaA(if (jugador) state.players[state.current].name else "Casilla", detalle)
                        "B" -> EncimaB(state, detalle)
                        else -> PilaC(config, state, jugador)
                    }
                }
            }
        }
    }
}

/** La manija gris de arriba de una carta que se desliza. */
@Composable
private fun Manija(texto: Boolean = true) {
    Column(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(44.dp, 5.dp).clip(RoundedCornerShape(3.dp)).background(Color.Gray))
        if (texto) Text("Desliza hacia arriba para volver", fontSize = 11.sp, color = Color.Gray)
    }
}

/** A: la ventana misma muestra lo tocado; su franja azul dice qué es. */
@Composable
private fun CambiaA(titulo: String, detalle: @Composable () -> Unit) {
    Calcomania(Modifier.fillMaxSize()) {
        Text(titulo, Modifier.fillMaxWidth().background(Chiva.Azul).padding(horizontal = 10.dp, vertical = 4.dp), color = Color.White, fontSize = 15.sp)
        Manija()
        Column(Modifier.fillMaxSize().clipToBounds().padding(horizontal = 8.dp)) { detalle() }
    }
}

/** B: «Lo que pasa» queda detrás, con su franja a la vista, y el detalle va encima como una carta. */
@Composable
private fun EncimaB(state: GameState, detalle: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        LoQuePasa(state)
        Carta(Modifier.padding(top = 30.dp, start = 6.dp, end = 6.dp), detalle = detalle)
    }
}

/** C: las cartas se apilan; debajo asoma la que se tocó antes (casilla bajo el jugador, o al revés). */
@Composable
private fun PilaC(config: GameConfig, state: GameState, jugador: Boolean) {
    Box(Modifier.fillMaxSize()) {
        LoQuePasa(state)
        Carta(Modifier.padding(top = 30.dp, start = 6.dp, end = 6.dp), manija = false) {
            if (jugador) SquareFace(config, state, 1) else Jugador(config, state)
        }
        Carta(Modifier.padding(top = 58.dp, start = 10.dp, end = 10.dp)) {
            if (jugador) Jugador(config, state) else SquareFace(config, state, 1)
        }
    }
}

@Composable
private fun Carta(modifier: Modifier, manija: Boolean = true, detalle: @Composable () -> Unit) {
    Calcomania(modifier.fillMaxSize(), sombra = 6.dp, borde = 2.5.dp, forma = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxSize().background(Color.White).clipToBounds().padding(horizontal = 6.dp)) {
            Manija(manija)
            detalle()
        }
    }
}

/** La ventana de FD.5 con una tarjeta de turno falsa, para lo que queda detrás. */
@Composable
private fun LoQuePasa(state: GameState) {
    Calcomania(Modifier.fillMaxSize()) {
        Text("Lo que pasa", Modifier.fillMaxWidth().background(Chiva.Azul).padding(horizontal = 10.dp, vertical = 4.dp), color = Color.White, fontSize = 15.sp)
        Row(Modifier.fillMaxWidth().padding(8.dp).background(Chiva.Turno).padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
            PlayerToken(state, 2, 24.dp)
            Spacer(Modifier.width(6.dp))
            Text("Botty 3", Modifier.weight(1f), fontSize = 15.sp)
            IconImage(Icon.dado(2), 24.dp)
            IconImage(Icon.dado(1), 24.dp)
        }
    }
}

/** El jugador en la ventana: medallón, nombre y dinero; debajo sus propiedades con sus jugadas (es su turno). */
@Composable
private fun Jugador(config: GameConfig, state: GameState) {
    val yo = state.current
    val p = state.players[yo]
    val suyas = state.holdings.filterValues { it.owner == yo }.toSortedMap()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PlayerToken(state, yo, 44.dp)
            Column(Modifier.weight(1f)) {
                Text(p.name, fontSize = 18.sp)
                Text("Propiedades (${suyas.size})", fontSize = 13.sp)
            }
            Text(money(p.money), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        suyas.forEach { (casilla, h) ->
            val sq = config.squares[casilla]
            val banda = (sq as? Property)?.let { q -> config.groups.firstOrNull { it.id == q.group } }
                ?.let { Color(android.graphics.Color.parseColor(it.color)) } ?: Color.LightGray
            val jugadas = propertyMoves(config, state, casilla)
            Calcomania(sombra = 2.dp, borde = 1.5.dp, forma = RoundedCornerShape(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp, 34.dp).background(banda))
                    Text(sq.name + if (h.mortgaged) " · hipotecada" else "", Modifier.weight(1f).padding(horizontal = 8.dp), fontSize = 14.sp, maxLines = 1)
                    BuildingIcons(h, 14.dp)
                    Spacer(Modifier.width(6.dp))
                }
                if (jugadas.isNotEmpty()) {
                    Row(Modifier.padding(start = 6.dp, end = 9.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        jugadas.forEach { j ->
                            val texto = when (j.action) {
                                is Action.Build -> "Casa −${money(j.amount)}"
                                is Action.SellBuilding -> "Vender +${money(j.amount)}"
                                is Action.Mortgage -> "Hipotecar +${money(j.amount)}"
                                is Action.Unmortgage -> "Deshipotecar −${money(j.amount)}"
                                else -> ""
                            }
                            BotonChiva(texto, {}, Modifier.weight(1f), principal = j.action is Action.Build || j.action is Action.Unmortgage)
                        }
                    }
                }
            }
        }
    }
}
