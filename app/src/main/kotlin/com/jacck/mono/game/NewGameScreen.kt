package com.jacck.mono.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.R
import com.jacck.mono.board.PlayerColors
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.GameState

/**
 * Menú de nueva partida (F3.5, maqueta A, D-25): preset, cuántos juegan (2-6) y sus nombres, todo
 * en una pantalla. Una casilla vacía juega con el nombre de muestra (gris); con nombres repetidos
 * no se puede empezar. `rememberSaveable` guarda lo elegido si Android recrea la pantalla.
 * Con una partida guardada (`saved`), arriba va «Seguir la partida» (F3.6, D-26).
 */
@Composable
fun NewGameScreen(saved: GameState?, onResume: () -> Unit, onStart: (Preset, List<String>) -> Unit) {
    val defaults = stringArrayResource(R.array.default_names).toList()
    var preset by rememberSaveable { mutableStateOf(Preset.CLASSIC) }
    var count by rememberSaveable { mutableStateOf(3) }
    var typed by rememberSaveable { mutableStateOf(List(defaults.size) { "" }) }
    val names = playerNames(typed, defaults, count)
    val repeated = repeatedNames(names)

    Column(
        Modifier.fillMaxSize().safeDrawingPadding().imePadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (saved != null) {
                FilledTonalButton(onClick = onResume, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.menu_resume), fontSize = 18.sp)
                        Text(stringResource(R.string.menu_resume_detail, saved.players.joinToString(", ") { it.name }, saved.turn + 1), fontSize = 13.sp)
                    }
                }
            }
            Text(stringResource(R.string.menu_title), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.menu_game), fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(preset == Preset.CLASSIC, { preset = Preset.CLASSIC }, { Text(stringResource(R.string.preset_classic)) })
                FilterChip(preset == Preset.TIO_RICO, { preset = Preset.TIO_RICO }, { Text(stringResource(R.string.preset_tio_rico)) })
            }
            Text(stringResource(R.string.menu_players), fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (2..defaults.size).forEach { n -> FilterChip(n == count, { count = n }, { Text("$n") }) }
            }
            (0 until count).forEach { i ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(22.dp).background(PlayerColors[i], CircleShape))
                    OutlinedTextField(
                        value = typed[i],
                        onValueChange = { v -> typed = typed.toMutableList().also { it[i] = v.take(MAX_NAME) } },
                        placeholder = { Text(defaults[i]) },
                        singleLine = true,
                        isError = i in repeated,
                        supportingText = if (i in repeated) ({ Text(stringResource(R.string.menu_repeated)) }) else null,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = { onStart(preset, names) },
            enabled = repeated.isEmpty(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text(stringResource(R.string.menu_start), fontSize = 18.sp) }
    }
}
