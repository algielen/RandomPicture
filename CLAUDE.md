# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Compile and package
mvn package

# Run via Maven
mvn exec:java

# Run tests
mvn test

# Build shaded (fat) JAR → target/shade/RandomPicture-shaded-1.6.jar
mvn package   # shade plugin runs automatically during package phase
```

### Distributable builds

`build-jlink.sh` — creates a self-contained JRE image under `target/jlink/`. Requires JavaFX jmods at
`C:/development/resources/javafx-jmods-25.0.2`.

`build-jpackage.bat` / `build-jpackage.sh` — wraps the shaded JAR into a native app image under `target/exe/`. Requires
the shaded JAR to exist first (`mvn package`).

## Architecture

The app is a JavaFX slideshow viewer that picks images at random from one or more directories.

**Entry points:**

- `Main` — thin wrapper that delegates to `MainFX.main()`. Used as the JAR manifest main class because JavaFX's
  `Application` subclass cannot be launched directly from a shaded JAR without the module system.
- `MainFX extends Application` — the real JavaFX entry point; builds the scene graph and owns the UI lifecycle.

**Background loading pipeline:**

- `MainFX` holds two `ConcurrentLinkedQueue`s: one for pre-loaded `Image` objects and one for their corresponding `Path`
  s (kept in sync).
- `ImageLoader extends Thread` runs continuously, filling those queues up to a buffer of 10 images. It calls
  `wait(1000)` on the queue when the buffer is full and is woken up by `notifyAll()` whenever the UI consumes an image.
- `RandomSelector` is called by `ImageLoader` to pick a random file: it recurses into directories, randomly choosing
  child entries until it finds a regular file (no full directory listing is cached — each traversal is a fresh random
  walk).

**Configuration:**

- At startup `MainFX` looks for `randompicture.properties` next to the JAR (external config). If absent or unreadable,
  it falls back to the bundled `src/main/resources/props.properties`.
- `paths` property (semicolon-separated) → list of root directories to scan. `defaultImage` → classpath resource used as
  the initial placeholder and window icon.

**Accepted image extensions:** `jpg`, `jpeg`, `png`, `gif`, `bmp`, `tiff`, `avi` (matched via `PathMatcher` glob in
`ImageLoader`). Non-matching files are added to an in-memory `excluded` set so they are not retried.

**UI interactions:**

- Left-click / Space / Enter → advance to next pre-loaded image
- Right-click → show current file path in the status bar
- F11 → toggle fullscreen
- Status bar buttons: "Open folder" (opens parent dir in explorer) / "Open file" (opens file in default viewer)

## Module System

The project uses Java Platform Module System (`module-info.java`). The module name is `randompicture`. JavaFX requires
reflective access to the GUI package — this is declared with `opens be.algielen.randompicture.gui to javafx.graphics`.

The shaded JAR (`maven-shade-plugin`) is used for distribution because jlink/jpackage require the module path; the shade
approach bundles everything for simpler deployment.

## Java & JavaFX Versions

Java 25, JavaFX 25.0.2. The `maven.compiler.source` property in `pom.xml` is the single source of truth for the Java
version.
