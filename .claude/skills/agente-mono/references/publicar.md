# Publicar en GitHub

*Antes de un push, una etiqueta, una release o un cambio de visibilidad. El repositorio es público (D-80) y la regla 3 sigue: nada sale sin el autor.*

## 1. Lo que se publica, con nombre (M-131)

Una opción que publica dice en su descripción **todo** lo que sale: los commits (`git log --oneline origin/main..main`), la etiqueta y los archivos. Aprobarla vale como orden solo para esa lista. Si después aparece algo más (otro commit, otro archivo), es otra pregunta.

## 2. Versión nueva del APK (M-132, D-81)

1. Subir `versionCode` (+1) y `versionName` en `app/build.gradle.kts` y cerrar con `cierre_paso.py`. Android no instala encima una versión con un `versionCode` menor o igual.
2. Copiar `app/build/outputs/apk/debug/app-debug.apk` al scratchpad como `mono-<versión>.apk`.
3. Barrido con caso sembrado (M-127): `unzip -q` en una carpeta del scratchpad, un archivo con los valores sembrados y `grep -rlaE` de la ruta de inicio, el correo y las IPs reales de la red del autor (ESTADO, «Cortafuegos»). Debe dar 3 con el sembrado y 0 sin él.
4. Firma: `PATH=~/android-studio/jbr/bin:$PATH ~/Android/Sdk/build-tools/<versión>/apksigner verify --print-certs <apk>`. `keytool -printcert -jarfile` no lee la firma v2, y sin el JBR `apksigner` falla con «java: no encontrado». Debe salir `CN=Android Debug` con el SHA-256 de D-81 (`cb5561e7…`); con otra llave, Android pide desinstalar y se pierde lo guardado.
5. Pregunta al autor según §1, y con su orden: `git push origin main` y `gh release create v<versión> <apk> -R TheShimmyXD/Mono --target main --title "Mono <versión>" --notes "<qué cambió>"`.
6. Comprobar sin descargar en primer plano: `gh release view v<versión> -R TheShimmyXD/Mono --json assets --jq '.assets[] | .name + " " + (.size|tostring)'` (el mismo tamaño que el local) y `curl -sIL -o /dev/null -w '%{url_effective}\n' https://github.com/TheShimmyXD/Mono/releases/latest`. La suma sha256 de la descarga, solo con `run_in_background`.
7. El autor la instala encima en el Redmi desde el enlace; antes y después, `telefono.py captura` del menú con «Seguir la partida» y, después, `telefono.py log` con «sigue la partida guardada: turno N» (el README afirma que lo guardado no se borra).
