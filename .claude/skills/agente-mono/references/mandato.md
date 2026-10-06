# Mandato del proyecto Mono

*Versión 1 · 2026-10-06 · Fuente: `PROMPT_PLANEACION.md`, respuestas del autor y `PLAN_MONO.md`. Si este archivo y el plan difieren, manda el plan; anota la diferencia en ESTADO.*

## 1. Qué es

Un juego de propiedades para Android, «como el tio rico o el monopoly pero para celulares» (autor, 2026-10-06), donde «lo nuevo aca es la personalizacion»: los nombres y precios de las locaciones, las reglas y «hasta el tamaño del mapa» se pueden editar. Un motor de lógica en Kotlin puro, «basado en» las reglas básicas extraídas de los dos reglamentos (`fuentes/`), sostiene una interfaz en Jetpack Compose. Después, «si un amigo se instala la app, pueda conectarse por bt conmigo y jugar conmigo». Teléfono de pruebas: Xiaomi Redmi Note 13 Pro.

## 2. Hito

**«Primera quiebra»** (F4.5): en su Redmi, el autor edita un tablero (el nombre y el precio de una casilla, el número de casillas), juegan 2-4 personas pasándose el teléfono y la partida termina cuando alguien quiebra, sin reglas inventadas. **Nada de F5 (Bluetooth) antes de que lo apruebe.** Segundo hito, **«Enlace»** (F5.6): el autor y un amigo, cada uno en su teléfono.

## 3. Fuera del alcance de la versión 1

Jugar contra la máquina, partidas por internet, iOS, publicación en Play Store, arte o marcas de Hasbro y del Tío Rico, animaciones elaboradas.

## 4. Reglas

1. **Nada se supone de las reglas.** Cada regla del motor cita su R-## de `REGLAS.md` o una D-## del autor; donde los dos juegos difieren, es una opción configurable. *(autor, 2026-10-06)*
2. **Los reglamentos y lo generado nunca entran enteros al contexto.** Página o recorte con `pagina.py`; nunca `build/`, `.gradle/`, APK ni el logcat completo. *(autor, 2026-10-06)*
3. **`fuentes/` es intocable y nada sale sin el autor.** Ni GitHub, ni Play Store, ni keystores. *(autor, 2026-10-06)*
4. **Primero el motor:** no se escribe interfaz para algo que el motor no resuelva ya, probado. *(Azorian regla 2)*
5. **Cada regla, su prueba** en `engine/src/test`, con el R-## en el nombre. *(Azorian regla 3)*
6. **Motor puro y determinista:** `engine` sin Android; azar solo por semilla. *(D-02, D-03)*
7. **Interfaz con opciones y captura:** 2-3 opciones con captura antes de una pantalla nueva; captura después de cambiarla. *(L18; vid2aud W12)*
8. **Cada cifra se mide; lo que juzga el autor no lo declaras tú.** *(L17)*
9. **Dependencias** en `gradle/libs.versions.toml`, con versión, en el mismo commit y anotadas en ESTADO. *(Azorian regla 7)*
- **Sin decisiones en silencio:** fichas D-## en `DECISIONES.md`.
- **Nada sale sin permiso:** ni `push`, ni repositorios, ni publicaciones.

## 5. Decisiones de partida

D-01 Kotlin + Jetpack Compose · D-02 módulos `engine` (JVM puro) y `app` · D-03 motor determinista (estado inmutable, azar por semilla) · D-04 reglas con fuente (`REGLAS.md`, R-##) · D-05 2-6 jugadores, anillo de N casillas, local primero. Detalle en `Agente_Mono/DECISIONES.md`.
