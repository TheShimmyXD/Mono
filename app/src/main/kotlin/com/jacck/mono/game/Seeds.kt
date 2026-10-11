package com.jacck.mono.game

import kotlin.random.Random

/**
 * La semilla de cada partida (FF.1, D-84): una nueva cada vez que empieza una, del menú, de una
 * sala o «Seguir la partida». La del extra de prueba [fixed] vale solo para la primera, como los
 * demás extras (D-63). Antes salía una sola al abrir la app y las partidas siguientes se repetían.
 */
class Seeds(private val fixed: Long?, private val fresh: () -> Long = Random.Default::nextLong) {
    private var first = true

    fun next(): Long = (if (first && fixed != null) fixed else fresh()).also { first = false }
}
