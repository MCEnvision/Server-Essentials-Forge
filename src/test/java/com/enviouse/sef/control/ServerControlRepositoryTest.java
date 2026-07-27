package com.enviouse.sef.control;

import com.enviouse.sef.kernel.ActionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerControlRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void everyCatalogFeatureCreatesPersistsAndReloads() throws Exception {
        ServerControlRepository repository = new ServerControlRepository();
        repository.load(temporaryDirectory);
        UUID actor = UUID.randomUUID();

        for (ServerControlCatalog.FeatureDefinition feature : ServerControlCatalog.FEATURES) {
            ActionResult<ServerControlRepository.ControlRecord> created = repository.create(
                    feature.id(),
                    actor,
                    null,
                    feature.title(),
                    "bounded test record",
                    null,
                    Map.of("source", "test"));
            assertTrue(created.successful(), feature.id());
        }

        assertTrue(repository.dirty());
        repository.flush();
        assertFalse(repository.dirty());

        ServerControlRepository replacement = new ServerControlRepository();
        assertEquals(
                com.enviouse.sef.storage.repository.StorageRepository.RepositoryState.READY,
                replacement.load(temporaryDirectory).state());
        assertEquals(ServerControlCatalog.FEATURES.size(), replacement.diagnostic().records());
        for (ServerControlCatalog.FeatureDefinition feature : ServerControlCatalog.FEATURES) {
            assertEquals(1, replacement.records(feature.id()).size(), feature.id());
        }
    }

    @Test
    void revisionsPreventStaleMutationAndHistoryTracksTransitions() {
        ServerControlRepository repository = new ServerControlRepository();
        repository.load(temporaryDirectory);
        UUID actor = UUID.randomUUID();
        var created = repository.create(
                "reports",
                actor,
                UUID.randomUUID(),
                "report",
                "details",
                null,
                Map.of());
        assertTrue(created.successful());
        var category = repository.configure(
                created.value().id(),
                actor,
                "category",
                "conduct",
                false,
                created.value().revision());
        assertTrue(category.successful());
        var description = repository.configure(
                created.value().id(),
                actor,
                "description",
                "details",
                false,
                category.value().revision());
        assertTrue(description.successful());
        var priority = repository.configure(
                created.value().id(),
                actor,
                "priority",
                "normal",
                false,
                description.value().revision());
        assertTrue(priority.successful());

        var activated = repository.transition(
                created.value().id(),
                actor,
                ServerControlRepository.RecordState.ACTIVE,
                priority.value().revision(),
                "assigned");
        assertTrue(activated.successful());

        var stale = repository.update(
                created.value().id(),
                actor,
                "replacement",
                "details",
                created.value().revision());
        assertFalse(stale.successful());
        assertEquals(ActionResult.ReasonCode.CONFLICT, stale.reason());
        assertEquals(5, repository.history(created.value().id()).size());
    }

    @Test
    void invalidBoundsFailWithoutMutation() {
        ServerControlRepository repository = new ServerControlRepository();
        repository.load(temporaryDirectory);

        var blank = repository.create(
                "tickets",
                UUID.randomUUID(),
                null,
                " ",
                "",
                null,
                Map.of());
        var oversized = repository.create(
                "tickets",
                UUID.randomUUID(),
                null,
                "x".repeat(129),
                "",
                null,
                Map.of());

        assertFalse(blank.successful());
        assertFalse(oversized.successful());
        assertEquals(0, repository.diagnostic().records());
    }

    @Test
    void configureAllValidatesAndCommitsAsOneRevision() {
        ServerControlRepository repository = new ServerControlRepository();
        repository.load(temporaryDirectory);
        UUID actor = UUID.randomUUID();
        var created = repository.create(
                "reports",
                actor,
                actor,
                "draft",
                "",
                null,
                Map.of("route", "gui"));
        assertTrue(created.successful());

        var configured = repository.configureAll(
                created.value().id(),
                actor,
                "report",
                "details",
                Map.of(
                        "category", "conduct",
                        "description", "player report",
                        "priority", "normal",
                        "attachments", ""),
                created.value().revision());

        assertTrue(configured.successful());
        assertEquals(created.value().revision() + 1L, configured.value().revision());
        assertEquals("conduct", configured.value().metadata().get("field.category"));
        assertFalse(configured.value().metadata().containsKey("field.attachments"));
        assertEquals(2, repository.history(created.value().id()).size());

        var invalid = repository.configureAll(
                created.value().id(),
                actor,
                "changed",
                "changed",
                Map.of("priority", "impossible"),
                configured.value().revision());
        assertFalse(invalid.successful());
        var unchanged = repository.find(created.value().id()).orElseThrow();
        assertEquals(configured.value().revision(), unchanged.revision());
        assertEquals("report", unchanged.title());
        assertEquals("normal", unchanged.metadata().get("field.priority"));
    }
}
