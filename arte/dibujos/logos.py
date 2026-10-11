"""Logos (FB.5): ícono adaptativo de 108 x 108 en dos capas, fondo y frente."""
import re

from .formas import circle, f, poly, rect
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


def rombos():
    """Fondo verde con los rombos amarillos, rojos y blancos de la carrocería de la chiva."""
    fondo = [("leaf", rect(0, 0, LW, LW))]
    for fila, y in enumerate(range(0, LW + 18, 18)):
        for x in range(-9 if fila % 2 else 0, LW + 18, 18):
            fondo.append((("ochre", "roof", "cloud")[(x // 18 + fila) % 3], poly([(x, y - 7), (x + 7, y), (x, y + 7), (x - 7, y)])))
    return fondo


def mono(k):
    """El personaje del mono (`pj_mono`) escalado por k y centrado en el lienzo (su caja: x 3-45, y 4,5-39)."""
    return escalar(PERSONAJES["mono"](), k, 54 - 24 * k, 54 - 21.75 * k)


# En los logos el amarillo es `bolt`: con `sun`, `trazos` le pone rayos a la forma.
def logo_chiva():
    """La chiva de frente sobre los rombos de su carrocería."""
    k = 1.4
    frente = escalar(PERSONAJES["chiva"](), k, 54 - 24 * k, 55 - 24.25 * k)
    return rombos(), frente


def logo_mono_solo():
    """FI.1 A: el mono solo sobre amarillo liso."""
    return [("bolt", rect(0, 0, LW, LW))], mono(1.45)


def logo_mono_rombos():
    """FI.1 B: el mono sobre los rombos de la chiva."""
    return rombos(), mono(1.45)


def logo_mono_medallon():
    """FI.1 C: el mono en un medallón rojo y amarillo sobre verde liso."""
    frente = [("roof", circle(54, 54, 32)), ("bolt", circle(54, 54, 27.5))] + mono(1.25)
    return [("leaf", rect(0, 0, LW, LW))], frente


# El de la app (FB.5, D-38, opción B); las otras dos opciones de FB.5 (mono en el sol, dado) están en `00e8e3e`.
LOGOS = {"chiva": logo_chiva, "mono_solo": logo_mono_solo, "mono_rombos": logo_mono_rombos,
         "mono_medallon": logo_mono_medallon}
LOGO_APP = "chiva"
