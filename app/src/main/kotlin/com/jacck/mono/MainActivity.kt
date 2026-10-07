package com.jacck.mono

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.jacck.mono.demo.DemoBoardScreen

/** Etiqueta de los Log.* de la app; `telefono.py log` filtra por ella. */
const val LOG_TAG = "Mono"

/**
 * Pantalla de arranque: por ahora el tablero de muestra (F3.2). Los extras del intent eligen cuál:
 * `n` (16..48, múltiplo de 4; 40 por defecto) y `jugadores` (2-6; 4 por defecto).
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val n = intent.getIntExtra("n", 40)
        val players = intent.getIntExtra("jugadores", 4).coerceIn(2, 6)
        Log.i(LOG_TAG, "MainActivity creada: tablero de $n con $players jugadores")
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DemoBoardScreen(n, players)
                }
            }
        }
    }
}
