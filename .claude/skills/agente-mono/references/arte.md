# Arte (fase FA)

*Se lee en FA (D-27, D-28), con `interfaz.md` para maquetas y capturas.*

## 1. Flujo

- Todo el arte sale de `arte/arte.py`: cada lugar es una función que devuelve formas `(papel, forma)` en orden de dibujo (`rect`, `poly`, `circle`, `arch`, `gothic`, `lines`), registrada en `LUGARES`. El color de cada papel está en `PALETA` y `LINEAS` (estilo «arte de chiva», D-28); un papel nuevo entra a `PALETA` en la misma tanda.
- Nunca se edita a mano un `arte/svg/*.svg` ni un `res/drawable/arte_<id>.xml`: se cambia `arte.py` y se corre `python3 arte/arte.py`. `cierre_paso.py` corre `arte.py --revisar` (sale 1 si algo no está al día).
- Lienzo 200 × 140: cielo, fondo (cerros o mar), el monumento que identifica el lugar al centro, casas a los lados, suelo; franjas de chiva arriba y abajo. Dibujo propio y estilizado, sin calcar fotos ni logos (D-27).

## 2. Revisar antes del teléfono

- El sistema no trae renderizador de SVG. Venv persistente fuera del proyecto, una vez por equipo (~1,5 min): `python3 -m venv ~/.cache/mono-arte && ~/.cache/mono-arte/bin/pip -q install cairosvg`; si ya existe, se reusa (M-034).
- Hoja de contacto: todos los dibujos del paso en una sola imagen de ≤ 1600 px (cairosvg + PIL, script en el scratchpad) y `Read` de esa imagen; no se leen los PNG uno a uno.
- Lo que se corrige ahí no gasta una instalación. Después, el Redmi (`interfaz.md` §2): la captura es lo que juzga el autor.

## 3. Tamaños y tandas

- En una casilla del tablero (~36 dp) la escena no se lee: ahí va lo que decida FA.3 (recorte o color del grupo).
- Íconos (FA.2): lienzo propio de 24 × 24, sin franjas, mismo contorno; salen como `ic_<id>.xml`.
- FA.4 y FA.5: un grupo de 4 lugares por sub-paso, con hoja de contacto, captura y commit.
