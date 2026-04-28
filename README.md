# RandomPicture

A JavaFX desktop app that displays images at random from one or more directory trees. Click or press Space to advance,
F11 for fullscreen.

## Requirements

- Java 25+
- Maven 3.x
- For jlink/jpackage: [JavaFX 25 jmods](https://gluonhq.com/products/javafx/) (separate download, not needed to build or
  run the JAR)

## Configuration

The app looks for `randompicture.properties` next to the JAR at startup. If absent it falls back to the bundled
defaults (empty path, `heavybreathing.gif` placeholder).

```properties
# Semicolon-separated list of root directories to scan recursively
paths=C:/Pictures;D:/Photos
# Classpath resource used as the initial placeholder image
defaultImage=heavybreathing.gif
```

## Build

```bash
mvn package
```

Produces two artifacts:

- `target/RandomPicture-1.6.jar` — modular JAR (for jlink)
- `target/shade/RandomPicture-shaded-1.6.jar` — fat JAR (for direct execution)

## Run

```bash
java -jar target/shade/RandomPicture-shaded-1.6.jar
```

Or during development:

```bash
mvn exec:java
```

## Distribution

### jlink — self-contained JRE image

Produces a minimal JRE + app bundle in `target/jlink/RandomPicture/`. No JDK required on the target machine.

```bash
mvn verify -Pjlink
```

Override the JavaFX jmods location if it differs from the default:

```bash
mvn verify -Pjlink -Djavafx.jmods.path=/path/to/javafx-jmods-25.0.2
```

Run the result:

```
target/jlink/RandomPicture/bin/randompicture
```

The default jmods path can also be set permanently per machine in `~/.m2/settings.xml`:

```xml

<settings>
    <profiles>
        <profile>
            <id>javafx-jmods</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <javafx.jmods.path>/path/to/javafx-jmods-25.0.2</javafx.jmods.path>
            </properties>
        </profile>
    </profiles>
</settings>
```

### jpackage — native app image

Wraps the fat JAR into a native executable image in `target/exe/`. Requires jpackage (bundled with JDK 16+).

```bash
mvn verify -Pjpackage
```

The same `-Djavafx.jmods.path` override applies.

## Controls

| Input                      | Action                             |
|----------------------------|------------------------------------|
| Left click / Space / Enter | Next random image                  |
| Right click                | Show current file path             |
| F11                        | Toggle fullscreen                  |
| Status bar → "Open folder" | Open containing folder in Explorer |
| Status bar → "Open file"   | Open image in default viewer       |
