"""Logos (FB.5): ícono adaptativo de 108 x 108 en dos capas, fondo y frente."""
import re

from .formas import f, rect
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


def mono(k):
    """El personaje del mono (`pj_mono`) escalado por k y centrado en el lienzo (su caja: x 3-45, y 4,5-39)."""
    return escalar(PERSONAJES["mono"](), k, 54 - 24 * k, 54 - 21.75 * k)


# En los logos el amarillo es `bolt`: con `sun`, `trazos` le pone rayos a la forma.
def logo_mono_solo():
    """El mono solo sobre amarillo liso."""
    return [("bolt", rect(0, 0, LW, LW))], mono(1.45)


# El de la app (FI.1, D-94, opción A). Las otras dos con el mono (sobre los rombos, en su medallón) y la
# chiva de FB.5 (D-38) están en `1afc61c`; las de FB.5 (mono en el sol, dado), en `00e8e3e`.
LOGOS = {"mono_solo": logo_mono_solo}
LOGO_APP = "mono_solo"
