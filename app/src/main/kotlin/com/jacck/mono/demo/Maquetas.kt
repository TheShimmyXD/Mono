package com.jacck.mono.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.jacck.mono.board.Board
import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.Property

/**
 * Maquetas de la pantalla que se está diseñando (M-029), con datos falsos; se abren con el extra
 * `maqueta`. F4.1, editor de casillas sobre el Clásico, editando «Las Cruces» (casilla 1):
 * A lista de casillas + ficha, B tablero en anillo + ficha, C la escritura editable con ‹ ›.
 */
@Composable
fun Maqueta(letra: String) {
    val config = remember { Preset.CLASSIC.load() }
    PantallaChiva {
        when (letra) {
            "A" -> ListaYFicha(config)
            "B" -> TableroYFicha(config)
            else -> Escritura(config)
        }
    }
}

private const val ELEGIDA = 1

private fun colorDe(config: GameConfig, i: Int): Color? =
    (config.squares[i] as? Property)?.let { p -> config.groups.first { it.id == p.group } }
        ?.let { Color(android.graphics.Color.parseColor(it.color)) }

@Composable
private fun Titulo(texto: String) =
    Text(texto, fontSize = 22.sp, color = Chiva.Tinta, modifier = Modifier.fillMaxWidth().padding(10.dp), textAlign = TextAlign.Center)

/** Un campo de texto falso: etiqueta encima y el valor en una caja blanca con contorno. */
@Composable
private fun Campo(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(etiqueta, fontSize = 12.sp, color = Chiva.Tinta)
        Box(
            Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp))
                .border(2.dp, Chiva.Tinta, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 6.dp),
        ) { Text(valor, fontSize = 16.sp, color = Chiva.Tinta, maxLines = 1) }
    }
}

@Composable
private fun Grupos(config: GameConfig, elegido: String) {
    Text("Grupo", fontSize = 12.sp, color = Chiva.Tinta)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        config.groups.forEach { g ->
            val c = Color(android.graphics.Color.parseColor(g.color))
            Box(
                Modifier.size(30.dp).background(c, CircleShape)
                    .border(if (g.id == elegido) 4.dp else 2.dp, Chiva.Tinta, CircleShape),
            )
        }
    }
}

@Composable
private fun Alquileres(p: Property) {
    val etiquetas = listOf("Solar", "1 casa", "2", "3", "4", "Hotel")
    Text("Alquiler", fontSize = 12.sp, color = Chiva.Tinta)
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        p.rents.forEachIndexed { k, r -> Campo(etiquetas[k], "$r", Modifier.weight(1f)) }
    }
}

/** La ficha de la casilla elegida: nombre, precio, grupo y alquileres. */
@Composable
private fun Ficha(config: GameConfig) {
    val p = config.squares[ELEGIDA] as Property
    Calcomania(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Campo("Nombre", p.name, Modifier.weight(2f))
                Campo("Precio", "$${p.price}", Modifier.weight(1f))
            }
            Grupos(config, p.group)
            Alquileres(p)
        }
    }
}

@Composable
private fun ListaYFicha(config: GameConfig) {
    Titulo("Casillas · Clásico")
    Calcomania(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        (0..9).forEach { i ->
            val sq = config.squares[i]
            Row(
                Modifier.fillMaxWidth().background(if (i == ELEGIDA) Chiva.Turno else Color.White)
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${i + 1}", fontSize = 13.sp, modifier = Modifier.width(26.dp))
                Box(Modifier.size(14.dp, 22.dp).background(colorDe(config, i) ?: Color.Transparent))
                Spacer(Modifier.width(8.dp))
                Text(sq.name, fontSize = 16.sp, modifier = Modifier.weight(1f), maxLines = 1)
                Text((sq as? OwnableSquare)?.let { "$${it.price}" } ?: "", fontSize = 15.sp)
            }
        }
        Text("… 30 más", fontSize = 13.sp, modifier = Modifier.padding(8.dp))
    }
    Spacer(Modifier.height(10.dp))
    Ficha(config)
    Spacer(Modifier.height(10.dp))
    Box(Modifier.padding(horizontal = 12.dp)) { BotonChiva("Guardar casilla", {}) }
}

@Composable
private fun TableroYFicha(config: GameConfig) {
    val state = remember {
        Engine.newGame(config, listOf("Ana", "Beto"), seed = 7).state.let { s ->
            s.copy(players = s.players.mapIndexed { k, pl -> if (k == 0) pl.copy(position = ELEGIDA) else pl })
        }
    }
    Board(config, state, Modifier.fillMaxWidth().aspectRatio(1f).padding(6.dp)) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Toca una casilla\npara editarla", fontSize = 16.sp, textAlign = TextAlign.Center)
        }
    }
    Ficha(config)
    Spacer(Modifier.height(10.dp))
    Box(Modifier.padding(horizontal = 12.dp)) { BotonChiva("Guardar casilla", {}) }
}

@Composable
private fun Escritura(config: GameConfig) {
    val p = config.squares[ELEGIDA] as Property
    Titulo("Casilla 2 de 40")
    Calcomania(Modifier.fillMaxWidth().padding(horizontal = 28.dp), sombra = 5.dp, borde = 3.dp) {
        Box(Modifier.fillMaxWidth().background(colorDe(config, ELEGIDA)!!).padding(12.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ESCRITURA", fontSize = 12.sp, color = Color.White)
                Box(Modifier.background(Color.White, RoundedCornerShape(6.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text(p.name + " ✎", fontSize = 22.sp, color = Chiva.Tinta)
                }
            }
        }
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            val filas = listOf("Precio" to p.price) +
                listOf("Solar", "Con 1 casa", "Con 2 casas", "Con 3 casas", "Con 4 casas", "Con hotel").zip(p.rents)
            filas.forEach { (etiqueta, v) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(etiqueta, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    Text("−", fontSize = 22.sp, modifier = Modifier.padding(horizontal = 10.dp))
                    Box(
                        Modifier.width(72.dp).border(2.dp, Chiva.Tinta, RoundedCornerShape(6.dp)).padding(4.dp),
                        contentAlignment = Alignment.Center,
                    ) { Text("$$v", fontSize = 16.sp) }
                    Text("+", fontSize = 22.sp, modifier = Modifier.padding(horizontal = 10.dp))
                }
            }
            Grupos(config, p.group)
        }
    }
    Spacer(Modifier.height(16.dp))
    Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        BotonChiva("‹ Anterior", {}, Modifier.weight(1f), principal = false)
        BotonChiva("Siguiente ›", {}, Modifier.weight(1f), principal = false)
    }
    Spacer(Modifier.height(10.dp))
    Box(Modifier.padding(horizontal = 12.dp)) { BotonChiva("Listo", {}) }
}
