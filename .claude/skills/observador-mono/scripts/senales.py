#!/usr/bin/env python3
"""Señales automáticas del observador sobre una traza (kit de iniciar-proyecto).

Viene del senales.py de vid2aud (W01-W12), que reemplazó al auditar_sesion.py de Venona y
Azorian. Los criterios comunes tienen el mismo número en todos los proyectos nuevos; la
letra sale de [observador].prefijo del .toml (p. ej. "W" -> W01):

  01 datos del autor o secretos enmascarados en la sesión
  02 estilo python-programmer (check_structure.py)          solo perfiles de Python
  03 cierre de paso tras la última escritura del proyecto
  04 punto de control: ESTADO después del trabajo; entrada en SESIONES
  05 economía de contexto: resultados grandes, compactaciones, lecturas de [observador].no_leer
  06 errores de herramientas
  07 verificar antes de escribir: autocorrecciones del asistente
  08 correcciones o reclamos del autor
  09 archivos de las skills sobre su tope
  10 python-programmer cargada antes del primer código       solo perfiles de Python
  11 rm, mv o sobrescritura sobre [observador].intocables fuera del scratchpad
  12+ propios del proyecto: project_signals() al final de este archivo

Lee traza.json (de extraer_sesion.py) y el estado actual del proyecto, y escribe
hallazgos.md en la misma carpeta. Cada señal es una pista para confirmar en la traza, no un
veredicto.

Uso (desde la raíz del proyecto):
  python3 .claude/skills/observador-<nombre>/scripts/senales.py <carpeta_de_la_traza>
Solo biblioteca estándar.
"""

from __future__ import annotations

import json
import re
import subprocess
import sys
import tomllib
from pathlib import Path

PYTHON_PROFILES = {"app", "sci", "cli", "lib"}
# ">" que escribe; no "2>/dev/null", "2>&1" ni ">&2" (Mono, calibración 2026-10-06).
DESTRUCTIVE = re.compile(r"\b(rm|mv|shred|truncate)\b|(?<![0-9&])>(?!&)\s*(?!/dev/null)\S")
CLOSING = ("cierre_paso.py",)
CHECKS = ("ruff", "pytest", "check_structure.py", "check_secrets.py")
COMPLAINTS = re.compile(
    r"(?i)\b(no era|no es as[ií]|est[aá] mal|otra vez|te dije|no funciona|no sirve|"
    r"no quiero|por qu[eé] hiciste|deshaz|equivocad)"
)
SELF_FIXES = re.compile(
    r"(?i)\b(corrijo|me equivoqu[eé]|en realidad no|no dice|error m[ií]o|lo arreglo|"
    r"me confund[ií])"
)
STYLE_SKILL = "python-programmer"
SCRATCH_MARKERS = ("scratchpad", "tmp_path", "/tmp/")
LARGE = 8000


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


_root: dict[str, str] = {"path": ""}


def in_paths(path: str, prefixes: list[str]) -> bool:
    """Si path (absoluta o relativa a la raíz) cae dentro de alguna ruta del proyecto.

    Solo cuenta desde la raíz: .claude/skills/x/tests/ no es tests/ del proyecto.
    """
    root = _root["path"]
    if root and path.startswith(root + "/"):
        path = path[len(root) + 1 :]
    return any(path.startswith(prefix) for prefix in prefixes)


def tools(events: list[dict]) -> list[dict]:
    return [e for e in events if e.get("tipo") == "TOOL"]


# Cuerpo de un heredoc (<<'EOF' ... EOF): son datos, no órdenes (Mono, calibración 2026-10-06).
RX_HEREDOC = re.compile(r"<<-?\s*(['\"]?)(\w+)\1[^\n]*\n.*?^\s*\2\s*$", re.M | re.S)


def strip_heredocs(command: str) -> str:
    """La orden sin el cuerpo de sus heredocs; se queda la línea que los abre."""
    return RX_HEREDOC.sub(lambda m: m.group(0).split("\n", 1)[0], command)


def command_of(event: dict) -> str:
    return (
        strip_heredocs((event.get("entrada") or {}).get("command", ""))
        if event.get("herramienta") == "Bash"
        else ""
    )


def written_paths(event: dict) -> list[str]:
    entry = event.get("entrada") or {}
    if event.get("herramienta") in ("Write", "Edit"):
        return [entry.get("file_path", "")]
    return list(event.get("escritas_bash") or [])


def last_write(events: list[dict], prefixes: list[str]) -> int:
    """Número del último evento que escribió en prefixes (0 si ninguno)."""
    found = [e["n"] for e in tools(events) if any(in_paths(p, prefixes) for p in written_paths(e))]
    return max(found, default=0)


def last_screen_write(events: list[dict], ui: str, root: Path) -> int:
    """K15 (M-014): último evento que cambió una pantalla: un .kt de la interfaz con
    @Composable o setContent (según el archivo actual), o un recurso fuera de res/values/."""
    def is_screen(path: str) -> bool:
        if "/res/values/" in path:
            return False
        if path.endswith(".kt"):
            file = Path(path) if Path(path).is_absolute() else root / path
            try:
                text = file.read_text(encoding="utf-8")
            except OSError:
                return True
            return "@Composable" in text or "setContent" in text
        return True
    found = [e["n"] for e in tools(events)
             if any(in_paths(p, [ui]) and is_screen(p) for p in written_paths(e))]
    return max(found, default=0)


def commands_after(events: list[dict], n: int) -> list[str]:
    # Azorian M-017: el propio comando de la escritura cuenta (sed -i ... && cierre_paso.py).
    return [command_of(e) for e in tools(events) if e["n"] >= n and e.get("herramienta") == "Bash"]


def missing_checks(commands: list[str], python: bool) -> list[str]:
    """03: comprobaciones de cierre que no aparecen; cierre_paso.py las cubre todas."""
    joined = "\n".join(commands)
    # Cuenta la ejecución (python ... cierre_paso.py), no escribir el script.
    if any(re.search(r"python\S*\s+\S*" + re.escape(script), joined) for script in CLOSING):
        return []
    return [check for check in CHECKS if check not in joined] if python else ["cierre_paso.py"]


def code_before_style(events: list[dict], code: list[str]) -> list[int]:
    """10: escrituras de código antes de cargar python-programmer."""
    loaded = next(
        (
            e["n"]
            for e in tools(events)
            if e.get("herramienta") == "Skill"
            and (e.get("entrada") or {}).get("skill") == STYLE_SKILL
        ),
        None,
    )
    early = []
    for e in tools(events):
        if loaded is not None and e["n"] > loaded:
            break
        if any(in_paths(p, code) and p.endswith(".py") for p in written_paths(e)):
            early.append(e["n"])
    return early


def compile_any(patterns: list[str]) -> re.Pattern | None:
    return re.compile("|".join(f"(?:{p})" for p in patterns)) if patterns else None


def forbidden_reads(events: list[dict], rx: re.Pattern | None) -> list[int]:
    """05: lecturas con Read o cat/head/tail de lo que nunca entra al contexto."""
    if rx is None:
        return []
    hits = []
    for e in tools(events):
        entry = e.get("entrada") or {}
        if e.get("herramienta") == "Read" and rx.search(entry.get("file_path", "")):
            hits.append(e["n"])
        # cat/head/tail que leen en la misma línea; "cat > x <<EOF" es una escritura.
        # El argumento acaba en |, ;, & o >: "head -3 a && wc -c b.jar" no lee b.jar (Mono M-003)
        reader = re.search(r"\b(?:cat|head|tail|less)\s+(?![>]|<<)([^|;&\n>]*)", command_of(e))
        if reader and rx.search(reader.group(1)):
            hits.append(e["n"])
    return hits


def touched_untouchables(events: list[dict], rx: re.Pattern | None) -> list[int]:
    """11: rm, mv o sobrescritura sobre lo intocable fuera del scratchpad y de las pruebas."""
    if rx is None:
        return []
    hits = []
    for e in tools(events):
        command = command_of(e)
        outside = not any(marker in command for marker in SCRATCH_MARKERS)
        if command and DESTRUCTIVE.search(command) and rx.search(command) and outside:
            hits.append(e["n"])
        elif e.get("herramienta") in ("Write", "Edit"):
            path = (e.get("entrada") or {}).get("file_path", "")
            if rx.search(path) and not any(m in path for m in SCRATCH_MARKERS):
                hits.append(e["n"])
    return hits


def author_complaints(events: list[dict]) -> list[int]:
    return [
        e["n"] for e in events if e.get("tipo") == "AUTOR" and COMPLAINTS.search(e.get("texto", ""))
    ]


# Lo que devuelve una herramienta cuando el autor rechaza la llamada (p. ej. para aclarar): no es un error (M-082).
REJECTED = "The user doesn't want to proceed"


def tool_errors(events: list[dict]) -> tuple[list[int], list[int]]:
    """(llamadas con error de verdad, llamadas que el autor rechazó para aclarar o corregir)."""
    failed = [e for e in tools(events) if e.get("error")]
    rejected = [e["n"] for e in failed if str(e.get("resultado_inicio", "")).startswith(REJECTED)]
    return [e["n"] for e in failed if e["n"] not in rejected], rejected


def self_corrections(events: list[dict]) -> list[int]:
    return [
        e["n"]
        for e in events
        if e.get("tipo") == "CLAUDE" and SELF_FIXES.search(e.get("texto", ""))
    ]


def skill_sizes(root: Path, conf: dict) -> tuple[list[str], list[str]]:
    """09: archivos de las skills por encima de su tope, y los que pasan del 90 % (consolidar, paso 6)."""
    over, near = [], []
    limits = conf["topes"]
    for skill in conf["observador"]["skills"]:
        folder = root / ".claude" / "skills" / skill
        files = [(folder / "SKILL.md", limits["skill_md"])]
        files += [
            (p, limits["referencia_md"]) for p in sorted((folder / "references").glob("*.md"))
        ]
        for path, limit in files:
            size = len(path.read_text(encoding="utf-8")) if path.is_file() else 0
            if size > limit:
                over.append(f"{path.relative_to(root)} ({size}/{limit})")
            elif size >= 0.9 * limit:
                near.append(f"{path.relative_to(root)} ({size}/{limit})")
    return over, near


def run_structure(root: Path, conf: dict) -> str:
    script = Path(conf["observador"]["check_structure"]).expanduser()
    try:
        done = subprocess.run(
            [sys.executable, str(script), str(root)], capture_output=True, text=True, timeout=120
        )
    except (OSError, subprocess.TimeoutExpired) as exc:
        return f"no se pudo correr: {exc}"
    lines = [line.strip() for line in done.stdout.splitlines() if line.strip()]
    return next(
        (line for line in lines if line.startswith("Resultado:")), lines[-1] if lines else ""
    )


def audit(root: Path, conf: dict, data: dict) -> list[tuple[str, str, str]]:
    """(criterio, severidad, texto) de cada señal."""
    events, summary = data["eventos"], data["resumen"]
    _root["path"] = str(root)
    project, observer = conf["proyecto"], conf["observador"]
    x = observer.get("prefijo", "X")
    python = project.get("perfil", "app") in PYTHON_PROFILES
    code = project.get("codigo", [])
    agent_dir = project["carpeta_agente"]
    found: list[tuple[str, str, str]] = []

    masked = (summary.get("masked") or {}).get("total") or {}
    if masked:
        found.append((f"{x}01", "Media", f"Enmascarado en la sesión: {masked}"))

    if python:
        structure = run_structure(root, conf)
        if "0 errores" not in structure:
            found.append((f"{x}02", "Alta", f"check_structure.py: {structure}"))

    last_code = last_write(events, code) if code else 0
    if last_code:
        missing = missing_checks(commands_after(events, last_code), python)
        if missing:
            found.append(
                (
                    f"{x}03",
                    "Media",
                    f"Tras la última escritura (#{last_code}) faltó: {', '.join(missing)}",
                )
            )
        if last_write(events, [f"{agent_dir}/ESTADO.md"]) < last_code:
            found.append((f"{x}04", "Media", f"ESTADO.md no se actualizó después de #{last_code}"))
    if observer["skill_agente"] in summary.get("skills", []) and not last_write(
        events, [f"{agent_dir}/SESIONES.md"]
    ):
        found.append((f"{x}04", "Media", "Sesión del agente sin entrada en SESIONES.md"))

    reads = forbidden_reads(events, compile_any(observer.get("no_leer", [])))
    if reads:
        found.append((f"{x}05", "Alta", f"Lecturas de lo que no entra al contexto: #{reads}"))
    if summary.get("resultados_grandes"):
        found.append(
            (
                f"{x}05",
                "Media",
                f"{summary['resultados_grandes']} resultados de más de {LARGE} car.",
            )
        )
    if summary.get("compactaciones"):
        found.append(
            (f"{x}05", "Alta", f"El contexto se compactó {summary['compactaciones']} vez(es)")
        )

    errors, rejected = tool_errors(events)
    if errors:
        found.append((f"{x}06", "Info", f"{len(errors)} llamadas con error: #{errors[:12]}"))

    fixes = self_corrections(events)
    if fixes:
        found.append((f"{x}07", "Info", f"Autocorrecciones del asistente: #{fixes[:12]}"))

    complaints = author_complaints(events)
    if complaints:
        found.append(
            (f"{x}08", "Alta", f"Mensajes del autor con corrección o reclamo: #{complaints}")
        )
    if rejected:
        found.append((f"{x}08", "Info", f"El autor rechazó la llamada para aclarar o corregir: #{rejected[:12]}"))

    over, near = skill_sizes(root, conf)
    if over:
        found.append((f"{x}09", "Media", "Sobre el tope: " + "; ".join(over)))
    if near:
        found.append((f"{x}09", "Info", "Cerca del tope (≥ 90 %): " + "; ".join(near)))

    if python and code:
        early = code_before_style(events, code)
        if early:
            found.append(
                (f"{x}10", "Media", f"Código escrito antes de cargar {STYLE_SKILL}: #{early}")
            )

    touched = touched_untouchables(events, compile_any(observer.get("intocables", [])))
    if touched:
        found.append((f"{x}11", "Alta", f"Se tocó algo intocable fuera del scratchpad: #{touched}"))

    found += project_signals(root, conf, data)
    return found


# Mono: IDs de reglas (R-07), código impuro en el motor (D-02, D-03) y la captura (K15).
RX_RULE_ID = re.compile(r"\bR-(\d{2,3})\b")
# «R-01..R-40» nombra un tramo del reglamento, no una regla programada (M-022).
RX_RULE_RANGE = re.compile(r"\bR-\d{2,3}\s*(?:\.\.|–)\s*R-\d{2,3}\b")
RX_CATALOG_ID = re.compile(r"^#{2,4}\s+R-(\d{2,3})\b", re.M)
RX_IMPURE = re.compile(
    r"^\s*import\s+(?:android|androidx)\.|Math\.random|System\.currentTimeMillis"
    r"|\bRandom\(\s*\)|LocalDateTime\.now|java\.io\.File\b",
    re.M,
)


# K17 (M-051): mensajes al autor en otro idioma. Palabras de función, sin las que comparten (no, a).
ENGLISH = re.compile(r"\b(the|and|is|are|this|that|with|now|next|before|after|which|it's|I'm|I'll|I've|so|then|what|there)\b", re.I)
SPANISH = re.compile(r"\b(el|la|los|las|que|y|es|en|con|para|por|una|un|del|ahora|lo|se|ya)\b", re.I)


def foreign_messages(events: list[dict]) -> list[int]:
    """Mensajes del asistente en inglés: al menos 3 palabras de función inglesas y el doble que españolas."""
    hits = []
    for e in events:
        if e.get("tipo") != "CLAUDE":
            continue
        text = re.sub(r"`[^`]*`", "", e.get("texto", ""))
        en, es = len(ENGLISH.findall(text)), len(SPANISH.findall(text))
        if en >= 3 and en > 2 * es:
            hits.append(e["n"])
    return hits


def kotlin_files(folder: Path) -> list[Path]:
    return sorted(folder.rglob("*.kt")) if folder.is_dir() else []


def rule_ids(paths: list[Path]) -> set[int]:
    """Números de R-## citados en esos archivos."""
    return {int(n) for p in paths for n in RX_RULE_ID.findall(RX_RULE_RANGE.sub("", p.read_text(encoding="utf-8")))}


def catalog_ids(path: Path) -> set[int]:
    """Números de las fichas «### R-##» de REGLAS.md."""
    text = path.read_text(encoding="utf-8") if path.is_file() else ""
    return {int(n) for n in RX_CATALOG_ID.findall(text)}


def impure_lines(root: Path, paths: list[Path]) -> list[str]:
    """ruta:línea de imports de Android, azar sin semilla, hora o archivos en el motor."""
    hits = []
    for path in paths:
        for i, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
            if RX_IMPURE.search(line):
                hits.append(f"{path.relative_to(root)}:{i}")
    return hits


def ids_text(ids: set[int]) -> str:
    return ", ".join(f"R-{n:02d}" for n in sorted(ids))


def project_signals(root: Path, conf: dict, data: dict) -> list[tuple[str, str, str]]:
    """Señales propias de Mono (K12-K15 de criterios.md), sobre el estado actual y la sesión.

    K12 R-## citada en el motor que no existe en REGLAS.md (regla 1).
    K13 motor impuro: Android, azar sin semilla, hora o archivos (D-02, D-03).
    K14 R-## del motor sin ninguna prueba que la cite (regla 5).
    K15 escritura en la interfaz sin `telefono.py captura` después (regla 7).
    K17 mensajes al autor en otro idioma que el español (M-051).
    """
    observer = conf["observador"]
    x = observer.get("prefijo", "X")
    found: list[tuple[str, str, str]] = []
    engine = kotlin_files(root / observer.get("motor", "engine/src/main"))
    cited = rule_ids(engine)
    catalog = catalog_ids(root / observer.get("reglas", "REGLAS.md"))
    if cited - catalog:
        found.append((f"{x}12", "Alta", f"Citadas en el motor sin ficha en REGLAS.md: {ids_text(cited - catalog)}"))
    impure = impure_lines(root, engine)
    if impure:
        found.append((f"{x}13", "Alta", f"Motor impuro: {', '.join(impure[:10])}"))
    tested = rule_ids(kotlin_files(root / observer.get("pruebas_motor", "engine/src/test")))
    # El KDoc de `model/` describe la forma de los datos, no la lógica: no cuenta para K14 (M-012).
    logic = rule_ids([p for p in engine if "model" not in p.parts])
    if logic - tested:
        found.append((f"{x}14", "Media", f"Reglas del motor sin prueba: {ids_text(logic - tested)}"))
    ui = observer.get("interfaz")
    events = data.get("eventos", [])
    last_ui = last_screen_write(events, ui, root) if ui else 0
    if last_ui:
        after = commands_after(events, last_ui)
        if not any("telefono.py" in c and "captura" in c for c in after):
            found.append((f"{x}15", "Media", f"Interfaz cambiada (#{last_ui}) sin captura después"))
    foreign = foreign_messages(events)
    if foreign:
        found.append((f"{x}17", "Alta", f"{len(foreign)} mensajes al autor en inglés: #{foreign[:12]}"))
    return found


def write_report(folder: Path, found: list[tuple[str, str, str]], summary: dict) -> Path:
    path = folder / "hallazgos.md"
    lines = [
        f"# Señales de la sesión {summary.get('sesion', '')[:8]}",
        "",
        f"Llamadas: {summary.get('llamadas', 0)} · errores: {summary.get('errores', 0)}"
        f" · compactaciones: {summary.get('compactaciones', 0)}"
        f" · skills: {', '.join(summary.get('skills', [])) or '-'}",
        "",
    ]
    if not found:
        lines.append("Sin señales automáticas. Queda la revisión cualitativa (SKILL.md, paso 4).")
    for criterion, severity, text in found:
        lines.append(f"- **{criterion}** · {severity} · {text}")
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    return path


def main() -> int:
    if len(sys.argv) != 2:
        print(__doc__)
        return 1
    folder = Path(sys.argv[1]).resolve()
    cfg = find_config(Path.cwd()) or find_config(Path(__file__).resolve().parent)
    if cfg is None:
        print("No se encontró el .toml del proyecto.")
        return 1
    conf = tomllib.loads(cfg.read_text(encoding="utf-8"))
    data = json.loads((folder / "traza.json").read_text(encoding="utf-8"))
    found = audit(cfg.parent, conf, data)
    report = write_report(folder, found, data["resumen"])
    print(report)
    for criterion, severity, text in found:
        print(f"{criterion} {severity}: {text[:160]}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
