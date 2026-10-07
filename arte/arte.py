"""Arte de Mono (FA, D-28): estilo «arte de chiva».

Cada lugar es una lista de formas con un papel (cielo, muro, techo...); el estilo dice el color
de cada papel. Sale la fuente SVG (`arte/svg/<id>.svg`) y el VectorDrawable de la app
(`app/src/main/res/drawable/arte_<id>.xml`). Lienzo 200 x 140 (10:7).

Uso, desde la raíz del proyecto: `python3 arte/arte.py` (escribe todo) o `--revisar` (solo
comprueba que lo escrito está al día; sale 1 si no).
"""
import math
import sys
from pathlib import Path
from xml.sax.saxutils import quoteattr

W, H = 200, 140
RAIZ = Path(__file__).resolve().parent.parent
SVG_DIR = RAIZ / "arte" / "svg"
VD_DIR = RAIZ / "app" / "src" / "main" / "res" / "drawable"


# ---------- formas ----------
def f(n):
    return f"{n:.2f}".rstrip("0").rstrip(".")


def rect(x, y, w, h):
    return f"M{f(x)} {f(y)}H{f(x + w)}V{f(y + h)}H{f(x)}Z", (x, y, x + w, y + h)


def poly(pts):
    d = "M" + "L".join(f"{f(x)} {f(y)}" for x, y in pts) + "Z"
    xs, ys = [p[0] for p in pts], [p[1] for p in pts]
    return d, (min(xs), min(ys), max(xs), max(ys))


def circle(cx, cy, r):
    d = f"M{f(cx - r)} {f(cy)}A{f(r)} {f(r)} 0 1 1 {f(cx + r)} {f(cy)}A{f(r)} {f(r)} 0 1 1 {f(cx - r)} {f(cy)}Z"
    return d, (cx - r, cy - r, cx + r, cy + r)


def arch(x, y, w, h):
    r = w / 2
    d = f"M{f(x)} {f(y + h)}V{f(y + r)}A{f(r)} {f(r)} 0 0 1 {f(x + w)} {f(y + r)}V{f(y + h)}Z"
    return d, (x, y, x + w, y + h)


def gothic(x, y, w, h):
    m = y + h * 0.45
    d = (f"M{f(x)} {f(y + h)}V{f(m)}Q{f(x)} {f(y + h * 0.1)} {f(x + w / 2)} {f(y)}"
         f"Q{f(x + w)} {f(y + h * 0.1)} {f(x + w)} {f(m)}V{f(y + h)}Z")
    return d, (x, y, x + w, y + h)


def lines(segs):
    d = "".join(f"M{f(a)} {f(b)}L{f(c)} {f(e)}" for a, b, c, e in segs)
    return d, None


def polyline(pts):
    return "M" + "L".join(f"{f(x)} {f(y)}" for x, y in pts), None


# ---------- lugares ----------
def villa_de_leyva():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(162, 30, 13)),
         ("far", poly([(0, 78), (22, 58), (40, 66), (70, 40), (98, 60), (126, 46), (150, 58), (176, 44), (200, 60), (200, 96), (0, 96)])),
         ("near", poly([(0, 88), (40, 74), (80, 84), (120, 72), (160, 82), (200, 74), (200, 100), (0, 100)])),
         ("ground", rect(0, 98, W, 42))]
    vp = (100, 92)
    segs = []
    for xb in (-80, -20, 40, 100, 160, 220, 280):
        t = (98 - vp[1]) / (140 - vp[1])
        segs.append((vp[0] + (xb - vp[0]) * t, 98, xb, 140))
    for y in (103, 110, 120, 134):
        segs.append((0, y, W, y))
    s.append(("gline", lines(segs)))
    # casas izquierda
    s += [("wall", rect(4, 74, 62, 26)), ("wshade", rect(4, 76, 62, 3)),
          ("roof", poly([(0, 76), (10, 66), (60, 66), (70, 76)])),
          ("window", rect(10, 82, 9, 9)), ("window", rect(28, 82, 9, 9)), ("door", arch(47, 84, 10, 16)),
          ("trim", rect(8, 91, 13, 2.4)), ("trim", rect(26, 91, 13, 2.4))]
    # casas derecha
    s += [("wall", rect(134, 76, 62, 24)), ("wshade", rect(134, 78, 62, 3)),
          ("roof", poly([(130, 78), (140, 68), (192, 68), (200, 78)])),
          ("window", rect(142, 84, 9, 9)), ("window", rect(178, 84, 9, 9)), ("door", arch(160, 86, 10, 14)),
          ("trim", rect(140, 93, 13, 2.4)), ("trim", rect(176, 93, 13, 2.4))]
    # iglesia
    s += [("wall", poly([(92, 46), (92, 30), (100, 23), (108, 30), (108, 46)])),
          ("door", arch(96.5, 31, 7, 9)), ("ochre", circle(100, 37.5, 2)),
          ("wall", rect(76, 58, 48, 42)), ("wshade", rect(112, 58, 12, 42)),
          ("wall", poly([(72, 60), (100, 43), (128, 60)])), ("wshade", rect(72, 59, 56, 2.6)),
          ("window", circle(100, 52, 3.2)), ("window", arch(82, 68, 6, 10)), ("window", arch(113, 68, 6, 10)),
          ("door", arch(92, 80, 16, 20)), ("wshade", rect(86, 99, 28, 2)),
          ("ink", lines([(100, 15.5, 100, 23), (97, 18.5, 103, 18.5)]))]
    return s


def cartagena():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(34, 28, 12)),
         ("cloud", poly([(140, 30), (146, 22), (156, 22), (162, 16), (174, 18), (180, 26), (188, 30)])),
         ("sea", rect(0, 78, W, 20)),
         ("wave", lines([(6, 84, 22, 84), (40, 90, 58, 90), (150, 86, 168, 86), (178, 92, 194, 92), (10, 93, 24, 93)])),
         # casas de balcón detrás de la muralla
         ("paint", rect(136, 62, 30, 34)), ("wall", rect(166, 68, 30, 28)),
         ("roof", poly([(133, 64), (138, 57), (164, 57), (169, 64)])),
         ("roof", poly([(163, 70), (168, 63), (193, 63), (199, 70)])),
         ("trim", rect(140, 74, 22, 3)), ("window", rect(143, 66, 6, 7)), ("window", rect(153, 66, 6, 7)),
         ("trim", rect(170, 80, 22, 3)), ("window", rect(173, 72, 6, 7)), ("window", rect(183, 72, 6, 7)),
         # palmera
         ("trunk", poly([(28, 100), (33, 100), (40, 58), (37, 57)])),
         ("leaf", poly([(38, 57), (20, 52), (8, 60), (24, 56)])),
         ("leaf", poly([(38, 57), (30, 42), (16, 40), (28, 47)])),
         ("leaf", poly([(38, 57), (48, 42), (62, 42), (50, 48)])),
         ("leaf", poly([(38, 57), (56, 54), (66, 64), (52, 59)])),
         # Torre del Reloj
         ("ochre", rect(68, 60, 64, 40)), ("oshade", rect(120, 60, 12, 40)),
         ("ochre", rect(64, 56, 72, 5)),
         ("door", arch(91, 72, 18, 28)), ("door", arch(74, 80, 11, 20)), ("door", arch(115, 80, 11, 20)),
         ("ochre", rect(85, 32, 30, 25)), ("oshade", rect(107, 32, 8, 25)),
         ("wall", circle(100, 44, 7.5)), ("ink", lines([(100, 44, 100, 39), (100, 44, 103.5, 45.5)])),
         ("ochre", rect(89, 21, 22, 12)), ("door", arch(92.5, 23, 6, 9)), ("door", arch(101.5, 23, 6, 9)),
         ("roof", poly([(86, 22), (100, 6), (114, 22)])),
         # muralla
         ("stone", rect(0, 96, W, 44)), ("sshade", rect(0, 128, W, 12)),
         ("gline", lines([(0, 108, W, 108), (0, 118, W, 118)]))]
    for x in range(0, 200, 16):
        s.append(("stone", rect(x + 2, 91, 9, 6)))
    s += [("stone", rect(168, 78, 14, 19)), ("stone", arch(167, 70, 16, 12)), ("door", rect(173.5, 84, 3, 7))]
    return s


def chapinero():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(170, 22, 10)),
         ("cloud", poly([(18, 30), (24, 23), (34, 23), (40, 17), (52, 19), (58, 26), (66, 30)])),
         ("near", poly([(0, 70), (30, 50), (60, 58), (95, 30), (130, 48), (165, 36), (200, 52), (200, 100), (0, 100)])),
         # edificios de ladrillo
         ("brick", rect(4, 70, 44, 32)), ("bshade", rect(4, 70, 44, 3)),
         ("window", rect(10, 77, 7, 9)), ("window", rect(22, 77, 7, 9)), ("window", rect(34, 77, 7, 9)),
         ("window", rect(10, 90, 7, 9)), ("window", rect(34, 90, 7, 9)),
         ("brick", rect(152, 66, 44, 36)), ("bshade", rect(152, 66, 44, 3)),
         ("window", rect(158, 73, 7, 9)), ("window", rect(170, 73, 7, 9)), ("window", rect(182, 73, 7, 9)),
         ("window", rect(158, 87, 7, 9)), ("window", rect(182, 87, 7, 9)),
         # iglesia de Lourdes
         ("stone", poly([(63, 66), (69, 46), (75, 66)])), ("stone", poly([(125, 66), (131, 46), (137, 66)])),
         ("stone", rect(64, 64, 72, 38)), ("sshade", rect(124, 64, 12, 38)),
         ("stone", poly([(77, 66), (100, 46), (123, 66)])),
         ("slate", poly([(88, 37), (100, 4), (112, 37)])),
         ("stone", rect(90, 36, 20, 30)), ("sshade", rect(104, 36, 6, 30)),
         ("door", gothic(95.5, 40, 9, 15)), ("window", circle(100, 61, 4.6)),
         ("door", gothic(92.5, 78, 15, 24)), ("door", gothic(71, 84, 11, 18)), ("door", gothic(118, 84, 11, 18)),
         ("ink", lines([(100, 0.5, 100, 6), (97.5, 2.5, 102.5, 2.5)])),
         # plaza y árboles
         ("ground", rect(0, 100, W, 40)),
         ("gline", lines([(0, 108, W, 108), (0, 120, W, 120), (40, 100, 20, 140), (100, 100, 100, 140), (160, 100, 180, 140)])),
         ("trunk", rect(36, 94, 3, 14)), ("leaf", circle(37.5, 88, 10)),
         ("trunk", rect(161, 94, 3, 14)), ("leaf", circle(162.5, 88, 10))]
    return s


LUGARES = {"villa_de_leyva": villa_de_leyva, "cartagena": cartagena, "chapinero": chapinero}

# ---------- estilo C: arte de chiva ----------
CONTORNO, ANCHO = "#1B1B1B", 1.5
PALETA = {
    "sky": "#38C6F4", "sun": "#FFC21A", "sunray": "#FFC21A", "cloud": "#FFFFFF", "far": "#1E9E8C",
    "near": "#3DB54A", "ground": "#FFB238", "wall": "#FFFFFF", "wshade": "#CFE0FF", "roof": "#E63946",
    "rshade": "#B3202C", "door": "#5B2A86", "window": "#1D7BEF", "trim": "#E5007E", "ochre": "#FFD60A",
    "oshade": "#F4A700", "stone": "#F7D9A8", "sshade": "#E7B97A", "brick": "#E4572E", "bshade": "#B83E1C",
    "sea": "#1D7BEF", "trunk": "#8B5A2B", "leaf": "#2BB04A", "paint": "#E5007E", "slate": "#7B61D9",
}
LINEAS = {"gline": ("#D97C0B", 1.0), "wave": ("#FFFFFF", 1.3), "ink": ("#1B1B1B", 1.4)}
FRANJA = ["#E63946", "#FFC21A", "#1D7BEF", "#2BB04A", "#E5007E"]


def rayos(cx, cy, r):
    out = []
    for i in range(12):
        a = math.radians(i * 30)
        pts = [(cx + math.cos(a + s) * (r + e), cy + math.sin(a + s) * (r + e))
               for s, e in ((-0.13, 1.5), (0, 7), (0.13, 1.5))]
        out.append(("sunray", poly(pts)))
    return out


def franjas():
    """Bandas de chiva arriba y abajo: dientes de colores sobre negro."""
    out = []
    for y0, arriba in ((0, False), (H - 9, True)):
        out.append(("#1B1B1B", None, 0, rect(0, y0, W, 9)[0]))
        for i, x in enumerate(range(0, W, 10)):
            pts = ([(x, y0 + 9), (x + 5, y0 + 1.5), (x + 10, y0 + 9)] if arriba
                   else [(x, y0), (x + 5, y0 + 7.5), (x + 10, y0)])
            out.append((FRANJA[i % len(FRANJA)], None, 0, poly(pts)[0]))
    return out


def trazos(escena):
    """(relleno, contorno, ancho, d) en orden de dibujo; el sol lleva rayos detrás."""
    for papel, (d, caja) in list(escena):
        if papel == "sun":
            x0, y0, x1, _ = caja
            escena = [escena[0]] + rayos((x0 + x1) / 2, (y0 + caja[3]) / 2, (x1 - x0) / 2) + escena[1:]
            break
    out = []
    for papel, (d, _) in escena:
        if papel in LINEAS:
            color, ancho = LINEAS[papel]
            out.append((None, color, ancho, d))
        else:
            out.append((PALETA[papel], CONTORNO, ANCHO, d))
    return out + franjas()


def svg(t):
    cuerpo = "".join(
        f'<path d="{d}" fill="{r or "none"}"'
        + (f' stroke="{c}" stroke-width="{f(a)}" stroke-linecap="round" stroke-linejoin="round"' if c else "")
        + "/>" for r, c, a, d in t)
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W * 4}" height="{H * 4}">'
            f"{cuerpo}</svg>\n")


def vector_drawable(t):
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
            f'    android:width="{W}dp" android:height="{H}dp"\n'
            f'    android:viewportWidth="{W}" android:viewportHeight="{H}">\n'
            + "\n".join(filas) + "\n</vector>\n")


def salidas():
    for nombre, fn in LUGARES.items():
        t = trazos(fn())
        yield SVG_DIR / f"{nombre}.svg", svg(t)
        yield VD_DIR / f"arte_{nombre}.xml", vector_drawable(t)


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
    print(f"arte: {len(LUGARES)} lugares -> {len(LUGARES) * 2} archivos ({len(viejos)} cambiados)")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
