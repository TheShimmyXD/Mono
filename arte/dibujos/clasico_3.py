"""Lugares del Clásico (FA.5, 3 de 3): una función por lugar."""
import math

from .clasico_1 import banderines, casa_colonial, sombrilla
from .clasico_2 import carpa, carro, cometa, eucalipto
from .formas import arch, circle, f, H, lines, poly, polyline, raw, rect, rrect, star, W
from .lugares_1 import tierra_plaza, vasija
from .lugares_3 import saman


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
