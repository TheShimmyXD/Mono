# Arte (fase FA)

*Se lee en FA (D-27, D-28), con `interfaz.md` para maquetas y capturas.*

## 1. Flujo

- Todo el arte sale de `arte/arte.py`: cada lugar es una función que devuelve formas `(papel, forma)` en orden de dibujo (`rect`, `poly`, `circle`, `arch`, `gothic`, `lines`), registrada en `LUGARES`. El color de cada papel está en `PALETA` y `LINEAS` (estilo «arte de chiva», D-28); un papel nuevo entra a `PALETA` en la misma tanda.
- Nunca se edita a mano un `arte/svg/*.svg` ni un `res/drawable/arte_<id>.xml`: se cambia `arte.py` y se corre `python3 arte/arte.py`. `cierre_paso.py` corre `arte.py --revisar` (sale 1 si algo no está al día).
- Ayudantes de dos clases: los que devuelven una forma (`vasija`, `star`, `rrect`, `rayo`, `signo_peso`) van con su papel, `("clay", vasija(…))`; los que devuelven una lista (`casa_colonial`, `vagon`, `chiva`…) se suman con `+`. Los íconos ya usan nombres de casilla (`energia`, `carcel`, `salida`…): la escena lleva sufijo (`energia_bogota`) y la clave en `LUGARES` es el `artKey`.
- **Lienzo nuevo** (como íconos 24 × 24 o personajes 48 × 48, `pj_<id>`): sección propia con su dict (`ICONOS`, `PERSONAJES`), su ancho de contorno y su bucle en `salidas()`. No hace falta leer `arte.py` (M-057): `trazos(escena, ancho_contorno, con_franjas=False, ancho_lineas=None)` → `svg(t, w, h, escala)` y `vector_drawable(t, w, h)`; un personaje en otro lienzo, `escalar(formas, k, dx, dy)`; los papeles, `python3 -I -c "import sys; sys.path.insert(0, 'arte'); import arte; print(sorted(arte.PALETA), sorted(arte.LINEAS))"`. El papel `sun` recibe rayos de `trazos`: en un fondo o un patrón, `bolt` o `sunray` (mismo amarillo).
- Al extraer un ayudante de `arte.py`: md5 de los SVG que lo usaban antes y después, y `arte.py` dice «0 cambiados» (M-041).
- Lienzo 200 × 140: cielo, fondo (cerros o mar), el monumento que identifica el lugar al centro, casas a los lados, suelo; franjas de chiva arriba y abajo. Dibujo propio y estilizado, sin calcar fotos ni logos (D-27).

## 2. Revisar antes del teléfono

- El sistema no trae renderizador de SVG. Venv persistente fuera del proyecto, una vez por equipo (~1,5 min): `python3 -m venv ~/.cache/mono-arte && ~/.cache/mono-arte/bin/pip -q install cairosvg`; si ya existe, se reusa (M-034).
- Hoja de contacto: `~/.cache/mono-arte/bin/python .claude/skills/agente-mono/scripts/hoja_arte.py <id>… --salida <scratchpad>/hoja.png` corre `arte.py` (se detiene si falla) y une los dibujos en ≤ 1600 px; `Read` de esa imagen, no de los PNG uno a uno. Tras corregir, la hoja solo con los cambiados.
- Antes de leerla, repasa en el código los fallos ya vistos (M-038): el cerro baja hasta donde empieza el suelo (si no, asoma una franja de cielo); nada útil en las franjas (y < 9 o > 131); detalles sueltos (flores, frutas, animales) con radio ≥ 8, porque el contorno de 1,4 se come los más chicos; ruedas y patas antes que la carrocería; trazos blancos (`wave`, `shine`, `glyph`) nunca sobre papel blanco (`cloud`, `wall`); cables y líneas no cruzan el monumento; nada del tema (bote, sol) detrás de un árbol, un poste o un vehículo.
- Lo que se corrige ahí no gasta una instalación. Después, el Redmi: `telefono.py cartas <casilla>… --salida capturas/<paso>_cartas.png [--tio-rico]` instala, abre cada carta, captura y las une (M-037). La captura es lo que juzga el autor.

## 3. Tamaños y tandas

- En una casilla del tablero (~36 dp) la escena no se lee: ahí va lo que decida FA.3 (recorte o color del grupo).
- Íconos (FA.2): lienzo propio de 24 × 24, sin franjas, mismo contorno; salen como `ic_<id>.xml`.
- FA.4 y FA.5: un grupo de 4 lugares por sub-paso, con hoja de contacto, captura y commit.

## 4. Ícono de la app y material de terceros

- **Ícono o nombre de la app:** `telefono.py adb -- shell am start -a android.intent.action.MAIN -c android.intent.category.HOME` y `captura`; la pantalla de inicio es del autor: a `capturas/` va solo el recorte del ícono (M-059).
- **Letra o imagen de terceros:** solo con licencia libre (OFL, CC0, Apache); se baja a una carpeta propia del scratchpad junto con su licencia, se comprueba que tenga tildes, ñ, ¿ y ¡ (`~/.cache/mono-arte/bin/python -I -c` con `ImageFont.truetype(ttf, 40)`: un glifo que falta da los mismos `bytes(f.getmask(ch))` que `'\uE000'`; no hay `fontTools`), la licencia queda en `arte/<tipo>/` y se cita en su D-## (D-33, M-047).
