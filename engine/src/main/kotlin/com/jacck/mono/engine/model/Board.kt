package com.jacck.mono.engine.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Configuración de una partida: el tablero (anillo de N casillas, D-05), sus grupos y las
 * opciones de reglas. Es lo que se edita (F4) y lo que guarda un preset (F2.8).
 * El validador (F2.7) comprueba los rangos de D-09; aquí solo está la forma.
 */
@Serializable
data class GameConfig(
    val name: String,
    val groups: List<ColorGroup>,
    val squares: List<Square>,
    val rules: RuleOptions,
    /** Las cartas de los dos mazos (R-18, D-14); el orden es el de la caja, sin barajar. */
    val cards: List<Card> = emptyList(),
)

/** Grupo de color (R-16, R-25). `color` es `#RRGGBB` y solo lo usa la interfaz. */
@Serializable
data class ColorGroup(val id: String, val name: String, val color: String)

/** Mazo de cartas: A = Casualidad / Lotería, B = Arca Comunal / Sorpresa (R-18). */
@Serializable
enum class Deck { A, B }

/** Una casilla del anillo. En JSON lleva `"type"` con el `@SerialName` de su clase. */
@Serializable
sealed interface Square {
    val name: String
}

/** Casilla que se compra: tiene precio y, si no lo fija la regla, valor de hipoteca. */
sealed interface OwnableSquare : Square {
    val price: Int
    /** Hipoteca impresa (R-31); null cuando `mortgageValue` = mitad del total (R-49). */
    val mortgage: Int?
}

/** La salida: GO o Estación Santa Fe (R-07, R-10, R-45). */
@Serializable
@SerialName("start")
data class Start(override val name: String) : Square

/**
 * Propiedad de un grupo de color (R-11, R-13, R-15). `rents` va del solar sin construir al
 * hotel: [base, 1 casa, …, `maxHouses` casas, hotel]. Precios de casa y hotel solo si la
 * regla no los fija (`housePrice`, `hotelPrice`).
 */
@Serializable
@SerialName("property")
data class Property(
    override val name: String,
    val group: String,
    override val price: Int,
    val rents: List<Int>,
    val housePrice: Int? = null,
    val hotelPrice: Int? = null,
    override val mortgage: Int? = null,
) : OwnableSquare

/** Ferrocarril: `rents[k]` es el alquiler con k + 1 en manos del mismo dueño. Sin casas. */
@Serializable
@SerialName("station")
data class Station(
    override val name: String,
    override val price: Int,
    val rents: List<Int>,
    override val mortgage: Int? = null,
) : OwnableSquare

/** Servicio público: alquiler = dados × `diceMultipliers[k]`, con k + 1 del mismo dueño. */
@Serializable
@SerialName("utility")
data class Utility(
    override val name: String,
    override val price: Int,
    val diceMultipliers: List<Int>,
    override val mortgage: Int? = null,
) : OwnableSquare

/**
 * Impuesto: `fixed` + `perHotel` por cada hotel o castillo; con `percent` > 0 el jugador
 * elige entre `fixed` y ese % de su patrimonio (R-19, R-52..R-54).
 */
@Serializable
@SerialName("tax")
data class Tax(
    override val name: String,
    val fixed: Int,
    val percent: Int = 0,
    val perHotel: Int = 0,
) : Square

/** Roba una carta del mazo (R-18). */
@Serializable
@SerialName("card")
data class CardSquare(override val name: String, val deck: Deck) : Square

/** La Cárcel, o «De visita» si se cae en ella (R-20, R-21). */
@Serializable
@SerialName("jail")
data class Jail(override val name: String) : Square

/** «Váyase a la Cárcel» (R-20). */
@Serializable
@SerialName("goToJail")
data class GoToJail(override val name: String) : Square

/** Descanso: Parada Libre (R-24). */
@Serializable
@SerialName("rest")
data class Rest(override val name: String) : Square
