package com.enviouse.sef.invsee;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OfflinePlayerInventoryAdapterTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void commitBacksUpAndAtomicallyPublishesRevisionedInventory() throws Exception {
        UUID playerId = UUID.randomUUID();
        Path playerData = temporaryDirectory.resolve("playerdata");
        Path backups = temporaryDirectory.resolve("backups");
        Files.createDirectories(playerData);
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        writePlayerData(
                playerData.resolve(playerId + ".dat"),
                registries,
                new ItemStack(Items.DIAMOND, 3));
        OfflinePlayerInventoryAdapter adapter = new OfflinePlayerInventoryAdapter(
                playerData,
                backups,
                registries,
                DataFixers.getDataFixer(),
                4 * 1024 * 1024,
                4);

        var original = adapter.load(playerId, "offline_player");
        assertEquals(3, original.stacks().getFirst().getCount());
        SimpleContainer edited = menu(original);
        edited.setItem(0, new ItemStack(Items.EMERALD, 7));

        var committed = adapter.commit(original, edited);

        assertFalse(committed.revision().equals(original.revision()));
        var reloaded = adapter.load(playerId, "offline_player");
        assertTrue(ItemStack.isSameItem(reloaded.stacks().getFirst(), new ItemStack(Items.EMERALD)));
        assertEquals(7, reloaded.stacks().getFirst().getCount());
        try (var files = Files.list(backups)) {
            Path backup = files.findFirst().orElseThrow();
            CompoundTag backedUp = NbtIo.readCompressed(backup, NbtAccounter.create(4 * 1024 * 1024));
            ItemStack stack = ItemStack.parse(
                    registries,
                    backedUp.getList("Inventory", CompoundTag.TAG_COMPOUND).getCompound(0))
                    .orElseThrow();
            assertTrue(ItemStack.isSameItem(stack, new ItemStack(Items.DIAMOND)));
            assertEquals(3, stack.getCount());
        }
    }

    @Test
    void staleRevisionCannotOverwriteNewerPlayerData() throws Exception {
        UUID playerId = UUID.randomUUID();
        Path playerData = temporaryDirectory.resolve("playerdata");
        Files.createDirectories(playerData);
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        writePlayerData(
                playerData.resolve(playerId + ".dat"),
                registries,
                new ItemStack(Items.DIAMOND));
        OfflinePlayerInventoryAdapter adapter = new OfflinePlayerInventoryAdapter(
                playerData,
                temporaryDirectory.resolve("backups"),
                registries,
                DataFixers.getDataFixer(),
                4 * 1024 * 1024,
                4);
        var stale = adapter.load(playerId, "offline_player");
        SimpleContainer first = menu(stale);
        first.setItem(0, new ItemStack(Items.EMERALD));
        adapter.commit(stale, first);
        SimpleContainer conflicting = menu(stale);
        conflicting.setItem(0, new ItemStack(Items.GOLD_INGOT));

        assertThrows(
                OfflinePlayerInventoryAdapter.StaleRevisionException.class,
                () -> adapter.commit(stale, conflicting));
        assertTrue(ItemStack.isSameItem(
                adapter.load(playerId, "offline_player").stacks().getFirst(),
                new ItemStack(Items.EMERALD)));
    }

    @Test
    void unsupportedItemEntryIsPreservedAndForcesReadOnlyMode() throws Exception {
        UUID playerId = UUID.randomUUID();
        Path playerData = temporaryDirectory.resolve("playerdata");
        Files.createDirectories(playerData);
        CompoundTag player = new CompoundTag();
        NbtUtils.addCurrentDataVersion(player);
        ListTag inventory = new ListTag();
        CompoundTag unknown = new CompoundTag();
        unknown.putByte("Slot", (byte) 0);
        unknown.putString("id", "missing_namespace:missing_item");
        unknown.putInt("count", 1);
        inventory.add(unknown);
        player.put("Inventory", inventory);
        NbtIo.writeCompressed(player, playerData.resolve(playerId + ".dat"));
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        OfflinePlayerInventoryAdapter adapter = new OfflinePlayerInventoryAdapter(
                playerData,
                temporaryDirectory.resolve("backups"),
                registries,
                DataFixers.getDataFixer(),
                4 * 1024 * 1024,
                4);

        var snapshot = adapter.load(playerId, "offline_player");

        assertFalse(snapshot.mutable());
        assertEquals(1, snapshot.preservedEntries().size());
        assertThrows(java.io.IOException.class, () -> adapter.commit(snapshot, menu(snapshot)));
    }

    private static SimpleContainer menu(OfflinePlayerInventoryAdapter.Snapshot snapshot) {
        SimpleContainer container = new SimpleContainer(OfflinePlayerInventoryAdapter.MENU_SLOTS);
        for (int slot = 0; slot < snapshot.stacks().size(); slot++) {
            container.setItem(slot, snapshot.stacks().get(slot).copy());
        }
        return container;
    }

    private static void writePlayerData(
            Path file,
            RegistryAccess registries,
            ItemStack stack
    ) throws Exception {
        CompoundTag player = new CompoundTag();
        NbtUtils.addCurrentDataVersion(player);
        ListTag inventory = new ListTag();
        CompoundTag entry = new CompoundTag();
        entry.putByte("Slot", (byte) 0);
        inventory.add(stack.save(registries, entry));
        player.put("Inventory", inventory);
        NbtIo.writeCompressed(player, file);
    }
}
