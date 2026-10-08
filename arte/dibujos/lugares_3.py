"""Lugares de Tío Rico (3 de 4): una función por lugar y sus ayudantes."""
from .formas import arch, circle, H, lines, poly, rect, rrect, star, W
from .lugares_1 import tierra_plaza
from .lugares_2 import palma_cera


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
