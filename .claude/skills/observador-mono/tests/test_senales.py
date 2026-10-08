"""Pruebas de senales.py (solo biblioteca estándar).

Uso: python3 -m unittest discover -s .claude/skills/observador-<nombre>/tests
"""

from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
import senales  # noqa: E402

ROOT = "/p/proyecto"


def tool(n, name, **entry):
    event = {"n": n, "tipo": "TOOL", "herramienta": name, "entrada": entry}
    if name == "Bash" and "escritas" in entry:
        event["escritas_bash"] = entry.pop("escritas")
    return event


def conf(perfil="app", **observer):
    return {
        "proyecto": {"perfil": perfil, "carpeta_agente": "Agente_X", "codigo": ["src/"]},
        "observador": {
            "prefijo": "Z",
            "skill_agente": "agente-x",
            "skills": [],
            "check_structure": "/no/existe.py",
            **observer,
        },
        "topes": {"skill_md": 100, "referencia_md": 100, "estado_md": 100},
    }


class TestSenales(unittest.TestCase):
    def setUp(self):
        senales._root["path"] = ROOT

    def test_a_call_the_author_rejects_is_not_a_tool_error(self):
        asked = {**tool(80, "AskUserQuestion"), "error": True, "resultado_inicio": senales.REJECTED + " with this tool use."}
        broken = {**tool(95, "Bash", command="python3 x.py"), "error": True, "resultado_inicio": "Exit code 1"}
        self.assertEqual(senales.tool_errors([asked, broken, tool(96, "Read")]), ([95], [80]))

    def test_skill_sizes_warn_from_90_percent(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            for name, size in (("cerca", 95), ("sobre", 101), ("holgada", 50)):
                folder = root / ".claude" / "skills" / name
                folder.mkdir(parents=True)
                (folder / "SKILL.md").write_text("x" * size, encoding="utf-8")
            over, near = senales.skill_sizes(root, conf(skills=["cerca", "sobre", "holgada"]))
        self.assertEqual(over, [".claude/skills/sobre/SKILL.md (101/100)"])
        self.assertEqual(near, [".claude/skills/cerca/SKILL.md (95/100)"])

    def test_in_paths_counts_only_from_the_root(self):
        self.assertTrue(senales.in_paths(f"{ROOT}/tests/test_x.py", ["tests/"]))
        self.assertFalse(senales.in_paths(f"{ROOT}/.claude/skills/a/tests/t.py", ["tests/"]))

    def test_closing_script_must_run_not_be_written(self):
        self.assertEqual(senales.missing_checks(["python3 .claude/x/cierre_paso.py"], True), [])
        self.assertIn(
            "pytest", senales.missing_checks(["cat > scripts/cierre_paso.py <<'EOF'"], True)
        )
        self.assertEqual(senales.missing_checks([], False), ["cierre_paso.py"])
        # M-089: `python3 -u` (M-079) también es correr el cierre.
        self.assertEqual(senales.missing_checks(["python3 -u .claude/x/cierre_paso.py > /x/c.txt 2>&1"], False), [])

    def test_commands_after_include_the_writing_command(self):
        events = [
            tool(1, "Bash", command="ls"),
            tool(2, "Bash", command="sed -i s/a/b/ src/x.kt && python3 c/cierre_paso.py"),
        ]
        self.assertEqual(
            senales.commands_after(events, 2),
            ["sed -i s/a/b/ src/x.kt && python3 c/cierre_paso.py"],
        )

    def test_forbidden_reads_from_config_patterns(self):
        rx = senales.compile_any([r"\.mp4$", r"\.html?\b"])
        events = [
            tool(1, "Bash", command="cat > tests/t.py <<'EOF'\nv = 'a.mp4'\nEOF"),
            tool(2, "Bash", command="head -c 500 pagina.html"),
            tool(3, "Read", file_path="/x/clase.mp4"),
        ]
        self.assertEqual(senales.forbidden_reads(events, rx), [2, 3])
        self.assertEqual(senales.forbidden_reads(events, None), [])

    def test_forbidden_reads_stop_at_chained_commands(self):
        rx = senales.compile_any([r"\.jar$"])
        events = [
            tool(1, "Bash", command="head -3 gradlew && wc -c gradle/wrapper/gradle-wrapper.jar"),
            tool(2, "Bash", command="cat a.txt || sha256sum b.jar"),
            tool(3, "Bash", command="cat libs/motor.jar"),
        ]
        self.assertEqual(senales.forbidden_reads(events, rx), [3])

    def test_untouchables_outside_scratch(self):
        rx = senales.compile_any([r"Videos/"])
        events = [
            tool(1, "Bash", command="rm ~/Videos/clase.mp4"),
            tool(2, "Bash", command="rm /tmp/claude-1/scratchpad/Videos/a.mp4"),
            tool(3, "Write", file_path="/datos/Videos/nota.txt"),
            tool(4, "Bash", command="ls ~/Videos/"),
        ]
        self.assertEqual(senales.touched_untouchables(events, rx), [1, 3])

    def test_redirections_to_dev_null_do_not_touch(self):
        rx = senales.compile_any([r"(^|[/\s'\"])fuentes/"])
        events = [
            tool(1, "Bash", command="pdfinfo fuentes/a.pdf 2>/dev/null"),
            tool(2, "Bash", command="ls fuentes/ 2>&1 | head"),
            tool(3, "Bash", command="echo x > fuentes/a.pdf"),
            tool(4, "Bash", command="echo x >> fuentes/b.txt"),
        ]
        self.assertEqual(senales.touched_untouchables(events, rx), [3, 4])

    def test_heredoc_body_is_data_not_a_command(self):
        rx = senales.compile_any([r"(^|[/\s'\"])fuentes/"])
        body = "cd x && python3 - <<'PYEOF'\nrun('echo x > fuentes/a.pdf')\nPYEOF\npython3 c/cierre_paso.py"
        events = [tool(1, "Bash", command=body), tool(2, "Bash", command="cat > fuentes/n.txt <<EOF\nhola\nEOF")]
        self.assertEqual(senales.touched_untouchables(events, rx), [2])
        self.assertEqual(senales.command_of(events[0]), "cd x && python3 - <<'PYEOF'\npython3 c/cierre_paso.py")

    def test_code_before_style_skill(self):
        events = [
            tool(1, "Write", file_path=f"{ROOT}/src/a.py"),
            tool(2, "Skill", skill="python-programmer"),
            tool(3, "Write", file_path=f"{ROOT}/src/b.py"),
        ]
        self.assertEqual(senales.code_before_style(events, ["src/"]), [1])

    def test_audit_uses_prefix_and_skips_python_checks_for_documents(self):
        data = {
            "eventos": [
                {"n": 1, "tipo": "AUTOR", "texto": "eso está mal"},
                tool(2, "Write", file_path=f"{ROOT}/src/a.py"),
            ],
            "resumen": {"skills": []},
        }
        found = senales.audit(Path(ROOT), conf("documento"), data)
        ids = [f[0] for f in found]
        self.assertIn("Z08", ids)
        self.assertNotIn("Z02", ids)
        self.assertNotIn("Z10", ids)
        self.assertIn("Z03", ids)


def mono_conf():
    return {
        "observador": {
            "prefijo": "K",
            "motor": "engine/src/main",
            "pruebas_motor": "engine/src/test",
            "interfaz": "app/src/main",
            "reglas": "REGLAS.md",
        }
    }


class TestSenalesMono(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.root = Path(self.tmp.name)
        senales._root["path"] = str(self.root)
        (self.root / "REGLAS.md").write_text("### R-07 · Salario\n### R-08 · Cárcel\n", encoding="utf-8")

    def tearDown(self):
        self.tmp.cleanup()

    def write(self, rel, text):
        path = self.root / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")

    def signals(self, events=()):
        found = senales.project_signals(self.root, mono_conf(), {"eventos": list(events)})
        return {criterion: text for criterion, _, text in found}

    def test_clean_engine_has_no_signals(self):
        self.write("engine/src/main/Rules.kt", "/** Salario (R-07). */\nfun salary() = 0\n")
        self.write("engine/src/test/RulesTest.kt", "fun `R-07 cobra el salario`() {}\n")
        self.assertEqual(self.signals(), {})

    def test_unknown_rule_and_rule_without_test(self):
        self.write("engine/src/main/Rules.kt", "// R-07 y R-99\n")
        self.write("engine/src/test/RulesTest.kt", "// R-07\n")
        found = self.signals()
        self.assertIn("R-99", found["K12"])
        self.assertIn("R-99", found["K14"])
        self.assertNotIn("R-07", found["K14"])

    def test_model_kdoc_does_not_count_for_k14(self):
        self.write("engine/src/main/model/Options.kt", "/** Fila 1 (R-08). */\nval x = 0\n")
        self.write("engine/src/main/Rules.kt", "/** Salario (R-07). */\n")
        self.write("engine/src/test/RulesTest.kt", "// R-07\n")
        self.assertEqual(self.signals(), {})

    def test_rule_range_is_not_a_citation(self):
        # M-022: «R-01..R-40» en un KDoc de Presets.kt daba K14 R-01.
        self.write("engine/src/main/Presets.kt", "/** Monopoly (R-01..R-40). */\n/** Salario (R-07). */\n")
        self.write("engine/src/test/RulesTest.kt", "// R-07\n")
        self.assertEqual(self.signals(), {})

    def test_impure_engine(self):
        self.write("engine/src/main/Dice.kt", "import android.util.Log\nval r = Random()\nval ok = Random(seed)\n")
        found = self.signals()
        self.assertIn("Dice.kt:1", found["K13"])
        self.assertIn("Dice.kt:2", found["K13"])
        self.assertNotIn("Dice.kt:3", found["K13"])

    def test_ui_change_needs_a_capture(self):
        self.write("app/src/main/Board.kt", "@Composable\nfun Board() {}\n")
        ui = f"{self.root}/app/src/main/Board.kt"
        write = tool(1, "Write", file_path=ui)
        self.assertIn("K15", self.signals([write]))
        capture = tool(2, "Bash", command="python3 s/telefono.py captura --salida /x/scratchpad")
        self.assertNotIn("K15", self.signals([write, capture]))

    def test_reading_a_capture_counts_as_capture(self):
        # M-090: capturas tomadas por un guion propio y miradas con Read.
        self.write("app/src/main/Board.kt", "@Composable\nfun Board() {}\n")
        write = tool(1, "Write", file_path=f"{self.root}/app/src/main/Board.kt")
        script = tool(2, "Bash", command="python3 -u /x/scratchpad/f54b.py")
        self.assertIn("K15", self.signals([write, script]))
        old = tool(0, "Read", file_path=f"{self.root}/capturas/vieja.png")
        self.assertIn("K15", self.signals([old, write, script]))
        read = tool(3, "Read", file_path=f"{self.root}/capturas/f5_4b_partida.png")
        self.assertNotIn("K15", self.signals([write, script, read]))

    def test_messages_in_english(self):
        # M-051: desde #76 de dfceab09 los mensajes al autor salieron en inglés.
        es = {"n": 1, "tipo": "CLAUDE", "texto": "Ya lo tengo: el lienzo es propio y la salida va en SVG."}
        en = {"n": 2, "tipo": "CLAUDE", "texto": "The engine now stores the character and the tests pass. Next is the menu."}
        code = {"n": 3, "tipo": "CLAUDE", "texto": "Corro `the and is this that with` para ver la salida del motor."}
        self.assertNotIn("K17", self.signals([es, code]))
        self.assertIn("#[2]", self.signals([es, en, code])["K17"])

    def test_text_or_logic_without_screen_is_not_ui(self):
        # M-014: textos de res/values y un .kt sin @Composable no cambian una pantalla.
        self.write("app/src/main/ErrorText.kt", "fun message() = 1\n")
        logic = tool(1, "Write", file_path=f"{self.root}/app/src/main/ErrorText.kt")
        texts = tool(2, "Write", file_path=f"{self.root}/app/src/main/res/values/strings.xml")
        self.assertNotIn("K15", self.signals([logic, texts]))


if __name__ == "__main__":
    unittest.main()
