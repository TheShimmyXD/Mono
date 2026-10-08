"""Arte de Mono (FA, D-28): estilo «arte de chiva».

Cada lugar es una lista de formas con un papel (cielo, muro, techo...); el estilo dice el color
de cada papel. Sale la fuente SVG (`arte/svg/<id>.svg`) y el VectorDrawable de la app
(`app/src/main/res/drawable/arte_<id>.xml`). Lienzo 200 x 140 (10:7).
Íconos (FA.2): lienzo 24 x 24 sin franjas; salen como `arte/svg/ic_<id>.svg` y `ic_<id>.xml`.
Personajes (FB.3): lienzo 48 x 48 sin fondo; salen como `arte/svg/pj_<id>.svg` y `pj_<id>.xml`.
Logos (FB.5): lienzo 108 x 108 del ícono adaptativo, en dos capas: fondo (llena todo) y frente (cabe en
el círculo central de 66); salen como `logo_<id>_fondo.xml` y `logo_<id>_frente.xml`, y el SVG con las dos.
El de la app (`LOGO_APP`) sale además como ícono adaptativo, `res/mipmap-anydpi-v26/ic_launcher.xml`.
El mapa lugar -> arte de la app (`board/ArteLugares.kt`) también sale de aquí (FA.3), y la lista de
personajes (`board/Personajes.kt`, FB.4).

Los dibujos están en `arte/dibujos/` (formas, lugares de Tío Rico y del Clásico, íconos,
personajes y logos; FE.2c); aquí quedan el estilo y la salida.

Uso, desde la raíz del proyecto: `python3 arte/arte.py` (escribe todo) o `--revisar` (solo
comprueba que lo escrito está al día; sale 1 si no).
"""
import math
import sys
from pathlib import Path
from xml.sax.saxutils import quoteattr

from dibujos.formas import f, franjas, H, poly, W
from dibujos.iconos import ICONO_ANCHO, ICONOS, IW
from dibujos.logos import LOGO_ANCHO, LOGO_APP, LOGO_LINEAS, LOGOS, LW
from dibujos.personajes import PERSONAJES, PJ_ANCHO, PW
from dibujos.registro import LUGARES

RAIZ = Path(__file__).resolve().parent.parent
SVG_DIR = RAIZ / "arte" / "svg"
VD_DIR = RAIZ / "app" / "src" / "main" / "res" / "drawable"
KT_ARTE = RAIZ / "app" / "src" / "main" / "kotlin" / "com" / "jacck" / "mono" / "board" / "ArteLugares.kt"
KT_PJ = KT_ARTE.with_name("Personajes.kt")
MIPMAP = VD_DIR.parent / "mipmap-anydpi-v26" / "ic_launcher.xml"

# ---------- estilo C: arte de chiva ----------
CONTORNO, ANCHO = "#1B1B1B", 1.5
PALETA = {
    "sky": "#38C6F4", "sun": "#FFC21A", "sunray": "#FFC21A", "cloud": "#FFFFFF", "far": "#1E9E8C",
    "near": "#3DB54A", "ground": "#FFB238", "wall": "#FFFFFF", "wshade": "#CFE0FF", "roof": "#E63946",
    "rshade": "#B3202C", "door": "#5B2A86", "window": "#1D7BEF", "trim": "#E5007E", "ochre": "#FFD60A",
    "oshade": "#F4A700", "stone": "#F7D9A8", "sshade": "#E7B97A", "brick": "#E4572E", "bshade": "#B83E1C",
    "sea": "#1D7BEF", "trunk": "#8B5A2B", "leaf": "#2BB04A", "paint": "#E5007E", "slate": "#7B61D9",
    "pip": "#1B1B1B", "tire": "#1B1B1B", "bolt": "#FFC21A",
    "coal": "#3A3340", "clay": "#D9622B", "cshade": "#A8441B", "lake": "#155FA8", "frailejon": "#BFE08F",
    "fstem": "#7A6A55", "moor": "#C7C24E", "sand": "#EDB35C", "sandshade": "#C98536", "moss": "#C9D3C2",
    "canyon": "#D99A5B", "canyon2": "#B5673A", "coffee": "#1E8A3C", "palm": "#E4DCC8", "bamboo": "#9BC53D",
    "wood": "#D49A5A", "granite": "#C3C6D6", "gshade": "#9599B2", "dolphin": "#F497B6", "turq": "#2EC4B6", "shallow": "#8EE8D8",
    "night": "#2B2466", "skin": "#F2B98A",
}
LINEAS = {"gline": ("#D97C0B", 1.0), "wave": ("#FFFFFF", 1.3), "ink": ("#1B1B1B", 1.4),
          "bar": ("#1B1B1B", 1.5), "dollar": ("#1B1B1B", 1.4), "glyph": ("#FFFFFF", 2.2),
          "dash": ("#1B1B1B", 0.9), "rope": ("#8B5A2B", 1.0), "shine": ("#FFFFFF", 1.3),
          "fall": ("#7FD3F0", 1.8),
          "stamp": ("#E63946", 2.0), "bigglyph": ("#FFFFFF", 4.2), "qblack": ("#1B1B1B", 10), "qyellow": ("#FFC21A", 6)}


def rayos(cx, cy, r):
    out = []
    for i in range(12):
        a = math.radians(i * 30)
        pts = [(cx + math.cos(a + s) * (r + e), cy + math.sin(a + s) * (r + e))
               for s, e in ((-0.13, 1.5), (0, 7), (0.13, 1.5))]
        out.append(("sunray", poly(pts)))
    return out


def trazos(escena, ancho_contorno=ANCHO, con_franjas=True, ancho_lineas=None):
    """(relleno, contorno, ancho, d) en orden de dibujo; el sol lleva rayos detrás.

    `ancho_lineas`, si se da, reemplaza el grosor de los papeles de `LINEAS` (logos, a otra escala)."""
    for papel, (d, caja) in list(escena):
        if papel == "sun":
            x0, y0, x1, _ = caja
            escena = [escena[0]] + rayos((x0 + x1) / 2, (y0 + caja[3]) / 2, (x1 - x0) / 2) + escena[1:]
            break
    out = []
    for papel, (d, _) in escena:
        if papel in LINEAS:
            color, ancho = LINEAS[papel]
            out.append((None, color, ancho_lineas or ancho, d))
        else:
            out.append((PALETA[papel], CONTORNO, ancho_contorno, d))
    return out + franjas() if con_franjas else out


def svg(t, w=W, h=H, escala=4):
    cuerpo = "".join(
        f'<path d="{d}" fill="{r or "none"}"'
        + (f' stroke="{c}" stroke-width="{f(a)}" stroke-linecap="round" stroke-linejoin="round"' if c else "")
        + "/>" for r, c, a, d in t)
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w * escala}" height="{h * escala}">'
            f"{cuerpo}</svg>\n")


def vector_drawable(t, w=W, h=H):
    filas = []
    for r, c, a, d in t:
        attrs = [f"android:pathData={quoteattr(d)}"]
        if r:
            attrs.append(f'android:fillColor="{r}"')
        if c:
            attrs += [f'android:strokeColor="{c}"', f'android:strokeWidth="{f(a)}"',
                      'android:strokeLineCap="round"', 'android:strokeLineJoin="round"']
        filas.append("    <path " + " ".join(attrs) + " />")
    return ('<?xml version="1.0" encoding="utf-8"?>\n'
            "<!-- Generado por arte/arte.py: no editar a mano. -->\n"
            '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
            f'    android:width="{w}dp" android:height="{h}dp"\n'
            f'    android:viewportWidth="{w}" android:viewportHeight="{h}">\n'
            + "\n".join(filas) + "\n</vector>\n")


def kotlin_arte():
    filas = "\n".join(f'    "{n}" to R.drawable.arte_{n},' for n in sorted(LUGARES))
    return ("// Generado por arte/arte.py: no editar a mano.\n"
            "package com.jacck.mono.board\n\n"
            "import com.jacck.mono.R\n\n"
            "/** Arte de cada lugar por su clave (`artKey` del nombre), FA (D-28). */\n"
            "internal val ArteLugares: Map<String, Int> = mapOf(\n"
            f"{filas}\n)\n")


def kotlin_personajes():
    filas = "\n".join(f'    "{n}" to R.drawable.pj_{n},' for n in PERSONAJES)
    return ("// Generado por arte/arte.py: no editar a mano.\n"
            "package com.jacck.mono.board\n\n"
            "import com.jacck.mono.R\n\n"
            "/** Los personajes en orden (FB.3, D-35): id que se guarda con la partida -> dibujo. */\n"
            "internal val Personajes: List<Pair<String, Int>> = listOf(\n"
            f"{filas}\n)\n")


def icono_app():
    return ('<?xml version="1.0" encoding="utf-8"?>\n'
            "<!-- Generado por arte/arte.py: no editar a mano. -->\n"
            '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
            f'    <background android:drawable="@drawable/logo_{LOGO_APP}_fondo" />\n'
            f'    <foreground android:drawable="@drawable/logo_{LOGO_APP}_frente" />\n'
            "</adaptive-icon>\n")


def salidas():
    yield KT_ARTE, kotlin_arte()
    yield MIPMAP, icono_app()
    yield KT_PJ, kotlin_personajes()
    for nombre, fn in LUGARES.items():
        t = trazos(fn())
        yield SVG_DIR / f"{nombre}.svg", svg(t)
        yield VD_DIR / f"arte_{nombre}.xml", vector_drawable(t)
    for nombre, fn in ICONOS.items():
        t = trazos(fn(), ICONO_ANCHO, con_franjas=False)
        yield SVG_DIR / f"ic_{nombre}.svg", svg(t, IW, IW, 20)
        yield VD_DIR / f"ic_{nombre}.xml", vector_drawable(t, IW, IW)
    for nombre, fn in PERSONAJES.items():
        t = trazos(fn(), PJ_ANCHO, con_franjas=False)
        yield SVG_DIR / f"pj_{nombre}.svg", svg(t, PW, PW, 10)
        yield VD_DIR / f"pj_{nombre}.xml", vector_drawable(t, PW, PW)
    for nombre, fn in LOGOS.items():
        fondo, frente = fn()
        tf = trazos(fondo, LOGO_ANCHO, con_franjas=False)
        tt = trazos(frente, LOGO_ANCHO, con_franjas=False, ancho_lineas=LOGO_LINEAS)
        yield SVG_DIR / f"logo_{nombre}.svg", svg(tf + tt, LW, LW, 4)
        yield VD_DIR / f"logo_{nombre}_fondo.xml", vector_drawable(tf, LW, LW)
        yield VD_DIR / f"logo_{nombre}_frente.xml", vector_drawable(tt, LW, LW)


def main(argv):
    viejos = [p for p, txt in salidas() if not p.exists() or p.read_text() != txt]
    if "--revisar" in argv:
        for p in viejos:
            print("desactualizado:", p.relative_to(RAIZ))
        print("arte: al día" if not viejos else f"arte: {len(viejos)} archivos por regenerar")
        return 1 if viejos else 0
    for p, txt in salidas():
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(txt)
    n = len(LUGARES) + len(ICONOS) + len(PERSONAJES)
    print(f"arte: {len(LUGARES)} lugares, {len(ICONOS)} íconos, {len(PERSONAJES)} personajes y {len(LOGOS)} logos"
          f" -> {n * 2 + len(LOGOS) * 3 + 3} archivos"
          f" ({len(viejos)} cambiados)")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
