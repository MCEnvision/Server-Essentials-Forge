package com.enviouse.sef.docs;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectDocumentationGeneratorTest {
    @Test
    void trackedCommandAndPermissionReferencesMatchRuntimeRegistries() throws Exception {
        Path root = repositoryRoot();
        if (Boolean.getBoolean("sef.updateProjectReferences")) {
            ProjectDocumentationGenerator.writeAll(root);
        }

        assertEquals(
                normalizeLineEndings(ProjectDocumentationGenerator.commandReference(root)),
                normalizeLineEndings(Files.readString(root.resolve("docs/COMMAND_REFERENCE.md"), StandardCharsets.UTF_8)));
        assertEquals(
                normalizeLineEndings(ProjectDocumentationGenerator.permissionReference()),
                normalizeLineEndings(Files.readString(root.resolve("docs/PERMISSION_REFERENCE.md"), StandardCharsets.UTF_8)));
    }

    private static Path repositoryRoot() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("settings.gradle"))
                    && Files.isDirectory(current.resolve("src"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("repository root is unavailable");
    }

    private static String normalizeLineEndings(String value) {
        return value.replace("\r\n", "\n").replace('\r', '\n');
    }
}
