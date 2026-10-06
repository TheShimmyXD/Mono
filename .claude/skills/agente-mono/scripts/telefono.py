#!/usr/bin/env python3
"""El telefono (o el emulador) por adb: instalar, capturar y leer el log filtrado.

adb no esta en el PATH: sale de [android] sdk de mono.toml (H5). El log nunca se lee
completo (regla 2): solo la etiqueta de la app y los errores de AndroidRuntime, acotados.

Desde la raiz del proyecto:
  python3 .claude/skills/agente-mono/scripts/telefono.py dispositivos
  python3 .claude/skills/agente-mono/scripts/telefono.py instalar        (installDebug + abrir)
  python3 .claude/skills/agente-mono/scripts/telefono.py captura --salida <scratchpad>
  python3 .claude/skills/agente-mono/scripts/telefono.py log [-n 60]
  python3 .claude/skills/agente-mono/scripts/telefono.py emulador       (arranca el AVD aparte)
Con varios dispositivos se prefiere el fisico (el Redmi); --serie elige uno.
"""

from __future__ import annotations

import argparse
import datetime as dt
import os
import subprocess
import sys
import tomllib
from pathlib import Path

DEFAULT_LOG_LINES = 60
# Lineas de salida de gradle que se muestran si la instalacion falla.
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


def sdk_path(conf: dict) -> Path:
    return Path(conf["android"]["sdk"]).expanduser()


def parse_devices(output: str) -> list[str]:
    """Series en estado 'device' de `adb devices` (sin unauthorized ni offline)."""
    serials = []
    for line in output.splitlines():
        parts = line.split()
        if len(parts) >= 2 and parts[1] == "device" and not line.startswith("List of"):
            serials.append(parts[0])
    return serials


def pick_device(serials: list[str], wanted: str | None = None) -> str | None:
    """La serie pedida, o el primer telefono fisico, o el primer emulador."""
    if wanted:
        return wanted if wanted in serials else None
    physical = [s for s in serials if not s.startswith("emulator-")]
    return (physical or serials or [None])[0]


def logcat_command(adb: str, serial: str, tag: str) -> list[str]:
    """Log ya escrito (-d), solo la etiqueta de la app y los errores fatales."""
    return [adb, "-s", serial, "logcat", "-d", "-v", "brief", f"{tag}:V", "AndroidRuntime:E", "*:S"]


def launch_target(conf: dict) -> str | None:
    """paquete/actividad para `am start -n`; None si F0.2 aun no fijo el paquete."""
    package = conf["android"].get("paquete") or ""
    activity = conf["android"].get("actividad") or ".MainActivity"
    return f"{package}/{activity}" if package else None


def gradle_env(conf: dict) -> dict:
    """Entorno de gradle: el de [cierre] entorno (JAVA_HOME del JBR de Android Studio)."""
    env = dict(os.environ)
    for key, value in ((conf.get("cierre") or {}).get("entorno") or {}).items():
        env[key] = str(Path(value).expanduser()) if value.startswith("~") else value
    return env


def run(argv: list[str], **kwargs) -> subprocess.CompletedProcess:
    return subprocess.run(argv, capture_output=True, text=True, **kwargs)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    parser.add_argument("orden", choices=["dispositivos", "instalar", "captura", "log", "emulador"])
    parser.add_argument("--serie")
    parser.add_argument("--salida", type=Path)
    parser.add_argument("-n", type=int, default=DEFAULT_LOG_LINES)
    args = parser.parse_args()

    cfg = find_config(Path.cwd())
    if cfg is None:
        print("No se encontro mono.toml hacia arriba de la carpeta actual.")
        return 1
    root, conf = cfg.parent, tomllib.loads(cfg.read_text(encoding="utf-8"))
    adb = str(sdk_path(conf) / "platform-tools" / "adb")

    if args.orden == "emulador":
        emulator = sdk_path(conf) / "emulator" / "emulator"
        log = root / "build" / "emulador.log"
        log.parent.mkdir(exist_ok=True)
        with log.open("w") as handle:
            subprocess.Popen(
                ["setsid", str(emulator), "-avd", conf["android"]["avd"]],
                stdout=handle, stderr=subprocess.STDOUT, start_new_session=True,
            )
        print(f"Emulador {conf['android']['avd']} arrancando (log: build/emulador.log).")
        return 0

    serials = parse_devices(run([adb, "devices"]).stdout)
    if args.orden == "dispositivos":
        print("\n".join(serials) if serials else "Ninguno: conecta el Redmi con depuracion USB.")
        return 0
    serial = pick_device(serials, args.serie)
    if serial is None:
        print("Sin dispositivo: conecta el Redmi (depuracion USB) o corre `telefono.py emulador`.")
        return 1

    if args.orden == "instalar":
        env = gradle_env(conf) | {"ANDROID_SERIAL": serial}
        done = run(["./gradlew", "--console=plain", "-q", ":app:installDebug"], cwd=root, env=env)
        if done.returncode != 0:
            lines = (done.stdout + done.stderr).strip().splitlines()
            print("installDebug: FALLA\n" + "\n".join(lines[-TAIL_LINES:]))
            return 1
        target = launch_target(conf)
        if target:
            run([adb, "-s", serial, "shell", "am", "start", "-n", target])
        print(f"Instalada en {serial}" + (f" y abierta ({target})." if target else "."))
        return 0

    if args.orden == "captura":
        if args.salida is None:
            print("Falta --salida <carpeta> (el scratchpad, con su ruta literal).")
            return 1
        args.salida.mkdir(parents=True, exist_ok=True)
        out = args.salida / f"captura_{dt.datetime.now():%H%M%S}.png"
        with out.open("wb") as handle:
            subprocess.run([adb, "-s", serial, "exec-out", "screencap", "-p"], stdout=handle)
        print(f"{out} ({out.stat().st_size // 1024} KB, {serial})")
        return 0

    tag = conf["android"].get("etiqueta_log") or conf["proyecto"]["nombre"]
    lines = run(logcat_command(adb, serial, tag)).stdout.splitlines()
    print("\n".join(lines[-args.n:]) if lines else f"Sin lineas de {tag} ni errores fatales.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
