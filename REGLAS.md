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

Monopoly: 8/8 páginas leídas; cifras leídas dos veces (al transcribir y comparando cada ficha con su página), 2026-10-06 `e47b9e54`.

## Monopoly

### R-01 · Objetivo
- **Regla:** gana quien termina siendo el jugador más rico, comprando, alquilando y vendiendo propiedades.
- **Cifras:** —
- **Fuente:** monopoly p. 1 («OBJETIVO…»).
- **Tío Rico:** por leer (F1.2).

### R-02 · Equipo
- **Regla:** un tablero, dos dados, fichas, 32 casas y 12 hoteles; barajas de Casualidad y Arca Comunal; una Escritura de Propiedad por finca; billetes.
- **Cifras:** 2 dados, 32 casas, 12 hoteles.
- **Fuente:** monopoly p. 1 («EL EQUIPO…»).
- **Tío Rico:** por leer (F1.2).

### R-03 · Preparación y dinero inicial
- **Regla:** las cartas de Casualidad y Arca Comunal van boca abajo en su espacio. Cada jugador elige una ficha y recibe $1500; el resto del dinero es del Banco.
- **Cifras:** $1500 = 2×$500 + 2×$100 + 2×$50 + 6×$20 + 5×$10 + 5×$5 + 5×$1 (suma comprobada: 1000 + 200 + 100 + 120 + 50 + 25 + 5 = 1500).
- **Fuente:** monopoly p. 1 («PREPARACION…»).
- **Tío Rico:** por leer (F1.2).

### R-04 · Banquero
- **Regla:** un jugador hace de Banquero y subastador; si también juega, separa su dinero del Banco. Con más de cinco jugadores puede limitarse a ser banquero.
- **Cifras:** más de 5 jugadores.
- **Fuente:** monopoly p. 2 («EL BANQUERO…»).
- **Tío Rico:** por leer (F1.2).
- **Nota:** en la app el Banco lo lleva el motor; esta regla no se programa.

### R-05 · El Banco
- **Regla:** el Banco guarda el dinero, las Escrituras y las casas y hoteles sin comprar. Paga sueldos y gratificaciones; vende y remata (subasta) propiedades; vende casas y hoteles; presta sobre hipotecas. Cobra contribuciones, multas, préstamos, intereses y el precio de lo que vende o remata. **Nunca se arruina:** si se queda sin dinero, emite el que necesite.
- **Cifras:** —
- **Fuente:** monopoly p. 2 («EL BANCO…» y los dos párrafos siguientes).
- **Tío Rico:** por leer (F1.2).

### R-06 · Quién empieza y orden de turnos
- **Regla:** cada jugador tira los dados; empieza el del total mayor. Al terminar su jugada, el turno pasa al jugador de su izquierda.
- **Cifras:** —
- **Fuente:** monopoly p. 2 («EL JUEGO…», 1.ª y 4.ª frases).
- **Tío Rico:** por leer (F1.2).
- **Sin decir:** qué pasa si hay empate en la tirada inicial (lo decide el autor).

### R-07 · Movimiento
- **Regla:** todas las fichas empiezan en «GO» (la Salida). En su turno el jugador tira los dos dados y avanza, en el sentido de la flecha, tantos espacios como la suma. Las fichas se quedan donde caen y desde ahí siguen; varias fichas pueden compartir espacio.
- **Cifras:** —
- **Fuente:** monopoly p. 2 («EL JUEGO…»).
- **Tío Rico:** por leer (F1.2).

### R-08 · Efecto del espacio
- **Regla:** según el espacio en que cae, el jugador puede comprar terrenos u otras propiedades, o debe pagar alquiler o contribuciones, robar una tarjeta de Casualidad o de Arca Comunal, ir a la Cárcel, etc. (detalle en las reglas de cada espacio).
- **Cifras:** —
- **Fuente:** monopoly p. 2 (párrafo «Según sea el espacio…»).
- **Tío Rico:** por leer (F1.2).

### R-09 · Dobles
- **Regla:** con dobles, el jugador avanza normalmente, cumple lo del espacio y vuelve a tirar. Si saca **tres dobles seguidos**, no avanza con la tercera tirada: va directo al espacio «En la Cárcel» (ver la regla de la Cárcel).
- **Cifras:** 3 dobles seguidos.
- **Fuente:** monopoly p. 2 (último párrafo).
- **Tío Rico:** por leer (F1.2).

### R-10 · Sueldo al pasar por GO
- **Regla:** cada vez que la ficha cae en GO (Salida) o pasa por él, por los dados o por una carta, el Banco le paga un sueldo. Se cobra **una sola vez por vuelta**. Ejemplo del reglamento: si al pasar por GO cae en Arca Comunal (2 espacios después) o en Azar (7 después) y la carta dice «Avance hasta GO», cobra $200 por pasar y otros $200 por llegar a GO.
- **Cifras:** $200; Arca Comunal a 2 espacios de GO, Azar a 7 (en el ejemplo).
- **Fuente:** monopoly p. 3 («"GO" (Adelante)…» y párrafo siguiente).
- **Tío Rico:** por leer (F1.2).
- **Opción:** `salary` (si difiere; F1.3).

### R-11 · Compra de una propiedad sin dueño
- **Regla:** quien cae en una propiedad sin dueño puede comprarla al Banco al precio impreso; recibe su Escritura y la pone boca arriba delante de sí.
- **Cifras:** el precio impreso de cada propiedad.
- **Fuente:** monopoly p. 3 («COMPRA DE UNA PROPIEDAD…»).
- **Tío Rico:** por leer (F1.2).

### R-12 · Subasta
- **Regla:** si no la compra, el Banco la subasta en el acto y la vende al mejor postor, que paga al Banco lo ofrecido. Pueden pujar todos, incluso quien rechazó comprarla al precio impreso. La subasta puede empezar con cualquier precio.
- **Cifras:** —
- **Fuente:** monopoly p. 3 (párrafo «Si el jugador opta por no comprar…»).
- **Tío Rico:** por leer (F1.2).
- **Sin decir:** qué pasa si nadie puja (la propiedad sigue del Banco, por defecto; lo confirma el autor).

### R-13 · Alquiler
- **Regla:** quien cae en una propiedad con dueño le paga el alquiler impreso en la Escritura.
- **Cifras:** las de cada Escritura.
- **Fuente:** monopoly p. 3 («LLEGADA A UNA PROPIEDAD CON DUEÑO…»).
- **Tío Rico:** por leer (F1.2).

### R-14 · Sin alquiler si está hipotecada
- **Regla:** un solar hipotecado no cobra alquiler. La hipoteca se marca poniendo la Escritura boca abajo.
- **Cifras:** —
- **Fuente:** monopoly p. 3 (párrafo «Si el solar está hipotecado…»).
- **Tío Rico:** por leer (F1.2).

### R-15 · Alquiler con casas
- **Regla:** con una o más casas, el alquiler es mayor que el del solar sin construir (cifras en cada Escritura).
- **Cifras:** las de cada Escritura.
- **Fuente:** monopoly p. 3 («Nota:…»).
- **Tío Rico:** por leer (F1.2).

### R-16 · Grupo completo: alquiler doble
- **Regla:** quien tiene todas las Escrituras de un grupo de color cobra **doble alquiler** por los solares sin construir de ese grupo. Vale para los solares no hipotecados aunque otro del grupo esté hipotecado.
- **Cifras:** ×2. Ejemplos de grupo: Paseo Tablado y Plaza del Parque; Avenidas Connecticut, Vermont y Oriental.
- **Fuente:** monopoly p. 3 (último párrafo).
- **Tío Rico:** por leer (F1.2).

### R-17 · Plazo para cobrar el alquiler
- **Regla:** casas y hoteles dan alquileres mucho más altos que los solares sin construir. Si el dueño no exige el alquiler antes de que el siguiente jugador tire los dados, ya no puede cobrarlo.
- **Cifras:** —
- **Fuente:** monopoly p. 4 (dos primeros párrafos).
- **Tío Rico:** por leer (F1.2).
- **Opción:** en la app el cobro es automático por defecto; un modo «hay que reclamarlo» sería una opción (lo decide el autor).

### R-18 · Casualidad y Arca Comunal
- **Regla:** quien cae en esos espacios roba la carta de encima de la baraja que toca, cumple lo impreso y la devuelve boca abajo debajo de la baraja. La carta «Salir libre de la Cárcel» se guarda hasta usarla y entonces vuelve debajo de la baraja; quien la tiene puede venderla a otro jugador en cualquier momento al precio que acuerden.
- **Cifras:** —
- **Fuente:** monopoly p. 4 («LLEGADA A LOS ESPACIOS DE "CASUALIDAD" O "ARCA COMUNAL"…» y párrafo siguiente).
- **Tío Rico:** por leer (F1.2).
- **Nota:** el texto de cada carta no está en el reglamento (están en las barajas); lo decide el autor.

### R-19 · Impuesto (contribuciones)
- **Regla:** quien cae en el espacio de contribuciones elige: pagar $200 o el 10 % de su patrimonio total. Patrimonio = efectivo + precio impreso de sus propiedades (hipotecadas o no) + precio de costo de sus edificios. Debe elegir **antes** de sumar sus bienes.
- **Cifras:** $200 o 10 %.
- **Fuente:** monopoly p. 4 («IMPUESTO…» y párrafo siguiente).
- **Tío Rico:** por leer (F1.2).

### R-20 · Ir a la Cárcel
- **Regla:** se va a la Cárcel (1) al caer en «Váyase a la Cárcel», (2) al robar la carta «Váyase a la Cárcel» o (3) al sacar dobles tres veces seguidas (R-09). La ficha va directo a la Cárcel, sin cobrar los $200 de GO aunque lo pase. Ir a la Cárcel termina el turno.
- **Cifras:** $200 no se cobran; 3 dobles.
- **Fuente:** monopoly p. 4 («LA CARCEL…» y párrafo siguiente).
- **Tío Rico:** por leer (F1.2).

### R-21 · De visita
- **Regla:** quien cae en la Cárcel en una jugada normal está «De Visita No Más»: sin castigo, y sigue normal en su próximo turno.
- **Cifras:** —
- **Fuente:** monopoly p. 4 (párrafo «Si en el curso de una jugada normal…»).
- **Tío Rico:** por leer (F1.2).

### R-22 · Salir de la Cárcel
- **Regla:** se sale de cuatro maneras: (1) sacando dobles en alguno de los tres turnos siguientes a entrar: avanza en el acto lo que marcan esos dados y, aunque fueron dobles, **no** vuelve a tirar; (2) usando la carta «Salir libre de la Cárcel» si la tiene; (3) comprándosela a otro jugador y usándola; (4) pagando una multa de $50 antes de tirar en cualquiera de sus dos turnos siguientes. Si en el tercer turno no saca dobles, paga la multa de $50, sale y avanza lo que marcó esa tirada.
- **Cifras:** 3 turnos; multa $50; pago voluntario en los 2 turnos siguientes.
- **Fuente:** monopoly p. 4 (último párrafo) y p. 5 (dos primeros párrafos).
- **Tío Rico:** por leer (F1.2).
- **Opción:** `jailFine` (si difiere; F1.3).

### R-23 · Negocios desde la Cárcel
- **Regla:** estando en la Cárcel se puede comprar y vender propiedades, casas y hoteles, y cobrar alquileres.
- **Cifras:** —
- **Fuente:** monopoly p. 5 (párrafo «Aunque esté en la cárcel…»).
- **Tío Rico:** por leer (F1.2).

### R-24 · Parada Libre
- **Regla:** quien cae en Parada Libre no recibe dinero, propiedad ni premio: es solo un descanso.
- **Cifras:** —
- **Fuente:** monopoly p. 5 («PARADA LIBRE…»).
- **Tío Rico:** por leer (F1.2).
- **Opción:** la regla casera del «bote» en Parada Libre no está en el reglamento: si se quiere, es una opción del autor.

### R-25 · Comprar casas
- **Regla:** quien tiene todas las propiedades de un grupo de color puede comprar casas al Banco y levantarlas en ellas, en cualquier momento, según su dinero. El precio de cada casa está en la Escritura del solar. La primera casa va en cualquier solar del grupo; la segunda, en un solar sin casa de ese grupo o de otro grupo completo suyo. El doble alquiler de R-16 sigue valiendo en los solares sin construir del grupo.
- **Cifras:** precio por casa en cada Escritura.
- **Fuente:** monopoly p. 5 («LAS CASAS…» y tres párrafos siguientes).
- **Tío Rico:** por leer (F1.2).

### R-26 · Construir y vender parejo
- **Regla:** se construye en partes iguales: no se pone una segunda casa en un solar del grupo hasta que todos tengan una, y así fila por fila, **hasta 4 casas por solar**. Del mismo modo, las casas se venden al Banco en partes iguales (ver la venta de propiedades).
- **Cifras:** máximo 4 casas por solar; diferencia máxima de 1 casa entre solares del grupo.
- **Fuente:** monopoly p. 5 (dos últimos párrafos).
- **Tío Rico:** por leer (F1.2).

### R-27 · Hoteles
- **Regla:** para comprar un hotel hay que tener cuatro casas en cada solar del grupo completo. Entonces se compra al Banco para cualquier solar del grupo, devolviendo al Banco las cuatro casas de ese solar y pagando el precio del hotel de la Escritura. Como máximo un hotel por solar.
- **Cifras:** 4 casas en cada solar; 1 hotel por solar; se devuelven 4 casas.
- **Fuente:** monopoly p. 6 («LOS HOTELES…»).
- **Tío Rico:** por leer (F1.2).

### R-28 · Escasez de edificios
- **Regla:** si el Banco no tiene casas, quien quiera construir espera a que otro devuelva o venda casas al Banco. Si quedan pocas casas u hoteles y dos o más jugadores quieren más de los que hay, se subastan al mejor postor.
- **Cifras:** el tope de R-02 (32 casas, 12 hoteles).
- **Fuente:** monopoly p. 6 («LA ESCASEZ DE EDIFICIOS…»).
- **Tío Rico:** por leer (F1.2).
- **Opción:** `limitedBuildings` (tope de casas y hoteles sí/no; lo decide el autor en F1.3).

### R-29 · Venta entre jugadores
- **Regla:** solares sin edificar, ferrocarriles y servicios públicos (pero no edificios) se pueden vender a otro jugador en privado, por lo que acuerden. No se vende un solar de un grupo de color si algún solar de ese grupo tiene edificios: antes hay que venderlos al Banco.
- **Cifras:** —
- **Fuente:** monopoly p. 6 («VENTA DE PROPIEDADES…»).
- **Tío Rico:** por leer (F1.2).

### R-30 · Vender edificios al Banco
- **Regla:** casas y hoteles se revenden al Banco en cualquier momento a **la mitad** de lo que costaron. Las casas de un grupo se venden una a una, parejo y al revés de como se construyeron (R-26). Los hoteles de un grupo se pueden vender todos a la vez y enteros, o de una casa por vez (un hotel equivale a cinco casas), también parejo y al revés.
- **Cifras:** 1/2 del precio; hotel = 5 casas.
- **Fuente:** monopoly p. 6 (párrafos 2 a 4 de «VENTA DE PROPIEDADES»).
- **Tío Rico:** por leer (F1.2).

### R-31 · Hipotecar
- **Regla:** las propiedades sin edificios se hipotecan en cualquier momento y solo con el Banco, que paga el valor hipotecario impreso en la Escritura. Para hipotecar un solar con edificios, antes se venden al Banco, a mitad de precio, todos los edificios de ese solar y de los demás solares del grupo. Una propiedad hipotecada no cobra alquiler (R-14), pero las no hipotecadas del mismo grupo sí.
- **Cifras:** valor hipotecario de cada Escritura; edificios a 1/2.
- **Fuente:** monopoly p. 6 («HIPOTECAS…» y párrafo siguiente).
- **Tío Rico:** por leer (F1.2).

### R-32 · Levantar la hipoteca
- **Regla:** para levantar la hipoteca se paga al Banco el valor de la hipoteca **más un 10 %** de interés. Cuando ningún solar de un grupo esté hipotecado, el dueño puede volver a comprar casas al Banco a precio completo.
- **Cifras:** hipoteca + 10 %.
- **Fuente:** monopoly p. 6 (última línea) y p. 7 (primer párrafo).
- **Tío Rico:** por leer (F1.2).
- **Sin decir:** cómo se redondea el 10 % si no es entero (lo decide el autor).

### R-33 · Vender una propiedad hipotecada
- **Regla:** quien hipoteca sigue siendo dueño: nadie se la quita levantando la hipoteca. Puede venderla hipotecada a otro jugador al precio que acuerden. El nuevo dueño elige: levantar la hipoteca en el acto pagando capital + 10 %, o pagar ya solo el 10 % de interés y, si la levanta más tarde, pagar capital + **otro** 10 %.
- **Cifras:** 10 %; otro 10 % si se levanta después.
- **Fuente:** monopoly p. 7 (segundo párrafo).
- **Tío Rico:** por leer (F1.2).

### R-34 · Quiebra ante otro jugador
- **Regla:** un jugador quiebra cuando debe más de lo que puede pagar. Si debe a otro jugador, le entrega todos sus bienes y se retira. Sus casas y hoteles vuelven al Banco a cambio de la mitad de lo que pagó por ellos, y ese dinero va al acreedor. Las propiedades hipotecadas pasan al acreedor, que paga en el acto al Banco el 10 % de interés; después puede levantar la hipoteca pagando el capital o quedársela así y levantarla más adelante; si la mantiene hasta un turno posterior, al levantarla paga nuevos intereses.
- **Cifras:** edificios a 1/2; 10 % de interés inmediato.
- **Fuente:** monopoly p. 7 («QUIEBRA…»).
- **Tío Rico:** por leer (F1.2).

### R-35 · Quiebra ante el Banco y fin de la partida
- **Regla:** si debe al Banco (contribuciones o multas que no puede pagar ni vendiendo edificios e hipotecando), entrega todos sus bienes al Banco, que subasta en el acto todo menos los edificios. El jugador quebrado se retira en el acto. **Gana el último jugador que queda.**
- **Cifras:** —
- **Fuente:** monopoly p. 7 (párrafo «Si el jugador es deudor del Banco…»).
- **Tío Rico:** por leer (F1.2).
- **Nota:** R-01 habla del «más rico»; el fin por quiebras es esta regla.

### R-36 · Préstamos
- **Regla:** solo se pide dinero prestado al Banco, hipotecando propiedades. Ningún jugador presta ni pide prestado a otro.
- **Cifras:** —
- **Fuente:** monopoly p. 7 («MISCELANEA…»).
- **Tío Rico:** por leer (F1.2).

### R-37 · Juego corto: preparación
- **Regla:** variante «Juego corto» (60 a 90 minutos). En la preparación se barajan las Escrituras, las corta el jugador a la izquierda del Banquero y el Banquero reparte dos a cada jugador, de una en una. Cada uno paga en el acto al Banco el precio impreso de las dos. Después se juega como en el juego común.
- **Cifras:** 60-90 min; 2 Escrituras por jugador.
- **Fuente:** monopoly p. 8 («REGLAS para un JUEGO CORTO…», punto 1).
- **Tío Rico:** por leer (F1.2).
- **Opción:** preset o interruptor `shortGame` (F1.3).

### R-38 · Juego corto: hoteles con tres casas
- **Regla:** basta con **tres** casas (no cuatro) en cada solar del grupo completo para comprar un hotel. El alquiler del hotel es el mismo que en el juego común. El hotel se devuelve al Banco siempre a la mitad de su precio de compra, que en este juego es de una casa menos que en el común.
- **Cifras:** 3 casas; devolución a 1/2 (precio del hotel + 3 casas, no 4).
- **Fuente:** monopoly p. 8 (punto 2 y dos párrafos siguientes).
- **Tío Rico:** por leer (F1.2).

### R-39 · Juego corto: fin y recuento
- **Regla:** la primera quiebra retira al jugador como en el juego común. En la **segunda** quiebra termina la partida: el quebrado entrega a su acreedor (jugador o Banco) todo lo de valor, incluidos edificios. Los demás suman: (1) efectivo; (2) solares, ferrocarriles y servicios públicos a su precio impreso; (3) propiedades hipotecadas a la mitad del precio impreso; (4) casas a su precio de compra; (5) hoteles a su precio de compra, incluido el de las tres casas entregadas. **Gana el más rico.**
- **Cifras:** fin en la 2.ª quiebra; hipotecadas a 1/2; hotel = precio + 3 casas.
- **Fuente:** monopoly p. 8 (punto 3 y párrafo siguiente).
- **Tío Rico:** por leer (F1.2).

### R-40 · Juego con límite de tiempo
- **Regla:** antes de empezar se fija la hora de terminar; a esa hora gana el jugador más rico. Al inicio se barajan y cortan las Escrituras y el Banquero da dos a cada jugador, que pagan en el acto su precio al Banco.
- **Cifras:** 2 Escrituras por jugador.
- **Fuente:** monopoly p. 8 («EL JUEGO CON LIMITE DE TIEMPO…»).
- **Tío Rico:** por leer (F1.2).
- **Sin decir:** cómo se cuenta la riqueza al terminar (por defecto, el recuento de R-39; lo confirma el autor).
