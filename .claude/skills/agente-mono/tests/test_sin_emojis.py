"""Pruebas de sin_emojis.py (solo biblioteca estándar).

Uso: python3 -m unittest discover -s .claude/skills/agente-<nombre>/tests
"""

from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

SCRIPTS = Path(__file__).resolve().parents[1] / "scripts"
sys.path.insert(0, str(SCRIPTS))
import sin_emojis  # noqa: E402


class SinEmojisTest(unittest.TestCase):
    def setUp(self) -> None:
        self.dir = tempfile.TemporaryDirectory()
        self.root = Path(self.dir.name)

    def tearDown(self) -> None:
        self.dir.cleanup()

    def write(self, name: str, text: str) -> Path:
        path = self.root / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")
        return path

    def test_encuentra_emojis_y_la_estrella_en_textos_logs_y_comentarios(self) -> None:
        self.write("res/values/strings.xml", '<string name="a">📶 Otro teléfono</string>\n<string name="b">★ %1$s</string>\n')
        self.write("kotlin/A.kt", 'val x = 1\nLog.i(TAG, "listo 🤖") // ⭐\n')
        found = sin_emojis.buscar([self.root])
        self.assertEqual(3, len(found))
        self.assertTrue(found[0].endswith("A.kt:2: 🤖⭐"), found)
        self.assertTrue(any("strings.xml:1: 📶" in f for f in found), found)
        self.assertTrue(any("strings.xml:2: ★" in f for f in found), found)

    def test_el_espanol_y_los_signos_del_juego_no_cuentan(self) -> None:
        self.write("A.kt", 'val t = "¿Cuánto? ¡Ñandú! $1500 · 4 × 6 — «sí» → ok"\n')
        self.write("B.png", "📶")  # no es código ni recurso de texto
        self.assertEqual([], sin_emojis.buscar([self.root]))

    def test_main_sale_1_con_hallazgos_y_0_sin_ellos(self) -> None:
        limpio = self.write("limpio/A.kt", "val a = 1\n")
        self.assertEqual(0, sin_emojis.main([str(limpio.parent)]))
        self.write("sucio/B.kt", 'val b = "🎲"\n')
        self.assertEqual(1, sin_emojis.main([str(self.root / "sucio")]))


if __name__ == "__main__":
    unittest.main()
