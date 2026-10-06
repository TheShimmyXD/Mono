# Mejoras propuestas por el observador — Mono

*Estados: Propuesta · Aplicada <fecha> → archivo · Rechazada · No funcionó. No se repite una mejora aplicada o rechazada.*

| ID | Skill | Criterio | Mejora | Evidencia | Estado |
|---|---|---|---|---|---|
| M-001 | agente-mono (y autor) | K05 | Abrir Claude Code desde la raíz para que se carguen las skills; línea 0 en «Arranque» si el SKILL se leyó a mano | INF-2026-10-06-5527059a: #4 de 5527059a y bc6ecdee (`skills: -`, ~14 000 car.) | No funcionó (INF-2026-10-06-e47b9e54: el trabajo se lanzó antes de las skills y `/clear` no las recarga) → M-006 |
| M-002 | observador-mono | K05/K06 | `extraer_sesion.py` busca transcripciones por el `cwd` registrado; `--ultima-con` cuenta un `cat` del SKILL.md | INF-2026-10-06-5527059a: `--listar` vacío tras renombrar (H3) | Aplicada 2026-10-06 → observador-mono/scripts/extraer_sesion.py + 2 pruebas |
| M-003 | observador-mono | K05 | `forbidden_reads` corta en `&&` y `||` (falso positivo) | INF-2026-10-06-5527059a: bc6ecdee #14 | Aplicada 2026-10-06 → observador-mono/scripts/senales.py + 1 prueba |
| M-004 | agente-mono | K06/K08 | `telefono.py` explica `no permissions`, `unauthorized` e `INSTALL_FAILED_USER_RESTRICTED`; `--salida <carpeta>`; «Redmi listo» en `interfaz.md` | INF-2026-10-06-5527059a: bc6ecdee #41, #62, #65, #70 | Aplicada 2026-10-06 → agente-mono/scripts/telefono.py + 3 pruebas, SKILL.md (tabla), references/interfaz.md («Redmi listo») |
| M-005 | ambas | K08 | Disparadores «despliega el agente» y «despliega el observador» | INF-2026-10-06-5527059a: palabras del autor | Aplicada 2026-10-06 → descriptions de agente-mono y observador-mono |
| M-006 | ambas (y autor) | K05 | Lanzar un proceso nuevo de Claude Code desde la raíz, no `/clear` en el trabajo viejo; punto 0 en el Arranque del agente y en el Flujo del observador | INF-2026-10-06-e47b9e54: #4 (10 483 car.), `skills: -` en 3 sesiones | Aplicada 2026-10-06 → agente-mono/SKILL.md (Arranque 0) + observador-mono/SKILL.md (Flujo 0); falta que el autor abra un Claude Code nuevo |
| M-007 | agente-mono | K06 | `telefono.py adb -- <args>` con el adb del SDK; nunca `adb` suelto | INF-2026-10-06-e47b9e54: #9-#11 | Aplicada 2026-10-06 → agente-mono/scripts/telefono.py (`adb -- <args>`) + 1 prueba + SKILL.md |
| M-008 | agente-mono | K06 | Decir en el cierre qué queda fuera de Git | INF-2026-10-06-e47b9e54: #18-#21 | Aplicada 2026-10-06 → agente-mono/SKILL.md (Cierre de cada paso) |
| M-009 | agente-mono | K16 | Recortes del Tío Rico por columna en `reglas.md` §2 | INF-2026-10-06-e47b9e54: r50-0 y r50-33 leídos dos veces | Propuesta |
| M-010 | agente-mono | K07/K12 | `cierre_paso.py` comprueba los R-## (únicos, consecutivos, citas con ficha) | INF-2026-10-06-e47b9e54: #72 | Aplicada 2026-10-06 → agente-mono/scripts/cierre_paso.py (paso `reglas`) + 3 pruebas + SKILL.md |
