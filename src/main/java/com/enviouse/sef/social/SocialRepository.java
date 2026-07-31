package com.enviouse.sef.social;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class SocialRepository implements StorageRepository {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .setPrettyPrinting()
            .create();
    private static final int SCHEMA_VERSION = 1;
    private static final int MAXIMUM_PROFILES = 1_000_000;
    private static final int MAXIMUM_MAIL = 2_000_000;
    private static final int MAXIMUM_REMINDERS = 10_000;
    private static final int MAXIMUM_TEXT_PAGES = 10_000;

    private final Map<UUID, SocialPreferences> preferences = new LinkedHashMap<>();
    private final Map<UUID, MailRecord> mail = new LinkedHashMap<>();
    private final Map<UUID, LinkedHashSet<UUID>> mailByRecipient = new LinkedHashMap<>();
    private final Map<UUID, ConnectionTemplates> connectionTemplates = new LinkedHashMap<>();
    private final Map<String, ReminderDefinition> reminders = new LinkedHashMap<>();
    private final Map<ReminderStateKey, ReminderState> reminderStates = new LinkedHashMap<>();
    private final Map<String, String> textPages = new LinkedHashMap<>();

    private Path path;
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.NEW;
    private boolean dirty;

    public synchronized SocialPreferences preferences(UUID playerId) {
        return preferences.getOrDefault(playerId, SocialPreferences.defaults(playerId));
    }

    public synchronized SocialPreferences updatePreferences(SocialPreferences replacement) {
        ensureWritable();
        if (!preferences.containsKey(replacement.playerId()) && preferences.size() >= MAXIMUM_PROFILES) {
            throw new IllegalStateException("social profile limit reached");
        }
        preferences.put(replacement.playerId(), replacement);
        changed();
        return replacement;
    }

    public synchronized boolean ignores(UUID playerId, UUID other) {
        return preferences(playerId).ignoredPlayers().contains(other);
    }

    public synchronized SocialPreferences setIgnored(UUID playerId, UUID other, boolean ignored) {
        SocialPreferences current = preferences(playerId);
        Set<UUID> replacement = new LinkedHashSet<>(current.ignoredPlayers());
        if (ignored) {
            if (replacement.size() >= 512 && !replacement.contains(other)) {
                throw new IllegalStateException("ignore list limit reached");
            }
            replacement.add(other);
        } else {
            replacement.remove(other);
        }
        return updatePreferences(current.withIgnoredPlayers(replacement));
    }

    public synchronized ActionResult<MailRecord> sendMail(
            UUID sender,
            UUID recipient,
            String body,
            Instant expiresAt,
            long mailboxLimit
    ) {
        ensureWritable();
        if (mail.size() >= MAXIMUM_MAIL) {
            pruneExpiredMail(Instant.now());
        }
        if (mail.size() >= MAXIMUM_MAIL) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "global mail limit reached");
        }
        long active = recipientMail(recipient).stream()
                .filter(record -> !record.expired(Instant.now()))
                .count();
        if (mailboxLimit >= 0 && active >= mailboxLimit) {
            return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "recipient mailbox is full");
        }
        MailRecord record = new MailRecord(
                UUID.randomUUID(),
                sender,
                recipient,
                body,
                Instant.now(),
                expiresAt,
                false,
                false);
        mail.put(record.id(), record);
        indexMail(record);
        changed();
        return ActionResult.success(record);
    }

    public synchronized List<MailRecord> mail(UUID recipient, boolean archived) {
        Instant now = Instant.now();
        return recipientMail(recipient).stream()
                .filter(record -> !record.expired(now))
                .filter(record -> archived || !record.archived())
                .sorted(Comparator.comparing(MailRecord::createdAt).reversed())
                .toList();
    }

    public synchronized long unreadMail(UUID recipient) {
        return mail(recipient, false).stream().filter(record -> !record.read()).count();
    }

    public synchronized ActionResult<MailRecord> updateMail(UUID recipient, UUID id, MailMutation mutation) {
        ensureWritable();
        MailRecord current = mail.get(id);
        if (current == null || !current.recipientId().equals(recipient)) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "mail not found");
        }
        MailRecord replacement = switch (mutation) {
            case READ -> current.withRead(true);
            case ARCHIVE -> current.withArchived(true);
            case DELETE -> null;
        };
        if (replacement == null) {
            mail.remove(id);
            removeMailIndex(current);
        } else {
            mail.put(id, replacement);
        }
        changed();
        return ActionResult.success(current);
    }

    public synchronized int clearMail(UUID recipient) {
        ensureWritable();
        Set<UUID> ids = mailByRecipient.remove(recipient);
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        int removed = 0;
        for (UUID id : ids) {
            if (mail.remove(id) != null) {
                removed++;
            }
        }
        if (removed > 0) {
            changed();
        }
        return removed;
    }

    public synchronized ConnectionTemplates connectionTemplates(UUID playerId) {
        return connectionTemplates.getOrDefault(playerId, new ConnectionTemplates(playerId, "", "", 1));
    }

    public synchronized ConnectionTemplates setConnectionTemplate(UUID playerId, boolean join, String template) {
        ensureWritable();
        if (!connectionTemplates.containsKey(playerId) && connectionTemplates.size() >= MAXIMUM_PROFILES) {
            throw new IllegalStateException("connection template limit reached");
        }
        ConnectionTemplates current = connectionTemplates(playerId);
        ConnectionTemplates replacement = join
                ? new ConnectionTemplates(playerId, template, current.leaveTemplate(), current.revision() + 1)
                : new ConnectionTemplates(playerId, current.joinTemplate(), template, current.revision() + 1);
        connectionTemplates.put(playerId, replacement);
        changed();
        return replacement;
    }

    public synchronized ReminderDefinition putReminder(ReminderDefinition definition) {
        ensureWritable();
        if (!reminders.containsKey(definition.id()) && reminders.size() >= MAXIMUM_REMINDERS) {
            throw new IllegalStateException("reminder limit reached");
        }
        reminders.put(definition.id(), definition);
        changed();
        return definition;
    }

    public synchronized Optional<ReminderDefinition> reminder(String id) {
        return Optional.ofNullable(reminders.get(normalizeId(id)));
    }

    public synchronized List<ReminderDefinition> reminders() {
        return reminders.values().stream().sorted(Comparator.comparing(ReminderDefinition::id)).toList();
    }

    public synchronized int reminderCount() {
        return reminders.size();
    }

    public synchronized boolean deleteReminder(String id) {
        ensureWritable();
        ReminderDefinition removed = reminders.remove(normalizeId(id));
        if (removed != null) {
            reminderStates.keySet().removeIf(key -> key.reminderId().equals(removed.id()));
            changed();
        }
        return removed != null;
    }

    public synchronized ReminderState reminderState(UUID playerId, String reminderId) {
        ReminderStateKey key = new ReminderStateKey(playerId, reminderId);
        return reminderStates.getOrDefault(key, ReminderState.defaults(key));
    }

    public synchronized ReminderState updateReminderState(ReminderState replacement) {
        ensureWritable();
        if (!reminderStates.containsKey(replacement.key())
                && reminderStates.size() >= MAXIMUM_PROFILES * 10L) {
            throw new IllegalStateException("reminder state limit reached");
        }
        reminderStates.put(replacement.key(), replacement);
        changed();
        return replacement;
    }

    public synchronized String textPage(String id) {
        return textPages.get(normalizeId(id));
    }

    public synchronized Map<String, String> textPages() {
        return Map.copyOf(textPages);
    }

    public synchronized void setTextPage(String id, String content) {
        ensureWritable();
        String key = normalizeId(id);
        if (content == null || content.isBlank()) {
            textPages.remove(key);
        } else {
            if (!textPages.containsKey(key) && textPages.size() >= MAXIMUM_TEXT_PAGES) {
                throw new IllegalStateException("text page limit reached");
            }
            textPages.put(key, bounded(content, 16_384, "text page"));
        }
        changed();
    }

    @Override
    public String id() {
        return "sef:social";
    }

    @Override
    public String domain() {
        return "social identity mail and reminders";
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public synchronized Path path() {
        return path;
    }

    @Override
    public synchronized LoadResult load(Path managedRoot) {
        path = managedRoot.resolve("social.json");
        clear();
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = java.nio.file.Files.exists(
                    path,
                    java.nio.file.LinkOption.NOFOLLOW_LINKS)
                    ? RepositoryState.RECOVERY
                    : RepositoryState.MISSING;
            return new LoadResult(state, state == RepositoryState.MISSING ? "new repository" : "storage unavailable");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null) {
                throw new IllegalStateException("social data is empty");
            }
            loadSnapshot(snapshot);
            state = RepositoryState.READY;
            dirty = document.migrated();
            return new LoadResult(state, "loaded social data");
        } catch (RuntimeException exception) {
            clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        ensureWritable();
        StorageService.write(path, domain(), SCHEMA_VERSION, GSON.toJsonTree(snapshot()), document);
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(document);
        dirty = false;
        state = RepositoryState.READY;
    }

    @Override
    public synchronized boolean dirty() {
        return dirty;
    }

    @Override
    public synchronized RepositoryState state() {
        return state;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
                List.copyOf(preferences.values()),
                List.copyOf(mail.values()),
                List.copyOf(connectionTemplates.values()),
                List.copyOf(reminders.values()),
                List.copyOf(reminderStates.values()),
                Map.copyOf(textPages));
    }

    private void loadSnapshot(Snapshot snapshot) {
        requireSize(snapshot.preferences(), MAXIMUM_PROFILES, "social profiles");
        requireSize(snapshot.mail(), MAXIMUM_MAIL, "mail");
        requireSize(snapshot.connectionTemplates(), MAXIMUM_PROFILES, "connection templates");
        requireSize(snapshot.reminders(), MAXIMUM_REMINDERS, "reminders");
        requireSize(snapshot.reminderStates(), MAXIMUM_PROFILES * 10L, "reminder states");
        if (snapshot.textPages().size() > MAXIMUM_TEXT_PAGES) {
            throw new IllegalStateException("text page limit exceeded");
        }
        snapshot.preferences().forEach(value -> putUnique(preferences, value.playerId(), value, "social profile"));
        snapshot.mail().forEach(value -> {
            putUnique(mail, value.id(), value, "mail");
            indexMail(value);
        });
        snapshot.connectionTemplates().forEach(value ->
                putUnique(connectionTemplates, value.playerId(), value, "connection template"));
        snapshot.reminders().forEach(value -> putUnique(reminders, value.id(), value, "reminder"));
        snapshot.reminderStates().forEach(value ->
                putUnique(reminderStates, value.key(), value, "reminder state"));
        snapshot.textPages().forEach((key, value) -> textPages.put(normalizeId(key), bounded(value, 16_384, "text page")));
    }

    private void ensureWritable() {
        if (path == null || state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED || state == RepositoryState.ERROR) {
            throw new IllegalStateException("social repository is not writable");
        }
    }

    private void changed() {
        dirty = true;
        if (state == RepositoryState.MISSING || state == RepositoryState.NEW) {
            state = RepositoryState.READY;
        }
    }

    private void clear() {
        preferences.clear();
        mail.clear();
        mailByRecipient.clear();
        connectionTemplates.clear();
        reminders.clear();
        reminderStates.clear();
        textPages.clear();
        dirty = false;
    }

    private List<MailRecord> recipientMail(UUID recipient) {
        Set<UUID> ids = mailByRecipient.get(recipient);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream().map(mail::get).filter(Objects::nonNull).toList();
    }

    private void indexMail(MailRecord record) {
        mailByRecipient.computeIfAbsent(record.recipientId(), ignored -> new LinkedHashSet<>()).add(record.id());
    }

    private void removeMailIndex(MailRecord record) {
        LinkedHashSet<UUID> ids = mailByRecipient.get(record.recipientId());
        if (ids == null) {
            return;
        }
        ids.remove(record.id());
        if (ids.isEmpty()) {
            mailByRecipient.remove(record.recipientId());
        }
    }

    private void pruneExpiredMail(Instant now) {
        int before = mail.size();
        List<MailRecord> expired = mail.values().stream().filter(record -> record.expired(now)).toList();
        for (MailRecord record : expired) {
            mail.remove(record.id());
            removeMailIndex(record);
        }
        if (mail.size() != before) {
            changed();
        }
    }

    private static <K, V> void putUnique(Map<K, V> target, K key, V value, String label) {
        if (target.putIfAbsent(key, value) != null) {
            throw new IllegalStateException("duplicate " + label);
        }
    }

    private static void requireSize(List<?> values, long maximum, String label) {
        if (values == null || values.size() > maximum) {
            throw new IllegalStateException(label + " collection is invalid");
        }
    }

    private static String normalizeId(String value) {
        String result = Objects.requireNonNull(value, "id").trim().toLowerCase(Locale.ROOT);
        if (!result.matches("[a-z0-9][a-z0-9_.-]{0,63}")) {
            throw new IllegalArgumentException("invalid id");
        }
        return result;
    }

    private static String bounded(String value, int maximum, String label) {
        String result = Objects.requireNonNull(value, label).trim();
        if (result.length() > maximum || result.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(label + " is invalid");
        }
        return result;
    }

    public record Snapshot(
            List<SocialPreferences> preferences,
            List<MailRecord> mail,
            List<ConnectionTemplates> connectionTemplates,
            List<ReminderDefinition> reminders,
            List<ReminderState> reminderStates,
            Map<String, String> textPages
    ) {
        public Snapshot {
            preferences = preferences == null ? List.of() : List.copyOf(preferences);
            mail = mail == null ? List.of() : List.copyOf(mail);
            connectionTemplates = connectionTemplates == null ? List.of() : List.copyOf(connectionTemplates);
            reminders = reminders == null ? List.of() : List.copyOf(reminders);
            reminderStates = reminderStates == null ? List.of() : List.copyOf(reminderStates);
            textPages = textPages == null ? Map.of() : Map.copyOf(textPages);
        }
    }

    public record SocialPreferences(
            UUID playerId,
            boolean messagesEnabled,
            boolean repliesEnabled,
            Set<UUID> ignoredPlayers,
            boolean socialSpyRequested,
            SpyAudience spyAudience,
            SpyMatch spyMatch,
            boolean spyContent,
            Set<UUID> spySelectedPlayers,
            Set<String> spyRoutes,
            long revision
    ) {
        public SocialPreferences {
            Objects.requireNonNull(playerId, "playerId");
            ignoredPlayers = boundedIds(ignoredPlayers, 512, "ignored players");
            spyAudience = Objects.requireNonNull(spyAudience, "spyAudience");
            spyMatch = Objects.requireNonNull(spyMatch, "spyMatch");
            spySelectedPlayers = boundedIds(spySelectedPlayers, 32, "spy selected players");
            Set<String> routes = new LinkedHashSet<>();
            for (String route : spyRoutes == null ? Set.<String>of() : spyRoutes) {
                routes.add(normalizeId(route));
            }
            if (routes.size() > 64) {
                throw new IllegalArgumentException("spy route limit exceeded");
            }
            spyRoutes = Set.copyOf(routes);
            if (revision < 1) {
                throw new IllegalArgumentException("invalid social revision");
            }
        }

        public static SocialPreferences defaults(UUID playerId) {
            return new SocialPreferences(
                    playerId, true, true, Set.of(), false,
                    SpyAudience.EVERYONE, SpyMatch.EITHER, false, Set.of(), Set.of(), 1);
        }

        public SocialPreferences withIgnoredPlayers(Set<UUID> value) {
            return new SocialPreferences(playerId, messagesEnabled, repliesEnabled, value, socialSpyRequested,
                    spyAudience, spyMatch, spyContent, spySelectedPlayers, spyRoutes, revision + 1);
        }

        public SocialPreferences withMessagesEnabled(boolean value) {
            return new SocialPreferences(playerId, value, repliesEnabled, ignoredPlayers, socialSpyRequested,
                    spyAudience, spyMatch, spyContent, spySelectedPlayers, spyRoutes, revision + 1);
        }

        public SocialPreferences withRepliesEnabled(boolean value) {
            return new SocialPreferences(playerId, messagesEnabled, value, ignoredPlayers, socialSpyRequested,
                    spyAudience, spyMatch, spyContent, spySelectedPlayers, spyRoutes, revision + 1);
        }

        public SocialPreferences withSpy(
                boolean requested,
                SpyAudience audience,
                SpyMatch match,
                boolean content,
                Set<UUID> selected,
                Set<String> routes
        ) {
            return new SocialPreferences(playerId, messagesEnabled, repliesEnabled, ignoredPlayers, requested,
                    audience, match, content, selected, routes, revision + 1);
        }
    }

    private static Set<UUID> boundedIds(Set<UUID> values, int maximum, String label) {
        Set<UUID> result = Set.copyOf(values == null ? Set.of() : values);
        if (result.size() > maximum) {
            throw new IllegalArgumentException(label + " limit exceeded");
        }
        return result;
    }

    public record MailRecord(
            UUID id,
            UUID senderId,
            UUID recipientId,
            String body,
            Instant createdAt,
            Instant expiresAt,
            boolean read,
            boolean archived
    ) {
        public MailRecord {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(senderId, "senderId");
            Objects.requireNonNull(recipientId, "recipientId");
            body = bounded(body, 16_384, "mail body");
            Objects.requireNonNull(createdAt, "createdAt");
            Objects.requireNonNull(expiresAt, "expiresAt");
            if (!expiresAt.isAfter(createdAt)) {
                throw new IllegalArgumentException("mail expiry must follow creation");
            }
        }

        public boolean expired(Instant now) {
            return !expiresAt.isAfter(now);
        }

        public MailRecord withRead(boolean value) {
            return new MailRecord(id, senderId, recipientId, body, createdAt, expiresAt, value, archived);
        }

        public MailRecord withArchived(boolean value) {
            return new MailRecord(id, senderId, recipientId, body, createdAt, expiresAt, read, value);
        }
    }

    public record ConnectionTemplates(UUID playerId, String joinTemplate, String leaveTemplate, long revision) {
        public ConnectionTemplates {
            Objects.requireNonNull(playerId, "playerId");
            joinTemplate = template(joinTemplate);
            leaveTemplate = template(leaveTemplate);
            if (revision < 1) {
                throw new IllegalArgumentException("invalid connection template revision");
            }
        }

        private static String template(String value) {
            return value == null || value.isBlank() ? "" : bounded(value, 512, "connection template");
        }
    }

    public record ReminderDefinition(
            String id,
            boolean enabled,
            String message,
            ReminderAudience audience,
            long repeatSeconds,
            int maximumDeliveries,
            boolean allowDismissal,
            int acknowledgementRevision,
            UUID createdBy,
            Instant updatedAt
    ) {
        public ReminderDefinition {
            id = normalizeId(id);
            message = bounded(message, 4096, "reminder message");
            Objects.requireNonNull(audience, "audience");
            if (repeatSeconds < 0 || repeatSeconds > 31_536_000L
                    || maximumDeliveries < 1 || maximumDeliveries > 1000
                    || acknowledgementRevision < 1) {
                throw new IllegalArgumentException("invalid reminder policy");
            }
            Objects.requireNonNull(createdBy, "createdBy");
            Objects.requireNonNull(updatedAt, "updatedAt");
        }

        public ReminderDefinition withEnabled(boolean value, UUID actor) {
            return new ReminderDefinition(id, value, message, audience, repeatSeconds, maximumDeliveries,
                    allowDismissal, acknowledgementRevision, actor, Instant.now());
        }
    }

    public record ReminderStateKey(UUID playerId, String reminderId) {
        public ReminderStateKey {
            Objects.requireNonNull(playerId, "playerId");
            reminderId = normalizeId(reminderId);
        }
    }

    public record ReminderState(
            ReminderStateKey key,
            Instant lastDeliveredAt,
            int deliveryCount,
            boolean dismissed,
            int acknowledgedRevision
    ) {
        public ReminderState {
            Objects.requireNonNull(key, "key");
            if (deliveryCount < 0 || acknowledgedRevision < 0) {
                throw new IllegalArgumentException("invalid reminder state");
            }
        }

        public static ReminderState defaults(ReminderStateKey key) {
            return new ReminderState(key, null, 0, false, 0);
        }

        public ReminderState delivered(Instant now, int revision) {
            return new ReminderState(key, now, deliveryCount + 1, dismissed, revision);
        }

        public ReminderState withDismissed(boolean value) {
            return new ReminderState(key, lastDeliveredAt, deliveryCount, value, acknowledgedRevision);
        }
    }

    public enum SpyAudience {
        EVERYONE,
        SELECTED
    }

    public enum SpyMatch {
        SENDER,
        RECIPIENT,
        EITHER
    }

    public enum ReminderAudience {
        ALL,
        FIRST_JOIN,
        COMMAND_FALLBACK,
        UNREAD_MAIL
    }

    public enum MailMutation {
        READ,
        ARCHIVE,
        DELETE
    }

    private static final class InstantAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
        @Override
        public JsonElement serialize(Instant source, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(source.toString());
        }

        @Override
        public Instant deserialize(JsonElement source, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                return Instant.parse(source.getAsString());
            } catch (RuntimeException exception) {
                throw new JsonParseException("invalid instant", exception);
            }
        }
    }
}
