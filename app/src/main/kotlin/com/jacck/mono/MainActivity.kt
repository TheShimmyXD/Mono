package com.jacck.mono

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jacck.mono.demo.DemoBoardScreen
import com.jacck.mono.demo.Maqueta
import com.jacck.mono.demo.withSampleProperties
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.SavedGame
import com.jacck.mono.game.GameScreen
import com.jacck.mono.game.GameViewModel
import com.jacck.mono.game.NewGameScreen
import com.jacck.mono.game.SaveFile

/** Etiqueta de los Log.* de la app; `telefono.py log` filtra por ella. */
const val LOG_TAG = "Mono"

/**
 * Pantalla de arranque: el menú de nueva partida (F3.5) y después la partida. Con alguno de los
 * extras de prueba se salta el menú: `jugadores` (2-6; 4), `tio_rico` (true: ese preset) y `semilla`
 * (repite una partida).
 * Con `n` (16..48) se abre en cambio el tablero de muestra de F3.2. Para probar F3.4 sin jugar media
 * partida: `propiedades` (true: quien empieza tiene marrones, celestes y una estación hipotecada)
 * y `hoja` (true: abre «Mis propiedades»). Con `maqueta` (letra) se abre la maqueta de la pantalla
 * que se está diseñando (`demo/Maquetas.kt`, M-029). La partida del menú se guarda tras cada jugada
 * y el menú ofrece seguirla (F3.6, D-26); las de los extras de prueba no se guardan.
 */
/** Extras que abren la partida sin pasar por el menú (pruebas por adb). */
private val GAME_EXTRAS = listOf("jugadores", "tio_rico", "semilla", "propiedades", "hoja")

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val players = intent.getIntExtra("jugadores", 4).coerceIn(2, 6)
        val demo = if (intent.hasExtra("n")) intent.getIntExtra("n", 40) else null
        val preset = if (intent.getBooleanExtra("tio_rico", false)) Preset.TIO_RICO else Preset.CLASSIC
        val seed = intent.getLongExtra("semilla", System.currentTimeMillis())
        val sample = intent.getBooleanExtra("propiedades", false)
        val sheet = intent.getBooleanExtra("hoja", false)
        val mockup = intent.getStringExtra("maqueta")
        val direct = GAME_EXTRAS.any(intent::hasExtra)
        val saveFile = SaveFile(filesDir)
        val names = resources.getStringArray(R.array.default_names).take(players)
        Log.i(LOG_TAG, "MainActivity creada: ${demo?.let { "muestra de $it" } ?: "$preset"}, $players jugadores, semilla $seed")
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (mockup != null) {
                        Maqueta(mockup)
                    } else if (demo != null) {
                        DemoBoardScreen(demo, players)
                    } else {
                        var chosen by rememberSaveable { mutableStateOf(if (direct) preset to names else null) }
                        var resumed by rememberSaveable { mutableStateOf(false) }
                        val saved = remember { if (direct) null else saveFile.read() }
                        val resume = saved.takeIf { resumed }
                        val game = chosen
                        if (game == null && resume == null) {
                            NewGameScreen(saved?.state, onResume = { resumed = true }) { p, n ->
                                Log.i(LOG_TAG, "menú: $p con ${n.size} jugadores")
                                chosen = p to n
                            }
                        } else {
                            val keep: (GameConfig, GameState) -> Unit = if (direct) { _, _ -> } else { c, s -> saveFile.write(SavedGame(c, s)) }
                            val vm = viewModel {
                                if (resume != null) {
                                    Log.i(LOG_TAG, "sigue la partida guardada: turno ${resume.state.turn}, ${resume.state.players.map { it.name to it.money }}")
                                    GameViewModel(resume.config, resume.state.players.map { it.name }, seed, resumed = resume.state, onState = keep)
                                } else {
                                    val (p, n) = requireNotNull(game)
                                    GameViewModel(p.load(), n, seed, prepare = if (sample) ::withSampleProperties else { s -> s }, onState = keep)
                                }
                            }
                            GameScreen(vm, openProperties = sheet) { System.currentTimeMillis() }
                        }
                    }
                }
            }
        }
    }
}
