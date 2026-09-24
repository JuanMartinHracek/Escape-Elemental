# Changelog

Todos los cambios importantes de este proyecto se documentan en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Versionado Semántico](https://semver.org/lang/es/).

## [Unreleased]

## [0.2.0] - 2026-09-24

### Added
- `Main`: punto de entrada `Game` que abre `MenuScreen`, sin assets externos.
- `MenuScreen`: menú con opciones Ser Host (Fuego), Unirse como Cliente (Agua, pide IP del host) y Jugar Local 2P; muestra IP local y puerto `54555`.
- `Player`: física de plataformas AABB (gravedad, salto, colisión por ejes), estados `FUEGO`/`AGUA`, animación squash & stretch, respawn y vidas.
- `Level`: 3 niveles jugables con plataformas estáticas, 1 plataforma móvil, hazards de lava/agua, botón de presión, puerta, salidas diferenciadas, gemas y spawns.
- `GameScreen`: loop de juego con hazards que matan al personaje contrario, botón que abre la puerta, gemas (+100 pts), 3 vidas con reinicio (`R`), victoria por salidas simultáneas (+500 pts) y HUD con nivel/puntos/vidas/estado de red.
- `NetManager`: red TCP cliente-servidor (`ServerSocket`/`Socket`, puerto `54555`) que sincroniza 20 veces/s posición, velocidad, dirección, animación, vida, gemas, nivel y victoria entre las dos PCs.
- `Draw`: dibujado solo con `SpriteBatch` (píxel blanco + círculo generados por `Pixmap`), sin `ShapeRenderer`.
- `Lwjgl3Launcher`: ventana `960x540`.

### Fixed
- Salto `780` → `820` y alturas de plataformas rebajadas a pasos de `80-100px` (tope de salto `~153px`).
- Nivel 2: plataformas en `140`/`220` (paso de `110px`, muy justo) → `90`/`170`.
- Nivel 3: plataforma central en `260` (paso de `140px`, imposible de alcanzar) → `190`; plataforma móvil `150` → `140`; laterales `120` → `90`; puerta `230` → `160`.
- Puertas con tope en `190`: no se pueden saltar por arriba, obligan a cooperar con el botón.
- Gemas y botones reubicados sobre las plataformas nuevas.
- `MenuScreen`/`GameScreen`: `return` inmediato tras cada `setScreen` (causaba `IndexBufferObject cannot be used after it has been disposed` al pulsar `3`).
- Render migrado a solo `SpriteBatch` (el `ShapeRenderer` crasheaba el driver AMD `atio6axx.dll` en RX 570); `Lwjgl3Launcher` con emulación `GL30 3.2`, vsync off y `60` FPS.
- `settings.gradle`: eliminado el módulo `html` (el plugin GWT rompía el import en Eclipse con Gradle `9.7.1`, error `Could not create task ':html:test'`).

## [0.1.0] - 2026-09-24

### Added
- Creación del proyecto con `gdx-liftoff` (`Core` + `Desktop LWJGL3`, plantilla `Classic`, paquete `io.github.elementalescape`).
