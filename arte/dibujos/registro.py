"""Mapa id -> dibujo de cada lugar (`LUGARES`), de Tío Rico y del Clásico."""
from .clasico_1 import (
    barrio_egipto, calle_19, calle_del_embudo, galerias, la_perseverancia, la_soledad, las_cruces,
    palermo, park_way, san_victorino, teusaquillo,
)
from .clasico_2 import (
    avenida_jimenez, carrera_septima, cedritos, el_chico, estacion_de_la_sabana, estacion_del_norte,
    estacion_del_sur, niza, pasadena, rosales, santa_barbara, usaquen,
)
from .clasico_3 import (
    acueducto_bogota, arca_comunal, carcel_bogota, casualidad_bogota, contribuciones, energia_bogota,
    estacion_del_oriente, impuesto_de_lujo, parada_libre_bogota, salida_bogota, vayase_a_la_carcel,
    zona_t,
)
from .lugares_1 import cartagena, chapinero, guane, mongua, raquira, topaga, villa_de_leyva, zapatoca
from .lugares_2 import (
    barichara, cienaga, filandia, guatape, jardin, jerico, manizales, marsella, rionegro, salento,
    san_gil,
)
from .lugares_3 import (
    barranquilla, buga, cali, leticia, mompox, palmira, puerto_gaitan, santa_marta, tulua, yopal,
)
from .lugares_4 import (
    bogota, estacion_santa_fe, hamaca, loteria, medellin, mirador, san_andres, sorpresa,
    tierra_de_la_aventura, tierra_de_la_frontera, tierra_del_futuro, villavicencio,
)


LUGARES = {"villa_de_leyva": villa_de_leyva, "cartagena": cartagena, "chapinero": chapinero,
           "topaga": topaga, "mongua": mongua, "raquira": raquira,
           "guane": guane, "zapatoca": zapatoca, "san_gil": san_gil, "barichara": barichara,
           "marsella": marsella, "filandia": filandia, "salento": salento, "manizales": manizales,
           "jerico": jerico, "jardin": jardin, "guatape": guatape, "rionegro": rionegro,
           "cienaga": cienaga, "mompox": mompox, "santa_marta": santa_marta, "barranquilla": barranquilla,
           "tulua": tulua, "buga": buga, "palmira": palmira, "cali": cali,
           "puerto_gaitan": puerto_gaitan, "yopal": yopal, "leticia": leticia, "villavicencio": villavicencio,
           "san_andres": san_andres, "medellin": medellin, "bogota": bogota,
           "estacion_santa_fe": estacion_santa_fe, "mirador": mirador, "hamaca": hamaca, "loteria": loteria,
           "sorpresa": sorpresa, "tierra_del_futuro": tierra_del_futuro, "tierra_de_la_aventura": tierra_de_la_aventura,
           "tierra_de_la_frontera": tierra_de_la_frontera,
           "las_cruces": las_cruces, "calle_del_embudo": calle_del_embudo, "san_victorino": san_victorino,
           "la_perseverancia": la_perseverancia, "barrio_egipto": barrio_egipto, "teusaquillo": teusaquillo,
           "galerias": galerias, "la_soledad": la_soledad, "palermo": palermo, "park_way": park_way,
           "calle_19": calle_19, "avenida_jimenez": avenida_jimenez, "carrera_septima": carrera_septima, "niza": niza,
           "pasadena": pasadena, "cedritos": cedritos, "santa_barbara": santa_barbara, "rosales": rosales,
           "el_chico": el_chico, "usaquen": usaquen,
           "estacion_de_la_sabana": estacion_de_la_sabana, "estacion_del_norte": estacion_del_norte,
           "estacion_del_sur": estacion_del_sur, "estacion_del_oriente": estacion_del_oriente, "zona_t": zona_t,
           "empresa_de_energia": energia_bogota, "acueducto": acueducto_bogota, "contribuciones": contribuciones,
           "impuesto_de_lujo": impuesto_de_lujo, "salida": salida_bogota, "carcel": carcel_bogota,
           "vayase_a_la_carcel": vayase_a_la_carcel, "parada_libre": parada_libre_bogota, "arca_comunal": arca_comunal,
           "casualidad": casualidad_bogota}
