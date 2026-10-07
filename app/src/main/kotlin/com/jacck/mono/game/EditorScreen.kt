package com.jacck.mono.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.BotonChiva
import com.jacck.mono.Calcomania
import com.jacck.mono.Chiva
import com.jacck.mono.OpcionChiva
import com.jacck.mono.PantallaChiva
import com.jacck.mono.R
import com.jacck.mono.board.Board
import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.MAX_SQUARE_NAME
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Station
import com.jacck.mono.engine.model.Tax
import com.jacck.mono.engine.model.Utility
import com.jacck.mono.message

/**
 * El editor (F4.1..F4.3; D-39..D-41), con dos pestañas: «Casillas», el tablero en anillo arriba (se
 * toca la casilla), los errores del tablero en rojo y la ficha debajo, con «Quitar» y «Añadir
 * después»; y «Reglas» (`RulesPane`). «Listo» (o atrás) vuelve al menú
 * con lo guardado; lo no guardado se pierde.
 */
@Composable
fun EditorScreen(vm: EditorViewModel, toBottom: Boolean = false, onDone: () -> Unit) {
    val scroll = rememberScrollState()
    // Extra de prueba `abajo`: abre al final, para capturar la ficha sin deslizar (adb no desliza, D-23).
    if (toBottom) LaunchedEffect(scroll.maxValue) { scroll.scrollTo(scroll.maxValue) }
    BackHandler(onBack = onDone)
    val config = vm.config
    // El tablero pide una partida para dibujarse; sin fichas, solo marca la casilla elegida.
    val state = remember(config) { Engine.newGame(config, listOf("", ""), seed = 0).state }
    val res = LocalContext.current.resources
    PantallaChiva {
        Text(
            stringResource(R.string.editor_title, config.name), fontSize = 22.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp),
        )
        Row(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OpcionChiva(stringResource(R.string.editor_tab_squares), !vm.showRules, { vm.showRules = false }, Modifier.weight(1f))
            OpcionChiva(stringResource(R.string.editor_tab_rules), vm.showRules, { vm.showRules = true }, Modifier.weight(1f))
        }
        if (vm.showRules) RulesPane(vm, Modifier.weight(1f)) else Column(
            Modifier.weight(1f).imePadding().verticalScroll(scroll).padding(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Board(
                // Más alto que ancho: con 40 casillas, 10 × 12 celdas de 38 dp en vez de 11 × 11 de 35 dp.
                config, state, Modifier.fillMaxWidth().aspectRatio(0.8f).padding(horizontal = 4.dp),
                onSquare = vm::select, highlight = vm.selected, showTokens = false,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.editor_count, config.squares.size) + "\n\n" +
                            (vm.selected?.let { stringResource(R.string.editor_selected, it + 1, config.squares[it].name) }
                                ?: stringResource(R.string.editor_hint)),
                        fontSize = 16.sp, textAlign = TextAlign.Center,
                    )
                }
            }
            vm.boardErrors.forEach { Text(it.message(res, config), color = Chiva.Techo, fontSize = 15.sp, modifier = Modifier.padding(horizontal = 16.dp)) }
            val i = vm.selected
            val draft = vm.draft
            if (i != null && draft != null) {
                Ficha(config, i, draft, vm::edit)
                Tamano(vm, i)
                val problems = vm.errors.map { it.message(res, config) } +
                    listOfNotNull(stringResource(R.string.editor_blank).takeIf { vm.blank })
                problems.forEach { Text(it, color = Chiva.Techo, fontSize = 15.sp, modifier = Modifier.padding(horizontal = 16.dp)) }
            }
        }
        Row(Modifier.padding(start = 16.dp, end = 19.dp, bottom = 14.dp, top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (vm.showRules) {
                BotonChiva(stringResource(R.string.rules_save), vm::saveRules, Modifier.weight(1f), enabled = vm.rulesChanged)
            } else {
                BotonChiva(stringResource(R.string.editor_save), vm::save, Modifier.weight(1f), enabled = vm.changed)
            }
            BotonChiva(stringResource(R.string.editor_done), onDone, Modifier.weight(1f), principal = false)
        }
    }
}

/** La ficha de la casilla [i]: nombre y, según su tipo, precio (o valor), grupo y alquileres. */
@Composable
private fun Ficha(config: GameConfig, i: Int, draft: SquareDraft, onEdit: (SquareDraft) -> Unit) {
    val sq = config.squares[i]
    val digits = { v: String -> v.filter(Char::isDigit).take(MAX_DIGITS) }
    Calcomania(Modifier.fillMaxWidth().padding(start = 12.dp, end = 16.dp)) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Campo(stringResource(R.string.editor_name), draft.name, { onEdit(draft.copy(name = it.take(MAX_SQUARE_NAME))) }, Modifier.weight(2f))
                when (sq) {
                    is Property, is Station, is Utility ->
                        Campo(stringResource(R.string.editor_price), draft.price, { onEdit(draft.copy(price = digits(it))) }, Modifier.weight(1f), number = true)
                    is Tax ->
                        Campo(stringResource(R.string.editor_value), draft.price, { onEdit(draft.copy(price = digits(it))) }, Modifier.weight(1f), number = true)
                    else -> {}
                }
            }
            if (sq is Property) {
                Text(stringResource(R.string.editor_group), fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    config.groups.forEach { g ->
                        Box(
                            Modifier.size(30.dp).background(Color(android.graphics.Color.parseColor(g.color)), CircleShape)
                                .border(if (g.id == draft.group) 4.dp else 2.dp, Chiva.Tinta, CircleShape)
                                .clickable(role = Role.RadioButton) { onEdit(draft.copy(group = g.id)) },
                        )
                    }
                }
            }
            if (draft.rents.isNotEmpty()) {
                val labels = stringArrayResource(if (sq is Station) R.array.editor_rents_station else R.array.editor_rents_property)
                Text(stringResource(R.string.editor_rent), fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    draft.rents.forEachIndexed { k, r ->
                        Campo(labels.getOrElse(k) { "${k + 1}" }, r, { v -> onEdit(draft.copy(rents = draft.rents.toMutableList().also { it[k] = digits(v) })) }, Modifier.weight(1f), number = true)
                    }
                }
            }
        }
    }
}

/** Quitar la casilla [i] o añadir otra después, del tipo elegido (F4.3, maqueta A, D-41). */
@Composable
private fun Tamano(vm: EditorViewModel, i: Int) {
    val config = vm.config
    val newName = stringResource(R.string.editor_new_name)
    val kinds = stringArrayResource(R.array.editor_kinds)
    Calcomania(Modifier.fillMaxWidth().padding(start = 12.dp, end = 16.dp)) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BotonChiva(stringResource(R.string.editor_remove), vm::remove, principal = false, enabled = removable(config, i))
            Text(stringResource(R.string.editor_add_label), fontSize = 15.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                NewKind.entries.forEach { k -> OpcionChiva(kinds[k.ordinal], k == vm.newKind, { vm.newKind = k }, Modifier.weight(1f)) }
            }
            BotonChiva(stringResource(R.string.editor_add), { vm.add(newName) }, enabled = newSquare(config, vm.newKind, i, newName) != null)
        }
    }
}

/** Campo de texto con etiqueta encima, en caja blanca con contorno de tinta (maqueta B). */
@Composable
private fun Campo(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier, number: Boolean = false) {
    Column(modifier) {
        Text(label, fontSize = 12.sp, maxLines = 1)
        BasicTextField(
            value, onChange, singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Chiva.Tinta),
            keyboardOptions = if (number) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp))
                .border(2.dp, Chiva.Tinta, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 6.dp),
        )
    }
}
