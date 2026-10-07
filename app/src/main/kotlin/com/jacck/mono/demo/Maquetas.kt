package com.jacck.mono.demo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.Calcomania
import com.jacck.mono.PantallaChiva
import com.jacck.mono.R

/**
 * Maquetas de la pantalla que se está diseñando (M-029), con datos falsos; se abren con el extra
 * `maqueta`. FB.5: tres logos sin texto (`arte.py`, `LOGOS`), cada uno en grande y a tamaño de
 * lanzador con máscara redonda y cuadrada. La de FB.4 (escoger personaje) está en `6e7461c`.
 */
@Composable
fun Maqueta(letra: String) {
    PantallaChiva {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Logo de Mono ($letra)", fontSize = 22.sp)
            Opcion("A · El mono en el sol", R.drawable.logo_mono_fondo, R.drawable.logo_mono_frente)
            Opcion("B · La chiva", R.drawable.logo_chiva_fondo, R.drawable.logo_chiva_frente)
            Opcion("C · El dado de colores", R.drawable.logo_dado_fondo, R.drawable.logo_dado_frente)
        }
    }
}

private val Squircle = RoundedCornerShape(30)

@Composable
private fun Opcion(titulo: String, fondo: Int, frente: Int) {
    Calcomania {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(titulo, fontSize = 17.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Icono(fondo, frente, 120.dp, Squircle)
                Icono(fondo, frente, 56.dp, Squircle)
                Icono(fondo, frente, 56.dp, CircleShape)
                Icono(fondo, frente, 40.dp, Squircle)
            }
        }
    }
}

/**
 * Como el lanzador: las dos capas de 108 se dibujan a 1,5 veces el tamaño y la máscara deja ver
 * solo las 72 del centro.
 */
@Composable
private fun Icono(fondo: Int, frente: Int, tam: Dp, forma: Shape) {
    Box(Modifier.size(tam).clip(forma), contentAlignment = Alignment.Center) {
        Image(painterResource(fondo), null, Modifier.requiredSize(tam * 1.5f))
        Image(painterResource(frente), null, Modifier.requiredSize(tam * 1.5f))
    }
}
