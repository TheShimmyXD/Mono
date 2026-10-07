#!/usr/bin/env python3
"""Invitado de terminal de F5.3 (D-44, D-46, D-47): el PC se une a la sala del teléfono por Bluetooth y mide la conexión.

En el teléfono, la sala del anfitrión: en el menú, un jugador en «📶 Otro teléfono» y «Esperar al otro
teléfono», o `telefono.py adb -- shell am start -S -n com.jacck.mono/.MainActivity --es enlace sala`.
En el PC (Bluetooth encendido y emparejado con el teléfono):

    python3 pc/invitado.py                 # 1 minuto
    python3 pc/invitado.py --minutos 10    # la prueba de F5.3

Habla el protocolo de `engine/.../link/` (un JSON por línea): manda `hello`, recibe la partida
(`snapshot`) y cada `--cada` segundos pide la partida entera (`resync` con `full`) y mide la ida y
vuelta. Sale con 0 si no hubo cortes ni respuestas raras.
"""
import argparse
import json
import socket
import statistics
import subprocess
import sys
import time

from eco import NOMBRE, canal, telefono

PROTOCOLO = 1  # PROTOCOL_VERSION de engine/.../link/Messages.kt


def hola(nombre: str) -> str:
    return json.dumps({"type": "hello", "version": PROTOCOLO, "name": nombre}, ensure_ascii=False)


def pedido(despues: int, entera: bool = True) -> str:
    return json.dumps({"type": "resync", "after": despues, "full": entera})


def partida(linea: str) -> dict:
    """Lo que interesa de un `snapshot`: tablero, casillas, jugadores, asientos del invitado y última acción."""
    m = json.loads(linea)
    if m.get("type") != "snapshot":
        raise ValueError(f"esperaba la partida y llegó {m.get('type')!r}: {m.get('reason', '')}")
    if m["version"] != PROTOCOLO:
        raise ValueError(f"el teléfono usa el protocolo {m['version']} y este invitado el {PROTOCOLO}")
    return {
        "tablero": m["config"]["name"], "casillas": len(m["config"]["squares"]),
        "jugadores": [p["name"] for p in m["state"]["players"]], "asientos": sorted(m["seats"]), "ultima": m["last"],
    }


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--mac", help="MAC del teléfono (por defecto, el Redmi de `bluetoothctl devices`)")
    ap.add_argument("--canal", type=int, help="canal RFCOMM (por defecto, el que anuncia Mono por SDP)")
    ap.add_argument("--nombre", default="PC", help="nombre del invitado (PC)")
    ap.add_argument("--minutos", type=float, default=1.0, help="cuánto dura la prueba (1)")
    ap.add_argument("--cada", type=float, default=10.0, help="segundos entre pedidos (10)")
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
            print(f"{mac} no anuncia el servicio {NOMBRE} (¿está abierta la sala?). sdptool: {sdp.stderr.strip() or 'sin el registro'}")
            return 1
    print(f"Conectando con {mac}, canal {ch}…")
    t0 = time.monotonic()
    s = socket.socket(socket.AF_BLUETOOTH, socket.SOCK_STREAM, socket.BTPROTO_RFCOMM)
    s.settimeout(15)
    s.connect((mac, ch))
    lector = s.makefile("r", encoding="utf-8", newline="\n")
    s.sendall((hola(a.nombre) + "\n").encode("utf-8"))
    p = partida(lector.readline())
    inicio = time.monotonic()
    print(f"Conectado y con la partida en {(inicio - t0) * 1000:.0f} ms: {p['tablero']} ({p['casillas']} casillas), "
          f"{', '.join(p['jugadores'])}; aquí juega {', '.join(p['jugadores'][i] for i in p['asientos'])}.")

    tiempos, raras, corte = [], 0, None
    fin = inicio + a.minutos * 60
    siguiente_minuto = 1
    try:
        while time.monotonic() < fin:
            time.sleep(min(a.cada, max(0.0, fin - time.monotonic())))
            t = time.monotonic()
            s.sendall((pedido(p["ultima"]) + "\n").encode("utf-8"))
            try:
                q = partida(lector.readline())
                tiempos.append((time.monotonic() - t) * 1000)
                if q != p:
                    raras += 1
                    print(f"  la partida cambió sin acciones: {q}")
            except ValueError as e:
                raras += 1
                print(f"  respuesta rara: {e}")
            if time.monotonic() - inicio >= siguiente_minuto * 60:
                print(f"  {siguiente_minuto} min: {len(tiempos)} pedidos bien, última ida y vuelta {tiempos[-1]:.0f} ms" if tiempos else
                      f"  {siguiente_minuto} min: sin respuestas")
                siguiente_minuto += 1
    except (OSError, TimeoutError) as e:
        corte = e
    finally:
        s.close()

    duro = time.monotonic() - inicio
    resumen = f"{len(tiempos)} pedidos bien, {raras} raros"
    if tiempos:
        resumen += f"; ida y vuelta: mín {min(tiempos):.0f}, mediana {statistics.median(tiempos):.0f}, máx {max(tiempos):.0f} ms"
    if corte:
        print(f"Se cortó a los {duro / 60:.1f} min ({corte}); {resumen}.")
        return 1
    print(f"{duro / 60:.1f} min conectados sin cortes; {resumen}.")
    return 0 if raras == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
