# Reglamentos y `REGLAS.md` (fase F1)

*Se lee en F1 y cada vez que una tarea necesite una regla que no está en `REGLAS.md`. Regla 1 y 2 del mandato.*

## 1. Las fuentes

| Clave (`mono.toml` [fuentes]) | Archivo | Qué es |
|---|---|---|
| `monopoly` | `fuentes/Monopoly(Spanish).pdf` | Reglamento en español, 8 páginas de 407 × 492 pt, escaneado (sin texto) |
| `tio_rico` | `fuentes/tio_rico.pdf` | «Tío Rico de Lujo», instrucciones en **una** página de 1178 × 808 pt, escaneada |

Son imágenes: `pdftotext` devuelve 0 palabras y `grep` no sirve. `fuentes/` es intocable (regla 3): solo se lee.

## 2. Cómo se leen

- Una página o un recorte a la vez, siempre con `scripts/pagina.py` al scratchpad, y después `Read` del PNG. Nunca el PDF con `Read`, ni rangos de páginas.
- Monopoly: una página por imagen a 100 dpi (~80 KB). Si una cifra no se lee, el recorte de esa zona a 150-200 dpi.
- Tío Rico: la página es grande (≈ 16 × 11 pulgadas). Se cubre con una rejilla: `pagina.py tio_rico 1 --rejilla 2x3` lista los 6 recortes; se lee cada uno a 100 dpi y se anota en la tabla de cobertura de `REGLAS.md`. Si un párrafo queda partido entre dos recortes, se lee un recorte extra que lo contenga.
- **Lo leído se transcribe en el mismo paso** a `REGLAS.md`: el reglamento no se vuelve a abrir para una regla ya transcrita.

## 3. Formato de `REGLAS.md` (raíz del proyecto)

```
## Cobertura
| Fuente | Página o recorte | Leído (sesión) | Reglas |
| monopoly | p. 3 | 2026-10-07 `abcd1234` | R-07..R-12 |
| tio_rico | p. 1 r0-0-50-33 | ... | R-30..R-33 |

### R-07 · Salario al pasar por la salida
- **Regla:** quien pasa o cae en la Salida cobra 200 del banco.      <- palabras propias, breve
- **Cifras:** 200.                                                    <- leídas dos veces
- **Fuente:** monopoly p. 3 (columna izquierda, 2.º párrafo).
- **Tío Rico:** difiere (ver R-31) | igual | no la tiene.
- **Opción:** `salary` (si difiere entre juegos; F1.3).
```

- Un ID R-## por regla y para siempre (no se renumera). Si una regla se corrige, se edita la ficha y se anota la sesión.
- **Cifras leídas dos veces**: una al transcribir y otra comparando la ficha con el recorte antes de cerrar el paso. Si el escaneo es ilegible, la ficha dice «ilegible» y va a «Pendiente del autor»; nunca se completa de memoria.
- Lo que el reglamento no dice (p. ej. qué pasa con el dinero de los impuestos) no se inventa: es una opción con valor por defecto decidido por el autor (D-##), y la ficha lo dice.

## 4. Diferencias y opciones (F1.3, F1.4)

- Tabla `## Diferencias` en `REGLAS.md`: regla · Monopoly · Tío Rico · nombre de la opción · valor en cada preset. El autor la aprueba con AskUserQuestion (Aprobar / Con cambios).
- Lo editable (F1.4) va en una D-## con el rango de cada campo (N de casillas, precios, número de jugadores 2-6 de D-05).
