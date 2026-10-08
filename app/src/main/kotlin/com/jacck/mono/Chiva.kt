package com.jacck.mono

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.jacck.mono.board.Icon
import com.jacck.mono.board.IconImage

/** Colores de la interfaz «Chiva de fiesta» (D-33), de la paleta del arte (D-28). */
object Chiva {
    val Tinta = Color(0xFF1B1B1B)
    val Sol = Color(0xFFFFC21A)
    val Techo = Color(0xFFE63946)
    val Ocre = Color(0xFFFFD60A)
    val Turno = Color(0xFFFFE08A)
    val Azul = Color(0xFF1D7BEF)
    val Verde = Color(0xFF2BB04A)
    val Magenta = Color(0xFFE5007E)
    val Franja = listOf(Techo, Sol, Azul, Verde, Magenta)
}

/** Lilita One (OFL, `arte/letra/`): la letra de rótulo de chiva, en todos los textos. */
val Lilita = FontFamily(Font(R.font.lilita_one))

private fun TextStyle.lilita() = copy(fontFamily = Lilita)

/**
 * Tema de Compose de la app (D-33): los colores de Material 3 salen de la paleta y todos los estilos
 * de letra usan Lilita, así cada `Text` y cada componente de Material los heredan sin decir nada.
 */
@Composable
fun ChivaTheme(content: @Composable () -> Unit) {
    val base = Typography()
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Chiva.Techo, onPrimary = Color.White, secondary = Chiva.Ocre, onSecondary = Chiva.Tinta,
            background = Chiva.Sol, onBackground = Chiva.Tinta, surface = Color.White, onSurface = Chiva.Tinta,
            surfaceContainerHighest = Color.White, surfaceContainerHigh = Color.White, surfaceContainer = Color.White,
            surfaceContainerLow = Color.White, surfaceVariant = Color.White, error = Chiva.Techo,
        ),
        typography = Typography(
            displayLarge = base.displayLarge.lilita(), displayMedium = base.displayMedium.lilita(),
            displaySmall = base.displaySmall.lilita(), headlineLarge = base.headlineLarge.lilita(),
            headlineMedium = base.headlineMedium.lilita(), headlineSmall = base.headlineSmall.lilita(),
            titleLarge = base.titleLarge.lilita(), titleMedium = base.titleMedium.lilita(),
            titleSmall = base.titleSmall.lilita(), bodyLarge = base.bodyLarge.lilita(),
            bodyMedium = base.bodyMedium.lilita(), bodySmall = base.bodySmall.lilita(),
            labelLarge = base.labelLarge.lilita(), labelMedium = base.labelMedium.lilita(),
            labelSmall = base.labelSmall.lilita(),
        ),
        content = content,
    )
}

/** Fondo de sol con franjas de chiva arriba y abajo (D-33); [content] ocupa el medio. */
@Composable
fun PantallaChiva(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(Chiva.Sol).safeDrawingPadding()) {
        FranjaChiva(12.dp)
        Column(Modifier.weight(1f).fillMaxWidth(), content = content)
        FranjaChiva(12.dp)
    }
}

/** Franja de chiva: dientes rojo, amarillo, azul, verde y magenta sobre tinta (D-28). */
@Composable
fun FranjaChiva(alto: Dp = 10.dp) {
    Row(Modifier.fillMaxWidth().height(alto).background(Chiva.Tinta)) {
        repeat(24) {
            Box(Modifier.weight(1f).fillMaxSize().padding(horizontal = 1.dp, vertical = alto / 4).background(Chiva.Franja[it % Chiva.Franja.size]))
        }
    }
}

/** Panel blanco con contorno de tinta y sombra negra sólida desplazada, como una calcomanía (D-33). */
@Composable
fun Calcomania(
    modifier: Modifier = Modifier.fillMaxWidth(), sombra: Dp = 4.dp, borde: Dp = 2.5.dp,
    forma: Shape = RoundedCornerShape(12.dp), content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier) {
        Box(Modifier.matchParentSize().offset(sombra, sombra).background(Chiva.Tinta, forma))
        Column(Modifier.fillMaxWidth().clip(forma).background(Color.White).border(borde, Chiva.Tinta, forma).padding(borde), content = content)
    }
}

/**
 * Botón de chiva (D-33): rojo con letra blanca el principal, ocre con letra de tinta el secundario;
 * contorno, sombra sólida y el texto en una línea (con cifra: verbo + signo, M-026).
 */
@Composable
fun BotonChiva(
    texto: String, onClick: () -> Unit, modifier: Modifier = Modifier.fillMaxWidth(), principal: Boolean = true,
    enabled: Boolean = true, icono: Icon? = null,
) {
    val forma = RoundedCornerShape(10.dp)
    Box(modifier.alpha(if (enabled) 1f else 0.45f)) {
        if (enabled) Box(Modifier.matchParentSize().offset(3.dp, 3.dp).background(Chiva.Tinta, forma))
        Row(
            Modifier.fillMaxWidth().clip(forma).background(if (principal) Chiva.Techo else Chiva.Ocre)
                .border(2.dp, Chiva.Tinta, forma).clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
        ) {
            icono?.let { IconImage(it, 20.dp); Spacer(Modifier.width(6.dp)) }
            // Un nombre largo achica la letra hasta 11 sp en vez de cortarse («Calle del Embudo», FB.2b).
            Text(
                texto, color = if (principal) Color.White else Chiva.Tinta, maxLines = 1, softWrap = false,
                autoSize = TextAutoSize.StepBased(minFontSize = 11.sp, maxFontSize = 16.sp),
            )
        }
    }
}

/**
 * Diálogo de turno como carta de chiva (FB.2, D-33): franjas arriba y abajo, [titulo] en una banda
 * de [color], [cuerpo] con scroll y debajo [botones] (`BotonChiva` apilados). No se cierra tocando
 * fuera ni con atrás: el motor espera una decisión.
 */
@Composable
fun DialogoChiva(
    titulo: String, color: Color = Chiva.Techo, botones: @Composable ColumnScope.() -> Unit = {},
    cuerpo: @Composable ColumnScope.() -> Unit = {},
) {
    Dialog(onDismissRequest = {}) {
        Calcomania(sombra = 5.dp, borde = 3.dp, forma = RoundedCornerShape(16.dp)) {
            FranjaChiva(8.dp)
            Box(Modifier.fillMaxWidth().background(color).padding(horizontal = 12.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
                Text(titulo, color = Color.White, fontSize = 20.sp, textAlign = TextAlign.Center)
            }
            Column(
                Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp), content = cuerpo,
            )
            Column(Modifier.padding(start = 14.dp, end = 17.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = botones)
            FranjaChiva(8.dp)
        }
    }
}

/**
 * Ficha de opción (FB.2c): roja con letra blanca si está elegida, blanca con letra de tinta si no;
 * [icono] va a la izquierda del texto (en lugar de un emoji, K18).
 */
@Composable
fun OpcionChiva(texto: String, elegida: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, icono: Icon? = null) {
    val forma = RoundedCornerShape(10.dp)
    Row(
        modifier.clip(forma).background(if (elegida) Chiva.Techo else Color.White).border(2.dp, Chiva.Tinta, forma)
            .clickable(role = Role.RadioButton, onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
    ) {
        icono?.let { IconImage(it, 18.dp); Spacer(Modifier.width(6.dp)) }
        Text(
            texto, color = if (elegida) Color.White else Chiva.Tinta, maxLines = 1, softWrap = false,
            autoSize = TextAutoSize.StepBased(minFontSize = 11.sp, maxFontSize = 16.sp),
        )
    }
}
