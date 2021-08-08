package com.codinglitch.ctweaks.registry.entities;

import com.codinglitch.ctweaks.registry.init.EntityInit;
import com.codinglitch.ctweaks.util.SoundsC;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class IllusionerGeyser extends Entity {
    private int total;
    private int warmupDelayTicks;
    private int lifeTicks = 60;
    private IllusionerModified owner;
    private UUID ownerUUID;

    public IllusionerGeyser(EntityType<?> entityType, Level world) {
        super(entityType, world);
    }

    public IllusionerGeyser(Level world, double x, double y, double z, int delay, IllusionerModified owner) {
        this(EntityInit.ILLUSIONER_GEYSER.get(), world);
        this.warmupDelayTicks = delay;
        this.total = delay;
        this.setOwner(owner);
        this.setPos(x, y, z);
    }

    public void setOwner(@Nullable IllusionerModified illusioner) {
        this.owner = illusioner;
        this.ownerUUID = illusioner == null ? null : illusioner.getUUID();
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.owner == null && this.ownerUUID != null && this.level instanceof ServerLevel) {
            Entity entity = ((ServerLevel)this.level).getEntity(this.ownerUUID);
            if (entity instanceof IllusionerModified) {
                this.owner = (IllusionerModified) entity;
            }
        }

        return this.owner;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.warmupDelayTicks = nbt.getInt("Warmup");
        if (nbt.hasUUID("Owner")) {
            this.ownerUUID = nbt.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("Warmup", this.warmupDelayTicks);
        if (this.ownerUUID != null) {
            nbt.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    public void tick() {
        super.tick();
        if (level.isClientSide) return;
        if (warmupDelayTicks > 0)
        {
            warmupDelayTicks--;
        }
        else
        {
            if (lifeTicks == 60)
            {
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundsC.illusioner_geyser.get(), this.getSoundSource(), 0.5f, (this.random.nextFloat() * 0.2F + 0.85F)+((float)total/20));
            }
            ((ServerLevel)this.level).sendParticles(ParticleTypes.CLOUD, getX(), getY(), getZ(), 1, 0.2f, 0.5f, 0.2f, 0.05f);

            for(LivingEntity livingentity : this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.2D, 0.0D, 0.2D))) {
                this.dealDamageTo(livingentity);
            }

            if (lifeTicks==0) this.remove(RemovalReason.DISCARDED);

            lifeTicks--;
        }
    }

    private void dealDamageTo(LivingEntity entity) {
        LivingEntity livingentity = this.getOwner();
        if (entity.isAlive() && !entity.isInvulnerable() && entity != livingentity) {
            if (livingentity == null) {
                entity.hurt(DamageSource.MAGIC, 2.0F);
            } else {
                if (livingentity.isAlliedTo(entity)) {
                    return;
                }

                entity.hurt(DamageSource.indirectMagic(this, livingentity), 2.0F);
            }
            if (owner.enraged)
            {
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 2));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0));
            }
        }
    }
}
