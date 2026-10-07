package com.jacck.mono.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.jacck.mono.OpcionChiva
import com.jacck.mono.PantallaChiva

/**
 * Maquetas del editor de reglas (F4.2a, M-029), con las cifras del Clásico y el salario cambiado a
 * $300 para ver cómo se marca un cambio. Se abren con el extra `maqueta` (A, B o C).
 */
@Composable
fun Maqueta(letra: String) {
    when (letra) {
        "A" -> MaquetaLista()
        "B" -> MaquetaPestanas()
        "C" -> MaquetaEsencial()
        else -> Text("Sin maqueta $letra", Modifier.fillMaxSize().safeDrawingPadding().padding(20.dp))
    }
}

/** Una regla de la maqueta: cifra con − / + o Sí / No; [antes] es el valor del preset si cambió. */
private sealed interface Fila { val texto: String }
private data class Cifra(override val texto: String, val valor: String, val antes: String? = null) : Fila
private data class SiNo(override val texto: String, val si: Boolean) : Fila

private val secciones = listOf(
    "Dinero" to listOf(
        Cifra("Dinero inicial", "$1500"), Cifra("Salario en la Salida", "$300", antes = "$200"),
        SiNo("Bote en Parada Libre", false), SiNo("El Banco no se queda sin dinero", true),
    ),
    "Dados y Cárcel" to listOf(
        SiNo("Los dobles dan otra tirada", true), Cifra("Dobles seguidos a la Cárcel", "3"),
        SiNo("Hay Cárcel", true), Cifra("Multa para salir", "$50"), Cifra("Turnos máximos dentro", "3"),
    ),
    "Casas y hoteles" to listOf(
        Cifra("Casas antes del hotel", "4"), SiNo("Construir parejo", true), SiNo("Solo al caer en el grupo", false),
        Cifra("Casas del Banco", "32"), Cifra("Hoteles del Banco", "12"), SiNo("Grupo completo cobra ×2", true),
    ),
    "Hipotecas" to listOf(
        SiNo("La hipotecada cobra alquiler", false), SiNo("Hipotecar con edificios", false), Cifra("Interés al levantarla", "10 %"),
    ),
)

/** A: todas las reglas en una lista larga por secciones (se desliza). */
@Composable
private fun MaquetaLista() {
    PantallaChiva {
        Titulo("Reglas · Clásico")
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            secciones.forEach { (nombre, filas) ->
                Text(nombre, fontSize = 18.sp, color = Chiva.Techo, modifier = Modifier.padding(top = 6.dp))
                filas.forEach { FilaRegla(it) }
            }
        }
        Botones()
    }
}

/** B: pestañas por tema; cada regla en su calcomanía con una línea que la explica. */
@Composable
private fun MaquetaPestanas() {
    val explica = mapOf(
        "Dinero inicial" to "Lo que recibe cada jugador al empezar.",
        "Salario en la Salida" to "Lo que cobra al caer o pasar por la Salida.",
        "Bote en Parada Libre" to "Impuestos y multas se juntan; quien cae ahí se los lleva.",
        "El Banco no se queda sin dinero" to "Si se acaban los billetes, se apunta en papel.",
    )
    PantallaChiva {
        Titulo("Reglas · Clásico")
        listOf(listOf("Dinero", "Dados", "Cárcel"), listOf("Casas", "Hipotecas", "Fin")).forEachIndexed { r, fila ->
            Row(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                fila.forEachIndexed { k, t -> OpcionChiva(t, elegida = r == 0 && k == 0, onClick = {}, modifier = Modifier.weight(1f)) }
            }
        }
        Column(Modifier.weight(1f).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            secciones[0].second.forEach { f ->
                Calcomania(Modifier.fillMaxWidth().padding(end = 4.dp)) {
                    Column(Modifier.padding(10.dp)) {
                        FilaRegla(f)
                        Text(explica[f.texto].orEmpty(), fontSize = 13.sp)
                    }
                }
            }
        }
        Botones()
    }
}

/** C: las 6 reglas que más se tocan en fichas grandes; las otras 28, detrás de «Más reglas». */
@Composable
private fun MaquetaEsencial() {
    val fichas = listOf(
        Cifra("Dinero inicial", "$1500"), Cifra("Salario", "$300", antes = "$200"), Cifra("Multa de la Cárcel", "$50"),
        Cifra("Casas antes del hotel", "4"), Cifra("Dobles a la Cárcel", "3"), Cifra("Casas del Banco", "32"),
    )
    PantallaChiva {
        Titulo("Reglas · Clásico")
        Column(Modifier.weight(1f).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            fichas.chunked(2).forEach { par ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    par.forEach { f ->
                        Calcomania(Modifier.weight(1f).padding(end = 4.dp)) {
                            Column(Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(f.texto, fontSize = 14.sp, textAlign = TextAlign.Center, maxLines = 1)
                                Text(f.valor, fontSize = 30.sp, color = if (f.antes != null) Chiva.Techo else Chiva.Tinta)
                                Text(f.antes?.let { "antes $it" } ?: " ", fontSize = 12.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { Paso("−"); Paso("+") }
                            }
                        }
                    }
                }
            }
            BotonChiva("Más reglas (28) ›", {}, principal = false)
            BotonChiva("Volver a las del Clásico", {}, principal = false)
        }
        Botones()
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(texto, fontSize = 22.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
}

/** Una regla: el texto a la izquierda; a la derecha − cifra + o Sí / No. */
@Composable
private fun FilaRegla(f: Fila) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(f.texto, fontSize = 15.sp)
            if (f is Cifra && f.antes != null) Text("antes ${f.antes}", fontSize = 12.sp, color = Chiva.Techo)
        }
        when (f) {
            is Cifra -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Paso("−")
                Text(f.valor, fontSize = 17.sp, textAlign = TextAlign.Center, color = if (f.antes != null) Chiva.Techo else Chiva.Tinta,
                    modifier = Modifier.size(width = 64.dp, height = 24.dp))
                Paso("+")
            }
            is SiNo -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OpcionChiva("Sí", f.si, {})
                OpcionChiva("No", !f.si, {})
            }
        }
    }
}

/** Botón cuadrado de − o +. */
@Composable
private fun Paso(signo: String) {
    val forma = RoundedCornerShape(8.dp)
    Box(Modifier.size(34.dp).background(Color.White, forma).border(2.dp, Chiva.Tinta, forma), contentAlignment = Alignment.Center) {
        Text(signo, fontSize = 20.sp)
    }
}

@Composable
private fun Botones() {
    Row(Modifier.padding(start = 16.dp, end = 19.dp, bottom = 14.dp, top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        BotonChiva("Guardar reglas", {}, Modifier.weight(1f))
        BotonChiva("Listo", {}, Modifier.weight(1f), principal = false)
    }
}
