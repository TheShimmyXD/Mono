package com.jacck.mono.demo

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.R
import com.jacck.mono.board.BuildingIcons
import com.jacck.mono.board.Icon
import com.jacck.mono.board.IconImage
import com.jacck.mono.board.PlayerColors
import com.jacck.mono.board.Token
import com.jacck.mono.engine.model.Holding

/**
 * Maquetas de la pantalla que se está diseñando (M-029), con datos falsos; se abren con el extra
 * `maqueta`. FA.3: la tarjeta que sale al tocar una casilla. A hoja inferior, B carta centrada de
 * chiva, C ficha compacta. Datos: Villa de Leyva (Tío Rico), de Ana, con 2 casas.
 */
@Composable
fun Maqueta(letra: String) {
    Box(Modifier.fillMaxSize().background(Color(0xFFF4EFE1)).safeDrawingPadding()) {
        FakeBoard()
        Box(Modifier.fillMaxSize().background(Color(0x99000000)))
        when (letra) {
            "A" -> SheetCard(Modifier.align(Alignment.BottomCenter))
            "B" -> ChivaCard(Modifier.align(Alignment.Center))
            else -> CompactCard(Modifier.align(Alignment.BottomCenter))
        }
    }
}

private val Boyaca = Color(0xFF8B4513)
private val Ink = Color(0xFF1B1B1B)
private val Rents = listOf(60, 300, 900, 2700, 5500)
private val Sample = Holding(owner = 0, houses = 2)

@Composable
private fun FakeBoard() {
    Column(Modifier.fillMaxSize().padding(4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(14) { r ->
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(7) { c ->
                    val edge = r == 0 || r == 13 || c == 0 || c == 6
                    Box(Modifier.weight(1f).fillMaxSize().background(if (edge) Color.White else Color.Transparent))
                }
            }
        }
    }
}

@Composable
private fun Art(modifier: Modifier = Modifier, crop: Boolean = false) {
    Image(
        painterResource(R.drawable.arte_villa_de_leyva), contentDescription = null,
        contentScale = if (crop) ContentScale.Crop else ContentScale.FillWidth, modifier = modifier,
    )
}

@Composable
private fun Owner() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Token(PlayerColors[0], 14.dp)
        Spacer(Modifier.width(6.dp))
        Text("De Ana", fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        BuildingIcons(Sample, 18.dp)
    }
}

/** Fila de alquiler; la del número de casas que tiene ahora va resaltada. */
@Composable
private fun RentLine(label: String, amount: Int, now: Boolean, icon: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().background(if (now) Color(0xFFFFE08A) else Color.Transparent).padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.invoke()
        Text(label, Modifier.weight(1f).padding(start = 4.dp), fontSize = 14.sp)
        Text("$$amount", fontWeight = if (now) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
    }
}

@Composable
private fun RentTable(withIcons: Boolean) {
    RentLine("Sin casas", Rents[0], false)
    for (k in 1..3) {
        RentLine(if (k == 1) "1 casa" else "$k casas", Rents[k], k == Sample.houses,
            if (withIcons) ({ BuildingIcons(Holding(0, houses = k), 14.dp) }) else null)
    }
    RentLine("Hotel", Rents[4], false, if (withIcons) ({ IconImage(Icon.HOTEL, 14.dp) }) else null)
}

@Composable
private fun SheetCard(modifier: Modifier) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column {
            Art(Modifier.fillMaxWidth().aspectRatio(200f / 140f))
            Row(Modifier.fillMaxWidth().background(Boyaca).padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("Villa de Leyva", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
                Text("Boyacá", color = Color.White, fontSize = 14.sp)
            }
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Owner()
                HorizontalDivider()
                RentTable(withIcons = false)
                HorizontalDivider()
                Text("Precio $1000 · Casa $1000 · Hotel $2000 · Hipoteca $500", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ChivaStripe() {
    val colors = listOf(0xFFE63946, 0xFFFFC21A, 0xFF1D7BEF, 0xFF2BB04A, 0xFFE5007E).map { Color(it) }
    Row(Modifier.fillMaxWidth().height(8.dp).background(Ink)) {
        repeat(20) { Box(Modifier.weight(1f).fillMaxSize().padding(horizontal = 1.dp, vertical = 2.dp).background(colors[it % 5])) }
    }
}

@Composable
private fun ChivaCard(modifier: Modifier) {
    Card(
        modifier.padding(24.dp).fillMaxWidth().border(3.dp, Ink, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column {
            ChivaStripe()
            Box(Modifier.fillMaxWidth().background(Boyaca).padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Text("VILLA DE LEYVA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
            Art(Modifier.fillMaxWidth().aspectRatio(200f / 140f).border(2.dp, Ink))
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Boyacá · Precio $1000", fontWeight = FontWeight.Bold)
                RentTable(withIcons = true)
                Text("Casa $1000 · Hotel $2000 · Hipoteca $500", fontSize = 13.sp)
                Owner()
            }
            ChivaStripe()
        }
    }
}

@Composable
private fun RentChip(now: Boolean, amount: Int, icon: @Composable () -> Unit) {
    Column(
        Modifier.clip(RoundedCornerShape(8.dp)).background(if (now) Color(0xFFFFE08A) else Color(0xFFEDE7F6)).padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.height(16.dp), contentAlignment = Alignment.Center) { icon() }
        Text("$$amount", fontSize = 12.sp, fontWeight = if (now) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun CompactCard(modifier: Modifier) {
    Surface(modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Art(Modifier.size(110.dp).clip(RoundedCornerShape(10.dp)).border(2.dp, Ink, RoundedCornerShape(10.dp)), crop = true)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(Modifier.width(40.dp).height(6.dp).background(Boyaca))
                    Text("Villa de Leyva", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Boyacá · Precio $1000", fontSize = 13.sp)
                    Text("Casa $1000 · Hotel $2000", fontSize = 13.sp)
                    Text("Hipoteca $500", fontSize = 13.sp)
                    Owner()
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                RentChip(false, Rents[0]) { Text("—", fontSize = 11.sp) }
                for (k in 1..3) RentChip(k == Sample.houses, Rents[k]) { BuildingIcons(Holding(0, houses = k), 12.dp) }
                RentChip(false, Rents[4]) { IconImage(Icon.HOTEL, 14.dp) }
            }
            Button(onClick = {}, Modifier.fillMaxWidth()) { Text("Cerrar") }
        }
    }
}
