package com.enviouse.sef.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuditDriftValidatorTest {
    @Test
    void normalizedOutputIgnoresObjectInsertionOrder() {
        JsonObject first = new JsonObject();
        first.addProperty("b", 2);
        first.addProperty("a", 1);
        JsonObject second = new JsonObject();
        second.addProperty("a", 1);
        second.addProperty("b", 2);
        assertDoesNotThrow(() -> AuditDriftValidator.requireDeterministic(first, second));
    }

    @Test
    void driftFixturesRejectDuplicateUnknownMissingAndSameCountIdentities() throws Exception {
        JsonArray rows = BuildInventoryGenerator.generate(repositoryRoot()).getAsJsonArray("rows");
        JsonArray duplicate = rows.deepCopy();
        duplicate.add(rows.get(0).deepCopy());
        assertThrows(IllegalArgumentException.class, () -> AuditEvidenceContract.validateInventorySet(duplicate));

        JsonArray unknownVersion = rows.deepCopy();
        unknownVersion.get(0).getAsJsonObject().addProperty("schemaVersion", 99);
        assertThrows(IllegalArgumentException.class, () -> AuditEvidenceContract.validateInventorySet(unknownVersion));

        JsonArray missingRoute = rows.deepCopy();
        missingRoute.get(0).getAsJsonObject().remove("evidenceRoute");
        assertThrows(IllegalArgumentException.class, () -> AuditEvidenceContract.validateInventorySet(missingRoute));

        JsonArray changedIdentity = rows.deepCopy();
        changedIdentity.get(0).getAsJsonObject().addProperty("semanticKey", "synthetic-added-surface");
        assertThrows(IllegalStateException.class,
                () -> AuditDriftValidator.requireExactSemanticKeys(rows, changedIdentity));
    }

    @Test
    void traceabilityFixtureRejectsMissingAssignmentAndAcceptsReconciledRows() throws Exception {
        JsonArray rows = AuditReconciliationGenerator.generate(repositoryRoot()).getAsJsonArray("rows");
        assertDoesNotThrow(() -> AuditDriftValidator.requireTraceability(rows));
        JsonArray missing = rows.deepCopy();
        missing.get(0).getAsJsonObject().remove("laterPhase");
        assertThrows(IllegalStateException.class, () -> AuditDriftValidator.requireTraceability(missing));
    }

    @Test
    void sourceLocationFixtureRejectsAbsolutePaths() throws Exception {
        JsonArray rows = AuditReconciliationGenerator.generate(repositoryRoot()).getAsJsonArray("rows");
        JsonArray absolute = rows.deepCopy();
        absolute.get(0).getAsJsonObject().getAsJsonArray("sourceLocations").set(0,
                JsonParser.parseString("\"/synthetic/private/path\""));
        assertThrows(IllegalStateException.class, () -> AuditDriftValidator.requireTraceability(absolute));
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
}
