# Mejoras propuestas por el observador — Mono

*Estados: Propuesta · Aplicada <fecha> → archivo · Rechazada · No funcionó. No se repite una mejora aplicada o rechazada.*

| ID | Skill | Criterio | Mejora | Evidencia | Estado |
|---|---|---|---|---|---|
| M-001 | agente-mono (y autor) | K05 | Abrir Claude Code desde la raíz para que se carguen las skills; línea 0 en «Arranque» si el SKILL se leyó a mano | INF-2026-10-06-5527059a: #4 de 5527059a y bc6ecdee (`skills: -`, ~14 000 car.) | Aplicada 2026-10-06 → agente-mono/SKILL.md (Arranque, punto 0) |
| M-002 | observador-mono | K05/K06 | `extraer_sesion.py` busca transcripciones por el `cwd` registrado; `--ultima-con` cuenta un `cat` del SKILL.md | INF-2026-10-06-5527059a: `--listar` vacío tras renombrar (H3) | Aplicada 2026-10-06 → observador-mono/scripts/extraer_sesion.py + 2 pruebas |
| M-003 | observador-mono | K05 | `forbidden_reads` corta en `&&` y `||` (falso positivo) | INF-2026-10-06-5527059a: bc6ecdee #14 | Aplicada 2026-10-06 → observador-mono/scripts/senales.py + 1 prueba |
| M-004 | agente-mono | K06/K08 | `telefono.py` explica `no permissions`, `unauthorized` e `INSTALL_FAILED_USER_RESTRICTED`; `--salida <carpeta>`; «Redmi listo» en `interfaz.md` | INF-2026-10-06-5527059a: bc6ecdee #41, #62, #65, #70 | Aplicada 2026-10-06 → agente-mono/scripts/telefono.py + 3 pruebas, SKILL.md (tabla), references/interfaz.md («Redmi listo») |
| M-005 | ambas | K08 | Disparadores «despliega el agente» y «despliega el observador» | INF-2026-10-06-5527059a: palabras del autor | Aplicada 2026-10-06 → descriptions de agente-mono y observador-mono |
