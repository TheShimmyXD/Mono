#!/usr/bin/env python3
"""Cierre de un paso en una orden (kit de iniciar-proyecto; viene de Venona M-049 y vid2aud).

Desde la raíz del proyecto (donde está su .toml):
- perfiles de Python (app, sci, cli, lib): ruff check --fix, ruff format, pytest -q,
  check_structure.py y check_secrets.py (de python-programmer);
- otros perfiles: los pasos de [cierre].pasos del .toml ([nombre, orden]), con las
  variables de [cierre].entorno (admiten ~); p. ej. ./gradlew :engine:test (Mono, 2026-10-06);
- todos los perfiles: el tope de ESTADO.md ([topes].estado_md).
Imprime una línea por paso y, si uno falla, las últimas líneas de su salida.
Sale 1 si algo falló; el commit va después.

Uso: python3 .claude/skills/agente-<nombre>/scripts/cierre_paso.py
"""

from __future__ import annotations

import os
import re
import shlex
import subprocess
import sys
import time
import tomllib
from pathlib import Path

PP_SCRIPTS = Path.home() / ".claude" / "skills" / "python-programmer" / "scripts"
PYTHON_PROFILES = {"app", "sci", "cli", "lib"}
# Líneas de salida que se muestran de un paso que falló.
TAIL_LINES = 15


def find_config(start: Path) -> Path | None:
    """Busca hacia arriba el .toml del proyecto: el que tiene [proyecto] con carpeta_agente."""
    for folder in (start, *start.parents):
        for candidate in sorted(folder.glob("*.toml")):
            try:
                conf = tomllib.loads(candidate.read_text(encoding="utf-8"))
            except (OSError, UnicodeDecodeError, tomllib.TOMLDecodeError):
                continue
            if "carpeta_agente" in (conf.get("proyecto") or {}):
                return candidate
    return None


def steps(root: Path, conf: dict) -> list[tuple[str, list[str]]]:
    """Nombre y orden de cada paso, en el orden en que se corren."""
    if conf["proyecto"].get("perfil", "app") not in PYTHON_PROFILES:
        return [(name, shlex.split(command)) for name, command in closing(conf)["pasos"]]
    venv = root / ".venv" / "bin"
    return [
        ("ruff check", [str(venv / "ruff"), "check", "--fix", "."]),
        ("ruff format", [str(venv / "ruff"), "format", "."]),
        ("pytest", [str(venv / "pytest"), "-q", "--color=no"]),
        ("estructura", [sys.executable, str(PP_SCRIPTS / "check_structure.py"), "."]),
        ("secretos", [sys.executable, str(PP_SCRIPTS / "check_secrets.py"), "."]),
    ]


def closing(conf: dict) -> dict:
    """[cierre] del .toml: pasos [[nombre, orden], ...] y entorno {VAR: valor con ~}."""
    section = conf.get("cierre") or {}
    env = {key: str(Path(value).expanduser()) if value.startswith("~") else value
           for key, value in (section.get("entorno") or {}).items()}
    return {"pasos": list(section.get("pasos") or []), "entorno": env}


def summarize(name: str, returncode: int, output: str) -> tuple[bool, list[str]]:
    """Si el paso salió bien y las líneas que lo resumen (pura: se prueba sin correr nada)."""
    lines = [line.rstrip() for line in output.splitlines() if line.strip()]
    last = lines[-1] if lines else "(sin salida)"
    ok = returncode == 0
    if name == "pytest":
        found = [line for line in lines if re.search(r"\d+ (passed|failed|error)", line)]
        last = found[-1].strip("= ") if found else last
    elif name == "estructura":
        result = next(
            (line.strip() for line in lines if line.strip().startswith("Resultado:")), last
        )
        errors = re.search(r"(\d+) errores", result)
        ok = ok and (errors is None or errors.group(1) == "0")
        notes = [line.strip() for line in lines if "[ERROR]" in line or "[AVISO]" in line]
        state = "OK" if ok else "FALLA"
        return ok, [f"{name}: {state} - {result}", *(f"  {note}" for note in notes)]
    elif name == "secretos":
        errors = re.search(r"(\d+) errores", last)
        ok = ok and (errors is None or errors.group(1) == "0")
    summary = [f"{name}: {'OK' if ok else 'FALLA'} - {last}"]
    if not ok:
        summary += [f"  | {line}" for line in lines[-TAIL_LINES:]]
    return ok, summary


def check_state_limit(root: Path, conf: dict) -> tuple[bool, str]:
    """ESTADO.md dentro de su tope de caracteres."""
    path = root / conf["proyecto"]["carpeta_agente"] / "ESTADO.md"
    limit = int(conf["topes"]["estado_md"])
    size = len(path.read_text(encoding="utf-8")) if path.is_file() else 0
    ok = size <= limit
    return ok, f"estado: {'OK' if ok else 'FALLA'} - {size}/{limit} caracteres"


def main() -> int:
    cfg = find_config(Path.cwd())
    if cfg is None:
        print("No se encontró el .toml del proyecto hacia arriba de la carpeta actual.")
        return 1
    root, conf = cfg.parent, tomllib.loads(cfg.read_text(encoding="utf-8"))
    all_ok = True
    for name, argv in steps(root, conf):
        start = time.monotonic()
        try:
            env = os.environ | {"NO_COLOR": "1", "QT_QPA_PLATFORM": "offscreen"}
            env |= closing(conf)["entorno"]
            done = subprocess.run(argv, cwd=root, capture_output=True, text=True, env=env)
            returncode, output = done.returncode, done.stdout + done.stderr
        except OSError as exc:
            returncode, output = 1, str(exc)
        ok, lines = summarize(name, returncode, output)
        seconds = time.monotonic() - start
        lines[0] += f" ({seconds:.0f} s)" if seconds >= 5 else ""
        print("\n".join(lines), flush=True)
        all_ok = all_ok and ok
    ok, line = check_state_limit(root, conf)
    print(line)
    all_ok = all_ok and ok
    print(
        "Cierre: listo para el commit."
        if all_ok
        else "Cierre: hay fallas; arréglalas antes del commit."
    )
    return 0 if all_ok else 1


if __name__ == "__main__":
    sys.exit(main())
