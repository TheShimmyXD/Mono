#!/usr/bin/env python3
"""Prueba de concepto de F5.1 (D-45): el PC habla por Bluetooth (RFCOMM) con la pantalla «Eco» del teléfono.

En el teléfono: `telefono.py adb -- shell am start -n com.jacck.mono/.MainActivity --es enlace eco`.
En el PC (Bluetooth encendido y emparejado con el teléfono):

    python3 pc/eco.py                 # 20 mensajes, mide la ida y vuelta
    python3 pc/eco.py --n 100 --pausa 0.2
    python3 pc/eco.py --minutos 10    # conexión larga (F5.3)

Sale con 0 si todos los ecos volvieron bien. Sin dependencias: `socket.AF_BLUETOOTH` del Python del
sistema y `sdptool` / `bluetoothctl` de BlueZ.
"""
import argparse
import re
import socket
import statistics
import subprocess
import sys
import time

UUID = "1a80cf3d-efe2-4ea4-98d1-6c4976e1d3c9"  # MONO_UUID de enlace/EcoServer.kt
NOMBRE = "Mono"                                 # SERVICE_NAME
SALUDO = "MONO eco 1"                           # GREETING


def telefono(salida_bluetoothctl: str) -> str | None:
    """MAC del primer dispositivo de `bluetoothctl devices` que parezca un teléfono Redmi/Xiaomi, o el único."""
    dispositivos = re.findall(r"^Device ([0-9A-F:]{17}) (.*)$", salida_bluetoothctl, re.M)
    for mac, nombre in dispositivos:
        if re.search(r"redmi|xiaomi|poco", nombre, re.I):
            return mac
    return dispositivos[0][0] if len(dispositivos) == 1 else None


def canal(salida_sdptool: str) -> int | None:
    """Canal RFCOMM del registro de Mono en la salida de `sdptool browse` (registros separados por línea en blanco)."""
    for registro in re.split(r"\n\s*\n", salida_sdptool):
        if UUID in registro.lower() or re.search(rf"^Service Name: {NOMBRE}\s*$", registro, re.M):
            m = re.search(r"Channel: (\d+)", registro)
            if m:
                return int(m.group(1))
    return None


def conectar(mac: str, ch: int, espera: float = 8.0):
    s = socket.socket(socket.AF_BLUETOOTH, socket.SOCK_STREAM, socket.BTPROTO_RFCOMM)
    s.settimeout(espera)
    s.connect((mac, ch))
    lector = s.makefile("r", encoding="utf-8", newline="\n")
    saludo = lector.readline().rstrip("\n")
    if saludo != SALUDO:
        s.close()
        raise ConnectionError(f"el canal {ch} no es Mono (dijo {saludo!r})")
    return s, lector


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--mac", help="MAC del teléfono (por defecto, el Redmi de `bluetoothctl devices`)")
    ap.add_argument("--canal", type=int, help="canal RFCOMM (por defecto, el que anuncia Mono por SDP)")
    ap.add_argument("--n", type=int, default=20, help="mensajes a mandar (20)")
    ap.add_argument("--pausa", type=float, default=0.0, help="segundos entre mensajes (0)")
    ap.add_argument("--minutos", type=float, help="en vez de --n, mandar uno por segundo durante estos minutos")
    a = ap.parse_args()

    mac = a.mac or telefono(subprocess.run(["bluetoothctl", "devices"], capture_output=True, text=True).stdout)
    if not mac:
        print("No encuentro el teléfono en `bluetoothctl devices`: emparéjalo o pasa --mac.")
        return 1
    ch = a.canal
    if ch is None:
        sdp = subprocess.run(["sdptool", "browse", mac], capture_output=True, text=True)
        ch = canal(sdp.stdout)
        if ch is None:
            print(f"{mac} no anuncia el servicio {NOMBRE} (¿está abierta la pantalla «Eco»?). sdptool: {sdp.stderr.strip() or 'sin el registro'}")
            return 1
    print(f"Conectando con {mac}, canal {ch}…")
    t0 = time.monotonic()
    s, lector = conectar(mac, ch)
    print(f"Conectado en {(time.monotonic() - t0) * 1000:.0f} ms; el teléfono dijo «{SALUDO}».")

    if a.minutos:
        a.n, a.pausa = int(a.minutos * 60), 1.0
    tiempos, malos = [], 0
    try:
        for i in range(1, a.n + 1):
            texto = f"mensaje {i} desde el PC · ñ"
            t = time.monotonic()
            s.sendall((texto + "\n").encode("utf-8"))
            respuesta = lector.readline().rstrip("\n")
            tiempos.append((time.monotonic() - t) * 1000)
            if respuesta != f"eco {i}: {texto}":
                malos += 1
                print(f"  {i}: respuesta inesperada {respuesta!r}")
            if a.minutos and i % 60 == 0:
                print(f"  {i // 60} min: {i - malos}/{i} ecos bien, última ida y vuelta {tiempos[-1]:.0f} ms")
            time.sleep(a.pausa)
    except (OSError, TimeoutError) as e:
        print(f"Se cortó tras {len(tiempos)} mensajes: {e}")
        malos += a.n - len(tiempos)
    finally:
        s.close()

    if tiempos:
        print(f"{len(tiempos) - malos}/{a.n} ecos correctos; ida y vuelta: mín {min(tiempos):.0f}, "
              f"mediana {statistics.median(tiempos):.0f}, máx {max(tiempos):.0f} ms.")
    return 0 if malos == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
