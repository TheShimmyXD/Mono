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


LUGARES = {"villa_de_leyva": villa_de_leyva, "cartagena": cartagena, "chapinero": chapinero,
           "topaga": topaga, "mongua": mongua, "raquira": raquira,
           "guane": guane, "zapatoca": zapatoca, "san_gil": san_gil, "barichara": barichara,
           "marsella": marsella, "filandia": filandia, "salento": salento, "manizales": manizales,
           "jerico": jerico, "jardin": jardin, "guatape": guatape, "rionegro": rionegro,
           "cienaga": cienaga, "mompox": mompox, "santa_marta": santa_marta, "barranquilla": barranquilla,
           "tulua": tulua, "buga": buga, "palmira": palmira, "cali": cali,
           "puerto_gaitan": puerto_gaitan, "yopal": yopal, "leticia": leticia, "villavicencio": villavicencio,
           "san_andres": san_andres, "medellin": medellin, "bogota": bogota}


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
