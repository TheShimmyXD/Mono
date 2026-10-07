"""Arte de Mono (FA, D-28): estilo «arte de chiva».

Cada lugar es una lista de formas con un papel (cielo, muro, techo...); el estilo dice el color
de cada papel. Sale la fuente SVG (`arte/svg/<id>.svg`) y el VectorDrawable de la app
(`app/src/main/res/drawable/arte_<id>.xml`). Lienzo 200 x 140 (10:7).
Íconos (FA.2): lienzo 24 x 24 sin franjas; salen como `arte/svg/ic_<id>.svg` y `ic_<id>.xml`.

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


def rrect(x, y, w, h, r):
    d = (f"M{f(x + r)} {f(y)}H{f(x + w - r)}A{f(r)} {f(r)} 0 0 1 {f(x + w)} {f(y + r)}V{f(y + h - r)}"
         f"A{f(r)} {f(r)} 0 0 1 {f(x + w - r)} {f(y + h)}H{f(x + r)}A{f(r)} {f(r)} 0 0 1 {f(x)} {f(y + h - r)}"
         f"V{f(y + r)}A{f(r)} {f(r)} 0 0 1 {f(x + r)} {f(y)}Z")
    return d, (x, y, x + w, y + h)


def raw(d):
    """Trazado escrito a mano (curvas que no son de las formas de arriba)."""
    return d, None


def star(cx, cy, r, ri):
    pts = []
    for i in range(10):
        a = math.radians(-90 + i * 36)
        rr = r if i % 2 == 0 else ri
        pts.append((cx + math.cos(a) * rr, cy + math.sin(a) * rr))
    return poly(pts)


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


# ---------- íconos (FA.2): lienzo 24 x 24 ----------
IW = 24


def casa():
    return [("wall", rect(5, 11, 14, 10)), ("roof", poly([(2.5, 12), (12, 3.5), (21.5, 12)])),
            ("door", arch(10, 14, 4, 7)), ("window", rect(15, 14, 2.5, 2.5))]


def hotel():
    s = [("roof", rect(4, 5.5, 16, 15.5)), ("ochre", rect(3, 3, 18, 3))]
    for y in (8.5, 12):
        s += [("ochre", rect(x, y, 2.4, 2.4)) for x in (6.3, 10.8, 15.3)]
    return s + [("door", arch(10, 15.5, 4, 5.5))]


PIPS = {1: [(12, 12)], 2: [(7.5, 16.5), (16.5, 7.5)], 3: [(7.5, 16.5), (12, 12), (16.5, 7.5)],
        4: [(7.5, 7.5), (16.5, 7.5), (7.5, 16.5), (16.5, 16.5)],
        5: [(7.5, 7.5), (16.5, 7.5), (12, 12), (7.5, 16.5), (16.5, 16.5)],
        6: [(7.5, 7), (16.5, 7), (7.5, 12), (16.5, 12), (7.5, 17), (16.5, 17)]}


def dado(n):
    s = [("wall", rrect(2.5, 2.5, 19, 19, 4))]
    if n == 1:
        return s + [("roof", circle(12, 12, 3))]
    return s + [("pip", circle(x, y, 1.5)) for x, y in PIPS[n]]


def salida():
    s = [("trunk", rect(4, 3, 2, 19)), ("wall", rect(6, 4, 14, 9))]
    for i in range(4):
        for j in range(3):
            if (i + j) % 2 == 0:
                s.append(("pip", rect(6 + i * 3.5, 4 + j * 3, 3.5, 3)))
    return s + [("near", poly([(1.5, 22), (3, 20), (7, 20), (8.5, 22)]))]


def carcel():
    return [("stone", rect(3, 3, 18, 18)), ("door", rect(6, 6, 12, 15)),
            ("bar", lines([(9, 6, 9, 21), (12, 6, 12, 21), (15, 6, 15, 21), (6, 11, 18, 11)]))]


def vayase_carcel():
    return [("roof", rect(10.5, 3, 3, 2.5)),
            ("wall", poly([(5.5, 11), (8.5, 5.5), (15.5, 5.5), (18.5, 11)])),
            ("window", poly([(8, 10.5), (9.6, 7), (11.4, 7), (11.4, 10.5)])),
            ("window", poly([(12.6, 10.5), (12.6, 7), (14.4, 7), (16, 10.5)])),
            ("leaf", rrect(1.5, 10.5, 21, 7, 2)), ("wall", rect(1.5, 13, 21, 1.8)),
            ("tire", circle(6.5, 18, 2.6)), ("tire", circle(17.5, 18, 2.6)),
            ("stone", circle(6.5, 18, 0.9)), ("stone", circle(17.5, 18, 0.9))]


def estacion():
    return [("door", poly([(5, 9), (5, 5.5), (4, 3.5), (10, 3.5), (9, 5.5), (9, 9)])),
            ("roof", rect(3, 9, 12, 7.5)), ("ochre", rect(14, 5.5, 7.5, 11)), ("window", rect(16, 7.5, 3.5, 3.5)),
            ("door", rect(13, 4, 9.5, 2)), ("ochre", poly([(3, 16.5), (0.8, 20), (3, 20)])),
            ("tire", rect(2.5, 16.5, 20, 1.8)),
            ("tire", circle(7, 19.5, 2.2)), ("tire", circle(12.5, 19.5, 2.2)), ("tire", circle(18.5, 19.2, 2.6)),
            ("stone", circle(7, 19.5, 0.8)), ("stone", circle(12.5, 19.5, 0.8)), ("stone", circle(18.5, 19.2, 0.9))]


def energia():
    return [("bolt", poly([(14.5, 1.5), (4.5, 13.5), (11, 13.5), (8.5, 22.5), (19.5, 9.5), (13, 9.5), (16, 1.5)]))]


def acueducto():
    return [("sea", raw("M12 2Q19 10.5 18.5 15A6.5 6.5 0 0 1 5.5 15Q5 10.5 12 2Z")),
            ("shine", raw("M8.6 14.2Q8.4 17 10.6 18.6"))]


def impuesto():
    return [("ochre", raw("M8.2 9Q3.2 13 4 17.8Q4.8 21.5 12 21.5Q19.2 21.5 20 17.8Q20.8 13 15.8 9Z")),
            ("ochre", poly([(9, 8), (7.5, 3), (10.3, 4.3), (12, 2.3), (13.7, 4.3), (16.5, 3), (15, 8)])),
            ("roof", rrect(8, 7.3, 8, 2.4, 1)),
            ("dollar", raw("M14.6 12.6Q14 11.4 12 11.4Q9.5 11.4 9.5 13.3Q9.5 15.1 12 15.1Q14.5 15.1 14.5 16.9"
                           "Q14.5 18.8 12 18.8Q9.9 18.8 9.3 17.6M12 10V20.2"))]


def casualidad():
    return [("ochre", rrect(7, 4, 14.5, 18.5, 2.2)), ("window", rrect(2.5, 1.5, 14.5, 18.5, 2.2)),
            ("glyph", raw("M6.6 7.8Q6.6 4.8 9.75 4.8Q12.9 4.8 12.9 7.5Q12.9 9.4 10.9 10.4Q9.75 11 9.75 13")),
            ("wall", circle(9.75, 16.3, 1.4))]


def arca():
    return [("trunk", raw("M2.5 12V9.5Q2.5 5 7 5H17Q21.5 5 21.5 9.5V12Z")), ("trunk", rect(2.5, 12, 19, 8.5)),
            ("ochre", rect(5.5, 5, 2.5, 15.5)), ("ochre", rect(16, 5, 2.5, 15.5)),
            ("bolt", rect(10.3, 10.5, 3.4, 4.2)), ("pip", circle(12, 12.6, 0.7))]


def loteria():
    return [("ochre", rect(1.5, 6, 21, 12)), ("roof", star(8.5, 12, 4.5, 1.9)),
            ("dash", lines([(16, 7.3, 16, 8.7), (16, 10.3, 16, 11.7), (16, 13.3, 16, 14.7), (16, 16.3, 16, 17.2)])),
            ("trim", circle(19.2, 12, 1.6))]


def sorpresa():
    return [("trim", rect(4, 11.5, 16, 10)), ("trim", rect(3, 8, 18, 3.5)),
            ("ochre", rect(10.5, 8, 3, 13.5)),
            ("ochre", poly([(12, 8), (7.5, 3), (5, 5), (7, 8)])), ("ochre", poly([(12, 8), (16.5, 3), (19, 5), (17, 8)]))]


def parada_libre():
    return [("tire", rect(11, 14, 2, 8.5)), ("window", rrect(4, 1.5, 16, 14, 2.5)),
            ("glyph", raw("M9.5 12.5V4.8H13Q15.6 4.8 15.6 7.15Q15.6 9.5 13 9.5H9.5"))]


def mirador():
    return [("tire", rect(4.5, 4.5, 5, 4)), ("tire", rect(14.5, 4.5, 5, 4)),
            ("door", rrect(3, 7.5, 8, 13, 3)), ("door", rrect(13, 7.5, 8, 13, 3)), ("door", rect(10, 10, 4, 4.5)),
            ("window", circle(7, 16.5, 2.6)), ("window", circle(17, 16.5, 2.6))]


def hamaca():
    return [("trunk", rect(1.5, 4, 2.2, 18)), ("trunk", rect(20.3, 4, 2.2, 18)),
            ("rope", lines([(3.7, 7, 6, 10.5), (20.3, 7, 18, 10.5)])),
            ("trim", raw("M5.5 10Q12 21 18.5 10Q12 15.5 5.5 10Z")),
            ("near", rect(0.5, 21, 23, 2))]


def turno():
    return [("roof", poly([(4, 3.5), (20.5, 12), (4, 20.5), (8.5, 12)]))]


ICONOS = {"casa": casa, "hotel": hotel, **{f"dado_{n}": (lambda n=n: dado(n)) for n in range(1, 7)},
          "salida": salida, "carcel": carcel, "vayase_carcel": vayase_carcel, "estacion": estacion,
          "energia": energia, "acueducto": acueducto, "impuesto": impuesto, "casualidad": casualidad,
          "arca": arca, "loteria": loteria, "sorpresa": sorpresa, "parada_libre": parada_libre,
          "mirador": mirador, "hamaca": hamaca, "turno": turno}
ICONO_ANCHO = 1.1

# ---------- estilo C: arte de chiva ----------
CONTORNO, ANCHO = "#1B1B1B", 1.5
PALETA = {
    "sky": "#38C6F4", "sun": "#FFC21A", "sunray": "#FFC21A", "cloud": "#FFFFFF", "far": "#1E9E8C",
    "near": "#3DB54A", "ground": "#FFB238", "wall": "#FFFFFF", "wshade": "#CFE0FF", "roof": "#E63946",
    "rshade": "#B3202C", "door": "#5B2A86", "window": "#1D7BEF", "trim": "#E5007E", "ochre": "#FFD60A",
    "oshade": "#F4A700", "stone": "#F7D9A8", "sshade": "#E7B97A", "brick": "#E4572E", "bshade": "#B83E1C",
    "sea": "#1D7BEF", "trunk": "#8B5A2B", "leaf": "#2BB04A", "paint": "#E5007E", "slate": "#7B61D9",
    "pip": "#1B1B1B", "tire": "#1B1B1B", "bolt": "#FFC21A",
}
LINEAS = {"gline": ("#D97C0B", 1.0), "wave": ("#FFFFFF", 1.3), "ink": ("#1B1B1B", 1.4),
          "bar": ("#1B1B1B", 1.5), "dollar": ("#1B1B1B", 1.4), "glyph": ("#FFFFFF", 2.2),
          "dash": ("#1B1B1B", 0.9), "rope": ("#8B5A2B", 1.0), "shine": ("#FFFFFF", 1.3)}
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


def trazos(escena, ancho_contorno=ANCHO, con_franjas=True):
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


def salidas():
    for nombre, fn in LUGARES.items():
        t = trazos(fn())
        yield SVG_DIR / f"{nombre}.svg", svg(t)
        yield VD_DIR / f"arte_{nombre}.xml", vector_drawable(t)
    for nombre, fn in ICONOS.items():
        t = trazos(fn(), ICONO_ANCHO, con_franjas=False)
        yield SVG_DIR / f"ic_{nombre}.svg", svg(t, IW, IW, 20)
        yield VD_DIR / f"ic_{nombre}.xml", vector_drawable(t, IW, IW)


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
    print(f"arte: {len(LUGARES)} lugares y {len(ICONOS)} íconos -> {(len(LUGARES) + len(ICONOS)) * 2} archivos"
          f" ({len(viejos)} cambiados)")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
