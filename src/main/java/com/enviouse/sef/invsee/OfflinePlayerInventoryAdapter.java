package com.enviouse.sef.invsee;

import com.enviouse.sef.recovery.ItemStackSnapshotCodec;
import com.mojang.datafixers.DataFixer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class OfflinePlayerInventoryAdapter {
    public static final int ADAPTER_VERSION = 1;
    public static final int INVENTORY_SLOTS = 41;
    public static final int MENU_SLOTS = 45;
    public static final int HARD_MAXIMUM_FILE_BYTES = 16 * 1024 * 1024;
    private static final int HARD_MAXIMUM_NBT_ENTRIES = 256;

    private final Path playerDataRoot;
    private final Path backupRoot;
    private final HolderLookup.Provider registries;
    private final DataFixer dataFixer;
    private final int maximumFileBytes;
    private final int maximumBackups;

    public OfflinePlayerInventoryAdapter(
            Path playerDataRoot,
            Path backupRoot,
            HolderLookup.Provider registries,
            DataFixer dataFixer,
            int maximumFileBytes,
            int maximumBackups
    ) {
        this.playerDataRoot = Objects.requireNonNull(playerDataRoot, "playerDataRoot")
                .toAbsolutePath()
                .normalize();
        this.backupRoot = Objects.requireNonNull(backupRoot, "backupRoot")
                .toAbsolutePath()
                .normalize();
        this.registries = Objects.requireNonNull(registries, "registries");
        this.dataFixer = Objects.requireNonNull(dataFixer, "dataFixer");
        if (maximumFileBytes < 64 * 1024 || maximumFileBytes > HARD_MAXIMUM_FILE_BYTES) {
            throw new IllegalArgumentException("offline player data size limit is outside bounds");
        }
        if (maximumBackups < 1 || maximumBackups > 128) {
            throw new IllegalArgumentException("offline inventory backup limit is outside bounds");
        }
        this.maximumFileBytes = maximumFileBytes;
        this.maximumBackups = maximumBackups;
    }

    public synchronized Snapshot load(UUID targetId, String targetName) throws IOException {
        UUID id = Objects.requireNonNull(targetId, "targetId");
        validateDirectory(playerDataRoot, false);
        Path source = ownedPlayerFile(id);
        validatePlayerFile(source);
        byte[] encoded = readBounded(source);
        String revision = sha256(encoded);
        CompoundTag raw;
        try (InputStream input = new ByteArrayInputStream(encoded)) {
            raw = NbtIo.readCompressed(input, NbtAccounter.create(maximumFileBytes));
        } catch (RuntimeException exception) {
            throw new IOException("offline player data could not be decoded", exception);
        }
        int sourceDataVersion = NbtUtils.getDataVersion(raw, -1);
        CompoundTag playerData;
        try {
            playerData = DataFixTypes.PLAYER.updateToCurrentVersion(dataFixer, raw, sourceDataVersion);
        } catch (RuntimeException exception) {
            throw new IOException("offline player data migration failed", exception);
        }
        NbtUtils.addCurrentDataVersion(playerData);
        DecodedInventory decoded = decodeInventory(playerData);
        return new Snapshot(
                id,
                boundedName(targetName),
                revision,
                sourceDataVersion,
                playerData,
                decoded.stacks(),
                decoded.preservedEntries(),
                decoded.mutable());
    }

    public synchronized Snapshot commit(Snapshot expected, SimpleContainer inventory) throws IOException {
        Objects.requireNonNull(expected, "expected");
        Objects.requireNonNull(inventory, "inventory");
        if (!expected.mutable()) {
            throw new IOException("offline inventory contains unsupported item data");
        }
        if (inventory.getContainerSize() != MENU_SLOTS) {
            throw new IOException("offline inventory menu size changed");
        }
        validateDirectory(playerDataRoot, false);
        Path target = ownedPlayerFile(expected.targetId());
        validatePlayerFile(target);
        byte[] current = readBounded(target);
        if (!sha256(current).equals(expected.revision())) {
            throw new StaleRevisionException();
        }

        List<ItemStack> stacks = new ArrayList<>(INVENTORY_SLOTS);
        for (int slot = 0; slot < INVENTORY_SLOTS; slot++) {
            stacks.add(inventory.getItem(slot).copy());
        }
        ItemStackSnapshotCodec.captureStacks(stacks, registries);
        CompoundTag replacement = expected.playerData();
        replacement.put("Inventory", encodeInventory(stacks, expected.preservedEntries()));
        NbtUtils.addCurrentDataVersion(replacement);

        createRecoveryBackup(expected.targetId(), expected.revision(), current);
        Path temporary = Files.createTempFile(
                playerDataRoot,
                "." + expected.targetId() + "-",
                ".dat.tmp");
        try {
            NbtIo.writeCompressed(replacement, temporary);
            validatePlayerFile(temporary);
            if (Files.size(temporary) > maximumFileBytes) {
                throw new IOException("offline player data exceeds the configured size limit");
            }
            force(temporary);
            byte[] committed = readBounded(temporary);
            try {
                Files.move(
                        temporary,
                        target,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                throw new IOException("atomic offline player data replacement is unavailable", exception);
            }
            return new Snapshot(
                    expected.targetId(),
                    expected.targetName(),
                    sha256(committed),
                    NbtUtils.getDataVersion(replacement, expected.sourceDataVersion()),
                    replacement,
                    stacks,
                    expected.preservedEntries(),
                    true);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private DecodedInventory decodeInventory(CompoundTag playerData) throws IOException {
        List<ItemStack> stacks = emptyStacks();
        ListTag preserved = new ListTag();
        boolean[] occupied = new boolean[INVENTORY_SLOTS];
        boolean mutable = true;
        ListTag inventory = playerData.getList("Inventory", Tag.TAG_COMPOUND);
        if (inventory.size() > HARD_MAXIMUM_NBT_ENTRIES) {
            throw new IOException("offline inventory entry count is outside bounds");
        }
        for (int index = 0; index < inventory.size(); index++) {
            CompoundTag entry = inventory.getCompound(index);
            int nbtSlot = entry.getByte("Slot") & 255;
            int slot = menuSlot(nbtSlot);
            if (slot < 0) {
                preserved.add(entry.copy());
                continue;
            }
            if (occupied[slot]) {
                throw new IOException("offline inventory contains duplicate slots");
            }
            occupied[slot] = true;
            ItemStack stack;
            try {
                stack = ItemStack.parse(registries, entry).orElse(ItemStack.EMPTY);
            } catch (RuntimeException exception) {
                stack = ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                preserved.add(entry.copy());
                mutable = false;
                continue;
            }
            stacks.set(slot, stack.copy());
        }
        ItemStackSnapshotCodec.captureStacks(stacks, registries);
        return new DecodedInventory(stacks, preserved, mutable);
    }

    private ListTag encodeInventory(List<ItemStack> stacks, ListTag preservedEntries) {
        ListTag result = preservedEntries.copy();
        for (int slot = 0; slot < INVENTORY_SLOTS; slot++) {
            ItemStack stack = stacks.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putByte("Slot", (byte) nbtSlot(slot));
            result.add(stack.save(registries, entry));
        }
        return result;
    }

    private void createRecoveryBackup(UUID targetId, String revision, byte[] source) throws IOException {
        validateDirectory(backupRoot, true);
        Path backup = backupRoot.resolve(targetId + "-" + revision + ".dat").normalize();
        if (!backupRoot.equals(backup.getParent())) {
            throw new IOException("offline inventory backup path escaped its root");
        }
        if (Files.exists(backup, LinkOption.NOFOLLOW_LINKS)) {
            if (Files.isSymbolicLink(backup)
                    || !MessageDigest.isEqual(source, readBoundedBackup(backup))) {
                throw new IOException("offline inventory recovery backup conflicts");
            }
            return;
        }
        pruneBackups(targetId, maximumBackups - 1);
        Files.write(
                backup,
                source,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE,
                LinkOption.NOFOLLOW_LINKS);
        force(backup);
    }

    private void pruneBackups(UUID targetId, int retainedBeforeWrite) throws IOException {
        String prefix = targetId + "-";
        List<Path> backups;
        try (var files = Files.list(backupRoot)) {
            backups = files
                    .filter(path -> path.getFileName().toString().startsWith(prefix))
                    .filter(path -> path.getFileName().toString().endsWith(".dat"))
                    .filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                    .sorted(Comparator.comparingLong(this::lastModified).reversed())
                    .toList();
        }
        for (int index = retainedBeforeWrite; index < backups.size(); index++) {
            Path candidate = backups.get(index);
            if (Files.isSymbolicLink(candidate)
                    || !backupRoot.equals(candidate.toAbsolutePath().normalize().getParent())) {
                throw new IOException("offline inventory backup path is unsafe");
            }
            Files.delete(candidate);
        }
    }

    private long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis();
        } catch (IOException exception) {
            return Long.MIN_VALUE;
        }
    }

    private Path ownedPlayerFile(UUID targetId) throws IOException {
        String name = targetId + ".dat";
        Path path = playerDataRoot.resolve(name).normalize();
        if (!playerDataRoot.equals(path.getParent()) || !name.equals(path.getFileName().toString())) {
            throw new IOException("offline player data path escaped its root");
        }
        return path;
    }

    private static void validateDirectory(Path directory, boolean create) throws IOException {
        for (Path current = directory; current != null; current = current.getParent()) {
            if (Files.exists(current, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(current)) {
                throw new IOException("offline inventory path contains a symbolic link");
            }
        }
        if (create) {
            Files.createDirectories(directory);
        }
        if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(directory)) {
            throw new IOException("offline inventory directory is unavailable or unsafe");
        }
    }

    private static void validatePlayerFile(Path file) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(
                file,
                BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS);
        if (!attributes.isRegularFile() || Files.isSymbolicLink(file)) {
            throw new IOException("offline player data file is unavailable or unsafe");
        }
        try {
            Object links = Files.getAttribute(file, "unix:nlink", LinkOption.NOFOLLOW_LINKS);
            if (links instanceof Number number && number.longValue() > 1L) {
                throw new IOException("offline player data file has multiple hard links");
            }
        } catch (UnsupportedOperationException ignored) {
        }
    }

    private byte[] readBounded(Path file) throws IOException {
        try (InputStream input = Files.newInputStream(
                file,
                StandardOpenOption.READ,
                LinkOption.NOFOLLOW_LINKS)) {
            byte[] data = input.readNBytes(maximumFileBytes + 1);
            if (data.length > maximumFileBytes) {
                throw new IOException("offline player data exceeds the configured size limit");
            }
            return data;
        }
    }

    private byte[] readBoundedBackup(Path file) throws IOException {
        try (InputStream input = Files.newInputStream(
                file,
                StandardOpenOption.READ,
                LinkOption.NOFOLLOW_LINKS)) {
            byte[] data = input.readNBytes(HARD_MAXIMUM_FILE_BYTES + 1);
            if (data.length > HARD_MAXIMUM_FILE_BYTES) {
                throw new IOException("offline inventory backup exceeds the hard size limit");
            }
            return data;
        }
    }

    private static void force(Path path) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
            channel.force(true);
        }
    }

    private static int menuSlot(int nbtSlot) {
        if (nbtSlot >= 0 && nbtSlot < 36) {
            return nbtSlot;
        }
        if (nbtSlot >= 100 && nbtSlot < 104) {
            return 36 + nbtSlot - 100;
        }
        if (nbtSlot == 150) {
            return 40;
        }
        return -1;
    }

    private static int nbtSlot(int menuSlot) {
        if (menuSlot < 36) {
            return menuSlot;
        }
        if (menuSlot < 40) {
            return 100 + menuSlot - 36;
        }
        return 150;
    }

    private static List<ItemStack> emptyStacks() {
        List<ItemStack> stacks = new ArrayList<>(INVENTORY_SLOTS);
        for (int slot = 0; slot < INVENTORY_SLOTS; slot++) {
            stacks.add(ItemStack.EMPTY);
        }
        return stacks;
    }

    private static String boundedName(String value) {
        String name = Objects.requireNonNullElse(value, "").strip();
        if (name.isEmpty() || name.length() > 64 || name.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("offline player name is invalid");
        }
        return name;
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("sha256 is unavailable", exception);
        }
    }

    public static final class StaleRevisionException extends IOException {
        public StaleRevisionException() {
            super("offline player data changed after the inventory was opened");
        }
    }

    public record Snapshot(
            UUID targetId,
            String targetName,
            String revision,
            int sourceDataVersion,
            CompoundTag playerData,
            List<ItemStack> stacks,
            ListTag preservedEntries,
            boolean mutable
    ) {
        public Snapshot {
            Objects.requireNonNull(targetId, "targetId");
            targetName = boundedName(targetName);
            if (revision == null || !revision.matches("[0-9a-f]{64}")) {
                throw new IllegalArgumentException("offline inventory revision is invalid");
            }
            playerData = Objects.requireNonNull(playerData, "playerData").copy();
            stacks = Objects.requireNonNull(stacks, "stacks").stream()
                    .map(stack -> Objects.requireNonNull(stack, "stack").copy())
                    .toList();
            if (stacks.size() != INVENTORY_SLOTS) {
                throw new IllegalArgumentException("offline inventory slot count is invalid");
            }
            preservedEntries = Objects.requireNonNull(preservedEntries, "preservedEntries").copy();
        }

        @Override
        public CompoundTag playerData() {
            return playerData.copy();
        }

        @Override
        public List<ItemStack> stacks() {
            return stacks.stream().map(ItemStack::copy).toList();
        }

        @Override
        public ListTag preservedEntries() {
            return preservedEntries.copy();
        }
    }

    private record DecodedInventory(
            List<ItemStack> stacks,
            ListTag preservedEntries,
            boolean mutable
    ) {
    }
}
