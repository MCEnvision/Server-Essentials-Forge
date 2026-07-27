package com.enviouse.sef.config.modules;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigurationDocumentationGenerator {
    private ConfigurationDocumentationGenerator() {
    }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 1) {
            throw new IllegalArgumentException("one output path is required");
        }
        Path destination = Path.of(arguments[0]).toAbsolutePath().normalize();
        Files.createDirectories(destination.getParent());
        Files.writeString(
                destination,
                new ModuleConfigRegistry().generatedReference(),
                StandardCharsets.UTF_8);
    }
}
