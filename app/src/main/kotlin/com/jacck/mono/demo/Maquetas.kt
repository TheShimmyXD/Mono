package com.jacck.mono.demo

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.PantallaChiva

/**
 * Maquetas de la pantalla que se está diseñando (M-029), con datos falsos; se abren con el extra
 * `maqueta`. Ahora no hay ninguna en diseño (la última, FC.2, quedó en `capturas/fc2_maquetas.png` y `fc2_maquetas2.png`, D-60).
 */
@Composable
fun Maqueta(letra: String) {
    PantallaChiva {
        Text("Sin maqueta en diseño ($letra)", Modifier.padding(16.dp), fontSize = 18.sp)
    }
}
