"""Pruebas de los mensajes de `invitado.py`: `python3 -m unittest discover -s pc`."""
import json
import unittest

from invitado import hola, partida, pedido

# Recorte de un `snapshot` como lo escribe `encode` de engine/.../link/Messages.kt.
SNAPSHOT = json.dumps({
    "type": "snapshot", "version": 1,
    "config": {"name": "Clásico", "squares": [{"type": "go"}] * 40},
    "state": {"players": [{"name": "Ana"}, {"name": "Beto"}, {"name": "Caro"}]},
    "seats": [2], "last": 0,
}, ensure_ascii=False)


class TestInvitado(unittest.TestCase):

    def test_hola_y_pedido_con_los_campos_del_protocolo(self):
        self.assertEqual(json.loads(hola("PC ñ")), {"type": "hello", "version": 1, "name": "PC ñ"})
        self.assertEqual(json.loads(pedido(5)), {"type": "resync", "after": 5, "full": True})

    def test_lee_la_partida(self):
        self.assertEqual(partida(SNAPSHOT), {"tablero": "Clásico", "casillas": 40, "jugadores": ["Ana", "Beto", "Caro"], "asientos": [2], "ultima": 0})

    def test_un_bye_no_es_la_partida(self):
        with self.assertRaisesRegex(ValueError, "bye"):
            partida(json.dumps({"type": "bye", "reason": "versión del protocolo 2"}))


if __name__ == "__main__":
    unittest.main()
