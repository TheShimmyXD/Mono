package com.jacck.mono.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.jacck.mono.OpcionChiva
import com.jacck.mono.PantallaChiva
import com.jacck.mono.board.Board
import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.CardSquare
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Square
import com.jacck.mono.engine.model.Station
import com.jacck.mono.engine.model.Tax
import com.jacck.mono.engine.model.Utility

/**
 * Maquetas de la pantalla que se está diseñando (M-029), con datos falsos; se abren con el extra
 * `maqueta`. F4.3, tamaño del mapa: A botones en la ficha, B pestaña «Tamaño» con − / + de 4 en 4,
 * C lista de casillas con ✕ y «+ añadir aquí».
 */
@Composable
fun Maqueta(letra: String) {
    val full = remember { Preset.CLASSIC.load() }
    // El estado sale del Clásico entero (40 casillas): el tablero recortado solo lee sus primeras.
    val state = remember { Engine.newGame(full, listOf("", ""), seed = 0).state }
    PantallaChiva {
        when (letra) {
            "A" -> MaquetaA(full, state)
            "B" -> MaquetaB(full, state)
            else -> MaquetaC(full)
        }
    }
}

@Composable
private fun Pestanas(vararg nombres: String, elegida: Int) {
    Text("Editar · Clásico", fontSize = 22.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp))
    Row(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        nombres.forEachIndexed { k, n -> OpcionChiva(n, k == elegida, {}, Modifier.weight(1f)) }
    }
}

@Composable
private fun Error(texto: String) =
    Text(texto, color = Chiva.Techo, fontSize = 15.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))

/** A: la ficha de la casilla tocada trae «Quitar» y «Añadir después» (con el tipo); el error, en rojo. */
@Composable
private fun MaquetaA(full: GameConfig, state: com.jacck.mono.engine.model.GameState) {
    val config = full.copy(squares = full.squares.filterIndexed { i, _ -> i != 13 && i != 14 })
    Pestanas("Casillas", "Reglas", elegida = 0)
    Board(config, state, Modifier.fillMaxWidth().aspectRatio(1f).padding(horizontal = 6.dp), highlight = 12, showTokens = false) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("38 casillas\n\nCasilla 13\n${config.squares[12].name}", fontSize = 16.sp, textAlign = TextAlign.Center)
        }
    }
    Error("El tablero tiene 38 casillas: debe tener de 16 a 48 y ser múltiplo de 4.")
    Calcomania(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Casilla 13 · ${config.squares[12].name}", fontSize = 17.sp)
            BotonChiva("Quitar esta casilla", {}, principal = false)
            Text("Añadir una casilla después de esta:", fontSize = 15.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Propiedad", "Tren", "Carta", "Impuesto").forEachIndexed { k, t -> OpcionChiva(t, k == 0, {}, Modifier.weight(1f)) }
            }
            BotonChiva("Añadir después", {})
        }
    }
}

/** B: pestaña «Tamaño»; − / + quita o añade una casilla por lado y el tablero se ve al instante. */
@Composable
private fun MaquetaB(full: GameConfig, state: com.jacck.mono.engine.model.GameState) {
    // 40 → 24: cuatro veces −4; aquí, a mano, las primeras seis de cada lado.
    val keep = (0..39).filter { it % 10 < 6 }
    val config = full.copy(squares = keep.map { full.squares[it] })
    Pestanas("Casillas", "Reglas", "Tamaño", elegida = 2)
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        BotonChiva("−4", {}, Modifier.width(72.dp), principal = false)
        Text("24 casillas", fontSize = 24.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        BotonChiva("+4", {}, Modifier.width(72.dp), principal = false)
    }
    Text(
        "De 16 a 48, de 4 en 4. Con −4 sale la última casilla de cada lado; con +4 entra una propiedad nueva por lado.",
        fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp),
    )
    Board(config, state, Modifier.fillMaxWidth().aspectRatio(1f).padding(6.dp), highlight = null, showTokens = false) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Salieron 16:\nSan Victorino,\nLa Perseverancia…", fontSize = 15.sp, textAlign = TextAlign.Center)
        }
    }
    Error("Los grupos Celeste, Naranja, Amarillo y Azul quedaron vacíos: se borran.")
}

/** C: la lista de casillas en orden, cada una con ✕ y una raya «+ añadir aquí» entre ellas. */
@Composable
private fun MaquetaC(full: GameConfig) {
    Pestanas("Casillas", "Reglas", "Lista", elegida = 2)
    Text("39 casillas", fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    Error("El tablero tiene 39 casillas: debe tener de 16 a 48 y ser múltiplo de 4.")
    Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        full.squares.take(12).forEachIndexed { i, sq ->
            if (i == 3) Text("Quitada: casilla 4", color = Chiva.Techo, fontSize = 13.sp, modifier = Modifier.padding(start = 40.dp))
            else Fila(full, i, sq)
            Text("+ añadir aquí", color = Chiva.Tinta.copy(alpha = 0.55f), fontSize = 12.sp, modifier = Modifier.padding(start = 40.dp))
        }
    }
}

@Composable
private fun Fila(config: GameConfig, i: Int, sq: Square) {
    val forma = RoundedCornerShape(8.dp)
    val color = (sq as? Property)?.let { p -> config.groups.first { it.id == p.group }.color }
        ?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.LightGray
    Row(
        Modifier.fillMaxWidth().background(Color.White, forma).border(1.5.dp, Chiva.Tinta, forma).padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("${i + 1}", fontSize = 14.sp, modifier = Modifier.width(22.dp))
        Box(Modifier.size(14.dp, 22.dp).background(color, RoundedCornerShape(3.dp)))
        Column(Modifier.weight(1f)) {
            Text(sq.name, fontSize = 15.sp, maxLines = 1)
            Text(tipo(sq), fontSize = 12.sp, color = Chiva.Tinta.copy(alpha = 0.6f))
        }
        Text("✕", fontSize = 18.sp, color = Chiva.Techo, modifier = Modifier.height(24.dp))
    }
}

private fun tipo(sq: Square) = when (sq) {
    is Property -> "Propiedad · $${sq.price}"
    is Station -> "Tren · $${sq.price}"
    is Utility -> "Servicio · $${sq.price}"
    is Tax -> "Impuesto · $${sq.fixed}"
    is CardSquare -> "Carta"
    else -> "Esquina"
}
