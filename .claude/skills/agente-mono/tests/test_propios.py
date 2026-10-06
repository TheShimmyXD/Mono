"""Pruebas de pagina.py y telefono.py (solo biblioteca estandar; no llaman a pdftoppm ni a adb).

Uso: python3 -m unittest discover -s .claude/skills/agente-mono/tests
"""

from __future__ import annotations

import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
import pagina  # noqa: E402
import telefono  # noqa: E402


class TestPagina(unittest.TestCase):
    def test_source_from_toml_or_path(self):
        conf = {"fuentes": {"monopoly": "fuentes/m.pdf"}}
        self.assertEqual(pagina.resolve_source("monopoly", Path("/r"), conf), Path("/r/fuentes/m.pdf"))
        self.assertEqual(pagina.resolve_source("/x/y.pdf", Path("/r"), conf), Path("/x/y.pdf"))

    def test_crop_must_stay_inside_the_page(self):
        self.assertEqual(pagina.parse_crop("0,0.5,0.5,0.5"), (0, 0.5, 0.5, 0.5))
        for bad in ("0,0,1", "0.6,0,0.5,0.5", "0,0,0,0.5", "-0.1,0,0.5,0.5"):
            with self.assertRaises(ValueError):
                pagina.parse_crop(bad)

    def test_crop_pixels_at_dpi(self):
        # Carta (612 x 792 pt) a 72 dpi: 1 pt = 1 px.
        self.assertEqual(pagina.crop_pixels((612, 792), 72, (0.5, 0.25, 0.5, 0.5)), (306, 198, 306, 396))

    def test_grid_covers_the_whole_page(self):
        tiles = pagina.grid(2, 3)
        self.assertEqual(len(tiles), 6)
        area = sum(w * h for _, _, w, h in tiles)
        self.assertAlmostEqual(area, 1.0)
        self.assertEqual(tiles[-1][:2], (0.5, 2 / 3))

    def test_output_name_has_no_dots(self):
        name = pagina.output_stem(Path("Monopoly(Spanish).pdf"), 2, (0, 0.5, 0.5, 0.25))
        self.assertEqual(name, "MonopolySpanish_p2_r0-50-50-25")
        self.assertNotIn(".", name)

    def test_render_one_page_only(self):
        argv = pagina.render_command(Path("a.pdf"), 3, 100, Path("/s/a_p3"), (1, 2, 3, 4))
        self.assertEqual(argv[1:5], ["-f", "3", "-l", "3"])
        self.assertIn("-W", argv)
        self.assertNotIn("-W", pagina.render_command(Path("a.pdf"), 1, 100, Path("/s/a"), None))


class TestTelefono(unittest.TestCase):
    def test_parse_devices_only_ready(self):
        out = (
            "List of devices attached\n"
            "abc123\tdevice\n"
            "emulator-5554\tdevice\n"
            "zzz\tunauthorized\n"
        )
        self.assertEqual(telefono.parse_devices(out), ["abc123", "emulator-5554"])
        self.assertEqual(telefono.parse_devices("List of devices attached\n"), [])

    def test_prefers_physical_phone(self):
        self.assertEqual(telefono.pick_device(["emulator-5554", "abc123"]), "abc123")
        self.assertEqual(telefono.pick_device(["emulator-5554"]), "emulator-5554")
        self.assertIsNone(telefono.pick_device([]))
        self.assertIsNone(telefono.pick_device(["abc"], "otro"))

    def test_logcat_is_filtered(self):
        argv = telefono.logcat_command("adb", "abc", "Mono")
        self.assertIn("-d", argv)
        self.assertEqual(argv[-3:], ["Mono:V", "AndroidRuntime:E", "*:S"])

    def test_launch_target_waits_for_package(self):
        self.assertIsNone(telefono.launch_target({"android": {"paquete": ""}}))
        conf = {"android": {"paquete": "com.x.mono", "actividad": ".MainActivity"}}
        self.assertEqual(telefono.launch_target(conf), "com.x.mono/.MainActivity")

    def test_gradle_env_expands_home(self):
        env = telefono.gradle_env({"cierre": {"entorno": {"JAVA_HOME": "~/jbr"}}})
        self.assertEqual(env["JAVA_HOME"], str(Path.home() / "jbr"))


if __name__ == "__main__":
    unittest.main()
