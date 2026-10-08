"""Lugares de Tío Rico (1 de 4): una función por lugar y sus ayudantes."""
import math

from .formas import arch, circle, f, gothic, H, lines, poly, polyline, rect, W


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
