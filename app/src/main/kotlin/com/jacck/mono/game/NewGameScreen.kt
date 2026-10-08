package com.jacck.mono.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.BotonChiva
import com.jacck.mono.Calcomania
import com.jacck.mono.Chiva
import com.jacck.mono.DialogoChiva
import com.jacck.mono.OpcionChiva
import com.jacck.mono.PantallaChiva
import com.jacck.mono.R
import com.jacck.mono.board.Icon
import com.jacck.mono.board.IconImage
import com.jacck.mono.board.PlayerColors
import com.jacck.mono.board.Medallon
import com.jacck.mono.board.Personajes
import com.jacck.mono.engine.model.GameState

/**
 * Menú de nueva partida (F3.5, maqueta A, D-25): preset, cuántos juegan (2-4, D-57) y sus nombres, todo
 * en una pantalla. Una casilla vacía juega con el nombre de muestra (gris); con nombres repetidos
 * no se puede empezar. `rememberSaveable` guarda lo elegido si Android recrea la pantalla.
 * Con una partida guardada (`saved`), arriba va «Seguir la partida» (F3.6, D-26). Estilo chiva (FB.2c, D-33).
 * Bajo cada nombre, los 8 personajes (FB.4, D-36): el suyo grande con su color, los de otros atenuados.
 * Un juego editado con errores (`broken`, F4.3) no se puede empezar y lo dice en rojo.
 * Tableros (F4.4, maqueta A, D-42): la lista de `boards` (originales y propios, con `Icon.PROPIO`); con uno propio
 * elegido, su nombre se cambia ahí mismo (`onRename`) y se puede borrar (con aviso); todos se editan
 * y se duplican. Lo elegido (`selected`) lo guarda quien llama, porque duplicar elige la copia.
 */
@Composable
fun NewGameScreen(
    saved: GameState?, onResume: () -> Unit, boards: List<BoardChoice>, selected: String, onSelect: (String) -> Unit,
    broken: Set<String>, onEdit: () -> Unit, onDuplicate: () -> Unit, onRename: (String) -> Unit, onDelete: () -> Unit,
    onJoin: () -> Unit, onStart: (List<String>, List<String>, Set<Int>, Set<Int>) -> Unit,
) {
    val defaults = stringArrayResource(R.array.default_names).toList()
    val board = boards.firstOrNull { it.key == selected } ?: boards.first()
    var nameText by rememberSaveable(board.key) { mutableStateOf(board.config.name) }
    var asking by rememberSaveable { mutableStateOf(false) }
    var count by rememberSaveable { mutableStateOf(DEFAULT_PLAYERS) }
    var typed by rememberSaveable { mutableStateOf(List(defaults.size) { "" }) }
    var picks by rememberSaveable { mutableStateOf(List(defaults.size) { it }) }
    // Quién juega en el otro teléfono (F5.3, D-47, D-48); con alguno, «Empezar» abre la sala.
    var remote by rememberSaveable { mutableStateOf(List(defaults.size) { false }) }
    // Quién juega la máquina (F5.8b, maqueta B, D-55): el robot junto al nombre.
    var machine by rememberSaveable { mutableStateOf(List(defaults.size) { false }) }
    val bots = (0 until count).filter { machine[it] }.toSet()
    val seats = (0 until count).filter { remote[it] && !machine[it] }.toSet()
    val tokens = playerTokens(picks, count, Personajes.size)
    val botName = stringResource(R.string.menu_bot_name)
    val names = playerNames(typed, defaults, count, bots) { botName.format(it) }
    val repeated = repeatedNames(names)

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Chiva.Tinta, focusedBorderColor = Chiva.Techo,
        unfocusedContainerColor = Color.White, focusedContainerColor = Color.White,
        unfocusedPlaceholderColor = Chiva.Tinta.copy(alpha = 0.4f), focusedPlaceholderColor = Chiva.Tinta.copy(alpha = 0.4f),
        disabledTextColor = Chiva.Tinta, disabledBorderColor = Chiva.Tinta.copy(alpha = 0.4f), disabledContainerColor = Color.White,
    )
    PantallaChiva {
        Column(
            Modifier.weight(1f).imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                stringResource(R.string.menu_title), fontSize = 34.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )
            BotonChiva(stringResource(R.string.menu_join), onJoin, principal = false, icono = Icon.ENLACE)
            if (saved != null) {
                Calcomania(Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onResume)) {
                    Column(
                        Modifier.fillMaxWidth().background(Chiva.Azul).padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(stringResource(R.string.menu_resume), color = Color.White, fontSize = 20.sp)
                        Text(
                            stringResource(R.string.menu_resume_detail, saved.players.joinToString(", ") { it.name }, saved.turn + 1),
                            color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            Calcomania {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.menu_game), fontSize = 18.sp)
                    boards.forEach { b ->
                        OpcionChiva(
                            stringResource(R.string.menu_board, b.config.name, b.config.squares.size),
                            b.key == board.key, { onSelect(b.key) }, Modifier.fillMaxWidth(), icono = Icon.PROPIO.takeIf { b.own },
                        )
                    }
                    if (board.own) {
                        OutlinedTextField(
                            value = nameText,
                            onValueChange = { v -> nameText = v.take(MAX_BOARD_NAME); onRename(nameText) },
                            label = { Text(stringResource(R.string.menu_board_name)) },
                            placeholder = { Text(board.config.name) },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Row(Modifier.padding(end = 3.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BotonChiva(stringResource(R.string.menu_edit), onEdit, Modifier.weight(1f), principal = false)
                        BotonChiva(stringResource(R.string.menu_duplicate), onDuplicate, Modifier.weight(1f), principal = false)
                        if (board.own) BotonChiva(stringResource(R.string.menu_delete), { asking = true }, Modifier.weight(1f), principal = false)
                    }
                    if (!board.own) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconImage(Icon.PROPIO, 18.dp)
                        Text(stringResource(R.string.menu_copy_note), fontSize = 14.sp, color = Chiva.Tinta.copy(alpha = 0.7f))
                    }
                    if (board.key in broken) Text(stringResource(R.string.menu_broken), color = Chiva.Techo, fontSize = 15.sp)
                }
            }
            Calcomania {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.menu_players), fontSize = 18.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (2..MAX_PLAYERS).forEach { n -> OpcionChiva("$n", n == count, { count = n }, Modifier.weight(1f)) }
                    }
                    (0 until count).forEach { i ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Con el robot, el nombre es «Botty N» y no se escribe; lo escrito vuelve al quitarlo (D-57).
                                OutlinedTextField(
                                    value = if (machine[i]) names[i] else typed[i],
                                    onValueChange = { v -> typed = typed.toMutableList().also { it[i] = v.take(MAX_NAME) } },
                                    enabled = !machine[i],
                                    placeholder = { Text(defaults[i]) },
                                    singleLine = true,
                                    isError = i in repeated,
                                    supportingText = if (i in repeated) ({ Text(stringResource(R.string.menu_repeated)) }) else null,
                                    colors = fieldColors,
                                    modifier = Modifier.weight(1f),
                                )
                                val forma = RoundedCornerShape(10.dp)
                                Box(
                                    Modifier.padding(top = 4.dp).size(52.dp).clip(forma).background(Color.White).border(2.dp, Chiva.Tinta, forma)
                                        .clickable(role = Role.Switch, onClickLabel = stringResource(R.string.menu_machine)) {
                                            machine = machine.toMutableList().also { it[i] = !it[i] }
                                        },
                                    contentAlignment = Alignment.Center,
                                ) { IconImage(Icon.MAQUINA, 34.dp, Modifier.alpha(if (machine[i]) 1f else 0.35f)) }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Personajes.forEachIndexed { k, (_, pj) ->
                                    val owner = tokens.indexOf(k)
                                    Medallon(
                                        pj, if (owner >= 0) PlayerColors[owner] else Color.LightGray, if (owner == i) 38.dp else 32.dp,
                                        Modifier.alpha(if (owner >= 0 && owner != i) 0.3f else 1f)
                                            .clickable(enabled = owner < 0, role = Role.Button) { picks = picks.toMutableList().also { it[i] = k } },
                                    )
                                }
                            }
                            if (machine[i]) Text(stringResource(R.string.menu_machine_note), fontSize = 15.sp, color = Chiva.Tinta.copy(alpha = 0.7f))
                            else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OpcionChiva(stringResource(R.string.menu_here), !remote[i], { remote = remote.toMutableList().also { it[i] = false } }, Modifier.weight(1f))
                                OpcionChiva(stringResource(R.string.menu_remote), remote[i], { remote = remote.toMutableList().also { it[i] = true } }, Modifier.weight(1.4f), Icon.ENLACE)
                            }
                        }
                    }
                }
            }
        }
        Box(Modifier.padding(start = 16.dp, end = 19.dp, bottom = 14.dp, top = 4.dp)) {
            BotonChiva(
                stringResource(if (seats.isEmpty()) R.string.menu_start else R.string.menu_wait), { onStart(names, tokens.map { Personajes[it].first }, seats, bots) },
                enabled = repeated.isEmpty() && board.key !in broken && seats.size + bots.size < count, // alguien juega aquí
            )
        }
    }
    if (asking) {
        DialogoChiva(stringResource(R.string.menu_delete_title, board.config.name), botones = {
            BotonChiva(stringResource(R.string.menu_delete), { asking = false; onDelete() })
            BotonChiva(stringResource(R.string.menu_delete_keep), { asking = false }, principal = false)
        }) {
            Text(stringResource(R.string.menu_delete_body), fontSize = 16.sp)
        }
    }
}
