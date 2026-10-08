"""Lugares de Tío Rico (4 de 4): una función por lugar y sus ayudantes."""
from .formas import arch, circle, franjas, gothic, H, lines, poly, raw, rect, rrect, star, W
from .lugares_1 import tierra_plaza
from .lugares_2 import palma_cera


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
