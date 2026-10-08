#!/usr/bin/env python3
"""Convierte la transcripción de una sesión de Claude Code en una traza auditable.

Kit de iniciar-proyecto: versión única de la que se copian los observadores de cada proyecto
(antes había tres copias que divergían: Azorian, Venona, vid2aud). Todo lo que se escribe pasa
por mask_secrets (correos, contraseñas, cookies, tokens y celulares).

Lee ~/.claude/projects/<proyecto>/<sesion>.jsonl (y sus subagentes) y escribe en la carpeta
de salida:
  traza.md               una línea por evento (#n | hora | tipo | resumen)
  traza.json             eventos completos (entradas de herramientas, tamaños, errores)
  contenido_escrito.md   todo el texto que la sesión escribió con Write/Edit o desde Bash
  resumen.json           métricas (llamadas, tokens, errores, skills, archivos, enmascarado)

Uso (desde la raíz del proyecto):
  extraer_sesion.py --listar                       sesiones del proyecto, con skills usadas
  extraer_sesion.py --actual                       la sesión en curso ($CLAUDE_CODE_SESSION_ID)
  extraer_sesion.py --ultima-con agente-<nombre>   la última sesión que invocó esa skill
  extraer_sesion.py 6bfed6ba                       por prefijo del id
  extraer_sesion.py --transcripcion RUTA.jsonl     una transcripción concreta (p. ej. de una
                                                   carpeta renombrada: Mono H3, L8)
Opciones: --corte-skill observador-<nombre> (excluye al propio observador) | --salida DIR
(por defecto <carpeta_agente>/Observador/trazas/<fecha>_<id8>).
Rutas y nombres salen del .toml del proyecto (el que tiene [proyecto] con carpeta_agente).
Solo lee la transcripción; solo escribe en la salida.
"""

from __future__ import annotations

import argparse
import datetime as dt
import glob
import json
import os
import re
import sys
import tomllib
from collections import Counter
from pathlib import Path

LARGE = 8000  # caracteres: resultado de herramienta "grande"

RX_BASH_WRITE = re.compile(
    r"(>>?\s*(?P<a>[\w./~-]+\.\w+))|(sed\s+-i\S*\s+(?:'[^']*'|\"[^\"]*\"|\S+)\s+(?P<b>[\w./~-]+\.\w+))"
    r"|open\((?P<c>[^,)]+),\s*['\"][wa]"
)
RX_PATH = re.compile(r"[\w./~-]*/[\w.~-]+\.(?:md|py|yaml|yml|json|toml|txt|csv|png|pdf|html|har)\b")

# (tipo, patrón, reemplazo). El orden importa: primero lo más específico.
SECRET_PATTERNS = [
    ("cookie", re.compile(r"(?im)\b((?:set-)?cookie\s*[:=]\s*)[^\n\"']+"), r"\1<cookie>"),
    (
        "auth",
        re.compile(r"(?i)\b(authorization\s*[:=]\s*)(?:bearer\s+|basic\s+)?[^\s\"',]+"),
        r"\1<auth>",
    ),
    ("token", re.compile(r"\beyJ[\w-]{8,}\.[\w-]{8,}\.[\w-]{8,}"), "<token>"),
    (
        "password",
        re.compile(
            r"(?i)\b(password|passwd|pwd|contrase(?:ñ|n)a|(?<!palabras )clave)(\\?[\"']?\s*[:=]\s*\\?[\"']?)"
            r"(?!<oculto>|(?:str|bytes|None|int|bool|Optional|Any|self|cls|getpass|HIDDEN)\b)"
            r"([^\s\"'\\&,;)}]{4,})"
        ),
        r"\1\2<oculto>",
    ),
    # "Palabras clave:" es texto comun, no una contraseña (Venona M-004).
    # Los noreply (atribución de commits) no son datos personales (M-001).
    ("correo", re.compile(r"(?<![\w.+-])(?!no-?reply@)[\w.+-]+@[\w-]+(?:\.[\w-]+)+"), "<correo>"),
    ("celular", re.compile(r"(?<![\w.])(?:\+?57\s?)?3\d{2}\s?\d{3}\s?\d{4}(?![\w.])"), "<celular>"),
    # MAC de un equipo real; las de ejemplo (AA:BB:CC:…, 11:22:33:…, 22:33:44:…) se quedan (M-128).
    (
        "mac",
        re.compile(r"(?<![\w:])(?!(?i:aa:bb:cc|11:22:33|22:33:44):)(?:[0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}(?![\w:])"),
        "<mac>",
    ),
]

# El serial del teléfono no tiene forma fija: se aprende de `adb devices` y de telefono.py (M-128).
SERIAL_SOURCES = re.compile(r"(?m)^([A-Za-z0-9]{6,32})\tdevice\b|Instalada en ([A-Za-z0-9]{6,32})\b")


def mask_secrets(text: str, counts: Counter | None = None, extra: tuple = ()) -> str:
    """Enmascara secretos y datos personales; suma en counts cuántos hubo de cada tipo."""
    for kind, rx, repl in (*SECRET_PATTERNS, *extra):
        text, n = rx.subn(repl, text)
        if n and counts is not None:
            counts[kind] += n
    return text


def mask_deep(value, counts: Counter, extra: tuple = ()):
    if isinstance(value, dict):
        return {k: mask_deep(v, counts, extra) for k, v in value.items()}
    if isinstance(value, list):
        return [mask_deep(v, counts, extra) for v in value]
    return mask_secrets(value, counts, extra) if isinstance(value, str) else value


def strings_of(value):
    if isinstance(value, dict):
        for v in value.values():
            yield from strings_of(v)
    elif isinstance(value, (list, tuple)):
        for v in value:
            yield from strings_of(v)
    elif isinstance(value, str):
        yield value


def serial_patterns(events: list, written: list) -> tuple:
    """Patrón que enmascara los seriales de teléfono vistos en la sesión (vacío si no hubo)."""
    found = set()
    for text in strings_of([events, written]):
        for m in SERIAL_SOURCES.finditer(text):
            found.add(m.group(1) or m.group(2))
    if not found:
        return ()
    rx = re.compile("|".join(re.escape(x) for x in sorted(found, key=len, reverse=True)))
    return (("serial", rx, "<serial>"),)


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


def transcripts_folder_name(root: str) -> str:
    """Nombre de la carpeta de ~/.claude/projects para un proyecto.

    Claude Code cambia por '-' todo caracter que no sea letra o número (también '_'), no solo
    '/' (hallazgo H1 de Azorian).
    """
    return re.sub(r"[^A-Za-z0-9]", "-", str(root))


CONFIG = find_config(Path.cwd()) or find_config(Path(__file__).resolve().parent)
if CONFIG is None:
    sys.exit("No se encontró el .toml del proyecto (ni desde la carpeta actual ni desde la skill).")
CONF = tomllib.loads(CONFIG.read_text(encoding="utf-8"))
PROJECT = str(CONFIG.parent)
PROJECTS = os.path.expanduser("~/.claude/projects")
OUTPUT_BASE = os.path.join(PROJECT, CONF["proyecto"]["carpeta_agente"], "Observador/trazas")


def first_cwd(path: str, max_lines: int = 50) -> str | None:
    """El cwd registrado en las primeras líneas de una transcripción."""
    with open(path, errors="replace") as f:
        for i, line in enumerate(f):
            if i >= max_lines:
                break
            m = re.search(r'"cwd":\s*"([^"]+)"', line)
            if m:
                return m.group(1)
    return None


def sessions(projects: str = PROJECTS, root: str = PROJECT) -> list[str]:
    """Transcripciones del proyecto: las de su carpeta y las de otra carpeta que registran su
    cwd (carpeta renombrada a mitad de un trabajo en segundo plano: Mono H3, M-002)."""
    own = os.path.join(projects, transcripts_folder_name(root))
    found = set(glob.glob(os.path.join(own, "*.jsonl")))
    for path in glob.glob(os.path.join(projects, "*", "*.jsonl")):
        if path in found:
            continue
        cwd = first_cwd(path)
        if cwd and (cwd == root or cwd.startswith(root + os.sep)):
            found.add(path)
    return sorted(found, key=os.path.getmtime)


# Una skill que no se cargó sola y se leyó a mano también cuenta como usada (Mono M-002).
SKILL_READ = re.compile(
    r'(?:"file_path":\s*"|\bcat\s+[^"|;&]*?)[^"]*?\.claude/skills/([\w-]+)/SKILL\.md'
)


def skills_of(path: str) -> set[str]:
    found = set()
    with open(path, errors="replace") as f:
        for line in f:
            if '"Skill"' in line:
                found.update(m.group(1) for m in re.finditer(r'"skill":\s*"([^"]+)"', line))
            if '"tool_use"' in line and "SKILL.md" in line:
                found.update(m.group(1) for m in SKILL_READ.finditer(line))
    return found


def locate(a) -> str:
    if getattr(a, "transcripcion", None):
        path = os.path.expanduser(a.transcripcion)
        if not (path.endswith(".jsonl") and os.path.isfile(path)):
            sys.exit(f"No existe la transcripción {path} (.jsonl).")
        return path
    if a.listar:
        for path in sessions():
            names = ", ".join(sorted(skills_of(path))) or "-"
            when = dt.datetime.fromtimestamp(os.path.getmtime(path))
            print(
                f"{os.path.basename(path)[:8]}  {when:%Y-%m-%d %H:%M}"
                f"  {os.path.getsize(path) // 1024:>6} KB  {names}"
            )
        sys.exit(0)
    if a.actual:
        sid = os.environ.get("CLAUDE_CODE_SESSION_ID")
        if not sid:
            sys.exit("No hay $CLAUDE_CODE_SESSION_ID: indica el id o usa --ultima-con.")
        a.sesion = sid
    if a.ultima_con:
        candidates = [p for p in sessions() if a.ultima_con in skills_of(p)]
        if not candidates:
            sys.exit(f"Ninguna sesión invocó la skill {a.ultima_con}.")
        return candidates[-1]
    candidates = [p for p in sessions() if os.path.basename(p).startswith(a.sesion or "")]
    if len(candidates) != 1:
        sys.exit(f"El id '{a.sesion}' coincide con {len(candidates)} sesiones. Usa --listar.")
    return candidates[0]


def text_of(content) -> str:
    if isinstance(content, str):
        return content
    out = []
    for block in content or []:
        if isinstance(block, dict):
            if block.get("type") == "text":
                out.append(block.get("text", ""))
            elif block.get("type") == "tool_result":
                out.append(text_of(block.get("content")))
    return "\n".join(out)


def summarize_input(name: str, e: dict) -> str:
    if name == "Bash":
        command = e.get("command", "").replace("\n", " / ")[:220]
        return f"{e.get('description', '')} [{command}]"
    if name in ("Read", "Write", "Edit"):
        extra = ""
        if name == "Read" and (e.get("offset") or e.get("limit") or e.get("pages")):
            pages = " pp." + e["pages"] if e.get("pages") else ""
            extra = f" [{e.get('offset', '')}:{e.get('limit', '')}{pages}]"
        return e.get("file_path", "") + extra
    if name == "Skill":
        return e.get("skill", "") + (" · " + e["args"][:120] if e.get("args") else "")
    if name == "Agent":
        return f"{e.get('subagent_type', 'general')}: {e.get('description', '')}"
    return json.dumps(e, ensure_ascii=False)[:200]


def bash_paths(command: str) -> tuple[set[str], set[str]]:
    """Rutas leídas y escritas por un comando de Bash (heurística)."""
    read = set(RX_PATH.findall(command))
    written = set()
    for m in RX_BASH_WRITE.finditer(command):
        path = m.group("a") or m.group("b") or (m.group("c") or "").strip("'\" ")
        if path and not path.startswith("/dev/"):
            written.add(path)
    # python embebido: open(p, 'w'), .write(, .write_text( ... se tratan como escrituras
    if re.search(
        r"open\([^)]*,\s*['\"][wa]|\.write\(|\.write_text\(|\.write_bytes\(|\.save\(", command
    ):
        for m in re.finditer(r"['\"]([\w./-]+\.(?:md|yaml|yml|json|py|toml))['\"]", command):
            written.add(m.group(1))
    return read - written, written


def new_metrics(sid: str) -> dict:
    return dict(
        sesion=sid,
        llamadas=0,
        por_herramienta={},
        errores=0,
        resultados_grandes=0,
        compactaciones=0,
        tokens_salida=0,
        tokens_entrada_max=0,
        skills=[],
        leidos=set(),
        escritos={},
    )


def process(path: str, origin: str, events: list, written: list, met: dict) -> None:
    pending = {}
    with open(path, errors="replace") as f:
        for line in f:
            try:
                o = json.loads(line)
            except json.JSONDecodeError:
                continue
            kind, ts = o.get("type"), (o.get("timestamp") or "")[11:19]
            if o.get("isCompactSummary") or (
                kind == "system" and "compact" in str(o.get("subtype", ""))
            ):
                met["compactaciones"] += 1
                events.append(
                    dict(
                        origen=origin,
                        hora=ts,
                        tipo="COMPACTACION",
                        resumen="el sistema resumió el contexto",
                    )
                )
                continue
            message = o.get("message") or {}
            if kind == "assistant":
                handle_assistant(message, origin, ts, events, written, met, pending)
            elif kind == "user":
                handle_user(o, message, origin, ts, events, met, pending)


def handle_assistant(message, origin, ts, events, written, met, pending) -> None:
    usage = message.get("usage") or {}
    met["tokens_salida"] += usage.get("output_tokens", 0)
    met["tokens_entrada_max"] = max(
        met["tokens_entrada_max"],
        usage.get("input_tokens", 0)
        + usage.get("cache_read_input_tokens", 0)
        + usage.get("cache_creation_input_tokens", 0),
    )
    for block in message.get("content") or []:
        if block.get("type") == "text" and block.get("text", "").strip():
            text = block["text"]
            events.append(
                dict(
                    origen=origin,
                    hora=ts,
                    tipo="CLAUDE",
                    resumen=text.strip()[:300].replace("\n", " "),
                    texto=text,
                )
            )
        elif block.get("type") == "tool_use":
            name, e = block.get("name"), block.get("input") or {}
            met["llamadas"] += 1
            met["por_herramienta"][name] = met["por_herramienta"].get(name, 0) + 1
            ev = dict(
                origen=origin,
                hora=ts,
                tipo="TOOL",
                herramienta=name,
                resumen=summarize_input(name, e),
                entrada=e,
            )
            if name == "Skill":
                met["skills"].append(e.get("skill"))
            if name == "Read":
                met["leidos"].add(e.get("file_path", ""))
            elif name in ("Write", "Edit"):
                p = e.get("file_path", "")
                met["escritos"][p] = met["escritos"].get(p, 0) + 1
                written.append((ts, name, p, e.get("content") or e.get("new_string") or ""))
            elif name == "Bash":
                command = e.get("command", "")
                read, wrote = bash_paths(command)
                met["leidos"].update(read)
                for p in wrote:
                    met["escritos"][p] = met["escritos"].get(p, 0) + 1
                ev["escritas_bash"] = sorted(wrote)
                if wrote or re.search(r"\.replace\(|cat\s*>|tee\s", command):
                    written.append((ts, "Bash", ",".join(sorted(wrote)) or "(bash)", command))
            pending[block.get("id")] = ev
            events.append(ev)


def handle_user(o, message, origin, ts, events, met, pending) -> None:
    content = message.get("content")
    has_text = isinstance(content, str) or (
        isinstance(content, list)
        and any(isinstance(b, dict) and b.get("type") == "text" for b in content)
    )
    if has_text:
        text = text_of(content).strip()
        if text.startswith("Base directory for this skill:"):
            return  # cuerpo de una skill cargada: ya consta en el evento Skill
        if text and not text.startswith("<system-reminder>") and not o.get("isMeta"):
            system = text.startswith(
                ("<task-notification>", "<local-command", "<command-name>", "[Request interrupted")
            )
            events.append(
                dict(
                    origen=origin,
                    hora=ts,
                    tipo="SISTEMA" if system else "AUTOR",
                    resumen=text[:300].replace("\n", " "),
                    texto=text,
                )
            )
    for block in content if isinstance(content, list) else []:
        if isinstance(block, dict) and block.get("type") == "tool_result":
            ev = pending.get(block.get("tool_use_id"))
            result = text_of(block.get("content"))
            if ev is not None:
                ev["chars_resultado"] = len(result)
                ev["error"] = bool(block.get("is_error"))
                ev["resultado_inicio"] = result[:400]
                if ev["error"]:
                    met["errores"] += 1
                if len(result) > LARGE:
                    met["resultados_grandes"] += 1


def cut_at_skill(events: list, met: dict, skill: str) -> list:
    """Descarta la sesión desde la primera invocación de skill (y el mensaje que la pidió)."""
    cut = next(
        (
            i
            for i, e in enumerate(events)
            if e.get("herramienta") == "Skill" and (e.get("entrada") or {}).get("skill") == skill
        ),
        None,
    )
    if cut is None:
        return events
    while cut > 0 and events[cut - 1]["tipo"] in ("AUTOR", "SISTEMA", "CLAUDE"):
        cut -= 1
    events = events[:cut]
    tools = [e for e in events if e["tipo"] == "TOOL"]
    met["skills"] = [e["entrada"]["skill"] for e in tools if e.get("herramienta") == "Skill"]
    met["corte"] = f"traza cortada antes de la skill {skill}"
    met["llamadas"] = len(tools)
    met["errores"] = sum(bool(e.get("error")) for e in tools)
    met["resultados_grandes"] = sum(e.get("chars_resultado", 0) > LARGE for e in tools)
    met["por_herramienta"] = dict(Counter(e["herramienta"] for e in tools))
    return events


def mask_events(events: list, written: list) -> tuple[list, list, dict]:
    """Enmascara eventos y escritos; devuelve el conteo por tipo y por evento.

    Por evento separa "entrada" (lo que escribió el asistente: comando o contenido de un
    archivo, donde suele haber datos de prueba) de "resto" (resultado de la herramienta o
    texto del autor, donde un secreto sí es grave).
    """
    total: Counter = Counter()
    by_event = {}
    masked_events = []
    extra = serial_patterns(events, written)
    for ev in events:
        sent, rest = Counter(), Counter()
        # en un evento de herramienta solo el resultado llegó de afuera; el resumen sale de la entrada
        tool = ev["tipo"] == "TOOL"
        masked = {
            k: mask_deep(v, sent if k == "entrada" or (tool and k != "resultado_inicio") else rest, extra)
            for k, v in ev.items()
        }
        masked_events.append(masked)
        if sent or rest:
            by_event[ev["n"]] = dict(
                tipo=ev["tipo"],
                herramienta=ev.get("herramienta", ""),
                entrada=dict(sent),
                resto=dict(rest),
            )
            total.update(sent + rest)
    masked_written = []
    for ts, name, path, text in written:
        counts = Counter()
        masked_written.append((ts, name, path, mask_secrets(text, counts, extra)))
        total.update(counts)
    return masked_events, masked_written, dict(total=dict(total), por_evento=by_event)


def write_outputs(out: str, sid: str, date: str, events: list, written: list, met: dict) -> None:
    os.makedirs(out, exist_ok=True)
    with open(os.path.join(out, "traza.json"), "w") as f:
        json.dump(dict(resumen=met, eventos=events), f, ensure_ascii=False, indent=1, default=list)
    with open(os.path.join(out, "resumen.json"), "w") as f:
        json.dump(met, f, ensure_ascii=False, indent=1, default=list)
    with open(os.path.join(out, "traza.md"), "w") as f:
        f.write(f"# Traza de la sesión {sid} ({date})\n\n")
        f.write(
            f"Llamadas: {met['llamadas']} · errores: {met['errores']} · resultados grandes"
            f" (>{LARGE} car.): {met['resultados_grandes']} · compactaciones:"
            f" {met['compactaciones']} · tokens de salida: {met['tokens_salida']} · contexto máx.:"
            f" {met['tokens_entrada_max']} · skills: {', '.join(met['skills']) or '-'}"
            f" · enmascarado: {met['masked']['total'] or 'nada'}\n\n"
        )
        for ev in events:
            mark = " [ERROR]" if ev.get("error") else ""
            size = (
                f" ({ev['chars_resultado']} car.)" if ev.get("chars_resultado", 0) > LARGE else ""
            )
            tool = ev.get("herramienta", "")
            origin = ev["origen"] if ev["origen"] != "principal" else ""
            f.write(
                f"#{ev['n']} {ev['hora']} {origin}{ev['tipo']}{'·' + tool if tool else ''}"
                f"{mark}{size}: {ev['resumen']}\n"
            )
    with open(os.path.join(out, "contenido_escrito.md"), "w") as f:
        for ts, name, path, text in written:
            f.write(f"\n\n===== {ts} {name} {path} =====\n{text}")


def main() -> None:
    ap = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter
    )
    ap.add_argument("sesion", nargs="?")
    ap.add_argument("--listar", action="store_true")
    ap.add_argument("--actual", action="store_true")
    ap.add_argument("--ultima-con")
    ap.add_argument("--transcripcion")
    ap.add_argument("--salida")
    ap.add_argument(
        "--corte-skill", default=None, help="descarta desde la primera invocación de esta skill"
    )
    a = ap.parse_args()
    path = locate(a)
    sid = os.path.basename(path)[:-6]
    events, written = [], []
    met = new_metrics(sid)
    process(path, "principal", events, written, met)
    for sub in sorted(glob.glob(os.path.join(path[:-6], "subagents", "*.jsonl"))):
        met["subagentes"] = met.get("subagentes", 0) + 1
        process(sub, "sub:" + os.path.basename(sub)[6:14], events, written, met)
    if a.corte_skill:
        events = cut_at_skill(events, met, a.corte_skill)
    for i, ev in enumerate(events, 1):
        ev["n"] = i
    events, written, met["masked"] = mask_events(events, written)
    met["leidos"] = sorted(x for x in met["leidos"] if x)
    met["eventos"] = len(events)
    met["mensajes_autor"] = sum(e["tipo"] == "AUTOR" for e in events)
    date = dt.datetime.fromtimestamp(os.path.getmtime(path)).strftime("%Y-%m-%d")
    out = a.salida or os.path.join(OUTPUT_BASE, f"{date}_{sid[:8]}")
    write_outputs(out, sid, date, events, written, met)
    print(out)
    print(
        f"eventos={met['eventos']} llamadas={met['llamadas']} errores={met['errores']}"
        f" grandes={met['resultados_grandes']} compactaciones={met['compactaciones']}"
        f" skills={met['skills']} enmascarado={met['masked']['total']}"
    )


if __name__ == "__main__":
    main()
