#!/usr/bin/env python3
"""Hojas de contacto del arte (FA): dibujos de arte/svg o capturas de cartas, en una imagen de <= 1600 px.

Con el venv de cairosvg y Pillow (arte.md §2), desde la raiz del proyecto:
  ~/.cache/mono-arte/bin/python .claude/skills/agente-mono/scripts/hoja_arte.py <id>... --salida <png>
      corre arte/arte.py (se detiene si falla) y une los SVG de esos ids, con su nombre debajo.
  ~/.cache/mono-arte/bin/python .claude/skills/agente-mono/scripts/hoja_arte.py --unir <carpeta> --salida <png>
      une las capturas PNG de la carpeta de izquierda a derecha y borra la carpeta (lo llama `telefono.py cartas`).
Mono M-037: antes se reescribia en el scratchpad en cada sesion. Fondo blanco: lo transparente
(personajes sin fondo) salia negro (M-053).
"""

from __future__ import annotations

import argparse
import io
import shutil
import subprocess
import sys
from pathlib import Path

VENV_ARTE = Path("~/.cache/mono-arte/bin/python").expanduser()
ANCHO_MAX = 1600
SEP = 8
COLUMNAS = 3
ALTO_CAPTURA = 900


def celda(columnas: int = COLUMNAS, ancho_max: int = ANCHO_MAX, sep: int = SEP) -> int:
    """Ancho de cada dibujo para que la hoja no pase de ancho_max."""
    return (ancho_max - sep * (columnas + 1)) // columnas


def filas(n: int, columnas: int = COLUMNAS) -> int:
    return (n + columnas - 1) // columnas


def alto_comun(tamanos: list[tuple[int, int]], ancho_max: int = ANCHO_MAX, sep: int = SEP,
               tope: int = ALTO_CAPTURA) -> int:
    """Alto comun de las capturas puestas en fila sin pasar de ancho_max (ni de tope)."""
    proporcion = sum(w / h for w, h in tamanos)
    return min(tope, int((ancho_max - sep * (len(tamanos) - 1)) / proporcion))


def faltantes(ids: list[str], carpeta: Path) -> list[str]:
    return [i for i in ids if not (carpeta / f"{i}.svg").exists()]


def correr_arte(raiz: Path) -> bool:
    """arte/arte.py con su salida entera (una linea); False si falla (no se arma la hoja)."""
    hecho = subprocess.run([sys.executable, str(raiz / "arte" / "arte.py")], cwd=raiz, capture_output=True, text=True)
    salida = (hecho.stdout + hecho.stderr).strip().splitlines()
    print("\n".join(salida[-6:]) if hecho.returncode else (salida[-1] if salida else "arte: sin salida"))
    return hecho.returncode == 0


def hoja_svg(raiz: Path, ids: list[str], salida: Path) -> int:
    import cairosvg
    from PIL import Image, ImageDraw

    carpeta = raiz / "arte" / "svg"
    sin_svg = faltantes(ids, carpeta)
    if sin_svg:
        print("Sin SVG (¿registrado en LUGARES?): " + ", ".join(sin_svg))
        return 1
    ancho = celda()
    dibujos = [(i, Image.open(io.BytesIO(cairosvg.svg2png(url=str(carpeta / f"{i}.svg"), output_width=ancho, background_color="white"))).convert("RGB"))
               for i in ids]
    alto = dibujos[0][1].height + 16
    columnas = min(COLUMNAS, len(ids))
    hoja = Image.new("RGB", (columnas * (ancho + SEP) + SEP, filas(len(ids)) * (alto + SEP) + SEP), "white")
    lapiz = ImageDraw.Draw(hoja)
    for k, (i, im) in enumerate(dibujos):
        x, y = SEP + (k % COLUMNAS) * (ancho + SEP), SEP + (k // COLUMNAS) * (alto + SEP)
        hoja.paste(im, (x, y))
        lapiz.text((x + 4, y + im.height + 3), i, fill="black")
    salida.parent.mkdir(parents=True, exist_ok=True)
    hoja.save(salida)
    print(f"{salida} {hoja.size[0]} x {hoja.size[1]} ({len(ids)} dibujos)")
    return 0


def unir(carpeta: Path, salida: Path) -> int:
    from PIL import Image

    archivos = sorted(carpeta.glob("*.png"))
    if not archivos:
        print(f"Sin capturas en {carpeta}.")
        return 1
    imagenes = [Image.open(f).convert("RGB") for f in archivos]
    alto = alto_comun([im.size for im in imagenes])
    imagenes = [im.resize((round(im.width * alto / im.height), alto)) for im in imagenes]
    hoja = Image.new("RGB", (sum(im.width for im in imagenes) + SEP * (len(imagenes) - 1), alto), "white")
    x = 0
    for im in imagenes:
        hoja.paste(im, (x, 0))
        x += im.width + SEP
    salida.parent.mkdir(parents=True, exist_ok=True)
    hoja.save(salida)
    shutil.rmtree(carpeta)
    print(f"{salida} {hoja.size[0]} x {hoja.size[1]} ({len(archivos)} capturas)")
    return 0


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    parser.add_argument("ids", nargs="*")
    parser.add_argument("--unir", type=Path, help="carpeta de capturas a unir (y borrar)")
    parser.add_argument("--salida", type=Path, required=True)
    return parser


def main() -> int:
    args = build_parser().parse_args()
    try:
        import cairosvg  # noqa: F401
        from PIL import Image  # noqa: F401
    except ImportError:
        print(f"Falta el venv del arte: corre esto con {VENV_ARTE} (arte.md §2).")
        return 1
    if args.unir:
        return unir(args.unir, args.salida)
    if not args.ids:
        print("Faltan los ids de los dibujos (o --unir <carpeta>).")
        return 1
    raiz = Path.cwd()
    if not (raiz / "arte" / "arte.py").exists():
        print("Corre desde la raiz del proyecto (donde esta arte/arte.py).")
        return 1
    return hoja_svg(raiz, args.ids, args.salida) if correr_arte(raiz) else 1


if __name__ == "__main__":
    sys.exit(main())
