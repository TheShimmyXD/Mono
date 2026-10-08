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
import com.jacck.mono.board.Icon

/**
 * Maquetas de la pantalla que se está diseñando (M-029), con datos falsos; se abren con el extra
 * `maqueta`. F5.3, crear o unirse a una partida por Bluetooth: A sala de espera desde el menú (el
 * amigo toma el último jugador; «Unirme» lista los emparejados), B cada jugador del menú elige
 * «Aquí» u «Otro teléfono», C como A pero «Unirme» busca teléfonos cercanos sin emparejar antes.
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

@Composable
private fun Titulo(texto: String) =
    Text(texto, fontSize = 30.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

@Composable
private fun Nota(texto: String) = Text(texto, fontSize = 14.sp, color = Chiva.Tinta.copy(alpha = 0.7f))

/** A: la sala con lo que se crea (tablero y jugadores del menú) y, abajo, unirse a un emparejado. */
@Composable
private fun MaquetaA() {
    Titulo("Por Bluetooth")
    Calcomania {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Crear partida", fontSize = 20.sp)
            Nota("Clásico · 40 casillas · Ana, Beto y Caro aquí")
            Text("Caro juega en el otro teléfono", fontSize = 16.sp)
            Text("Esperando… este teléfono se llama «Redmi Note 13 Pro»", fontSize = 16.sp)
            BotonChiva("Cancelar", {}, principal = false)
        }
    }
    Calcomania {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Unirme a la de un amigo", fontSize = 20.sp)
            OutlinedTextField("Caro", {}, label = { Text("Mi nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            BotonChiva("Teléfono de Juan", {}, principal = false)
            BotonChiva("PC-Jacck", {}, principal = false)
            Nota("¿No sale? Emparéjalos antes en Ajustes › Bluetooth.")
        }
    }
}

/** B: el menú de siempre; cada jugador dice dónde juega y «Empezar» espera al otro teléfono. */
@Composable
private fun MaquetaB() {
    Titulo("Nueva partida")
    BotonChiva("Unirme a la partida de un amigo", {}, principal = false, icono = Icon.ENLACE)
    Calcomania { Text("Juego: Clásico · 40 casillas", Modifier.padding(12.dp), fontSize = 18.sp) }
    Calcomania {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Jugadores", fontSize = 18.sp)
            listOf("Ana" to true, "Beto" to true, "Caro" to false, "Dani" to false).forEach { (n, aqui) ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(n, fontSize = 18.sp, modifier = Modifier.weight(1f).padding(top = 10.dp))
                    OpcionChiva("Aquí", aqui, {}, Modifier.weight(1f))
                    OpcionChiva("Otro", !aqui, {}, Modifier.weight(1f), Icon.ENLACE)
                }
            }
            Nota("Caro y Dani juegan en el otro teléfono.")
        }
    }
    BotonChiva("Esperar al otro teléfono", {})
}

/** C: unirse buscando teléfonos cercanos; uno nuevo pide emparejar al tocarlo. */
@Composable
private fun MaquetaC() {
    Titulo("Unirme por Bluetooth")
    Calcomania {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField("Caro", {}, label = { Text("Mi nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Text("Buscando teléfonos con Mono abierto…", fontSize = 16.sp)
            BotonChiva("Teléfono de Juan · nuevo", {}, principal = false)
            BotonChiva("Redmi de Laura · emparejado", {}, principal = false)
            Nota("Uno nuevo pide emparejar al tocarlo (un código en los dos teléfonos).")
            BotonChiva("Buscar otra vez", {}, principal = false)
        }
    }
    Nota("Pide «Dispositivos cercanos» también para buscar (BLUETOOTH_SCAN).")
}
