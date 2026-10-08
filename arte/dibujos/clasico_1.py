"""Lugares del Clásico (FA.5, 1 de 3): una función por lugar."""
import math

from .formas import arch, circle, H, lines, poly, polyline, rect, rrect, star, W
from .lugares_1 import tierra_plaza, vasija


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
