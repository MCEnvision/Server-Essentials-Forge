package com.enviouse.sef.disguise;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class SefBlazeFireball extends SmallFireball {
    private final Vec3 origin;
    private final float directDamage;
    private final int fireSeconds;
    private final boolean allowPvp;
    private final double maximumRangeSquared;

    public SefBlazeFireball(
            ServerPlayer owner,
            Vec3 direction,
            float directDamage,
            int fireSeconds,
            boolean allowPvp,
            int maximumRange
    ) {
        super(owner.level(), owner, direction);
        this.origin = owner.getEyePosition();
        this.directDamage = Math.clamp(directDamage, 0.0F, 100.0F);
        this.fireSeconds = Math.clamp(fireSeconds, 0, 60);
        this.allowPvp = allowPvp;
        int boundedRange = Math.clamp(maximumRange, 4, 128);
        this.maximumRangeSquared = (double) boundedRange * boundedRange;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && distanceToSqr(origin) > maximumRangeSquared) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level().isClientSide) {
            return;
        }
        Entity target = result.getEntity();
        Entity owner = getOwner();
        if (target instanceof ServerPlayer && owner instanceof ServerPlayer && !allowPvp) {
            discard();
            return;
        }
        if (directDamage > 0.0F) {
            target.hurt(damageSources().fireball(this, owner), directDamage);
        }
        if (fireSeconds > 0) {
            target.igniteForSeconds(fireSeconds);
        }
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!level().isClientSide) {
            discard();
        }
    }
}
