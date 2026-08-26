package com.atlashub.app.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ArchitectureRulesTest {

    private static final List<String> BOUNDED_CONTEXT_MODULES = List.of(
            "atlaspay-auth",
            "atlaspay-admin",
            "atlaspay-identity",
            "atlaspay-accounts",
            "atlaspay-ledger",
            "atlaspay-notifications",
            "atlaspay-eventbus"
    );

    private static final List<String> DOMAIN_FORBIDDEN_IMPORTS = List.of(
            "import org.springframework.",
            "import jakarta.persistence.",
            "import jakarta.servlet.",
            "import org.apache.kafka.",
            "import org.springframework.kafka.",
            "import org.springframework.web."
    );

    @Test
    void domainLayerDoesNotDependOnFrameworks() throws IOException {
        Path root = repositoryRoot();
        List<Path> violations = javaFiles(root)
                .filter(path -> isInLayer(path, "domain"))
                .filter(path -> containsAny(path, DOMAIN_FORBIDDEN_IMPORTS))
                .toList();

        assertTrue(violations.isEmpty(), () -> "Domain files contain forbidden framework imports: " + violations);
    }

    @Test
    void applicationLayerDoesNotImportInfrastructure() throws IOException {
        Path root = repositoryRoot();
        List<Path> violations = javaFiles(root)
                .filter(path -> isInLayer(path, "application"))
                .filter(path -> contains(path, ".adapter.out.external."))
                .toList();

        assertTrue(violations.isEmpty(), () -> "Application files import infrastructure packages: " + violations);
    }

    private static Stream<Path> javaFiles(Path root) throws IOException {
        return BOUNDED_CONTEXT_MODULES.stream()
                .map(root::resolve)
                .map(module -> module.resolve("src/main/java"))
                .filter(Files::exists)
                .flatMap(sourceRoot -> {
                    try {
                        return Files.walk(sourceRoot);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .filter(path -> path.toString().endsWith(".java"));
    }

    private static boolean isInLayer(Path path, String layer) {
        String marker = "src\\main\\java";
        String normalized = path.toString().replace('/', '\\');
        return normalized.contains(marker + "\\") && normalized.contains("\\" + layer + "\\");
    }

    private static boolean containsAny(Path path, List<String> values) {
        return values.stream().anyMatch(value -> contains(path, value));
    }

    private static boolean contains(Path path, String value) {
        try {
            return Files.readString(path).contains(value);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Path repositoryRoot() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (current != null && !Files.exists(current.resolve("settings.gradle"))) {
            current = current.getParent();
        }
        if (current == null) {
            throw new IllegalStateException("Could not locate repository root");
        }
        return current;
    }
}
