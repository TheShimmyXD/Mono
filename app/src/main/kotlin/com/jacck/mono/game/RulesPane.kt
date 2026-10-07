package com.jacck.mono.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.Calcomania
import com.jacck.mono.Chiva
import com.jacck.mono.OpcionChiva
import com.jacck.mono.R
import com.jacck.mono.engine.model.RuleOptions
import com.jacck.mono.message

/**
 * La pestaña «Reglas» del editor (F4.2, D-40, maqueta B): los temas en dos filas y, debajo, una
 * calcomanía por regla con su explicación; lo que difiere de la caja dice «antes …» en rojo.
 */
@Composable
fun RulesPane(vm: EditorViewModel, modifier: Modifier = Modifier) {
    val res = LocalContext.current.resources
    Column(modifier) {
        RuleTab.entries.chunked(3).forEach { fila ->
            Row(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                fila.forEach { t -> OpcionChiva(stringResource(t.label), t == vm.ruleTab, { vm.ruleTab = t }, Modifier.weight(1f)) }
            }
        }
        key(vm.ruleTab) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RULE_ROWS.filter { it.tab == vm.ruleTab }.forEach { RuleCard(it, vm.rules, vm.preset, vm::editRules) }
                vm.ruleErrors.forEach { Text(it.message(res, vm.config), color = Chiva.Techo, fontSize = 15.sp) }
            }
        }
    }
}

@Composable
private fun RuleCard(row: RuleRow, rules: RuleOptions, preset: RuleOptions, onEdit: (RuleOptions) -> Unit) {
    Calcomania(Modifier.fillMaxWidth().padding(end = 4.dp)) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            when (row) {
                is NumberRow -> NumberLine(row, rules, preset, onEdit)
                is ChoiceRow -> ChoiceLine(row, rules, preset, onEdit)
            }
            Text(stringResource(row.help), fontSize = 13.sp)
        }
    }
}

/** Nombre y «antes …» a la izquierda; [end] a la derecha. */
@Composable
private fun Header(label: Int, before: String?, end: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(label), fontSize = 15.sp)
            if (before != null) Text(stringResource(R.string.rules_before, before), fontSize = 12.sp, color = Chiva.Techo)
        }
        end()
    }
}

@Composable
private fun NumberLine(row: NumberRow, rules: RuleOptions, preset: RuleOptions, onEdit: (RuleOptions) -> Unit) {
    val value = row.get(rules)
    val was = row.get(preset)
    val before = if (value != was) valueText(row, was) else null
    Header(row.label, before) {
        if (value != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Paso("−", row.step(value, up = false) != value) { onEdit(row.step(rules, up = false)) }
                Text(valueText(row, value), fontSize = 17.sp, textAlign = TextAlign.Center, maxLines = 1,
                    color = if (before != null) Chiva.Techo else Chiva.Tinta, modifier = Modifier.width(78.dp))
                Paso("+", row.step(value, up = true) != value) { onEdit(row.step(rules, up = true)) }
            }
        }
    }
    row.optional?.let { opt ->
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OpcionChiva(stringResource(opt.off), value == null, { onEdit(row.set(rules, null)) }, Modifier.weight(1f))
            OpcionChiva(stringResource(opt.on), value != null, { if (value == null) onEdit(row.set(rules, was ?: opt.fallback)) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun ChoiceLine(row: ChoiceRow, rules: RuleOptions, preset: RuleOptions, onEdit: (RuleOptions) -> Unit) {
    val was = row.choices.firstOrNull { it.isOn(preset) }
    val before = was?.takeIf { !it.isOn(rules) }?.let { stringResource(it.label) }
    val chips = @Composable { m: Modifier ->
        row.choices.forEach { c -> OpcionChiva(stringResource(c.label), c.isOn(rules), { onEdit(c.apply(rules)) }, m) }
    }
    // Sí / No caben a la derecha del nombre; las opciones largas van en su propia fila.
    if (row.choices.all { it.label == R.string.rules_yes || it.label == R.string.rules_no }) {
        Header(row.label, before) { Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { chips(Modifier) } }
    } else {
        Header(row.label, before) {}
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { chips(Modifier.weight(1f)) }
    }
}

/** La cifra como se lee: «$1500», «10 %», «4» o el nombre del 0 («Nunca»); sin cifra, la opción apagada. */
@Composable
private fun valueText(row: NumberRow, value: Int?): String = when {
    value == null -> row.optional?.let { stringResource(it.off) } ?: ""
    value == 0 && row.zero != null -> stringResource(row.zero)
    row.kind == NumberKind.MONEY -> money(value)
    row.kind == NumberKind.PERCENT -> "$value %"
    else -> "$value"
}

/** Botón cuadrado de − o +; gris si ya no cambia la cifra. */
@Composable
private fun Paso(signo: String, enabled: Boolean, onClick: () -> Unit) {
    val forma = RoundedCornerShape(8.dp)
    Box(
        Modifier.size(34.dp).alpha(if (enabled) 1f else 0.35f).background(Color.White, forma).border(2.dp, Chiva.Tinta, forma)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(signo, fontSize = 20.sp)
    }
}
