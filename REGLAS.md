# Reglas — Mono

*Transcripción de los reglamentos de `fuentes/` (F1). Cada regla del motor cita su R-##. Formato y método: `.claude/skills/agente-mono/references/reglas.md`.*

## Cobertura

| Fuente | Página o recorte | Leído (sesión) | Reglas |
|---|---|---|---|
| monopoly | p. 1 | 2026-10-06 `e47b9e54` | R-01..R-03 |
| monopoly | p. 2 | 2026-10-06 `e47b9e54` | R-04..R-09 |
| monopoly | p. 3 | 2026-10-06 `e47b9e54` | R-10..R-16 |
| monopoly | p. 4 | 2026-10-06 `e47b9e54` | R-17..R-22 |
| monopoly | p. 5 | 2026-10-06 `e47b9e54` | R-22..R-26 |
| monopoly | p. 6 | 2026-10-06 `e47b9e54` | R-27..R-31 |
| monopoly | p. 7 | 2026-10-06 `e47b9e54` | R-32..R-36 |
| monopoly | p. 8 | 2026-10-06 `e47b9e54` | R-37..R-40 |
| tio_rico | p. 1 r0-0-50-33 | 2026-10-06 `e47b9e54` | — (foto del tablero y dados; casillas ilegibles) |
| tio_rico | p. 1 r50-0-50-33 | 2026-10-06 `e47b9e54` | título; arranque de las columnas (leídas enteras abajo) |
| tio_rico | p. 1 r0-33-50-33 | 2026-10-06 `e47b9e54` | — (foto del tablero y títulos) |
| tio_rico | p. 1 r50-33-50-33 | 2026-10-06 `e47b9e54` | parte de las columnas (leídas enteras abajo) |
| tio_rico | p. 1 r0-67-50-33 | 2026-10-06 `e47b9e54` | R-41 (Contenido) |
| tio_rico | p. 1 r40-38-20-62 (150 dpi) | 2026-10-06 `e47b9e54` | R-42 (Objetivo, Descripción, Preparación) |
| tio_rico | p. 1 r59-22-20-78 (150 dpi) | 2026-10-06 `e47b9e54` | R-43..R-51 |
| tio_rico | p. 1 r78-22-22-78 (150 dpi) | 2026-10-06 `e47b9e54` | R-52..R-54; Deudas, Lotería, Quiebra (iguales) |

Monopoly: 8/8 páginas leídas; cifras leídas dos veces (al transcribir y comparando cada ficha con su página), 2026-10-06 `e47b9e54`.

Tío Rico (1 página): la rejilla 2×3 parte las columnas de texto, así que el texto se leyó en tres recortes por columna a 150 dpi; el recorte r50-67-50-33 queda dentro de ellos (x 0,40-0,995, y 0,22-1). Lo que queda fuera es borde amarillo. Cifras leídas dos veces, 2026-10-06 `e47b9e54`.

## Monopoly

### R-01 · Objetivo
- **Regla:** gana quien termina siendo el jugador más rico, comprando, alquilando y vendiendo propiedades.
- **Cifras:** —
- **Fuente:** monopoly p. 1 («OBJETIVO…»).
- **Tío Rico:** igual (p. 1 «OBJETIVO»).

### R-02 · Equipo
- **Regla:** un tablero, dos dados, fichas, 32 casas y 12 hoteles; barajas de Casualidad y Arca Comunal; una Escritura de Propiedad por finca; billetes.
- **Cifras:** 2 dados, 32 casas, 12 hoteles.
- **Fuente:** monopoly p. 1 («EL EQUIPO…»).
- **Tío Rico:** difiere (ver R-41).

### R-03 · Preparación y dinero inicial
- **Regla:** las cartas de Casualidad y Arca Comunal van boca abajo en su espacio. Cada jugador elige una ficha y recibe $1500; el resto del dinero es del Banco.
- **Cifras:** $1500 = 2×$500 + 2×$100 + 2×$50 + 6×$20 + 5×$10 + 5×$5 + 5×$1 (suma comprobada: 1000 + 200 + 100 + 120 + 50 + 25 + 5 = 1500).
- **Fuente:** monopoly p. 1 («PREPARACION…»).
- **Tío Rico:** difiere (ver R-42).

### R-04 · Banquero
- **Regla:** un jugador hace de Banquero y subastador; si también juega, separa su dinero del Banco. Con más de cinco jugadores puede limitarse a ser banquero.
- **Cifras:** más de 5 jugadores.
- **Fuente:** monopoly p. 2 («EL BANQUERO…»).
- **Tío Rico:** difiere: el banquero «debe ser exclusivo», no juega (ver R-42).
- **Nota:** en la app el Banco lo lleva el motor; esta regla no se programa.

### R-05 · El Banco
- **Regla:** el Banco guarda el dinero, las Escrituras y las casas y hoteles sin comprar. Paga sueldos y gratificaciones; vende y remata (subasta) propiedades; vende casas y hoteles; presta sobre hipotecas. Cobra contribuciones, multas, préstamos, intereses y el precio de lo que vende o remata. **Nunca se arruina:** si se queda sin dinero, emite el que necesite.
- **Cifras:** —
- **Fuente:** monopoly p. 2 («EL BANCO…» y los dos párrafos siguientes).
- **Tío Rico:** no lo dice (no menciona que el Banco no se arruine).

### R-06 · Quién empieza y orden de turnos
- **Regla:** cada jugador tira los dados; empieza el del total mayor. Al terminar su jugada, el turno pasa al jugador de su izquierda.
- **Cifras:** —
- **Fuente:** monopoly p. 2 («EL JUEGO…», 1.ª y 4.ª frases).
- **Tío Rico:** difiere (ver R-43).
- **Sin decir:** qué pasa si hay empate en la tirada inicial (lo decide el autor).

### R-07 · Movimiento
- **Regla:** todas las fichas empiezan en «GO» (la Salida). En su turno el jugador tira los dos dados y avanza, en el sentido de la flecha, tantos espacios como la suma. Las fichas se quedan donde caen y desde ahí siguen; varias fichas pueden compartir espacio.
- **Cifras:** —
- **Fuente:** monopoly p. 2 («EL JUEGO…»).
- **Tío Rico:** igual; la salida se llama «Estación Santa Fe».

### R-08 · Efecto del espacio
- **Regla:** según el espacio en que cae, el jugador puede comprar terrenos u otras propiedades, o debe pagar alquiler o contribuciones, robar una tarjeta de Casualidad o de Arca Comunal, ir a la Cárcel, etc. (detalle en las reglas de cada espacio).
- **Cifras:** —
- **Fuente:** monopoly p. 2 (párrafo «Según sea el espacio…»).
- **Tío Rico:** igual (p. 1 «DESCRIPCIÓN DEL JUEGO»).

### R-09 · Dobles
- **Regla:** con dobles, el jugador avanza normalmente, cumple lo del espacio y vuelve a tirar. Si saca **tres dobles seguidos**, no avanza con la tercera tirada: va directo al espacio «En la Cárcel» (ver la regla de la Cárcel).
- **Cifras:** 3 dobles seguidos.
- **Fuente:** monopoly p. 2 (último párrafo).
- **Tío Rico:** difiere: no hay Cárcel ni tope de tres dobles (ver R-43).

### R-10 · Sueldo al pasar por GO
- **Regla:** cada vez que la ficha cae en GO (Salida) o pasa por él, por los dados o por una carta, el Banco le paga un sueldo. Se cobra **una sola vez por vuelta**. Ejemplo del reglamento: si al pasar por GO cae en Arca Comunal (2 espacios después) o en Azar (7 después) y la carta dice «Avance hasta GO», cobra $200 por pasar y otros $200 por llegar a GO.
- **Cifras:** $200; Arca Comunal a 2 espacios de GO, Azar a 7 (en el ejemplo).
- **Fuente:** monopoly p. 3 («"GO" (Adelante)…» y párrafo siguiente).
- **Tío Rico:** difiere (ver R-45).
- **Opción:** `salary` (si difiere; F1.3).

### R-11 · Compra de una propiedad sin dueño
- **Regla:** quien cae en una propiedad sin dueño puede comprarla al Banco al precio impreso; recibe su Escritura y la pone boca arriba delante de sí.
- **Cifras:** el precio impreso de cada propiedad.
- **Fuente:** monopoly p. 3 («COMPRA DE UNA PROPIEDAD…»).
- **Tío Rico:** igual (p. 1 «COMPRA DE PROPIEDADES»).

### R-12 · Subasta
- **Regla:** si no la compra, el Banco la subasta en el acto y la vende al mejor postor, que paga al Banco lo ofrecido. Pueden pujar todos, incluso quien rechazó comprarla al precio impreso. La subasta puede empezar con cualquier precio.
- **Cifras:** —
- **Fuente:** monopoly p. 3 (párrafo «Si el jugador opta por no comprar…»).
- **Tío Rico:** difiere (ver R-44).
- **Sin decir:** qué pasa si nadie puja (la propiedad sigue del Banco, por defecto; lo confirma el autor).

### R-13 · Alquiler
- **Regla:** quien cae en una propiedad con dueño le paga el alquiler impreso en la Escritura.
- **Cifras:** las de cada Escritura.
- **Fuente:** monopoly p. 3 («LLEGADA A UNA PROPIEDAD CON DUEÑO…»).
- **Tío Rico:** igual (p. 1 «LLEGADA A UNA PROPIEDAD CON DUEÑO»).

### R-14 · Sin alquiler si está hipotecada
- **Regla:** un solar hipotecado no cobra alquiler. La hipoteca se marca poniendo la Escritura boca abajo.
- **Cifras:** —
- **Fuente:** monopoly p. 3 (párrafo «Si el solar está hipotecado…»).
- **Tío Rico:** difiere: la hipotecada sigue cobrando (ver R-49).

### R-15 · Alquiler con casas
- **Regla:** con una o más casas, el alquiler es mayor que el del solar sin construir (cifras en cada Escritura).
- **Cifras:** las de cada Escritura.
- **Fuente:** monopoly p. 3 («Nota:…»).
- **Tío Rico:** igual (p. 1 «VENTAJAS PARA LOS DUEÑOS», 2.º párrafo).

### R-16 · Grupo completo: alquiler doble
- **Regla:** quien tiene todas las Escrituras de un grupo de color cobra **doble alquiler** por los solares sin construir de ese grupo. Vale para los solares no hipotecados aunque otro del grupo esté hipotecado.
- **Cifras:** ×2. Ejemplos de grupo: Paseo Tablado y Plaza del Parque; Avenidas Connecticut, Vermont y Oriental.
- **Fuente:** monopoly p. 3 (último párrafo).
- **Tío Rico:** igual (p. 1 «VENTAJAS PARA LOS DUEÑOS»; ejemplo: Tierra de la Fantasía, Cielos de Dumbo y Villa de Pinocho).

### R-17 · Plazo para cobrar el alquiler
- **Regla:** casas y hoteles dan alquileres mucho más altos que los solares sin construir. Si el dueño no exige el alquiler antes de que el siguiente jugador tire los dados, ya no puede cobrarlo.
- **Cifras:** —
- **Fuente:** monopoly p. 4 (dos primeros párrafos).
- **Tío Rico:** no lo dice.
- **Opción:** en la app el cobro es automático por defecto; un modo «hay que reclamarlo» sería una opción (lo decide el autor).

### R-18 · Casualidad y Arca Comunal
- **Regla:** quien cae en esos espacios roba la carta de encima de la baraja que toca, cumple lo impreso y la devuelve boca abajo debajo de la baraja. La carta «Salir libre de la Cárcel» se guarda hasta usarla y entonces vuelve debajo de la baraja; quien la tiene puede venderla a otro jugador en cualquier momento al precio que acuerden.
- **Cifras:** —
- **Fuente:** monopoly p. 4 («LLEGADA A LOS ESPACIOS DE "CASUALIDAD" O "ARCA COMUNAL"…» y párrafo siguiente).
- **Tío Rico:** igual, salvo que no menciona la carta «Salir libre de la Cárcel» (p. 1 «LOTERÍA CAMPANITA O SORPRESA BALLENA»).
- **Nota:** el texto de cada carta no está en el reglamento (están en las barajas); lo decide el autor.

### R-19 · Impuesto (contribuciones)
- **Regla:** quien cae en el espacio de contribuciones elige: pagar $200 o el 10 % de su patrimonio total. Patrimonio = efectivo + precio impreso de sus propiedades (hipotecadas o no) + precio de costo de sus edificios. Debe elegir **antes** de sumar sus bienes.
- **Cifras:** $200 o 10 %.
- **Fuente:** monopoly p. 4 («IMPUESTO…» y párrafo siguiente).
- **Tío Rico:** difiere: en su lugar, las tres «Tierras» (ver R-52..R-54).

### R-20 · Ir a la Cárcel
- **Regla:** se va a la Cárcel (1) al caer en «Váyase a la Cárcel», (2) al robar la carta «Váyase a la Cárcel» o (3) al sacar dobles tres veces seguidas (R-09). La ficha va directo a la Cárcel, sin cobrar los $200 de GO aunque lo pase. Ir a la Cárcel termina el turno.
- **Cifras:** $200 no se cobran; 3 dobles.
- **Fuente:** monopoly p. 4 («LA CARCEL…» y párrafo siguiente).
- **Tío Rico:** no la tiene: el reglamento no menciona la Cárcel.

### R-21 · De visita
- **Regla:** quien cae en la Cárcel en una jugada normal está «De Visita No Más»: sin castigo, y sigue normal en su próximo turno.
- **Cifras:** —
- **Fuente:** monopoly p. 4 (párrafo «Si en el curso de una jugada normal…»).
- **Tío Rico:** no la tiene.

### R-22 · Salir de la Cárcel
- **Regla:** se sale de cuatro maneras: (1) sacando dobles en alguno de los tres turnos siguientes a entrar: avanza en el acto lo que marcan esos dados y, aunque fueron dobles, **no** vuelve a tirar; (2) usando la carta «Salir libre de la Cárcel» si la tiene; (3) comprándosela a otro jugador y usándola; (4) pagando una multa de $50 antes de tirar en cualquiera de sus dos turnos siguientes. Si en el tercer turno no saca dobles, paga la multa de $50, sale y avanza lo que marcó esa tirada.
- **Cifras:** 3 turnos; multa $50; pago voluntario en los 2 turnos siguientes.
- **Fuente:** monopoly p. 4 (último párrafo) y p. 5 (dos primeros párrafos).
- **Tío Rico:** no la tiene.
- **Opción:** `jailFine` (si difiere; F1.3).

### R-23 · Negocios desde la Cárcel
- **Regla:** estando en la Cárcel se puede comprar y vender propiedades, casas y hoteles, y cobrar alquileres.
- **Cifras:** —
- **Fuente:** monopoly p. 5 (párrafo «Aunque esté en la cárcel…»).
- **Tío Rico:** no la tiene.

### R-24 · Parada Libre
- **Regla:** quien cae en Parada Libre no recibe dinero, propiedad ni premio: es solo un descanso.
- **Cifras:** —
- **Fuente:** monopoly p. 5 («PARADA LIBRE…»).
- **Tío Rico:** no la tiene: no menciona Parada Libre.
- **Opción:** la regla casera del «bote» en Parada Libre no está en el reglamento: si se quiere, es una opción del autor.

### R-25 · Comprar casas
- **Regla:** quien tiene todas las propiedades de un grupo de color puede comprar casas al Banco y levantarlas en ellas, en cualquier momento, según su dinero. El precio de cada casa está en la Escritura del solar. La primera casa va en cualquier solar del grupo; la segunda, en un solar sin casa de ese grupo o de otro grupo completo suyo. El doble alquiler de R-16 sigue valiendo en los solares sin construir del grupo.
- **Cifras:** precio por casa en cada Escritura.
- **Fuente:** monopoly p. 5 («LAS CASAS…» y tres párrafos siguientes).
- **Tío Rico:** difiere (ver R-46).

### R-26 · Construir y vender parejo
- **Regla:** se construye en partes iguales: no se pone una segunda casa en un solar del grupo hasta que todos tengan una, y así fila por fila, **hasta 4 casas por solar**. Del mismo modo, las casas se venden al Banco en partes iguales (ver la venta de propiedades).
- **Cifras:** máximo 4 casas por solar; diferencia máxima de 1 casa entre solares del grupo.
- **Fuente:** monopoly p. 5 (dos últimos párrafos).
- **Tío Rico:** difiere: no pide construir parejo; tope de 3 casas (ver R-46).

### R-27 · Hoteles
- **Regla:** para comprar un hotel hay que tener cuatro casas en cada solar del grupo completo. Entonces se compra al Banco para cualquier solar del grupo, devolviendo al Banco las cuatro casas de ese solar y pagando el precio del hotel de la Escritura. Como máximo un hotel por solar.
- **Cifras:** 4 casas en cada solar; 1 hotel por solar; se devuelven 4 casas.
- **Fuente:** monopoly p. 6 («LOS HOTELES…»).
- **Tío Rico:** difiere (ver R-47).

### R-28 · Escasez de edificios
- **Regla:** si el Banco no tiene casas, quien quiera construir espera a que otro devuelva o venda casas al Banco. Si quedan pocas casas u hoteles y dos o más jugadores quieren más de los que hay, se subastan al mejor postor.
- **Cifras:** el tope de R-02 (32 casas, 12 hoteles).
- **Fuente:** monopoly p. 6 («LA ESCASEZ DE EDIFICIOS…»).
- **Tío Rico:** no lo dice (trae 30 casas y 10 castillos, R-41).
- **Opción:** `limitedBuildings` (tope de casas y hoteles sí/no; lo decide el autor en F1.3).

### R-29 · Venta entre jugadores
- **Regla:** solares sin edificar, ferrocarriles y servicios públicos (pero no edificios) se pueden vender a otro jugador en privado, por lo que acuerden. No se vende un solar de un grupo de color si algún solar de ese grupo tiene edificios: antes hay que venderlos al Banco.
- **Cifras:** —
- **Fuente:** monopoly p. 6 («VENTA DE PROPIEDADES…»).
- **Tío Rico:** difiere (ver R-51).

### R-30 · Vender edificios al Banco
- **Regla:** casas y hoteles se revenden al Banco en cualquier momento a **la mitad** de lo que costaron. Las casas de un grupo se venden una a una, parejo y al revés de como se construyeron (R-26). Los hoteles de un grupo se pueden vender todos a la vez y enteros, o de una casa por vez (un hotel equivale a cinco casas), también parejo y al revés.
- **Cifras:** 1/2 del precio; hotel = 5 casas.
- **Fuente:** monopoly p. 6 (párrafos 2 a 4 de «VENTA DE PROPIEDADES»).
- **Tío Rico:** no lo dice (no hay reventa de edificios al Banco; sí se hipotecan, R-49).

### R-31 · Hipotecar
- **Regla:** las propiedades sin edificios se hipotecan en cualquier momento y solo con el Banco, que paga el valor hipotecario impreso en la Escritura. Para hipotecar un solar con edificios, antes se venden al Banco, a mitad de precio, todos los edificios de ese solar y de los demás solares del grupo. Una propiedad hipotecada no cobra alquiler (R-14), pero las no hipotecadas del mismo grupo sí.
- **Cifras:** valor hipotecario de cada Escritura; edificios a 1/2.
- **Fuente:** monopoly p. 6 («HIPOTECAS…» y párrafo siguiente).
- **Tío Rico:** difiere (ver R-49).

### R-32 · Levantar la hipoteca
- **Regla:** para levantar la hipoteca se paga al Banco el valor de la hipoteca **más un 10 %** de interés. Cuando ningún solar de un grupo esté hipotecado, el dueño puede volver a comprar casas al Banco a precio completo.
- **Cifras:** hipoteca + 10 %.
- **Fuente:** monopoly p. 6 (última línea) y p. 7 (primer párrafo).
- **Tío Rico:** difiere (ver R-50).
- **Sin decir:** cómo se redondea el 10 % si no es entero (lo decide el autor).

### R-33 · Vender una propiedad hipotecada
- **Regla:** quien hipoteca sigue siendo dueño: nadie se la quita levantando la hipoteca. Puede venderla hipotecada a otro jugador al precio que acuerden. El nuevo dueño elige: levantar la hipoteca en el acto pagando capital + 10 %, o pagar ya solo el 10 % de interés y, si la levanta más tarde, pagar capital + **otro** 10 %.
- **Cifras:** 10 %; otro 10 % si se levanta después.
- **Fuente:** monopoly p. 7 (segundo párrafo).
- **Tío Rico:** difiere (ver R-51).

### R-34 · Quiebra ante otro jugador
- **Regla:** un jugador quiebra cuando debe más de lo que puede pagar. Si debe a otro jugador, le entrega todos sus bienes y se retira. Sus casas y hoteles vuelven al Banco a cambio de la mitad de lo que pagó por ellos, y ese dinero va al acreedor. Las propiedades hipotecadas pasan al acreedor, que paga en el acto al Banco el 10 % de interés; después puede levantar la hipoteca pagando el capital o quedársela así y levantarla más adelante; si la mantiene hasta un turno posterior, al levantarla paga nuevos intereses.
- **Cifras:** edificios a 1/2; 10 % de interés inmediato.
- **Fuente:** monopoly p. 7 («QUIEBRA…»).
- **Tío Rico:** igual, con «casas u hoteles» y 10 % de interés (p. 1 «QUIEBRA», párrafos 1 a 3).

### R-35 · Quiebra ante el Banco y fin de la partida
- **Regla:** si debe al Banco (contribuciones o multas que no puede pagar ni vendiendo edificios e hipotecando), entrega todos sus bienes al Banco, que subasta en el acto todo menos los edificios. El jugador quebrado se retira en el acto. **Gana el último jugador que queda.**
- **Cifras:** —
- **Fuente:** monopoly p. 7 (párrafo «Si el jugador es deudor del Banco…»).
- **Tío Rico:** igual, salvo que no subasta «casas y castillos»; «¡El último jugador que queda, gana el juego!» (p. 1 «QUIEBRA», último párrafo).
- **Nota:** R-01 habla del «más rico»; el fin por quiebras es esta regla.

### R-36 · Préstamos
- **Regla:** solo se pide dinero prestado al Banco, hipotecando propiedades. Ningún jugador presta ni pide prestado a otro.
- **Cifras:** —
- **Fuente:** monopoly p. 7 («MISCELANEA…»).
- **Tío Rico:** igual (p. 1 «DEUDAS DE JUEGO»).

### R-37 · Juego corto: preparación
- **Regla:** variante «Juego corto» (60 a 90 minutos). En la preparación se barajan las Escrituras, las corta el jugador a la izquierda del Banquero y el Banquero reparte dos a cada jugador, de una en una. Cada uno paga en el acto al Banco el precio impreso de las dos. Después se juega como en el juego común.
- **Cifras:** 60-90 min; 2 Escrituras por jugador.
- **Fuente:** monopoly p. 8 («REGLAS para un JUEGO CORTO…», punto 1).
- **Tío Rico:** no la tiene (no trae variantes cortas).
- **Opción:** preset o interruptor `shortGame` (F1.3).

### R-38 · Juego corto: hoteles con tres casas
- **Regla:** basta con **tres** casas (no cuatro) en cada solar del grupo completo para comprar un hotel. El alquiler del hotel es el mismo que en el juego común. El hotel se devuelve al Banco siempre a la mitad de su precio de compra, que en este juego es de una casa menos que en el común.
- **Cifras:** 3 casas; devolución a 1/2 (precio del hotel + 3 casas, no 4).
- **Fuente:** monopoly p. 8 (punto 2 y dos párrafos siguientes).
- **Tío Rico:** no la tiene (no trae variantes cortas).

### R-39 · Juego corto: fin y recuento
- **Regla:** la primera quiebra retira al jugador como en el juego común. En la **segunda** quiebra termina la partida: el quebrado entrega a su acreedor (jugador o Banco) todo lo de valor, incluidos edificios. Los demás suman: (1) efectivo; (2) solares, ferrocarriles y servicios públicos a su precio impreso; (3) propiedades hipotecadas a la mitad del precio impreso; (4) casas a su precio de compra; (5) hoteles a su precio de compra, incluido el de las tres casas entregadas. **Gana el más rico.**
- **Cifras:** fin en la 2.ª quiebra; hipotecadas a 1/2; hotel = precio + 3 casas.
- **Fuente:** monopoly p. 8 (punto 3 y párrafo siguiente).
- **Tío Rico:** no la tiene (no trae variantes cortas).

### R-40 · Juego con límite de tiempo
- **Regla:** antes de empezar se fija la hora de terminar; a esa hora gana el jugador más rico. Al inicio se barajan y cortan las Escrituras y el Banquero da dos a cada jugador, que pagan en el acto su precio al Banco.
- **Cifras:** 2 Escrituras por jugador.
- **Fuente:** monopoly p. 8 («EL JUEGO CON LIMITE DE TIEMPO…»).
- **Tío Rico:** no la tiene (no trae variantes cortas).
- **Sin decir:** cómo se cuenta la riqueza al terminar (por defecto, el recuento de R-39; lo confirma el autor).

## Tío Rico Mc Pato

*Instrucciones de «Tío Rico Mc Pato» (Ronda S.A.S.), una página. Solo lleva ficha propia lo que difiere del Monopoly o no está en él; lo igual se marca en la línea «Tío Rico» de cada R-## de arriba.*

### R-41 · Contenido
- **Regla:** 1 tablero, 4 fichas metálicas, 30 casas, 10 castillos, 32 títulos de propiedad, 22 tarjetas «Lotería» y «Sorpresa», 200 billetes en 6 denominaciones y 2 dados.
- **Cifras:** 4 fichas (máximo 4 jugadores con las piezas de la caja), 30 casas, 10 castillos, 32 títulos, 22 tarjetas, 200 billetes, 6 denominaciones, 2 dados.
- **Fuente:** tio_rico p. 1 r0-67-50-33 («Contenido»).
- **Monopoly:** difiere de R-02 (32 casas y 12 hoteles).

### R-42 · Preparación y banquero
- **Regla:** las cartas de Lotería y Sorpresa van en su sitio del tablero. Cada jugador elige su ficha y la lleva a la Estación Santa Fe (la salida). El Banquero **debe ser exclusivo** (no juega): maneja el dinero y vende, compra, subasta e hipoteca propiedades, casas y castillos. Reparte a cada jugador **3 billetes por denominación**.
- **Cifras:** 3 billetes × 6 denominaciones.
- **Fuente:** tio_rico p. 1 r40-38-20-62 («PREPARACIÓN DEL JUEGO»).
- **Monopoly:** difiere de R-03 y R-04.
- **Sin decir:** el valor de las 6 denominaciones. El autor lo leyó de los billetes: $100, $200, $500, $1000, $2000 y $5000 → dinero inicial 3 × 8800 = $26 400 (D-08).
- **Opción:** `startingMoney` (F1.3).

### R-43 · Quién empieza, sentido del turno y «par»
- **Regla:** empezando por el banquero, cada jugador lanza «el dado» por turnos; empieza el de mayor número y los demás siguen **por su derecha**. «Todo par autoriza un nuevo lanzamiento.»
- **Cifras:** —
- **Fuente:** tio_rico p. 1 r59-22-20-78 («INICIANDO EL JUEGO»).
- **Monopoly:** difiere de R-06 (allí el turno pasa a la izquierda).
- **Ambigua:** «par» puede ser un empate en la tirada inicial (se repite) o los dobles (dan otra tirada, sin el tope de tres de R-09). Lo decide el autor. También choca que el banquero «lance» si es exclusivo (R-42).
- **Opción:** `turnDirection` (F1.3).

### R-44 · Subasta con base
- **Regla:** si quien cae en una propiedad del Banco no la compra, el Banco la subasta con **base en su valor menos $200**.
- **Cifras:** base = precio − $200.
- **Fuente:** tio_rico p. 1 r59-22-20-78 («COMPRA DE PROPIEDADES»).
- **Monopoly:** difiere de R-12 (cualquier precio inicial).
- **Opción:** `auctionBaseDiscount` (F1.3).

### R-45 · Sueldo
- **Regla:** cada vez que la ficha cae en la Salida o pasa por ella, por los dados o por una tarjeta, el banquero le paga un sueldo.
- **Cifras:** $2000 (impreso «$2.000»).
- **Fuente:** tio_rico p. 1 r59-22-20-78 («SUELDO»).
- **Monopoly:** difiere de R-10 ($200; y no dice «una vez por vuelta»).
- **Opción:** `salary` (F1.3).

### R-46 · Construir casas
- **Regla:** con las 3 propiedades del mismo color, **al llegar a una de ellas** se pueden construir hasta 3 casas. Cada casa vale $1000.
- **Cifras:** grupo de 3; hasta 3 casas; $1000 por casa (igual para todas).
- **Fuente:** tio_rico p. 1 r59-22-20-78 («CONSTRUCCIÓN DE CASAS»).
- **Monopoly:** difiere de R-25 y R-26 (en cualquier momento, parejo, 4 casas, precio por Escritura).
- **Sin decir:** si «hasta 3 casas» es por propiedad o en total, y si hay que construir parejo.
- **Opción:** `buildOnlyWhenLanding`, `maxHouses` (F1.3).

### R-47 · Castillos
- **Regla:** al llegar a una propiedad que ya tiene sus 3 casas, se le puede construir un castillo. Valor de cada castillo: las 3 casas más $2000. Casas y castillos se ponen sobre los títulos.
- **Cifras:** castillo = 3 casas + $2000 (= $5000 con casas de $1000, R-46).
- **Fuente:** tio_rico p. 1 r59-22-20-78 («CONSTRUCCIÓN DE CASTILLOS»).
- **Monopoly:** difiere de R-27 (hotel con 4 casas y precio por Escritura).
- **Sin decir:** si las 3 casas se devuelven al Banco al poner el castillo.

### R-48 · Alquiler doble con castillos
- **Regla:** «Cada vez que un jugador construya un castillo en cada propiedad del mismo color, puede cobrar doble alquiler por su propiedad.»
- **Cifras:** ×2.
- **Fuente:** tio_rico p. 1 r59-22-20-78 («ALQUILER DOBLE»).
- **Monopoly:** no la tiene.
- **Ambigua:** si el doble se aplica al tener castillo en todas las del grupo o por cada castillo. Lo decide el autor.

### R-49 · Hipoteca
- **Regla:** quien no tiene liquidez puede hipotecar al Banco propiedades, casas y castillos, por **la mitad del precio total** de la propiedad; las conserva con el título boca abajo. Mientras dure la hipoteca no puede edificar, pero **sigue cobrando alquiler**.
- **Cifras:** 1/2 del precio total.
- **Fuente:** tio_rico p. 1 r59-22-20-78 («HIPOTECA»).
- **Monopoly:** difiere de R-14 y R-31 (valor impreso; sin alquiler; antes se venden los edificios).
- **Opción:** `rentWhileMortgaged` (F1.3).

### R-50 · Levantar la hipoteca
- **Regla:** se paga al Banco el valor de la hipoteca más $200 de gastos e intereses.
- **Cifras:** hipoteca + $200.
- **Fuente:** tio_rico p. 1 r59-22-20-78 («LEVANTAMIENTO DE HIPOTECAS»).
- **Monopoly:** difiere de R-32 (+10 %).
- **Opción:** `unmortgageFee` (F1.3).

### R-51 · Venta entre jugadores
- **Regla:** los jugadores pueden venderse propiedades, casas y castillos con base en su valor de compra o en su hipoteca; si está hipotecada, el nuevo dueño responde por ella. Por cada propiedad, casa o castillo vendido se pagan derechos «a Tribilín, Tío Rico y a Hugo, Paco y Luis, de $200 c/u».
- **Cifras:** $200 de derechos.
- **Fuente:** tio_rico p. 1 r59-22-20-78 («VENTA DE PROPIEDADES»).
- **Monopoly:** difiere de R-29 y R-33 (precio libre, sin edificios, sin derechos).
- **Ambigua:** «$200 c/u» puede ser $200 por cada cosa vendida o $200 a cada uno de los tres destinatarios ($600). Quién paga (vendedor o comprador) no se dice.

### R-52 · Tierra del Futuro
- **Regla:** quien cae en esta casilla paga $1500 más $200 por cada castillo que tenga, «al centro espacial de Hugo, Paco y Luis».
- **Cifras:** $1500 + $200 por castillo.
- **Fuente:** tio_rico p. 1 r78-22-22-78 («TIERRA DEL FUTURO»).
- **Monopoly:** difiere de R-19 (impuesto de $200 o 10 %).
- **Sin decir:** a quién va el dinero en el juego (por defecto, al Banco).

### R-53 · Tierra de la Aventura
- **Regla:** quien cae en esta casilla paga $1800 «a la Aldea de Tribilín en la selva».
- **Cifras:** $1800.
- **Fuente:** tio_rico p. 1 r78-22-22-78 («TIERRA DE LA AVENTURA»).
- **Monopoly:** difiere de R-19.

### R-54 · Tierra de la Frontera
- **Regla:** quien cae en esta casilla paga $2000 «a Tío Rico para sus minas».
- **Cifras:** $2000.
- **Fuente:** tio_rico p. 1 r78-22-22-78 («TIERRA DE LA FRONTERA»).
- **Monopoly:** difiere de R-19.

## Diferencias

*F1.3. Cada diferencia entre los reglamentos es una opción de la configuración, con su valor en cada preset (F2.8). ◇ = ningún reglamento lo dice o es ambiguo: lo decidió el autor en D-08 (tabla aprobada el 2026-10-06). Los nombres de las opciones son los del código (Kotlin).*

### Opciones de reglas

| # | Regla | Monopoly | Tío Rico | Opción | Clásico | Tío Rico |
|---|---|---|---|---|---|---|
| 1 | R-03, R-42 | $1500 por jugador | 3 billetes × 6 denominaciones (valores sin decir) | `startingMoney` | 1500 | ◇ 26 400 = 3 × (100 + 200 + 500 + 1000 + 2000 + 5000) |
| 2 | R-10, R-45 | $200 al caer o pasar por la salida | $2000 | `salary` | 200 | 2000 |
| 3 | R-06, R-43 | turno a la izquierda | turno a la derecha | — (en la app el orden es el de la lista de jugadores) | — | — |
| 4 | R-06, R-43 | empieza el mayor; empate sin decir | empieza el mayor; «todo par…» ambiguo | `startTieRule` | ◇ repiten solo los empatados | ◇ igual |
| 5 | R-09, R-43 | dobles: otra tirada; 3 seguidos → Cárcel | «todo par autoriza un nuevo lanzamiento» | `doublesRollAgain` · `doublesToJail` | sí · 3 | ◇ sí · 0 («par» = dobles, sin tope) |
| 6 | R-20..R-23 | Cárcel, «Váyase a la Cárcel», de visita | no hay Cárcel | `jail` | sí | no |
| 7 | R-22 | multa $50; máximo 3 turnos dentro | — | `jailFine` · `jailMaxTurns` | 50 · 3 | — |
| 8 | R-12, R-44 | subasta desde cualquier precio | subasta con base = precio − $200 | `auctionBase` | 0 | precio − 200 |
| 9 | R-12 | nadie puja: sin decir | sin decir | `unsoldStaysWithBank` | ◇ sí | ◇ sí |
| 10 | R-14, R-49 | hipotecada no cobra alquiler | hipotecada sí cobra | `rentWhileMortgaged` | no | sí |
| 11 | R-31, R-49 | valor impreso en la Escritura; antes se venden los edificios | 1/2 del precio total, edificios incluidos | `mortgageValue` | impreso | mitad del total |
| 12 | R-31, R-49 | hipoteca solo sin edificios | hipoteca propiedades, casas y castillos; no se edifica mientras dure | `mortgageWithBuildings` | no | sí |
| 13 | R-32, R-50 | levantar = hipoteca + 10 % | hipoteca + $200 | `unmortgageFee` | 10 % | 200 fijo |
| 14 | R-32 | redondeo del 10 % sin decir | — | `percentRounding` | ◇ hacia arriba, a $1 | — |
| 15 | R-25, R-46 | construir en cualquier momento | solo al caer en una del grupo | `buildOnlyWhenLanding` | no | sí |
| 16 | R-26, R-46 | hasta 4 casas por solar | hasta 3 (por propiedad: R-47 habla de «una propiedad que ya tiene sus 3 casas») | `maxHouses` | 4 | 3 |
| 17 | R-26, R-46 | construir y vender parejo | sin decir | `evenBuild` | sí | ◇ no |
| 18 | R-25, R-46 | precio de la casa en cada Escritura | $1000 todas | `housePrice` | por casilla | 1000 fijo |
| 19 | R-27, R-47 | hotel: 4 casas + precio de la Escritura; se devuelven las 4 | castillo: 3 casas + $2000; devolución sin decir | `hotelPrice` · `hotelReturnsHouses` | por casilla · sí | 2000 fijo · ◇ sí |
| 20 | R-02, R-28, R-41 | 32 casas y 12 hoteles; escasez → subasta | 30 casas y 10 castillos; escasez sin decir | `houseStock` · `hotelStock` | 32 · 12 | ◇ 30 · 10 (misma escasez) |
| 21 | R-16 | grupo completo sin construir: alquiler ×2 | igual | `groupDoubleRent` | sí | sí |
| 22 | R-48 | — | castillo en todas las del grupo: alquiler ×2 | `hotelGroupDoubleRent` | no | ◇ sí, con castillo en todas |
| 23 | R-30 | edificios al Banco a 1/2 | sin decir | `sellBuildingsToBank` | sí, a 1/2 | ◇ sí, a 1/2 |
| 24 | R-29, R-51 | entre jugadores solo terrenos, a precio libre | también casas y castillos, «con base en su valor» | `tradeBuildings` | no | sí |
| 25 | R-51 | sin derechos | $200 «c/u» por cada cosa vendida; quién paga, sin decir | `tradeFee` | 0 | ◇ 200 por cosa vendida, lo paga el vendedor al Banco |
| 26 | R-33, R-51 | el comprador de una hipotecada paga ya el 10 % | «el nuevo dueño responde por ella» | `mortgagedTradeInterest` | 10 % | 0 |
| 27 | R-05 | el Banco nunca se arruina | sin decir | `bankUnlimited` | sí | ◇ sí |
| 28 | R-17 | alquiler que no se reclama se pierde | sin decir | `rentMustBeClaimed` | ◇ no (cobro automático) | ◇ no |
| 29 | R-24 | Parada Libre: nada | no hay | `freeParkingPot` (regla casera) | ◇ no | — |
| 30 | R-35, R-37..R-40 | fin: queda uno; variantes juego corto y límite de tiempo | queda uno | `endCondition` | último en pie (corto y tiempo, opcionales) | último en pie |
| 31 | R-37, R-40 | juego corto o con tiempo: 2 Escrituras a cada uno, pagadas | — | `startingDeeds` | 0 (2 en corto y tiempo) | 0 |
| 32 | R-38 | juego corto: hotel con 3 casas | — | `maxHouses` (= 3 en corto) | 4 (3 en corto) | — |
| 33 | R-40 | recuento al acabar el tiempo: sin decir | — | `wealthCount` | ◇ el de R-39 | — |
| 34 | R-02, R-41 | fichas para 8 (sin cifra) | 4 fichas | `players` (D-05) | 2-6 | ◇ 2-6 (la app no usa fichas físicas) |

### Casillas y cartas (van en el tablero del preset, no en las opciones)

| Regla | Monopoly | Tío Rico | Dónde va |
|---|---|---|---|
| R-19, R-52..R-54 | Impuesto: $200 o 10 % del patrimonio, a elegir antes de contar | Tierra del Futuro $1500 + $200 por castillo; de la Aventura $1800; de la Frontera $2000 | Casilla `Tax` con `fixed`, `percent`, `perHotel` |
| R-18 | Casualidad y Arca Comunal, con «Salir libre de la Cárcel» | Lotería y Sorpresa | Mazos del preset (F2.5); el texto de las cartas lo da el autor |
| R-07 | Salida «GO» | «Estación Santa Fe» | Nombre de la casilla `Start` |
| R-52 | — | el dinero de las Tierras va «al centro espacial…», etc. | ◇ al Banco |

Lo que no cambia entre los dos juegos (R-01, R-07, R-08, R-11, R-13, R-15, R-34, R-35, R-36) no lleva opción.
