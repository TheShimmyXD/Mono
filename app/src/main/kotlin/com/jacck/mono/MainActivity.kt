package com.jacck.mono

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jacck.mono.demo.DemoBoardScreen
import com.jacck.mono.demo.Maqueta
import com.jacck.mono.demo.withSampleProperties
import com.jacck.mono.demo.withPhase
import com.jacck.mono.enlace.GuestScreen
import com.jacck.mono.enlace.LinkedGame
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.validate
import com.jacck.mono.engine.model.SavedGame
import com.jacck.mono.game.BoardChoice
import com.jacck.mono.game.BoardShelf
import com.jacck.mono.game.EditorScreen
import com.jacck.mono.game.EditorViewModel
import com.jacck.mono.game.RuleTab
import com.jacck.mono.game.GameScreen
import com.jacck.mono.game.GameViewModel
import com.jacck.mono.game.MAX_PLAYERS
import com.jacck.mono.game.NewGameScreen
import com.jacck.mono.game.SaveFile
import com.jacck.mono.game.boardChoices
import com.jacck.mono.game.boardName
import com.jacck.mono.game.copyName
import com.jacck.mono.game.playerNames

/** Etiqueta de los Log.* de la app; `telefono.py log` filtra por ella. */
const val LOG_TAG = "Mono"

/**
 * Pantalla de arranque: el menú de nueva partida (F3.5) y después la partida. Con alguno de los
 * extras de prueba se salta el menú: `jugadores` (2-6; 4), `tio_rico` (true: ese preset) y `semilla`
 * (repite una partida).
 * Con `n` (16..48) se abre en cambio el tablero de muestra de F3.2. Para probar F3.4 sin jugar media
 * partida: `propiedades` (true: quien empieza tiene marrones, celestes y una estación hipotecada)
 * y `hoja` (true: abre «Mis propiedades»); `casilla` (índice) abre la carta de esa casilla (FA.3); `fase` (`compra`, `subasta`, `carcel`, `impuesto`,
 * `deuda`, `fin`; `alquiler` y `carta`, FD.2; `sueldo` y `banco`, FD.3) abre ese diálogo de turno, sin el aviso inicial (FB.2, `demo/SamplePhases.kt`). Con `maqueta` (letra) se abre la maqueta de la pantalla
 * que se está diseñando (`demo/Maquetas.kt`, M-029). Con `editor` (índice, `--ei`) se abre el editor de casillas del Clásico con esa casilla elegida (F4.1, D-39); con `reglas` (tema: `dinero`, `dados`, `casas`, `alquiler`, `hipotecas`, `fin`), su pestaña «Reglas» en ese tema (F4.2, D-40); con `quitar` (índices separados por lo que no sea dígito, `--es quitar 13+14`), el editor con esas casillas ya quitadas (F4.3, D-41), y `abajo` (true) lo abre desplazado hasta la ficha. Con `tablero` (`CLASSIC`, `TIO_RICO` o el id de uno propio, `t1`) el menú abre con ese tablero elegido (F4.4, D-42). Con `enlace` (`sala`) se abre la sala del anfitrión con el Clásico y el último de `jugadores` en el otro teléfono (F5.3, `enlace/HostScreen.kt`, D-47), y con `unirme`, la pantalla del invitado (`enlace/GuestScreen.kt`). Con `maquina` (índices, `--es maquina 1+2`) la partida abre con esos jugadores en manos de la máquina (F5.8b, D-55), y con `seguro` (true), junto a `hoja` o `fase=deuda`, sale ya el «¿Seguro?» de la quiebra (D-56). Con `oculto` (true) abre con el dinero oculto y con `pausa` (ms, `--ei`; 900) la máquina espera eso antes de cada jugada, para que la grabación alcance la primera (FD.2, D-66). La partida del menú se guarda tras cada jugada
 * y el menú ofrece seguirla (F3.6, D-26); las de los extras de prueba no se guardan.
 */
/** Extras que abren la partida sin pasar por el menú (pruebas por adb). */
private val GAME_EXTRAS = listOf("jugadores", "tio_rico", "semilla", "propiedades", "hoja", "tarjeta", "atras", "casilla", "fase", "maquina", "seguro", "oculto", "pausa")

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val players = intent.getIntExtra("jugadores", 4).coerceIn(2, MAX_PLAYERS)
        val demo = if (intent.hasExtra("n")) intent.getIntExtra("n", 40) else null
        val preset = if (intent.getBooleanExtra("tio_rico", false)) Preset.TIO_RICO else Preset.CLASSIC
        val seed = intent.getLongExtra("semilla", System.currentTimeMillis())
        val sample = intent.getBooleanExtra("propiedades", false)
        val sheet = intent.getBooleanExtra("hoja", false)
        val sure = intent.getBooleanExtra("seguro", false)
        val back = intent.getBooleanExtra("atras", false)
        val hidden = intent.getBooleanExtra("oculto", false)
        val pause = intent.getIntExtra("pausa", 900).toLong()
        val card = if (intent.hasExtra("tarjeta")) intent.getIntExtra("tarjeta", 0) else null
        val square = if (intent.hasExtra("casilla")) intent.getIntExtra("casilla", 0) else null
        val mockup = intent.getStringExtra("maqueta")
        val phase = intent.getStringExtra("fase")
        val link = intent.getStringExtra("enlace")
        val machineSeats = intent.getStringExtra("maquina")?.split(Regex("\\D+"))?.filter { it.isNotEmpty() }?.map { it.toInt() }?.filter { it < players }.orEmpty()
        val direct = GAME_EXTRAS.any(intent::hasExtra)
        val saveFile = SaveFile(filesDir)
        val shelf = BoardShelf(filesDir)
        val names = playerNames(emptyList(), resources.getStringArray(R.array.default_names).toList(), players, machineSeats.toSet()) {
            getString(R.string.menu_bot_name, it)
        }
        Log.i(LOG_TAG, "MainActivity creada: ${demo?.let { "muestra de $it" } ?: "$preset"}, $players jugadores, semilla $seed")
        setContent {
            ChivaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (link == "sala") {
                        LinkedGame("sala", Preset.CLASSIC.load(), names, emptyList(), setOf(names.lastIndex), emptySet(), seed) { finish() }
                    } else if (link == "unirme") {
                        GuestScreen { finish() }
                    } else if (mockup != null) {
                        Maqueta(mockup)
                    } else if (demo != null) {
                        DemoBoardScreen(demo, players)
                    } else {
                        var chosen by rememberSaveable { mutableStateOf(if (direct) Triple(preset.name, names, emptyList<String>()) else null) }
                        var resumed by rememberSaveable { mutableStateOf(false) }
                        // Jugadores del otro teléfono (F5.3, D-47): con alguno, la partida espera en la sala.
                        var seats by rememberSaveable { mutableStateOf(emptyList<Int>()) }
                        var bots by rememberSaveable { mutableStateOf(machineSeats) } // los de la máquina (F5.8b)
                        var joining by rememberSaveable { mutableStateOf(false) }
                        var rooms by rememberSaveable { mutableStateOf(0) } // una sala nueva cada vez (su ViewModel)
                        var games by rememberSaveable { mutableStateOf(0) } // y una partida nueva al volver del menú (D-63)
                        // Tableros (F4.4, D-42): los originales y los propios de `files/tableros`; `selected` y
                        // `editing` son su clave (nombre del preset o id). `opened` cuenta las veces que se abre
                        // el editor, para que cada vez empiece de lo guardado y no del ViewModel anterior.
                        var own by remember { mutableStateOf(shelf.list().also { Log.i(LOG_TAG, "tableros propios: ${it.map { b -> "${b.id} ${b.config.name}" }}") }) }
                        val boards = remember(own) { boardChoices(own) }
                        var selected by rememberSaveable { mutableStateOf(intent.getStringExtra("tablero") ?: Preset.CLASSIC.name) }
                        var editing by rememberSaveable { mutableStateOf(if (intent.hasExtra("editor") || intent.hasExtra("reglas") || intent.hasExtra("quitar")) Preset.CLASSIC.name else null) }
                        var opened by rememberSaveable { mutableStateOf(0) }
                        val board = boards.firstOrNull { it.key == selected } ?: boards.first()
                        // Guarda `config` en su tablero propio o, si es un original, en una copia que queda elegida.
                        val store: (BoardChoice, GameConfig) -> Unit = { b, config ->
                            val id = if (b.own) shelf.save(config, b.key)
                            else shelf.save(config.copy(name = copyName(b.config.name, boards.map { it.config.name })))
                            Log.i(LOG_TAG, "tablero guardado: $id ${shelf.list().first { it.id == id }.config.name}, ${config.squares.size} casillas")
                            own = shelf.list()
                            selected = id
                        }
                        val menuState = rememberSaveableStateHolder()
                        var saved by remember { mutableStateOf(
                            (if (direct) null else saveFile.read()).also { Log.i(LOG_TAG, "guardada al abrir: ${it?.state?.turn?.let { t -> "turno $t" } ?: "ninguna"}") }
                        ) }
                        val resume = saved.takeIf { resumed }
                        val game = chosen
                        val ed = boards.firstOrNull { it.key == editing }
                        if (joining) {
                            GuestScreen {
                                Log.i(LOG_TAG, "unirme: vuelve al menú")
                                joining = false
                            }
                        } else if (game == null && resume == null && ed != null) {
                            val evm = viewModel(key = "editor-${ed.key}-$opened") { EditorViewModel(ed.config)
                                .also { vm -> (intent.getStringExtra("quitar") ?: intent.getIntExtra("quitar", -1).takeIf { it >= 0 }?.toString())?.split(Regex("\\D+"))?.mapNotNull { it.trim().toIntOrNull() }?.sortedDescending()
                                    ?.filter { it in vm.config.squares.indices }?.forEach { vm.select(it); vm.remove() } }
                                .also { vm -> intent.getIntExtra("editor", -1).takeIf { it in vm.config.squares.indices }?.let(vm::select) }
                                .also { vm -> RuleTab.entries.firstOrNull { it.name.equals(intent.getStringExtra("reglas"), ignoreCase = true) }
                                    ?.let { vm.showRules = true; vm.ruleTab = it } }
                            }
                            EditorScreen(evm, toBottom = intent.getBooleanExtra("abajo", false)) {
                                if (evm.config != ed.config) store(ed, evm.config)
                                Log.i(LOG_TAG, "editor: vuelve al menú${if (evm.config == ed.config) " sin cambios" else ""}")
                                editing = null
                                opened++
                            }
                        } else if (game == null && resume == null) {
                            // El menú conserva lo escrito (jugadores, personajes) al ir al editor y volver.
                            menuState.SaveableStateProvider("menu") {
                                val broken = remember(own) { own.filter { validate(it.config).isNotEmpty() }.map { it.id }.toSet() }
                                NewGameScreen(
                                    saved?.state, onResume = { resumed = true }, boards = boards, selected = board.key, onSelect = { selected = it },
                                    broken = broken, onEdit = { editing = board.key }, onJoin = { joining = true }, onDuplicate = { store(board.copy(own = false), board.config) },
                                    onRename = { typed ->
                                        val name = boardName(typed, board.config.name)
                                        if (name != board.config.name) { shelf.save(board.config.copy(name = name), board.key); own = shelf.list() }
                                    },
                                    onDelete = {
                                        shelf.delete(board.key)
                                        Log.i(LOG_TAG, "tablero borrado: ${board.key} ${board.config.name}")
                                        own = shelf.list()
                                        selected = Preset.CLASSIC.name
                                    },
                                ) { n, t, s, b ->
                                    Log.i(LOG_TAG, "menú: ${board.key} ${board.config.name} (${board.config.squares.size} casillas) con ${n.size} jugadores, personajes $t, otro teléfono $s, máquina $b")
                                    seats = s.sorted()
                                    bots = b.sorted()
                                    chosen = Triple(board.key, n, t)
                                }
                            }
                        } else if (game != null && resume == null && seats.isNotEmpty()) {
                            val config = boards.firstOrNull { it.key == game.first }?.config ?: Preset.CLASSIC.load()
                            LinkedGame("sala-$rooms", config, game.second, game.third, seats.toSet(), bots.toSet(), seed) {
                                Log.i(LOG_TAG, "sala: vuelve al menú")
                                chosen = null
                                seats = emptyList()
                                bots = emptyList()
                                rooms++
                            }
                        } else {
                            val machines = resume?.bots ?: bots.toSet()
                            // Los extras de prueba valen solo para la primera partida; las del menú se guardan (D-63).
                            val test = direct && games == 0
                            val keep: (GameConfig, GameState) -> Unit = if (test) { _, _ -> } else { c, s ->
                                saveFile.write(SavedGame(c, s, machines))
                                Log.i(LOG_TAG, "guardada: turno ${s.turn}, ${s.phase::class.simpleName}")
                            }
                            val vm = viewModel(key = "partida-$games") {
                                if (resume != null) {
                                    Log.i(LOG_TAG, "sigue la partida guardada: turno ${resume.state.turn}, ${resume.state.players.map { it.name to it.money }}")
                                    GameViewModel(resume.config, resume.state.players.map { it.name }, seed, resumed = resume.state, onState = keep, bots = machines)
                                } else {
                                    val (p, n, t) = requireNotNull(game)
                                    val config = boards.firstOrNull { it.key == p }?.config ?: Preset.CLASSIC.load()
                                    val prepare: (GameState) -> GameState = when {
                                        test && phase != null -> withPhase(config, phase)
                                        test && sample -> ::withSampleProperties
                                        else -> { s -> s }
                                    }
                                    GameViewModel(config, n, seed, prepare = prepare, onState = keep, tokens = t, bots = machines, pause = if (test) pause else 900).also { if (test && (phase != null || card != null || back)) it.dismissNotices() }
                                }
                            }
                            GameScreen(
                                vm, openProperties = test && sheet, openSquare = square.takeIf { test }, askBankruptcy = test && sure,
                                openCard = card.takeIf { test }, openMenu = test && back, hideMoney = test && hidden,
                                onMenu = {
                                    saved = saveFile.read()
                                    Log.i(LOG_TAG, "partida: vuelve al menú; guardada ${saved?.state?.turn?.let { t -> "turno $t" } ?: "ninguna"}")
                                    chosen = null
                                    resumed = false
                                    bots = emptyList()
                                    games++
                                },
                            ) { System.currentTimeMillis() }
                        }
                    }
                }
            }
        }
    }
}
