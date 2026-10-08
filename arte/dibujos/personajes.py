"""Personajes (FB.3): lienzo 48 x 48 sin fondo; salen como `pj_<id>`."""
from .formas import circle, f, lines, poly, raw, rect, rrect


PW = 48


def elipse(cx, cy, rx, ry):
    d = f"M{f(cx - rx)} {f(cy)}A{f(rx)} {f(ry)} 0 1 1 {f(cx + rx)} {f(cy)}A{f(rx)} {f(ry)} 0 1 1 {f(cx - rx)} {f(cy)}Z"
    return d, (cx - rx, cy - ry, cx + rx, cy + ry)


def brillos(*pts):
    """Brillo de los ojos: un punto blanco (trazo redondo de largo casi cero)."""
    return lines([(x, y, x + 0.05, y) for x, y in pts])


def pj_mono():
    return [("trunk", poly([(19.5, 11), (23, 4.5), (25, 9), (28, 5), (29, 11)])),
            ("trunk", circle(9, 23, 6)), ("trunk", circle(39, 23, 6)),
            ("skin", circle(9, 23, 3)), ("skin", circle(39, 23, 3)),
            ("trunk", circle(24, 24, 15)),
            ("skin", raw("M24 16Q17 11.5 13.8 17.5Q11.5 23 15.2 27.5Q13.5 34 17.5 37Q24 41 30.5 37Q34.5 34 32.8 27.5"
                         "Q36.5 23 34.2 17.5Q31 11.5 24 16Z")),
            ("pip", circle(19, 22.5, 2.3)), ("pip", circle(29, 22.5, 2.3)), ("shine", brillos((18.2, 21.6), (28.2, 21.6))),
            ("ink", lines([(22.4, 28.6, 22.4, 29.2), (25.6, 28.6, 25.6, 29.2)])),
            ("ink", raw("M19 32.5Q24 36.8 29 32.5"))]


def pj_chiva():
    return [("tire", rrect(9, 36, 8, 9, 2.5)), ("tire", rrect(31, 36, 8, 9, 2.5)),
            ("clay", rrect(9, 5, 9, 8, 2)), ("leaf", rrect(19.5, 3.5, 9, 9.5, 2)), ("window", rrect(30, 6, 9, 7, 2)),
            ("roof", rect(5.5, 12, 37, 4.5)),
            ("ochre", rect(7.5, 16.5, 33, 9)), ("window", rect(10.5, 17.8, 27, 6.4)),
            ("bar", lines([(24, 17.8, 24, 24.2)])),
            ("trim", rrect(7.5, 25.5, 33, 12, 2)),
            ("ochre", circle(13, 30.5, 3.2)), ("ochre", circle(35, 30.5, 3.2)),
            ("granite", rect(19, 27.5, 10, 8)), ("ink", lines([(21.5, 28.5, 21.5, 34.5), (24, 28.5, 24, 34.5), (26.5, 28.5, 26.5, 34.5)])),
            ("granite", rrect(5, 36.5, 38, 4, 1.5))]


def pj_sombrero():
    return [("palm", elipse(24, 32, 21, 8)),
            ("ink", raw("M7 32A17 5.6 0 0 0 41 32M11 32A13 3.9 0 0 0 37 32")),
            ("palm", raw("M14 32V19Q14 10 24 10Q34 10 34 19V32Q24 35.5 14 32Z")),
            ("coal", raw("M14 20.5Q24 23 34 20.5V24Q24 26.5 14 24Z")),
            ("coal", raw("M14 27Q24 29.5 34 27V30.5Q24 33 14 30.5Z")),
            ("ink", raw("M16 14.5Q24 17 32 14.5"))]


def pj_colibri():
    return [("coffee", poly([(12, 32), (5.5, 43), (10, 43), (15, 34)])), ("leaf", poly([(10, 30), (2.5, 38.5), (6.5, 40.5), (13, 33)])),
            ("leaf", raw("M10.5 31Q16 23.5 26 21.5Q31.5 21.5 31.5 27Q30.5 33.5 22 35.5Q13.5 36.5 10.5 31Z")),
            ("coal", poly([(37, 19), (46.5, 16.5), (37.5, 21.8)])),
            ("leaf", circle(32, 21, 6.5)),
            ("trim", raw("M27.2 23.6Q31.5 30.5 37.2 24.2Q32 26.5 27.2 23.6Z")),
            ("turq", raw("M18 26Q12.5 10 23.5 3.5Q28 14 25 26Z")),
            ("pip", circle(33.6, 19.4, 1.7))]


def pj_perro():
    return [("roof", raw("M13.5 33Q24 39.5 34.5 33L24 46.5Z")),
            ("wood", raw("M24 8Q37 8 37 22Q37 35 24 38Q11 35 11 22Q11 8 24 8Z")),
            ("trunk", circle(29.5, 18.5, 4.8)),
            ("trunk", raw("M13 11Q4 12.5 5.5 27.5Q9 30.5 13.5 24Z")), ("trunk", raw("M35 11Q44 12.5 42.5 27.5Q39 30.5 34.5 24Z")),
            ("palm", raw("M17 29.5Q17 23 24 23Q31 23 31 29.5Q31 36 24 37Q17 36 17 29.5Z")),
            ("roof", raw("M22 32.5Q24 38 26 32.5Z")),
            ("pip", raw("M21 25.3H27Q27 28.8 24 29.4Q21 28.8 21 25.3Z")),
            ("ink", raw("M24 29.4V31.6M20.5 31Q22.5 33.6 24 31.6Q25.5 33.6 27.5 31")),
            ("pip", circle(19, 19, 2.1)), ("pip", circle(29.5, 19, 2.1)), ("shine", brillos((18.3, 18.2), (28.8, 18.2)))]


def pj_arepa():
    return [("sandshade", circle(24, 27.5, 17)), ("sand", circle(24, 25, 17)),
            ("gline", lines([(12, 16, 17, 13), (12.5, 37, 16, 39), (36, 37.5, 39, 34), (11, 25, 11.5, 29)])),
            ("ochre", poly([(29, 9.5), (37.5, 13.5), (33, 19.5), (24.5, 15.5)])),
            ("dolphin", circle(15.5, 30.5, 2.4)), ("dolphin", circle(32.5, 30.5, 2.4)),
            ("pip", circle(18.5, 25.5, 2.2)), ("pip", circle(29.5, 25.5, 2.2)), ("shine", brillos((17.8, 24.7), (28.8, 24.7))),
            ("ink", raw("M19.5 31Q24 35.5 28.5 31"))]


def pj_tinto():
    return [("ochre", elipse(23, 40, 19, 5)),
            ("window", raw("M34 21Q43 21 42 28.5Q41 35 33 34V30.5Q38 31 38.3 28.2Q38.6 24.6 34 25Z")),
            ("window", raw("M9 18H37V27Q37 39.5 23 39.5Q9 39.5 9 27Z")),
            ("ochre", rect(9, 23, 28, 4)), ("roof", poly([(23, 30), (25.5, 33), (23, 36), (20.5, 33)])),
            ("trunk", elipse(23, 18, 14, 3.2)),
            ("ink", raw("M18 12.5Q15 10 18 7Q21 4 18 1.8M28 12.5Q25 10 28 7Q31 4 28 1.8"))]


def pj_guacamaya():
    return [("window", poly([(14, 34), (5, 47), (11, 47), (20, 36)])), ("roof", poly([(17.5, 35), (14, 47), (19.5, 47), (23, 37)])),
            ("roof", raw("M14 36Q12 24 20 18Q26 14 30 18Q34 24 30 32Q26 40 18 40Q15 39 14 36Z")),
            ("ochre", raw("M15 26Q22 22 28.5 26.5Q26.5 34 18 38.5Q14 33 15 26Z")),
            ("window", raw("M16 32.5Q22 31.5 26.3 31Q24 36.5 18 38.5Q15.5 35.5 16 32.5Z")),
            ("roof", circle(27, 14, 9)),
            ("cloud", raw("M28.2 9.2Q34 8.6 35.3 13.8Q34.5 18.6 29.2 18.2Q26.2 14 28.2 9.2Z")),
            ("palm", raw("M33.5 9.8Q42.5 8.8 43 16Q42.5 21.5 39.2 22.5Q40 16.2 34 16Z")),
            ("coal", raw("M34 16Q39.4 16 39.2 20.5Q36.2 21.6 34 19.2Z")),
            ("pip", circle(30.6, 12.4, 1.7))]


PERSONAJES = {"mono": pj_mono, "chiva": pj_chiva, "sombrero": pj_sombrero, "colibri": pj_colibri,
              "perro": pj_perro, "arepa": pj_arepa, "tinto": pj_tinto, "guacamaya": pj_guacamaya}
PJ_ANCHO = 1.4
