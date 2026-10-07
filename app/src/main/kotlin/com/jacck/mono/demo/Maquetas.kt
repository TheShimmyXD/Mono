package com.jacck.mono.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.board.PlayerColors

/**
 * Maquetas de la pantalla que toca (M-029), con datos falsos; se abren con el extra `maqueta`.
 * F3.5, menú de nueva partida: A (todo en una pantalla), B (asistente, paso de nombres)
 * y C (mesa de jugadores con tarjetas).
 */
@Composable
fun Maqueta(letra: String) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        when (letra) {
            "A" -> MaquetaA()
            "B" -> MaquetaB()
            else -> MaquetaC()
        }
    }
}

private val nombres = listOf("Ana", "Beto", "Caro")

@Composable
private fun Ficha(i: Int, size: Int = 22) =
    Box(Modifier.size(size.dp).background(PlayerColors[i], CircleShape))

@Composable
private fun ColumnScope.MaquetaA() {
    Text("Nueva partida", style = MaterialTheme.typography.headlineMedium)
    Text("Juego", fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = true, onClick = {}, label = { Text("Clásico") })
        FilterChip(selected = false, onClick = {}, label = { Text("Tío Rico") })
    }
    Text("Jugadores", fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (2..6).forEach { FilterChip(selected = it == 3, onClick = {}, label = { Text("$it") }) }
    }
    nombres.forEachIndexed { i, n ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Ficha(i)
            OutlinedTextField(value = n, onValueChange = {}, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
    }
    Spacer(Modifier.weight(1f))
    Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Empezar", fontSize = 18.sp) }
}

@Composable
private fun ColumnScope.MaquetaB() {
    Text("Paso 3 de 3", color = MaterialTheme.colorScheme.outline)
    Text("¿Cómo se llaman?", style = MaterialTheme.typography.headlineMedium)
    Text("Clásico · 3 jugadores", color = MaterialTheme.colorScheme.outline)
    nombres.forEachIndexed { i, n ->
        Card(colors = CardDefaults.cardColors(containerColor = PlayerColors[i].copy(alpha = 0.15f)), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Ficha(i, 32)
                Text(n, fontSize = 22.sp)
            }
        }
    }
    Spacer(Modifier.weight(1f))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f).height(56.dp)) { Text("Atrás") }
        Button(onClick = {}, modifier = Modifier.weight(1f).height(56.dp)) { Text("Empezar") }
    }
    Text("Paso 1: Clásico o Tío Rico (dos tarjetas) · Paso 2: botones 2..6", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
}

@Composable
private fun ColumnScope.MaquetaC() {
    Text("Nueva partida", style = MaterialTheme.typography.headlineMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        PresetCard("Clásico", "$1500 · 40 casillas", true, Modifier.weight(1f))
        PresetCard("Tío Rico", "$26 400 · 44 casillas", false, Modifier.weight(1f))
    }
    Text("En la mesa (3)", fontWeight = FontWeight.Bold)
    nombres.forEachIndexed { i, n ->
        Row(
            Modifier.fillMaxWidth().border(1.dp, PlayerColors[i], RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Ficha(i, 28)
            Spacer(Modifier.width(12.dp))
            Text(n, fontSize = 20.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = {}) { Text("Quitar") }
        }
    }
    OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("+ Añadir jugador") }
    Spacer(Modifier.weight(1f))
    Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Empezar con 3", fontSize = 18.sp) }
}

@Composable
private fun PresetCard(title: String, detail: String, selected: Boolean, modifier: Modifier) {
    val edge = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray
    Column(modifier.border(if (selected) 3.dp else 1.dp, edge, RoundedCornerShape(12.dp)).padding(12.dp)) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(detail, fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
    }
}
