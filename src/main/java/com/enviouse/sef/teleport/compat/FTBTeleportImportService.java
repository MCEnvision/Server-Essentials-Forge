package com.enviouse.sef.teleport.compat;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.teleport.SavedLocation;
import com.enviouse.sef.teleport.TeleportRepository;
import dev.ftb.mods.ftbessentials.util.FTBEPlayerData;
import dev.ftb.mods.ftbessentials.util.FTBEWorldData;
import dev.ftb.mods.ftbessentials.util.SavedTeleportManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public final class FTBTeleportImportService {
    private static final String IMPORT_ID = "ftb_essentials:homes_and_warps:v1";

    private FTBTeleportImportService() {
    }

    public static boolean importOnce(MinecraftServer server) {
        TeleportRepository repository = KernelServices.teleports();
        if (repository.completedImport(IMPORT_ID)) {
            return true;
        }
        FTBEWorldData worldData = FTBEWorldData.instance;
        if (worldData == null) {
            ServerEssentialsForge.LOGGER.warn(
                    "[SEF] FTB Essentials teleport import was deferred because its world data is not ready");
            return false;
        }

        int homes = 0;
        int warps = 0;
        int invalid = 0;
        for (UUID playerId : FTBEPlayerData.getAllKnownPlayers()) {
            FTBEPlayerData playerData = FTBEPlayerData.getOrCreate(server, playerId).orElse(null);
            if (playerData == null) {
                invalid++;
                continue;
            }
            for (SavedTeleportManager.DestinationEntry entry :
                    playerData.homeManager().destinations().toList()) {
                try {
                    ActionResult<com.enviouse.sef.teleport.HomeRecord> result = repository.setHome(
                            playerId,
                            entry.name(),
                            decode(entry.destination().write()),
                            1000,
                            1000,
                            false);
                    if (result.successful()) {
                        homes++;
                    } else if (result.reason() != com.enviouse.sef.kernel.ActionResult.ReasonCode.CONFIRMATION_REQUIRED) {
                        invalid++;
                    }
                } catch (RuntimeException exception) {
                    invalid++;
                    ServerEssentialsForge.LOGGER.warn(
                            "[SEF] Skipped invalid FTB Essentials home {} for {}",
                            entry.name(),
                            playerId,
                            exception);
                }
            }
        }
        for (SavedTeleportManager.DestinationEntry entry :
                worldData.warpManager().destinations().toList()) {
            try {
                ActionResult<com.enviouse.sef.teleport.WarpRecord> result = repository.setServerWarp(
                        entry.name(),
                        decode(entry.destination().write()),
                        false);
                if (result.successful()) {
                    warps++;
                } else if (result.reason() != com.enviouse.sef.kernel.ActionResult.ReasonCode.CONFIRMATION_REQUIRED) {
                    invalid++;
                }
            } catch (RuntimeException exception) {
                invalid++;
                ServerEssentialsForge.LOGGER.warn(
                        "[SEF] Skipped invalid FTB Essentials warp {}",
                        entry.name(),
                        exception);
            }
        }
        if (invalid > 0) {
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] FTB Essentials teleport import retained its retry marker because {} records failed",
                    invalid);
            return false;
        }
        repository.markImportComplete(IMPORT_ID);
        ServerEssentialsForge.LOGGER.info(
                "[SEF] Imported {} FTB Essentials homes and {} warps into independent SEF records",
                homes,
                warps);
        return true;
    }

    private static SavedLocation decode(CompoundTag tag) {
        return new SavedLocation(
                tag.getString("dim"),
                tag.getInt("x") + 0.5D,
                tag.getInt("y"),
                tag.getInt("z") + 0.5D,
                tag.contains("yRot") ? tag.getFloat("yRot") : 0F,
                tag.contains("xRot") ? tag.getFloat("xRot") : 0F);
    }
}
