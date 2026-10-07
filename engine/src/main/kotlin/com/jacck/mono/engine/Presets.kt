package com.jacck.mono.engine

import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.MonoJson

/**
 * Los presets del juego (F2.8, D-18): un JSON por preset en `resources/presets/`, dentro del
 * jar del motor (y del APK). Las opciones son las columnas de `REGLAS.md` `## Diferencias`;
 * las casillas y las cartas, las de D-17 y D-18. `PresetTest` comprueba cada valor con su R-##.
 */
enum class Preset(private val file: String) {
    /** Monopoly (R-01..R-40). */
    CLASSIC("clasico"),

    /** Tío Rico (R-41..R-54 donde difiere). */
    TIO_RICO("tio_rico");

    /** Lee el JSON del preset; cada llamada devuelve una configuración nueva. */
    fun load(): GameConfig {
        val stream = requireNotNull(Preset::class.java.getResourceAsStream("/presets/$file.json")) { "falta el preset $file" }
        return MonoJson.decodeConfig(stream.use { it.readBytes().decodeToString() })
    }
}
