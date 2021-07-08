package com.codinglitch.ctweaks.registry.entities;

import com.codinglitch.ctweaks.CTweaks;
import com.codinglitch.ctweaks.util.ReferenceC;
import com.codinglitch.ctweaks.util.SoundsC;
import com.codinglitch.ctweaks.util.UtilityC;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import net.minecraft.block.AnvilBlock;
import net.minecraft.client.gui.screen.inventory.AnvilScreen;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.ai.brain.task.VillagerTasks;
import net.minecraft.entity.ai.goal.BreedGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class PolarBearEntityModified extends PolarBearEntity implements IJumpingMount {
    protected int ticksUntilAttack = 0;
    protected float dirX = 0;
    protected float dirZ = 0;

    public PolarBearEntityModified(EntityType<? extends PolarBearEntity> p_i50249_1_, World p_i50249_2_) {
        super(p_i50249_1_, p_i50249_2_);
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
    public AgeableEntity getBreedOffspring(ServerWorld serverWorld, AgeableEntity entity) {
        AgeableEntity child = super.getBreedOffspring(serverWorld, entity);
        child.setBaby(true);
        return child;
    }

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        if (!this.isBaby() & !this.isAngry()) {
            this.doPlayerRide(player);
            return ActionResultType.sidedSuccess(this.level.isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    public void doPlayerRide(PlayerEntity player)
    {
        player.startRiding(this);
    }

    @Override
    public boolean canBeRiddenInWater(Entity rider) {
        return true;
    }

    @Override
    public void travel(Vector3d vector) {
        if (this.isAlive()) {
            if (this.isVehicle()) {
                LivingEntity livingentity = (LivingEntity)this.getControllingPassenger();
                this.yRot = livingentity.yRot;
                this.yRotO = this.yRot;
                this.xRot = livingentity.xRot * 0.5F;
                this.setRot(this.yRot, this.xRot);
                this.yBodyRot = UtilityC.interpolate(this.yBodyRot,this.yRot,0.1f);
                this.yHeadRot = this.yBodyRot;
                float x = livingentity.xxa * 0.15F;
                float z = livingentity.zza * 0.25F * (livingentity.isSprinting() ? 1.4f : 1f);

                dirX = x > 0 ? x : dirX;
                dirZ = z > 0 ? z : dirZ;

                if (this.isControlledByLocalInstance()) {

                    this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
                    super.travel(new Vector3d(x, vector.y, z));
                    if (isUnderWater())
                    {
                        this.setDeltaMovement(this.getDeltaMovement().add(0, 0.05F, 0));
                    }
                } else if (livingentity instanceof PlayerEntity) {
                    this.setDeltaMovement(Vector3d.ZERO);
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
        if (entity instanceof MobEntity) {
            MobEntity mobentity = (MobEntity)entity;
            this.yBodyRot = mobentity.yBodyRot;
        }

        if (isStanding()) {
            float num = 1-((float)ticksUntilAttack/100);

            float f3 = MathHelper.sin(this.yBodyRot * ((float)Math.PI / 180F));
            float f = MathHelper.cos(this.yBodyRot * ((float)Math.PI / 180F));
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
        if (this.isVehicle() & getControllingPassenger() instanceof PlayerEntity) {
            ticksUntilAttack = 100;
            this.playWarningSound();
            this.setStanding(true);

            AxisAlignedBB aabb = getBoundingBox().inflate(0f).expandTowards(dirX*-20,0,dirZ*-20);
            for (LivingEntity entity : level.getNearbyEntities(LivingEntity.class, EntityPredicate.DEFAULT, this,  aabb))
            {
                if (entity == this | entity == getControllingPassenger()) continue;
                AxisAlignedBB entityBoundingBox = entity.getBoundingBox();
                if (entityBoundingBox == null) continue;

                if (entityBoundingBox.intersects(aabb))
                {
                    float damage = (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    entity.hurt(DamageSource.playerAttack((PlayerEntity) this.getControllingPassenger()), damage);
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
