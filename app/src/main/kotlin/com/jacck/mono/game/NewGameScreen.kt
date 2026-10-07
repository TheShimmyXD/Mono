package com.jacck.mono.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import com.jacck.mono.OpcionChiva
import com.jacck.mono.PantallaChiva
import com.jacck.mono.R
import com.jacck.mono.board.PlayerColors
import com.jacck.mono.board.Medallon
import com.jacck.mono.board.Personajes
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.GameState

/**
 * Menú de nueva partida (F3.5, maqueta A, D-25): preset, cuántos juegan (2-6) y sus nombres, todo
 * en una pantalla. Una casilla vacía juega con el nombre de muestra (gris); con nombres repetidos
 * no se puede empezar. `rememberSaveable` guarda lo elegido si Android recrea la pantalla.
 * Con una partida guardada (`saved`), arriba va «Seguir la partida» (F3.6, D-26). Estilo chiva (FB.2c, D-33).
 * Bajo cada nombre, los 8 personajes (FB.4, D-36): el suyo grande con su color, los de otros atenuados.
 */
@Composable
fun NewGameScreen(
    saved: GameState?, onResume: () -> Unit, edited: Set<Preset> = emptySet(), onEdit: (Preset) -> Unit = {},
    onStart: (Preset, List<String>, List<String>) -> Unit,
) {
    val defaults = stringArrayResource(R.array.default_names).toList()
    var preset by rememberSaveable { mutableStateOf(Preset.CLASSIC) }
    var count by rememberSaveable { mutableStateOf(3) }
    var typed by rememberSaveable { mutableStateOf(List(defaults.size) { "" }) }
    var picks by rememberSaveable { mutableStateOf(List(defaults.size) { it }) }
    val tokens = playerTokens(picks, count, Personajes.size)
    val names = playerNames(typed, defaults, count)
    val repeated = repeatedNames(names)

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Chiva.Tinta, focusedBorderColor = Chiva.Techo,
        unfocusedContainerColor = Color.White, focusedContainerColor = Color.White,
        unfocusedPlaceholderColor = Chiva.Tinta.copy(alpha = 0.4f), focusedPlaceholderColor = Chiva.Tinta.copy(alpha = 0.4f),
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OpcionChiva(stringResource(R.string.preset_classic), preset == Preset.CLASSIC, { preset = Preset.CLASSIC }, Modifier.weight(1f))
                        OpcionChiva(stringResource(R.string.preset_tio_rico), preset == Preset.TIO_RICO, { preset = Preset.TIO_RICO }, Modifier.weight(1f))
                    }
                    Box(Modifier.padding(end = 3.dp)) {
                        BotonChiva(stringResource(if (preset in edited) R.string.menu_edited else R.string.menu_edit), { onEdit(preset) }, principal = false)
                    }
                }
            }
            Calcomania {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.menu_players), fontSize = 18.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (2..defaults.size).forEach { n -> OpcionChiva("$n", n == count, { count = n }, Modifier.weight(1f)) }
                    }
                    (0 until count).forEach { i ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = typed[i],
                                onValueChange = { v -> typed = typed.toMutableList().also { it[i] = v.take(MAX_NAME) } },
                                placeholder = { Text(defaults[i]) },
                                singleLine = true,
                                isError = i in repeated,
                                supportingText = if (i in repeated) ({ Text(stringResource(R.string.menu_repeated)) }) else null,
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth(),
                            )
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
                        }
                    }
                }
            }
        }
        Box(Modifier.padding(start = 16.dp, end = 19.dp, bottom = 14.dp, top = 4.dp)) {
            BotonChiva(stringResource(R.string.menu_start), { onStart(preset, names, tokens.map { Personajes[it].first }) }, enabled = repeated.isEmpty())
        }
    }
}
