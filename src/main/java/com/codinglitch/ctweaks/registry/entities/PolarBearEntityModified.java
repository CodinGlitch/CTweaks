package com.codinglitch.ctweaks.registry.entities;

import com.codinglitch.ctweaks.util.UtilityC;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public class PolarBearEntityModified extends PolarBear implements PlayerRideableJumping {
    protected int ticksUntilAttack = 0;
    protected float dirX = 0;
    protected float dirZ = 0;

    public PolarBearEntityModified(EntityType<? extends PolarBear> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return ItemTags.FISHES.contains(stack.getItem());
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverWorld, AgeableMob entity) {
        AgeableMob child = super.getBreedOffspring(serverWorld, entity);
        child.setBaby(true);
        return child;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        boolean flag = this.isAngryAt(player);
        if (!this.isBaby() & !flag & !this.level.isClientSide) {
            this.doPlayerRide(player);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    public void doPlayerRide(Player player)
    {
        player.startRiding(this);
    }

    @Override
    public boolean canBeRiddenInWater(Entity rider) {
        return true;
    }

    @Override
    public void travel(Vec3 vector) {
        if (this.isAlive()) {
            if (this.isVehicle()) {
                LivingEntity livingentity = (LivingEntity)this.getControllingPassenger();
                float yRot = livingentity.getYRot();
                this.yRotO = this.getYRot();
                float xRot = livingentity.getXRot() * 0.5F;
                this.setRot(yRot, xRot);
                this.yBodyRot = UtilityC.interpolate(this.yBodyRot,this.getYRot(),0.1f);
                this.yHeadRot = this.yBodyRot;
                float x = livingentity.xxa * 0.15F;
                float z = livingentity.zza * 0.25F * (livingentity.isSprinting() ? 1.4f : 1f);

                dirX = x > 0 ? x : dirX;
                dirZ = z > 0 ? z : dirZ;

                if (this.isControlledByLocalInstance()) {

                    this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
                    super.travel(new Vec3(x, vector.y, z));
                    if (isUnderWater())
                    {
                        this.setDeltaMovement(this.getDeltaMovement().add(0, 0.05F, 0));
                    }
                } else if (livingentity instanceof Player) {
                    this.setDeltaMovement(Vec3.ZERO);
                }

                this.calculateEntityAnimation(this, false);
            } else {
                this.flyingSpeed = 0.02F;
                super.travel(vector);
            }
        }
    }

    public void positionRider(Entity entity) {
        super.positionRider(entity);
        if (entity instanceof Mob) {
            Mob mobentity = (Mob)entity;
            this.yBodyRot = mobentity.yBodyRot;
        }

        if (isStanding()) {
            float num = 1-((float)ticksUntilAttack/100);

            float f3 = Mth.sin(this.yBodyRot * ((float)Math.PI / 180F));
            float f = Mth.cos(this.yBodyRot * ((float)Math.PI / 180F));
            float f1 = 0.7F * num;
            float f2 = 0.15F * num;
            entity.setPos(this.getX() + (double)(f1 * f3), this.getY() + this.getPassengersRidingOffset() + entity.getMyRidingOffset() + (double)f2, this.getZ() - (double)(f1 * f));
            if (entity instanceof LivingEntity) {
                ((LivingEntity)entity).yBodyRot = this.yBodyRot;
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    public void onPlayerJump(int scale) {
        this.setDeltaMovement(this.getDeltaMovement().add(0, 0.0065F*scale, 0));
    }

    @Override
    public boolean canJump() {
        return ticksUntilAttack < 1;
    }

    @Override
    public void handleStartJump(int scale) {
        if (this.isVehicle() & getControllingPassenger() instanceof Player) {
            ticksUntilAttack = 100;
            this.playWarningSound();
            this.setStanding(true);

            AABB aabb = getBoundingBox().inflate(0f).expandTowards(dirX*-20,0,dirZ*-20);
            for (LivingEntity entity : level.getNearbyEntities(LivingEntity.class, TargetingConditions.DEFAULT, this,  aabb))
            {
                if (entity == this | entity == getControllingPassenger()) continue;
                AABB entityBoundingBox = entity.getBoundingBox();
                if (entityBoundingBox == null) continue;

                if (entityBoundingBox.intersects(aabb))
                {
                    float damage = (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    entity.hurt(DamageSource.playerAttack((Player) this.getControllingPassenger()), damage);
                }
            }
        }
    }

    @Override
    public void handleStopJump() {
    }

    @Override
    @Nullable
    public Entity getControllingPassenger() {
        return this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
    }

    @Override
    public boolean canBeControlledByRider() {
        return this.getControllingPassenger() instanceof LivingEntity;
    }

    @Override
    public void tick() {
        super.tick();

        if (ticksUntilAttack > 0)
        {
            ticksUntilAttack--;
            if (ticksUntilAttack < 80)
            {
                this.setStanding(false);
            }
        }
    }
}
