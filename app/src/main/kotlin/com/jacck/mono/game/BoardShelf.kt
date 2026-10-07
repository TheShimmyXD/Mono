package com.jacck.mono.game

import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.MonoJson
import java.io.File

/** Largo máximo del nombre de un tablero: cabe en una línea de la lista del menú (D-42). */
const val MAX_BOARD_NAME = 20

/** Un tablero propio: `id` es el nombre de su archivo (`t3` → `tableros/t3.json`). */
data class OwnBoard(val id: String, val config: GameConfig)

/**
 * Lo que el menú deja elegir (F4.4, D-42): un original (`key` = nombre del [Preset]) o uno propio
 * (`key` = su id). Los originales salen siempre del JSON del motor y nunca se sobrescriben.
 */
data class BoardChoice(val key: String, val config: GameConfig, val own: Boolean)

fun boardChoices(own: List<OwnBoard>): List<BoardChoice> =
    Preset.entries.map { BoardChoice(it.name, it.load(), own = false) } + own.map { BoardChoice(it.id, it.config, own = true) }

/**
 * Los tableros propios (F4.4, D-42): un JSON por tablero en `tableros/` de la carpeta privada de la
 * app, con su nombre dentro (`GameConfig.name`). Se escribe a un temporal y se renombra, como
 * [SaveFile], para que cerrar la app a mitad de escritura no deje un archivo cortado.
 */
class BoardShelf(dir: File) {

    private val folder = File(dir, "tableros")

    /** Los tableros en el orden en que se crearon; un archivo que no se puede leer se salta. */
    fun list(): List<OwnBoard> = folder.list().orEmpty()
        .mapNotNull { number(it)?.let { n -> n to it } }
        .sortedBy { it.first }
        .mapNotNull { (_, file) ->
            try {
                OwnBoard(file.removeSuffix(".json"), MonoJson.decodeConfig(File(folder, file).readText()))
            } catch (_: IllegalArgumentException) {
                null // SerializationException es un IllegalArgumentException: JSON viejo o dañado.
            }
        }

    /** Guarda `config` en el tablero `id`, o en uno nuevo si es null; devuelve su id. */
    fun save(config: GameConfig, id: String? = null): String {
        folder.mkdirs()
        val key = id ?: "t${(folder.list().orEmpty().mapNotNull(::number).maxOrNull() ?: 0) + 1}"
        val temp = File(folder, "$key.json.tmp")
        temp.writeText(MonoJson.encodeConfig(config))
        check(temp.renameTo(File(folder, "$key.json"))) { "no se pudo guardar el tablero $key" }
        return key
    }

    fun delete(id: String) {
        File(folder, "$id.json").delete()
    }

    private fun number(file: String): Int? = FILE.matchEntire(file)?.groupValues?.get(1)?.toInt()

    private companion object {
        val FILE = Regex("t(\\d+)\\.json")
    }
}

/**
 * Nombre de una copia (al duplicar o al editar un original): «<nombre> copia» y, si ya hay uno así
 * (sin distinguir mayúsculas), «copia 2», «copia 3»…; recortado para que quepa en [MAX_BOARD_NAME].
 */
fun copyName(name: String, taken: Collection<String>): String {
    val used = taken.map { it.lowercase() }.toSet()
    return generateSequence(1) { it + 1 }
        .map { n -> if (n == 1) " copia" else " copia $n" }
        .map { suffix -> name.trim().take(MAX_BOARD_NAME - suffix.length).trimEnd() + suffix }
        .first { it.lowercase() !in used }
}

/** El nombre escrito sin espacios en los bordes; vacío, se queda el de antes. */
fun boardName(typed: String, before: String): String = typed.trim().take(MAX_BOARD_NAME).trim().ifEmpty { before }
