#!/usr/bin/env python3
"""Tablero de arranque del agente (kit de iniciar-proyecto; viene de vid2aud, Venona y Azorian).

Lee la hoja de ruta (el plan con casillas) y cuenta las tareas por fase.
Formato de una tarea en el plan:
    - [ ] **F3.2** texto de la tarea
    - [x] **S1.1** texto de una tarea terminada
Una tarea con "*(Extra)*" en el texto es opcional: no cuenta para cerrar la fase.
Una tarea con "*(Espera a F7)*" cuenta, pero no es la siguiente hasta cerrar F7.
La fase de una tarea es el prefijo de su ID (S1.1 -> S, F3.2 -> F3, FV.2 -> FV).

Uso (desde cualquier carpeta del proyecto):
    python3 .claude/skills/agente-<nombre>/scripts/tablero.py [--plan RUTA] [--tasks N]
Con --tasks N imprime además, completas, las N tareas obligatorias abiertas siguientes
(con su *Terminado*), para no leer el plan con sed.
El plan sale del .toml del proyecto (el que tiene [proyecto] con carpeta_agente).
Solo lee; no modifica nada.
"""

from __future__ import annotations

import argparse
import re
import sys
import tomllib
from pathlib import Path

RX_TASK = re.compile(
    r"^\s*- \[(?P<done>[ xX])\] \*\*(?P<id>(?:(?P<s>S)\d+|(?P<f>F(?:\d+|[A-Z]+)))\.\d+)\*\*\s*(?P<text>.*)$"
)
RX_PHASE = re.compile(r"^#{2,4} (?P<phase>F(?:\d+|[A-Z]+)) · (?P<name>.+)$")
RX_WAIT = re.compile(r"\*\(Espera a (?P<phase>F(?:\d+|[A-Z]+))\)\*")


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


def parse_plan(text: str, name_s: str = "Skills") -> tuple[list[dict], dict]:
    """Devuelve las tareas en orden y los nombres de las fases."""
    tasks, names = [], {"S": name_s}
    for line in text.splitlines():
        m = RX_PHASE.match(line)
        if m:
            names[m["phase"]] = m["name"].strip()
            continue
        m = RX_TASK.match(line)
        if m:
            tasks.append(
                dict(
                    id=m["id"],
                    phase=m["s"] or m["f"],
                    done=m["done"] in "xX",
                    extra="*(Extra)*" in m["text"],
                    waiting=bool(RX_WAIT.search(m["text"])),
                    text=m["text"].strip(),
                )
            )
    return tasks, names


def summarize(tasks: list[dict], names: dict) -> list[str]:
    """Arma las líneas del tablero."""
    if not tasks:
        return ["No se encontraron tareas con el formato «- [ ] **F1.1** ...» en el plan."]
    phases = list(dict.fromkeys(t["phase"] for t in tasks))
    required = [t for t in tasks if not t["extra"]]
    extras = [t for t in tasks if t["extra"]]
    closed, current = [], None
    for p in phases:
        pending = [t for t in required if t["phase"] == p and not t["done"]]
        if not pending:
            closed.append(p)
        elif current is None and any(not t["waiting"] for t in pending):
            current = p
    lines = []
    if current is None:
        lines.append("Todas las fases obligatorias están cerradas.")
    else:
        mine = [t for t in required if t["phase"] == current]
        done = sum(t["done"] for t in mine)
        lines.append(f"Fase actual: {current} · {names.get(current, '')} · {done}/{len(mine)}")
    lines.append(
        f"Global: {sum(t['done'] for t in required)}/{len(required)}"
        f" (extras {sum(t['done'] for t in extras)}/{len(extras)})"
        f" · Fases cerradas: {', '.join(closed) or 'ninguna'}"
    )
    nxt = next((t for t in required if not t["done"] and not t["waiting"]), None)
    if nxt:
        lines.append(f"Siguiente: {nxt['id']} {nxt['text'][:160]}")
    started = [
        p
        for p in phases
        if p != current and p not in closed and any(t["done"] for t in required if t["phase"] == p)
    ]
    if started:
        lines.append(f"Empezadas sin cerrar: {', '.join(started)}")
    waiting = [t["id"] for t in required if t["waiting"] and not t["done"]]
    if waiting:
        lines.append(f"En espera: {', '.join(waiting)}")
    return lines


def next_tasks(tasks: list[dict], n: int) -> list[str]:
    """Las n tareas obligatorias abiertas siguientes, con su texto completo."""
    pending = [t for t in tasks if not t["done"] and not t["extra"] and not t["waiting"]]
    return [f"- {t['id']} {t['text']}{_missing_done(t)}" for t in pending[: max(n, 0)]]


def _missing_done(task: dict) -> str:
    """Una tarea sin *Terminado* se le propone al autor (arranque del agente)."""
    return "" if "*Terminado" in task["text"] else " [sin *Terminado*: propónlo al autor]"


def main() -> int:
    ap = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter
    )
    ap.add_argument("--plan", help="ruta del plan (por defecto, la del .toml del proyecto)")
    ap.add_argument("--tasks", type=int, default=0, help="imprime las N tareas siguientes")
    a = ap.parse_args()
    name_s = "Skills"
    cfg = find_config(Path.cwd()) or find_config(Path(__file__).resolve().parent)
    if cfg is not None:
        conf = tomllib.loads(cfg.read_text(encoding="utf-8"))
        name_s = f"Skills de {conf['proyecto'].get('nombre', '')}".strip()
    if a.plan:
        plan = Path(a.plan)
    elif cfg is None:
        print(
            "No se encontró el .toml del proyecto ([proyecto] con carpeta_agente).", file=sys.stderr
        )
        return 1
    else:
        plan = cfg.parent / conf["proyecto"]["plan"]
    if not plan.is_file():
        print(f"No existe el plan: {plan}", file=sys.stderr)
        return 1
    tasks, names = parse_plan(plan.read_text(encoding="utf-8"), name_s)
    print("\n".join(summarize(tasks, names)))
    if a.tasks:
        print("\n".join(["Tareas siguientes:", *next_tasks(tasks, a.tasks)]))
    return 0


if __name__ == "__main__":
    sys.exit(main())
