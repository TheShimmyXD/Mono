package com.jacck.mono.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.BotonChiva
import com.jacck.mono.Calcomania
import com.jacck.mono.Chiva
import com.jacck.mono.OpcionChiva
import com.jacck.mono.PantallaChiva

/**
 * Maquetas de la pantalla que se está diseñando (M-029), con datos falsos; se abren con el extra
 * `maqueta`. F4.4, tableros propios: A lista en el menú con Editar / Duplicar / Borrar y el nombre
 * ahí mismo, B pantalla «Mis tableros» con una tarjeta por tablero, C fichas en el menú y el nombre
 * arriba en el editor («Guardar» / «Guardar como nuevo»).
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

/** Tableros falsos: nombre, casillas, salario y si es propio. */
private val tableros = listOf(
    Triple("Clásico", 40, false), Triple("Tío Rico", 40, false),
    Triple("Clásico de 24", 24, true), Triple("La candelaria", 40, true),
)

@Composable
private fun Titulo(texto: String) =
    Text(texto, fontSize = 30.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

@Composable
private fun Nota(texto: String) = Text(texto, fontSize = 14.sp, color = Chiva.Tinta.copy(alpha = 0.7f))

/** A: la tarjeta «Juego» lista todos; con uno propio elegido, su nombre y Editar / Duplicar / Borrar. */
@Composable
private fun MaquetaA() {
    Titulo("Nueva partida")
    Calcomania {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Juego", fontSize = 18.sp)
            tableros.forEach { (n, c, propio) ->
                OpcionChiva("${if (propio) "★ " else ""}$n · $c casillas", n == "Clásico de 24", {}, Modifier.fillMaxWidth())
            }
            OutlinedTextField("Clásico de 24", {}, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonChiva("Editar", {}, Modifier.weight(1f), principal = false)
                BotonChiva("Duplicar", {}, Modifier.weight(1f), principal = false)
                BotonChiva("Borrar", {}, Modifier.weight(1f), principal = false)
            }
            Nota("Editar el Clásico o Tío Rico guarda una copia ★: los originales no cambian.")
        }
    }
    Calcomania { Text("Jugadores …", Modifier.padding(12.dp), fontSize = 18.sp) }
}

/** B: el menú solo dice cuál y «Cambiar»; la pantalla «Mis tableros» tiene una tarjeta por tablero. */
@Composable
private fun MaquetaB() {
    Calcomania {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Juego: Clásico de 24", fontSize = 18.sp, modifier = Modifier.weight(1f).padding(top = 10.dp))
            BotonChiva("Cambiar", {}, Modifier.weight(0.7f), principal = false)
        }
    }
    Titulo("Mis tableros")
    tableros.forEach { (n, c, propio) ->
        Calcomania {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${if (propio) "★ " else ""}$n", fontSize = 18.sp)
                Nota("$c casillas · salario $200${if (propio) "" else " · original"}")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BotonChiva("Jugar", {}, Modifier.weight(1f), principal = n == "Clásico de 24")
                    BotonChiva("Editar", {}, Modifier.weight(1f), principal = false)
                    BotonChiva("Copiar", {}, Modifier.weight(1f), principal = false)
                    if (propio) BotonChiva("Borrar", {}, Modifier.weight(1f), principal = false)
                }
            }
        }
    }
}

/** C: fichas en el menú (como hoy, con los propios); el nombre y «Guardar como nuevo» van en el editor. */
@Composable
private fun MaquetaC() {
    Calcomania {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Juego", fontSize = 18.sp)
            tableros.chunked(2).forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    fila.forEach { (n, _, propio) -> OpcionChiva("${if (propio) "★ " else ""}$n", n == "Clásico de 24", {}, Modifier.weight(1f)) }
                }
            }
            BotonChiva("Editar casillas y reglas", {}, principal = false)
        }
    }
    Text("En el editor ↓", fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    Calcomania {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField("Clásico de 24", {}, label = { Text("Nombre del tablero") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OpcionChiva("Casillas", true, {}, Modifier.weight(1f))
                OpcionChiva("Reglas", false, {}, Modifier.weight(1f))
            }
            Text("(anillo y ficha como hoy)", fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonChiva("Guardar", {}, Modifier.weight(1f))
                BotonChiva("Guardar como nuevo", {}, Modifier.weight(1.4f), principal = false)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonChiva("Borrar tablero", {}, Modifier.weight(1f), principal = false)
                BotonChiva("Volver sin guardar", {}, Modifier.weight(1.2f), principal = false)
            }
        }
    }
}
