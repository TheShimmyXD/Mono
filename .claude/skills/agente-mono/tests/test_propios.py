"""Pruebas de pagina.py, telefono.py y hoja_arte.py (solo biblioteca estandar; no llaman a pdftoppm ni a adb).

Uso: python3 -m unittest discover -s .claude/skills/agente-mono/tests
"""

from __future__ import annotations

import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
import pagina  # noqa: E402
import telefono  # noqa: E402
import hoja_arte  # noqa: E402
import tempfile  # noqa: E402


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

    def test_device_problems_explain_the_state(self):
        out = (
            "List of devices attached\n"
            "5xsk  no permissions (user autor is not in the plugdev group); see [x]\n"
            "ab12  unauthorized usb:1-2 transport_id:1\n"
            "cd34  device usb:1-3 product:emerald_global\n"
        )
        problems = telefono.device_problems(out)
        self.assertEqual(len(problems), 2)
        self.assertTrue(problems[0].startswith("5xsk: no permissions -> falta la regla udev"))
        self.assertIn("Permitir depuracion USB", problems[1])
        self.assertEqual(telefono.parse_devices(out), ["cd34"])

    def test_device_problems_ignore_daemon_lines(self):
        out = "* daemon not running; starting now at tcp:5037\nList of devices attached\n"
        self.assertEqual(telefono.device_problems(out), [])

    def test_install_hint_for_hyperos(self):
        out = "InstallException: INSTALL_FAILED_USER_RESTRICTED: Install canceled by user"
        self.assertIn("Instalar via USB", telefono.install_hint(out))
        self.assertIsNone(telefono.install_hint("BUILD FAILED"))

    def test_options_after_the_order_and_adb_passthrough(self):
        # M-018: con REMAINDER, `captura --salida X` perdía --salida.
        own, rest = telefono.split_passthrough(["captura", "--salida", "/x"])
        self.assertEqual(rest, [])
        self.assertEqual(telefono.build_parser().parse_args(own).salida, Path("/x"))
        own, rest = telefono.split_passthrough(["adb", "--", "shell", "am", "start", "--ei", "n", "16"])
        self.assertEqual(own, ["adb"])
        self.assertEqual(rest, ["shell", "am", "start", "--ei", "n", "16"])

    def test_screen_awake(self):
        # M-020: con la pantalla en reposo, screencap da un PNG negro.
        self.assertFalse(telefono.screen_awake("Power Manager State:\n  mWakefulness=Dozing\n"))
        self.assertTrue(telefono.screen_awake("  mWakefulness=Awake\n"))
        self.assertTrue(telefono.screen_awake(""))

    def test_prefers_physical_phone(self):
        self.assertEqual(telefono.pick_device(["emulator-5554", "abc123"]), "abc123")
        self.assertEqual(telefono.pick_device(["emulator-5554"]), "emulator-5554")
        self.assertIsNone(telefono.pick_device([]))
        self.assertIsNone(telefono.pick_device(["abc"], "otro"))

    def test_logcat_is_filtered(self):
        argv = telefono.logcat_command("adb", "abc", "Mono")
        self.assertIn("-d", argv)
        self.assertEqual(argv[-3:], ["Mono:V", "AndroidRuntime:E", "*:S"])

    def test_adb_passthrough_uses_serial_and_drops_separator(self):
        self.assertEqual(
            telefono.adb_passthrough("/sdk/adb", "abc", ["--", "shell", "wm", "size"]),
            ["/sdk/adb", "-s", "abc", "shell", "wm", "size"],
        )
        self.assertEqual(telefono.adb_passthrough("adb", "abc", ["shell"]), ["adb", "-s", "abc", "shell"])

    def test_launch_target_waits_for_package(self):
        self.assertIsNone(telefono.launch_target({"android": {"paquete": ""}}))
        conf = {"android": {"paquete": "com.x.mono", "actividad": ".MainActivity"}}
        self.assertEqual(telefono.launch_target(conf), "com.x.mono/.MainActivity")

    def test_gradle_env_expands_home(self):
        env = telefono.gradle_env({"cierre": {"entorno": {"JAVA_HOME": "~/jbr"}}})
        self.assertEqual(env["JAVA_HOME"], str(Path.home() / "jbr"))



class TestCartasYHoja(unittest.TestCase):
    """M-037: cartas del Redmi y hoja de contacto como scripts de la skill."""

    def test_carta_command_clasico_y_tio_rico(self):
        argv = telefono.carta_command("adb", "S1", "com.jacck.mono/.MainActivity", 9, False)
        self.assertEqual(argv[:8], ["adb", "-s", "S1", "shell", "am", "start", "-S", "-n"])
        self.assertIn("9", argv)
        self.assertNotIn("tio_rico", argv)
        self.assertEqual(telefono.carta_command("adb", "S1", "p/.A", 3, True)[-3:], ["--ez", "tio_rico", "true"])

    def test_cartas_opciones_mezcladas(self):
        own, _ = telefono.split_passthrough(["cartas", "--salida", "x.png", "1", "3", "--tio-rico"])
        args = telefono.build_parser().parse_intermixed_args(own)
        self.assertEqual((args.orden, args.objetivos, args.tio_rico), ("cartas", ["1", "3"], True))
        self.assertEqual(telefono.build_parser().parse_intermixed_args(["captura", "--salida", "/x"]).objetivos, [])

    def test_pantallas_extras_por_tipo(self):
        """M-048: el tipo del extra sale del valor; semilla va como long; '-' abre sin extras."""
        self.assertEqual(telefono.extras_args("fase=compra"), ["--es", "fase", "compra"])
        self.assertEqual(
            telefono.extras_args("propiedades=true,jugadores=3,semilla=7"),
            ["--ez", "propiedades", "true", "--ei", "jugadores", "3", "--el", "semilla", "7"],
        )
        self.assertEqual(telefono.extras_args("-"), [])
        argv = telefono.pantalla_command("adb", "S1", "p/.A", "fase=fin")
        self.assertEqual(argv, ["adb", "-s", "S1", "shell", "am", "start", "-S", "-n", "p/.A", "--es", "fase", "fin"])
        own, _ = telefono.split_passthrough(["pantallas", "-", "fase=compra", "--salida", "x.png"])
        args = telefono.build_parser().parse_intermixed_args(own)
        self.assertEqual((args.orden, args.objetivos), ("pantallas", ["-", "fase=compra"]))

    def test_hoja_no_pasa_de_1600(self):
        self.assertLessEqual(3 * hoja_arte.celda() + 4 * hoja_arte.SEP, hoja_arte.ANCHO_MAX)
        self.assertEqual((hoja_arte.filas(4), hoja_arte.filas(3), hoja_arte.filas(7)), (2, 1, 3))
        tamanos = [(1220, 2712)] * 4
        alto = hoja_arte.alto_comun(tamanos)
        self.assertLessEqual(sum(round(w * alto / h) for w, h in tamanos) + 3 * hoja_arte.SEP, hoja_arte.ANCHO_MAX + 4)
        self.assertEqual(hoja_arte.alto_comun([(1220, 2712)]), hoja_arte.ALTO_CAPTURA)

    def test_faltantes(self):
        with tempfile.TemporaryDirectory() as d:
            (Path(d) / "niza.svg").write_text("<svg/>")
            self.assertEqual(hoja_arte.faltantes(["niza", "zona_t"], Path(d)), ["zona_t"])


if __name__ == "__main__":
    unittest.main()
