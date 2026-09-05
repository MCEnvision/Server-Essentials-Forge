package com.enviouse.sef.docs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UniversalCommandMatrixGeneratorTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void matrixContainsEveryCommandAndUnavailableFamilyWithAllDimensions() {
        JsonObject matrix = UniversalCommandMatrixGenerator.generate();
        UniversalCommandMatrixGenerator.validate(matrix);
        assertEquals(710, matrix.get("rowCount").getAsInt());
        assertEquals(694, matrix.get("commandRowCount").getAsInt());
        assertEquals(16, matrix.get("unavailableRowCount").getAsInt());
        assertFalse(matrix.get("complete").getAsBoolean());
        assertTrue(matrix.get("openRowCount").getAsInt() > 0);
        assertEquals(UniversalCommandMatrixGenerator.DIMENSIONS.size(),
                matrix.getAsJsonArray("dimensions").size());
        assertEquals("src/main/java/com/enviouse/sef/kernel/KernelServices.java",
                matrix.getAsJsonArray("rows").get(0).getAsJsonObject()
                        .getAsJsonArray("sourceLocations").get(0).getAsString());
    }

    @Test
    void matrixGenerationIsDeterministicAndCanWriteToExternalRoot() throws Exception {
        JsonObject first = UniversalCommandMatrixGenerator.generate();
        JsonObject second = UniversalCommandMatrixGenerator.generate();
        assertEquals(first.toString(), second.toString());
        Path output = UniversalCommandMatrixGenerator.write(
                temporaryDirectory.resolve("inventory"), "universal-command-matrix.json");
        JsonObject persisted = JsonParser.parseString(
                Files.readString(output, StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals(AuditDriftValidator.normalized(first), AuditDriftValidator.normalized(persisted));

        String evidenceRoot = System.getProperty("sef.audit.evidenceRoot", "").trim();
        if (!evidenceRoot.isEmpty()) {
            UniversalCommandMatrixGenerator.write(
                    Path.of(evidenceRoot), "universal-command-matrix.json");
        }
    }

    @Test
    void validatorRejectsStatusCountDrift() {
        JsonObject matrix = UniversalCommandMatrixGenerator.generate();
        matrix.addProperty("openRowCount", matrix.get("openRowCount").getAsInt() - 1);
        assertThrows(IllegalArgumentException.class,
                () -> UniversalCommandMatrixGenerator.validate(matrix));
    }

    @Test
    void validatorRejectsNotApplicableRequiredCommandDimension() {
        JsonObject matrix = UniversalCommandMatrixGenerator.generate();
        JsonObject firstCommand = matrix.getAsJsonArray("rows").asList().stream()
                .map(element -> element.getAsJsonObject())
                .filter(row -> row.get("category").getAsString().equals("command-matrix"))
                .findFirst()
                .orElseThrow();
        firstCommand.getAsJsonObject("dimensions")
                .getAsJsonObject("authority")
                .addProperty("status", "not_applicable");
        firstCommand.getAsJsonObject("dimensions")
                .getAsJsonObject("authority")
                .addProperty("reason", "invalid test mutation");
        assertThrows(IllegalArgumentException.class,
                () -> UniversalCommandMatrixGenerator.validate(matrix));
    }

    @Test
    void validatorRejectsRowStatusThatHidesAnOpenDimension() {
        JsonObject matrix = UniversalCommandMatrixGenerator.generate();
        JsonObject firstCommand = matrix.getAsJsonArray("rows").asList().stream()
                .map(element -> element.getAsJsonObject())
                .filter(row -> row.get("category").getAsString().equals("command-matrix"))
                .findFirst()
                .orElseThrow();
        firstCommand.addProperty("status", "partial");
        assertThrows(IllegalArgumentException.class,
                () -> UniversalCommandMatrixGenerator.validate(matrix));
    }
}
