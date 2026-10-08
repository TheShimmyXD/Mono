"""Lugares de Tío Rico (2 de 4): una función por lugar y sus ayudantes."""
from .formas import arch, circle, gothic, H, lines, poly, polyline, rect, rrect, W
from .lugares_1 import arbol_musgo, tierra_plaza


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
