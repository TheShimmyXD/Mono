#!/usr/bin/env python3
"""Renderiza UNA pagina o un recorte de un reglamento escaneado a PNG (regla 2 del mandato).

Los reglamentos no tienen texto (son imagenes): se leen como imagen, una pagina o un recorte
a la vez, y lo leido se transcribe a REGLAS.md para no volver a abrirlos. Nunca rangos.

Desde la raiz del proyecto (donde esta mono.toml):
  python3 .claude/skills/agente-mono/scripts/pagina.py --info monopoly
  python3 .claude/skills/agente-mono/scripts/pagina.py monopoly 3 --salida <scratchpad>
  python3 .claude/skills/agente-mono/scripts/pagina.py tio_rico 1 --recorte 0,0,0.5,0.34 \
      --dpi 150 --salida <scratchpad>
  python3 .claude/skills/agente-mono/scripts/pagina.py tio_rico 1 --rejilla 2x3

--recorte x,y,an,al: fracciones de la pagina (0-1) desde la esquina superior izquierda.
--rejilla CxF: imprime los recortes de una rejilla de C columnas y F filas (no renderiza),
  para cubrir una pagina grande entera y listarla en REGLAS.md.
<fuente> es una clave de [fuentes] en mono.toml o una ruta a un PDF.
"""

from __future__ import annotations

import argparse
import re
import subprocess
import sys
import tomllib
from pathlib import Path

DEFAULT_DPI = 100
# Encima de esto la imagen pesa mucho en el contexto sin ganar legibilidad (escaneos).
MAX_DPI = 200
POINTS_PER_INCH = 72


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


def resolve_source(name: str, root: Path, conf: dict) -> Path:
    """Clave de [fuentes] o ruta directa."""
    sources = conf.get("fuentes") or {}
    return root / sources[name] if name in sources else Path(name)


def parse_crop(text: str) -> tuple[float, float, float, float]:
    """'x,y,an,al' en fracciones de la pagina; cada valor en [0, 1] y el recorte dentro."""
    parts = [float(p) for p in text.split(",")]
    if len(parts) != 4:
        raise ValueError("el recorte lleva 4 valores: x,y,an,al")
    x, y, w, h = parts
    if min(parts) < 0 or w <= 0 or h <= 0 or x + w > 1 + 1e-9 or y + h > 1 + 1e-9:
        raise ValueError("el recorte debe quedar dentro de la pagina (fracciones de 0 a 1)")
    return x, y, w, h


def crop_pixels(
    page_pts: tuple[float, float], dpi: int, crop: tuple[float, float, float, float]
) -> tuple[int, int, int, int]:
    """Recorte en fracciones -> x, y, ancho, alto en pixeles a ese dpi (para pdftoppm)."""
    width_px = page_pts[0] / POINTS_PER_INCH * dpi
    height_px = page_pts[1] / POINTS_PER_INCH * dpi
    x, y, w, h = crop
    return round(x * width_px), round(y * height_px), round(w * width_px), round(h * height_px)


def grid(columns: int, rows: int) -> list[tuple[float, float, float, float]]:
    """Recortes que cubren la pagina entera, por filas (de arriba abajo, izquierda a derecha)."""
    return [
        (c / columns, r / rows, 1 / columns, 1 / rows) for r in range(rows) for c in range(columns)
    ]


def output_stem(pdf: Path, page: int, crop: tuple[float, float, float, float] | None) -> str:
    """Nombre del PNG sin puntos: <pdf>_p<N>[_r<x>-<y>-<an>-<al> en porcentaje]."""
    stem = re.sub(r"[^A-Za-z0-9_-]", "", pdf.stem)
    tag = "_r" + "-".join(str(round(v * 100)) for v in crop) if crop else ""
    return f"{stem}_p{page}{tag}"


def render_command(
    pdf: Path, page: int, dpi: int, prefix: Path, pixels: tuple[int, int, int, int] | None
) -> list[str]:
    """Orden de pdftoppm para una sola pagina (-f N -l N), con recorte opcional."""
    argv = ["pdftoppm", "-f", str(page), "-l", str(page), "-r", str(dpi), "-png", "-singlefile"]
    if pixels is not None:
        x, y, w, h = pixels
        argv += ["-x", str(x), "-y", str(y), "-W", str(w), "-H", str(h)]
    return [*argv, str(pdf), str(prefix)]


def pdf_info(pdf: Path) -> tuple[int, tuple[float, float]]:
    """Numero de paginas y tamano de la primera en puntos (pdfinfo)."""
    out = subprocess.run(["pdfinfo", str(pdf)], capture_output=True, text=True, check=True).stdout
    pages = int(re.search(r"^Pages:\s+(\d+)", out, re.M).group(1))
    size = re.search(r"^Page size:\s+([\d.]+) x ([\d.]+)", out, re.M)
    return pages, (float(size.group(1)), float(size.group(2)))


def page_size(pdf: Path, page: int) -> tuple[float, float]:
    """Tamano de una pagina concreta en puntos."""
    argv = ["pdfinfo", "-f", str(page), "-l", str(page), str(pdf)]
    out = subprocess.run(argv, capture_output=True, text=True, check=True).stdout
    size = re.search(rf"^Page\s+{page} size:\s+([\d.]+) x ([\d.]+)", out, re.M)
    return float(size.group(1)), float(size.group(2))


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    parser.add_argument("fuente")
    parser.add_argument("pagina", type=int, nargs="?")
    parser.add_argument("--info", action="store_true")
    parser.add_argument("--recorte")
    parser.add_argument("--rejilla")
    parser.add_argument("--dpi", type=int, default=DEFAULT_DPI)
    parser.add_argument("--salida", type=Path)
    args = parser.parse_args()

    cfg = find_config(Path.cwd())
    root, conf = (cfg.parent, tomllib.loads(cfg.read_text("utf-8"))) if cfg else (Path.cwd(), {})
    pdf = resolve_source(args.fuente, root, conf)
    if not pdf.is_file():
        print(f"No existe {pdf} (F0.1 copia los reglamentos a fuentes/).")
        return 1
    pages, size = pdf_info(pdf)
    if args.info:
        print(f"{pdf.name}: {pages} paginas; pagina 1 de {size[0]:.0f} x {size[1]:.0f} pt")
        return 0
    if args.pagina is None or not 1 <= args.pagina <= pages:
        print(f"Indica una pagina entre 1 y {pages}.")
        return 1
    if args.rejilla:
        columns, rows = (int(n) for n in args.rejilla.lower().split("x"))
        for x, y, w, h in grid(columns, rows):
            print(f"--recorte {x:.4g},{y:.4g},{w:.4g},{h:.4g}")
        return 0
    if args.salida is None:
        print("Falta --salida <carpeta> (el scratchpad, con su ruta literal).")
        return 1
    if not 1 <= args.dpi <= MAX_DPI:
        print(f"--dpi entre 1 y {MAX_DPI}.")
        return 1
    crop = parse_crop(args.recorte) if args.recorte else None
    pixels = crop_pixels(page_size(pdf, args.pagina), args.dpi, crop) if crop else None
    args.salida.mkdir(parents=True, exist_ok=True)
    prefix = args.salida / output_stem(pdf, args.pagina, crop)
    subprocess.run(render_command(pdf, args.pagina, args.dpi, prefix, pixels), check=True)
    # pdftoppm -singlefile anade .png al prefijo (no se usa with_suffix: el nombre lleva puntos).
    out = prefix.parent / f"{prefix.name}.png"
    print(f"{out} ({out.stat().st_size // 1024} KB)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
