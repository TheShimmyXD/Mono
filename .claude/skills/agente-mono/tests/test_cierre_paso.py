"""Pruebas de cierre_paso.py (solo biblioteca estándar; no corre ruff ni pytest)."""

from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
import cierre_paso  # noqa: E402


class TestCierrePaso(unittest.TestCase):
    def test_pytest_summary_line(self):
        ok, lines = cierre_paso.summarize("pytest", 0, "....\n===== 12 passed in 0.5s =====\n")
        self.assertTrue(ok)
        self.assertEqual(lines, ["pytest: OK - 12 passed in 0.5s"])

    def test_structure_with_errors_fails_even_with_exit_zero(self):
        ok, lines = cierre_paso.summarize(
            "estructura", 0, "[ERROR] a.py\nResultado: 1 errores, 0 avisos\n"
        )
        self.assertFalse(ok)
        self.assertIn("[ERROR] a.py", lines[1])

    def test_document_profile_has_no_python_steps(self):
        conf = {"proyecto": {"perfil": "documento"}}
        self.assertEqual(cierre_paso.steps(Path("/x"), conf), [])
        conf = {"proyecto": {"perfil": "app"}}
        self.assertEqual(len(cierre_paso.steps(Path("/x"), conf)), 5)

    def test_other_profile_runs_steps_from_toml(self):
        conf = {
            "proyecto": {"perfil": "otro"},
            "cierre": {
                "pasos": [["motor", "./gradlew --console=plain :engine:test"]],
                "entorno": {"JAVA_HOME": "~/jbr", "CI": "1"},
            },
        }
        self.assertEqual(
            cierre_paso.steps(Path("/x"), conf),
            [("motor", ["./gradlew", "--console=plain", ":engine:test"])],
        )
        env = cierre_paso.closing(conf)["entorno"]
        self.assertEqual(env["JAVA_HOME"], str(Path.home() / "jbr"))
        self.assertEqual(env["CI"], "1")

    def test_estado_limit(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "Agente_X").mkdir()
            (root / "Agente_X" / "ESTADO.md").write_text("a" * 50, encoding="utf-8")
            conf = {"proyecto": {"carpeta_agente": "Agente_X"}, "topes": {"estado_md": 40}}
            ok, line = cierre_paso.check_state_limit(root, conf)
            self.assertFalse(ok)
            self.assertIn("50/40", line)


if __name__ == "__main__":
    unittest.main()
