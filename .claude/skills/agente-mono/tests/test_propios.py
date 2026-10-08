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

    def test_la_primera_captura_tras_instalar_espera_mas(self):
        self.assertEqual(telefono.espera(0, telefono.PANTALLA_ESPERA_S), 12)
        self.assertEqual(telefono.espera(1, telefono.PANTALLA_ESPERA_S), 6)
        self.assertEqual(telefono.espera(2, telefono.CARTA_ESPERA_S), 5)

    def test_grabar_hoja_de_fotogramas(self):
        # M-099: dos fotogramas por segundo, pero nunca mas de 24 en la hoja.
        self.assertEqual(telefono.fotogramas_por_segundo(11), 2)
        self.assertEqual(telefono.fotogramas_por_segundo(30) * 30, telefono.MAX_FOTOGRAMAS)
        argv = telefono.fotogramas_command(Path("capturas/FC.1_A.mp4"), Path("/t/f"), 30)
        self.assertEqual(argv[:2], ["gst-launch-1.0", "-q"])
        self.assertIn("video/x-raw,framerate=4/5", argv)
        self.assertIn("video/x-raw,width=180,height=400", argv)
        self.assertEqual(argv[-1], "location=/t/f/f_%03d.png")
        self.assertEqual(telefono.hoja_de(Path("capturas/FC.1_A.mp4")), Path("capturas/FC.1_A_hoja.png"))
        # 22 fotogramas de 180 x 400 en 8 columnas: 3 filas.
        self.assertEqual(hoja_arte.rejilla(22, 8, 180, 400), (8 * 188 - 8, 3 * 408 - 8))
        self.assertEqual(hoja_arte.rejilla(3, 8, 180, 400), (3 * 188 - 8, 400))
    def test_grabar_empieza_a_grabar_antes_de_abrir(self):
        grabacion, abrir = telefono.grabar_ordenes("adb", "S", "pkg/.A", "jugadores=2", 14.2, "/sdcard/v.mp4")
        self.assertIn("screenrecord", grabacion)
        self.assertEqual(grabacion[grabacion.index("--time-limit") + 1], "15")  # hacia arriba (M-114)
        self.assertEqual(abrir[4:7], ["am", "start", "-S"])

    def test_segundos_con_decimales(self):
        args = telefono.build_parser().parse_args(["fotogramas", "v.mp4", "--segundos", "1.5"])
        self.assertEqual(args.segundos, 1.5)
        self.assertFalse(args.ya)

    def test_autor_jugando_por_la_partida_guardada(self):
        lines = [
            "10-08 01:56:44.950 I/Mono    (21127): MainActivity creada: CLASSIC, 2 jugadores, semilla 7",
            "10-08 02:03:50.404 I/Mono    (22478): guardada: turno 0, Roll",
            "10-08 02:04:10.000 I/Mono    (22478): turno 0 · xd: Roll -> []",
        ]
        self.assertIn("02:03:50", telefono.autor_jugando(lines, "10-08 02:05:00"))
        self.assertIsNone(telefono.autor_jugando(lines, "10-08 02:07:00"))  # hace mas de 3 min
        self.assertIsNone(telefono.autor_jugando(lines[:1], "10-08 02:05:00"))  # solo partidas de extras
        self.assertIsNone(telefono.autor_jugando(lines, ""))

    def test_fotogramas_franja_de_arriba_y_mas_grandes(self):
        argv = " ".join(telefono.tramo_command(Path("/ff"), Path("v.mp4"), 4, 2, 4, Path("h.png"), alto=0.3, columnas=2))
        self.assertIn("fps=4,crop=iw:ih*0.3:0:0,scale=1080:-1,tile=2x4", argv)
        self.assertNotIn("crop", " ".join(telefono.tramo_command(Path("/ff"), Path("v.mp4"), 4, 2, 4, Path("h.png"))))
        with self.assertRaises(ValueError):
            telefono.tramo_command(Path("/ff"), Path("v.mp4"), 0, 1, 4, Path("h.png"), alto=0)

    def test_fotogramas_de_un_tramo_con_ffmpeg(self):
        # M-107: 3 s a 8 por segundo son 24 fotogramas, 3 filas de 8.
        argv = telefono.tramo_command(Path("/ff"), Path("v.mp4"), 12.5, 3, 8, Path("h.png"))
        self.assertEqual(argv[argv.index("-ss") + 1], "12.5")
        self.assertIn("fps=8,scale=270:-1,tile=8x3", argv)
        self.assertIn("tile=8x1", " ".join(telefono.tramo_command(Path("/ff"), Path("v.mp4"), 0, 1, 5, Path("h.png"))))

    def test_copia_para_enviar(self):
        self.assertEqual(telefono.envio_de(Path("capturas/fd1.mp4")), Path("capturas/fd1_envio.mp4"))
        self.assertIn("scale=720:-2", telefono.envio_command(Path("/ff"), Path("a.mp4"), Path("b.mp4")))
        self.assertIsNone(telefono.ffmpeg_path({}))
        self.assertIsNone(telefono.ffmpeg_path({"herramientas": {"ffmpeg": "/no/existe/ffmpeg"}}))

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

    def test_capture_to_png_file_or_folder(self):
        # M-075: en dde8fe6a #38 `--salida capturas/X.png` creo una carpeta con ese nombre.
        now = telefono.dt.datetime(2026, 10, 7, 18, 15, 27)
        self.assertEqual(Path("capturas/X.png"), telefono.capture_path(Path("capturas/X.png"), now))
        self.assertEqual(Path("capturas/tmp/captura_181527.png"), telefono.capture_path(Path("capturas/tmp"), now))

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

    def test_buffer_command_agranda_main(self):
        self.assertEqual(telefono.buffer_command("adb", "abc"), ["adb", "-s", "abc", "logcat", "-b", "main", "-G", "4M"])

    def test_buffer_size_lee_main_de_logcat_g(self):
        out = ("main: ring buffer is 256 KiB (254 KiB consumed, 1 MiB readable), max entry is 5120 B\n"
               "system: ring buffer is 4 MiB (3 MiB consumed)")
        self.assertEqual(telefono.buffer_size(out), "256 KiB")
        self.assertIsNone(telefono.buffer_size("error: device offline"))

    def test_logcat_is_filtered(self):
        argv = telefono.logcat_command("adb", "abc", "Mono")
        self.assertIn("-d", argv)
        self.assertIn("time", argv)
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
    def test_am_warning(self):
        # M-054: en dfceab09 #26 la primera captura salio del menu y la salida de am start se perdia.
        ok = "Stopping: com.jacck.mono\nStarting: Intent { cmp=com.jacck.mono/.MainActivity (has extras) }\n"
        self.assertIsNone(telefono.am_warning(ok))
        mal = ok + "Warning: Activity not started, its current task has been brought to the front\n"
        self.assertTrue(telefono.am_warning(mal).startswith("Warning: Activity not started"))

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

    def test_resolver_con_prefijo(self):
        with tempfile.TemporaryDirectory() as d:
            for n in ("niza", "ic_enlace", "pj_mono"):
                (Path(d) / f"{n}.svg").write_text("<svg/>")
            self.assertEqual([hoja_arte.resolver(i, Path(d)) for i in ("niza", "enlace", "ic_enlace", "mono", "nada")],
                             ["niza", "ic_enlace", "ic_enlace", "pj_mono", None])
            self.assertEqual(hoja_arte.faltantes(["enlace", "nada"], Path(d)), ["nada"])


if __name__ == "__main__":
    unittest.main()
