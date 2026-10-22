import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Apply OpenJDK's CVE-2026-91403 mitigation to a generated runtime, never the host JDK. */
class PatchRuntimeTrust {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Expected runtime image directory and fingerprint file");
        var requestedImage = Path.of(args[0]);
        // jpackage app images have a .app suffix on macOS.
        if (!Files.exists(requestedImage) && Files.isDirectory(Path.of(args[0] + ".app"))) {
            requestedImage = Path.of(args[0] + ".app");
        }
        var image = requestedImage.toRealPath();
        var fingerprints = Files.readAllLines(Path.of(args[1]), StandardCharsets.US_ASCII).stream()
                .filter(line -> !line.isBlank() && !line.startsWith("#")).distinct().toList();
        if (fingerprints.size() != 16 || fingerprints.stream().anyMatch(line -> !line.matches("[0-9A-F]{64}"))) {
            throw new IllegalArgumentException("Expected the 16 verified eMudhra certificate fingerprints");
        }
        int patched = 0;
        try (var paths = Files.walk(image)) {
            for (var file : paths.filter(path -> path.endsWith(Path.of("lib", "security", "blocked.certs"))).toList()) {
                if (!file.toRealPath().startsWith(image)) throw new IllegalArgumentException("Blocklist escapes runtime image");
                var properties = new Properties();
                try (var reader = Files.newBufferedReader(file, StandardCharsets.US_ASCII)) {
                    properties.load(reader);
                }
                if (!"SHA-256".equals(properties.getProperty("Algorithm"))) {
                    throw new IllegalArgumentException("Unsupported blocklist algorithm: " + file);
                }
                var additions = fingerprints.stream().filter(hash -> !properties.containsKey(hash)).toList();
                if (!additions.isEmpty()) {
                    Files.writeString(file, "\n# CVE-2026-91403\n" + String.join("\n", additions) + "\n",
                            StandardCharsets.US_ASCII, StandardOpenOption.APPEND);
                }
                patched++;
            }
        }
        if (patched != 1) throw new IllegalStateException("Expected one generated runtime blocklist, found " + patched);
        System.out.println("Generated runtime blocks all 16 compromised eMudhra certificates");
    }
}
