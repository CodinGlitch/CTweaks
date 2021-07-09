package com.codinglitch.ctweaks.registry.entities;

import com.codinglitch.ctweaks.CTweaks;
import com.codinglitch.ctweaks.util.ReferenceC;
import com.codinglitch.ctweaks.util.SoundsC;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.renderer.entity.GiantZombieRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

public class FoxEntityModified extends FoxEntityCopy {
    public static final Attribute MAX_HEALTH = new RangedAttribute(ReferenceC.MODID + "fox_health", 0, 0, 20);

    public FoxEntityModified(EntityType<? extends FoxEntityCopy> entityType, World world) {
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
            this.oldWaterCost = FoxEntityModified.this.getPathfindingMalus(PathNodeType.WATER);
            FoxEntityModified.this.setPathfindingMalus(PathNodeType.WATER, 0.0F);
            CTweaks.logger.info("start");
        }

        @Override
        public void stop() {
            FoxEntityModified.this.navigation.stop();
            FoxEntityModified.this.setPathfindingMalus(PathNodeType.WATER, this.oldWaterCost);

            spitOutItem(getItemBySlot(EquipmentSlotType.MAINHAND));
            setItemSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
            setImportant(false);


            double d0 = random.nextGaussian() * 0.02D;
            double d1 = random.nextGaussian() * 0.02D;
            double d2 = random.nextGaussian() * 0.02D;
            ((ServerWorld) level).sendParticles(ParticleTypes.HEART, getRandomX(1.0D), getRandomY() + 0.5D, getRandomZ(1.0D), 1, d0, d1, d2, 5f);
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
            PathNodeType pathnodetype = WalkNodeProcessor.getBlockPathTypeStatic(FoxEntityModified.this.level, pos.mutable());
            if (pathnodetype != PathNodeType.WALKABLE) {
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
    public boolean canMate(AnimalEntity animalEntity) {
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
                PlayerEntity player = level.getPlayerByUUID(itemEntity.getThrower());
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
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        Item item = itemstack.getItem();
        ActionResultType actionresulttype = super.mobInteract(player, hand);
        if (player.isCrouching())
        {
            this.spitOutItem(this.getItemBySlot(EquipmentSlotType.MAINHAND));
            this.setItemSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
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
                return flag ? ActionResultType.CONSUME : ActionResultType.PASS;
            } else {
                if (this.isTame()) {
                    if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                        if (!player.abilities.instabuild) {
                            itemstack.shrink(1);
                        }

                        this.heal((float)item.getFoodProperties().getNutrition());
                        return ActionResultType.SUCCESS;
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
                            return ActionResultType.SUCCESS;
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

                    return ActionResultType.SUCCESS;
                }
                return super.mobInteract(player, hand);
            }
        }
        return actionresulttype;
    }
}
