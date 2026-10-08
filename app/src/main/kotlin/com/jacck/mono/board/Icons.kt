package com.jacck.mono.board

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.jacck.mono.R
import com.jacck.mono.engine.model.CardSquare
import com.jacck.mono.engine.model.Deck
import com.jacck.mono.engine.model.GoToJail
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.Jail
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Rest
import com.jacck.mono.engine.model.Square
import com.jacck.mono.engine.model.Start
import com.jacck.mono.engine.model.Station
import com.jacck.mono.engine.model.Tax
import com.jacck.mono.engine.model.Utility
import java.text.Normalizer

/**
 * Íconos propios en estilo chiva (FA.2, D-29): se dibujan en `arte/arte.py` y salen como
 * `res/drawable/ic_<id>.xml` (VectorDrawable). Se pintan con [Image], que respeta sus colores.
 */
enum class Icon(@DrawableRes val res: Int) {
    CASA(R.drawable.ic_casa), HOTEL(R.drawable.ic_hotel),
    DADO_1(R.drawable.ic_dado_1), DADO_2(R.drawable.ic_dado_2), DADO_3(R.drawable.ic_dado_3),
    DADO_4(R.drawable.ic_dado_4), DADO_5(R.drawable.ic_dado_5), DADO_6(R.drawable.ic_dado_6),
    SALIDA(R.drawable.ic_salida), CARCEL(R.drawable.ic_carcel), VAYASE_CARCEL(R.drawable.ic_vayase_carcel),
    ESTACION(R.drawable.ic_estacion), ENERGIA(R.drawable.ic_energia), ACUEDUCTO(R.drawable.ic_acueducto),
    IMPUESTO(R.drawable.ic_impuesto), CASUALIDAD(R.drawable.ic_casualidad), ARCA(R.drawable.ic_arca),
    LOTERIA(R.drawable.ic_loteria), SORPRESA(R.drawable.ic_sorpresa), PARADA_LIBRE(R.drawable.ic_parada_libre),
    MIRADOR(R.drawable.ic_mirador), HAMACA(R.drawable.ic_hamaca), TURNO(R.drawable.ic_turno),
    ENLACE(R.drawable.ic_enlace), PROPIO(R.drawable.ic_propio);

    companion object {
        /** La cara del dado que muestra [n] (1-6). */
        fun dado(n: Int): Icon = entries[DADO_1.ordinal + n - 1]
    }
}

/**
 * Ícono de las casillas que no son propiedades (su nombre no cabe y el ícono se reconoce de lejos).
 * Por tipo; servicios, mazos y descansos de los presets se distinguen por el nombre, y uno que no
 * se reconoce (renombrado en el editor) cae en el ícono general de su tipo (D-29).
 */
fun Square.icon(): Icon? {
    val key = Normalizer.normalize(name, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "").lowercase()
    return when (this) {
        is Start -> Icon.SALIDA
        is Station -> Icon.ESTACION
        is Utility -> if ("acueducto" in key || "agua" in key) Icon.ACUEDUCTO else Icon.ENERGIA
        is Tax -> Icon.IMPUESTO
        is CardSquare -> when {
            "loteria" in key -> Icon.LOTERIA
            "sorpresa" in key -> Icon.SORPRESA
            deck == Deck.A -> Icon.CASUALIDAD
            else -> Icon.ARCA
        }
        is Jail -> Icon.CARCEL
        is GoToJail -> Icon.VAYASE_CARCEL
        is Rest -> when {
            "mirador" in key -> Icon.MIRADOR
            "hamaca" in key -> Icon.HAMACA
            else -> Icon.PARADA_LIBRE
        }
        is Property -> null
    }
}

@Composable
fun IconImage(icon: Icon, size: Dp, modifier: Modifier = Modifier) {
    Image(painterResource(icon.res), contentDescription = null, modifier = modifier.size(size))
}

/** Hotel, o una casa por cada casa (R-25, R-27); nada si no hay edificios. */
@Composable
fun BuildingIcons(holding: Holding, size: Dp) {
    when {
        holding.hotel -> IconImage(Icon.HOTEL, size)
        holding.houses > 0 -> Row { repeat(holding.houses) { IconImage(Icon.CASA, size) } }
    }
}
