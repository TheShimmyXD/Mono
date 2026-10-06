package com.jacck.mono.engine.model

import kotlinx.serialization.Serializable

/**
 * Opciones de reglas: una por fila de `REGLAS.md` `## Diferencias` (F1.3), con los rangos
 * de D-09. Sin valores por defecto a propósito: cada preset (F2.8) los da todos y cita su R-##.
 *
 * Las cifras son enteros en $ del juego; los porcentajes, enteros de 0 a 100.
 * La fila 3 (sentido del turno) no tiene campo: el orden es el de la lista de jugadores.
 * La fila 33 (recuento con límite de tiempo) es siempre el de R-39 (D-08).
 */
@Serializable
data class RuleOptions(
    /** Fila 34: jugadores, 2-6 (D-05). */
    val minPlayers: Int,
    val maxPlayers: Int,
    /** Fila 1: dinero inicial (R-03, R-42). */
    val startingMoney: Int,
    /** Fila 2: sueldo al caer o pasar por la salida (R-10, R-45). */
    val salary: Int,
    /** Fila 4: empate en la tirada inicial (R-06, R-43, D-08). */
    val startTieRule: StartTieRule,
    /** Fila 5: los dobles dan otra tirada (R-09, R-43). */
    val doublesRollAgain: Boolean,
    /** Fila 5: dobles seguidos que mandan a la Cárcel; 0 = sin tope (R-09, D-08). */
    val doublesToJail: Int,
    /** Fila 6: hay Cárcel (R-20..R-23). */
    val jail: Boolean,
    /** Fila 7: multa para salir y turnos máximos dentro (R-22). */
    val jailFine: Int,
    val jailMaxTurns: Int,
    /** Fila 8: la subasta empieza en precio − este descuento; null = desde cualquier precio (R-12, R-44). */
    val auctionPriceDiscount: Int?,
    /** Fila 9: si nadie puja, la propiedad sigue del Banco (R-12, D-08). */
    val unsoldStaysWithBank: Boolean,
    /** Fila 10: la hipotecada sigue cobrando alquiler (R-14, R-49). */
    val rentWhileMortgaged: Boolean,
    /** Fila 11: de dónde sale el valor de la hipoteca (R-31, R-49). */
    val mortgageValue: MortgageValue,
    /** Fila 12: se hipoteca con edificios encima (R-31, R-49). */
    val mortgageWithBuildings: Boolean,
    /** Fila 13: costo de levantar la hipoteca, además del capital (R-32, R-50). */
    val unmortgageFee: Fee,
    /** Fila 14: redondeo de los porcentajes a $1 (R-32, D-08). */
    val percentRounding: Rounding,
    /** Fila 15: solo se construye al caer en una del grupo (R-25, R-46). */
    val buildOnlyWhenLanding: Boolean,
    /** Filas 16 y 32: casas máximas por propiedad antes del hotel, 1-4 (R-26, R-38, R-46). */
    val maxHouses: Int,
    /** Fila 17: construir y vender parejo (R-26, D-08). */
    val evenBuild: Boolean,
    /** Fila 18: precio fijo de la casa; null = el de cada casilla (R-25, R-46). */
    val housePrice: Int?,
    /** Fila 19: precio fijo del hotel o castillo; null = el de cada casilla (R-27, R-47). */
    val hotelPrice: Int?,
    /** Fila 19: al poner el hotel, sus casas vuelven al Banco (R-27, R-47, D-08). */
    val hotelReturnsHouses: Boolean,
    /** Fila 20: casas y hoteles del Banco; 0 = sin límite (R-02, R-28, R-41). */
    val houseStock: Int,
    val hotelStock: Int,
    /** Fila 21: grupo completo sin construir cobra ×2 (R-16). */
    val groupDoubleRent: Boolean,
    /** Fila 22: hotel en todas las del grupo cobra ×2 (R-48, D-08). */
    val hotelGroupDoubleRent: Boolean,
    /** Fila 23: los edificios se venden al Banco a la mitad (R-30, D-08). */
    val sellBuildingsToBank: Boolean,
    /** Fila 24: entre jugadores también se venden edificios (R-29, R-51). */
    val tradeBuildings: Boolean,
    /** Fila 25: derechos por cosa vendida entre jugadores, los paga el vendedor (R-51, D-08). */
    val tradeFee: Int,
    /** Fila 26: interés que paga ya quien recibe una hipotecada, en % (R-33, R-51). */
    val mortgagedTradeInterest: Int,
    /** Fila 27: el Banco nunca se queda sin dinero (R-05, D-08). */
    val bankUnlimited: Boolean,
    /** Fila 28: el alquiler hay que reclamarlo (R-17, D-08). */
    val rentMustBeClaimed: Boolean,
    /** Fila 29: bote en Parada Libre, regla casera (R-24, D-08). */
    val freeParkingPot: Boolean,
    /** Fila 30: cómo termina la partida (R-35, R-39, R-40). */
    val endCondition: EndCondition,
    /** Fila 31: Escrituras que recibe y paga cada jugador al empezar (R-37, R-40). */
    val startingDeeds: Int,
    /** Fila 35: interés en % que paga ya quien recibe una hipotecada en una quiebra (R-34, D-15). */
    val bankruptcyInterest: Int,
)

/** Empate en la tirada inicial (R-06, D-08). */
@Serializable
enum class StartTieRule { REROLL_TIED, REROLL_ALL }

/** Valor de la hipoteca: el impreso en la casilla (R-31) o la mitad del total con edificios (R-49). */
@Serializable
enum class MortgageValue { PRINTED, HALF_TOTAL }

/** Redondeo a $1 de un porcentaje (R-32, D-08). */
@Serializable
enum class Rounding { UP, DOWN, NEAREST }

/** Fin de la partida: queda uno (R-35), segunda quiebra (R-39) o límite de tiempo (R-40). */
@Serializable
enum class EndCondition { LAST_STANDING, SECOND_BANKRUPTCY, TIME_LIMIT }

/** Un cobro de porcentaje más una parte fija: 10 % (R-32) o $200 (R-50). */
@Serializable
data class Fee(val percent: Int = 0, val fixed: Int = 0)
