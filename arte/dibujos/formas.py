"""Formas del arte (lienzo 200 x 140): cada una devuelve `(papel, forma)`; y las franjas de la chiva."""
import math


W, H = 200, 140


def f(n):
    return f"{n:.2f}".rstrip("0").rstrip(".")


def rect(x, y, w, h):
    return f"M{f(x)} {f(y)}H{f(x + w)}V{f(y + h)}H{f(x)}Z", (x, y, x + w, y + h)


def poly(pts):
    d = "M" + "L".join(f"{f(x)} {f(y)}" for x, y in pts) + "Z"
    xs, ys = [p[0] for p in pts], [p[1] for p in pts]
    return d, (min(xs), min(ys), max(xs), max(ys))


def circle(cx, cy, r):
    d = f"M{f(cx - r)} {f(cy)}A{f(r)} {f(r)} 0 1 1 {f(cx + r)} {f(cy)}A{f(r)} {f(r)} 0 1 1 {f(cx - r)} {f(cy)}Z"
    return d, (cx - r, cy - r, cx + r, cy + r)


def arch(x, y, w, h):
    r = w / 2
    d = f"M{f(x)} {f(y + h)}V{f(y + r)}A{f(r)} {f(r)} 0 0 1 {f(x + w)} {f(y + r)}V{f(y + h)}Z"
    return d, (x, y, x + w, y + h)


def gothic(x, y, w, h):
    m = y + h * 0.45
    d = (f"M{f(x)} {f(y + h)}V{f(m)}Q{f(x)} {f(y + h * 0.1)} {f(x + w / 2)} {f(y)}"
         f"Q{f(x + w)} {f(y + h * 0.1)} {f(x + w)} {f(m)}V{f(y + h)}Z")
    return d, (x, y, x + w, y + h)


def lines(segs):
    d = "".join(f"M{f(a)} {f(b)}L{f(c)} {f(e)}" for a, b, c, e in segs)
    return d, None


def polyline(pts):
    return "M" + "L".join(f"{f(x)} {f(y)}" for x, y in pts), None


def rrect(x, y, w, h, r):
    d = (f"M{f(x + r)} {f(y)}H{f(x + w - r)}A{f(r)} {f(r)} 0 0 1 {f(x + w)} {f(y + r)}V{f(y + h - r)}"
         f"A{f(r)} {f(r)} 0 0 1 {f(x + w - r)} {f(y + h)}H{f(x + r)}A{f(r)} {f(r)} 0 0 1 {f(x)} {f(y + h - r)}"
         f"V{f(y + r)}A{f(r)} {f(r)} 0 0 1 {f(x + r)} {f(y)}Z")
    return d, (x, y, x + w, y + h)


def raw(d):
    """Trazado escrito a mano (curvas que no son de las formas de arriba)."""
    return d, None


def star(cx, cy, r, ri):
    pts = []
    for i in range(10):
        a = math.radians(-90 + i * 36)
        rr = r if i % 2 == 0 else ri
        pts.append((cx + math.cos(a) * rr, cy + math.sin(a) * rr))
    return poly(pts)
FRANJA = ["#E63946", "#FFC21A", "#1D7BEF", "#2BB04A", "#E5007E"]


def franjas():
    """Bandas de chiva arriba y abajo: dientes de colores sobre negro."""
    out = []
    for y0, arriba in ((0, False), (H - 9, True)):
        out.append(("#1B1B1B", None, 0, rect(0, y0, W, 9)[0]))
        for i, x in enumerate(range(0, W, 10)):
            pts = ([(x, y0 + 9), (x + 5, y0 + 1.5), (x + 10, y0 + 9)] if arriba
                   else [(x, y0), (x + 5, y0 + 7.5), (x + 10, y0)])
            out.append((FRANJA[i % len(FRANJA)], None, 0, poly(pts)[0]))
    return out
