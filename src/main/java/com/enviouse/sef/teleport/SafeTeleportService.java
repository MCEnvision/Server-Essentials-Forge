package com.enviouse.sef.teleport;

import com.enviouse.sef.storage.repository.LocationHistoryRepository;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class SafeTeleportService {
    private final LocationHistoryRepository history;
    private volatile ClaimValidator claimValidator = ClaimValidator.ALLOW_ALL;

    public SafeTeleportService(LocationHistoryRepository history) {
        this.history = Objects.requireNonNull(history, "history");
    }

    public void setClaimValidator(ClaimValidator replacement) {
        claimValidator = Objects.requireNonNull(replacement, "replacement");
    }

    public Validation validate(
            MinecraftServer server,
            Player actor,
            Player target,
            SavedLocation candidate,
            Policy policy
    ) {
        return validate(server, actor, target, candidate, policy, false);
    }

    public Validation validate(
            MinecraftServer server,
            Player actor,
            Player target,
            SavedLocation candidate,
            Policy policy,
            boolean surfaceOnly
    ) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(candidate, "candidate");
        Objects.requireNonNull(policy, "policy");
        ResourceLocation dimensionId = ResourceLocation.tryParse(candidate.dimensionId());
        if (dimensionId == null) {
            return Validation.failure(ResultCode.DIMENSION_MISSING, "invalid dimension identifier");
        }
        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));
        if (level == null) {
            return Validation.failure(ResultCode.DIMENSION_MISSING, candidate.dimensionId());
        }
        if (!policy.allowInCombat() && target.getCombatTracker().getCombatDuration() > 0) {
            return Validation.failure(ResultCode.COMBAT_DENIED, "target is in combat");
        }

        BlockPos origin = candidate.blockPosition();
        Set<Long> inspectedChunks = new HashSet<>();
        int checks = 0;
        for (BlockPos position : candidates(origin, policy.searchRadius())) {
            if (++checks > policy.maximumChecks()) {
                return Validation.failure(ResultCode.NO_SAFE_SPACE, "safe position check budget exhausted");
            }
            ChunkPos chunk = new ChunkPos(position);
            inspectedChunks.add(chunk.toLong());
            if (inspectedChunks.size() > policy.maximumChunks()) {
                return Validation.failure(ResultCode.CHUNK_BUDGET_EXCEEDED, "chunk inspection budget exhausted");
            }
            if (!level.hasChunkAt(position)) {
                continue;
            }
            ResultCode result = inspect(level, actor, target, position, policy, surfaceOnly);
            if (result == ResultCode.SUCCESS) {
                SavedLocation resolved = new SavedLocation(
                        candidate.dimensionId(),
                        position.getX() + 0.5D,
                        position.getY(),
                        position.getZ() + 0.5D,
                        candidate.yaw(),
                        candidate.pitch());
                return Validation.success(level, resolved, checks, inspectedChunks.size());
            }
            if (result == ResultCode.OUTSIDE_BORDER || result == ResultCode.CLAIM_DENIED) {
                return Validation.failure(result, "destination policy denied");
            }
        }
        return Validation.failure(ResultCode.NO_SAFE_SPACE, "no safe loaded destination was found");
    }

    public TeleportResult teleport(
            MinecraftServer server,
            ServerPlayer actor,
            ServerPlayer target,
            SavedLocation candidate,
            String reason,
            Policy policy,
            DestinationGuard guard
    ) {
        return teleport(server, actor, target, candidate, reason, policy, guard, false);
    }

    public TeleportResult teleport(
            MinecraftServer server,
            ServerPlayer actor,
            ServerPlayer target,
            SavedLocation candidate,
            String reason,
            Policy policy,
            DestinationGuard guard,
            boolean surfaceOnly
    ) {
        if (target == null || target.hasDisconnected()) {
            return TeleportResult.failure(ResultCode.TARGET_OFFLINE, "target is offline");
        }
        if (!Objects.requireNonNull(guard, "guard").stillValid()) {
            return TeleportResult.failure(ResultCode.STATE_CHANGED, "destination changed before validation");
        }
        Validation validation = validate(server, actor, target, candidate, policy, surfaceOnly);
        if (!validation.successful()) {
            return TeleportResult.failure(validation.code(), validation.detail());
        }
        if (!guard.stillValid()) {
            return TeleportResult.failure(ResultCode.STATE_CHANGED, "destination changed before commit");
        }

        SavedLocation departure = SavedLocation.from(target);
        if (policy.recordHistory()) {
            history.record(target.getUUID(), new LocationHistoryRepository.LocationRecord(
                    departure.dimensionId(),
                    departure.x(),
                    departure.y(),
                    departure.z(),
                    departure.yaw(),
                    departure.pitch(),
                    Instant.now(),
                    boundedReason(reason)));
        }

        target.unRide();
        if (target.isSleeping()) {
            target.stopSleepInBed(true, true);
        }
        SavedLocation destination = validation.location();
        target.teleportTo(
                validation.level(),
                destination.x(),
                destination.y(),
                destination.z(),
                Set.<RelativeMovement>of(),
                destination.yaw(),
                destination.pitch());
        target.setDeltaMovement(Vec3.ZERO);
        target.resetFallDistance();
        if (policy.invulnerabilityTicks() > 0) {
            target.invulnerableTime = Math.max(target.invulnerableTime, policy.invulnerabilityTicks());
        }
        return TeleportResult.success(destination);
    }

    private ResultCode inspect(
            ServerLevel level,
            Player actor,
            Player target,
            BlockPos feet,
            Policy policy,
            boolean surfaceOnly
    ) {
        if (!level.getWorldBorder().isWithinBounds(feet)) {
            return ResultCode.OUTSIDE_BORDER;
        }
        if (feet.getY() < level.getMinBuildHeight()
                || feet.getY() + 1 >= level.getMaxBuildHeight()) {
            return ResultCode.HAZARD;
        }
        if (level.dimension() == Level.NETHER
                && !policy.allowNetherRoof()
                && feet.getY() >= level.getMaxBuildHeight() - 2) {
            return ResultCode.HAZARD;
        }
        if (!claimValidator.canEnter(actor, target, level, feet)) {
            return ResultCode.CLAIM_DENIED;
        }
        BlockPos head = feet.above();
        BlockPos support = feet.below();
        BlockState feetState = level.getBlockState(feet);
        BlockState headState = level.getBlockState(head);
        BlockState supportState = level.getBlockState(support);
        if (feetState.getFluidState().is(FluidTags.WATER)
                || headState.getFluidState().is(FluidTags.WATER)
                || supportState.getFluidState().is(FluidTags.WATER)) {
            return ResultCode.HAZARD;
        }
        if (surfaceOnly
                && level.getHeight(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                feet.getX(),
                feet.getZ()) != feet.getY()) {
            return ResultCode.HAZARD;
        }
        if (!feetState.getCollisionShape(level, feet).isEmpty()
                || !headState.getCollisionShape(level, head).isEmpty()) {
            return ResultCode.NO_SAFE_SPACE;
        }
        if (!supportState.isFaceSturdy(level, support, Direction.UP)) {
            return ResultCode.HAZARD;
        }
        if (!policy.allowHazards()
                && (dangerous(feetState) || dangerous(headState) || dangerous(supportState))) {
            return ResultCode.HAZARD;
        }
        return ResultCode.SUCCESS;
    }

    private static boolean dangerous(BlockState state) {
        return state.getFluidState().is(FluidTags.WATER)
                || state.getFluidState().is(FluidTags.LAVA)
                || state.is(Blocks.FIRE)
                || state.is(Blocks.SOUL_FIRE)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.MAGMA_BLOCK)
                || state.is(Blocks.CAMPFIRE)
                || state.is(Blocks.SOUL_CAMPFIRE)
                || state.is(Blocks.SWEET_BERRY_BUSH)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.WITHER_ROSE);
    }

    private static Iterable<BlockPos> candidates(BlockPos origin, int radius) {
        Set<BlockPos> positions = new java.util.LinkedHashSet<>();
        positions.add(origin);
        for (int distance = 1; distance <= radius; distance++) {
            for (int x = -distance; x <= distance; x++) {
                positions.add(origin.offset(x, 0, -distance));
                positions.add(origin.offset(x, 0, distance));
            }
            for (int z = -distance + 1; z < distance; z++) {
                positions.add(origin.offset(-distance, 0, z));
                positions.add(origin.offset(distance, 0, z));
            }
        }
        if (radius > 0) {
            Set<BlockPos> expanded = new java.util.LinkedHashSet<>();
            for (BlockPos horizontal : positions) {
                expanded.add(horizontal);
                for (int y = 1; y <= Math.min(4, radius); y++) {
                    expanded.add(horizontal.above(y));
                    expanded.add(horizontal.below(y));
                }
            }
            return expanded;
        }
        return positions;
    }

    private static String boundedReason(String value) {
        String result = value == null ? "teleport" : value.trim();
        if (result.isEmpty()) {
            return "teleport";
        }
        return result.length() > 64 ? result.substring(0, 64) : result;
    }

    public record Policy(
            int searchRadius,
            int maximumChecks,
            int maximumChunks,
            boolean allowHazards,
            boolean allowNetherRoof,
            boolean allowInCombat,
            boolean recordHistory,
            int invulnerabilityTicks
    ) {
        public Policy {
            if (searchRadius < 0 || searchRadius > 32
                    || maximumChecks < 1 || maximumChecks > 100_000
                    || maximumChunks < 1 || maximumChunks > 256
                    || invulnerabilityTicks < 0 || invulnerabilityTicks > 200) {
                throw new IllegalArgumentException("Safe teleport policy is outside hard bounds");
            }
        }
    }

    public record Validation(
            boolean successful,
            ResultCode code,
            String detail,
            ServerLevel level,
            SavedLocation location,
            int inspectedPositions,
            int inspectedChunks
    ) {
        public Validation {
            code = Objects.requireNonNull(code, "code");
            detail = detail == null ? "" : detail;
        }

        public static Validation success(
                ServerLevel level,
                SavedLocation location,
                int inspectedPositions,
                int inspectedChunks
        ) {
            return new Validation(
                    true,
                    ResultCode.SUCCESS,
                    "",
                    level,
                    location,
                    inspectedPositions,
                    inspectedChunks);
        }

        public static Validation failure(ResultCode code, String detail) {
            return new Validation(false, code, detail, null, null, 0, 0);
        }
    }

    public record TeleportResult(
            boolean successful,
            ResultCode code,
            String detail,
            SavedLocation location
    ) {
        public TeleportResult {
            code = Objects.requireNonNull(code, "code");
            detail = detail == null ? "" : detail;
        }

        public static TeleportResult success(SavedLocation location) {
            return new TeleportResult(true, ResultCode.SUCCESS, "", location);
        }

        public static TeleportResult failure(ResultCode code, String detail) {
            return new TeleportResult(false, code, detail, null);
        }
    }

    public enum ResultCode {
        SUCCESS,
        DIMENSION_MISSING,
        OUTSIDE_BORDER,
        CHUNK_BUDGET_EXCEEDED,
        CHUNK_TIMEOUT,
        NO_SAFE_SPACE,
        HAZARD,
        CLAIM_DENIED,
        COMBAT_DENIED,
        MOVEMENT_CANCELLED,
        PERMISSION_LOST,
        TARGET_OFFLINE,
        STATE_CHANGED,
        PROVIDER_ERROR
    }

    @FunctionalInterface
    public interface DestinationGuard {
        DestinationGuard ALWAYS = () -> true;

        boolean stillValid();
    }

    @FunctionalInterface
    public interface ClaimValidator {
        ClaimValidator ALLOW_ALL = (actor, target, level, position) -> true;

        boolean canEnter(Player actor, Player target, ServerLevel level, BlockPos position);
    }
}
