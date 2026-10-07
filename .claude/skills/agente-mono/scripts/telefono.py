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
  python3 .claude/skills/agente-mono/scripts/telefono.py cartas 1 3 6 8 --salida capturas/FA.5f_cartas.png [--tio-rico]
      (instala, abre la carta de cada casilla, captura y une con hoja_arte.py; M-037)
  python3 .claude/skills/agente-mono/scripts/telefono.py pantallas - fase=compra propiedades=true,hoja=true --salida capturas/FB.2_pantallas.png
      (instala, abre la app con cada juego de extras -'-' sin extras: el menu-, espera 6 s, captura y une; M-048)
Con varios dispositivos se prefiere el fisico (el Redmi); --serie elige uno.
"""

from __future__ import annotations

import argparse
import datetime as dt
import os
import subprocess
import sys
import time
import tomllib
from pathlib import Path

DEFAULT_LOG_LINES = 60
# Espera tras abrir una carta: el arranque en frio tarda (FA.3: una captura salio en blanco).
CARTA_ESPERA_S = 5
# Una pantalla que abre un dialogo tarda mas en aparecer entero (M-046)
PANTALLA_ESPERA_S = 6
# Extras que la app lee con getLongExtra: van con --el
LONG_EXTRAS = {"semilla"}
# Lineas de salida de gradle que se muestran si la instalacion falla.
TAIL_LINES = 15
# Estados de `adb devices` que no son 'device', con la pista para arreglarlos (Mono M-004).
DEVICE_HINTS = {
    "no permissions": "falta la regla udev de Linux (redmi.md)",
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


# El búfer `main` del Redmi trae 256 KiB: ~35 s de log; una prueba del autor dura minutos (M-066).
LOG_BUFFER = "4M"


def buffer_command(adb: str, serial: str, size: str = LOG_BUFFER) -> list[str]:
    """Agranda el búfer `main` del log (se pierde al reiniciar el teléfono; `instalar` lo repone)."""
    return [adb, "-s", serial, "logcat", "-b", "main", "-G", size]


def buffer_size(output: str) -> str | None:
    """«main: ring buffer is 4 MiB (…)» de `logcat -g` -> «4 MiB», o None."""
    for line in output.splitlines():
        if line.startswith("main:") and "ring buffer is" in line:
            return line.split("ring buffer is", 1)[1].split("(")[0].strip()
    return None


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


def carta_command(adb: str, serial: str, target: str, casilla: int, tio_rico: bool) -> list[str]:
    """Reabre la app (-S) en una partida de prueba con la carta de `casilla` abierta (extra de FA.3)."""
    argv = [adb, "-s", serial, "shell", "am", "start", "-S", "-n", target,
            "--ei", "jugadores", "2", "--el", "semilla", "7", "--ei", "casilla", str(casilla)]
    return argv + (["--ez", "tio_rico", "true"] if tio_rico else [])


def gradle_env(conf: dict) -> dict:
    """Entorno de gradle: el de [cierre] entorno (JAVA_HOME del JBR de Android Studio)."""
    env = dict(os.environ)
    for key, value in ((conf.get("cierre") or {}).get("entorno") or {}).items():
        env[key] = str(Path(value).expanduser()) if value.startswith("~") else value
    return env


def run(argv: list[str], **kwargs) -> subprocess.CompletedProcess:
    return subprocess.run(argv, capture_output=True, text=True, **kwargs)


def extras_args(item: str) -> list[str]:
    """`fase=compra,hoja=true` -> argumentos de am start; el tipo sale del valor (M-048)."""
    if item in ("", "-"):
        return []
    argv: list[str] = []
    for pair in item.split(","):
        key, _, value = pair.partition("=")
        if key in LONG_EXTRAS:
            kind = "el"
        elif value in ("true", "false"):
            kind = "ez"
        elif value.lstrip("-").isdigit():
            kind = "ei"
        else:
            kind = "es"
        argv += [f"--{kind}", key, value]
    return argv


def am_warning(output: str) -> str | None:
    """Primera linea de `am start` con Error o Warning (p. ej. la actividad no arranco), o None (M-054)."""
    return next((l.strip() for l in output.splitlines() if "Error" in l or "Warning" in l), None)


def pantalla_command(adb: str, serial: str, target: str, item: str) -> list[str]:
    """Reabre la app (-S) con los extras de `item` (M-048)."""
    return [adb, "-s", serial, "shell", "am", "start", "-S", "-n", target, *extras_args(item)]


def build_parser() -> argparse.ArgumentParser:
    """Opciones propias; lo que va tras `--` lo separa `split_passthrough` antes."""
    parser = argparse.ArgumentParser(description=__doc__.split("\n")[0], epilog="adb: telefono.py adb -- <argumentos>")
    parser.add_argument("orden", choices=["dispositivos", "instalar", "captura", "log", "emulador", "adb", "cartas", "pantallas"])
    parser.add_argument("objetivos", nargs="*", help="cartas: indices de casillas; pantallas: extras por captura")
    parser.add_argument("--tio-rico", action="store_true", help="cartas: preset Tio Rico (si no, el Clasico)")
    parser.add_argument("--serie")
    parser.add_argument("--salida", type=Path)
    parser.add_argument("-n", type=int, default=DEFAULT_LOG_LINES)
    return parser


def serie(root: Path, adb: str, serial: str, shots: list[tuple[str, list[str]]], wait: float, salida: Path) -> int:
    """Abre cada (etiqueta, orden), espera, captura y une en `salida` (hoja_arte.py --unir, venv del arte)."""
    carpeta = root / "capturas" / "tmp" / f"serie_{dt.datetime.now():%H%M%S}"
    carpeta.mkdir(parents=True)
    for k, (label, argv) in enumerate(shots):
        started = run(argv)
        warning = am_warning(started.stdout + started.stderr)
        if warning:
            print(f"{label or '-'}: {warning}")
        time.sleep(wait)
        if not screen_awake(run([adb, "-s", serial, "shell", "dumpsys", "power"]).stdout):
            print("Pantalla apagada: pide al autor que desbloquee el Redmi. Capturas a medias en " + str(carpeta))
            return 1
        name = "".join(c if c.isalnum() else "_" for c in label)
        with (carpeta / f"{k:02d}_{name}.png").open("wb") as handle:
            subprocess.run([adb, "-s", serial, "exec-out", "screencap", "-p"], stdout=handle)
    hoja = Path(__file__).with_name("hoja_arte.py")
    venv = Path("~/.cache/mono-arte/bin/python").expanduser()
    done = run([str(venv), str(hoja), "--unir", str(carpeta), "--salida", str(salida)], cwd=root)
    print(f"Instalada en {serial}; {len(shots)} capturas. " + (done.stdout + done.stderr).strip())
    return done.returncode


def cartas(args, root: Path, adb: str, serial: str, target: str | None) -> int:
    """Captura la carta de cada casilla y las une en --salida (M-037)."""
    if not args.objetivos or not all(o.isdigit() for o in args.objetivos) or args.salida is None or target is None:
        print("Uso: telefono.py cartas <casilla>... --salida capturas/<paso>_cartas.png [--tio-rico]")
        return 1
    shots = [(f"casilla_{c}", carta_command(adb, serial, target, int(c), args.tio_rico)) for c in args.objetivos]
    return serie(root, adb, serial, shots, CARTA_ESPERA_S, args.salida)


def pantallas(args, root: Path, adb: str, serial: str, target: str | None) -> int:
    """Captura la app abierta con cada juego de extras y las une en --salida (M-048)."""
    if not args.objetivos or args.salida is None or target is None:
        print("Uso: telefono.py pantallas <extras>... --salida capturas/<paso>.png  (extras: fase=compra o a=1,b=true; '-' sin extras)")
        return 1
    shots = [(item, pantalla_command(adb, serial, target, item)) for item in args.objetivos]
    return serie(root, adb, serial, shots, PANTALLA_ESPERA_S, args.salida)


def main() -> int:
    own, rest = split_passthrough(sys.argv[1:])
    args = build_parser().parse_intermixed_args(own)
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

    if args.orden in ("instalar", "cartas", "pantallas"):
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
        run(buffer_command(adb, serial))
        size = buffer_size(run([adb, "-s", serial, "logcat", "-b", "main", "-g"]).stdout)
        print(f"log: búfer main {size or 'desconocido'}")
        target = launch_target(conf)
        if args.orden == "instalar":
            if target:
                run([adb, "-s", serial, "shell", "am", "start", "-n", target])
            print(f"Instalada en {serial}" + (f" y abierta ({target})." if target else "."))
            return 0
        return (cartas if args.orden == "cartas" else pantallas)(args, root, adb, serial, target)

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
