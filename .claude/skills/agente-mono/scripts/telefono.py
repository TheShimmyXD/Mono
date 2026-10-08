#!/usr/bin/env python3
"""El telefono (o el emulador) por adb: instalar, capturar y leer el log filtrado.

adb no esta en el PATH: sale de [android] sdk de mono.toml (H5). El log nunca se lee
completo (regla 2): solo la etiqueta de la app y los errores de AndroidRuntime, acotados.

Desde la raiz del proyecto:
  python3 .claude/skills/agente-mono/scripts/telefono.py dispositivos
  python3 .claude/skills/agente-mono/scripts/telefono.py instalar        (installDebug + abrir)
  python3 .claude/skills/agente-mono/scripts/telefono.py captura --salida <carpeta o archivo .png>  (en una carpeta: captura_HHMMSS.png)
  python3 .claude/skills/agente-mono/scripts/telefono.py log [-n 60]
  python3 .claude/skills/agente-mono/scripts/telefono.py emulador       (arranca el AVD aparte)
  python3 .claude/skills/agente-mono/scripts/telefono.py adb -- shell wm size   (cualquier orden de adb, M-007)
  python3 .claude/skills/agente-mono/scripts/telefono.py cartas 1 3 6 8 --salida capturas/FA.5f_cartas.png [--tio-rico]
      (instala, abre la carta de cada casilla, captura y une con hoja_arte.py; M-037)
  python3 .claude/skills/agente-mono/scripts/telefono.py pantallas - fase=compra propiedades=true,hoja=true --salida capturas/FB.2_pantallas.png
      (instala, abre la app con cada juego de extras -'-' sin extras: el menu-, espera 6 s, captura y une; M-048)
  python3 .claude/skills/agente-mono/scripts/telefono.py grabar maqueta=A --segundos 11 --salida capturas/FC.1_A.mp4
      (instala, abre con esos extras, graba con screenrecord, baja el video y deja al lado <nombre>_hoja.png
       con hasta 24 fotogramas por GStreamer; M-099). Los extras van en UN argumento, separados por comas
       (jugadores=2,semilla=7,maquina=0-1). Dice a que hora del telefono empezo el video y, si pasa de 30 MiB,
       deja <nombre>_envio.mp4 para SendUserFile (ffmpeg de [herramientas] en mono.toml; M-107, M-108)
  python3 .claude/skills/agente-mono/scripts/telefono.py fotogramas capturas/FD.1.mp4 --desde 12.5 --segundos 3 [--fps 8] [--alto 0.3 --columnas 2] --salida <png>
      (hoja con los fotogramas seguidos de ese tramo, 8 por fila, con ffmpeg; no usa el telefono; M-107. Con --alto,
      solo la franja de arriba de cada fotograma; con --columnas, menos por fila y mas grandes: la hoja sigue en 2160 px, M-113)
  instalar, cartas, pantallas y grabar no siguen si el autor jugo en el Redmi hace poco (una partida suya escribe
      «guardada: turno N» en el log; las de los extras no se guardan): avisan con la hora; --ya sigue igual (M-112)
Con varios dispositivos se prefiere el fisico (el Redmi); --serie elige uno.
"""

from __future__ import annotations

import argparse
import datetime as dt
import math
import os
import subprocess
import sys
import time
import tomllib
from fractions import Fraction
from pathlib import Path

DEFAULT_LOG_LINES = 60
# Espera tras abrir una carta: el arranque en frio tarda (FA.3: una captura salio en blanco).
CARTA_ESPERA_S = 5
# Una pantalla que abre un dialogo tarda mas en aparecer entero (M-046)
PANTALLA_ESPERA_S = 6
# La primera tras instalar arranca en frio: a los 6 s salio en blanco y a los 8 s bien (F5.3, M-080)
PRIMERA_EXTRA_S = 6
# Grabar (M-099): espera tras abrir antes de grabar (FC.1: 5 s con una maqueta, 2 s para ver el
# comienzo de una partida), tope de fotogramas en la hoja y su tamano (la proporcion del Redmi, 1080 x 2400).
GRABAR_ARRANQUE_S = 1
GRABAR_BIT_RATE = "4000000"
MAX_FOTOGRAMAS = 24
FOTOGRAMA = (180, 400)
COLUMNAS_FOTOGRAMAS = 8
# SendUserFile no sube archivos de mas de 30 MiB: grabar deja una copia mas liviana (M-107).
ENVIO_MAX_BYTES = 30 * 1024 * 1024
ENVIO_ANCHO = 720
TRAMO_ANCHO = 270
AUTOR_RECIENTE_S = 180
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


def ffmpeg_path(conf: dict) -> Path | None:
    """El ffmpeg de [herramientas] en mono.toml, o None si no esta configurado o no existe (M-107)."""
    raw = conf.get("herramientas", {}).get("ffmpeg")
    path = Path(raw).expanduser() if raw else None
    return path if path is not None and path.exists() else None


def tramo_command(ffmpeg: Path, video: Path, desde: float, segundos: float, fps: int, salida: Path,
                  alto: float = 1.0, columnas: int = COLUMNAS_FOTOGRAMAS) -> list[str]:
    """ffmpeg que pone en una sola imagen los fotogramas seguidos de [desde, desde + segundos), `columnas` por fila.

    Con `alto` < 1 solo la franja de arriba de cada fotograma; con menos columnas, cada uno mas grande
    (la hoja sigue en TRAMO_ANCHO * COLUMNAS_FOTOGRAMAS px de ancho, M-113).
    """
    if not 0 < alto <= 1 or columnas < 1:
        raise ValueError(f"--alto entre 0 y 1 y --columnas >= 1: {alto}, {columnas}")
    filas = max(1, -(-round(segundos * fps) // columnas))
    ancho = TRAMO_ANCHO * COLUMNAS_FOTOGRAMAS // columnas
    recorte = f"crop=iw:ih*{alto}:0:0," if alto < 1 else ""
    return [str(ffmpeg), "-v", "error", "-ss", str(desde), "-t", str(segundos), "-i", str(video),
            "-vf", f"fps={fps},{recorte}scale={ancho}:-1,tile={columnas}x{filas}", "-frames:v", "1", "-y", str(salida)]


def envio_command(ffmpeg: Path, video: Path, salida: Path) -> list[str]:
    """ffmpeg que deja una copia de 720 px de ancho, sin audio, que cabe en SendUserFile."""
    return [str(ffmpeg), "-v", "error", "-i", str(video), "-vf", f"scale={ENVIO_ANCHO}:-2", "-c:v", "libx264",
            "-crf", "26", "-an", "-y", str(salida)]


def envio_de(video: Path) -> Path:
    """La copia para enviar va al lado del video: FD.1.mp4 -> FD.1_envio.mp4."""
    return video.with_name(f"{video.stem}_envio.mp4")


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


def autor_jugando(lines: list[str], ahora: str, ventana_s: int = AUTOR_RECIENTE_S) -> str | None:
    """La ultima linea «guardada: turno» del log si es de hace menos de `ventana_s` segundos, o None (M-112).

    Solo la partida del autor se guarda (las de los extras no, D-63). `ahora` y las lineas llevan la hora
    del telefono como `logcat -v time`: `10-08 02:03:05.697`.
    """
    def hora(texto: str) -> dt.datetime | None:
        try:
            return dt.datetime.strptime("2000-" + texto[:14], "%Y-%m-%d %H:%M:%S")
        except ValueError:
            return None
    now = hora(ahora)
    for line in reversed(lines):
        if "guardada: turno" in line:
            when = hora(line)
            if now is None or when is None:
                return None
            return line if 0 <= (now - when).total_seconds() < ventana_s else None
    return None


def grabar_ordenes(adb: str, serial: str, target: str, item: str, segundos: float, remoto: str) -> list[list[str]]:
    """screenrecord primero y despues am start: el video empieza antes que la app y no pierde la primera jugada (M-111)."""
    return [[adb, "-s", serial, "shell", "screenrecord", "--time-limit", str(math.ceil(segundos)), "--bit-rate", GRABAR_BIT_RATE, remoto],
            pantalla_command(adb, serial, target, item)]


def logcat_command(adb: str, serial: str, tag: str) -> list[str]:
    """Log ya escrito (-d) con la hora de cada línea, solo la etiqueta de la app y los errores fatales."""
    return [adb, "-s", serial, "logcat", "-d", "-v", "time", f"{tag}:V", "AndroidRuntime:E", "*:S"]


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
    parser.add_argument("orden", choices=["dispositivos", "instalar", "captura", "log", "emulador", "adb", "cartas", "pantallas", "grabar", "fotogramas"])
    parser.add_argument("objetivos", nargs="*", help="cartas: indices de casillas; pantallas: extras por captura; grabar: un juego de extras; fotogramas: el video")
    parser.add_argument("--segundos", type=float, default=10, help="grabar: duracion del video (screenrecord admite hasta 180; se redondea hacia arriba); fotogramas: largo del tramo")
    parser.add_argument("--desde", type=float, default=0.0, help="fotogramas: segundo del video donde empieza el tramo")
    parser.add_argument("--fps", type=int, default=8, help="fotogramas: fotogramas por segundo del tramo")
    parser.add_argument("--alto", type=float, default=1.0, help="fotogramas: franja de arriba de cada fotograma (0 a 1)")
    parser.add_argument("--columnas", type=int, default=COLUMNAS_FOTOGRAMAS, help="fotogramas: por fila (menos = mas grandes)")
    parser.add_argument("--ya", action="store_true", help="instalar, cartas, pantallas, grabar: sigue aunque el autor haya jugado hace poco")
    parser.add_argument("--tio-rico", action="store_true", help="cartas: preset Tio Rico (si no, el Clasico)")
    parser.add_argument("--serie")
    parser.add_argument("--salida", type=Path)
    parser.add_argument("-n", type=int, default=DEFAULT_LOG_LINES)
    return parser


def capture_path(salida: Path, now: dt.datetime) -> Path:
    """Archivo de la captura: `salida` si termina en .png (como en `cartas` y `pantallas`, M-075); si no, captura_HHMMSS.png dentro."""
    return salida if salida.suffix.lower() == ".png" else salida / f"captura_{now:%H%M%S}.png"


def espera(k: int, base: float) -> float:
    """Segundos antes de la captura k de una serie: la primera, recien instalada la app, espera PRIMERA_EXTRA_S mas."""
    return base + PRIMERA_EXTRA_S if k == 0 else base


def serie(root: Path, adb: str, serial: str, shots: list[tuple[str, list[str]]], wait: float, salida: Path) -> int:
    """Abre cada (etiqueta, orden), espera, captura y une en `salida` (hoja_arte.py --unir, venv del arte)."""
    carpeta = root / "capturas" / "tmp" / f"serie_{dt.datetime.now():%H%M%S}"
    carpeta.mkdir(parents=True)
    for k, (label, argv) in enumerate(shots):
        started = run(argv)
        warning = am_warning(started.stdout + started.stderr)
        if warning:
            print(f"{label or '-'}: {warning}")
        time.sleep(espera(k, wait))
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


def fotogramas_por_segundo(segundos: int) -> Fraction:
    """Dos por segundo, o menos para que la hoja no pase de MAX_FOTOGRAMAS (M-099)."""
    return min(Fraction(2), Fraction(MAX_FOTOGRAMAS, max(segundos, 1)))


def fotogramas_command(video: Path, carpeta: Path, segundos: int) -> list[str]:
    """gst-launch-1.0 que saca los fotogramas del video, ya del tamano FOTOGRAMA, a carpeta/f_NNN.png."""
    tasa = fotogramas_por_segundo(segundos)
    ancho, alto = FOTOGRAMA
    return ["gst-launch-1.0", "-q", "filesrc", f"location={video}", "!", "qtdemux", "!", "decodebin", "!",
            "videoconvert", "!", "videorate", "!", f"video/x-raw,framerate={tasa.numerator}/{tasa.denominator}", "!",
            "videoscale", "!", f"video/x-raw,width={ancho},height={alto}", "!", "pngenc", "!",
            "multifilesink", f"location={carpeta}/f_%03d.png"]


def hoja_de(video: Path) -> Path:
    """La hoja de fotogramas va al lado del video: FC.1_A.mp4 -> FC.1_A_hoja.png."""
    return video.with_name(f"{video.stem}_hoja.png")


def grabar(args, root: Path, adb: str, serial: str, target: str | None) -> int:
    """Graba la app abierta con unos extras y deja el video y su hoja de fotogramas (M-099)."""
    if len(args.objetivos) != 1 or args.salida is None or args.salida.suffix.lower() != ".mp4" or target is None:
        print("Uso: telefono.py grabar <extras> --segundos N --salida capturas/<paso>.mp4  ('-' sin extras)")
        return 1
    if not screen_awake(run([adb, "-s", serial, "shell", "dumpsys", "power"]).stdout):
        print("Pantalla apagada: pide al autor que desbloquee el Redmi. Sin video.")
        return 1
    remoto = "/sdcard/mono_grabar.mp4"
    grabacion, abrir = grabar_ordenes(adb, serial, target, args.objetivos[0], args.segundos, remoto)
    inicio = run([adb, "-s", serial, "shell", "date", "+%H:%M:%S"]).stdout.strip()
    recorder = subprocess.Popen(grabacion, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    time.sleep(GRABAR_ARRANQUE_S)
    started = run(abrir)
    warning = am_warning(started.stdout + started.stderr)
    if warning:
        print(warning)
    recorder.wait()
    video = args.salida if args.salida.is_absolute() else root / args.salida
    video.parent.mkdir(parents=True, exist_ok=True)
    pulled = run([adb, "-s", serial, "pull", remoto, str(video)])
    run([adb, "-s", serial, "shell", "rm", remoto])
    if pulled.returncode != 0:
        print("adb pull: FALLA " + (pulled.stdout + pulled.stderr).strip()[-200:])
        return 1
    carpeta = root / "capturas" / "tmp" / f"grabar_{dt.datetime.now():%H%M%S}"
    carpeta.mkdir(parents=True)
    frames = run(fotogramas_command(video, carpeta, math.ceil(args.segundos)))
    if frames.returncode != 0:
        print("gst-launch-1.0: FALLA " + (frames.stdout + frames.stderr).strip()[-300:])
        return 1
    hoja = Path(__file__).with_name("hoja_arte.py")
    venv = Path("~/.cache/mono-arte/bin/python").expanduser()
    done = run([str(venv), str(hoja), "--unir", str(carpeta), "--salida", str(hoja_de(video)),
                "--columnas", str(COLUMNAS_FOTOGRAMAS)], cwd=root)
    print(f"{video} ({video.stat().st_size // 1024} KB, {math.ceil(args.segundos)} s, {serial}). " + (done.stdout + done.stderr).strip())
    print(f"El video empieza a las {inicio} (hora del telefono), {GRABAR_ARRANQUE_S} s antes de abrir la app: "
          f"una linea del log a las T esta en el segundo T - {inicio} (M-108, M-111).")
    ffmpeg = ffmpeg_path(args.conf)
    if video.stat().st_size > ENVIO_MAX_BYTES:
        if ffmpeg is None:
            print("Pasa de 30 MiB y no hay ffmpeg en [herramientas] de mono.toml: no cabe en SendUserFile.")
        elif run(envio_command(ffmpeg, video, envio_de(video))).returncode == 0:
            print(f"Para SendUserFile: {envio_de(video)} ({envio_de(video).stat().st_size // 1024} KB).")
    return done.returncode


def fotogramas(args, root: Path) -> int:
    """Hoja con los fotogramas seguidos de un tramo del video (M-107): para ver una animacion corta."""
    ffmpeg = ffmpeg_path(args.conf)
    if len(args.objetivos) != 1 or args.salida is None or ffmpeg is None:
        print("Uso: telefono.py fotogramas <video.mp4> --desde S --segundos N [--fps 8] --salida <png>"
              + ("" if ffmpeg else "  (falta [herramientas] ffmpeg en mono.toml)"))
        return 1
    video = Path(args.objetivos[0])
    video = video if video.is_absolute() else root / video
    salida = args.salida if args.salida.is_absolute() else root / args.salida
    salida.parent.mkdir(parents=True, exist_ok=True)
    try:
        argv = tramo_command(ffmpeg, video, args.desde, args.segundos, args.fps, salida, args.alto, args.columnas)
    except ValueError as e:
        print(e)
        return 1
    done = run(argv)
    if done.returncode != 0:
        print("ffmpeg: FALLA " + (done.stdout + done.stderr).strip()[-300:])
        return 1
    print(f"{salida}: {round(args.segundos * args.fps)} fotogramas desde el segundo {args.desde}, {args.columnas} por fila"
          + (f", franja de arriba {args.alto:.0%}." if args.alto < 1 else "."))
    return 0


def main() -> int:
    own, rest = split_passthrough(sys.argv[1:])
    args = build_parser().parse_intermixed_args(own)
    args.resto = rest

    cfg = find_config(Path.cwd())
    if cfg is None:
        print("No se encontro mono.toml hacia arriba de la carpeta actual.")
        return 1
    root, conf = cfg.parent, tomllib.loads(cfg.read_text(encoding="utf-8"))
    args.conf = conf
    adb = str(sdk_path(conf) / "platform-tools" / "adb")
    if args.orden == "fotogramas":
        return fotogramas(args, root)
    if args.orden == "grabar" and len(args.objetivos) > 1:  # antes de instalar: no toca el telefono
        print("Los extras van en un solo argumento, separados por comas: grabar jugadores=2,semilla=7,maquina=0-1 (M-108).")
        return 1

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

    if args.orden in ("instalar", "cartas", "pantallas", "grabar"):
        if not args.ya:
            tag = conf["android"].get("etiqueta_log") or conf["proyecto"]["nombre"]
            ahora = run([adb, "-s", serial, "shell", "date", "+%m-%d %H:%M:%S"]).stdout.strip()
            jugando = autor_jugando(run(logcat_command(adb, serial, tag)).stdout.splitlines(), ahora)
            if jugando:
                print(f"AVISO: el autor jugo en el Redmi hace menos de {AUTOR_RECIENTE_S // 60} min ({jugando.strip()[:40]}); "
                      "instalar o abrir la app le cierra la partida. Preguntale antes; con --ya sigue (M-112).")
                return 1
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
        return {"cartas": cartas, "pantallas": pantallas, "grabar": grabar}[args.orden](args, root, adb, serial, target)

    if args.orden == "captura":
        if args.salida is None:
            print("Falta --salida <carpeta o archivo .png> (el scratchpad, con su ruta literal).")
            return 1
        if not screen_awake(run([adb, "-s", serial, "shell", "dumpsys", "power"]).stdout):
            print("Pantalla apagada: pide al autor que desbloquee el Redmi (HyperOS no deja encenderla por adb). Sin captura.")
            return 1
        out = capture_path(args.salida, dt.datetime.now())
        out.parent.mkdir(parents=True, exist_ok=True)
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
