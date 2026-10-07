# Criterios del observador de Mono

Cada criterio indica su **origen**, cómo se **detecta** y la **mejora típica**. Un criterio nuevo solo entra con un origen verificable: el autor, una regla escrita de una skill, o una propuesta del observador aprobada. Los comunes (K01-K11) vienen del kit de `iniciar-proyecto` y tienen el mismo número en todos los proyectos nuevos; su genealogía (C## de MkUltra, A## de Azorian, V## de Venona, W## de vid2aud) está en `~/.claude/skills/iniciar-proyecto/references/criterios_base.md`.

| ID | Criterio | Origen | Detección | Mejora típica |
|---|---|---|---|---|
| K01 | Ningún dato personal ni secreto del autor en el proyecto, las muestras, las trazas o los commits | `python-programmer` regla 5; Venona V01 | Auto: lo enmascarado por `extraer_sesion.py` | Regla o script que evite el paso por el chat |
| K03 | Cada paso cierra con `cierre_paso.py` | SKILL del agente; Venona V07 | Auto: tras la última escritura en `codigo` del `.toml` falta el cierre | Recordatorio en el punto de control |
| K04 | Punto de control: ESTADO después del trabajo; entrada en SESIONES con el id al cerrar | SKILL del agente; MkUltra C06 | Auto | Reforzar «terminar paso → guardar → seguir» |
| K05 | Economía de contexto: nada de `no_leer` en el contexto; salidas acotadas; sin compactaciones | Regla 2 del mandato; MkUltra C07 | Auto: lecturas que casan con `[observador].no_leer`; resultados > 8000 car.; compactaciones | Un script o filtro que resuma en vez de leer |
| K06 | Los errores de herramientas se entienden y no se repiten | MkUltra C08 | Auto: llamadas con error | Documentar el error recurrente en la skill que lo provoca |
| K07 | Verificar antes de escribir | Autor (MkUltra C10) | Auto (Info): autocorrecciones del asistente | Mover la verificación al paso previo |
| K08 | Las correcciones del autor se vuelven regla | Autor (MkUltra C11) | Auto: mensajes del autor con corrección o reclamo | La regla exacta que la habría evitado |
| K09 | Los archivos de las skills no superan su tope; se consolida antes de añadir | Azorian A11 (hallazgo H3 de MkUltra) | Auto: tamaños frente a `[topes]` (Media si lo pasa; Info desde el 90 %, M-044) | Reorganizar, no añadir |
| K11 | Lo intocable no se toca | Regla 3 del mandato | Auto: rm, mv o escritura sobre `[observador].intocables` fuera del scratchpad | Script o prueba que lo impida |
| K12 | Cada regla del motor cita una ficha R-## que existe en `REGLAS.md` (o una D-## del autor); nada de reglas de memoria | Regla 1 del mandato (autor, 2026-10-06); D-04 | Auto: R-## citadas en `[observador] motor` sin ficha. Revisión: lógica de reglas sin R-## ni D-##; cifras de una ficha no comparadas con el recorte | Exigir la ficha antes del código (`reglas.md` §3) |
| K13 | El motor es puro y determinista: sin Android, sin azar sin semilla, sin hora ni archivos | D-02, D-03; Azorian A03 | Auto: `import android(x).`, `Random()`, `Math.random`, `System.currentTimeMillis`, `LocalDateTime.now`, `java.io.File` en el motor | Mover la dependencia a `app/` o inyectarla (semilla) |
| K14 | Cada regla, su prueba: toda R-## citada en el motor aparece en una prueba de `engine/src/test` | Regla 5 del mandato; Azorian A02 | Auto: R-## del motor que ninguna prueba cita. Revisión: ¿la prueba compara con la ficha o con otra función del motor? | Prueba con el ID en el nombre y dados fijados (`motor.md` §4) |
| K15 | Interfaz con opciones y captura: 2-3 opciones antes de una pantalla nueva; captura del teléfono después de cambiarla | Regla 7 del mandato; L18; vid2aud W12 | Auto: escritura en `[observador] interfaz` sin `telefono.py captura` después. Revisión: pantalla nueva sin opciones | Recordatorio en el punto de control (`interfaz.md` §1-2) |
| K16 | Los reglamentos se leen por página o recorte y lo leído se transcribe en el mismo paso; la cobertura de `REGLAS.md` está al día | Regla 2 del mandato; Azorian M-007 | Revisión: imágenes de `pagina.py` leídas dos veces para la misma regla; recortes no anotados en «Cobertura». (K05 ya marca lecturas del PDF) | Transcribir antes de cerrar el paso (`reglas.md` §2) |
| K17 | Todo lo que va al autor (mensajes, descripciones de las llamadas, informe de cierre) va en español | El autor (idioma configurado: español); M-051 | Auto: mensajes del asistente con ≥ 3 palabras de función inglesas y más del doble que españolas | Línea en «Tono» del SKILL del agente |

*K02 y K10 (estilo y skill de Python) no aplican: el perfil es `otro` (Kotlin). F0.1 copia los reglamentos a `fuentes/` con `cp`: no es señal de K11 (solo `rm`, `mv` y escrituras lo son).*

## Límites de la evidencia

- La transcripción guarda las llamadas, sus resultados y los archivos; no siempre todo el texto que el autor vio, ni el razonamiento. Se juzga lo que se hizo.
- Las señales son heurísticas: cada una se confirma en la traza o en el archivo antes de volverla propuesta.
- K09 y K12-K14 miran el estado **actual** de los archivos: una señal puede venir de una sesión anterior.
