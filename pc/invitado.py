#!/usr/bin/env python3
"""Invitado de terminal de F5.3 (D-44, D-46, D-47, D-48): el PC se une a la sala del teléfono por la red local y mide la conexión.

En el teléfono, la sala del anfitrión: en el menú, un jugador en «📶 Otro teléfono» y «Esperar al otro
teléfono», o `telefono.py adb -- shell am start -S -n com.jacck.mono/.MainActivity --es enlace sala`.
En el PC, en el mismo Wi-Fi que el teléfono (o conectado a su punto de acceso):

    python3 pc/invitado.py                 # 1 minuto; busca la sala por mDNS (avahi-browse)
    python3 pc/invitado.py --minutos 10    # la prueba de F5.3
    python3 pc/invitado.py --ip 192.168.43.1 --puerto 40123   # la dirección que muestra la sala

Habla el protocolo de `engine/.../link/` (un JSON por línea): manda `hello`, recibe la partida
(`snapshot`) y cada `--cada` segundos pide la partida entera (`resync` con `full`) y mide la ida y
vuelta. Sale con 0 si no hubo cortes ni respuestas raras.
"""
import argparse
import json
import re
import socket
import statistics
import subprocess
import sys
import time

PROTOCOLO = 1  # PROTOCOL_VERSION de engine/.../link/Messages.kt
TIPO = "_mono._tcp"  # SERVICE_TYPE de app/.../enlace/LanServer.kt


def salas(salida_avahi: str) -> list[tuple[str, str, int]]:
    """(nombre, IPv4, puerto) de cada sala resuelta en la salida de `avahi-browse -rpt _mono._tcp`."""
    vistas = []
    for linea in salida_avahi.splitlines():
        c = linea.split(";")
        if len(c) >= 9 and c[0] == "=" and c[2] == "IPv4" and c[4] == TIPO:
            # avahi escapa cada byte raro como \ddd (decimal): «Redmi\032Note» = «Redmi Note».
            nombre = re.sub(rb"\\(\d{3})", lambda m: bytes([int(m.group(1))]), c[3].encode()).decode("utf-8", "replace")
            if (nombre, c[7], int(c[8])) not in vistas:
                vistas.append((nombre, c[7], int(c[8])))
    return vistas


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


def conectar_red(a) -> socket.socket | None:
    ip, puerto = a.ip, a.puerto
    if ip is None or puerto is None:
        r = subprocess.run(["avahi-browse", "-rpt", TIPO], capture_output=True, text=True, timeout=20)
        vistas = salas(r.stdout)
        if not vistas:
            print(f"No veo ninguna sala {TIPO} en la red (¿mismo Wi-Fi y sala abierta?). Prueba con --ip y --puerto.")
            return None
        nombre, ip, puerto = vistas[0]
        print(f"Sala «{nombre}» en {ip}:{puerto}" + (f" (hay {len(vistas)})" if len(vistas) > 1 else ""))
    s = socket.create_connection((ip, puerto), timeout=15)
    s.setsockopt(socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)
    return s


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--ip", help="dirección de la sala (por defecto, la que se anuncia por mDNS)")
    ap.add_argument("--puerto", type=int, help="puerto de la sala, con --ip")
    ap.add_argument("--nombre", default="PC", help="nombre del invitado (PC)")
    ap.add_argument("--minutos", type=float, default=1.0, help="cuánto dura la prueba (1)")
    ap.add_argument("--cada", type=float, default=10.0, help="segundos entre pedidos (10)")
    a = ap.parse_args()

    t0 = time.monotonic()
    s = conectar_red(a)
    if s is None:
        return 1
    lector = s.makefile("r", encoding="utf-8", newline="\n")
    s.sendall((hola(a.nombre) + "\n").encode("utf-8"))
    p = partida(lector.readline())
    inicio = time.monotonic()
    print(f"Conectado y con la partida en {(inicio - t0) * 1000:.0f} ms (búsqueda incluida): {p['tablero']} ({p['casillas']} casillas), "
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
