"""Pruebas de lo que `eco.py` lee de BlueZ: `python3 -m unittest discover -s pc`."""
import unittest

from eco import canal, telefono

SDP = """Browsing AA:BB:CC:11:22:33 ...
Service Name: Headset Gateway
Service RecHandle: 0x10003
Protocol Descriptor List:
  "L2CAP" (0x0100)
  "RFCOMM" (0x0003)
    Channel: 2

Service Name: Mono
Service RecHandle: 0x1000b
Service Class ID List:
  UUID 128: 1a80cf3d-efe2-4ea4-98d1-6c4976e1d3c9
Protocol Descriptor List:
  "L2CAP" (0x0100)
  "RFCOMM" (0x0003)
    Channel: 12
"""


class EcoTest(unittest.TestCase):
    def test_canal_de_mono_y_no_el_de_otro_servicio(self):
        self.assertEqual(12, canal(SDP))

    def test_sin_registro_de_mono(self):
        self.assertIsNone(canal(SDP.split("\n\n")[0]))

    def test_telefono_redmi_entre_varios(self):
        self.assertEqual("AA:BB:CC:11:22:33", telefono(
            "Device 11:22:33:44:55:66 Audífonos\nDevice AA:BB:CC:11:22:33 Redmi Note 13 Pro\n"))

    def test_ninguno_si_hay_varios_sin_redmi(self):
        self.assertIsNone(telefono("Device 11:22:33:44:55:66 A\nDevice 22:33:44:55:66:77 B\n"))


if __name__ == "__main__":
    unittest.main()
