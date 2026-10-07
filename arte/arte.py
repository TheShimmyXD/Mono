"""Arte de Mono (FA, D-28): estilo «arte de chiva».

Cada lugar es una lista de formas con un papel (cielo, muro, techo...); el estilo dice el color
de cada papel. Sale la fuente SVG (`arte/svg/<id>.svg`) y el VectorDrawable de la app
(`app/src/main/res/drawable/arte_<id>.xml`). Lienzo 200 x 140 (10:7).
Íconos (FA.2): lienzo 24 x 24 sin franjas; salen como `arte/svg/ic_<id>.svg` y `ic_<id>.xml`.
El mapa lugar -> arte de la app (`board/ArteLugares.kt`) también sale de aquí (FA.3).

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
KT_ARTE = RAIZ / "app" / "src" / "main" / "kotlin" / "com" / "jacck" / "mono" / "board" / "ArteLugares.kt"


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


def vasija(cx, base, w, h):
    """Cántaro de barro: boca estrecha, panza ancha, base angosta."""
    y0, nw = base - h, w * 0.36
    d = (f"M{f(cx - nw / 2)} {f(y0)}H{f(cx + nw / 2)}V{f(y0 + h * 0.12)}"
         f"C{f(cx + w * 0.66)} {f(y0 + h * 0.25)} {f(cx + w * 0.6)} {f(base - h * 0.12)} {f(cx + w * 0.2)} {f(base)}"
         f"H{f(cx - w * 0.2)}C{f(cx - w * 0.6)} {f(base - h * 0.12)} {f(cx - w * 0.66)} {f(y0 + h * 0.25)} "
         f"{f(cx - nw / 2)} {f(y0 + h * 0.12)}Z")
    return d, (cx - w / 2, y0, cx + w / 2, base)


def roseta(cx, cy, r, n=9):
    """Roseta de hojas (frailejón): n puntas alrededor de un centro."""
    pts = []
    for i in range(2 * n):
        a = math.radians(-90 + i * 180 / n)
        rr = r if i % 2 == 0 else r * 0.6
        pts.append((cx + math.cos(a) * rr, cy + math.sin(a) * rr * 0.8))
    return poly(pts)


def frailejon(cx, base, h):
    return [("fstem", poly([(cx - 4, base), (cx + 4, base), (cx + 3, base - h), (cx - 3, base - h)])),
            ("ink", lines([(cx, base - h - 6, cx + 1, base - h - 15)])), ("ochre", circle(cx + 1, base - h - 16.5, 2.4)),
            ("frailejon", roseta(cx, base - h, 13))]


def tierra_plaza(s, y=98):
    s.append(("ground", rect(0, y, W, H - y)))
    s.append(("gline", lines([(0, y + 8, W, y + 8), (0, y + 20, W, y + 20),
                              (50, y, 34, H), (100, y, 100, H), (150, y, 166, H)])))


def topaga():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(30, 28, 11)),
         ("far", poly([(0, 66), (30, 44), (58, 58), (96, 34), (132, 52), (166, 38), (200, 54), (200, 96), (0, 96)])),
         ("near", poly([(0, 58), (26, 50), (58, 62), (90, 74), (130, 70), (170, 62), (200, 70), (200, 100), (0, 100)])),
         # bocamina de carbón
         ("coal", arch(14, 66, 26, 34)), ("trunk", rect(11, 64, 5, 36)), ("trunk", rect(38, 64, 5, 36)),
         ("trunk", rect(8, 60, 38, 6))]
    s.append(("ground", rect(0, 98, W, 42)))
    s.append(("gline", lines([(0, 124, W, 124), (120, 98, 110, 140), (170, 98, 180, 140)])))
    s.append(("ink", lines([(0, 117, 92, 117)] + [(x, 114, x, 120) for x in range(6, 92, 10)])))
    # vagoneta
    s += [("coal", circle(50, 98, 5)), ("coal", circle(59, 96, 6)), ("coal", circle(68, 98, 5)),
          ("roof", poly([(42, 99), (76, 99), (72, 112), (46, 112)])), ("rshade", rect(46, 104, 26, 2.5)),
          ("tire", circle(51, 113, 3.6)), ("tire", circle(67, 113, 3.6))]
    # iglesia de San Judas Tadeo, con el diablo de Tópaga en el rosetón
    s += [("wall", rect(126, 34, 16, 66)), ("wshade", rect(136, 34, 6, 66)),
          ("roof", poly([(123, 36), (134, 14), (145, 36)])), ("door", arch(129.5, 40, 7, 11)),
          ("window", arch(130, 62, 6, 10)),
          ("wall", rect(82, 58, 44, 42)), ("wshade", rect(116, 58, 10, 42)),
          ("wall", poly([(78, 60), (104, 42), (130, 60)])), ("wshade", rect(78, 59, 52, 2.6)),
          ("roof", circle(104, 69, 6)),
          ("roof", poly([(99, 66), (97, 58), (101.5, 63.5)])), ("roof", poly([(109, 66), (111, 58), (106.5, 63.5)])),
          ("ink", lines([(101.5, 71, 106.5, 71)])),
          ("door", arch(96, 80, 16, 20)), ("window", arch(86, 74, 6, 10)), ("window", arch(116, 74, 6, 10)),
          ("wshade", rect(90, 99, 28, 2)), ("ink", lines([(134, 7, 134, 14), (131.5, 9.5, 136.5, 9.5)]))]
    # casas
    s += [("wall", rect(150, 74, 48, 26)), ("wshade", rect(150, 76, 48, 3)),
          ("roof", poly([(146, 76), (155, 67), (194, 67), (200, 76)])),
          ("window", rect(156, 82, 9, 9)), ("door", arch(172, 84, 10, 16)), ("window", rect(186, 82, 8, 9)),
          ("trim", rect(154, 91, 13, 2.4))]
    return s


def mongua():
    s = [("sky", rect(0, 0, W, H)),
         ("cloud", poly([(14, 30), (20, 22), (30, 22), (36, 16), (48, 18), (54, 26), (62, 30)])),
         ("cloud", poly([(140, 24), (146, 18), (156, 18), (162, 13), (172, 15), (178, 24)])),
         ("far", poly([(0, 70), (24, 46), (50, 56), (80, 34), (112, 50), (140, 36), (172, 50), (200, 42), (200, 96), (0, 96)])),
         ("moor", poly([(0, 78), (40, 70), (90, 76), (140, 70), (200, 76), (200, 104), (0, 104)])),
         ("lake", poly([(8, 86), (20, 80), (46, 79), (68, 82), (74, 88), (60, 93), (30, 94), (12, 92)])),
         ("wave", lines([(20, 85, 34, 85), (44, 89, 58, 89)]))]
    # templo de piedra con su torre central
    s += [("stone", rect(82, 62, 54, 40)), ("sshade", rect(124, 62, 12, 40)),
          ("stone", poly([(78, 64), (109, 48), (140, 64)])), ("sshade", rect(78, 63, 62, 2.6)),
          ("stone", rect(98, 22, 22, 34)), ("sshade", rect(113, 22, 7, 34)),
          ("stone", rect(95, 20, 28, 4)), ("roof", poly([(97, 20), (109, 6), (121, 20)])),
          ("door", arch(102, 27, 6, 10)), ("door", arch(110, 27, 6, 10)), ("window", circle(109, 46, 3.6)),
          ("door", arch(100, 80, 18, 22)), ("door", arch(87, 84, 9, 14)), ("door", arch(122, 84, 9, 14)),
          ("ink", lines([(109, 0.5, 109, 6), (106.5, 2.5, 111.5, 2.5)]))]
    s += [("moor", rect(0, 100, W, 40)),
          ("gline", lines([(0, 112, W, 112), (0, 124, W, 124)]))]
    for cx, base, h in ((22, 122, 24), (52, 112, 14), (156, 114, 16), (182, 124, 26)):
        s += frailejon(cx, base, h)
    return s


def raquira():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(166, 26, 11)),
         ("near", poly([(0, 62), (36, 46), (70, 58), (110, 42), (150, 56), (200, 46), (200, 100), (0, 100)]))]
    # casas pintadas a los lados
    for x, w, alto, muro, borde in ((2, 26, 34, "paint", "ochre"), (28, 28, 28, "ochre", "trim"),
                                    (146, 28, 30, "window", "ochre"), (174, 26, 36, "leaf", "paint")):
        y = 100 - alto
        s += [(muro, rect(x, y, w, alto)), (borde, rect(x, y, w, 4)),
              ("door", arch(x + w / 2 - 4, 100 - 16, 8, 16)), ("wall", rect(x + 3, y + 8, 6, 7)),
              ("wall", rect(x + w - 9, y + 8, 6, 7))]
    tierra_plaza(s)
    # cántaro gigante de la plaza
    cx, base, w, h = 100, 108, 52, 72
    s += [("clay", vasija(cx, base, w, h)),
          ("clay", rect(cx - w * 0.24, base - h - 3, w * 0.48, 5)),
          ("ochre", rect(cx - 21, base - h * 0.62, 42, 7)),
          ("trim", poly([(cx - 21, base - h * 0.62 + 7), (cx - 14, base - h * 0.62 + 14), (cx - 7, base - h * 0.62 + 7),
                         (cx, base - h * 0.62 + 14), (cx + 7, base - h * 0.62 + 7), (cx + 14, base - h * 0.62 + 14),
                         (cx + 21, base - h * 0.62 + 7)])),
          ("cshade", rect(cx - 17, base - 20, 34, 4))]
    # vasijas pequeñas
    for x, b, ww, hh, papel in ((40, 120, 18, 22, "clay"), (60, 126, 14, 16, "cshade"),
                                (146, 124, 16, 20, "cshade"), (166, 120, 20, 24, "clay")):
        s += [(papel, vasija(x, b, ww, hh)), ("ochre", rect(x - ww * 0.3, b - hh * 0.55, ww * 0.6, 2.5))]
    return s


def espiral(cx, cy, r0, r1, vueltas):
    k = math.log(r0 / r1) / (vueltas * 2 * math.pi)
    pts = [(cx + r0 * math.exp(-k * t) * math.cos(t), cy + r0 * math.exp(-k * t) * math.sin(t))
           for t in [i * vueltas * 2 * math.pi / 120 for i in range(121)]]
    return polyline(pts), k


def arbol_musgo(cx, base, r):
    """Árbol del Gallineral con barbas de viejo colgando."""
    out = [("trunk", poly([(cx - 3, base), (cx + 3, base), (cx + 2, base - r * 1.6), (cx - 2, base - r * 1.6)])),
           ("leaf", circle(cx, base - r * 2, r))]
    for dx, largo in ((-0.7, 1.1), (-0.25, 1.4), (0.25, 1.2), (0.7, 1.0)):
        x = cx + dx * r
        out.append(("moss", poly([(x - 3.5, base - r * 1.5), (x + 3.5, base - r * 1.5), (x, base - r * 1.5 + r * largo)])))
    return out


def guane():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(32, 26, 10)),
         ("far", poly([(0, 62), (34, 42), (64, 54), (100, 36), (140, 52), (172, 40), (200, 50), (200, 96), (0, 96)])),
         ("near", poly([(0, 82), (50, 72), (100, 80), (150, 70), (200, 78), (200, 100), (0, 100)])),
         # capilla de Santa Lucía al fondo
         ("wall", rect(144, 62, 38, 34)), ("wshade", rect(172, 62, 10, 34)),
         ("wall", poly([(140, 64), (163, 50), (186, 64)])), ("wall", poly([(156, 52), (156, 40), (163, 34), (170, 40), (170, 52)])),
         ("door", arch(160, 41, 6, 8)), ("door", arch(156, 78, 14, 18)), ("ink", lines([(163, 27, 163, 34), (160.5, 30, 165.5, 30)])),
         ("wall", rect(8, 70, 40, 28)), ("wshade", rect(8, 72, 40, 3)), ("roof", poly([(4, 72), (13, 63), (44, 63), (52, 72)])),
         ("window", rect(14, 78, 8, 8)), ("door", arch(32, 82, 9, 16)),
         ("ground", rect(0, 96, W, 44))]
    # Camino Real empedrado
    s += [("stone", poly([(56, 140), (88, 96), (112, 96), (150, 140)])),
          ("gline", lines([(62, 132, 140, 132), (70, 121, 129, 121), (78, 110, 120, 110), (84, 102, 115, 102),
                           (90, 140, 96, 132), (110, 140, 112, 132), (86, 132, 92, 121), (104, 132, 106, 121),
                           (120, 132, 118, 121), (94, 121, 96, 110), (108, 121, 108, 110), (96, 110, 98, 102)]))]
    # amonita gigante sobre su piedra
    cx, cy, r = 100, 62, 30
    s += [("sand", rect(70, 88, 60, 12)), ("sandshade", rect(70, 96, 60, 4)),
          ("stone", circle(cx, cy, r))]
    (d, _), k = espiral(cx, cy, r, 3, 3)
    costillas = []
    for i in range(36):
        t = i * 2 * math.pi / 18
        r_a, r_b = r * math.exp(-k * t), r * math.exp(-k * (t + 2 * math.pi))
        if r_b < 4:
            break
        costillas.append((cx + r_a * math.cos(t), cy + r_a * math.sin(t), cx + r_b * math.cos(t), cy + r_b * math.sin(t)))
    s += [("sandshade", circle(cx, cy, 8)), ("dash", lines(costillas)), ("ink", (d, None))]
    return s


def zapatoca():
    s = [("sky", rect(0, 0, W, H)),
         ("cloud", poly([(130, 28), (136, 20), (146, 20), (152, 14), (164, 16), (170, 24), (178, 28)])),
         ("canyon", poly([(0, 46), (30, 40), (60, 52), (90, 44), (130, 54), (170, 42), (200, 48), (200, 96), (0, 96)])),
         ("canyon2", poly([(0, 62), (40, 58), (60, 70), (100, 66), (140, 72), (170, 60), (200, 64), (200, 96), (0, 96)])),
         ("gline", lines([(20, 64, 50, 64), (110, 70, 150, 74), (160, 66, 190, 66)])),
         # Cueva del Nitro en la peña
         ("canyon", poly([(0, 66), (18, 60), (42, 68), (52, 100), (0, 100)])),
         ("coal", arch(10, 76, 22, 24)),
         ("near", rect(0, 98, W, 4))]
    tierra_plaza(s, 100)
    # templo de San Joaquín: fachada blanca y torre alta con reloj
    s += [("wall", rect(70, 60, 60, 42)), ("wshade", rect(118, 60, 12, 42)),
          ("wall", poly([(66, 62), (100, 46), (134, 62)])), ("wshade", rect(66, 61, 68, 2.6)),
          ("wall", rect(90, 30, 20, 32)), ("wshade", rect(104, 30, 6, 32)),
          ("window", arch(88, 13, 24, 19)), ("wall", rect(86, 28, 28, 4)),
          ("ochre", circle(100, 41, 5.5)), ("ink", lines([(100, 41, 100, 37.5), (100, 41, 103, 42.5)])),
          ("door", arch(96, 49, 8, 9)),
          ("door", arch(92, 80, 16, 22)), ("door", arch(76, 84, 9, 14)), ("door", arch(115, 84, 9, 14)),
          ("ink", lines([(100, 7, 100, 13)]))]
    # casa de balcón
    s += [("wall", rect(146, 66, 50, 36)), ("wshade", rect(146, 68, 50, 3)),
          ("roof", poly([(142, 68), (151, 59), (192, 59), (200, 68)])),
          ("window", rect(152, 72, 8, 10)), ("window", rect(166, 72, 8, 10)), ("window", rect(180, 72, 8, 10)),
          ("trunk", rect(148, 82, 46, 3)), ("dash", lines([(x, 85, x, 90) for x in range(151, 194, 5)])),
          ("trunk", rect(148, 90, 46, 2)), ("door", arch(164, 92, 10, 10))]
    return s


def san_gil():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(100, 24, 10)),
         ("far", poly([(0, 58), (30, 40), (64, 54), (100, 44), (136, 56), (170, 38), (200, 50), (200, 90), (0, 90)])),
         ("near", poly([(0, 78), (40, 68), (90, 76), (140, 66), (200, 74), (200, 92), (0, 92)])),
         ("sea", rect(0, 86, W, 34)),
         ("wave", lines([(6, 92, 20, 92), (40, 112, 58, 112), (136, 94, 150, 94), (150, 114, 168, 114),
                         (60, 96, 70, 96), (176, 102, 192, 102), (8, 106, 22, 106)])),
         ("near", rect(0, 118, W, 22))]
    for cx, base, r in ((22, 100, 17), (178, 100, 17)):
        s += arbol_musgo(cx, base, r)
    # balsa de rafting por el río Fonce
    rem = [(76, 80, 64, 108), (100, 79, 92, 108), (124, 80, 136, 108)]
    s += [("paint", rect(73, 84, 10, 12)), ("window", rect(95, 83, 10, 12)), ("leaf", rect(117, 84, 10, 12)),
          ("stone", circle(78, 79, 4)), ("stone", circle(100, 78, 4)), ("stone", circle(122, 79, 4)),
          ("roof", arch(73.5, 72, 9, 6)), ("ochre", arch(95.5, 71, 9, 6)), ("roof", arch(117.5, 72, 9, 6)),
          ("bar", lines(rem)),
          ("ochre", rrect(58, 94, 84, 16, 8)), ("oshade", rect(64, 104, 72, 3)),
          ("wave", lines([(44, 102, 54, 98), (146, 98, 156, 102)]))]
    return s


def barichara():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(30, 26, 10)),
         ("far", poly([(0, 64), (36, 44), (70, 56), (104, 40), (140, 56), (174, 44), (200, 54), (200, 96), (0, 96)])),
         ("near", poly([(0, 84), (40, 74), (90, 80), (140, 72), (200, 80), (200, 100), (0, 100)]))]
    for x0, w in ((2, 58), (140, 58)):
        s += [("wall", rect(x0, 76, w, 24)), ("wshade", rect(x0, 78, w, 3)),
              ("roof", poly([(x0 - 4, 78), (x0 + 5, 69), (x0 + w - 5, 69), (x0 + w + 4, 78)])),
              ("door", arch(x0 + 8, 84, 9, 16)), ("window", rect(x0 + 26, 84, 8, 8)), ("trunk", rect(x0 + 24, 92, 12, 2.4)),
              ("door", arch(x0 + w - 16, 84, 9, 16))]
    s.append(("stone", rect(0, 98, W, 42)))
    s.append(("gline", lines([(0, 106, W, 106), (0, 116, W, 116), (0, 128, W, 128)]
                             + [(x + (y % 20), y, x + (y % 20), y + 8) for y in (98, 108, 118) for x in range(6, 200, 20)])))
    # catedral de la Inmaculada en piedra arenisca, dos torres
    for x in (68, 116):
        s += [("sand", rect(x, 34, 16, 68)), ("sandshade", rect(x + 11, 34, 5, 68)),
              ("sand", rect(x - 2, 32, 20, 4)), ("door", arch(x + 4.5, 40, 7, 11)),
              ("sand", arch(x + 2, 20, 12, 14)), ("ink", lines([(x + 8, 12, x + 8, 20)])),
              ("window", arch(x + 5, 64, 6, 10))]
    s += [("sand", rect(84, 52, 32, 50)), ("sandshade", rect(84, 52, 32, 3)),
          ("sand", poly([(82, 54), (100, 36), (118, 54)])), ("window", circle(100, 64, 5)),
          ("door", arch(91, 76, 18, 26)), ("sandshade", rect(66, 100, 68, 3))]
    return s


def cafeto(cx, base, r):
    out = [("trunk", rect(cx - 1.5, base - r * 0.6, 3, r * 0.6)), ("coffee", circle(cx, base - r * 1.2, r))]
    for dx, dy in ((-0.45, -0.1), (0.35, 0.15), (0.05, -0.5), (0.5, -0.45), (-0.3, 0.35)):
        out.append(("roof", circle(cx + dx * r, base - r * 1.2 + dy * r, 1.8)))
    return out


def palma_cera(cx, base, h):
    top = base - h
    return [("palm", poly([(cx - 2, base), (cx + 2, base), (cx + 1.2, top), (cx - 1.2, top)])),
            ("leaf", poly([(cx, top), (cx - 16, top + 2), (cx - 24, top + 10), (cx - 12, top + 5)])),
            ("leaf", poly([(cx, top), (cx + 16, top + 2), (cx + 24, top + 10), (cx + 12, top + 5)])),
            ("leaf", poly([(cx, top), (cx - 8, top - 10), (cx - 18, top - 12), (cx - 8, top - 4)])),
            ("leaf", poly([(cx, top), (cx + 8, top - 10), (cx + 18, top - 12), (cx + 8, top - 4)]))]


def guadua(x, base, h):
    return [("bamboo", rect(x, base - h, 3.5, h)), ("dash", lines([(x, y, x + 3.5, y) for y in range(int(base - h) + 8, int(base), 9)])),
            ("leaf", poly([(x + 2, base - h), (x - 10, base - h + 6), (x - 2, base - h + 2)])),
            ("leaf", poly([(x + 2, base - h + 4), (x + 14, base - h + 10), (x + 4, base - h + 5)]))]


def marsella():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(170, 26, 10)),
         ("far", poly([(0, 54), (40, 36), (80, 50), (120, 34), (160, 48), (200, 38), (200, 90), (0, 90)])),
         ("near", poly([(0, 66), (50, 54), (100, 62), (150, 52), (200, 60), (200, 104), (0, 104)]))]
    for x in (8, 15, 22, 182, 189):
        s += guadua(x, 100, 54 - (x % 3) * 6)
    # finca cafetera de bahareque con balcón de colores
    s += [("wall", rect(56, 52, 88, 48)), ("wshade", rect(56, 54, 88, 3)),
          ("roof", poly([(48, 54), (62, 38), (138, 38), (152, 54)])), ("rshade", rect(56, 54, 88, 2)),
          ("window", rect(62, 60, 12, 12)), ("paint", rect(62, 60, 3, 12)), ("paint", rect(71, 60, 3, 12)),
          ("window", rect(126, 60, 12, 12)), ("paint", rect(126, 60, 3, 12)), ("paint", rect(135, 60, 3, 12)),
          ("door", rect(92, 58, 16, 16)),
          ("leaf", rect(56, 75, 88, 3)), ("dash", lines([(x, 78, x, 84) for x in range(60, 142, 6)])), ("leaf", rect(56, 84, 88, 2.5)),
          ("door", rect(66, 88, 12, 12)), ("door", rect(122, 88, 12, 12)), ("ochre", rect(92, 86, 16, 14))]
    s += [("coffee", rect(0, 100, W, 40)),
          ("gline", lines([(0, 112, W, 108), (0, 126, W, 122)]))]
    for cx, base, r in ((14, 116, 9), (40, 120, 10), (66, 116, 8), (134, 116, 8), (160, 120, 10), (186, 116, 9),
                        (26, 136, 9), (100, 134, 9), (174, 136, 9)):
        s += cafeto(cx, base, r)
    return s


def filandia():
    s = [("sky", rect(0, 0, W, H)),
         ("cloud", poly([(16, 28), (22, 21), (32, 21), (38, 15), (50, 17), (56, 24), (64, 28)])),
         ("far", poly([(0, 66), (36, 50), (72, 60), (110, 46), (150, 60), (200, 48), (200, 100), (0, 100)])),
         ("near", poly([(0, 84), (40, 76), (100, 82), (150, 74), (200, 80), (200, 104), (0, 104)]))]
    for x in (150, 157, 186):
        s += guadua(x, 100, 40)
    # mirador de la Colina Iluminada: torre de madera
    s += [("wood", poly([(80, 102), (120, 102), (110, 30), (90, 30)])),
          ("trunk", poly([(110, 102), (120, 102), (110, 30), (106, 30)])),
          ("dash", lines([(84, 92, 116, 76), (84, 76, 116, 92), (87, 70, 113, 54), (87, 54, 113, 70), (89, 48, 111, 36), (89, 36, 111, 48)])),
          ("trunk", rect(76, 76, 48, 4)), ("trunk", rect(80, 52, 40, 4)), ("trunk", rect(84, 30, 32, 4)),
          ("wood", rect(88, 20, 24, 10)), ("window", rect(91, 22, 18, 6)),
          ("roof", poly([(82, 21), (100, 11), (118, 21)]))]
    # casas de balcón
    for x0, muro in ((4, "paint"), (38, "leaf")):
        s += [("wall", rect(x0, 74, 32, 28)), (muro, rect(x0, 84, 32, 3)), ("dash", lines([(x, 87, x, 92) for x in range(x0 + 3, x0 + 32, 5)])),
              (muro, rect(x0, 92, 32, 2)), ("roof", poly([(x0 - 2, 76), (x0 + 5, 68), (x0 + 27, 68), (x0 + 34, 76)])),
              ("window", rect(x0 + 5, 77, 7, 6)), ("window", rect(x0 + 20, 77, 7, 6)), ("door", rect(x0 + 12, 94, 8, 8))]
    tierra_plaza(s, 100)
    return s


def salento():
    s = [("sky", rect(0, 0, W, H)),
         ("cloud", poly([(70, 34), (78, 26), (90, 26), (98, 20), (112, 22), (120, 30), (130, 34)])),
         ("far", poly([(0, 58), (30, 38), (66, 54), (104, 40), (140, 56), (176, 36), (200, 46), (200, 100), (0, 100)])),
         ("near", poly([(0, 84), (40, 74), (100, 80), (160, 72), (200, 78), (200, 140), (0, 140)]))]
    for cx, base, h in ((24, 108, 82), (60, 96, 62), (140, 96, 66), (176, 110, 86), (104, 88, 44)):
        s += palma_cera(cx, base, h)
    # Willys del Cocora
    s += [("gline", lines([(0, 124, W, 124)])),
          ("tire", circle(64, 104, 8)), ("wall", circle(64, 104, 3)),
          ("door", rect(76, 88, 12, 10)), ("window", rect(68, 96, 66, 18)), ("window", rect(108, 92, 26, 6)),
          ("bar", lines([(100, 96, 98, 78), (98, 78, 82, 78), (82, 78, 80, 96)])),
          ("wall", rect(132, 96, 4, 14)), ("ochre", circle(130, 97, 2.5)), ("trim", rect(80, 104, 20, 3)),
          ("tire", circle(84, 117, 7)), ("tire", circle(120, 117, 7)), ("wall", circle(84, 117, 2.5)), ("wall", circle(120, 117, 2.5))]
    return s


def manizales():
    s = [("sky", rect(0, 0, W, H)),
         ("far", poly([(0, 58), (24, 38), (40, 26), (56, 30), (80, 48), (110, 40), (150, 54), (200, 58), (200, 96), (0, 96)])),
         ("wall", poly([(17, 44), (24, 38), (40, 26), (56, 30), (66, 38), (56, 37), (47, 33), (38, 39), (28, 37)])),
         ("near", poly([(0, 82), (40, 70), (90, 78), (140, 68), (200, 76), (200, 104), (0, 104)])),
         # cable aéreo
         ("bar", lines([(120, 20, 200, 40)])), ("roof", rrect(153, 34, 14, 12, 3)), ("window", rect(156, 37, 8, 5)),
         ("bar", lines([(160, 28, 160, 34)]))]
    # edificios de la ciudad
    for x, w, h, papel in ((4, 30, 34, "brick"), (36, 22, 26, "paint"), (152, 24, 30, "ochre"), (176, 22, 38, "window")):
        s += [(papel, rect(x, 102 - h, w, h))] + [("wall", rect(x + 4 + i * 8, 102 - h + 6, 4, 5)) for i in range(int((w - 4) // 8))]
    # Catedral Basílica: torre neogótica
    s += [("slate", rect(68, 66, 64, 36)), ("stone", rect(68, 66, 64, 36)), ("sshade", rect(120, 66, 12, 36)),
          ("stone", poly([(64, 68), (100, 52), (136, 68)])),
          ("stone", rect(88, 30, 24, 40)), ("sshade", rect(105, 30, 7, 40)),
          ("stone", rect(84, 28, 32, 4)), ("slate", poly([(90, 28), (100, 10), (110, 28)])),
          ("door", gothic(94.5, 36, 11, 18)), ("window", circle(100, 62, 4.5)),
          ("door", gothic(92, 80, 16, 22)), ("door", gothic(74, 84, 10, 18)), ("door", gothic(116, 84, 10, 18)),
          ("ink", lines([(100, 10, 100, 4), (97.5, 6, 102.5, 6)]))]
    tierra_plaza(s, 100)
    return s


def silla(x, base, papel):
    return [(papel, rect(x, base - 16, 2.5, 16)), (papel, rect(x, base - 8, 10, 2.5)), (papel, rect(x + 8, base - 8, 2, 8))]


def avion(cx, cy):
    return [("window", poly([(cx + 10, cy), (cx + 22, cy - 6), (cx + 26, cy - 6), (cx + 18, cy)])),
            ("wall", poly([(cx - 26, cy - 3), (cx + 16, cy - 3), (cx + 26, cy - 14), (cx + 30, cy - 14), (cx + 26, cy + 4),
                           (cx - 22, cy + 4), (cx - 28, cy + 1)])),
            ("window", poly([(cx - 4, cy + 1), (cx + 8, cy + 1), (cx - 6, cy + 14), (cx - 12, cy + 14)])),
            ("trim", rect(cx - 20, cy, 40, 2)), ("door", rect(cx - 25, cy - 2, 4, 2.5))]


def jerico():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(168, 24, 9)),
         ("far", poly([(0, 60), (20, 40), (34, 34), (50, 42), (80, 56), (120, 44), (160, 54), (200, 46), (200, 96), (0, 96)])),
         # Cristo Rey en el morro
         ("wall", rect(32, 20, 4, 15)), ("wall", rect(26, 23, 16, 3)), ("wall", circle(34, 17, 2.8)),
         ("near", poly([(0, 80), (40, 70), (90, 78), (140, 68), (200, 76), (200, 104), (0, 104)]))]
    for x0, muro, borde in ((140, "window", "ochre"), (170, "paint", "leaf")):
        s += [(muro, rect(x0, 70, 28, 32)), ("roof", poly([(x0 - 3, 72), (x0 + 4, 64), (x0 + 24, 64), (x0 + 31, 72)])),
              (borde, rect(x0, 82, 28, 3)), ("dash", lines([(x, 85, x, 90) for x in range(x0 + 3, x0 + 28, 5)])),
              (borde, rect(x0, 90, 28, 2)), ("wall", rect(x0 + 5, 74, 7, 7)), ("door", rect(x0 + 10, 93, 8, 9))]
    # catedral de ladrillo con dos torres y cúpula
    s += [("brick", rect(66, 34, 16, 68)), ("bshade", rect(77, 34, 5, 68)), ("brick", rect(118, 34, 16, 68)), ("bshade", rect(129, 34, 5, 68)),
          ("window", arch(68, 22, 12, 13)), ("window", arch(120, 22, 12, 13)), ("brick", rect(64, 32, 20, 4)), ("brick", rect(116, 32, 20, 4)),
          ("door", arch(71, 40, 6, 10)), ("door", arch(123, 40, 6, 10)),
          ("window", arch(88, 28, 24, 24)), ("ochre", rect(98, 22, 4, 7)),
          ("brick", rect(82, 50, 36, 52)), ("bshade", rect(82, 50, 36, 3)),
          ("brick", poly([(80, 52), (100, 40), (120, 52)])), ("wall", circle(100, 62, 5)),
          ("door", arch(90, 76, 20, 26)), ("wall", rect(64, 100, 72, 3))]
    tierra_plaza(s, 102)
    return s


def jardin():
    s = [("sky", rect(0, 0, W, H)),
         ("cloud", poly([(14, 30), (20, 22), (30, 22), (36, 16), (48, 18), (54, 26), (62, 30)])),
         ("far", poly([(0, 56), (30, 40), (64, 52), (100, 34), (136, 50), (170, 36), (200, 46), (200, 96), (0, 96)])),
         ("near", poly([(0, 78), (40, 68), (100, 76), (160, 66), (200, 74), (200, 102), (0, 102)]))]
    for x0, muro in ((4, "paint"), (164, "window")):
        s += [(muro, rect(x0, 70, 32, 32)), ("roof", poly([(x0 - 3, 72), (x0 + 4, 64), (x0 + 28, 64), (x0 + 35, 72)])),
              ("wall", rect(x0 + 5, 76, 8, 8)), ("wall", rect(x0 + 19, 76, 8, 8)), ("door", rect(x0 + 12, 90, 8, 12)),
              ("ochre", rect(x0, 98, 32, 4))]
    # basílica neogótica de piedra
    s += [("granite", rect(64, 58, 72, 44)), ("gshade", rect(124, 58, 12, 44)),
          ("granite", poly([(62, 60), (68, 40), (74, 60)])), ("granite", poly([(126, 60), (132, 40), (138, 60)])),
          ("granite", poly([(76, 60), (100, 42), (124, 60)])),
          ("granite", rect(90, 28, 20, 34)), ("gshade", rect(104, 28, 6, 34)),
          ("granite", poly([(88, 30), (100, 12), (112, 30)])),
          ("wshade", rect(62, 59, 76, 2.4)),
          ("door", gothic(95, 32, 10, 16)), ("window", circle(100, 56, 4.4)),
          ("door", gothic(92, 76, 16, 26)), ("door", gothic(72, 80, 10, 18)), ("door", gothic(118, 80, 10, 18)),
          ("window", gothic(72, 64, 8, 12)), ("window", gothic(120, 64, 8, 12)),
          ("ink", lines([(100, 12, 100, 6), (97.5, 8, 102.5, 8)]))]
    tierra_plaza(s, 102)
    for i, x in enumerate((8, 24, 40, 56, 132, 148, 164, 180)):
        s += silla(x, 124 + (i % 2) * 8, ("roof", "ochre", "window", "leaf", "paint")[i % 5])
    return s


def guatape():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(30, 24, 9)),
         ("cloud", poly([(150, 26), (156, 19), (166, 19), (172, 13), (184, 15), (190, 23), (198, 26)])),
         ("far", poly([(0, 76), (40, 62), (80, 72), (120, 60), (160, 70), (200, 62), (200, 96), (0, 96)])),
         # Piedra del Peñol
         ("granite", poly([(60, 98), (60, 62), (66, 38), (80, 24), (100, 20), (120, 24), (134, 38), (140, 62), (140, 98)])),
         ("gshade", poly([(126, 98), (128, 56), (122, 30), (134, 38), (140, 62), (140, 98)])),
         ("gline", lines([(70, 40, 74, 80), (114, 34, 118, 70)])),
         ("wall", rect(94, 14, 12, 8)), ("roof", poly([(92, 15), (100, 9), (108, 15)])),
         ("ink", polyline([(104, 98), (96, 90), (106, 82), (96, 74), (106, 66), (96, 58), (106, 50), (96, 42), (106, 34), (100, 24)])),
         ("shine", polyline([(104, 97), (96, 89), (106, 81), (96, 73), (106, 65), (96, 57), (106, 49), (96, 41), (106, 33), (100, 25)])),
         # embalse con islas
         ("sea", rect(0, 94, W, 46)), ("near", poly([(52, 100), (62, 94), (74, 100)])), ("near", poly([(130, 100), (142, 93), (154, 100)])),
         ("wave", lines([(60, 108, 74, 108), (126, 112, 140, 112), (80, 124, 94, 124), (110, 128, 124, 128)])),
         ("roof", poly([(92, 116), (112, 116), (108, 122), (96, 122)])), ("wall", poly([(102, 116), (102, 104), (110, 114)]))]
    # zócalos de colores en la orilla
    for x, muro, zocalo in ((0, "wall", "paint"), (24, "ochre", "window"), (152, "wall", "leaf"), (176, "window", "ochre")):
        s += [(muro, rect(x, 104, 24, 28)), ("roof", poly([(x - 2, 106), (x + 4, 98), (x + 20, 98), (x + 26, 106)])),
              (zocalo, rect(x, 120, 24, 12)), ("door", rect(x + 8, 118, 8, 14)), ("wall", rect(x + 8, 108, 8, 6))]
    return s


def rionegro():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(30, 26, 9)),
         ("cloud", poly([(130, 66), (136, 59), (146, 59), (152, 53), (164, 55), (170, 63), (178, 66)])),
         ("far", poly([(0, 72), (40, 58), (80, 68), (120, 56), (160, 66), (200, 58), (200, 96), (0, 96)]))]
    s += avion(156, 32)
    # catedral de San Nicolás
    s += [("wall", rect(70, 64, 60, 36)), ("wshade", rect(118, 64, 12, 36)),
          ("wall", poly([(66, 66), (100, 52), (134, 66)])), ("wall", rect(92, 38, 16, 26)), ("wshade", rect(103, 38, 5, 26)),
          ("roof", arch(90, 26, 20, 14)), ("door", arch(96, 44, 8, 10)), ("ink", lines([(100, 26, 100, 20)])),
          ("door", arch(92, 78, 16, 22)), ("window", arch(77, 74, 6, 10)), ("window", arch(117, 74, 6, 10))]
    for x0 in (4, 150):
        s += [("wall", rect(x0, 78, 46, 22)), ("roof", poly([(x0 - 3, 80), (x0 + 5, 72), (x0 + 41, 72), (x0 + 49, 80)])),
              ("door", arch(x0 + 19, 86, 8, 14)), ("window", rect(x0 + 6, 84, 8, 7)), ("window", rect(x0 + 32, 84, 8, 7))]
    # cultivos de flores
    s.append(("coffee", rect(0, 100, W, 40)))
    for i, y in enumerate((106, 116, 128)):
        papel = ("roof", "ochre", "paint")[i]
        s.append(("leaf", rect(0, y - 3, W, 6)))
        for x in range(6 + (i % 2) * 6, 200, 12):
            s.append((papel, circle(x, y - 1, 3)))
    return s


def mangle(cx, base, r):
    return [("ink", lines([(cx - r * 0.8, base, cx - 2, base - r * 0.8), (cx, base, cx, base - r * 0.8),
                           (cx + r * 0.8, base, cx + 2, base - r * 0.8)])),
            ("coffee", circle(cx, base - r * 1.4, r))]


def cienaga():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(160, 40, 11)),
         ("cloud", poly([(20, 30), (26, 23), (36, 23), (42, 17), (54, 19), (60, 27), (68, 30)])),
         ("coffee", poly([(0, 78), (0, 70), (20, 66), (40, 70), (60, 64), (90, 70), (120, 64), (150, 70), (180, 64), (200, 68), (200, 78)])),
         ("sea", rect(0, 76, W, 64)),
         ("wave", lines([(10, 92, 24, 92), (120, 120, 136, 120), (150, 104, 166, 104), (30, 130, 44, 130), (100, 96, 112, 96)]))]
    # palafitos de la Ciénaga Grande
    for x, w, piso, h, muro in ((10, 30, 88, 20, "paint"), (78, 44, 82, 30, "window"), (156, 34, 90, 22, "ochre")):
        s += [("trunk", rect(x + 2, piso, 3, 112 - piso)), ("trunk", rect(x + w - 5, piso, 3, 112 - piso)),
              ("trunk", rect(x + w / 2 - 1.5, piso, 3, 112 - piso)),
              ("wood", rect(x - 3, piso, w + 6, 3.5)), (muro, rect(x, piso - h, w, h)),
              ("roof", poly([(x - 4, piso - h + 2), (x + w / 2, piso - h - 12), (x + w + 4, piso - h + 2)])),
              ("door", rect(x + w / 2 - 4, piso - 13, 8, 13)), ("wall", rect(x + 4, piso - h + 5, 6, 6))]
    s += mangle(58, 92, 8) + mangle(144, 96, 7)
    # pescador con su atarraya
    s += [("trunk", poly([(30, 116), (94, 116), (86, 125), (38, 125)])),
          ("leaf", rect(52, 101, 7, 15)), ("stone", circle(55.5, 97, 3.6)), ("ochre", poly([(49, 95), (62, 95), (55.5, 89)])),
          ("dash", lines([(60, 103, 70 + 4 * i, 92 + 5 * i) for i in range(6)] + [(70, 92, 90, 117)])),
          ("bar", lines([(59, 104, 66, 100)]))]
    return s


def mompox():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(28, 26, 10)),
         ("cloud", poly([(140, 30), (146, 22), (156, 22), (162, 16), (174, 18), (180, 26), (188, 30)])),
         ("coffee", poly([(0, 74), (30, 68), (70, 74), (130, 68), (170, 74), (200, 70), (200, 80), (0, 80)]))]
    for x0 in (2, 152):
        s += [("wall", rect(x0, 64, 46, 36)), ("wshade", rect(x0, 66, 46, 3)),
              ("roof", poly([(x0 - 3, 66), (x0 + 5, 58), (x0 + 41, 58), (x0 + 49, 66)])),
              ("window", rect(x0 + 5, 72, 10, 14)), ("window", rect(x0 + 31, 72, 10, 14)),
              ("dash", lines([(x, 72, x, 86) for x in (x0 + 8, x0 + 11.5, x0 + 34, x0 + 37.5)])),
              ("door", arch(x0 + 18, 76, 10, 24))]
    # iglesia de Santa Bárbara: fachada amarilla y torre octogonal con balcón
    s += [("ochre", rect(66, 52, 56, 48)), ("oshade", rect(110, 52, 12, 48)),
          ("ochre", arch(76, 30, 36, 26)), ("oshade", rect(70, 52, 50, 3)),
          ("wall", circle(94, 44, 4.5)), ("door", arch(86, 72, 16, 28)),
          ("door", arch(72, 62, 7, 12)), ("door", arch(109, 62, 7, 12)),
          ("ochre", rect(122, 30, 20, 70)), ("oshade", rect(136, 30, 6, 70)),
          ("trim", rect(118, 46, 28, 3)), ("dash", lines([(x, 41, x, 46) for x in range(120, 146, 4)])), ("trim", rect(118, 40, 28, 2)),
          ("ochre", rect(124, 22, 16, 18)), ("door", arch(128.5, 26, 7, 12)),
          ("window", arch(122, 10, 20, 14)), ("ink", lines([(132, 10, 132, 4)])),
          ("door", arch(127, 60, 10, 14))]
    # río Magdalena con su chalupa
    s += [("stone", rect(0, 98, W, 8)), ("sea", rect(0, 104, W, 36)),
          ("wave", lines([(10, 114, 26, 114), (150, 126, 166, 126), (40, 128, 54, 128), (176, 112, 190, 112)])),
          ("paint", poly([(70, 112), (130, 112), (124, 122), (76, 122)])), ("wall", rect(84, 104, 30, 8)),
          ("roof", rect(82, 102, 34, 3))]
    return s


def santa_marta():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(164, 24, 9)),
         ("far", poly([(0, 62), (20, 34), (36, 40), (56, 20), (84, 44), (120, 52), (200, 48), (200, 80), (0, 80)])),
         ("wall", poly([(12, 45), (20, 34), (36, 40), (56, 20), (70, 33), (62, 32), (56, 28), (46, 37), (36, 44), (26, 38)])),
         ("sea", rect(0, 76, W, 26)),
         ("wave", lines([(56, 84, 70, 84), (6, 94, 20, 94), (150, 90, 166, 90)])),
         ("wall", poly([(24, 88), (40, 88), (36, 94), (28, 94)])), ("wall", poly([(32, 88), (32, 76), (40, 86)])),
         ("stone", rect(0, 100, W, 40)), ("sshade", rect(0, 100, W, 3))]
    # palmera
    s += [("trunk", poly([(58, 124), (63, 124), (68, 76), (65, 75)])),
          ("leaf", poly([(66, 75), (48, 70), (38, 78), (52, 74)])),
          ("leaf", poly([(66, 75), (58, 60), (46, 58), (56, 65)])),
          ("leaf", poly([(66, 75), (76, 60), (88, 60), (78, 66)])),
          ("leaf", poly([(66, 75), (84, 72), (92, 82), (78, 77)]))]
    # Catedral: torre a la izquierda y cúpula detrás
    dx = 22
    s += [("roof", arch(98 + dx, 36, 30, 30)), ("wall", rect(110 + dx, 30, 6, 7)),
          ("wall", rect(64 + dx, 30, 18, 82)), ("wshade", rect(76 + dx, 30, 6, 82)),
          ("wall", arch(64 + dx, 18, 18, 14)), ("door", arch(68.5 + dx, 36, 9, 12)), ("ink", lines([(73 + dx, 18, 73 + dx, 11)])),
          ("wall", rect(82 + dx, 60, 52, 52)), ("wshade", rect(122 + dx, 60, 12, 52)),
          ("wall", poly([(80 + dx, 62), (108 + dx, 46), (136 + dx, 62)])), ("wshade", rect(80 + dx, 61, 56, 2.6)),
          ("window", circle(108 + dx, 72, 4.5)), ("door", arch(98 + dx, 86, 20, 26)),
          ("window", arch(88 + dx, 84, 6, 10)), ("window", arch(122 + dx, 84, 6, 10))]
    return s


def barranquilla():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(30, 24, 9)),
         ("cloud", poly([(36, 54), (42, 47), (52, 47), (58, 41), (70, 43), (76, 51), (84, 54)])),
         ("sea", rect(0, 76, W, 26)), ("wave", lines([(130, 96, 146, 96)])),
         # puente Pumarejo
         ("wall", rect(120, 84, 80, 4)),
         ("wall", poly([(166, 96), (170, 30), (174, 30), (178, 96)])),
         ("bar", lines([(172, 34, 128 + 8 * i, 84) for i in range(5)] + [(172, 34, 182 + 6 * i, 84) for i in range(3)]))]
    # Ventana al Mundo
    colores = ["roof", "ochre", "leaf", "window", "paint", "roof", "ochre"]
    ys = [12, 26, 40, 54, 68, 82, 96, 104]
    def half(y):
        return 22 * (y - 12) / 92
    for i in range(len(ys) - 1):
        y0, y1 = ys[i], ys[i + 1]
        s.append((colores[i], poly([(100 - half(y0), y0), (100 + half(y0), y0), (100 + half(y1), y1), (100 - half(y1), y1)])))
    s.append(("sky", poly([(100, 48), (110, 100), (90, 100)])))
    s += [("ground", rect(0, 100, W, 40)),
          ("gline", lines([(0, 110, W, 110), (0, 124, W, 124), (60, 100, 40, 140), (140, 100, 160, 140)]))]
    # marimonda del Carnaval
    cx, cy = 34, 104
    s += [("roof", circle(cx - 15, cy - 6, 8)), ("leaf", circle(cx + 15, cy - 6, 8)),
          ("window", rrect(cx - 13, cy - 18, 26, 30, 10)),
          ("ochre", rect(cx - 13, cy - 18, 26, 6)), ("paint", rect(cx - 13, cy - 12, 26, 4)),
          ("wall", circle(cx - 6, cy - 3, 4)), ("wall", circle(cx + 6, cy - 3, 4)),
          ("tire", circle(cx - 6, cy - 3, 1.6)), ("tire", circle(cx + 6, cy - 3, 1.6)),
          ("roof", poly([(cx - 3, cy + 2), (cx + 3, cy + 2), (cx + 4, cy + 24), (cx + 9, cy + 28), (cx - 1, cy + 28), (cx - 3, cy + 22)]))]
    # flores de carnaval
    for x, y, papel in ((150, 116, "roof"), (166, 122, "ochre"), (182, 114, "paint"), (66, 124, "ochre")):
        s.append((papel, star(x, y, 6, 3)))
    return s


def saman(cx, base, w, h):
    out = [("trunk", poly([(cx - 4, base), (cx + 4, base), (cx + 3, base - h * 0.5), (cx + 16, base - h * 0.78),
                           (cx + 11, base - h * 0.8), (cx, base - h * 0.6), (cx - 11, base - h * 0.8),
                           (cx - 16, base - h * 0.78), (cx - 3, base - h * 0.5)]))]
    r = w / 6
    for i, dy in enumerate((0.9, 0.35, 0.1, 0.35, 0.9)):
        out.append(("coffee", circle(cx - w / 2 + r + i * (w - 2 * r) / 4, base - h + r + dy * r, r)))
    for i, dy in enumerate((0.2, -0.2, 0.2)):
        out.append(("leaf", circle(cx - w / 4 + i * w / 4, base - h + r * 0.9 + dy * r, r * 0.8)))
    return out


def tulua():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(30, 28, 10)),
         ("far", poly([(0, 60), (30, 44), (64, 56), (100, 40), (140, 54), (176, 42), (200, 50), (200, 96), (0, 96)])),
         ("near", poly([(0, 82), (50, 72), (110, 80), (160, 70), (200, 78), (200, 104), (0, 104)])),
         ("ground", rect(0, 100, W, 12))]
    # iglesia de San Bartolomé
    s += [("wall", rect(142, 60, 42, 40)), ("wshade", rect(174, 60, 10, 40)),
          ("wall", poly([(138, 62), (163, 48), (188, 62)])), ("wshade", rect(138, 61, 50, 2.6)),
          ("wall", rect(156, 32, 14, 18)), ("wshade", rect(165, 32, 5, 18)), ("roof", poly([(154, 32), (163, 19), (172, 32)])),
          ("door", arch(159.5, 36, 7, 10)), ("window", circle(163, 56, 3)), ("door", arch(156, 80, 14, 20)),
          ("ink", lines([(163, 19, 163, 12), (160.5, 14.5, 165.5, 14.5)]))]
    # samán del Jardín Botánico Juan María Céspedes
    s += saman(76, 102, 96, 74)
    # casita a la izquierda
    s += [("wall", rect(2, 80, 22, 20)), ("roof", poly([(0, 82), (6, 74), (20, 74), (26, 82)])), ("door", rect(9, 90, 7, 10))]
    # río Tuluá con su puente
    s += [("sea", rect(0, 110, W, 21)),
          ("wave", lines([(8, 120, 22, 120), (60, 126, 74, 126), (150, 124, 166, 124), (100, 116, 112, 116)])),
          ("stone", poly([(98, 112), (106, 104), (200, 104), (200, 131), (106, 131)])), ("sshade", rect(106, 104, 94, 3)),
          ("sea", arch(114, 114, 34, 17)), ("sea", arch(158, 114, 34, 17)),
          ("trim", rect(104, 100, 96, 4)), ("dash", lines([(x, 100, x, 104) for x in range(108, 200, 8)]))]
    return s


def buga():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(176, 26, 9)),
         ("cloud", poly([(14, 34), (20, 27), (30, 27), (36, 21), (48, 23), (54, 31), (62, 34)])),
         ("far", poly([(0, 66), (36, 50), (76, 62), (120, 48), (160, 60), (200, 50), (200, 96), (0, 96)])),
         ("near", poly([(0, 84), (50, 76), (100, 82), (150, 74), (200, 80), (200, 104), (0, 104)]))]
    # casas coloniales a los lados
    for x0 in (2, 156):
        s += [("wall", rect(x0, 74, 42, 26)), ("wshade", rect(x0, 76, 42, 3)),
              ("roof", poly([(x0 - 3, 76), (x0 + 5, 68), (x0 + 37, 68), (x0 + 45, 76)])),
              ("window", rect(x0 + 5, 82, 8, 9)), ("window", rect(x0 + 29, 82, 8, 9)), ("door", arch(x0 + 16, 84, 10, 16))]
    # Basílica del Señor de los Milagros: ladrillo rosado, dos torres con cúpula y cúpula central
    s += [("roof", arch(84, 36, 32, 26)), ("brick", rect(96, 28, 8, 9)), ("ink", lines([(100, 28, 100, 20), (97.5, 23, 102.5, 23)]))]
    for x0 in (60, 120):
        s += [("brick", rect(x0, 42, 20, 58)), ("bshade", rect(x0 + 14, 42, 6, 58)),
              ("trim", rect(x0 - 2, 58, 24, 3)), ("trim", rect(x0 - 2, 40, 24, 3)),
              ("brick", rect(x0 + 2, 28, 16, 12)), ("door", arch(x0 + 6, 30, 8, 10)),
              ("roof", arch(x0 + 1, 17, 18, 13)), ("ink", lines([(x0 + 10, 17, x0 + 10, 12)])),
              ("door", arch(x0 + 6, 45, 8, 11)), ("window", arch(x0 + 6, 66, 8, 12))]
    s += [("brick", rect(80, 56, 40, 44)), ("bshade", rect(80, 56, 40, 3)),
          ("brick", poly([(78, 58), (100, 44), (122, 58)])),
          ("wall", circle(100, 66, 6)), ("window", circle(100, 66, 4)),
          ("door", arch(91, 78, 18, 22)), ("trim", rect(80, 75, 40, 2.4))]
    tierra_plaza(s, 100)
    return s


def palmira():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(160, 26, 10)),
         ("far", poly([(0, 62), (40, 48), (84, 58), (130, 44), (170, 56), (200, 48), (200, 90), (0, 90)])),
         ("near", rect(0, 86, W, 16)),
         ("cloud", poly([(22, 22), (30, 14), (40, 16), (46, 12), (56, 20), (50, 26), (30, 26)]))]
    # ingenio azucarero con su chimenea
    s += [("cloud", poly([(36, 30), (42, 24), (52, 26), (58, 20), (66, 24), (64, 32), (46, 34)])),
          ("brick", poly([(40, 100), (52, 100), (49, 32), (43, 32)])), ("bshade", poly([(48, 100), (52, 100), (49, 32), (47, 32)])),
          ("trim", rect(42, 34, 8, 3)), ("trim", rect(41, 52, 10, 3)),
          ("wall", rect(54, 66, 56, 34)), ("wshade", rect(54, 68, 56, 3)),
          ("slate", poly([(50, 68), (66, 54), (98, 54), (114, 68)])),
          ("window", rect(60, 74, 10, 10)), ("window", rect(76, 74, 10, 10)), ("window", rect(92, 74, 10, 10)),
          ("door", rect(76, 88, 12, 12))]
    # palmas de la Villa de las Palmas
    for cx, h in ((136, 60), (186, 70), (14, 62)):
        s += palma_cera(cx, 102, h)
    # cañaduzal
    s += [("bamboo", rect(0, 100, W, 31))]
    tallos = []
    for x in range(4, W, 7):
        tallos += [(x, 131, x + 2, 104), (x + 3, 131, x - 3, 108)]
    s.append(("dash", lines(tallos)))
    s += [("gline", lines([(0, 112, W, 112)]))]
    # carreta de caña
    s += [("bamboo", poly([(112, 106), (114, 94), (168, 94), (170, 106)])),
          ("dash", lines([(114, 98, 168, 98), (113, 102, 169, 102)])), ("leaf", poly([(166, 94), (178, 88), (170, 96)])),
          ("leaf", poly([(114, 94), (104, 87), (116, 97)])),
          ("paint", rect(112, 106, 58, 10)), ("wall", rect(118, 108, 46, 3)),
          ("tire", circle(124, 118, 6)), ("tire", circle(158, 118, 6)),
          ("wall", circle(124, 118, 2.2)), ("wall", circle(158, 118, 2.2))]
    return s


def cali():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(140, 26, 9)),
         ("far", poly([(0, 70), (14, 46), (30, 30), (48, 40), (70, 56), (110, 48), (150, 58), (200, 50), (200, 96), (0, 96)])),
         ("near", poly([(0, 86), (40, 78), (100, 84), (160, 76), (200, 82), (200, 104), (0, 104)]))]
    # Cristo Rey en el cerro
    s += [("wall", poly([(28, 30), (32, 30), (33, 18), (27, 18)])),
          ("wall", poly([(21, 18), (39, 18), (39, 15.5), (21, 15.5)])), ("wall", circle(30, 12.5, 2.6))]
    # palmas
    for cx, h in ((44, 58), (190, 64)):
        s += palma_cera(cx, 104, h)
    # iglesia de San Francisco y su Torre Mudéjar
    s += [("ochre", rect(108, 64, 52, 38)), ("oshade", rect(148, 64, 12, 38)),
          ("ochre", poly([(104, 66), (134, 52), (164, 66)])), ("oshade", rect(104, 65, 60, 2.6)),
          ("door", arch(126, 80, 14, 22)), ("window", circle(133, 72, 3))]
    s += [("brick", rect(76, 34, 26, 68)), ("bshade", rect(95, 34, 7, 68)),
          ("trim", rect(73, 32, 32, 3)), ("trim", rect(73, 60, 32, 3)),
          ("brick", rect(80, 18, 18, 15)), ("bshade", rect(93, 18, 5, 15)),
          ("roof", poly([(78, 18), (89, 9), (100, 18)])),
          ("door", arch(85, 21, 8, 10)), ("door", arch(80, 40, 6, 14)), ("door", arch(92, 40, 6, 14)),
          ("dash", lines([(78 + 6 * i, 66, 84 + 6 * i, 72) for i in range(4)] + [(84 + 6 * i, 66, 78 + 6 * i, 72) for i in range(4)]
                         + [(78 + 6 * i, 76, 84 + 6 * i, 82) for i in range(4)] + [(84 + 6 * i, 76, 78 + 6 * i, 82) for i in range(4)])),
          ("door", arch(84, 88, 10, 14))]
    s += [("ground", rect(0, 100, W, 12)), ("gline", lines([(0, 106, W, 106)])),
          ("sea", rect(0, 112, W, 19)), ("wave", lines([(10, 120, 24, 120), (90, 124, 104, 124), (170, 120, 184, 120)]))]
    # Gato del Río sobre su pedestal
    cx = 160
    s += [("stone", rect(cx - 12, 92, 24, 12)), ("sshade", rect(cx + 6, 92, 6, 12)),
          ("oshade", poly([(cx - 9, 92), (cx + 9, 92), (cx + 7, 74), (cx - 5, 74)])),
          ("oshade", circle(cx + 1, 70, 7)),
          ("oshade", poly([(cx - 5, 66), (cx - 4, 58), (cx, 64)])), ("oshade", poly([(cx + 2, 64), (cx + 6, 58), (cx + 7, 66)])),
          ("ink", lines([(cx - 2, 69, cx - 2, 70.5), (cx + 4, 69, cx + 4, 70.5)])),
          ("oshade", poly([(cx + 9, 90), (cx + 16, 88), (cx + 18, 80), (cx + 15, 79), (cx + 14, 86), (cx + 8, 87)]))]
    return s


def caballo(cx, base, papel="trunk"):
    """Caballo mirando a la derecha; cx al centro del lomo."""
    return [(papel, rect(cx - 12, base - 10, 3, 10)), (papel, rect(cx - 6, base - 10, 3, 10)),
            (papel, rect(cx + 6, base - 10, 3, 10)), (papel, rect(cx + 11, base - 10, 3, 10)),
            (papel, poly([(cx - 14, base - 22), (cx - 22, base - 12), (cx - 18, base - 12), (cx - 13, base - 18)])),
            (papel, rrect(cx - 15, base - 24, 30, 15, 6)),
            (papel, poly([(cx + 9, base - 22), (cx + 16, base - 34), (cx + 26, base - 28), (cx + 24, base - 24), (cx + 18, base - 26), (cx + 15, base - 18)])),
            ("coal", poly([(cx + 9, base - 24), (cx + 15, base - 34), (cx + 13, base - 34), (cx + 7, base - 25)]))]


def llanero(cx, base):
    """Jinete sentado en el lomo (base = lomo del caballo)."""
    return [("window", rect(cx - 2, base - 4, 4, 10)), ("wall", poly([(cx - 4, base), (cx + 4, base), (cx + 3, base - 13), (cx - 3, base - 13)])),
            ("bar", lines([(cx + 2, base - 9, cx + 10, base - 5)])),
            ("stone", circle(cx, base - 16, 3.4)),
            ("coal", rect(cx - 9, base - 19, 18, 2.2)), ("coal", rrect(cx - 4, base - 24, 8, 6, 2))]


def vaca(cx, base):
    return [("wall", rect(cx - 11, base - 8, 3, 8)), ("wall", rect(cx + 7, base - 8, 3, 8)),
            ("wall", rrect(cx - 13, base - 20, 26, 13, 4)), ("coal", circle(cx - 4, base - 15, 3)), ("coal", circle(cx + 6, base - 12, 2.4)),
            ("wall", rrect(cx - 21, base - 22, 9, 9, 3)), ("ink", lines([(cx - 21, base - 22, cx - 25, base - 26), (cx - 12, base - 22, cx - 9, base - 26)]))]


def puerto_gaitan():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(100, 52, 13)),
         ("cloud", poly([(20, 30), (26, 23), (36, 23), (42, 17), (54, 19), (60, 27), (68, 30)])),
         ("far", poly([(0, 66), (60, 62), (140, 64), (200, 60), (200, 80), (0, 80)])),
         ("moor", rect(0, 76, W, 14))]
    # morichales
    for cx, h in ((18, 52), (36, 44), (164, 50), (186, 56), (148, 38)):
        s += palma_cera(cx, 86, h)
    # río Manacacías con su playa de arena
    s += [("sea", rect(0, 88, W, 43)),
          ("sand", poly([(0, 131), (0, 112), (40, 106), (90, 110), (120, 122), (150, 131)])),
          ("sandshade", poly([(0, 131), (0, 124), (60, 120), (110, 126), (124, 131)])),
          ("wave", lines([(120, 98, 136, 98), (160, 108, 176, 108), (20, 96, 34, 96), (70, 94, 82, 94), (170, 124, 186, 124)]))]
    # curiara con su pescador
    s += [("trunk", poly([(116, 104), (178, 104), (170, 112), (124, 112)])), ("wood", rect(126, 104, 42, 2.5)),
          ("ochre", rect(140, 92, 6, 12)), ("stone", circle(143, 89, 3.2)), ("leaf", rect(137, 86, 12, 2)),
          ("bar", lines([(146, 96, 164, 116)]))]
    # garza en la playa
    s += [("wall", poly([(56, 108), (66, 104), (72, 98), (70, 96), (64, 100), (54, 104)])),
          ("ink", lines([(60, 107, 60, 116), (63, 106, 64, 116), (72, 97, 78, 99)]))]
    return s


def yopal():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(44, 32, 11)),
         ("cloud", poly([(130, 30), (136, 23), (146, 23), (152, 17), (164, 19), (170, 27), (178, 30)])),
         ("far", poly([(0, 70), (40, 60), (80, 66), (120, 56), (160, 66), (200, 62), (200, 82), (0, 82)])),
         ("moor", rect(0, 80, W, 51)),
         ("gline", lines([(0, 96, 60, 94), (120, 108, 200, 104), (0, 122, 80, 120)]))]
    # casa llanera con techo de palma
    s += [("wall", rect(140, 64, 50, 22)), ("wshade", rect(140, 66, 50, 3)),
          ("sand", poly([(132, 66), (150, 48), (180, 48), (198, 66)])), ("dash", lines([(140 + 6 * i, 52, 136 + 6 * i, 64) for i in range(10)])),
          ("door", rect(160, 72, 10, 14)), ("window", rect(146, 72, 8, 7)), ("window", rect(176, 72, 8, 7))]
    s += palma_cera(124, 86, 44) + palma_cera(14, 84, 50)
    # vaquería: el llanero arrea el ganado
    s += vaca(52, 116) + vaca(84, 108) + vaca(30, 128)
    s += caballo(126, 124) + llanero(124, 100)
    return s


def leticia():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(166, 26, 9)),
         ("coffee", poly([(0, 74), (0, 50), (12, 44), (24, 50), (36, 40), (52, 46), (66, 38), (82, 48), (100, 42),
                          (118, 48), (134, 38), (150, 46), (168, 40), (186, 48), (200, 44), (200, 74)])),
         ("leaf", poly([(0, 78), (0, 62), (16, 58), (34, 64), (54, 56), (74, 62), (96, 56), (120, 62), (146, 54),
                        (170, 62), (200, 56), (200, 78)]))]
    # ceiba que sobresale de la selva
    s += [("trunk", poly([(30, 76), (40, 76), (37, 34), (33, 34)])),
          ("coffee", circle(24, 30, 10)), ("coffee", circle(46, 30, 10)), ("leaf", circle(35, 24, 11))]
    # palafitos a la orilla
    for x, w, papel in ((140, 28, "paint"), (172, 24, "ochre")):
        s += [("trunk", rect(x + 3, 72, 3, 16)), ("trunk", rect(x + w - 6, 72, 3, 16)), ("wood", rect(x - 2, 72, w + 4, 3)),
              (papel, rect(x, 58, w, 14)), ("sand", poly([(x - 4, 60), (x + w / 2, 48), (x + w + 4, 60)])),
              ("door", rect(x + w / 2 - 3, 62, 6, 10))]
    # río Amazonas, delfín rosado y victorias regias
    s += [("lake", rect(0, 82, W, 49)),
          ("wave", lines([(10, 96, 24, 96), (150, 98, 166, 98), (40, 120, 54, 120), (110, 126, 124, 126)])),
          ("dolphin", poly([(78, 108), (84, 94), (96, 86), (110, 88), (120, 96), (124, 106), (118, 104), (112, 96),
                            (102, 94), (92, 98), (86, 108)])),
          ("dolphin", poly([(118, 92), (126, 88), (124, 94)])), ("dolphin", poly([(96, 88), (100, 80), (104, 88)])),
          ("dolphin", poly([(78, 108), (72, 112), (82, 114), (86, 108)])), ("ink", lines([(114, 92, 114.5, 92.5)])),
          ("wave", lines([(70, 110, 92, 110)]))]
    for cx, cy, r in ((26, 112, 12), (52, 124, 9), (160, 116, 13), (186, 126, 8)):
        s += [("leaf", circle(cx, cy, r)), ("trim", rect(cx - r, cy - 1.2, 2 * r, 2.4)), ("leaf", circle(cx, cy, r - 2.5))]
    # guacamaya en vuelo
    s += [("roof", poly([(96, 30), (108, 26), (118, 30), (108, 34)])), ("window", poly([(104, 28), (100, 16), (110, 26)])),
          ("ochre", poly([(108, 32), (104, 42), (112, 32)])), ("roof", poly([(96, 30), (82, 38), (98, 32)])),
          ("wall", circle(118, 29, 2.4))]
    return s


def villavicencio():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(170, 30, 11)),
         ("far", poly([(0, 96), (0, 24), (20, 30), (36, 22), (56, 40), (74, 54), (96, 70), (120, 80), (200, 82), (200, 96)])),
         ("near", poly([(0, 100), (0, 52), (24, 58), (46, 70), (70, 84), (100, 90), (200, 90), (200, 104), (0, 104)])),
         ("moor", poly([(80, 90), (200, 84), (200, 104), (80, 104)]))]
    for cx, h in ((176, 30), (192, 26), (150, 24)):
        s += palma_cera(cx, 92, h)
    # catedral de Nuestra Señora del Carmen
    s += [("wall", rect(10, 66, 34, 34)), ("wshade", rect(36, 66, 8, 34)),
          ("wall", rect(20, 44, 14, 24)), ("wshade", rect(29, 44, 5, 24)), ("slate", poly([(18, 44), (27, 30), (36, 44)])),
          ("door", gothic(23.5, 50, 7, 11)), ("door", gothic(20, 82, 14, 18)), ("ink", lines([(27, 30, 27, 24), (24.5, 26, 29.5, 26)]))]
    # monumento del arpa llanera: caja diagonal, cuello curvo y columna
    cx = 104
    p0, p1, p2 = (cx - 14, 32), (cx, 20), (cx + 20, 28)
    cuerdas = []
    for k in range(1, 9):
        t = k / 9
        x = (1 - t) ** 2 * p0[0] + 2 * t * (1 - t) * p1[0] + t ** 2 * p2[0]
        y = (1 - t) ** 2 * p0[1] + 2 * t * (1 - t) * p1[1] + t ** 2 * p2[1]
        fondo = 92 - 60 * max(0, cx + 8 - x) / 22
        cuerdas.append((x, y, x, fondo))
    s += [("stone", rect(cx - 24, 92, 50, 10)), ("sshade", rect(cx + 14, 92, 12, 10)),
          ("ink", lines(cuerdas)),
          ("wood", poly([(cx + 8, 92), (cx + 20, 92), (cx - 18, 26), (cx - 14, 32)])),
          ("wood", rect(cx + 18, 24, 5, 68)),
          ("wood", raw(f"M{cx - 18} 26 Q{cx} 12 {cx + 23} 21 L{cx + 23} 28 Q{cx} 20 {cx - 14} 32 Z")),
          ("ochre", circle(cx + 20.5, 20, 3))]
    s += [("ground", rect(0, 100, W, 31)), ("gline", lines([(0, 110, W, 110), (0, 122, W, 122), (60, 100, 40, 131), (140, 100, 160, 131)]))]
    # pareja bailando joropo
    s += [("wall", poly([(150, 126), (162, 126), (158, 110), (154, 110)])), ("coal", rect(154, 102, 4, 9)), ("stone", circle(156, 99, 3)),
          ("coal", rect(150, 96, 12, 2)),
          ("roof", poly([(166, 126), (184, 126), (178, 110), (172, 110)])), ("wall", rect(172, 102, 6, 9)), ("stone", circle(175, 99, 3)),
          ("trim", circle(178, 97, 1.6)), ("bar", lines([(158, 106, 172, 106)]))]
    return s


def san_andres():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(32, 28, 10)),
         ("cloud", poly([(120, 26), (126, 19), (136, 19), (142, 13), (154, 15), (160, 23), (168, 26)])),
         # mar de los siete colores: franjas de turquesa a azul profundo
         ("lake", rect(0, 58, W, 14)), ("sea", rect(0, 70, W, 14)), ("turq", rect(0, 82, W, 14)), ("shallow", rect(0, 94, W, 14)),
         ("wave", lines([(20, 64, 34, 64), (150, 76, 166, 76), (60, 88, 74, 88), (170, 100, 184, 100)]))]
    # Johnny Cay con sus palmas
    s += [("sand", poly([(96, 72), (108, 64), (150, 64), (164, 72)])), ("leaf", poly([(108, 64), (114, 56), (144, 56), (150, 64)]))]
    for cx, h in ((118, 26), (130, 32), (142, 24)):
        s += palma_cera(cx, 64, h)
    # velero
    s += [("wall", poly([(50, 76), (82, 76), (76, 82), (56, 82)])), ("bar", lines([(66, 76, 66, 42)])),
          ("wall", poly([(67, 44), (67, 74), (86, 74)])), ("paint", poly([(65, 48), (65, 74), (50, 74)]))]
    # playa con palmera y cangrejo
    s += [("sand", poly([(0, 131), (0, 106), (60, 102), (120, 108), (200, 104), (200, 131)])),
          ("sandshade", poly([(0, 131), (0, 122), (80, 118), (200, 124), (200, 131)]))]
    s += [("trunk", poly([(184, 126), (189, 126), (180, 82), (177, 83)])),
          ("leaf", poly([(178, 82), (160, 78), (150, 86), (164, 82)])), ("leaf", poly([(178, 82), (170, 66), (158, 64), (168, 72)])),
          ("leaf", poly([(178, 82), (190, 66), (202, 66), (192, 72)])), ("leaf", poly([(178, 82), (198, 80), (206, 90), (192, 84)])),
          ("coffee", circle(176, 84, 2.6)), ("coffee", circle(181, 85, 2.6))]
    s += [("roof", rrect(34, 116, 16, 9, 4)), ("ink", lines([(36, 124, 32, 128), (40, 125, 38, 129), (44, 125, 46, 129), (48, 124, 52, 128),
                                                            (36, 117, 32, 112), (48, 117, 52, 112)])),
          ("wall", circle(39, 115, 1.6)), ("wall", circle(45, 115, 1.6))]
    return s


def medellin():
    s = [("sky", rect(0, 0, W, H)), ("cloud", poly([(14, 24), (20, 17), (30, 17), (36, 12), (48, 14), (54, 22), (60, 24)])),
         ("far", poly([(0, 40), (40, 30), (80, 42), (120, 34), (160, 44), (200, 32), (200, 100), (0, 100)])),
         ("near", poly([(0, 56), (30, 60), (60, 76), (90, 96), (110, 96), (140, 74), (170, 58), (200, 52), (200, 104), (0, 104)]))]
    # casitas de las laderas
    colores = ["brick", "ochre", "paint", "window", "wall", "leaf", "roof"]
    k = 0
    for x0, y0, dx, dy, n in ((4, 62, 12, 7, 6), (2, 76, 13, 7, 6), (150, 64, 11, -3, 5), (140, 80, 12, -2, 5)):
        for i in range(n):
            x, y = x0 + i * dx, y0 + i * dy
            s += [(colores[k % 7], rect(x, y, 10, 8)), ("door", rect(x + 3.5, y + 3, 3, 5))]
            k += 1
    # Metrocable
    s += [("bar", lines([(0, 34, 200, 18)])), ("wall", rect(56, 26, 3, 74)), ("wall", rect(146, 18, 3, 82))]
    for x in (30, 100, 176):
        y = 34 - 16 * x / 200
        s += [("bar", lines([(x, y, x, y + 6)])), ("ochre", rrect(x - 6, y + 6, 12, 10, 3)), ("window", rect(x - 4, y + 8, 8, 4))]
    # edificio Coltejer, la aguja
    s += [("granite", poly([(88, 104), (112, 104), (112, 50), (100, 22), (88, 50)])),
          ("gshade", poly([(104, 104), (112, 104), (112, 50), (100, 22), (104, 50)])),
          ("sky", poly([(96, 50), (104, 50), (100, 36)])),
          ("dash", lines([(88, y, 112, y) for y in range(58, 104, 6)])), ("ink", lines([(100, 22, 100, 14)]))]
    # metro sobre su viaducto
    s += [("ground", rect(0, 104, W, 27)),
          ("stone", rect(0, 100, W, 5)), ("stone", rect(30, 105, 6, 26)), ("stone", rect(98, 105, 6, 26)), ("stone", rect(166, 105, 6, 26)),
          ("wall", rrect(40, 88, 120, 12, 3)), ("ochre", rect(40, 95, 120, 2.5))]
    s += [("window", rect(46 + 13 * i, 90, 9, 4)) for i in range(9)]
    return s


def bogota():
    s = [("sky", rect(0, 0, W, H)),
         ("far", poly([(0, 70), (20, 44), (44, 22), (62, 26), (84, 46), (110, 52), (150, 48), (200, 56), (200, 96), (0, 96)])),
         ("coffee", poly([(0, 84), (30, 72), (70, 80), (120, 70), (160, 76), (200, 70), (200, 100), (0, 100)])),
         # Monserrate en la cima y el funicular
         ("wall", rect(37, 16, 16, 9)), ("wall", rect(42, 10, 6, 7)), ("roof", poly([(41, 10), (45, 5), (49, 10)])),
         ("door", arch(43, 19, 4, 6))]
    # Torre Colpatria
    s += [("granite", rect(164, 30, 22, 70)), ("gshade", rect(180, 30, 6, 70)), ("granite", rect(168, 24, 14, 6)),
          ("dash", lines([(164, y, 186, y) for y in range(36, 100, 6)])), ("ink", lines([(175, 24, 175, 14)])),
          ("roof", rect(164, 30, 22, 2)), ("window", rect(164, 46, 22, 2)), ("paint", rect(164, 62, 22, 2))]
    # Catedral Primada: fachada neoclásica de dos torres
    s += [("stone", rect(56, 60, 88, 40)), ("sshade", rect(56, 60, 88, 3)),
          ("stone", poly([(80, 60), (100, 46), (120, 60)])), ("sshade", rect(80, 58, 40, 2.4)),
          ("window", circle(100, 54, 3))]
    for x0 in (56, 124):
        s += [("stone", rect(x0, 34, 20, 66)), ("sshade", rect(x0 + 14, 34, 6, 66)), ("trim", rect(x0 - 2, 56, 24, 3)),
              ("stone", rect(x0 + 3, 22, 14, 12)), ("door", arch(x0 + 6.5, 25, 7, 9)),
              ("slate", arch(x0 + 3, 14, 14, 10)), ("ink", lines([(x0 + 10, 14, x0 + 10, 10)])),
              ("door", arch(x0 + 6, 40, 8, 12)), ("door", arch(x0 + 6, 76, 8, 14))]
    s += [("door", arch(92, 76, 16, 24)), ("door", arch(80, 82, 8, 14)), ("door", arch(112, 82, 8, 14)),
          ("dash", lines([(x, 64, x, 100) for x in (78, 90, 110, 122)]))]
    # Plaza de Bolívar con sus palomas
    tierra_plaza(s, 100)
    for x, y in ((40, 116), (60, 124), (150, 118), (170, 126), (128, 112)):
        s += [("granite", rrect(x - 4, y - 3, 8, 5, 2)), ("granite", circle(x + 4, y - 4, 2.2)), ("ink", lines([(x + 6, y - 4, x + 8, y - 3.5)]))]
    return s


def estacion_santa_fe():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(176, 26, 9)),
         ("far", poly([(0, 60), (40, 46), (80, 56), (120, 44), (160, 54), (200, 46), (200, 90), (0, 90)])),
         ("near", rect(0, 84, W, 18))]
    # edificio de la estación
    s += [("ochre", rect(20, 46, 160, 40)), ("oshade", rect(20, 48, 160, 3)),
          ("ochre", poly([(70, 46), (100, 28), (130, 46)])), ("oshade", rect(68, 45, 64, 2.6)),
          ("wall", circle(100, 39, 5)), ("ink", lines([(100, 39, 100, 36), (100, 39, 102.5, 40)])),
          ("roof", poly([(14, 48), (24, 40), (176, 40), (186, 48)]))]
    s += [("door", arch(28 + 20 * i, 56, 10, 18)) for i in range(8)]
    s += [("wall", rect(20, 74, 160, 4)), ("trim", rect(20, 78, 160, 3))]
    # andén y rieles
    s += [("stone", rect(0, 86, W, 8)), ("sshade", rect(0, 92, W, 2)), ("ground", rect(0, 94, W, 37)),
          ("dash", lines([(x, 116, x + 6, 126) for x in range(0, W, 10)])),
          ("bar", lines([(0, 118, W, 118), (0, 126, W, 126)]))]
    # vagón
    s += [("leaf", rect(126, 90, 54, 22)), ("ochre", rect(126, 104, 54, 3)),
          ("window", rect(132, 94, 10, 8)), ("window", rect(148, 94, 10, 8)), ("window", rect(164, 94, 10, 8)),
          ("roof", rect(124, 87, 58, 4)), ("tire", circle(136, 116, 5)), ("tire", circle(170, 116, 5))]
    # locomotora de vapor
    s += [("cloud", poly([(64, 62), (68, 54), (78, 54), (82, 46), (94, 48), (94, 58), (76, 66)])),
          ("coal", rect(66, 92, 54, 18)), ("roof", rect(66, 90, 54, 3)),
          ("coal", rect(72, 74, 8, 18)), ("coal", poly([(68, 70), (84, 70), (80, 76), (72, 76)])),
          ("roof", rect(96, 78, 26, 14)), ("window", rect(100, 81, 8, 7)), ("coal", rect(94, 74, 30, 5)),
          ("ochre", circle(68, 100, 3)), ("trim", poly([(58, 116), (66, 104), (66, 116)])),
          ("roof", circle(78, 116, 7)), ("roof", circle(96, 116, 7)), ("roof", circle(112, 116, 6)),
          ("tire", circle(78, 116, 2)), ("tire", circle(96, 116, 2)), ("tire", circle(112, 116, 2)),
          ("bar", lines([(78, 116, 112, 116)]))]
    return s


def mirador():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(150, 30, 10)),
         ("cloud", poly([(20, 30), (26, 23), (36, 23), (42, 17), (54, 19), (60, 27), (68, 30)])),
         ("far", poly([(0, 60), (30, 40), (60, 54), (96, 34), (130, 52), (170, 38), (200, 48), (200, 100), (0, 100)])),
         ("near", poly([(0, 78), (40, 70), (80, 82), (120, 72), (160, 84), (200, 76), (200, 110), (0, 110)]))]
    for x, y, papel in ((30, 72, "roof"), (42, 74, "ochre"), (150, 76, "paint"), (162, 78, "window"), (174, 76, "roof")):
        s += [("wall", rect(x, y, 9, 7)), (papel, poly([(x - 1, y), (x + 4.5, y - 4), (x + 10, y)]))]
    # balcón de madera del mirador
    s += [("wood", rect(0, 104, W, 6)), ("trunk", rect(0, 110, W, 21)),
          ("dash", lines([(0, 120, W, 120)])),
          ("wood", rect(0, 84, W, 4))]
    s += [("wood", rect(x, 88, 3, 16)) for x in range(6, W, 12)]
    # telescopio de monedas
    cx = 100
    s += [("granite", rect(cx - 2, 70, 4, 34)), ("granite", poly([(cx - 10, 104), (cx + 10, 104), (cx + 4, 98), (cx - 4, 98)])),
          ("slate", poly([(cx - 16, 62), (cx + 14, 50), (cx + 18, 58), (cx - 12, 70)])),
          ("slate", rect(cx - 6, 64, 12, 10)), ("window", circle(cx + 16, 54, 4)), ("wshade", circle(cx - 14, 66, 3))]
    return s


def hamaca():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(100, 26, 10)),
         ("sea", rect(0, 64, W, 30)), ("wave", lines([(60, 74, 74, 74), (120, 82, 136, 82), (90, 88, 104, 88)])),
         ("sand", rect(0, 92, W, 39)), ("sandshade", poly([(0, 131), (0, 120), (80, 116), (200, 122), (200, 131)]))]
    # dos palmeras
    for xb, xt, lado in ((30, 40, 1), (172, 160, -1)):
        s += [("trunk", poly([(xb - 3, 122), (xb + 3, 122), (xt + 2, 40), (xt - 2, 40)]))]
        for dx, dy in ((-24, 6), (-14, -12), (14, -12), (24, 6)):
            s.append(("leaf", poly([(xt, 40), (xt + dx * 0.6, 40 + dy - 4), (xt + dx, 40 + dy + 4), (xt + dx * 0.4, 40 + dy * 0.3 + 2)])))
        s += [("coffee", circle(xt - 3, 43, 3)), ("coffee", circle(xt + 3, 43, 3))]
    # hamaca de colores
    franjas = ["roof", "ochre", "leaf", "window", "paint"]
    pts_top = [(42 + 116 * t / 10, 66 + 36 * (1 - ((t - 5) / 5) ** 2)) for t in range(11)]
    for k, col in enumerate(franjas):
        a = [(x, y + k * 3.2) for x, y in pts_top]
        b = [(x, y + (k + 1) * 3.2) for x, y in pts_top]
        s.append((col, poly(a + b[::-1])))
    s += [("rope", lines([(36, 62, 42, 66), (40, 60, 42, 82), (164, 62, 158, 66), (160, 60, 158, 82)]))]
    return s


def loteria():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(28, 26, 9))]
    for x, y, r in ((150, 24, 5), (176, 40, 4), (60, 20, 4), (124, 14, 3)):
        s.append(("ochre", star(x, y, r * 1.6, r * 0.7)))
    s += [("ground", rect(0, 100, W, 31)), ("gline", lines([(0, 110, W, 110), (0, 122, W, 122)]))]
    # balotera
    cx, cy = 100, 62
    s += [("trunk", poly([(cx - 30, 106), (cx + 30, 106), (cx + 20, 90), (cx - 20, 90)])),
          ("wood", rect(cx - 3, 74, 6, 18)),
          ("shallow", circle(cx, cy, 30)), ("dash", lines([(cx - 30, cy, cx + 30, cy), (cx, cy - 30, cx, cy + 30)]))]
    for (dx, dy), col in zip(((-12, 14), (2, 18), (14, 10), (-18, 2), (6, 2), (-4, -10), (16, -8)),
                             ("roof", "ochre", "leaf", "window", "paint", "brick", "slate")):
        s += [(col, circle(cx + dx, cy + dy, 5)), ("wall", circle(cx + dx, cy + dy, 2))]
    s += [("ink", lines([(cx + 30, cy, cx + 40, cy), (cx + 40, cy, cx + 40, cy + 10)])), ("roof", circle(cx + 40, cy + 12, 3))]
    # billetes de lotería a los lados
    for x0, y0, col in ((10, 52, "paint"), (150, 50, "leaf"), (14, 74, "window"), (154, 72, "roof")):
        s += [("wall", rect(x0, y0, 36, 20)), (col, rect(x0, y0, 36, 5)), ("dash", lines([(x0 + 4, y0 + 10, x0 + 32, y0 + 10), (x0 + 4, y0 + 15, x0 + 24, y0 + 15)])),
              (col, circle(x0 + 30, y0 + 14, 3))]
    # balota que sale ganadora
    s += [("ochre", circle(150, 116, 7)), ("wall", circle(150, 116, 3.5)),
          ("shine", lines([(140, 106, 136, 102), (160, 106, 164, 102), (150, 104, 150, 99)]))]
    return s


def sorpresa():
    s = [("sky", rect(0, 0, W, H)), ("ground", rect(0, 104, W, 27)), ("gline", lines([(0, 114, W, 114), (0, 124, W, 124)]))]
    # confeti y estrellas que salen de la caja
    for x, y, papel in ((60, 30, "roof"), (140, 26, "ochre"), (40, 60, "leaf"), (166, 56, "paint"), (100, 18, "window"),
                        (76, 48, "ochre"), (128, 46, "roof"), (24, 34, "paint"), (178, 30, "leaf")):
        s.append((papel, star(x, y, 6, 2.8)))
    s += [(papel, rect(x, y, 4, 4)) for x, y, papel in ((50, 44, "paint"), (150, 40, "window"), (90, 34, "leaf"),
                                                         (116, 30, "paint"), (30, 76, "ochre"), (170, 74, "roof"))]
    # caja de regalo con la tapa al aire
    s += [("paint", rect(66, 66, 68, 44)), ("trim", rect(66, 66, 68, 4)),
          ("ochre", rect(94, 66, 12, 44)),
          ("paint", poly([(56, 54), (128, 40), (132, 52), (60, 66)])), ("ochre", poly([(88, 48), (100, 46), (104, 58), (92, 60)])),
          ("ochre", poly([(96, 47), (84, 32), (80, 42)])), ("ochre", poly([(96, 47), (108, 30), (112, 40)])),
          ("shine", lines([(100, 64, 100, 58), (84, 64, 78, 56), (116, 64, 122, 56)]))]
    return s


def molino(cx, base, h, r):
    return [("wall", poly([(cx - 2, base), (cx + 2, base), (cx + 1, base - h), (cx - 1, base - h)])),
            ("wall", poly([(cx, base - h), (cx - 2, base - h - r), (cx + 2, base - h - r)])),
            ("wall", poly([(cx, base - h), (cx + r * 0.87, base - h + r * 0.5 - 2), (cx + r * 0.87 - 2, base - h + r * 0.5 + 2)])),
            ("wall", poly([(cx, base - h), (cx - r * 0.87, base - h + r * 0.5 - 2), (cx - r * 0.87 + 2, base - h + r * 0.5 + 2)])),
            ("granite", circle(cx, base - h, 2.4))]


def tierra_del_futuro():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(36, 28, 11)),
         ("far", poly([(0, 66), (40, 54), (80, 62), (120, 50), (160, 60), (200, 52), (200, 96), (0, 96)])),
         ("near", poly([(0, 84), (60, 76), (120, 84), (200, 76), (200, 131), (0, 131)]))]
    s += molino(120, 80, 46, 18) + molino(156, 80, 44, 18) + molino(186, 80, 38, 14)
    # paneles solares
    for x0, y0 in ((8, 98), (52, 98), (96, 98), (30, 116), (74, 116), (118, 116), (140, 98)):
        s += [("granite", rect(x0 + 16, y0 + 6, 3, 8)),
              ("window", poly([(x0, y0 + 8), (x0 + 36, y0 + 8), (x0 + 32, y0), (x0 + 4, y0)])),
              ("dash", lines([(x0 + 12, y0 + 8, x0 + 13, y0), (x0 + 24, y0 + 8, x0 + 23, y0), (x0 + 2, y0 + 4, x0 + 34, y0 + 4)]))]
    return s


def tierra_de_la_aventura():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(170, 26, 9)),
         ("canyon", poly([(0, 131), (0, 40), (20, 44), (40, 38), (62, 54), (70, 131)])),
         ("canyon", poly([(200, 131), (200, 46), (180, 42), (160, 50), (138, 60), (130, 131)])),
         ("canyon2", poly([(0, 131), (0, 80), (30, 86), (56, 96), (74, 131)])),
         ("canyon2", poly([(200, 131), (200, 86), (170, 92), (142, 100), (126, 131)])),
         ("leaf", poly([(0, 44), (20, 40), (40, 34), (54, 46), (40, 40), (20, 46)])),
         ("sea", poly([(66, 131), (78, 100), (122, 100), (134, 131)])),
         ("wave", lines([(84, 112, 96, 112), (104, 122, 118, 122), (98, 104, 110, 104)]))]
    # parapente
    s += [("roof", raw("M60 28 Q90 10 120 28 L114 32 Q90 18 66 32 Z")),
          ("rope", lines([(64, 31, 88, 52), (116, 31, 92, 52), (78, 25, 89, 52), (102, 25, 91, 52)])),
          ("ochre", rect(87, 52, 6, 8)), ("stone", circle(90, 50, 2.6))]
    # balsa de rafting
    s += [("ochre", rrect(86, 112, 30, 9, 4)),
          ("roof", circle(94, 110, 2.6)), ("window", circle(104, 110, 2.6)),
          ("bar", lines([(92, 112, 84, 122), (106, 112, 114, 104)]))]
    # carpas
    s += [("leaf", poly([(10, 80), (24, 64), (38, 80)])), ("door", poly([(22, 80), (24, 70), (26, 80)])),
          ("paint", poly([(150, 60), (162, 46), (174, 60)])), ("door", poly([(160, 60), (162, 52), (164, 60)]))]
    return s


def tierra_de_la_frontera():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(160, 28, 10)),
         ("coffee", poly([(0, 76), (0, 52), (16, 46), (32, 54), (50, 44), (70, 52), (90, 46), (110, 54), (130, 44),
                          (150, 52), (170, 46), (186, 52), (200, 48), (200, 76)])),
         ("lake", rect(0, 74, W, 26)),
         ("wave", lines([(10, 84, 24, 84), (120, 92, 136, 92), (160, 82, 174, 82)]))]
    # puente sobre el río
    s += [("granite", rect(0, 70, W, 5)), ("gshade", rect(0, 74, W, 2))]
    s += [("granite", rect(x, 75, 5, 25)) for x in (40, 100, 160)]
    s += [("bar", lines([(0, 62, W, 62)] + [(x, 62, x, 70) for x in range(6, W, 12)]))]
    s += [("ground", rect(0, 100, W, 31)), ("gline", lines([(0, 110, W, 110), (0, 122, W, 122), (100, 100, 100, 131)]))]
    # hito de frontera con el tricolor
    cx = 100
    s += [("stone", rect(cx - 18, 112, 36, 8)), ("sshade", rect(cx + 8, 112, 10, 8)),
          ("wall", poly([(cx - 10, 112), (cx + 10, 112), (cx + 6, 40), (cx - 6, 40)])),
          ("wshade", poly([(cx + 3, 112), (cx + 10, 112), (cx + 6, 40), (cx + 3, 40)])),
          ("wall", poly([(cx - 6, 40), (cx + 6, 40), (cx, 30)])),
          ("ochre", rect(cx - 7, 56, 14, 8)), ("window", rect(cx - 7.5, 64, 15, 4)), ("roof", rect(cx - 8, 68, 16, 4))]
    # mojones de camino y palmas
    s += palma_cera(30, 104, 40) + palma_cera(172, 104, 44)
    return s


# ---------- Clásico (FA.5): Bogotá ----------
def casa_colonial(x, base, w, h, papel="wall", sombra="wshade"):
    """Casa colonial bogotana: muro encalado, teja, puerta y balcón de madera."""
    top, wx = base - h, x + w * 0.55
    return [(papel, rect(x, top, w, h)), (sombra, rect(x, top, w, 3)),
            ("roof", poly([(x - 3, top), (x + w * 0.2, top - 7), (x + w * 0.8, top - 7), (x + w + 3, top)])),
            ("door", rect(x + w * 0.12, base - 16, 9, 16)),
            ("window", rect(wx, top + 6, 10, 12)), ("trunk", rect(wx - 3, top + 13, 16, 6)),
            ("dash", lines([(wx + i * 3.3, top + 13, wx + i * 3.3, top + 19) for i in range(4)]))]


def sombrilla(cx, y, r, a, b, h):
    """Sombrilla de vendedor: gajos de dos colores sobre un palo."""
    s = [("ink", lines([(cx, y, cx, y + h)]))]
    for i in range(6):
        t = [math.pi * (i + k / 2) / 6 for k in range(3)]
        s.append((a if i % 2 == 0 else b,
                  poly([(cx, y)] + [(cx - r * math.cos(u), y - r * 0.55 * math.sin(u)) for u in t])))
    return s


def banderines(x0, x1, y, caida, n):
    """Cuerda de banderines de fiesta que cuelga entre dos puntos."""
    papeles = ["roof", "ochre", "window", "leaf", "trim"]
    alto = lambda x: y + caida * (1 - ((x - (x0 + x1) / 2) / ((x1 - x0) / 2)) ** 2)
    xs = [x0 + (x1 - x0) * i / 24 for i in range(25)]
    s = [("rope", polyline([(x, alto(x)) for x in xs]))]
    paso = (x1 - x0) / n
    for i in range(n):
        xa = x0 + paso * (i + 0.2)
        xb = xa + paso * 0.6
        s.append((papeles[i % 5], poly([(xa, alto(xa)), (xb, alto(xb)), ((xa + xb) / 2, alto((xa + xb) / 2) + 7)])))
    return s


def las_cruces():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(172, 24, 9)),
         ("far", poly([(0, 62), (28, 40), (56, 48), (90, 26), (120, 40), (150, 30), (200, 46), (200, 106), (0, 106)]))]
    # torre de la iglesia de Las Cruces
    s += [("stone", rect(28, 30, 18, 50)), ("sshade", rect(41, 30, 5, 50)), ("slate", poly([(26, 30), (37, 14), (48, 30)])),
          ("ink", lines([(37, 14, 37, 8), (34.5, 10.5, 39.5, 10.5)])), ("door", arch(33, 36, 8, 11)), ("window", circle(37, 56, 3))]
    s += casa_colonial(4, 104, 44, 30) + casa_colonial(154, 104, 42, 32, "ochre", "oshade")
    # plaza de mercado republicana
    s += [("wall", rect(54, 56, 92, 48)), ("wshade", rect(54, 56, 92, 3)),
          ("wall", poly([(78, 42), (100, 30), (122, 42)])), ("wall", rect(80, 42, 40, 14)), ("wshade", rect(78, 41, 44, 2.4)),
          ("ochre", circle(100, 49, 5)), ("ink", lines([(100, 49, 100, 45.5), (100, 49, 102.5, 50)])),
          ("roof", rect(84, 58, 32, 8)), ("trim", rect(54, 70, 92, 4)), ("door", arch(88, 74, 24, 30))]
    s += [("window", rect(x, 60, 8, 7)) for x in (60, 70, 122, 132)]
    s += [("window", arch(x, 79, 7, 14)) for x in (60, 72, 121, 133)]
    tierra_plaza(s, 104)
    # puestos de fruta
    for x0, frutas in ((14, ("paint", "roof", "ochre", "paint")), (150, ("ochre", "roof", "leaf", "ochre"))):
        s += [(p, circle(x0 + 6 + 8 * i, 110, 4)) for i, p in enumerate(frutas)]
        s += [("wood", rect(x0, 112, 36, 10)), ("dash", lines([(x0, 117, x0 + 36, 117)]))]
    return s


def fachadas(s, dfin, yt0, yt1, ysuelo, cortes, colores_izq, colores_der):
    """Fachadas de colores a cada lado de una calle en perspectiva: d es la distancia al borde (0 a dfin)."""
    for lado, colores in ((1, colores_izq), (-1, colores_der)):
        def q(d, v):
            x = d if lado == 1 else W - d
            yt, yb = yt0 + d / dfin * (yt1 - yt0), ysuelo + (dfin - d) / dfin * (H - ysuelo)
            return x, yt + v * (yb - yt)
        for (d0, d1), papel in zip(zip(cortes, cortes[1:]), colores):
            caja = lambda u0, u1, v0, v1: poly([q(d0 + u0 * (d1 - d0), v0), q(d0 + u1 * (d1 - d0), v0),
                                                q(d0 + u1 * (d1 - d0), v1), q(d0 + u0 * (d1 - d0), v1)])
            k = 1 - d0 / dfin * 0.6
            s += [(papel, caja(0, 1, 0, 1)),
                  ("roof", poly([q(d0, 0), q(d1, 0), (q(d1, 0)[0], q(d1, 0)[1] - 6 * k + 1.5), (q(d0, 0)[0], q(d0, 0)[1] - 6 * k)])),
                  ("door", caja(0.14, 0.4, 0.6, 1)), ("window", caja(0.14, 0.4, 0.18, 0.36)),
                  ("window", caja(0.56, 0.86, 0.22, 0.44)), ("trunk", caja(0.5, 0.92, 0.38, 0.5)),
                  ("window", caja(0.56, 0.86, 0.62, 0.8))]


def calle_del_embudo():
    s = [("sky", rect(0, 0, W, H)),
         ("cloud", poly([(136, 30), (142, 23), (152, 23), (158, 17), (170, 19), (176, 26), (184, 30)])),
         ("far", poly([(30, 96), (64, 54), (100, 28), (136, 54), (170, 96)])),
         ("wall", rect(94, 24, 12, 7)), ("wall", rect(97.5, 19, 5, 6)), ("roof", poly([(97, 19), (100, 15), (103, 19)]))]
    # la capillita del Chorro de Quevedo, al fondo de la calle
    s += [("wall", poly([(96, 76), (96, 68), (100, 64), (104, 68), (104, 76)])), ("door", arch(98, 67.5, 4, 5)),
          ("ochre", circle(100, 71, 1.4)), ("roof", poly([(88, 83), (100, 75), (112, 83)])),
          ("wall", rect(90, 82, 20, 16)), ("wshade", rect(105, 82, 5, 16)), ("door", arch(96.5, 88, 7, 10))]
    # la calle se abre como un embudo hacia el que mira
    s += [("stone", poly([(0, H), (W, H), (114, 98), (86, 98)]))]
    for i in range(1, 4):
        s.append(("sshade", lines([(86 + 7 * i, 98, 50 * i, H)])))
    s.append(("sshade", lines([(86 - (y - 98) / 42 * 86, y, 114 + (y - 98) / 42 * 86, y) for y in (104, 113, 125)])))
    fachadas(s, 86, 22, 78, 98, (0, 32, 60, 86), ("window", "ochre", "wall"), ("clay", "wall", "leaf"))
    return s


def san_victorino():
    s = [("sky", rect(0, 0, W, H)), ("cloud", poly([(70, 26), (76, 19), (86, 19), (92, 13), (104, 15), (110, 22), (118, 26)])),
         ("far", poly([(0, 56), (40, 36), (80, 46), (120, 30), (160, 42), (200, 34), (200, 106), (0, 106)]))]
    # almacenes: dos edificios altos y una hilera de locales con letreros
    s += [("brick", rect(0, 40, 46, 64)), ("bshade", rect(0, 40, 46, 3)),
          ("granite", rect(154, 30, 46, 74)), ("gshade", rect(190, 30, 10, 74))]
    s += [("window", rect(x, y, 7, 8)) for x in (6, 19, 32) for y in (48, 62, 76)]
    s += [("window", rect(x, y, 7, 8)) for x in (160, 173) for y in (38, 52, 66, 80)]
    s += [("wall", rect(46, 62, 108, 42)), ("wshade", rect(46, 62, 108, 3))]
    for i, papel in enumerate(("roof", "window", "leaf", "trim")):
        x = 48 + 26.5 * i
        s += [(papel, rect(x, 66, 24, 8)), ("door", rect(x + 3, 86, 18, 18)),
              (papel, poly([(x, 80), (x + 24, 80), (x + 26, 87), (x - 2, 87)])), ("wall", rect(x + 4, 80, 4, 7)), ("wall", rect(x + 16, 80, 4, 7))]
    tierra_plaza(s, 104)
    # la escultura de alas amarillas en medio de la plaza
    s += [("stone", rect(90, 100, 20, 7)), ("granite", rect(98, 50, 4, 52)),
          ("ochre", poly([(100, 54), (70, 24), (62, 48)])), ("oshade", poly([(100, 54), (62, 48), (76, 68)])),
          ("ochre", poly([(100, 54), (130, 24), (138, 48)])), ("oshade", poly([(100, 54), (138, 48), (124, 68)]))]
    # vendedores con sombrilla y bultos
    s += sombrilla(30, 96, 20, "roof", "ochre", 20) + sombrilla(170, 96, 20, "window", "wall", 20)
    s += [("wood", rect(16, 112, 28, 10)), ("paint", rrect(18, 104, 11, 9, 3)), ("leaf", rrect(30, 105, 11, 8, 3)),
          ("wood", rect(156, 112, 28, 10)), ("ochre", rrect(158, 104, 11, 9, 3)), ("clay", rrect(170, 105, 11, 8, 3))]
    return s


def la_perseverancia():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(28, 22, 9)),
         ("far", poly([(0, 44), (40, 26), (80, 36), (120, 16), (160, 30), (200, 20), (200, 100), (0, 100)])),
         ("near", poly([(0, 76), (60, 62), (120, 52), (200, 40), (200, 104), (0, 104)]))]
    # casitas escalonadas en la ladera de los cerros
    for i, (x, base) in enumerate(((112, 60), (130, 57), (148, 53), (166, 50), (184, 47))):
        papel = ("wall", "ochre", "clay", "window", "wall")[i]
        s += [(papel, rect(x, base - 12, 14, 12)), ("roof", poly([(x - 2, base - 12), (x + 7, base - 19), (x + 16, base - 12)])),
              ("door", rect(x + 3, base - 7, 4, 7)), ("window", rect(x + 9, base - 9, 3.5, 3.5))]
    s += banderines(4, 196, 26, 14, 14)
    # plaza de mercado del barrio
    s += [("roof", poly([(0, 78), (18, 64), (104, 64), (122, 78)])), ("brick", rect(6, 76, 110, 26)), ("bshade", rect(6, 76, 110, 3)),
          ("ochre", rect(42, 68, 38, 7))]
    s += [("door", arch(x, 84, 10, 18)) for x in (13, 30, 47, 64, 81, 98)]
    tierra_plaza(s, 102)
    # la chicha: múcuras de barro y totumas
    s += [("clay", vasija(150, 126, 36, 36)), ("cshade", rect(143, 92, 14, 3)),
          ("clay", vasija(182, 128, 22, 24)), ("cshade", rect(178, 106, 8, 2.4)),
          ("sand", poly([(108 + 20 * i / 10, 116 + 7 * math.sin(math.pi * i / 10)) for i in range(11)])),
          ("ochre", rect(109, 114, 18, 2.4))]
    return s


def barrio_egipto():
    s = [("sky", rect(0, 0, W, H)), ("ochre", star(162, 24, 9, 4)),
         ("far", poly([(0, 58), (36, 34), (70, 44), (104, 22), (140, 38), (170, 30), (200, 42), (200, 100), (0, 100)])),
         ("near", poly([(0, 94), (50, 84), (70, 76), (130, 76), (150, 84), (200, 94), (200, 106), (0, 106)]))]
    # iglesia de Egipto con su torre, sobre el atrio y la escalinata
    s += [("wall", rect(116, 22, 14, 54)), ("wshade", rect(126, 22, 4, 54)), ("door", arch(119.5, 28, 7, 9)),
          ("roof", poly([(114, 22), (123, 10), (132, 22)])), ("ink", lines([(123, 10, 123, 4), (120.5, 6.5, 125.5, 6.5)])),
          ("wall", poly([(76, 48), (98, 32), (120, 48)])), ("wall", rect(78, 46, 40, 30)), ("wshade", rect(76, 46, 44, 3)),
          ("window", circle(98, 41, 3)), ("door", arch(91, 56, 14, 20)), ("window", arch(81, 56, 6, 10)), ("window", arch(109, 56, 6, 10))]
    s += [("stone", rect(74 - 5 * i, 76 + 4 * i, 52 + 10 * i, 4)) for i in range(4)]
    s += casa_colonial(4, 104, 40, 28) + casa_colonial(158, 104, 38, 30, "ochre", "oshade")
    s += banderines(4, 70, 52, 8, 6) + banderines(134, 196, 52, 8, 6)
    tierra_plaza(s, 104)
    return s


def casa_tudor(x, base, w, h, papel="brick", sombra="bshade"):
    """Casa de estilo inglés: ladrillo, techo empinado, hastial con entramado y chimenea."""
    top, cx = base - h, x + w / 2
    return [("brick", rect(x + w * 0.72, top - h * 0.75, 6, 18)),
            ("rshade", poly([(x - 4, top + 2), (cx, top - h * 0.8), (x + w + 4, top + 2)])),
            ("wall", poly([(x + 7, top + 1), (cx, top - h * 0.8 + 9), (x + w - 7, top + 1)])),
            ("rope", lines([(cx, top - h * 0.8 + 9, cx, top + 1), (x + w * 0.3, top - h * 0.2, x + w * 0.7, top - h * 0.2),
                            (x + w * 0.3, top - h * 0.2, cx, top - h * 0.5), (x + w * 0.7, top - h * 0.2, cx, top - h * 0.5)])),
            (papel, rect(x, top, w, h)), (sombra, rect(x, top, w, 3)),
            ("window", rect(x + 5, top + 7, 11, 11)), ("ink", lines([(x + 10.5, top + 7, x + 10.5, top + 18), (x + 5, top + 12.5, x + 16, top + 12.5)])),
            ("door", arch(x + w - 17, base - 19, 11, 19)), ("trim", rect(x + 4, top + 18, 13, 3))]


def teusaquillo():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(178, 22, 8)),
         ("cloud", poly([(20, 28), (26, 21), (36, 21), (42, 15), (54, 17), (60, 24), (68, 28)])),
         ("far", poly([(0, 62), (40, 44), (80, 54), (120, 40), (160, 50), (200, 42), (200, 106), (0, 106)]))]
    s += casa_tudor(8, 100, 50, 32) + casa_tudor(75, 100, 52, 40, "clay", "cshade") + casa_tudor(144, 100, 50, 32)
    s += [("trunk", rect(65, 82, 3, 20)), ("leaf", circle(66.5, 76, 9)), ("trunk", rect(134, 82, 3, 20)), ("leaf", circle(135.5, 76, 9))]
    # jardín con reja blanca, andén y calle
    s += [("near", rect(0, 100, W, 12)), ("shine", lines([(x, 101, x, 110) for x in range(4, W, 6)] + [(0, 104, W, 104)])),
          ("stone", rect(0, 112, W, 9)), ("granite", rect(0, 121, W, 19)), ("ochre", rect(0, 125, W, 2))]
    return s


def camisa(cx, y, papel):
    return papel, poly([(cx - 3, y), (cx - 7, y + 3), (cx - 5, y + 6), (cx - 3.5, y + 5), (cx - 3.5, y + 14),
                        (cx + 3.5, y + 14), (cx + 3.5, y + 5), (cx + 5, y + 6), (cx + 7, y + 3), (cx + 3, y)])


def bolsa(x, y, papel):
    return [("ink", polyline([(x + 4, y), (x + 4, y - 5), (x + 10, y - 5), (x + 10, y)])), (papel, rect(x, y, 14, 16))]


def galerias():
    s = [("sky", rect(0, 0, W, H)), ("cloud", poly([(140, 22), (146, 15), (156, 15), (162, 9), (174, 11), (180, 18), (188, 22)])),
         ("far", poly([(0, 54), (40, 38), (80, 46), (120, 30), (160, 44), (200, 36), (200, 106), (0, 106)])),
         ("brick", rect(0, 56, 30, 48)), ("bshade", rect(0, 56, 30, 3)), ("wall", rect(170, 50, 30, 54)), ("wshade", rect(170, 50, 30, 3))]
    s += [("window", rect(x, y, 7, 8)) for x in (5, 17) for y in (64, 80)] + [("window", rect(x, y, 7, 8)) for x in (176, 188) for y in (58, 74)]
    # el centro comercial: fachada de vidrio, franja de colores y entrada con marquesina
    s += [("granite", rect(30, 40, 140, 64)), ("gshade", rect(30, 40, 140, 3)), ("window", rect(36, 48, 128, 26)),
          ("shine", lines([(x, 48, x, 74) for x in range(48, 164, 12)] + [(36, 61, 164, 61)]))]
    s += [(p, rect(30 + 28 * i, 34, 28, 7)) for i, p in enumerate(("roof", "ochre", "trim", "leaf", "window"))]
    s += [("door", arch(86, 80, 28, 24)), ("roof", poly([(80, 78), (120, 78), (124, 84), (76, 84)])),
          ("wall", rect(38, 82, 42, 22)), ("wall", rect(120, 82, 42, 22))]
    s += [camisa(cx, 86, p) for cx, p in ((47, "paint"), (59, "leaf"), (71, "roof"), (129, "window"), (141, "ochre"), (153, "trim"))]
    tierra_plaza(s, 104)
    for x, p in ((34, "trim"), (52, "ochre"), (136, "window"), (154, "leaf")):
        s += bolsa(x, 110 + (x % 3), p)
    return s


def la_soledad():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(28, 22, 9)),
         ("cloud", poly([(120, 26), (126, 19), (136, 19), (142, 13), (154, 15), (160, 22), (168, 26)])),
         ("far", poly([(0, 56), (40, 40), (80, 50), (120, 36), (160, 46), (200, 38), (200, 96), (0, 96)]))]
    # casas de los años cuarenta con antepecho escalonado
    for x, w, h, papel, sombra in ((0, 50, 38, "ochre", "oshade"), (50, 50, 46, "wall", "wshade"),
                                   (100, 50, 40, "window", "sea"), (150, 50, 44, "clay", "cshade")):
        top = 94 - h
        s += [(papel, poly([(x, 94), (x, top), (x + w * 0.3, top), (x + w * 0.3, top - 5), (x + w * 0.7, top - 5),
                            (x + w * 0.7, top), (x + w, top), (x + w, 94)])), (sombra, rect(x, top, w, 3)),
              ("wall" if papel != "wall" else "wshade", circle(x + w / 2, top + 6, 3.5)),
              ("door", rect(x + 6, 76, 10, 18)), ("window", rect(x + 22, top + 14, 20, 12)),
              ("dash", lines([(x + 22, top + 18, x + 42, top + 18), (x + 22, top + 22, x + 42, top + 22)]))]
    # el parque con su pila y sus urapanes
    s += [("near", rect(0, 94, W, 46)), ("stone", poly([(70, 94), (130, 94), (160, H), (40, H)]))]
    for cx in (16, 184):
        s += [("trunk", rect(cx - 2, 70, 4, 30)), ("leaf", circle(cx, 62, 14)), ("coffee", circle(cx + 6, 70, 8))]
    s += [("lake", rrect(70, 108, 60, 12, 5)), ("stone", rrect(68, 106, 64, 4, 2)), ("stone", rect(97, 92, 6, 16)),
          ("stone", rrect(88, 90, 24, 4, 2)), ("shine", lines([(92, 90, 86, 100), (108, 90, 114, 100), (100, 90, 100, 84)])),
          ("wood", rect(14, 116, 22, 4)), ("ink", lines([(17, 120, 17, 125), (33, 120, 33, 125)])),
          ("wood", rect(164, 116, 22, 4)), ("ink", lines([(167, 120, 167, 125), (183, 120, 183, 125)]))]
    return s


def casa_republicana(x, base, w, h, papel, sombra):
    """Casa republicana de dos pisos: cornisa, frontón, ventanas altas y balcón de hierro."""
    top = base - h
    s = [(papel, poly([(x + w * 0.3, top), (x + w / 2, top - 9), (x + w * 0.7, top)])),
         (papel, rect(x, top, w, h)), (sombra, rect(x, top, w, 3)), ("wall", rect(x - 2, top - 2, w + 4, 4)),
         ("wall", rect(x - 2, top + h * 0.48, w + 4, 3)), ("door", arch(x + w / 2 - 6, base - 20, 12, 20))]
    for u in (0.12, 0.66):
        s += [("window", arch(x + w * u, top + 7, 10, 15)), ("window", arch(x + w * u, base - 20, 10, 14))]
    s += [("bar", lines([(x + w * 0.08, top + 22, x + w * 0.92, top + 22)] +
                        [(x + w * 0.08 + i * w * 0.84 / 8, top + 22, x + w * 0.08 + i * w * 0.84 / 8, top + h * 0.48)
                         for i in range(9)]))]
    return s


def guayacan(cx, base, r):
    """Árbol en flor: tronco y copa de flores amarillas."""
    return [("trunk", poly([(cx - 2, base), (cx + 2, base), (cx + 1.5, base - r * 1.4), (cx - 1.5, base - r * 1.4)])),
            ("ochre", circle(cx, base - r * 1.9, r)), ("oshade", circle(cx + r * 0.45, base - r * 1.6, r * 0.5)),
            ("ochre", circle(cx - r * 0.6, base - r * 1.5, r * 0.55))]


def palermo():
    s = [("sky", rect(0, 0, W, H)), ("cloud", poly([(76, 24), (82, 17), (92, 17), (98, 11), (110, 13), (116, 20), (124, 24)])),
         ("far", poly([(0, 56), (40, 38), (80, 48), (120, 34), (160, 44), (200, 36), (200, 106), (0, 106)]))]
    s += casa_republicana(4, 104, 56, 54, "ochre", "oshade") + casa_republicana(72, 104, 56, 62, "stone", "sshade")
    s += casa_republicana(140, 104, 56, 54, "dolphin", "trim")
    s += guayacan(66, 106, 12) + guayacan(134, 106, 12)
    s += [("stone", rect(0, 104, W, 10)), ("sshade", lines([(x, 104, x, 114) for x in range(10, W, 20)])),
          ("granite", rect(0, 114, W, 26)), ("shine", lines([(x, 124, x + 10, 124) for x in range(6, W, 22)]))]
    return s


def bici(x, y, papel):
    """Bicicleta de lado: x, y es el eje de la rueda trasera."""
    return [("tire", circle(x, y, 7)), ("sky", circle(x, y, 5)), ("tire", circle(x + 22, y, 7)), ("sky", circle(x + 22, y, 5)),
            ("bar", lines([(x, y, x + 8, y - 11), (x + 8, y - 11, x + 19, y - 11), (x + 19, y - 11, x + 22, y),
                           (x, y, x + 11, y), (x + 11, y, x + 8, y - 11), (x + 11, y, x + 19, y - 11), (x + 19, y - 11, x + 18, y - 15)])),
            (papel, rrect(x + 4, y - 14, 8, 3, 1.5)), (papel, rect(x + 15, y - 16.5, 6, 2.4))]


def park_way():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(30, 24, 9)),
         ("far", poly([(0, 58), (40, 42), (80, 50), (120, 36), (160, 46), (200, 40), (200, 80), (0, 80)]))]
    s += [(p, rect(x, 60, 14, 12)) for x, p in ((0, "wall"), (14, "ochre"), (28, "clay"), (158, "wall"), (172, "window"), (186, "ochre"))]
    # el parque lineal: prado al centro, senderos a los lados, en perspectiva
    s += [("coffee", rect(0, 70, W, 70)), ("stone", poly([(0, H), (200, H), (106, 70), (94, 70)])),
          ("near", poly([(34, H), (166, H), (103, 70), (97, 70)]))]
    for t in (0.08, 0.2, 0.38, 0.62, 0.95):
        y, k = 70 + 66 * t, t
        for lado in (-1, 1):
            cx = 100 + lado * (8 + 80 * t)
            s += [("trunk", rect(cx - 0.8 - 2 * k, y - 6 - 34 * k, 1.6 + 4 * k, 6 + 34 * k)),
                  ("leaf", circle(cx, y - 8 - 38 * k, 3 + 13 * k)), ("coffee", circle(cx + lado * (1 + 4 * k), y - 4 - 30 * k, 2 + 7 * k))]
    s += bici(132, 124, "roof") + [("wood", rect(42, 112, 24, 4)), ("ink", lines([(45, 116, 45, 122), (63, 116, 63, 122)]))]
    return s


def calle_19():
    s = [("sky", rect(0, 0, W, H)), ("cloud", poly([(112, 22), (118, 15), (128, 15), (134, 9), (146, 11), (152, 18), (160, 22)])),
         ("far", poly([(0, 50), (40, 34), (80, 44), (120, 28), (160, 40), (200, 32), (200, 96), (0, 96)]))]
    for x, w, top, papel, sombra in ((0, 36, 22, "granite", "gshade"), (36, 30, 40, "brick", "bshade"), (66, 40, 16, "wall", "wshade"),
                                     (106, 30, 44, "ochre", "oshade"), (136, 38, 26, "brick", "bshade"), (174, 26, 38, "granite", "gshade")):
        s += [(papel, rect(x, top, w, 96 - top)), (sombra, rect(x + w - 5, top, 5, 96 - top))]
        s += [("window", rect(x + 4 + c * 9, y, 6, 6)) for c in range(int((w - 8) // 9)) for y in range(int(top) + 5, 88, 10)]
    s += [("stone", rect(0, 94, W, 6)), ("granite", rect(0, 100, W, 40)),
          ("shine", lines([(x, 120, x + 12, 120) for x in range(4, W, 24)]))]
    # semáforo
    s += [("granite", rect(170, 56, 4, 44)), ("coal", rect(164, 40, 16, 30)),
          ("roof", circle(172, 46, 3.5)), ("ochre", circle(172, 55, 3.5)), ("leaf", circle(172, 64, 3.5))]
    # buseta de colores
    s += [("wall", rrect(24, 72, 104, 36, 5)), ("roof", rect(24, 94, 104, 5)), ("window", rect(24, 99, 104, 3)),
          ("ochre", rect(24, 72, 104, 4))]
    s += [("window", rect(x, 79, 13, 12)) for x in (30, 47, 64, 81, 98)] + [("window", rect(114, 78, 11, 15)),
          ("ochre", rect(125, 100, 5, 4)), ("tire", circle(46, 110, 7)), ("tire", circle(108, 110, 7)),
          ("granite", circle(46, 110, 3)), ("granite", circle(108, 110, 3))]
    return s


def avenida_jimenez():
    s = [("sky", rect(0, 0, W, H)), ("cloud", poly([(120, 24), (126, 17), (136, 17), (142, 11), (154, 13), (160, 20), (168, 24)])),
         ("far", poly([(40, 96), (70, 50), (96, 26), (124, 46), (150, 60), (200, 70), (200, 98), (40, 98)])),
         ("wall", rect(90, 22, 12, 6)), ("wall", rect(93.5, 17, 5, 6)), ("roof", poly([(93, 17), (96, 13), (99, 17)]))]
    # iglesia de San Francisco: torre colonial y fachada de piedra
    s += [("stone", rect(36, 52, 34, 46)), ("sshade", rect(36, 52, 34, 3)), ("door", arch(46, 74, 14, 24)), ("window", circle(53, 63, 3.5)),
          ("stone", rect(12, 22, 24, 76)), ("sshade", rect(30, 22, 6, 76)), ("trim", rect(10, 44, 28, 3)),
          ("door", arch(18, 28, 10, 13)), ("door", arch(19, 54, 8, 12)),
          ("slate", arch(14, 10, 20, 14)), ("ink", lines([(24, 10, 24, 4), (21.5, 6.5, 26.5, 6.5)]))]
    # edificio de oficinas
    s += [("brick", rect(156, 18, 40, 80)), ("bshade", rect(188, 18, 8, 80))]
    s += [("window", rect(x, y, 6, 7)) for x in (161, 171, 181) for y in range(24, 92, 11)]
    # bus rojo articulado
    s += [("tire", circle(84, 92, 5)), ("tire", circle(122, 92, 5)), ("tire", circle(144, 92, 5)),
          ("roof", rrect(72, 66, 80, 24, 3)), ("rshade", rect(72, 84, 80, 3)), ("ink", lines([(112, 66, 112, 87)]))]
    s += [("window", rect(x, 70, 8, 9)) for x in (76, 87, 98, 116, 127, 138)]
    # el eje ambiental: plaza de ladrillo y canal de agua con piedras
    s += [("clay", rect(0, 98, W, 42)), ("cshade", lines([(0, y, W, y) for y in (122, 130)])),
          ("granite", rect(0, 104, W, 14)), ("sea", rect(0, 106, W, 10)),
          ("wave", polyline([(x, 111 + (2 if (x // 10) % 2 else -2)) for x in range(0, W + 1, 10)])),
          ("stone", rrect(40, 107, 12, 6, 3)), ("stone", rrect(118, 108, 14, 6, 3)), ("stone", rrect(170, 107, 10, 6, 3))]
    return s


def globos(x, y, colores):
    """Vendedor de globos: un ramo de globos atados a un punto."""
    pos = [(-12, -44), (-2, -50), (9, -46), (-8, -34), (5, -36), (15, -38)]
    s = [("ink", lines([(x, y, x + dx, y + dy + 5) for dx, dy in pos]))]
    return s + [(colores[i % len(colores)], circle(x + dx, y + dy, 5.5)) for i, (dx, dy) in enumerate(pos)]


def carrera_septima():
    s = [("sky", rect(0, 0, W, H)), ("cloud", poly([(130, 22), (136, 15), (146, 15), (152, 9), (164, 11), (170, 18), (178, 22)])),
         ("far", poly([(40, 84), (70, 54), (100, 44), (130, 56), (160, 84)]))]
    # Torre Colpatria al fondo de la peatonal
    s += [("granite", rect(93, 22, 14, 62)), ("gshade", rect(103, 22, 4, 62)), ("granite", rect(96, 17, 8, 5)),
          ("ink", lines([(100, 17, 100, 10)])), ("dash", lines([(93, y, 107, y) for y in range(28, 84, 5)])),
          ("roof", rect(93, 22, 14, 2)), ("window", rect(93, 40, 14, 2))]
    s += [("stone", poly([(0, H), (W, H), (118, 84), (82, 84)])),
          ("sshade", lines([(82 - (y - 84) / 56 * 82, y, 118 + (y - 84) / 56 * 82, y) for y in (90, 99, 112, 128)])),
          ("sshade", lines([(100, 84, 100, H)]))]
    fachadas(s, 82, 16, 58, 84, (0, 30, 58, 82), ("brick", "stone", "ochre"), ("wall", "brick", "clay"))
    s += sombrilla(66, 106, 16, "roof", "ochre", 18) + [("wood", rect(54, 120, 24, 8))]
    s += globos(138, 126, ("roof", "ochre", "window", "leaf", "trim"))
    return s


def junco(x, base, h, papel="leaf"):
    return [(papel, poly([(x - 1.2, base), (x + 1.2, base), (x + 2, base - h)])), ("trunk", rrect(x + 0.6, base - h * 0.8, 3, 7, 1.5))]


def niza():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(170, 22, 9)),
         ("far", poly([(0, 56), (40, 40), (80, 50), (120, 36), (160, 46), (200, 40), (200, 90), (0, 90)]))]
    for x, papel in ((4, "wall"), (40, "ochre"), (76, "clay"), (112, "wall"), (148, "window"), (184, "ochre")):
        s += [(papel, rect(x, 70, 30, 20)), ("roof", poly([(x - 2, 70), (x + 15, 60), (x + 32, 70)])),
              ("window", rect(x + 4, 75, 8, 7)), ("door", rect(x + 18, 78, 7, 12))]
    # el humedal: agua, juncos y una garza
    s += [("near", rect(0, 88, W, 8)), ("lake", poly([(0, 96), (W, 96), (W, 122), (0, 122)])),
          ("wave", lines([(x, y, x + 12, y) for x, y in ((20, 102), (60, 110), (150, 104), (110, 116), (170, 114))])),
          ("near", poly([(0, 118), (40, 114), (90, 120), (140, 116), (200, 118), (200, H), (0, H)]))]
    for x0 in (8, 52, 160):
        for i in range(5):
            s += junco(x0 + i * 5, 120 - (i % 2) * 2, 22 + (i * 7) % 12, "leaf" if i % 2 else "coffee")
    s += [("ink", lines([(118, 112, 118, 92), (124, 112, 122, 92)])),
          ("wall", poly([(106, 84), (114, 76), (130, 78), (136, 86), (126, 94), (112, 92)])),
          ("wall", poly([(128, 80), (132, 66), (128, 58), (132, 52), (136, 54), (134, 60), (138, 68), (134, 82)])),
          ("wall", circle(134, 52, 4)), ("ochre", poly([(137, 51), (149, 54), (137, 54)])), ("ink", circle(135, 51, 0.6))]
    return s


def cometa(cx, cy, r, a, b):
    """Cometa de agosto: rombo de dos colores con su cola."""
    return [("rope", polyline([(cx, cy + r * 1.4), (cx - 4, cy + r * 1.4 + 8), (cx + 3, cy + r * 1.4 + 16), (cx - 2, cy + r * 1.4 + 24)])),
            (a, poly([(cx, cy - r), (cx + r, cy), (cx, cy + r * 1.4), (cx - r, cy)])),
            (b, poly([(cx, cy - r), (cx + r, cy), (cx, cy)])), (b, poly([(cx, cy), (cx - r, cy), (cx, cy + r * 1.4)]))]


def carro(x, y, papel):
    """Carro de lado: x, y es la esquina inferior izquierda de la carrocería."""
    return [(papel, poly([(x + 10, y - 14), (x + 16, y - 22), (x + 34, y - 22), (x + 40, y - 14)])),
            ("window", poly([(x + 14, y - 14), (x + 18, y - 20), (x + 24, y - 20), (x + 24, y - 14)])),
            ("window", poly([(x + 27, y - 14), (x + 27, y - 20), (x + 33, y - 20), (x + 37, y - 14)])),
            (papel, rrect(x, y - 15, 50, 13, 4)), ("tire", circle(x + 12, y - 2, 5)), ("tire", circle(x + 38, y - 2, 5)),
            ("granite", circle(x + 12, y - 2, 2)), ("granite", circle(x + 38, y - 2, 2)), ("ochre", rect(x + 46, y - 12, 4, 3))]


def pasadena():
    s = [("sky", rect(0, 0, W, H)),
         ("far", poly([(0, 62), (40, 48), (80, 56), (120, 44), (160, 54), (200, 46), (200, 102), (0, 102)]))]
    s += [("ink", lines([(40, 42, 70, 120), (104, 30, 110, 120), (162, 38, 140, 120)]))]
    s += cometa(40, 30, 10, "roof", "ochre") + cometa(104, 18, 9, "window", "leaf") + cometa(162, 26, 11, "trim", "ochre")
    # casas con garaje
    for x, papel, sombra in ((6, "wall", "wshade"), (104, "ochre", "oshade")):
        s += [(papel, rect(x, 66, 90, 36)), (sombra, rect(x, 66, 90, 3)),
              ("roof", poly([(x - 4, 68), (x + 22, 52), (x + 68, 52), (x + 94, 68)])),
              ("granite", rect(x + 52, 78, 32, 24)), ("dash", lines([(x + 52, y, x + 84, y) for y in (84, 90, 96)])),
              ("window", rect(x + 8, 72, 14, 11)), ("door", rect(x + 28, 82, 12, 20)), ("window", rect(x + 60, 70, 16, 5))]
    s += [("near", rect(0, 102, W, 10)), ("stone", rect(0, 112, W, 6)), ("granite", rect(0, 118, W, 22)),
          ("shine", lines([(x, 128, x + 10, 128) for x in range(6, W, 22)]))]
    s += carro(18, 128, "roof") + carro(126, 129, "window")
    return s


def cedro(cx, base, r):
    """Cedro andino: tronco y copa ancha por capas."""
    return [("trunk", poly([(cx - 3, base), (cx + 3, base), (cx + 2, base - r * 1.6), (cx - 2, base - r * 1.6)])),
            ("coffee", circle(cx - r * 0.6, base - r * 1.7, r * 0.7)), ("coffee", circle(cx + r * 0.6, base - r * 1.7, r * 0.7)),
            ("leaf", circle(cx, base - r * 2.1, r * 0.8)), ("leaf", circle(cx - r * 0.5, base - r * 1.5, r * 0.45))]


def cedritos():
    s = [("sky", rect(0, 0, W, H)), ("cloud", poly([(80, 22), (86, 15), (96, 15), (102, 9), (114, 11), (120, 18), (128, 22)])),
         ("far", poly([(0, 54), (40, 36), (80, 46), (120, 32), (160, 44), (200, 34), (200, 106), (0, 106)]))]
    # edificios de ladrillo con balcones
    for x, w, top in ((8, 70, 34), (122, 70, 24)):
        s += [("brick", rect(x, top, w, 104 - top)), ("bshade", rect(x + w - 6, top, 6, 104 - top)), ("wall", rect(x - 2, top - 3, w + 4, 4))]
        for y in range(top + 6, 84, 14):
            s += [("window", rect(x + 6, y, 12, 9)), ("window", rect(x + 28, y, 12, 9)), ("window", rect(x + 50, y, 12, 9)),
                  ("bar", lines([(x + 4, y + 9, x + 64, y + 9)])), ("wall", rect(x + 4, y + 9, 60, 1.6))]
        s += [("door", rect(x + w / 2 - 7, 90, 14, 14))]
    s += [("near", rect(0, 104, W, 12)), ("stone", rect(0, 116, W, 24)), ("sshade", lines([(x, 116, x, H) for x in range(12, W, 24)]))]
    s += cedro(100, 116, 18) + cedro(14, 120, 11) + cedro(188, 120, 11)
    return s


def santa_barbara():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(28, 22, 8)),
         ("far", poly([(0, 56), (40, 40), (80, 50), (120, 34), (160, 46), (200, 38), (200, 104), (0, 104)]))]
    # el centro comercial moderno detrás
    s += [("granite", rect(110, 22, 70, 42)), ("window", rect(114, 26, 62, 34)),
          ("shine", lines([(x, 26, x, 60) for x in range(124, 176, 10)] + [(114, 43, 176, 43)])),
          ("granite", rect(36, 36, 46, 28)), ("gshade", rect(76, 36, 6, 28))]
    s += [("window", rect(x, 42, 8, 6)) for x in (42, 54, 66)] + [("window", rect(x, 53, 8, 6)) for x in (42, 54, 66)]
    # la casa de la hacienda con su corredor de arcos
    s += [("roof", poly([(10, 68), (30, 54), (170, 54), (190, 68)])), ("rshade", rect(10, 66, 180, 3)),
          ("wall", rect(18, 68, 164, 34)), ("wshade", rect(18, 68, 164, 3))]
    s += [("door", arch(x, 76, 11, 26)) for x in range(25, 176, 16)]
    s += palma_cera(14, 110, 62) + palma_cera(186, 110, 62)
    tierra_plaza(s, 102)
    return s


def rosa(cx, cy, r, papel, sombra):
    """Rosa vista de frente: dos hojas y pétalos en capas."""
    return [("leaf", poly([(cx, cy), (cx - r * 1.8, cy + r * 0.6), (cx - r * 0.6, cy + r * 1.2)])),
            ("leaf", poly([(cx, cy), (cx + r * 1.8, cy + r * 0.4), (cx + r * 0.8, cy + r * 1.2)])),
            *[(papel, circle(cx + r * 0.5 * math.cos(math.radians(a)), cy + r * 0.5 * math.sin(math.radians(a)), r * 0.52))
              for a in range(-90, 270, 72)],
            (sombra, circle(cx, cy, r * 0.45)), (papel, circle(cx + r * 0.08, cy - r * 0.05, r * 0.22))]


def rosales():
    s = [("sky", rect(0, 0, W, H)), ("cloud", poly([(20, 26), (26, 19), (36, 19), (42, 13), (54, 15), (60, 22), (68, 26)])),
         ("far", poly([(0, 44), (50, 30), (100, 22), (150, 14), (200, 10), (200, 104), (0, 104)])),
         ("near", poly([(0, 96), (60, 84), (120, 66), (200, 50), (200, 116), (0, 116)]))]
    # edificios de ladrillo escalonados, subiendo el cerro
    for x0, base in ((20, 104), (108, 92)):
        for i in range(4):
            x, top = x0 + 18 * i, base - 14 * (i + 1)
            s += [("brick", rect(x, top, 18, base - top)), ("leaf", rect(x, top - 3, 18, 3))]
            s += [("window", rect(x + 3, y, 12, 6)) for y in range(int(top) + 4, int(base) - 6, 9)]
    # rosales
    s += [("coffee", poly([(0, 140), (0, 112), (20, 106), (60, 110), (100, 104), (140, 110), (180, 104), (200, 108), (200, 140)]))]
    for i, (cx, cy) in enumerate(((16, 114), (44, 124), (72, 112), (100, 124), (128, 112), (156, 124), (184, 114))):
        s += rosa(cx, cy, 10, *(("roof", "rshade"), ("dolphin", "trim"), ("ochre", "oshade"), ("roof", "rshade"))[i % 4])
    return s


def el_chico():
    s = [("sky", rect(0, 0, W, H)),
         ("far", poly([(0, 50), (40, 34), (80, 42), (120, 28), (160, 38), (200, 30), (200, 100), (0, 100)]))]
    # torres de vidrio alrededor del parque
    for x, w, top in ((0, 40, 18), (40, 28, 40), (136, 30, 34), (166, 34, 14)):
        s += [("granite", rect(x, top, w, 100 - top)), ("window", rect(x + 3, top + 4, w - 6, 92 - top)),
              ("shine", lines([(x + 3, y, x + w - 3, y) for y in range(int(top) + 12, 92, 8)]))]
    # terrazas de restaurantes con parasoles y luces colgantes
    s += [("wall", rect(68, 70, 68, 30)), ("wshade", rect(68, 70, 68, 3)), ("trim", rect(68, 74, 68, 5))]
    s += [("door", rect(x, 84, 10, 16)) for x in (74, 92, 110)] + [("window", rect(124, 84, 8, 10))]
    s += [("rope", polyline([(x, 40 + 6 * math.sin(math.pi * (x - 40) / 60)) for x in range(40, 161, 6)]))]
    s += [("ochre", circle(x, 40 + 6 * math.sin(math.pi * (x - 40) / 60) + 2, 2)) for x in range(46, 160, 12)]
    s += [("near", rect(0, 100, W, 40)), ("stone", poly([(80, 100), (120, 100), (140, H), (60, H)]))]
    s += sombrilla(30, 106, 16, "wall", "roof", 14) + sombrilla(170, 106, 16, "wall", "window", 14)
    s += [("wood", rect(20, 118, 20, 4)), ("wood", rect(160, 118, 20, 4)),
          ("trunk", rect(98, 104, 4, 16)), ("leaf", circle(100, 98, 10)), ("coffee", circle(105, 103, 6))]
    return s


def carpa(x, base, w, techo, cosas):
    """Carpa del mercado de pulgas: techo de lona, patas y mesa con cosas."""
    s = [("ink", lines([(x, base - 18, x, base), (x + w, base - 18, x + w, base)])),
         ("wood", rect(x, base - 8, w, 4))]
    s += [(p, rect(x + 3 + i * (w - 6) / len(cosas), base - 13, (w - 6) / len(cosas) - 2, 5)) for i, p in enumerate(cosas)]
    s += [(techo, poly([(x - 3, base - 18), (x + w / 2, base - 28), (x + w + 3, base - 18)])),
          (techo, rect(x - 3, base - 19, w + 6, 4)), ("roof", rect(x - 3, base - 16, w + 6, 2))]
    return s


def usaquen():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(172, 22, 8)),
         ("far", poly([(0, 40), (40, 24), (80, 34), (120, 18), (160, 30), (200, 22), (200, 100), (0, 100)])),
         ("near", poly([(0, 74), (50, 64), (100, 68), (150, 58), (200, 66), (200, 100), (0, 100)]))]
    # iglesia de Santa Bárbara de Usaquén: fachada blanca y torre con reloj
    s += [("wall", rect(112, 26, 18, 66)), ("wshade", rect(125, 26, 5, 66)), ("ochre", circle(121, 40, 5)),
          ("ink", lines([(121, 40, 121, 37), (121, 40, 123.5, 41)])), ("door", arch(117, 50, 8, 11)),
          ("roof", poly([(110, 26), (121, 14), (132, 26)])), ("ink", lines([(121, 14, 121, 8), (118.5, 10.5, 123.5, 10.5)])),
          ("wall", poly([(66, 52), (89, 36), (112, 52)])), ("wall", rect(68, 50, 44, 42)), ("wshade", rect(66, 50, 46, 3)),
          ("window", circle(89, 46, 3)), ("door", arch(81, 66, 16, 26)), ("window", arch(72, 64, 5, 9)), ("window", arch(101, 64, 5, 9))]
    s += casa_colonial(4, 96, 44, 28, "ochre", "oshade") + casa_colonial(150, 96, 46, 30)
    tierra_plaza(s, 96)
    s += carpa(12, 128, 34, "wall", ("roof", "window", "ochre")) + carpa(84, 126, 34, "dolphin", ("leaf", "clay"))
    s += carpa(154, 128, 34, "wall", ("trim", "ochre", "window"))
    return s


def vagon(x, base, w, h, papel, techo="roof"):
    """Vagón de pasajeros de lado: base es el nivel de los rieles; ruedas antes que la carrocería."""
    top = base - 5 - h
    s = [("tire", circle(x + 10, base - 5, 5)), ("tire", circle(x + w - 10, base - 5, 5)),
         (papel, rect(x, top, w, h)), (techo, rect(x - 2, top - 3, w + 4, 4)), ("trim", rect(x, top + h - 5, w, 2.5))]
    n = max(1, int((w - 6) // 12))
    paso = (w - 8) / n
    s += [("window", rect(x + 5 + i * paso, top + 3, paso - 4, 6)) for i in range(n)]
    return s


def locomotora(x, base, w, papel):
    """Locomotora diésel de lado, mirando a la derecha: base es el nivel de los rieles."""
    top = base - 23
    s = [("tire", circle(x + c, base - 5, 5)) for c in (10, 22, w - 22, w - 10)]
    s += [(papel, poly([(x, base - 5), (x, top), (x + w - 10, top), (x + w, top + 8), (x + w, base - 5)])),
          ("coal", rect(x + 2, top - 3, w - 16, 3)), ("ochre", rect(x, base - 12, w, 3)),
          ("window", poly([(x + w - 20, top + 3), (x + w - 11, top + 3), (x + w - 4, top + 9), (x + w - 20, top + 9)])),
          ("dash", lines([(x + 6 + 5 * i, top + 4, x + 6 + 5 * i, top + 10) for i in range(5)]))]
    return s


def rieles(s, y):
    """Balasto, traviesas y dos rieles; y es el riel de arriba."""
    s += [("dash", lines([(x, y, x + 6, y + 6) for x in range(0, W, 10)])), ("bar", lines([(0, y, W, y), (0, y + 5, W, y + 5)]))]


def estacion_de_la_sabana():
    s = [("sky", rect(0, 0, W, H)), ("cloud", poly([(140, 24), (146, 17), (156, 17), (162, 11), (174, 13), (180, 20), (188, 24)])),
         ("far", poly([(0, 62), (40, 48), (80, 58), (120, 44), (160, 54), (200, 46), (200, 96), (0, 96)]))]
    # alas laterales
    for x in (12, 150):
        s += [("granite", rect(x, 54, 38, 38)), ("gshade", rect(x, 54, 38, 3)), ("wall", rect(x - 2, 51, 42, 4)),
              ("door", arch(x + 14, 78, 10, 14))]
        s += [("window", arch(x + 6 + 11 * i, 60, 6, 12)) for i in range(3)]
    # cuerpo neoclásico: columnas, frontón con reloj y bandera
    s += [("granite", rect(50, 44, 100, 48))]
    s += [("door", arch(60.5 + 17 * i, 60, 7, 26)) for i in range(5)]
    for i in range(6):
        cx = 52 + 17 * i
        s += [("wall", rect(cx, 46, 7, 40)), ("wall", rect(cx - 1.5, 46, 10, 3)), ("wall", rect(cx - 1.5, 84, 10, 3))]
    s += [("wall", rect(46, 38, 108, 8)), ("wshade", rect(46, 44, 108, 2)),
          ("wall", poly([(46, 38), (100, 22), (154, 38)])), ("wshade", poly([(62, 36), (100, 26), (138, 36)])),
          ("ochre", circle(100, 32, 4.5)), ("ink", lines([(100, 32, 100, 29), (100, 32, 102.5, 33)])),
          ("ink", lines([(100, 22, 100, 10)])), ("ochre", rect(100, 10, 14, 4)), ("window", rect(100, 14, 14, 2)), ("roof", rect(100, 16, 14, 2))]
    # andén, rieles y el tren de vapor de la Sabana
    s += [("ground", rect(0, 100, W, 40)), ("stone", rect(0, 92, W, 8)), ("sshade", rect(0, 98, W, 2))]
    rieles(s, 124)
    s += [("cloud", circle(34, 70, 6)), ("cloud", circle(26, 60, 8)), ("cloud", circle(14, 48, 9)),
          ("roof", circle(30, 117, 7)), ("roof", circle(48, 117, 7)), ("roof", circle(64, 118, 6)),
          ("coal", rect(20, 98, 46, 15)), ("roof", rect(20, 96, 46, 3)),
          ("coal", rect(28, 82, 8, 16)), ("coal", poly([(24, 77), (40, 77), (36, 83), (28, 83)])),
          ("roof", rect(58, 86, 22, 27)), ("window", rect(62, 90, 10, 8)), ("coal", rect(56, 82, 26, 5)),
          ("ochre", circle(17, 103, 4)), ("trim", poly([(10, 123), (20, 109), (20, 123)])),
          ("tire", circle(30, 117, 2)), ("tire", circle(48, 117, 2)), ("tire", circle(64, 118, 2)),
          ("bar", lines([(30, 117, 64, 118)]))]
    s += vagon(88, 124, 52, 16, "ochre") + vagon(146, 124, 50, 16, "leaf")
    return s


def eucalipto(cx, base, h):
    """Eucalipto de la Sabana: tronco alto y copa angosta por capas."""
    return [("trunk", poly([(cx - 2.5, base), (cx + 2.5, base), (cx + 1.5, base - h), (cx - 1.5, base - h)])),
            ("coffee", circle(cx + h * 0.1, base - h * 0.62, h * 0.16)), ("leaf", circle(cx - h * 0.1, base - h * 0.78, h * 0.17)),
            ("coffee", circle(cx + h * 0.06, base - h * 0.95, h * 0.15)), ("leaf", circle(cx - h * 0.04, base - h * 1.08, h * 0.11))]


def estacion_del_norte():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(176, 22, 8)),
         ("far", poly([(0, 64), (50, 52), (100, 60), (150, 48), (200, 58), (200, 90), (0, 90)])),
         ("near", rect(0, 84, W, 30))]
    # estación de pueblo: muro amarillo, teja y letrero
    s += [("ochre", rect(60, 60, 70, 28)), ("oshade", rect(124, 60, 6, 28)), ("trim", rect(60, 64, 70, 3)),
          ("roof", poly([(52, 62), (95, 42), (138, 62)])), ("rshade", rect(52, 60, 86, 3)),
          ("wall", rect(82, 50, 26, 8)), ("ink", lines([(86, 54, 104, 54)])),
          ("door", rect(68, 70, 10, 18)), ("door", rect(112, 70, 10, 18)),
          ("window", rect(85, 70, 8, 10)), ("window", rect(97, 70, 8, 10))]
    s += eucalipto(16, 94, 60) + eucalipto(36, 92, 46) + eucalipto(146, 90, 52)
    s += vaca(178, 100) + vaca(40, 106)
    # el tren de la Sabana hacia el norte
    s += [("stone", rect(0, 112, W, 28)), ("sshade", rect(0, 112, W, 2))]
    rieles(s, 124)
    s += vagon(8, 124, 50, 14, "wall") + vagon(64, 124, 50, 14, "wall") + locomotora(120, 124, 64, "roof")
    return s


def estacion_del_sur():
    s = [("sky", rect(0, 0, W, H)), ("cloud", poly([(14, 26), (20, 19), (30, 19), (36, 13), (48, 15), (54, 22), (62, 26)])),
         # el cañón del Salto del Tequendama
         ("far", poly([(0, 30), (40, 26), (80, 30), (80, 110), (0, 110)])),
         ("far", poly([(120, 30), (200, 30), (200, 110), (120, 110)])),
         ("canyon2", poly([(62, 44), (80, 34), (80, 110), (68, 110)])), ("canyon2", poly([(120, 34), (138, 44), (132, 110), (120, 110)])),
         ("coffee", poly([(0, 70), (40, 62), (70, 74), (76, 106), (0, 106)])),
         ("coffee", poly([(124, 78), (160, 66), (200, 72), (200, 106), (128, 106)]))]
    # la cascada y su neblina
    s += [("sea", poly([(78, 28), (122, 28), (121, 34), (79, 34)])), ("cloud", poly([(80, 33), (120, 33), (126, 100), (74, 100)])),
          ("fall", lines([(87, 38, 84, 92), (96, 36, 95, 94), (104, 36, 105, 94), (113, 38, 116, 92)])),
          ("cloud", circle(78, 86, 10)), ("cloud", circle(122, 86, 10)), ("cloud", circle(100, 82, 11))]
    # la Casa del Salto al borde del abismo
    s += [("wall", rect(148, 18, 34, 12)), ("slate", poly([(145, 18), (151, 10), (179, 10), (185, 18)])),
          ("window", rect(152, 21, 6, 5)), ("window", rect(172, 21, 6, 5)), ("door", arch(161, 21, 8, 9))]
    # viaducto de piedra con el tren
    s += [("stone", rect(0, 112, W, 19)), ("sshade", rect(0, 112, W, 3))]
    s += [("sea", arch(x, 118, 26, 13)) for x in (6, 46, 86, 126, 166)]
    s += [("wave", lines([(x + 5, 127, x + 21, 127) for x in (6, 46, 86, 126, 166)])),
          ("roof", rect(0, 107, W, 5)), ("bar", lines([(0, 107, W, 107)]))]
    s += vagon(12, 107, 48, 12, "ochre") + vagon(66, 107, 48, 12, "ochre") + locomotora(120, 107, 62, "slate")
    return s


def estacion_del_oriente():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(30, 24, 8)),
         ("far", poly([(0, 108), (0, 84), (50, 64), (100, 40), (124, 32), (162, 32), (200, 46), (200, 108)])),
         ("coffee", poly([(0, 108), (0, 96), (40, 84), (80, 76), (120, 70), (160, 64), (200, 70), (200, 108)]))]
    # santuario de Monserrate en la cima
    s += [("wall", rect(126, 22, 34, 10)), ("roof", poly([(124, 22), (143, 16), (162, 22)])),
          ("wall", rect(139, 14, 9, 18)), ("wshade", rect(145, 14, 3, 18)), ("roof", poly([(137.5, 14), (143.5, 9.5), (149.5, 14)])),
          ("door", arch(140.5, 24, 6, 8)), ("window", arch(130, 25, 4, 5)), ("window", arch(153, 25, 4, 5))]
    # carril del funicular subiendo el cerro
    x0, y0, x1, y1 = 24, 108, 128, 34
    largo = math.hypot(x1 - x0, y1 - y0)
    nx, ny = (y0 - y1) / largo, (x1 - x0) / largo  # normal hacia abajo a la derecha
    def en(t, d=0):
        return x0 + (x1 - x0) * t + nx * d, y0 + (y1 - y0) * t + ny * d
    s += [("stone", poly([en(0, -4), en(1, -4), en(1, 4), en(0, 4)])),
          ("dash", lines([(*en(t / 20, -4), *en(t / 20, 4)) for t in range(21)])),
          ("bar", lines([(*en(0, -2.5), *en(1, -2.5)), (*en(0, 2.5), *en(1, 2.5))]))]
    # dos vagones escalonados que se cruzan
    for t, papel in ((0.42, "roof"), (0.8, "window")):
        (ax, ay), (bx, by) = en(t - 0.1, -3), en(t + 0.1, -3)
        s += [(papel, poly([(ax, ay), (bx, by), (bx, by - 13), (ax, ay - 13)])),
              ("ochre", poly([(ax, ay - 13), (bx, by - 13), (bx, by - 10.5), (ax, ay - 10.5)]))]
        dx, dy = (bx - ax) / 3, (by - ay) / 3
        for i in range(3):
            px, py = ax + dx * i + 1.5, ay + dy * i
            s.append(("wall" if papel == "window" else "window",
                      poly([(px, py - 9), (px + dx - 3, py + dy - 9), (px + dx - 3, py + dy - 4), (px, py - 4)])))
    # estación de abajo y las casas de La Candelaria
    s += [("wall", rect(4, 88, 46, 20)), ("roof", poly([(0, 90), (27, 78), (54, 90)])), ("rshade", rect(0, 88, 54, 3)),
          ("door", arch(20, 94, 14, 14)), ("window", rect(8, 94, 8, 8)), ("window", rect(38, 94, 8, 8))]
    tierra_plaza(s, 108)
    s += casa_colonial(100, 110, 44, 26, "ochre", "oshade") + casa_colonial(150, 110, 46, 30)
    s += sombrilla(66, 112, 14, "wall", "roof", 16)
    return s


def zona_t():
    s = [("night", rect(0, 0, W, H)), ("cloud", circle(28, 24, 9)), ("night", circle(33, 21, 8)),
         ("ochre", star(70, 18, 5, 2.2)), ("ochre", star(118, 26, 4.5, 2)), ("ochre", star(172, 15, 5, 2.2))]
    # torres de vidrio con ventanas encendidas
    for x, w, top in ((0, 34, 30), (40, 26, 42), (136, 26, 38), (166, 34, 26)):
        s += [("door", rect(x, top, w, 90 - top))]
        for j, y in enumerate(range(top + 5, 56, 8)):
            s += [("ochre" if (i + j) % 3 else "window", rect(x + 4 + i * 8, y, 5, 4)) for i in range(int((w - 6) // 8))]
    # restaurantes y bares al fondo de la T, con avisos de neón
    for i, papel in enumerate(("trim", "turq", "ochre", "dolphin", "clay")):
        x = 40 * i
        s += [(papel, rect(x, 58, 40, 32)), ("night", rrect(x + 6, 62, 28, 8, 3)),
              ("glyph", polyline([(x + 10 + 3 * k, 66 + (1.5 if k % 2 else -1.5)) for k in range(7)])),
              ("roof" if i % 2 else "leaf", rect(x, 72, 40, 4)), ("ochre", rect(x + 5, 78, 20, 12)), ("door", rect(x + 28, 78, 8, 12))]
    # la T peatonal
    s += [("stone", rect(0, 90, W, 50)), ("sand", rect(0, 92, W, 10)), ("sand", poly([(86, 102), (114, 102), (142, H), (58, H)])),
          ("gline", lines([(0, 97, W, 97), (100, 102, 100, H), (82, 112, 118, 112), (76, 124, 124, 124)]))]
    # luces colgantes entre dos postes
    s += [("ink", lines([(30, 60, 30, 120), (170, 60, 170, 120)])),
          ("rope", polyline([(x, 62 + 10 * math.sin(math.pi * (x - 30) / 140)) for x in range(30, 171, 7)]))]
    s += [("ochre", circle(x, 64 + 10 * math.sin(math.pi * (x - 30) / 140), 2.2)) for x in range(37, 170, 14)]
    # materas con árboles a los lados
    for cx in (16, 184):
        s += [("trunk", rect(cx - 2, 100, 4, 14)), ("leaf", circle(cx, 98, 10)), ("coffee", circle(cx + 5, 103, 6)),
              ("wood", rect(cx - 12, 112, 24, 12)), ("cshade", rect(cx - 12, 112, 24, 3))]
    return s


def rayo(cx, cy, k):
    """Rayo de la energía: zigzag de seis puntas centrado en cx, cy."""
    return poly([(cx + 2 * k, cy - 6 * k), (cx - 3 * k, cy + k), (cx, cy + k), (cx - 2 * k, cy + 6 * k), (cx + 3 * k, cy - k), (cx, cy - k)])


def torre_electrica(cx, base, h):
    """Torre de alta tensión de celosía; devuelve (formas, puntas de las crucetas)."""
    a, b = base - h, base - h + 12
    s = [("bar", lines([(cx - 9, base, cx - 2, a), (cx + 9, base, cx + 2, a), (cx - 12, a + 5, cx + 12, a + 5), (cx - 10, b, cx + 10, b)]
                       + [(cx - 9 + 7 * t, base - h * t, cx + 9 - 7 * (t + 0.25), base - h * (t + 0.25)) for t in (0, 0.25, 0.5)]
                       + [(cx + 9 - 7 * t, base - h * t, cx - 9 + 7 * (t + 0.25), base - h * (t + 0.25)) for t in (0, 0.25, 0.5)]))]
    return s, [(cx - 12, a + 5), (cx + 12, a + 5), (cx - 10, b), (cx + 10, b)]


def energia_bogota():
    s = [("sky", rect(0, 0, W, H)), ("cloud", poly([(130, 22), (136, 15), (146, 15), (152, 9.5), (164, 11), (170, 18), (178, 22)])),
         ("far", poly([(0, 46), (40, 34), (80, 40), (120, 30), (160, 40), (200, 34), (200, 100), (0, 100)])),
         ("lake", rect(50, 42, 100, 10))]
    # cerros a lado y lado de la represa
    s += [("coffee", poly([(0, 52), (40, 44), (68, 48), (66, 100), (0, 100)])),
          ("coffee", poly([(132, 48), (160, 42), (200, 50), (200, 100), (134, 100)]))]
    # la represa con su rebosadero
    s += [("granite", poly([(62, 48), (138, 48), (134, 98), (66, 98)])), ("gshade", rect(62, 48, 76, 4)),
          ("dash", lines([(64, y, 136, y) for y in (62, 74, 86)])),
          ("cloud", poly([(92, 52), (108, 52), (110, 98), (90, 98)])),
          ("fall", lines([(95, 54, 94, 96), (100, 54, 100, 96), (105, 54, 106, 96)]))]
    s += [("near", rect(0, 98, W, 42)), ("sea", poly([(90, 98), (110, 98), (128, H), (72, H)])),
          ("wave", lines([(88, 112, 100, 112), (98, 124, 112, 124)]))]
    s += eucalipto(18, 108, 50) + eucalipto(42, 104, 36)
    # casa de máquinas con el rayo, y la torre de alta tensión que se lleva la luz
    s += [("brick", rect(122, 80, 44, 28)), ("bshade", rect(160, 80, 6, 28)), ("slate", poly([(118, 82), (126, 72), (162, 72), (170, 82)])),
          ("door", arch(127, 92, 9, 16)), ("door", arch(152, 92, 9, 16)), ("coal", circle(144, 95, 8)), ("bolt", rayo(144, 95, 1.1))]
    t1, p1 = torre_electrica(186, 124, 66)
    s += t1
    cables = [((166, 76), p1[0]), ((166, 80), p1[2]), (p1[1], (200, p1[1][1] + 6)), (p1[3], (200, p1[3][1] + 6))]
    s += [("ink", polyline([(a[0] + (b[0] - a[0]) * k / 10, a[1] + (b[1] - a[1]) * k / 10 + 5 * math.sin(math.pi * k / 10)) for k in range(11)]))
          for a, b in cables]
    return s


def acueducto_bogota():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(174, 22, 8)),
         ("far", poly([(0, 44), (40, 30), (80, 40), (120, 28), (160, 38), (200, 30), (200, 96), (0, 96)]))]
    # el tanque del agua en el cerro, con la tubería que baja
    s += [("bar", lines([(30, 58, 26, 76), (48, 58, 52, 76), (30, 58, 52, 76), (48, 58, 26, 76)])),
          ("granite", rect(24, 34, 30, 24)), ("gshade", rect(48, 34, 6, 24)), ("window", rect(24, 44, 30, 4)),
          ("granite", poly([(22, 34), (39, 24), (56, 34)]))]
    s += casa_colonial(0, 96, 48, 30) + casa_colonial(152, 96, 48, 28, "ochre", "oshade")
    tierra_plaza(s, 96)
    # la pila con el Mono en lo alto
    s += [("stone", poly([(54, 118), (64, 104), (136, 104), (146, 118), (146, 126), (54, 126)])), ("sshade", rect(54, 118, 92, 3)),
          ("shallow", poly([(66, 106), (134, 106), (140, 116), (60, 116)])),
          ("stone", rect(94, 60, 12, 48)), ("sshade", rect(102, 60, 4, 48)),
          ("stone", poly([(82, 72), (118, 72), (112, 80), (88, 80)])), ("shallow", rect(84, 72, 32, 2.5)),
          ("fall", polyline([(86, 76), (78, 82), (72, 92), (70, 106)])), ("fall", polyline([(114, 76), (122, 82), (128, 92), (130, 106)])),
          ("stone", rect(90, 56, 20, 5)),
          ("granite", poly([(94, 56), (106, 56), (104, 44), (96, 44)])), ("granite", circle(100, 39, 5.5)),
          ("ink", lines([(106, 50, 110, 42), (107.5, 45, 112, 47)]))]
    s += [("clay", vasija(30, 128, 16, 18)), ("clay", vasija(170, 128, 16, 18))]
    return s


DOLAR = ((14.6, 12.6), (14, 11.4, 12, 11.4), (9.5, 11.4, 9.5, 13.3), (9.5, 15.1, 12, 15.1), (14.5, 15.1, 14.5, 16.9),
         (14.5, 18.8, 12, 18.8), (9.9, 18.8, 9.3, 17.6))


def signo_peso(cx, cy, k):
    """El $ del ícono de impuesto (lienzo 24) llevado a cx, cy y multiplicado por k."""
    def q(x, y):
        return f"{f(cx + (x - 12) * k)} {f(cy + (y - 15.1) * k)}"
    d = "M" + q(*DOLAR[0]) + "".join("Q" + q(a, b) + " " + q(c, e) for a, b, c, e in DOLAR[1:])
    return raw(d + "M" + q(12, 10) + "L" + q(12, 20.2))


def pila_monedas(x, base, n):
    """Pila de n monedas de oro vistas de lado."""
    return [("ochre", rrect(x - 10, base - 5 * (i + 1), 20, 5, 2)) for i in range(n)]


def contribuciones():
    s = [("sky", rect(0, 0, W, H)), ("cloud", poly([(20, 24), (26, 17), (36, 17), (42, 11), (54, 13), (60, 20), (68, 24)])),
         ("far", poly([(0, 48), (40, 36), (80, 44), (120, 32), (160, 42), (200, 36), (200, 96), (0, 96)]))]
    # la casa del recaudo: muro encalado, portada de piedra y letrero
    s += [("roof", poly([(12, 44), (24, 34), (176, 34), (188, 44)])), ("rshade", rect(12, 42, 176, 3)),
          ("wall", rect(18, 44, 164, 52)), ("wshade", rect(18, 44, 164, 3)),
          ("stone", rect(80, 50, 40, 46)), ("sshade", rect(80, 50, 40, 3)), ("door", arch(88, 64, 24, 32)),
          ("ochre", rrect(84, 52, 32, 9, 3)), ("dollar", signo_peso(100, 56.5, 0.7))]
    for x in (26, 52, 134):
        s += [("window", rect(x, 56, 14, 20)), ("bar", lines([(x + 4.7, 56, x + 4.7, 76), (x + 9.3, 56, x + 9.3, 76)])),
              ("wood", rect(x - 2, 76, 18, 3))]
    tierra_plaza(s, 96)
    # pilas de monedas y una moneda grande
    s += pila_monedas(24, 128, 4) + pila_monedas(46, 128, 6) + pila_monedas(68, 128, 3)
    s += [("ochre", circle(90, 118, 10)), ("oshade", circle(90, 118, 7)), ("dollar", signo_peso(90, 118, 1.1))]
    # el recibo y el sello que cae sobre él
    s += [("wall", poly([(112, 106), (176, 102), (180, 128), (116, 131)])),
          ("dash", lines([(120, 112, 150, 110.5), (120, 118, 146, 116.8), (120, 124, 140, 123.2)])),
          ("stamp", circle(160, 118, 8)), ("stamp", signo_peso(160, 118, 1.0)),
          ("wood", rect(155, 66, 10, 14)), ("trunk", circle(160, 62, 9)), ("roof", rect(142, 80, 36, 11)), ("rshade", rect(142, 88, 36, 3)),
          ("coal", rect(145, 91, 30, 4)), ("ink", lines([(136, 84, 128, 84), (136, 92, 126, 95), (184, 84, 192, 84), (184, 92, 194, 95)]))]
    return s


def impuesto_de_lujo():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(28, 20, 8))]
    # la joyería con su corona en lo alto
    s += [("door", rect(12, 30, 176, 72)), ("ochre", rect(8, 28, 184, 5)),
          ("ochre", poly([(84, 28), (82, 13), (92, 21), (100, 11), (108, 21), (118, 13), (116, 28)])),
          ("roof", circle(92, 24, 2.6)), ("window", circle(100, 22, 2.6)), ("leaf", circle(108, 24, 2.6)),
          ("trim", poly([(12, 36)] + [(12 + 11 * i + 5.5, 44 if i % 2 == 0 else 36) for i in range(16)] + [(188, 36)])),
          ("coal", rect(28, 48, 144, 46)), ("ochre", rect(26, 46, 148, 3))]
    # lingotes de oro a los lados
    for x, y in ((38, 92), (60, 92), (49, 85), (140, 92), (162, 92), (151, 85)):
        s += [("ochre", poly([(x - 10, y), (x + 10, y), (x + 7, y - 7), (x - 7, y - 7)])), ("shine", lines([(x - 4, y - 4, x + 2, y - 4)]))]
    # el anillo con su diamante
    s += [("ochre", circle(100, 80, 13)), ("coal", circle(100, 80, 8.5)), ("oshade", poly([(94, 66), (106, 66), (103, 72), (97, 72)])),
          ("shallow", poly([(90, 54), (110, 54), (118, 61), (82, 61)])), ("turq", poly([(82, 61), (118, 61), (100, 77)])),
          ("shine", lines([(96, 54, 92, 61), (104, 54, 108, 61), (92, 61, 100, 77), (108, 61, 100, 77)])),
          ("cloud", star(124, 54, 7, 2.2)), ("cloud", star(76, 70, 6, 2))]
    # andén y carros de lujo
    s += [("stone", rect(0, 102, W, 10)), ("sshade", rect(0, 110, W, 2)), ("granite", rect(0, 112, W, 28)),
          ("glyph", lines([(x, 122, x + 12, 122) for x in range(6, W, 28)]))]
    s += carro(22, 130, "roof") + carro(128, 130, "ochre")
    return s


def chiva(x, base, papel):
    """Chiva de lado, mirando a la derecha: carrocería pintada, bancas abiertas y parrilla con bultos."""
    s = [("tire", circle(x + 22, base - 8, 8)), ("tire", circle(x + 110, base - 8, 8)),
         ("granite", circle(x + 22, base - 8, 3)), ("granite", circle(x + 110, base - 8, 3)),
         ("roof", poly([(x + 108, base - 36), (x + 126, base - 32), (x + 132, base - 12), (x + 108, base - 12)])),
         ("granite", rect(x + 128, base - 28, 5, 14)), ("ochre", circle(x + 128, base - 32, 3.5)),
         ("window", poly([(x + 110, base - 50), (x + 118, base - 50), (x + 124, base - 36), (x + 110, base - 36)])),
         (papel, rect(x, base - 34, 112, 22)), ("roof", rect(x, base - 16, 112, 3)),
         ("wood", rect(x, base - 56, 112, 22))]
    s += [("coal", rect(x + 4 + 15 * i, base - 52, 10, 16)) for i in range(7)]
    s += [(("trim", "window", "leaf")[i % 3], poly([(x + 4 + 12 * i, base - 20), (x + 10 + 12 * i, base - 31), (x + 16 + 12 * i, base - 20)]))
          for i in range(9)]
    s += [("roof", rect(x - 2, base - 60, 116, 5)), ("bar", lines([(x + 2, base - 66, x + 108, base - 66)] + [(x + 4 + 26 * i, base - 66, x + 4 + 26 * i, base - 60) for i in range(5)])),
          ("ochre", rrect(x + 10, base - 74, 20, 12, 3)), ("clay", rrect(x + 36, base - 72, 16, 10, 3)), ("leaf", rrect(x + 58, base - 76, 24, 14, 3)),
          ("window", rrect(x + 86, base - 72, 16, 10, 3))]
    return s


def salida_bogota():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(166, 54, 8)),
         ("far", poly([(0, 62), (40, 48), (80, 56), (120, 44), (160, 54), (200, 46), (200, 100), (0, 100)])),
         ("near", rect(0, 92, W, 10)), ("stone", rect(0, 100, W, 6)), ("granite", rect(0, 106, W, 34)),
         ("glyph", lines([(x, 126, x + 14, 126) for x in range(4, W, 30)]))]
    # pancarta de salida a cuadros entre dos postes
    s += [("granite", rect(12, 22, 6, 84)), ("granite", rect(182, 22, 6, 84)), ("wall", rect(18, 22, 164, 14))]
    s += [("coal", rect(18 + 8 * i, 22 + 7 * (i % 2), 8, 7)) for i in range(21)]
    s += chiva(30, 126, "ochre")
    return s


def carcel_bogota():
    s = [("sky", rect(0, 0, W, H)), ("cloud", poly([(80, 22), (86, 15), (96, 15), (102, 9.5), (114, 11), (120, 18), (128, 22)])),
         ("far", poly([(0, 56), (40, 40), (80, 50), (120, 36), (160, 46), (200, 38), (200, 98), (0, 98)]))]
    # el Panóptico: muralla de piedra, almenas y torreones
    s += [("stone", rect(10, 44, 180, 54)), ("sshade", rect(10, 44, 180, 3))]
    s += [("stone", rect(x, 37, 7, 7)) for x in range(36, 160, 12)]
    for x in (6, 166):
        s += [("stone", rect(x, 32, 28, 66)), ("sshade", rect(x + 22, 32, 6, 66))]
        s += [("stone", rect(x + i * 8, 25, 5, 7)) for i in range(4)]
        s += [("coal", rect(x + 11, 46, 5, 12)), ("coal", rect(x + 11, 70, 5, 12))]
    for x in (44, 64, 124, 144):
        s += [("coal", rect(x, 56, 12, 16)), ("bar", lines([(x + 4, 56, x + 4, 72), (x + 8, 56, x + 8, 72)]))]
    # el portón con el preso de rayas detrás de la reja
    s += [("coal", arch(82, 58, 36, 40)), ("wall", rrect(88, 80, 24, 18, 4)),
          ("ink", lines([(88, 85, 112, 85), (88, 90, 112, 90), (88, 95, 112, 95)])), ("skin", circle(100, 72, 8.5)),
          ("ink", lines([(96.5, 70, 96.5, 71.5), (103.5, 70, 103.5, 71.5)])), ("ink", polyline([(96, 77), (100, 75.5), (104, 77)])),
          ("skin", circle(90, 74, 3.5)), ("skin", circle(110, 74, 3.5)),
          ("bar", lines([(x, 60, x, 98) for x in range(87, 116, 6)] + [(82, 78, 118, 78)]))]
    tierra_plaza(s, 98)
    return s


def vayase_a_la_carcel():
    s = [("sky", rect(0, 0, W, H)),
         ("far", poly([(0, 60), (40, 46), (80, 54), (120, 40), (160, 50), (200, 30), (200, 100), (0, 100)]))]
    # el Panóptico allá en el cerro y la flecha que lo señala
    s += [("stone", rect(164, 44, 32, 16)), ("sshade", rect(164, 44, 32, 2))]
    s += [("stone", rect(x, 40, 4, 4)) for x in range(165, 196, 7)]
    s += [("coal", arch(176, 50, 8, 10)), ("bar", lines([(178.5, 51, 178.5, 60), (181.5, 51, 181.5, 60)])),
          ("roof", poly([(104, 36), (138, 36), (138, 26), (158, 44), (138, 62), (138, 52), (104, 52)])), ("shine", lines([(110, 44, 132, 44)]))]
    s += [("near", rect(0, 92, W, 10)), ("stone", rect(0, 100, W, 6)), ("granite", rect(0, 106, W, 34)),
          ("glyph", lines([(x, 126, x + 14, 126) for x in range(70, W, 30)]))]
    # la patrulla con su sirena
    s += carro(120, 128, "wall") + [("leaf", rect(120, 118, 50, 4)), ("roof", rrect(138, 102, 7, 4, 1.5)), ("window", rrect(145, 102, 7, 4, 1.5)),
                                    ("stamp", lines([(136, 100, 132, 96), (154, 100, 158, 96), (145, 98, 145, 94)]))]
    # el policía pitando y señalando
    cx, b = 52, 128
    s += [("coal", rect(cx - 9, b - 24, 8, 24)), ("coal", rect(cx + 1, b - 24, 8, 24)),
          ("leaf", poly([(cx - 10, b - 48), (cx - 18, b - 30), (cx - 13, b - 28), (cx - 6, b - 42)])),
          ("leaf", rrect(cx - 12, b - 52, 24, 30, 5)), ("ochre", rect(cx - 12, b - 42, 24, 3.5)), ("coal", rect(cx - 12, b - 28, 24, 3)),
          ("leaf", poly([(cx + 8, b - 50), (cx + 36, b - 62), (cx + 38, b - 56), (cx + 10, b - 42)])), ("skin", circle(cx + 38, b - 59, 4.5)),
          ("skin", circle(cx, b - 61, 9)), ("leaf", poly([(cx - 10, b - 64), (cx - 8, b - 74), (cx + 8, b - 74), (cx + 10, b - 64)])),
          ("coal", rect(cx - 11, b - 66, 24, 3)), ("ochre", circle(cx, b - 70, 2.4)),
          ("ink", lines([(cx + 3, b - 62, cx + 3, b - 60.5)])), ("granite", rect(cx + 6, b - 58, 7, 3)),
          ("ink", lines([(cx + 16, b - 64, cx + 21, b - 68), (cx + 17, b - 59, cx + 23, b - 59)]))]
    return s


def parada_libre_bogota():
    s = [("sky", rect(0, 0, W, H)), ("sun", circle(176, 22, 8)),
         ("far", poly([(0, 56), (40, 42), (80, 50), (120, 38), (160, 48), (200, 40), (200, 90), (0, 90)])),
         ("near", rect(0, 84, W, 56))]
    s += cometa(124, 26, 9, "roof", "ochre")
    # el lago con su bote
    s += [("lake", poly([(60, 88), (150, 86), (172, 94), (146, 102), (74, 102), (50, 95)])),
          ("wave", lines([(70, 96, 82, 96), (130, 92, 142, 92)])), ("roof", poly([(128, 91), (156, 91), (152, 97), (132, 97)]))]
    # el letrero de la P
    s += [("granite", rect(32, 56, 5, 50)), ("window", rrect(18, 26, 33, 32, 5)), ("bigglyph", raw("M29 51V33H36Q42.5 33 42.5 39.5Q42.5 46 36 46H29"))]
    # banca bajo el árbol
    s += saman(92, 118, 60, 46)
    s += [("ink", lines([(72, 112, 72, 124), (108, 112, 108, 124), (74, 104, 72, 112), (106, 104, 108, 112)])),
          ("wood", rect(70, 102, 40, 4)), ("wood", rect(68, 110, 44, 4))]
    # carrito de helados con su sombrilla
    s += sombrilla(156, 92, 18, "wall", "roof", 18)
    s += [("tire", circle(144, 126, 5)), ("tire", circle(168, 126, 5)), ("wall", rrect(138, 108, 36, 16, 3)), ("dolphin", rect(138, 114, 36, 4))]
    return s


def arca_comunal():
    s = [("sky", rect(0, 0, W, H)), ("cloud", poly([(140, 22), (146, 15), (156, 15), (162, 9.5), (174, 11), (180, 18), (188, 22)])),
         ("far", poly([(0, 50), (40, 36), (80, 44), (120, 32), (160, 42), (200, 36), (200, 96), (0, 96)]))]
    # el salón comunal de fiesta
    s += [("roof", poly([(10, 46), (22, 36), (178, 36), (190, 46)])), ("rshade", rect(10, 44, 180, 3)),
          ("ochre", rect(16, 46, 168, 50)), ("oshade", rect(16, 46, 168, 3)),
          ("door", rect(26, 62, 16, 34)), ("door", rect(158, 62, 16, 34)), ("window", rect(50, 60, 14, 14)), ("window", rect(136, 60, 14, 14))]
    s += banderines(16, 184, 50, 10, 14)
    tierra_plaza(s, 96)
    # el cofre abierto rebosando monedas
    s += [("wood", poly([(62, 98), (138, 98), (144, 66), (68, 66)])), ("trunk", poly([(68, 66), (144, 66), (143, 71), (67, 71)])),
          ("leaf", poly([(74, 92), (96, 76), (100, 82), (80, 98)])), ("leaf", poly([(110, 80), (132, 90), (128, 97), (106, 88)])),
          ("ochre", poly([(62, 100), (70, 88), (86, 82), (100, 80), (114, 82), (130, 88), (138, 100)]))]
    for cx, cy in ((80, 92), (100, 86), (120, 92)):
        s += [("ochre", circle(cx, cy, 8)), ("oshade", circle(cx, cy, 5.5)), ("dollar", signo_peso(cx, cy, 0.75))]
    s += [("wood", rect(60, 98, 80, 30)), ("trunk", rect(60, 106, 80, 4)), ("trunk", rect(60, 120, 80, 4)),
          ("oshade", rect(60, 98, 6, 30)), ("oshade", rect(134, 98, 6, 30)), ("ochre", rect(94, 100, 12, 12)), ("coal", rect(99, 104, 2, 5)),
          ("cloud", star(146, 80, 7, 2.2)), ("cloud", star(56, 84, 6, 2))]
    for cx, cy in ((40, 122), (162, 124)):
        s += [("ochre", circle(cx, cy, 8)), ("oshade", circle(cx, cy, 5.5)), ("dollar", signo_peso(cx, cy, 0.75))]
    return s


def interrogacion(x, y, k):
    """Signo de pregunta grueso con contorno: x, y es la esquina de arriba a la izquierda."""
    d = (f"M{f(x)} {f(y + 12 * k)}Q{f(x)} {f(y)} {f(x + 13 * k)} {f(y)}Q{f(x + 26 * k)} {f(y)} {f(x + 26 * k)} {f(y + 12 * k)}"
         f"Q{f(x + 26 * k)} {f(y + 20 * k)} {f(x + 14 * k)} {f(y + 25 * k)}V{f(y + 33 * k)}")
    return [("qblack", raw(d)), ("qyellow", raw(d)), ("ochre", circle(x + 14 * k, y + 44 * k, 5.5 * k))]


def casualidad_bogota():
    s = [("sky", rect(0, 0, W, H)),
         ("far", poly([(0, 54), (40, 40), (80, 48), (120, 34), (160, 44), (200, 38), (200, 100), (0, 100)]))]
    s += carpa(146, 104, 44, "trim", ("ochre", "window")) + carpa(10, 104, 40, "turq", ("roof", "leaf"))
    tierra_plaza(s, 104)
    # la ruleta de feria
    cx, cy, r = 104, 64, 38
    s += [("wood", poly([(92, 98), (116, 98), (128, 128), (80, 128)])), ("coal", circle(cx, cy, r + 4))]
    papeles = ("roof", "ochre", "window", "leaf", "trim", "turq")
    for i in range(12):
        a0, a1 = math.radians(30 * i - 90), math.radians(30 * i - 60)
        arco = [(cx + r * math.cos(a0 + (a1 - a0) * k / 4), cy + r * math.sin(a0 + (a1 - a0) * k / 4)) for k in range(5)]
        s.append((papeles[i % 6], poly([(cx, cy)] + arco)))
    s += [("ochre", circle(cx, cy, 6)), ("roof", poly([(98, 17), (110, 17), (104, 29)]))]
    s += interrogacion(18, 18, 1.1) + interrogacion(160, 30, 0.8)
    return s


LUGARES = {"villa_de_leyva": villa_de_leyva, "cartagena": cartagena, "chapinero": chapinero,
           "topaga": topaga, "mongua": mongua, "raquira": raquira,
           "guane": guane, "zapatoca": zapatoca, "san_gil": san_gil, "barichara": barichara,
           "marsella": marsella, "filandia": filandia, "salento": salento, "manizales": manizales,
           "jerico": jerico, "jardin": jardin, "guatape": guatape, "rionegro": rionegro,
           "cienaga": cienaga, "mompox": mompox, "santa_marta": santa_marta, "barranquilla": barranquilla,
           "tulua": tulua, "buga": buga, "palmira": palmira, "cali": cali,
           "puerto_gaitan": puerto_gaitan, "yopal": yopal, "leticia": leticia, "villavicencio": villavicencio,
           "san_andres": san_andres, "medellin": medellin, "bogota": bogota,
           "estacion_santa_fe": estacion_santa_fe, "mirador": mirador, "hamaca": hamaca, "loteria": loteria,
           "sorpresa": sorpresa, "tierra_del_futuro": tierra_del_futuro, "tierra_de_la_aventura": tierra_de_la_aventura,
           "tierra_de_la_frontera": tierra_de_la_frontera,
           "las_cruces": las_cruces, "calle_del_embudo": calle_del_embudo, "san_victorino": san_victorino,
           "la_perseverancia": la_perseverancia, "barrio_egipto": barrio_egipto, "teusaquillo": teusaquillo,
           "galerias": galerias, "la_soledad": la_soledad, "palermo": palermo, "park_way": park_way,
           "calle_19": calle_19, "avenida_jimenez": avenida_jimenez, "carrera_septima": carrera_septima, "niza": niza,
           "pasadena": pasadena, "cedritos": cedritos, "santa_barbara": santa_barbara, "rosales": rosales,
           "el_chico": el_chico, "usaquen": usaquen,
           "estacion_de_la_sabana": estacion_de_la_sabana, "estacion_del_norte": estacion_del_norte,
           "estacion_del_sur": estacion_del_sur, "estacion_del_oriente": estacion_del_oriente, "zona_t": zona_t,
           "empresa_de_energia": energia_bogota, "acueducto": acueducto_bogota, "contribuciones": contribuciones,
           "impuesto_de_lujo": impuesto_de_lujo, "salida": salida_bogota, "carcel": carcel_bogota,
           "vayase_a_la_carcel": vayase_a_la_carcel, "parada_libre": parada_libre_bogota, "arca_comunal": arca_comunal,
           "casualidad": casualidad_bogota}


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


def kotlin_arte():
    filas = "\n".join(f'    "{n}" to R.drawable.arte_{n},' for n in sorted(LUGARES))
    return ("// Generado por arte/arte.py: no editar a mano.\n"
            "package com.jacck.mono.board\n\n"
            "import com.jacck.mono.R\n\n"
            "/** Arte de cada lugar por su clave (`artKey` del nombre), FA (D-28). */\n"
            "internal val ArteLugares: Map<String, Int> = mapOf(\n"
            f"{filas}\n)\n")


def salidas():
    yield KT_ARTE, kotlin_arte()
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
    print(f"arte: {len(LUGARES)} lugares y {len(ICONOS)} íconos -> {(len(LUGARES) + len(ICONOS)) * 2 + 1} archivos"
          f" ({len(viejos)} cambiados)")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
