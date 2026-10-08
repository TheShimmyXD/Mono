"""Íconos (FA.2): lienzo 24 x 24 sin franjas; salen como `ic_<id>`."""
from .formas import arch, circle, lines, poly, raw, rect, rrect, star
from .lugares_4 import hamaca, loteria, mirador, sorpresa


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


def enlace():
    """Lo que juega en el otro teléfono (F5, en lugar del emoji de señal): cuatro barras que suben."""
    return [("sea", rrect(2 + 5.3 * i, 21.5 - h, 4, h, 1)) for i, h in enumerate((5.5, 9.5, 14, 19))]


def propio():
    """Tablero propio (F4.4, en lugar del emoji de estrella)."""
    return [("bolt", star(12, 12.6, 10.5, 4.6))]


def maquina():
    """Jugador que juega solo (F5.8, D-52): cabeza de robot con antena."""
    return [("slate", rect(2, 11, 2.5, 5)), ("slate", rect(19.5, 11, 2.5, 5)),
            ("ink", lines([(12, 7, 12, 4)])), ("roof", circle(12, 3, 2)),
            ("stone", rrect(4, 7, 16, 14, 3.5)),
            ("bolt", circle(8.7, 12.5, 2.1)), ("bolt", circle(15.3, 12.5, 2.1)),
            ("pip", rect(8, 16.5, 8, 1.8))]


ICONOS = {"casa": casa, "hotel": hotel, **{f"dado_{n}": (lambda n=n: dado(n)) for n in range(1, 7)},
          "salida": salida, "carcel": carcel, "vayase_carcel": vayase_carcel, "estacion": estacion,
          "energia": energia, "acueducto": acueducto, "impuesto": impuesto, "casualidad": casualidad,
          "arca": arca, "loteria": loteria, "sorpresa": sorpresa, "parada_libre": parada_libre,
          "mirador": mirador, "hamaca": hamaca, "turno": turno,
          "enlace": enlace, "propio": propio, "maquina": maquina}
ICONO_ANCHO = 1.1
