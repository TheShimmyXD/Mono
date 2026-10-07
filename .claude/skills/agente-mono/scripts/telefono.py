#!/usr/bin/env python3
"""El telefono (o el emulador) por adb: instalar, capturar y leer el log filtrado.

adb no esta en el PATH: sale de [android] sdk de mono.toml (H5). El log nunca se lee
completo (regla 2): solo la etiqueta de la app y los errores de AndroidRuntime, acotados.

Desde la raiz del proyecto:
  python3 .claude/skills/agente-mono/scripts/telefono.py dispositivos
  python3 .claude/skills/agente-mono/scripts/telefono.py instalar        (installDebug + abrir)
  python3 .claude/skills/agente-mono/scripts/telefono.py captura --salida <carpeta>  (crea captura_HHMMSS.png)
  python3 .claude/skills/agente-mono/scripts/telefono.py log [-n 60]
  python3 .claude/skills/agente-mono/scripts/telefono.py emulador       (arranca el AVD aparte)
  python3 .claude/skills/agente-mono/scripts/telefono.py adb -- shell wm size   (cualquier orden de adb, M-007)
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
# Estados de `adb devices` que no son 'device', con la pista para arreglarlos (Mono M-004).
DEVICE_HINTS = {
    "no permissions": "falta la regla udev de Linux (interfaz.md, 'Redmi listo')",
    "unauthorized": "acepta 'Permitir depuracion USB' en la pantalla del telefono",
    "offline": "desconecta y vuelve a conectar el cable",
}
# Fallos de installDebug con causa conocida.
INSTALL_HINTS = {
    "INSTALL_FAILED_USER_RESTRICTED": (
        "HyperOS: activa 'Instalar via USB' en Opciones de desarrollador "
        "y toca Instalar en el telefono cuando lo pregunte"
    ),
}


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


def device_problems(output: str) -> list[str]:
    """Telefonos que adb ve pero no puede usar, cada uno con su pista."""
    problems = []
    for line in output.splitlines():
        parts = line.split(None, 1)
        if len(parts) < 2 or line.startswith(("List of", "*")) or parts[1].startswith("device"):
            continue
        state = parts[1].strip()
        key = next((k for k in DEVICE_HINTS if state.startswith(k)), None)
        label = key or state.split()[0]
        problems.append(f"{parts[0]}: {label} -> {DEVICE_HINTS.get(key, 'estado desconocido de adb')}")
    return problems


def install_hint(output: str) -> str | None:
    """La pista de un fallo de installDebug conocido, o None."""
    return next((hint for code, hint in INSTALL_HINTS.items() if code in output), None)


def pick_device(serials: list[str], wanted: str | None = None) -> str | None:
    """La serie pedida, o el primer telefono fisico, o el primer emulador."""
    if wanted:
        return wanted if wanted in serials else None
    physical = [s for s in serials if not s.startswith("emulator-")]
    return (physical or serials or [None])[0]


def logcat_command(adb: str, serial: str, tag: str) -> list[str]:
    """Log ya escrito (-d), solo la etiqueta de la app y los errores fatales."""
    return [adb, "-s", serial, "logcat", "-d", "-v", "brief", f"{tag}:V", "AndroidRuntime:E", "*:S"]


def adb_passthrough(adb: str, serial: str, rest: list[str]) -> list[str]:
    """Orden de adb con la ruta del SDK y la serie elegida; quita el '--' separador."""
    return [adb, "-s", serial, *(rest[1:] if rest[:1] == ["--"] else rest)]


def split_passthrough(argv: list[str]) -> tuple[list[str], list[str]]:
    """Separa lo que va tras `--` (para adb) de las opciones propias (M-018: REMAINDER se las tragaba)."""
    if "--" in argv:
        i = argv.index("--")
        return argv[:i], argv[i + 1:]
    return argv, []


def screen_awake(dumpsys_power: str) -> bool:
    """False si `dumpsys power` dice que la pantalla no está despierta (M-020); sin el dato, True."""
    for line in dumpsys_power.splitlines():
        line = line.strip()
        if line.startswith("mWakefulness="):
            return line == "mWakefulness=Awake"
    return True


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


def build_parser() -> argparse.ArgumentParser:
    """Opciones propias; lo que va tras `--` lo separa `split_passthrough` antes."""
    parser = argparse.ArgumentParser(description=__doc__.split("\n")[0], epilog="adb: telefono.py adb -- <argumentos>")
    parser.add_argument("orden", choices=["dispositivos", "instalar", "captura", "log", "emulador", "adb"])
    parser.add_argument("--serie")
    parser.add_argument("--salida", type=Path)
    parser.add_argument("-n", type=int, default=DEFAULT_LOG_LINES)
    return parser


def main() -> int:
    own, rest = split_passthrough(sys.argv[1:])
    args = build_parser().parse_args(own)
    args.resto = rest

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

    listing = run([adb, "devices"]).stdout
    serials, problems = parse_devices(listing), device_problems(listing)
    if args.orden == "dispositivos":
        lines = serials + problems
        print("\n".join(lines) if lines else "Ninguno: conecta el Redmi con depuracion USB.")
        return 0
    serial = pick_device(serials, args.serie)
    if serial is None:
        print("\n".join(problems) if problems else
              "Sin dispositivo: conecta el Redmi (depuracion USB) o corre `telefono.py emulador`.")
        return 1

    if args.orden == "adb":
        if not args.resto:
            print("Falta la orden: telefono.py adb -- <argumentos> (p. ej. shell wm size).")
            return 1
        done = run(adb_passthrough(adb, serial, args.resto))
        print((done.stdout + done.stderr).rstrip())
        return done.returncode

    if args.orden == "instalar":
        env = gradle_env(conf) | {"ANDROID_SERIAL": serial}
        done = run(["./gradlew", "--console=plain", "-q", ":app:installDebug"], cwd=root, env=env)
        if done.returncode != 0:
            output = (done.stdout + done.stderr).strip()
            hint = install_hint(output)
            if hint:
                print(f"installDebug: FALLA -> {hint}")
            else:
                print("installDebug: FALLA\n" + "\n".join(output.splitlines()[-TAIL_LINES:]))
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
        if not screen_awake(run([adb, "-s", serial, "shell", "dumpsys", "power"]).stdout):
            print("Pantalla apagada: pide al autor que desbloquee el Redmi (HyperOS no deja encenderla por adb). Sin captura.")
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
