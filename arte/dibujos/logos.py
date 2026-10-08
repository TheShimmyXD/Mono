"""Logos (FB.5): ícono adaptativo de 108 x 108 en dos capas, fondo y frente."""
import re

from .formas import f, poly, rect
from .personajes import PERSONAJES


LW = 108
LOGO_ANCHO, LOGO_LINEAS = 2.4, 1.8  # contorno y grosor de las líneas, a escala del lienzo


def escalar(formas, k, dx, dy):
    """Las formas de otro lienzo, escaladas por k y movidas (dx, dy); solo comandos absolutos."""
    out = []
    for papel, (d, caja) in formas:
        toks = re.findall(r"[A-Za-z]|-?\d*\.?\d+", d)
        res, i, cmd, arg = [], 0, None, 0
        while i < len(toks):
            t = toks[i]
            if t.isalpha():
                cmd, arg = t, 0
                res.append(t)
                i += 1
                continue
            v = float(t)
            if cmd == "H":
                v = v * k + dx
            elif cmd == "V":
                v = v * k + dy
            elif cmd == "A":
                j = arg % 7
                v = v * k if j < 2 else v if j < 5 else v * k + (dx if j == 5 else dy)
            else:
                v = v * k + (dx if arg % 2 == 0 else dy)
            res.append(f(v))
            arg += 1
            i += 1
        d2 = re.sub(r" ?([A-Za-z]) ?", r"\1", " ".join(res))
        caja2 = caja and (caja[0] * k + dx, caja[1] * k + dy, caja[2] * k + dx, caja[3] * k + dy)
        out.append((papel, (d2, caja2)))
    return out


# En los logos el amarillo es `bolt`: con `sun`, `trazos` le pone rayos a la forma.
def logo_chiva():
    """B: la chiva de frente sobre los rombos de su carrocería."""
    fondo = [("leaf", rect(0, 0, LW, LW))]
    for fila, y in enumerate(range(0, LW + 18, 18)):
        for x in range(-9 if fila % 2 else 0, LW + 18, 18):
            fondo.append((("ochre", "roof", "cloud")[(x // 18 + fila) % 3], poly([(x, y - 7), (x + 7, y), (x, y + 7), (x - 7, y)])))
    k = 1.4
    frente = escalar(PERSONAJES["chiva"](), k, 54 - 24 * k, 55 - 24.25 * k)
    return fondo, frente


# El de la app (FB.5, D-38, opción B); las otras dos opciones (mono en el sol, dado) están en `00e8e3e`.
LOGOS = {"chiva": logo_chiva}
LOGO_APP = "chiva"
