package com.codinglitch.ctweaks.registry.entities;

import com.codinglitch.ctweaks.CTweaks;
import com.codinglitch.ctweaks.util.ReferenceC;
import com.codinglitch.ctweaks.util.SoundsC;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.client.renderer.entity.GiantZombieRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FoxEntityModified extends FoxEntityCopy {
    public static final Attribute MAX_HEALTH = new RangedAttribute(ReferenceC.MODID + "fox_health", 0, 0, 20);

    public FoxEntityModified(EntityType<? extends FoxEntityCopy> entityType, Level world) {
        super(entityType, world);
        this.reassessTameGoals();
    }

    public DyeColor getCollarColor() {
        return DyeColor.RED;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(7, new SleepGoal());
        this.goalSelector.addGoal(13, new GiftOwnerGoal());
    }

    public class SleepGoal extends FoxEntityCopy.SleepGoal
    {
        @Override
        public void stop() {
            super.stop();
            if (!FoxEntityModified.this.isOrderedToSit())
            {
                FoxEntityModified.this.setSitting(FoxEntityModified.this.isOrderedToSit());
            }
        }
    }

    public class GiftOwnerGoal extends Goal
    {
        private int timeToRecalcPath;
        private float oldWaterCost;

        public GiftOwnerGoal()
        {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !FoxEntityModified.this.getMainHandItem().isEmpty() &
                    FoxEntityModified.this.isTame() &
                    random.nextInt(100)==0 &
                    !FoxEntityModified.this.isSleeping() &
                    !FoxEntityModified.this.isOrderedToSit() &
                    FoxEntityModified.this.ticksSinceGifted > 800 &
                    !isImportant();
        }

        @Override
        public boolean canContinueToUse() {
            if (FoxEntityModified.this.navigation.isDone()) {
                return false;
            } else if (FoxEntityModified.this.isOrderedToSit()) {
                return false;
            } else {
                return !(FoxEntityModified.this.distanceToSqr(FoxEntityModified.this.getOwner()) <= 1);
            }
        }

        @Override
        public void start() {
            this.timeToRecalcPath = 0;
            ticksSinceGifted = 0;
            this.oldWaterCost = FoxEntityModified.this.getPathfindingMalus(BlockPathTypes.WATER);
            FoxEntityModified.this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        }

        @Override
        public void stop() {
            FoxEntityModified.this.navigation.stop();
            FoxEntityModified.this.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);

            spitOutItem(getItemBySlot(EquipmentSlot.MAINHAND));
            setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            setImportant(false);


            double d0 = random.nextGaussian() * 0.02D;
            double d1 = random.nextGaussian() * 0.02D;
            double d2 = random.nextGaussian() * 0.02D;
            ((ServerLevel) level).sendParticles(ParticleTypes.HEART, getRandomX(1.0D), getRandomY() + 0.5D, getRandomZ(1.0D), 1, d0, d1, d2, 5f);
            playSound(SoundsC.fox_squeak.get(), 1.0F, 1.3F);
        }

        @Override
        public void tick() {
            FoxEntityModified.this.getLookControl().setLookAt(FoxEntityModified.this.getOwner(), 10.0F, (float)FoxEntityModified.this.getMaxHeadXRot());
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                if (!FoxEntityModified.this.isLeashed() && !FoxEntityModified.this.isPassenger()) {
                    if (FoxEntityModified.this.distanceToSqr(FoxEntityModified.this.getOwner()) >= 144.0D) {
                        this.teleportToOwner();
                    } else {
                        FoxEntityModified.this.navigation.moveTo(FoxEntityModified.this.getOwner(), 1);
                    }

                }
            }
        }

        private void teleportToOwner() {
            BlockPos blockpos = FoxEntityModified.this.getOwner().blockPosition();

            for(int i = 0; i < 10; ++i) {
                int j = this.randomIntInclusive(-3, 3);
                int k = this.randomIntInclusive(-1, 1);
                int l = this.randomIntInclusive(-3, 3);
                boolean flag = this.maybeTeleportTo(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
                if (flag) {
                    return;
                }
            }
        }

        private int randomIntInclusive(int from, int to) {
            return random.nextInt(to - from + 1) + from;
        }

        private boolean maybeTeleportTo(int x, int y, int z) {
            if (Math.abs((double)x - FoxEntityModified.this.getOwner().getX()) < 2.0D && Math.abs((double)z - FoxEntityModified.this.getOwner().getZ()) < 2.0D) {
                return false;
            } else if (!this.canTeleportTo(new BlockPos(x, y, z))) {
                return false;
            } else {
                FoxEntityModified.this.moveTo((double)x + 0.5D, (double)y, (double)z + 0.5D, FoxEntityModified.this.yRot, FoxEntityModified.this.xRot);
                FoxEntityModified.this.navigation.stop();
                return true;
            }
        }

        private boolean canTeleportTo(BlockPos pos) {
            BlockPathTypes pathnodetype = WalkNodeEvaluator.getBlockPathTypeStatic(FoxEntityModified.this.level, pos.mutable());
            if (pathnodetype != BlockPathTypes.WALKABLE) {
                return false;
            } else {
                BlockState blockstate = FoxEntityModified.this.level.getBlockState(pos.below());
                if (blockstate.getBlock() instanceof LeavesBlock) {
                    return false;
                } else {
                    BlockPos blockpos = pos.subtract(FoxEntityModified.this.blockPosition());
                    return FoxEntityModified.this.level.noCollision(FoxEntityModified.this, FoxEntityModified.this.getBoundingBox().move(blockpos));
                }
            }
        }
    }

    @Override
    public boolean canMate(Animal animalEntity) {
        if (animalEntity == this) {
            return false;
        } else if (!this.isTame()) {
            return false;
        } else if (!(animalEntity instanceof FoxEntityCopy)) {
            return false;
        } else {
            FoxEntityCopy foxEntity = (FoxEntityCopy)animalEntity;
            if (!foxEntity.isTame()) {
                return false;
            } else if (foxEntity.isInSittingPose()) {
                return false;
            } else {
                return this.isInLove() && foxEntity.isInLove();
            }
        }
    }

    @Override
    public boolean canFallInLove() {
        return isTame() & !isInLove();
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        super.pickUpItem(itemEntity);
        if (!this.isImportant()) {
            if (itemEntity.getThrower() != null) {
                Player player = level.getPlayerByUUID(itemEntity.getThrower());
                if (player != null) {
                    if (player == getOwner()) {
                        this.setImportant(true);
                        return;
                    }
                }
            }
            this.setImportant(false);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        Item item = itemstack.getItem();
        InteractionResult actionresulttype = super.mobInteract(player, hand);
        if (player.isCrouching())
        {
            this.spitOutItem(this.getItemBySlot(EquipmentSlot.MAINHAND));
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.setImportant(false);
        }
        else
        {
            if (this.level.isClientSide) {
                if (player.getMainHandItem().isEmpty() & this.isOwnedBy(player))
                {
                    double d0 = this.random.nextGaussian() * 0.02D;
                    double d1 = this.random.nextGaussian() * 0.02D;
                    double d2 = this.random.nextGaussian() * 0.02D;
                    this.level.addParticle(ParticleTypes.HEART, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
                }

                boolean flag = this.isOwnedBy(player) || this.isTame() || item == Items.SWEET_BERRIES && !this.isTame();
                return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
            } else {
                if (this.isTame()) {
                    if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                        if (!player.abilities.instabuild) {
                            itemstack.shrink(1);
                        }

                        this.heal((float)item.getFoodProperties().getNutrition());
                        return InteractionResult.SUCCESS;
                    }

                    if (player.getMainHandItem().isEmpty() & this.isOwnedBy(player))
                    {
                        this.playSound(SoundsC.fox_squeak.get(), 1.0F, 1.3F);
                        if (this.isSleeping())
                        {
                            this.wakeUp();
                        }
                    }
                    else
                    {
                        if ((!actionresulttype.consumesAction() || this.isBaby()) && this.isOwnedBy(player)) {
                            this.setOrderedToSit(!this.isOrderedToSit());
                            this.setSitting(this.isOrderedToSit());

                            this.jumping = false;
                            this.navigation.stop();
                            this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
                            this.setTarget((LivingEntity)null);
                            return InteractionResult.SUCCESS;
                        }
                    }
                } else if (item == Items.SWEET_BERRIES) {
                    if (!player.abilities.instabuild) {
                        itemstack.shrink(1);
                    }

                    if (this.random.nextInt(20) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
                        this.tame(player);
                        this.navigation.stop();
                        this.setTarget((LivingEntity)null);
                        this.setOrderedToSit(false);
                        this.level.broadcastEntityEvent(this, (byte)7);
                    } else {
                        this.level.broadcastEntityEvent(this, (byte)6);
                    }

                    return InteractionResult.SUCCESS;
                }
                return super.mobInteract(player, hand);
            }
        }
        return actionresulttype;
    }
}
