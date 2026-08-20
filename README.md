# Euclid's Compass

**Version 1.1.0**

**Euclid's Compass** is a single-file, offline-capable web app for doing straightedge-and-compass
constructions — the same constructions Euclid works through in the *Elements* — in an interactive
canvas, with layers, construction-equivalence checks, and touch support.

It's one HTML file. There's no build step, no server, and no dependencies beyond three Google
Fonts loaded at runtime (the app still works without them — it just falls back to your system
fonts). Open `euclids-compass.html` in a browser and start constructing.

---

## What it does

| Tool | What it's for |
|---|---|
| **Select** | Click a point, line, or circle/arc to select it. Drag a point to move it. Delete/Backspace removes it. |
| **Eraser** | Click an object to delete it, or drag across several to erase a whole stroke's worth at once — no need to open the Layers panel. |
| **Point** | Place a point. Snaps to existing points and to computed intersections. |
| **Line** | Click two points to draw a straight segment between them — the straightedge. |
| **Compass** | Click a centre, then a radius point, to draw a circle. Toggle **Arc** to swing only part of the circle. Toggle **Collapsing** / **Rigid** to control whether the compass "remembers" its span after it's lifted. |
| **Transport** | Click two points to capture a length, click a centre, click a direction — a new point is placed exactly that distance away. This is Euclid's method for copying a length with a compass that can't be walked across the page (Book I, Proposition 2). |
| **Check** | Click two lines, two circles/arcs, or two angles (vertex + two arm points, twice) to check them for equality, with the difference shown. |

Selected lines can be **extended or trimmed** to a new point, like sliding a straightedge further
along a line. **Layers** hold groups of objects with their own color, line style (solid / dashed /
dotted), visibility, and an optional **lock** that protects a layer's contents from editing,
deletion, or "Clear all" (with a checkbox to include locked layers anyway).

The app is a full pointer-events implementation: single-finger tap-and-drag on touch devices,
two-finger pinch to zoom and pan, right-click-drag or Space-drag to pan on desktop, scroll wheel to zoom.

---

## Euclid's tools, and how this app maps to them

Euclid's *Elements* opens with three postulates that describe exactly two instruments:

1. A **straightedge** (unmarked — no ruler markings, no measuring) used to draw a straight line
   between any two given points, and to extend a line as far as needed.
2. A **compass**, used to draw a circle with a given centre and a given radius.

Ancient tradition holds that Euclid's compass was a **collapsing compass**: the moment you lift it
off the page, it loses whatever span it was set to. You could set a radius and draw one circle, but
you couldn't casually pick the compass up and "carry" that same radius somewhere else on the page —
that had to be *proven* possible, not assumed. That proof is Proposition 2 of Book I, and it's the
reason the *Elements* spends real effort early on showing how to place a given length at a given
point using only a straightedge and a collapsing compass.

This app models both traditions:

- The **Compass** tool's **Collapsing** mode behaves like Euclid's instrument — every new circle
  requires re-measuring the radius from scratch.
- **Rigid** mode behaves like a modern drafting compass, which holds its span until you change it —
  useful for repetitive constructions, and closer to how most people expect a compass to work today.
- The **Transport** tool directly implements the *method* of Proposition I.2: capture a length
  between two points, then reproduce it exactly at a new location and orientation, regardless of
  which compass mode you're in.
- The **Check** tool mirrors the *Elements*' Common Notions about equal magnitudes ("things which
  equal the same thing also equal one another") — after a construction, you can verify that two
  segments, two circles, or two angles actually are equal, rather than just eyeballing it.

### Using this app alongside the text

The intended workflow is to read a proposition, then reproduce its construction here, step by step:

1. Open the proposition in the *Elements* (linked below) and read through its construction — Euclid
   describes each step as an instruction ("let the straight line AB be drawn," "with centre A and
   distance AB describe the circle BCD," and so on).
2. Reproduce each instruction with the matching tool: **Point** for "let a point be given," **Line**
   for straightedge steps, **Compass** for "describe a circle," **Transport** for steps that copy a
   length.
3. Use **Check** to confirm the proposition's claims of equality once you've finished — a good way to
   catch a construction mistake before it propagates into a later, dependent proposition.
4. Use **Layers** to separate the "given" elements of a problem from the construction lines you add
   to solve it, which is how many published diagrams of the *Elements* are drawn.

A good place to start is Book I, Proposition 1 — constructing an equilateral triangle on a given
line using only two circles and a straight line — which uses exactly the Point, Compass, and Line
tools in sequence.

### The text, online

This app doesn't include Euclid's text itself. For the full text of the *Elements* (all 13 books),
with the original diagrams and historical notes, see:

**[David E. Joyce's edition of Euclid's Elements](https://www.euclids-elements.org/elements/)**
(Clark University) — freely available online, and it includes its own separate tutorial on
[compass geometry](https://www.euclids-elements.org/geomlib/compass/) that pairs well with this app.

---

## Files

- `euclids-compass.html` — the entire application. Open it directly in a browser.

The app's version number is defined once, as the `APP_VERSION` constant near the top of the
`<script>` block in `euclids-compass.html`, and every on-page display of it (the header badge, the
Help modal title, the Help modal footer, and the `<meta name="application-version">` tag) is set
from that single constant at load time — so they can't drift out of sync with each other. This
README is updated by hand to match on each release.

---

## License

Copyright © 2026 Numerius Engineering LLC. Released under the MIT License — see below.

```
MIT License

Copyright (c) 2026 Numerius Engineering LLC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
