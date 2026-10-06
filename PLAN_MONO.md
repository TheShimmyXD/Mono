# Proyecto Mono — Plan de trabajo

*Versión 1 · 2026-10-06 · Carpeta: `0_SP_Codes/Project_Mono`*
*Encargo original: `PROMPT_PLANEACION.md`. Proyecto hermano del que se heredan las skills: Azorian (../../0_Python_Codes/Ogata_lib).*

---

## 0. El nombre clave

**Mono.** Nombre descriptivo elegido por el autor (2026-10-06), el mismo de la carpeta `Project_Mono`: viene de *Monopoly*. Se ofrecieron Ivy Bells (el cable soviético pinchado: dos aparatos conectados en secreto, como el Bluetooth) y Gold (el túnel de Berlín); el autor prefirió el descriptivo.

| Pieza | Nombre |
|---|---|
| Proyecto | `mono` |
| Skill del agente | `agente-mono` («despliega Mono», «¿en qué vamos?», «sigue con Mono») |
| Skill del observador | `observador-mono` («inspecciona lo hecho con el observador») |
| Carpeta de trabajo del agente | `Agente_Mono/` |
| Configuración compartida | `mono.toml` |

---

## 1. Visión y alcance

**Qué es.** Un juego de propiedades para Android, nacido del Tío Rico y del Monopoly, en el que todo se edita: los nombres y precios de las casillas, las reglas (salario, cárcel, hipotecas, subastas…) y el tamaño del tablero (un anillo de N casillas). Un motor de reglas en Kotlin puro, sacado de los dos reglamentos y probado en la JVM sin el teléfono, sostiene una interfaz en Jetpack Compose. Primero juegan 2-6 personas pasándose un teléfono; después, dos teléfonos con la app juegan la misma partida por Bluetooth.

**El orden es innegociable:** reglamentos transcritos (F1) → motor sin Android, probado regla por regla (F2) → interfaz (F3) → editor y hito (F4) → Bluetooth (F5).

**Hito de validación («Primera quiebra»):** en su Redmi Note 13 Pro, el autor edita un tablero (cambia el nombre y el precio de una casilla y el número de casillas), juegan 2-4 personas pasándose el teléfono y la partida termina cuando alguien quiebra, sin reglas inventadas (F4.5). Nada de F5 antes de que lo apruebe. Segundo hito, **«Enlace»** (F5.6): el autor y un amigo, cada uno en su teléfono, juegan la misma partida por Bluetooth.

**Fuera del alcance de la versión 1:** jugar contra la máquina, partidas por internet, iOS, publicación en Play Store, arte o marcas de Hasbro y del Tío Rico, animaciones elaboradas. Se anotan como ideas, no se programan.

---

## 2. Qué se hereda y qué cambia

Revisado el 2026-10-06: de **Azorian** (hermano), los SKILL.md del agente y del observador, `mandato.md`, `motor.md` §1-4, el principio de `ogata_indice.md` (cómo leer el PDF), `criterios.md` (A01-A15) y su registro de mejoras (46: 44 aplicadas, 1 propuesta, 1 rechazada); de MkUltra, Venona y vid2aud, el inventario y las «No funcionó» (2, de MkUltra, sobre lecturas grandes y rutas sin `cd`); las lecciones L1-L20 de iniciar-proyecto; y el `diff` entre el kit y los scripts de Azorian (tablero, extractor, auditor).

### 2.1 Se hereda tal cual

| Mecanismo | Por qué | Origen |
|---|---|---|
| ESTADO, SESIONES con id, historial, DECISIONES D-##; plan con casillas y `tablero.py` | Núcleo probado en cuatro proyectos | Patrón §2; Azorian H5 |
| Primero el motor, después la interfaz; el núcleo sin interfaz ni E/S | El juego se prueba sin el teléfono; la interfaz no esconde errores de reglas | Azorian mandato reglas 2 y 3; A03 |
| Cada pieza, su prueba, contra algo independiente (aquí, el reglamento) | Una prueba que compara el código consigo mismo no verifica | Azorian A01, A02, `motor.md` §4 (M-025) |
| La fuente se lee por página y se transcribe una vez | El Ogata (908 pp.) nunca entró entero; aquí, escaneos | Azorian M-007, M-031 |
| Tarea sin *Terminado* → proponerlo medible como pregunta cerrada | Evita marcar `[x]` sin criterio | Azorian M-020; Venona M-011 |
| Pregunta que no bloquea → «Pendiente del autor» y seguir | Sesiones irregulares | Azorian M-034 |
| D-## leída por su encabezado; borrar archivos pidiéndolo con `! git rm` | Economía; el modo automático bloquea borrar | Azorian M-033, M-053 |
| Tono «con ganas, es para divertirse»; lo visible primero, con su comando | Proyecto por gusto | Azorian SKILL («Tono», «Lo visible primero») |
| 2-3 opciones con captura antes de programar una pantalla | El autor eligió achicar cuando se le ofreció | L11, L18 (Venona M-061, vid2aud) |
| `tablero.py`, `cierre_paso.py`, `extraer_sesion.py`, `senales.py` desde el kit | Tres copias divergían | L7 |

### 2.2 Cambia

| # | En Azorian (../../0_Python_Codes/Ogata_lib) | En Mono |
|---|---|---|
| C1 | Python 3.12, NumPy/SciPy, PySide6; `python-programmer` | Kotlin + Jetpack Compose + Gradle (perfil `otro`): sin `python-programmer`, ruff ni pytest (D-01) |
| C2 | Cierre con ruff, pytest, `check_structure` | `[cierre] pasos` del `mono.toml`: `./gradlew :engine:test` y `:app:assembleDebug` (kit ampliado hoy, H2) |
| C3 | «El Ogata guía; la prueba y la eficiencia mandan» | Más estricta: cada regla del motor cita su R-## (reglamento y página) o una D-## del autor (regla 1) |
| C4 | `assert_allclose` con tolerancias con nombre | Reglas discretas: una prueba exacta por R-## con dados fijados, más invariantes (el dinero se conserva) y partidas aleatorias (F2.9) |
| C5 | Estética Ogata (`grafico.md`) | Interfaz Compose: 2-3 opciones con captura del teléfono y el autor elige (`interfaz.md`) |
| C6 | `search_ogata.py` sobre el texto del PDF | Los reglamentos no tienen texto: `pagina.py` renderiza una página o un recorte a PNG en el scratchpad |
| C7 | `medir_margenes`, `medir_ventana`, `huella_escenas`, `code_map` | No aplican (son de Python); un mapa del código Kotlin, si hace falta, es *(Extra)* |
| C8 | Dos integradores y microbenchmarks | Determinismo: estado serializable y azar solo por semilla inyectada, base del Bluetooth (D-03) |
| C9 | Un hito (el péndulo) | Dos: «Primera quiebra» (local) y «Enlace» (Bluetooth) |
| C10 | Ventana Qt en la misma máquina | El Redmi por `adb` (o el emulador `Medium_Phone`): `telefono.py` instala, captura y lee el log filtrado |

### 2.3 Hallazgos (lo que falla o no aplica, y cómo se corrige)

| # | Hallazgo (comprobado) | Consecuencia | Corrección |
|---|---|---|---|
| H1 | El kit no traía M-017 de Azorian: `commands_after` contaba solo los comandos *posteriores* a la escritura | Falso «no corrió el cierre» cuando `sed -i … && cierre_paso.py` va en un comando | Llevado al kit con su prueba (autor, 2026-10-06) |
| H2 | `cierre_paso.py` del kit no corre nada en perfiles que no son de Python | Un paso de Kotlin cerraría sin pruebas ni compilación | `[cierre] pasos` y `entorno` en el kit y en la plantilla del `.toml`, con prueba (autor, 2026-10-06) |
| H3 | La carpeta se renombró durante la sesión 0 (`Proyect_Mono` → `Project_Mono`) | La transcripción de la sesión 0 quedó con el nombre viejo; `--actual` no la encuentra (L8) | Calibración con el id de la sesión; lección L21 |
| H4 | Los dos reglamentos son escaneos sin texto (0 fuentes; 16 y 3 imágenes) y viven en `~/Descargas` | No se pueden buscar con `grep`; una limpieza de Descargas los borraría | F0.1 los copia a `fuentes/` (intocable, fuera de Git); `pagina.py` lee por página o recorte; `REGLAS.md` es la transcripción única |
| H5 | `java` y `adb` no están en el PATH; Android Studio trae JBR 25 (`~/android-studio/jbr`) y el SDK está en `~/Android/Sdk` | Gradle y los scripts fallarían desde la terminal del agente | `[cierre] entorno` (`JAVA_HOME`) y `[android] sdk` en `mono.toml`; los scripts no dependen del PATH |
| H6 | `adb devices` sin dispositivos (2026-10-06) | No se puede instalar en el Redmi todavía | F0.4: depuración USB (HyperOS pide además «Instalar vía USB») |
| H7 | Calibración del observador sobre la sesión 0: K11 marcaba `2>/dev/null` y el cuerpo de los heredocs como escrituras en `fuentes/`; `(^\|/)fuentes/` no casaba con rutas relativas en una orden | Falsas alarmas «Alta» en cada sesión | `DESTRUCTIVE` y `strip_heredocs` corregidos en el kit y en Mono, con pruebas; patrones `(^\|[/\s'"])` en `mono.toml` |

---

## 3. Fase S — Las dos skills

```
mono/
├── PROMPT_PLANEACION.md, PLAN_MONO.md, mono.toml
├── .claude/skills/
│   ├── agente-mono/{SKILL.md, references/, scripts/, tests/}
│   └── observador-mono/{SKILL.md, references/criterios.md, scripts/, tests/}
└── Agente_Mono/
    ├── ESTADO.md, ESTADO_historial.md, SESIONES.md, DECISIONES.md
    └── Observador/{trazas/, informes/, Mejoras_Propuestas.md}
```

Topes: SKILL.md 12000 car., referencias 8000, ESTADO 5000. Criterios del observador: K01-K11 comunes (kit) y K12+ propios.

### S · Skills de Mono

- [x] **S1.1** `mono.toml` y `Agente_Mono/` (ESTADO, SESIONES, historial, DECISIONES D-01..., Observador). *Terminado:* `tablero.py` cuenta este plan.
- [x] **S1.2** `agente-mono`: SKILL.md, `mandato.md`, referencias por fase, scripts con pruebas. *Terminado:* dentro de los topes, sin huecos, pruebas en verde.
- [x] **S1.3** `observador-mono`: SKILL.md, `criterios.md`, scripts con pruebas, calibrado sobre la sesión de creación. *Terminado:* `senales.py` corre sobre esta sesión y sus falsos positivos están corregidos.
- [x] **S2.1** Prueba en frío: conversación nueva **abierta en esta carpeta**, «despliega Mono». *Terminado:* el tablero sale bien y el agente arranca F0.1 sin preguntas.
- [x] **S2.2** Primera pasada del observador sobre S2.1. *Terminado:* informe `INF-<fecha>-<id8>.md`.

---

## 4. Arquitectura y decisiones iniciales

Capas: `engine/` (Kotlin/JVM puro: configuración, estado, acciones, reglas) → `app/` (Android: Compose, guardado, después Bluetooth). `engine` no importa nada de Android.

- **D-01 · Pila.** Kotlin + Jetpack Compose en Android Studio · elegida por el autor (2026-10-06): lo nativo de Android hoy, Bluetooth con la API del sistema, y el motor probado en la JVM.
- **D-02 · Dos módulos.** `engine` sin Android + `app` · las reglas se prueban en segundos sin teléfono y no se mezclan con la interfaz (Azorian D-04).
- **D-03 · Motor determinista.** Estado inmutable + acción → estado nuevo; dados y barajado con una semilla inyectada · reproduce cualquier partida en una prueba y, en F5, el anfitrión decide y los dos teléfonos aplican las mismas acciones.
- **D-04 · Reglas con fuente.** Catálogo `REGLAS.md` (R-##: texto, cifras, reglamento y página o recorte); donde Tío Rico y Monopoly difieren, una opción de configuración con su valor en cada preset · regla 1 del mandato.
- **D-05 · Jugadores y tablero.** 2-6 jugadores; tablero en anillo de N casillas · autor (P1-P2). Primero en un teléfono; Bluetooth después del hito (autor).
- Pendiente para F5.1: transporte Bluetooth (clásico RFCOMM o Nearby Connections).

---

## 5. Hoja de ruta

### F0 · Taller

- [x] **F0.1** Copiar los dos reglamentos a `fuentes/` (solo lectura), `git init` y `.gitignore` (Android, más `Agente_Mono/ESTADO*.md`, `SESIONES.md`, `Observador/trazas/`, `fuentes/`, `*.jks`, `keystore.properties`, `local.properties`). *Terminado:* `sha256sum` de las copias igual al de Descargas; `git status` no muestra `fuentes/` ni ESTADO.
- [x] **F0.2** Proyecto Gradle con `engine` (Kotlin/JVM) y `app` (Android, Compose), versiones en `gradle/libs.versions.toml` y una D-## con ellas (AGP, Kotlin, minSdk). *Terminado:* `./gradlew :engine:test :app:assembleDebug` termina en BUILD SUCCESSFUL con una prueba del motor.
- [x] **F0.3** `[cierre] pasos` y `[android] sdk` en `mono.toml`; hook `pre-commit` con el tope de ESTADO. *Terminado:* `cierre_paso.py` dice «listo para el commit» y un commit con ESTADO sobre el tope es rechazado.
- [x] **F0.4** «Hola Mono» en el Redmi: depuración USB, `telefono.py instalar` y `telefono.py captura`. *Terminado:* captura del Redmi con la app abierta, y el autor la ve en su teléfono.

### F1 · Reglamentos

- [x] **F1.1** Leer el Monopoly página por página (8 pp.) y transcribir cada regla a `REGLAS.md` como R-## (texto breve, cifras, página). *Terminado:* las 8 páginas leídas y listadas; cada R-## con su página; cada cifra leída dos veces.
- [x] **F1.2** Leer el Tío Rico por recortes de su única página y añadir sus R-## (o «igual a R-##» / «difiere de R-##»). *Terminado:* los recortes cubren toda la página y están listados en `REGLAS.md`; cada regla con su recorte.
- [x] **F1.3** Tabla de diferencias Tío Rico ↔ Monopoly: cada diferencia, una opción de configuración con su valor en cada preset. *Terminado:* el autor aprueba la tabla.
- [x] **F1.4** Qué es editable (casillas: nombre, precio, grupo, alquileres; reglas: opciones de F1.3; tamaño: N) y sus rangos. *Terminado:* ficha D-## aprobada por el autor.

### F2 · Motor

- [x] **F2.1** Modelo: configuración (tablero, tipos de casilla, reglas), estado de la partida y acciones, con serialización JSON. *Terminado:* ida y vuelta JSON idéntica de un tablero y de una partida, en prueba.
- [x] **F2.2** Dados con semilla, movimiento en el anillo de N casillas, salida y salario. *Terminado:* una prueba por R-## con dados fijados; misma semilla, misma partida.
- [x] **F2.3** Comprar, alquiler, grupos completos, casas y hoteles. *Terminado:* una prueba por R-## de la tarea.
- [x] **F2.4** Cárcel, impuestos y casillas especiales. *Terminado:* una prueba por R-## de la tarea.
- [x] **F2.5** Cartas: mazos del preset, barajado con semilla. *Terminado:* una prueba por R-## de la tarea.
- [x] **F2.6** Hipotecas, subasta (si la opción está activa), quiebra y fin. *Terminado:* una prueba por R-## de la tarea.
- [x] **F2.7** Validador de la configuración (N fuera de rango, grupos vacíos, precios negativos, opciones incoherentes) con mensajes en español. *Terminado:* una prueba por error; los presets pasan.
- [ ] **F2.8** Presets «Clásico» (Monopoly) y «Tío Rico» como JSON en `engine`. *Terminado:* cada valor del preset cita su R-##.
- [ ] **F2.9** Simulador: 1000 partidas aleatorias con 2-6 jugadores y N variable. *Terminado:* ninguna excepción; el dinero total se conserva; todas terminan o llegan al tope de turnos; tiempo medido en ESTADO.

### F3 · Interfaz de juego

- [ ] **F3.1** 2-3 maquetas del tablero en anillo, con captura en el teléfono. *Terminado:* el autor elige; D-##.
- [ ] **F3.2** Tablero dibujado desde la configuración (cualquier N) con fichas de 2-6 jugadores. *Terminado:* capturas con N = 20, 40 y 60 aprobadas por el autor.
- [ ] **F3.3** Turno: tirar, mover, comprar o pagar, con diálogos. *Terminado:* una vuelta completa jugada en el Redmi; captura.
- [ ] **F3.4** Panel de jugadores y propiedades (dinero, casas, hipotecas). *Terminado:* captura aprobada por el autor.
- [ ] **F3.5** Nueva partida: preset, 2-6 jugadores y nombres. *Terminado:* partida de 3 jugadores empezada desde el menú.
- [ ] **F3.6** Guardar y retomar. *Terminado:* tras cerrar la app a mitad de partida, vuelve igual.

### F4 · Editor y hito «Primera quiebra»

- [ ] **F4.1** Editor de casillas: nombre, precio, grupo y alquileres. *Terminado:* una casilla editada se ve en el tablero y en el cobro del alquiler.
- [ ] **F4.2** Editor de reglas (las opciones de F1.3). *Terminado:* cambiar el salario cambia lo que se cobra al pasar por la salida.
- [ ] **F4.3** Tamaño del mapa: añadir y quitar casillas, con el validador de F2.7. *Terminado:* un tablero de 24 casillas jugable; uno inválido muestra su error.
- [ ] **F4.4** Guardar, duplicar y elegir tableros propios. *Terminado:* un tablero propio sobrevive a cerrar la app.
- [ ] **F4.5** **Hito «Primera quiebra»** en el Redmi (sección 1). *Terminado:* el autor lo aprueba. Nada de F5 antes.

### F5 · Enlace

- [ ] **F5.1** Transporte: Bluetooth clásico (RFCOMM) o Nearby Connections, con prueba de concepto. *Terminado:* D-## aprobada por el autor.
- [ ] **F5.2** Protocolo: anfitrión que decide, acciones numeradas, el invitado aplica las mismas; probado en la JVM con un transporte falso. *Terminado:* dos motores idénticos tras 1000 partidas simuladas.
- [ ] **F5.3** Permisos (Android 12+), descubrir y conectar; HyperOS no corta la conexión. *Terminado:* conexión de 10 min sin cortes con el teléfono de un amigo.
- [ ] **F5.4** Partida de punta a punta en dos teléfonos. *Terminado:* una partida corta terminada, con captura de los dos.
- [ ] **F5.5** Desconexión y reconexión a mitad de partida. *Terminado:* se apaga el Bluetooth del invitado y la partida sigue igual al reconectar.
- [ ] **F5.6** **Hito «Enlace»**: el autor y un amigo, cada uno en su teléfono. *Terminado:* el autor lo aprueba.
- [ ] **F5.7** *(Extra)* 3-6 jugadores por Bluetooth.
- [ ] **F5.8** *(Extra)* Jugar contra la máquina.

---

## 6. Reglas del proyecto (van a `references/mandato.md`)

**No se negocian:**

1. **Nada se supone de las reglas.** Cada regla del motor cita su R-## de `REGLAS.md` (reglamento y página o recorte) o una D-## del autor; donde los dos juegos difieren, es una opción configurable. *(autor, 2026-10-06; más estricta que Azorian A01)*
2. **Los reglamentos y lo generado nunca entran enteros al contexto.** Una página o un recorte con `pagina.py`; nunca `build/`, `.gradle/`, APK ni el logcat completo (`telefono.py log` filtra). *(autor, 2026-10-06; Azorian M-007; L12)*
3. **Lo intocable.** `fuentes/` es de solo lectura; nada se publica (GitHub, Play Store) y no se crea ni se toca una keystore sin el autor. *(autor, 2026-10-06)*

**Las demás:**

4. **Primero el motor:** no se escribe interfaz para algo que el motor no resuelva ya, probado. *(Azorian regla 2)*
5. **Cada regla, su prueba** en `engine/src/test`, con el R-## en el nombre. *(Azorian regla 3, A02)*
6. **Motor puro y determinista:** `engine` sin Android; azar solo por semilla. *(D-02, D-03)*
7. **Interfaz con opciones y captura:** 2-3 opciones con captura antes de una pantalla nueva; captura del teléfono después de cambiarla. *(L18; vid2aud W12)*
8. **Sin decisiones en silencio:** ficha D-##. *(Azorian regla 5)*
9. **Cada cifra se mide; lo que juzga el autor no lo declaras tú.** *(L17)*
10. **Dependencias** en `gradle/libs.versions.toml`, con versión, en el mismo commit y anotadas en ESTADO. *(Azorian regla 7)*

---

## 7. Riesgos

| Riesgo | Mitigación |
|---|---|
| Reglamentos escaneados: se lee mal una cifra o se salta una regla | `pagina.py` por página o recorte; cada cifra se lee dos veces; el autor aprueba `REGLAS.md` (F1.3) |
| Los originales están en Descargas y se pueden borrar | Copia en `fuentes/`, intocable (F0.1) |
| Bluetooth en Android 12+: permisos en tiempo de ejecución; HyperOS corta conexiones por batería | Fase aparte después del hito; prueba de 10 min (F5.3); sin segundo Android, el protocolo se prueba en la JVM (F5.2) y en real con un amigo |
| El editor permite tableros o reglas incoherentes | Validador en el motor (F2.7) antes del editor (F4) |
| Dos teléfonos con estados distintos tras una desconexión | Motor determinista (D-03), anfitrión que decide, acciones numeradas (F5.2, F5.5) |
| El autor conoce poco Kotlin y Android | El agente explica en una línea cada decisión de Android o Compose al tomarla |
| Marcas registradas si algún día se publica | Presets con nombres propios; nada de arte de Hasbro (P3 pendiente) |

---

## 8. Respuestas del autor (2026-10-06)

- **Pila:** Kotlin + Compose (D-01). **Tamaño:** meses. **Multijugador:** local primero, Bluetooth después del hito. **Nombre:** Mono. **Hermano:** Azorian.
- **Reglamentos:** «tienes los dos en mi carpeta de Descargas» → `Monopoly(Spanish).pdf` y `tio_rico.pdf` (escaneos; H4).
- **P1** Jugadores: 2-6 (D-05).
- **P2** Tablero: anillo de N casillas (D-05; el rango exacto de N sale de F1.4).
- **P3** ¿Se publicará algún día? → **pendiente** (decide cómo se nombran los presets).
- **P4** Segundo Android: no hay → el Bluetooth se prueba con el teléfono de un amigo, y el protocolo, en la JVM.
- **P5** Kotlin y Android: los conoce poco → el agente explica sus decisiones al tomarlas.
- **P6** Nombre visible de la app en el teléfono → **pendiente** («Mono» mientras tanto).

---

## 9. Primer paso exacto

Con la Fase S cerrada: abrir una conversación nueva **dentro de esta carpeta**, escribir **«despliega Mono»** y el agente arranca en **F0.1**.
