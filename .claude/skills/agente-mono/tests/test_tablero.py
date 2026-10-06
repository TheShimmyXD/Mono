"""Pruebas de tablero.py (solo biblioteca estándar).

Uso: python3 -m unittest discover -s .claude/skills/agente-<nombre>/tests
"""

from __future__ import annotations

import subprocess
import sys
import unittest
from pathlib import Path

SCRIPTS = Path(__file__).resolve().parents[1] / "scripts"
sys.path.insert(0, str(SCRIPTS))
import tablero  # noqa: E402

EXAMPLE = """
### S · Skills
- [x] **S1.1** Crear la configuración.
- [x] **S1.2** Crear la carpeta.
### F0 · Esqueleto del proyecto
- [x] **F0.1** Generar el proyecto.
- [ ] **F0.2** README con glosario.
### F1 · Motor
- [x] **F1.1** Cuerpos.
- [ ] **F1.2** Integradores.
- [ ] **F1.3** *(Extra)* Algo opcional.
### FV · Identidad visual
- [ ] **FV.1** Paleta. *(Espera a F1)* *Terminado:* el autor la aprueba.
"""


class TestTablero(unittest.TestCase):
    def test_counts_and_next(self):
        tasks, names = tablero.parse_plan(EXAMPLE, "Skills de X")
        self.assertEqual(len(tasks), 8)
        lines = tablero.summarize(tasks, names)
        self.assertEqual(lines[0], "Fase actual: F0 · Esqueleto del proyecto · 1/2")
        self.assertIn("Global: 4/7 (extras 0/1) · Fases cerradas: S", lines[1])
        self.assertTrue(lines[2].startswith("Siguiente: F0.2"))
        self.assertEqual(lines[3], "Empezadas sin cerrar: F1")
        self.assertEqual(lines[4], "En espera: FV.1")

    def test_letter_phases_are_recognized(self):
        tasks, names = tablero.parse_plan(EXAMPLE)
        self.assertEqual(names["FV"], "Identidad visual")
        self.assertEqual(tasks[-1]["phase"], "FV")

    def test_next_tasks_skip_done_extra_and_waiting(self):
        tasks, _ = tablero.parse_plan(EXAMPLE)
        self.assertEqual(
            tablero.next_tasks(tasks, 5),
            [
                "- F0.2 README con glosario. [sin *Terminado*: propónlo al autor]",
                "- F1.2 Integradores. [sin *Terminado*: propónlo al autor]",
            ],
        )

    def test_finds_project_toml_and_ignores_pyproject(self):
        import tempfile

        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "pyproject.toml").write_text('[project]\nname = "x"\n', encoding="utf-8")
            (root / "x.toml").write_text(
                '[proyecto]\ncarpeta_agente = "Agente_X"\nplan = "PLAN.md"\n', encoding="utf-8"
            )
            (root / "sub").mkdir()
            self.assertEqual(tablero.find_config(root / "sub"), root / "x.toml")

    def test_real_plan_runs_when_inside_a_project(self):
        cfg = tablero.find_config(Path(__file__).resolve().parent)
        if cfg is None:
            self.skipTest("fuera de un proyecto (kit de iniciar-proyecto)")
        out = subprocess.run(
            [sys.executable, str(SCRIPTS / "tablero.py")],
            capture_output=True,
            text=True,
            cwd=cfg.parent,
        )
        self.assertEqual(out.returncode, 0, out.stderr)
        self.assertIn("Global:", out.stdout)


if __name__ == "__main__":
    unittest.main()
