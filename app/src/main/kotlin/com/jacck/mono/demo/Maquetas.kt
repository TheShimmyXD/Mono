package com.jacck.mono.demo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Maquetas de la pantalla que se está diseñando (M-029), con datos falsos; se abren con el extra
 * `maqueta`. Vacío entre pantallas: la última elegida fue el logo de la chiva (FB.5, D-38; las tres
 * opciones en `00e8e3e`).
 */
@Composable
fun Maqueta(letra: String) {
    Text("Sin maqueta $letra", Modifier.fillMaxSize().safeDrawingPadding().padding(20.dp))
}
