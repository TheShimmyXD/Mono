# Redmi: puesta a punto y problemas (F0.4, 2026-10-06)

*Se lee si `telefono.py dispositivos`, `instalar` o `captura` no responden como se espera (M-056). Lo de cada prueba está en `interfaz.md` §2.*

Si `telefono.py dispositivos` da una pista en vez de la serie, el autor lo arregla así (una vez por equipo y teléfono):
- `no permissions`: regla udev con sudo, en su terminal: `/etc/udev/rules.d/51-android.rules` con `SUBSYSTEM=="usb", ATTR{idVendor}=="18d1", MODE="0666"` y lo mismo con `2717` (Xiaomi); después `sudo udevadm control --reload-rules && sudo udevadm trigger`, `adb kill-server` y reconectar el cable.
- `unauthorized`: aceptar «¿Permitir depuración USB?» en el teléfono.
- Pantalla apagada (HyperOS la apaga a los ~10 min y adb no puede encenderla): `captura` lo detecta y no guarda nada; se pide al autor que desbloquee el Redmi (M-020).
- `INSTALL_FAILED_USER_RESTRICTED`: HyperOS pide «Instalar vía USB» en Opciones de desarrollador y tocar Instalar en el teléfono a tiempo.
- **Sin `svc power stayon`** (el autor, 2026-10-06): pide el desbloqueo justo antes y agrupa `instalar` + capturas en la misma llamada.
- Sin el Redmi conectado: `telefono.py emulador` (AVD `Medium_Phone`) y se espera a que `telefono.py dispositivos` lo liste.
