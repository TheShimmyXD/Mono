"""Pruebas de extraer_sesion.py (solo biblioteca estándar).

El módulo busca el .toml del proyecto al importarse: la prueba crea uno temporal si hace falta.
"""

from __future__ import annotations

import importlib
import os
import sys
import tempfile
import unittest
from collections import Counter
from pathlib import Path

SCRIPTS = Path(__file__).resolve().parents[1] / "scripts"


def load_module():
    sys.path.insert(0, str(SCRIPTS))
    previous = os.getcwd()
    with tempfile.TemporaryDirectory() as tmp:
        Path(tmp, "x.toml").write_text(
            '[proyecto]\ncarpeta_agente = "Agente_X"\n', encoding="utf-8"
        )
        os.chdir(tmp)
        try:
            sys.modules.pop("extraer_sesion", None)
            return importlib.import_module("extraer_sesion")
        finally:
            os.chdir(previous)


extraer_sesion = load_module()


class TestExtraerSesion(unittest.TestCase):
    def test_locate_explicit_transcript(self):
        import argparse
        import tempfile

        with tempfile.TemporaryDirectory() as tmp:
            path = os.path.join(tmp, "abcd1234-x.jsonl")
            open(path, "w").close()
            args = argparse.Namespace(transcripcion=path, listar=False, actual=False)
            self.assertEqual(extraer_sesion.locate(args), path)
            args.transcripcion = os.path.join(tmp, "no.jsonl")
            with self.assertRaises(SystemExit):
                extraer_sesion.locate(args)

    def test_sessions_found_by_recorded_cwd(self):
        import json

        with tempfile.TemporaryDirectory() as tmp:
            root = "/datos/u/Project_Mono"
            own = Path(tmp, extraer_sesion.transcripts_folder_name(root))
            old = Path(tmp, extraer_sesion.transcripts_folder_name("/datos/u/Proyect_Mono"))
            own.mkdir()
            old.mkdir()
            (own / "a.jsonl").write_text("{}\n")
            (old / "b.jsonl").write_text(json.dumps({"cwd": root}) + "\n")
            (old / "c.jsonl").write_text(json.dumps({"cwd": "/datos/u/Otro"}) + "\n")
            names = [os.path.basename(p) for p in extraer_sesion.sessions(tmp, root)]
            self.assertEqual(sorted(names), ["a.jsonl", "b.jsonl"])

    def test_skill_read_by_hand_counts_as_used(self):
        import json

        def use(name, **entry):
            content = [{"type": "tool_use", "name": name, "input": entry}]
            return json.dumps({"message": {"content": content}}) + "\n"

        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp, "s.jsonl")
            path.write_text(
                use("Skill", skill="python-programmer")
                + use("Bash", command="cat .claude/skills/agente-mono/SKILL.md && cat x.toml")
                + use("Read", file_path="/r/.claude/skills/observador-mono/SKILL.md")
                + use("Bash", command="grep -n despliega .claude/skills/otra/SKILL.md")
            )
            self.assertEqual(
                extraer_sesion.skills_of(str(path)),
                {"python-programmer", "agente-mono", "observador-mono"},
            )

    def test_transcripts_folder_replaces_every_non_alphanumeric(self):
        name = extraer_sesion.transcripts_folder_name("/datos/u/0_Python_Codes/Ogata_lib")
        self.assertEqual(name, "-datos-u-0-Python-Codes-Ogata-lib")

    def test_masks_secrets_and_counts_them(self):
        counts = Counter()
        text = "password: hunter22 correo ana@ejemplo.com y noreply@anthropic.com"
        masked = extraer_sesion.mask_secrets(text, counts)
        self.assertNotIn("hunter22", masked)
        self.assertNotIn("ana@ejemplo.com", masked)
        self.assertIn("noreply@anthropic.com", masked)
        self.assertEqual(counts["password"], 1)
        self.assertEqual(counts["correo"], 1)

    def test_masks_real_macs_but_not_examples_or_times(self):
        counts = Counter()
        text = "Browsing D4:17:61:0A:1B:2C y AA:BB:CC:11:22:33, 11:22:33:44:55:66 a las 10:42:40"
        masked = extraer_sesion.mask_secrets(text, counts)
        self.assertEqual(masked, "Browsing <mac> y AA:BB:CC:11:22:33, 11:22:33:44:55:66 a las 10:42:40")
        self.assertEqual(counts["mac"], 1)

    def test_learns_phone_serial_and_masks_it_everywhere(self):
        events = [
            dict(n=1, tipo="TOOL", herramienta="Bash", entrada="adb devices", resultado="List\nabc123xyz9\tdevice\n"),
            dict(n=2, tipo="TOOL", herramienta="Bash", entrada="barrido.py abc123xyz9 head", resultado="ok"),
            dict(n=3, tipo="TOOL", herramienta="Bash", entrada="telefono.py pantallas", resultado="Instalada en abc123xyz9; 7 capturas."),
        ]
        written = [("t", "Write", "x.md", "serial abc123xyz9")]
        masked, mwritten, counts = extraer_sesion.mask_events(events, written)
        self.assertNotIn("abc123xyz9", repr(masked) + repr(mwritten))
        self.assertEqual(counts["total"]["serial"], 4)

    def test_no_serial_no_extra_masking(self):
        self.assertEqual(extraer_sesion.serial_patterns([dict(resultado="List of devices attached")], []), ())

    def test_common_phrase_is_not_a_password(self):
        self.assertEqual(
            extraer_sesion.mask_secrets("Palabras clave: robot"), "Palabras clave: robot"
        )

    def test_bash_paths_separates_reads_and_writes(self):
        read, written = extraer_sesion.bash_paths("cat a/b.md > c/d.md")
        self.assertIn("c/d.md", written)
        self.assertIn("a/b.md", read)


if __name__ == "__main__":
    unittest.main()
