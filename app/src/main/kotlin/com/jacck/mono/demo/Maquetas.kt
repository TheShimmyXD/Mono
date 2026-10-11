package com.jacck.mono.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/**
 * Maquetas de la pantalla que se está diseñando (M-029), con datos falsos; se abren con el extra
 * `maqueta`. FF.2: cómo confirmar la venta de una casa (R-30, se pierde la mitad) en la hoja del jugador.
 */
@Composable
fun Maqueta(letra: String) {
    PantallaChiva {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("FF.2 · Confirmar la venta ($letra)", fontSize = 18.sp)
            Opcion("A · Segundo toque: el botón pide «¿Seguro?» 3 s") {
                Fila { BotonChiva("Construir −$50", {}, Modifier.weight(1f)); BotonChiva("Vender +$25", {}, Modifier.weight(1f), principal = false) }
                Nota("Tras el primer toque (vuelve solo a los 3 s):")
                Fila { BotonChiva("Construir −$50", {}, Modifier.weight(1f)); BotonChiva("¿Seguro? +$25", {}, Modifier.weight(1f)) }
            }
            Opcion("B · Ventana encima, como la de la quiebra") {
                Fila { BotonChiva("Construir −$50", {}, Modifier.weight(1f)); BotonChiva("Vender +$25", {}, Modifier.weight(1f), principal = false) }
                Calcomania(sombra = 3.dp, borde = 2.dp) {
                    Text("¿Vender una casa?", Modifier.fillMaxWidth().background(Chiva.Techo).padding(6.dp), color = Color.White, fontSize = 15.sp)
                    Text("Bogotá: recibes $25 (pagaste $50).", Modifier.padding(8.dp), fontSize = 13.sp)
                    Row(Modifier.padding(start = 8.dp, end = 11.dp, bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        BotonChiva("Vender +$25", {}, Modifier.weight(1f)); BotonChiva("Cancelar", {}, Modifier.weight(1f), principal = false)
                    }
                }
            }
            Opcion("C · Mantener presionado 1 s: se va llenando") {
                Fila { BotonChiva("Construir −$50", {}, Modifier.weight(1f)); Lleno("Mantén: vender +$25", 0f) }
                Nota("Mientras lo sostienes (soltar antes cancela):")
                Fila { BotonChiva("Construir −$50", {}, Modifier.weight(1f)); Lleno("Mantén: vender +$25", 0.6f) }
            }
        }
    }
}

@Composable
private fun Opcion(titulo: String, content: @Composable ColumnScope.() -> Unit) {
    Text(titulo, fontSize = 14.sp)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), content = content)
}

@Composable
private fun Nota(texto: String) = Text(texto, fontSize = 12.sp, color = Chiva.Tinta.copy(alpha = 0.7f))

/** Una propiedad de la hoja (como `PropertyRow`): Bogotá, azul oscuro, 2 casas, y sus botones. */
@Composable
private fun Fila(botones: @Composable RowScope.() -> Unit) {
    Calcomania(sombra = 2.dp, borde = 1.5.dp, forma = RoundedCornerShape(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp, 34.dp).background(Color(0xFF0072BB)))
            Text("Bogotá", Modifier.weight(1f).padding(horizontal = 8.dp), fontSize = 14.sp)
            repeat(2) { IconImage(Icon.CASA, 14.dp) }
            Box(Modifier.size(6.dp))
        }
        Row(Modifier.padding(start = 6.dp, end = 9.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), content = botones)
    }
}

/** Botón ocre que se llena de rojo de izquierda a derecha según [fraccion] (opción C). */
@Composable
private fun RowScope.Lleno(texto: String, fraccion: Float) {
    val forma = RoundedCornerShape(10.dp)
    Box(Modifier.weight(1f)) {
        Box(Modifier.matchParentSize().offset(3.dp, 3.dp).background(Chiva.Tinta, forma))
        Box(Modifier.fillMaxWidth().clip(forma).background(Chiva.Ocre).border(2.dp, Chiva.Tinta, forma)) {
            if (fraccion > 0f) Box(Modifier.matchParentSize()) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(fraccion).background(Chiva.Techo))
            }
            Text(texto, Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 6.dp), color = Chiva.Tinta, fontSize = 13.sp, maxLines = 1,
                textAlign = TextAlign.Center)
        }
    }
}
