"""Lugares del Clásico (FA.5, 2 de 3): una función por lugar."""
import math

from .clasico_1 import casa_colonial, fachadas, sombrilla
from .formas import arch, circle, H, lines, poly, polyline, rect, rrect, W
from .lugares_1 import tierra_plaza
from .lugares_2 import palma_cera
from .lugares_3 import vaca


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
