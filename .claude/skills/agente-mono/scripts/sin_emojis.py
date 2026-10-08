#!/usr/bin/env python3
"""Busca emojis y símbolos pictográficos en el código y los recursos de la app (K18, M-087).

El autor los prohibió dentro de la app (2026-10-07): lo que haría un emoji lo hace un ícono
dibujado con `arte.py` (`references/arte.md`). Revisa .kt, .kts, .xml y .json bajo las carpetas
dadas; textos, logs y comentarios por igual.

Uso: python3 .claude/skills/agente-mono/scripts/sin_emojis.py app/src/main
Sale 1 con `archivo:línea: símbolos` por cada línea que tenga alguno; 0 si no hay.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

# Emojis (1F000-1FAFF: caras, objetos, banderas, 📶), símbolos varios y dingbats (2600-27BF: ★, ✈),
# flechas y estrellas suplementarias (2B00-2BFF: ⭐), y los selectores que los vuelven emoji.
PICTOGRAFICOS = re.compile("[\U0001F000-\U0001FAFF☀-➿⬀-⯿️‍⃣]")
EXTENSIONES = {".kt", ".kts", ".xml", ".json"}


def buscar(carpetas: list[Path]) -> list[str]:
    """Una línea `archivo:número: símbolos` por cada línea con alguno, en orden de archivo."""
    hallados = []
    for carpeta in carpetas:
        archivos = [carpeta] if carpeta.is_file() else sorted(carpeta.rglob("*"))
        for archivo in archivos:
            if archivo.suffix not in EXTENSIONES or not archivo.is_file():
                continue
            for n, linea in enumerate(archivo.read_text(encoding="utf-8").splitlines(), 1):
                simbolos = PICTOGRAFICOS.findall(linea)
                if simbolos:
                    hallados.append(f"{archivo}:{n}: {''.join(simbolos)}")
    return hallados


def main(argv: list[str]) -> int:
    if not argv:
        print(__doc__.strip().splitlines()[-2])
        return 2
    hallados = buscar([Path(a) for a in argv])
    for h in hallados:
        print(h)
    print(f"{len(hallados)} líneas con emojis" if hallados else "sin emojis")
    return 1 if hallados else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
