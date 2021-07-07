package com.codinglitch.ctweaks.registry.entities;

import com.codinglitch.ctweaks.CTweaks;
import com.codinglitch.ctweaks.registry.init.EntityInit;
import com.google.common.collect.Lists;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.LookController;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.entity.passive.fish.AbstractGroupFishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FoxEntityCopy extends TameableEntity {
    private static final DataParameter<Integer> DATA_TYPE_ID = EntityDataManager.defineId(FoxEntityCopy.class, DataSerializers.INT);
    private static final DataParameter<Byte> DATA_FLAGS_ID = EntityDataManager.defineId(FoxEntityCopy.class, DataSerializers.BYTE);
    private static final DataParameter<Optional<UUID>> DATA_TRUSTED_ID_0 = EntityDataManager.defineId(FoxEntityCopy.class, DataSerializers.OPTIONAL_UUID);
    private static final DataParameter<Optional<UUID>> DATA_TRUSTED_ID_1 = EntityDataManager.defineId(FoxEntityCopy.class, DataSerializers.OPTIONAL_UUID);
    private static final Predicate<ItemEntity> ALLOWED_ITEMS = (p_213489_0_) -> {
        return !p_213489_0_.hasPickUpDelay() && p_213489_0_.isAlive();
    };
    private static final Predicate<Entity> TRUSTED_TARGET_SELECTOR = (p_213470_0_) -> {
        if (!(p_213470_0_ instanceof LivingEntity)) {
            return false;
        } else {
            LivingEntity livingentity = (LivingEntity)p_213470_0_;
            return livingentity.getLastHurtMob() != null && livingentity.getLastHurtMobTimestamp() < livingentity.tickCount + 600;
        }
    };
    private static final Predicate<Entity> STALKABLE_PREY = (p_213498_0_) -> {
        return p_213498_0_ instanceof ChickenEntity || p_213498_0_ instanceof RabbitEntity;
    };
    private static final Predicate<Entity> AVOID_PLAYERS = (p_213463_0_) -> {
        return !p_213463_0_.isDiscrete() && EntityPredicates.NO_CREATIVE_OR_SPECTATOR.test(p_213463_0_);
    };
    private Goal landTargetGoal;
    private Goal fishTargetGoal;
    private float interestedAngle;
    private float interestedAngleO;
    private float crouchAmount;
    private float crouchAmountO;
    private int ticksSinceEaten;
    private boolean isImportant;

    private LivingEntity ignoreStalk;

    public FoxEntityCopy(EntityType<? extends FoxEntityCopy> p_i50271_1_, World p_i50271_2_) {
        super(p_i50271_1_, p_i50271_2_);
        this.lookControl = new LookHelperController();
        this.moveControl = new MoveHelperController();
        this.setPathfindingMalus(PathNodeType.DANGER_OTHER, 0.0F);
        this.setPathfindingMalus(PathNodeType.DAMAGE_OTHER, 0.0F);
        this.setCanPickUpLoot(true);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TRUSTED_ID_0, Optional.empty());
        this.entityData.define(DATA_TRUSTED_ID_1, Optional.empty());
        this.entityData.define(DATA_TYPE_ID, 0);
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
    }

    protected void registerGoals() {
        this.landTargetGoal = new NearestAttackableTargetGoal<>(this, AnimalEntity.class, 10, false, false, (entity) -> {
            return entity instanceof ChickenEntity || entity instanceof RabbitEntity;
        });
        this.fishTargetGoal = new NearestAttackableTargetGoal<>(this, AbstractFishEntity.class, 20, false, false, (entity) -> {
            return entity instanceof AbstractGroupFishEntity;
        });
        this.goalSelector.addGoal(0, new SwimGoal());
        this.goalSelector.addGoal(1, new JumpGoal());
        this.goalSelector.addGoal(2, new PanicGoal(2.2D));
        this.goalSelector.addGoal(3, new MateGoal(1.0D));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
        this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, PlayerEntity.class, 16.0F, 1.6D, 1.4D, (p_213497_1_) -> {
            return AVOID_PLAYERS.test(p_213497_1_) && !this.trusts(p_213497_1_.getUUID()) && !this.isDefending();
        }));
        this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, WolfEntity.class, 8.0F, 1.6D, 1.4D, (p_213469_1_) -> {
            return !((WolfEntity)p_213469_1_).isTame() && !this.isDefending();
        }));
        this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, PolarBearEntity.class, 8.0F, 1.6D, 1.4D, (p_213493_1_) -> {
            return !this.isDefending();
        }));
        this.goalSelector.addGoal(5, new FollowTargetGoal());
        this.goalSelector.addGoal(6, new PounceGoal());
        this.goalSelector.addGoal(6, new FindShelterGoal(1.25D));
        this.goalSelector.addGoal(7, new BiteGoal((double)1.2F, true));
        this.goalSelector.addGoal(7, new SleepGoal());
        this.goalSelector.addGoal(8, new FollowGoal(this, 1.25D));
        this.goalSelector.addGoal(9, new StrollGoal(32, 200));
        this.goalSelector.addGoal(10, new EatBerriesGoal((double)1.2F, 12, 2));
        this.goalSelector.addGoal(10, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(11, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(11, new FindItemsGoal());
        this.goalSelector.addGoal(12, new WatchGoal(this, PlayerEntity.class, 24.0F));
        this.targetSelector.addGoal(3, new RevengeGoal(LivingEntity.class, false, false, (entity) -> {
            return TRUSTED_TARGET_SELECTOR.test(entity) && !this.trusts(entity.getUUID());
        }));
        this.targetSelector.addGoal(2, new OwnerHurtMobGoal(this));
    }

    public SoundEvent getEatingSound(ItemStack p_213353_1_) {
        return SoundEvents.FOX_EAT;
    }

    public void aiStep() {
        if (!this.level.isClientSide && this.isAlive() && this.isEffectiveAi()) {
            ++this.ticksSinceEaten;
            ItemStack itemstack = this.getItemBySlot(EquipmentSlotType.MAINHAND);
            if (this.canEat(itemstack)) {
                if (this.ticksSinceEaten > 600) {
                    ItemStack itemstack1 = itemstack.finishUsingItem(this.level, this);
                    if (!itemstack1.isEmpty()) {
                        this.setItemSlot(EquipmentSlotType.MAINHAND, itemstack1);
                    }

                    this.ticksSinceEaten = 0;
                } else if (this.ticksSinceEaten > 560 && this.random.nextFloat() < 0.1F) {
                    this.playSound(this.getEatingSound(itemstack), 1.0F, 1.0F);
                    this.level.broadcastEntityEvent(this, (byte)45);
                }
            }

            LivingEntity livingentity = this.getTarget();
            if (livingentity == null || !livingentity.isAlive()) {
                this.setIsCrouching(false);
                this.setIsInterested(false);
            }
        }

        if (this.isSleeping() || this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
        }

        super.aiStep();
        if (this.isDefending() && this.random.nextFloat() < 0.05F) {
            this.playSound(SoundEvents.FOX_AGGRO, 1.0F, 1.0F);
        }

    }

    protected boolean isImmobile() {
        return this.isDeadOrDying();
    }

    private boolean canEat(ItemStack p_213464_1_) {
        return p_213464_1_.getItem().isEdible() && this.getTarget() == null && this.onGround && !this.isSleeping();
    }

    protected void populateDefaultEquipmentSlots(DifficultyInstance p_180481_1_) {
        if (this.random.nextFloat() < 0.2F) {
            float f = this.random.nextFloat();
            ItemStack itemstack;
            if (f < 0.05F) {
                itemstack = new ItemStack(Items.EMERALD);
            } else if (f < 0.2F) {
                itemstack = new ItemStack(Items.EGG);
            } else if (f < 0.4F) {
                itemstack = this.random.nextBoolean() ? new ItemStack(Items.RABBIT_FOOT) : new ItemStack(Items.RABBIT_HIDE);
            } else if (f < 0.6F) {
                itemstack = new ItemStack(Items.WHEAT);
            } else if (f < 0.8F) {
                itemstack = new ItemStack(Items.LEATHER);
            } else {
                itemstack = new ItemStack(Items.FEATHER);
            }

            this.setItemSlot(EquipmentSlotType.MAINHAND, itemstack);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public void handleEntityEvent(byte p_70103_1_) {
        if (p_70103_1_ == 45) {
            ItemStack itemstack = this.getItemBySlot(EquipmentSlotType.MAINHAND);
            if (!itemstack.isEmpty()) {
                for(int i = 0; i < 8; ++i) {
                    Vector3d vector3d = (new Vector3d(((double)this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D)).xRot(-this.xRot * ((float)Math.PI / 180F)).yRot(-this.yRot * ((float)Math.PI / 180F));
                    this.level.addParticle(new ItemParticleData(ParticleTypes.ITEM, itemstack), this.getX() + this.getLookAngle().x / 2.0D, this.getY(), this.getZ() + this.getLookAngle().z / 2.0D, vector3d.x, vector3d.y + 0.05D, vector3d.z);
                }
            }
        } else {
            super.handleEntityEvent(p_70103_1_);
        }

    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MobEntity.createMobAttributes().add(Attributes.MOVEMENT_SPEED, (double)0.3F).add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.FOLLOW_RANGE, 32.0D).add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    public FoxEntityCopy getBreedOffspring(ServerWorld world, AgeableEntity entity) {
        FoxEntityCopy foxentity = EntityInit.FOX_MODIFIED.get().create(world);
        foxentity.setFoxType(this.random.nextBoolean() ? this.getFoxType() : ((FoxEntityCopy)entity).getFoxType());
        return foxentity;
    }

    @Nullable
    public ILivingEntityData finalizeSpawn(IServerWorld p_213386_1_, DifficultyInstance p_213386_2_, SpawnReason p_213386_3_, @Nullable ILivingEntityData p_213386_4_, @Nullable CompoundNBT p_213386_5_) {
        Optional<RegistryKey<Biome>> optional = p_213386_1_.getBiomeName(this.blockPosition());
        Type foxentity$type = Type.byBiome(optional);
        boolean flag = false;
        if (p_213386_4_ instanceof FoxData) {
            foxentity$type = ((FoxData)p_213386_4_).type;
            if (((FoxData)p_213386_4_).getGroupSize() >= 2) {
                flag = true;
            }
        } else {
            p_213386_4_ = new FoxData(foxentity$type);
        }

        this.setFoxType(foxentity$type);
        if (flag) {
            this.setAge(-24000);
        }

        if (p_213386_1_ instanceof ServerWorld) {
            this.setTargetGoals();
        }

        this.populateDefaultEquipmentSlots(p_213386_2_);
        return super.finalizeSpawn(p_213386_1_, p_213386_2_, p_213386_3_, p_213386_4_, p_213386_5_);
    }

    private void setTargetGoals() {
        if (this.getFoxType() == Type.RED) {
            this.targetSelector.addGoal(4, this.landTargetGoal);
            this.targetSelector.addGoal(6, this.fishTargetGoal);
        } else {
            this.targetSelector.addGoal(4, this.fishTargetGoal);
            this.targetSelector.addGoal(6, this.landTargetGoal);
        }

    }

    protected void usePlayerItem(PlayerEntity p_175505_1_, ItemStack p_175505_2_) {
        if (this.isFood(p_175505_2_)) {
            this.playSound(this.getEatingSound(p_175505_2_), 1.0F, 1.0F);
        }

        super.usePlayerItem(p_175505_1_, p_175505_2_);
    }

    protected float getStandingEyeHeight(Pose p_213348_1_, EntitySize p_213348_2_) {
        return this.isBaby() ? p_213348_2_.height * 0.85F : 0.4F;
    }

    public Type getFoxType() {
        return Type.byId(this.entityData.get(DATA_TYPE_ID));
    }

    private void setFoxType(Type p_213474_1_) {
        this.entityData.set(DATA_TYPE_ID, p_213474_1_.getId());
    }

    private List<UUID> getTrustedUUIDs() {
        List<UUID> list = Lists.newArrayList();
        list.add(this.entityData.get(DATA_TRUSTED_ID_0).orElse((UUID)null));
        list.add(this.entityData.get(DATA_TRUSTED_ID_1).orElse((UUID)null));
        return list;
    }

    private void addTrustedUUID(@Nullable UUID p_213465_1_) {
        if (this.entityData.get(DATA_TRUSTED_ID_0).isPresent()) {
            this.entityData.set(DATA_TRUSTED_ID_1, Optional.ofNullable(p_213465_1_));
        } else {
            this.entityData.set(DATA_TRUSTED_ID_0, Optional.ofNullable(p_213465_1_));
        }

    }

    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        List<UUID> list = this.getTrustedUUIDs();
        ListNBT listnbt = new ListNBT();

        for(UUID uuid : list) {
            if (uuid != null) {
                listnbt.add(NBTUtil.createUUID(uuid));
            }
        }

        compound.put("Trusted", listnbt);
        compound.putBoolean("Sleeping", this.isSleeping());
        compound.putString("Type", this.getFoxType().getName());
        compound.putBoolean("Sitting", this.isSitting());
        compound.putBoolean("Crouching", this.isCrouching());
        compound.putBoolean("Important", this.isImportant);
    }

    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        ListNBT listnbt = compound.getList("Trusted", 11);

        for(int i = 0; i < listnbt.size(); ++i) {
            this.addTrustedUUID(NBTUtil.loadUUID(listnbt.get(i)));
        }

        this.setSleeping(compound.getBoolean("Sleeping"));
        this.setFoxType(Type.byName(compound.getString("Type")));
        this.setSitting(compound.getBoolean("Sitting"));
        this.setIsCrouching(compound.getBoolean("Crouching"));
        this.isImportant = compound.getBoolean("Important");
        if (this.level instanceof ServerWorld) {
            this.setTargetGoals();
        }

    }

    public boolean isSitting() {
        return this.getFlag(1);
    }

    public void setSitting(boolean sitting) {
        this.setFlag(1, sitting);
    }

    public boolean isImportant()
    {
        return isImportant;
    }

    public void setImportant(boolean important)
    {
        this.isImportant = important;
    }

    public boolean isFaceplanted() {
        return this.getFlag(64);
    }

    private void setFaceplanted(boolean faceplanted) {
        this.setFlag(64, faceplanted);
    }

    private boolean isDefending() {
        return this.getFlag(128);
    }

    private void setDefending(boolean isDefending) {
        this.setFlag(128, isDefending);
    }

    public boolean isSleeping() {
        return this.getFlag(32);
    }

    private void setSleeping(boolean isSleeping) {
        this.setFlag(32, isSleeping);
    }

    private void setFlag(int flag, boolean value) {
        if (value) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) | flag));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) & ~flag));
        }

    }

    private boolean getFlag(int flag) {
        return (this.entityData.get(DATA_FLAGS_ID) & flag) != 0;
    }

    public boolean canTakeItem(ItemStack item) {
        EquipmentSlotType equipmentslottype = MobEntity.getEquipmentSlotForItem(item);
        if (!this.getItemBySlot(equipmentslottype).isEmpty()) {
            return false;
        } else {
            return equipmentslottype == EquipmentSlotType.MAINHAND && super.canTakeItem(item);
        }
    }

    public boolean canHoldItem(ItemStack itemStack) {
        Item item = itemStack.getItem();
        ItemStack holding = this.getItemBySlot(EquipmentSlotType.MAINHAND);
        return holding.isEmpty() || this.ticksSinceEaten > 0 && item.isEdible() && !holding.getItem().isEdible();
    }

    public void spitOutItem(ItemStack stack) {
        if (!stack.isEmpty() && !this.level.isClientSide) {
            ItemEntity itementity = new ItemEntity(this.level, this.getX() + this.getLookAngle().x, this.getY() + 1.0D, this.getZ() + this.getLookAngle().z, stack);
            itementity.setPickUpDelay(40);
            itementity.setThrower(this.getUUID());
            this.playSound(SoundEvents.FOX_SPIT, 1.0F, 1.0F);
            this.level.addFreshEntity(itementity);
        }
        if (!stack.isEmpty())
        {
            setImportant(false);
        }
    }

    private void dropItemStack(ItemStack stack) {
        ItemEntity itementity = new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), stack);
        this.level.addFreshEntity(itementity);
    }

    protected void pickUpItem(ItemEntity itemEntity) {
        if (isImportant) return;
        ItemStack itemstack = itemEntity.getItem();
        if (this.canHoldItem(itemstack)) {
            int i = itemstack.getCount();
            if (i > 1) {
                this.dropItemStack(itemstack.split(i - 1));
            }

            this.spitOutItem(this.getItemBySlot(EquipmentSlotType.MAINHAND));
            this.onItemPickup(itemEntity);
            this.setItemSlot(EquipmentSlotType.MAINHAND, itemstack.split(1));
            this.handDropChances[EquipmentSlotType.MAINHAND.getIndex()] = 2.0F;
            this.take(itemEntity, itemstack.getCount());
            itemEntity.remove();
            this.ticksSinceEaten = 0;
        }
    }

    public void tick() {
        super.tick();
        if (this.isEffectiveAi()) {
            boolean flag = this.isInWater();
            if (flag || this.getTarget() != null || this.level.isThundering()) {
                this.wakeUp();
            }

            if (this.isFaceplanted() && this.level.random.nextFloat() < 0.2F) {
                BlockPos blockpos = this.blockPosition();
                BlockState blockstate = this.level.getBlockState(blockpos);
                this.level.levelEvent(2001, blockpos, Block.getId(blockstate));
            }
        }

        this.interestedAngleO = this.interestedAngle;
        if (this.isInterested()) {
            this.interestedAngle += (1.0F - this.interestedAngle) * 0.4F;
        } else {
            this.interestedAngle += (0.0F - this.interestedAngle) * 0.4F;
        }

        this.crouchAmountO = this.crouchAmount;
        if (this.isCrouching()) {
            this.crouchAmount += 0.2F;
            if (this.crouchAmount > 3.0F) {
                this.crouchAmount = 3.0F;
            }
        } else {
            this.crouchAmount = 0.0F;
        }

    }

    public boolean isFood(ItemStack p_70877_1_) {
        return p_70877_1_.getItem() == Items.SWEET_BERRIES;
    }

    protected void onOffspringSpawnedFromEgg(PlayerEntity p_213406_1_, MobEntity p_213406_2_) {
        ((FoxEntityCopy)p_213406_2_).addTrustedUUID(p_213406_1_.getUUID());
    }

    public boolean isPouncing() {
        return this.getFlag(16);
    }

    public void setIsPouncing(boolean p_213461_1_) {
        this.setFlag(16, p_213461_1_);
    }

    public boolean isFullyCrouched() {
        return this.crouchAmount == 3.0F;
    }

    public void setIsCrouching(boolean p_213451_1_) {
        this.setFlag(4, p_213451_1_);
    }

    public boolean isCrouching() {
        return this.getFlag(4);
    }

    public void setIsInterested(boolean p_213502_1_) {
        this.setFlag(8, p_213502_1_);
    }

    public boolean isInterested() {
        return this.getFlag(8);
    }

    @OnlyIn(Dist.CLIENT)
    public float getHeadRollAngle(float p_213475_1_) {
        return MathHelper.lerp(p_213475_1_, this.interestedAngleO, this.interestedAngle) * 0.11F * (float)Math.PI;
    }

    @OnlyIn(Dist.CLIENT)
    public float getCrouchAmount(float p_213503_1_) {
        return MathHelper.lerp(p_213503_1_, this.crouchAmountO, this.crouchAmount);
    }

    public void setTarget(@Nullable LivingEntity p_70624_1_) {
        if (this.isDefending() && p_70624_1_ == null) {
            this.setDefending(false);
        }

        super.setTarget(p_70624_1_);
    }

    protected int calculateFallDamage(float p_225508_1_, float p_225508_2_) {
        return MathHelper.ceil((p_225508_1_ - 5.0F) * p_225508_2_);
    }

    void wakeUp() {
        this.setSleeping(false);
    }

    private void clearStates() {
        if (this.isSitting()) return;
        this.setIsInterested(false);
        this.setIsCrouching(false);
        this.setSleeping(false);
        this.setDefending(false);
        this.setFaceplanted(false);
    }

    private boolean canMove() {
        return !this.isSleeping() && !this.isSitting() && !this.isFaceplanted();
    }

    public void playAmbientSound() {
        SoundEvent soundevent = this.getAmbientSound();
        if (soundevent == SoundEvents.FOX_SCREECH) {
            this.playSound(soundevent, 2.0F, this.getVoicePitch());
        } else {
            super.playAmbientSound();
        }

    }

    @Nullable
    protected SoundEvent getAmbientSound() {
        if (this.isSleeping()) {
            return SoundEvents.FOX_SLEEP;
        } else {
            if (!this.level.isDay() && this.random.nextFloat() < 0.1F) {
                List<PlayerEntity> list = this.level.getEntitiesOfClass(PlayerEntity.class, this.getBoundingBox().inflate(16.0D, 16.0D, 16.0D), EntityPredicates.NO_SPECTATORS);
                if (list.isEmpty()) {
                    return SoundEvents.FOX_SCREECH;
                }
            }

            return SoundEvents.FOX_AMBIENT;
        }
    }

    @Nullable
    protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
        return SoundEvents.FOX_HURT;
    }

    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.FOX_DEATH;
    }

    private boolean trusts(UUID p_213468_1_) {
        return this.getTrustedUUIDs().contains(p_213468_1_);
    }

    protected void dropAllDeathLoot(DamageSource p_213345_1_) {
        ItemStack itemstack = this.getItemBySlot(EquipmentSlotType.MAINHAND);
        if (!itemstack.isEmpty()) {
            this.spawnAtLocation(itemstack);
            this.setItemSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
        }

        super.dropAllDeathLoot(p_213345_1_);
    }

    public static boolean isPathClear(FoxEntityCopy p_213481_0_, LivingEntity p_213481_1_) {
        double d0 = p_213481_1_.getZ() - p_213481_0_.getZ();
        double d1 = p_213481_1_.getX() - p_213481_0_.getX();
        double d2 = d0 / d1;
        int i = 6;

        for(int j = 0; j < 6; ++j) {
            double d3 = d2 == 0.0D ? 0.0D : d0 * (double)((float)j / 6.0F);
            double d4 = d2 == 0.0D ? d1 * (double)((float)j / 6.0F) : d3 / d2;

            for(int k = 1; k < 4; ++k) {
                if (!p_213481_0_.level.getBlockState(new BlockPos(p_213481_0_.getX() + d4, p_213481_0_.getY() + (double)k, p_213481_0_.getZ() + d3)).getMaterial().isReplaceable()) {
                    return false;
                }
            }
        }

        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public Vector3d getLeashOffset() {
        return new Vector3d(0.0D, (double)(0.55F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
    }

    public class AlertablePredicate implements Predicate<LivingEntity> {
        public boolean test(LivingEntity p_test_1_) {
            if (p_test_1_ instanceof FoxEntityCopy) {
                return false;
            } else if (!(p_test_1_ instanceof ChickenEntity) && !(p_test_1_ instanceof RabbitEntity) && !(p_test_1_ instanceof MonsterEntity)) {
                if (p_test_1_ instanceof TameableEntity) {
                    return !((TameableEntity)p_test_1_).isTame();
                } else if (!(p_test_1_ instanceof PlayerEntity) || !p_test_1_.isSpectator() && !((PlayerEntity)p_test_1_).isCreative()) {
                    if (FoxEntityCopy.this.trusts(p_test_1_.getUUID())) {
                        return false;
                    } else {
                        return !p_test_1_.isSleeping() && !p_test_1_.isDiscrete();
                    }
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }
    }

    abstract class BaseGoal extends Goal {
        private final EntityPredicate alertableTargeting = (new EntityPredicate()).range(12.0D).allowUnseeable().selector(FoxEntityCopy.this.new AlertablePredicate());

        private BaseGoal() {
        }

        protected boolean hasShelter() {
            BlockPos blockpos = new BlockPos(FoxEntityCopy.this.getX(), FoxEntityCopy.this.getBoundingBox().maxY, FoxEntityCopy.this.getZ());
            return !FoxEntityCopy.this.level.canSeeSky(blockpos) && FoxEntityCopy.this.getWalkTargetValue(blockpos) >= 0.0F;
        }

        protected boolean alertable() {
            return !FoxEntityCopy.this.level.getNearbyEntities(LivingEntity.class, this.alertableTargeting, FoxEntityCopy.this, FoxEntityCopy.this.getBoundingBox().inflate(12.0D, 6.0D, 12.0D)).isEmpty();
        }
    }

    class BiteGoal extends MeleeAttackGoal {
        public BiteGoal(double p_i50731_2_, boolean p_i50731_4_) {
            super(FoxEntityCopy.this, p_i50731_2_, p_i50731_4_);
        }

        protected void checkAndPerformAttack(LivingEntity p_190102_1_, double p_190102_2_) {
            if (getOwner() != null)
            {
                if (getOwner().position().distanceTo(position()) > 15)
                {
                    stop();
                }
            }
            double d0 = this.getAttackReachSqr(p_190102_1_);
            if (p_190102_2_ <= d0 && this.isTimeToAttack()) {
                this.resetAttackCooldown();
                this.mob.doHurtTarget(p_190102_1_);
                FoxEntityCopy.this.playSound(SoundEvents.FOX_BITE, 1.0F, 1.0F);
            }

        }

        public void start() {
            FoxEntityCopy.this.setIsInterested(false);
            super.start();
        }

        public boolean canUse() {
            return !FoxEntityCopy.this.isSitting() && !FoxEntityCopy.this.isSleeping() && !FoxEntityCopy.this.isCrouching() && !FoxEntityCopy.this.isFaceplanted() && super.canUse();
        }
    }

    public class EatBerriesGoal extends MoveToBlockGoal {
        protected int ticksWaited;

        public EatBerriesGoal(double p_i50737_2_, int p_i50737_4_, int p_i50737_5_) {
            super(FoxEntityCopy.this, p_i50737_2_, p_i50737_4_, p_i50737_5_);
        }

        public double acceptedDistance() {
            return 2.0D;
        }

        public boolean shouldRecalculatePath() {
            return this.tryTicks % 100 == 0;
        }

        protected boolean isValidTarget(IWorldReader p_179488_1_, BlockPos p_179488_2_) {
            BlockState blockstate = p_179488_1_.getBlockState(p_179488_2_);
            return blockstate.is(Blocks.SWEET_BERRY_BUSH) && blockstate.getValue(SweetBerryBushBlock.AGE) >= 2;
        }

        public void tick() {
            if (this.isReachedTarget()) {
                if (this.ticksWaited >= 40) {
                    this.onReachedTarget();
                } else {
                    ++this.ticksWaited;
                }
            } else if (!this.isReachedTarget() && FoxEntityCopy.this.random.nextFloat() < 0.05F) {
                FoxEntityCopy.this.playSound(SoundEvents.FOX_SNIFF, 1.0F, 1.0F);
            }

            super.tick();
        }

        protected void onReachedTarget() {
            if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(FoxEntityCopy.this.level, FoxEntityCopy.this)) {
                BlockState blockstate = FoxEntityCopy.this.level.getBlockState(this.blockPos);
                if (blockstate.is(Blocks.SWEET_BERRY_BUSH)) {
                    int i = blockstate.getValue(SweetBerryBushBlock.AGE);
                    blockstate.setValue(SweetBerryBushBlock.AGE, Integer.valueOf(1));
                    int j = 1 + FoxEntityCopy.this.level.random.nextInt(2) + (i == 3 ? 1 : 0);
                    ItemStack itemstack = FoxEntityCopy.this.getItemBySlot(EquipmentSlotType.MAINHAND);
                    if (itemstack.isEmpty()) {
                        FoxEntityCopy.this.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.SWEET_BERRIES));
                        --j;
                    }

                    if (j > 0) {
                        Block.popResource(FoxEntityCopy.this.level, this.blockPos, new ItemStack(Items.SWEET_BERRIES, j));
                    }

                    FoxEntityCopy.this.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0F, 1.0F);
                    FoxEntityCopy.this.level.setBlock(this.blockPos, blockstate.setValue(SweetBerryBushBlock.AGE, Integer.valueOf(1)), 2);
                }
            }
        }

        public boolean canUse() {
            return !FoxEntityCopy.this.isSleeping() && super.canUse() && !FoxEntityCopy.this.isSitting();
        }

        public void start() {
            this.ticksWaited = 0;
            super.start();
        }
    }

    class FindItemsGoal extends Goal {
        public FindItemsGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            if (!FoxEntityCopy.this.getItemBySlot(EquipmentSlotType.MAINHAND).isEmpty()) {
                return false;
            } else if (FoxEntityCopy.this.getTarget() == null && FoxEntityCopy.this.getLastHurtByMob() == null) {
                if (!FoxEntityCopy.this.canMove()) {
                    return false;
                } else if (FoxEntityCopy.this.getRandom().nextInt(10) != 0) {
                    return false;
                } else {
                    List<ItemEntity> list = FoxEntityCopy.this.level.getEntitiesOfClass(ItemEntity.class, FoxEntityCopy.this.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), ALLOWED_ITEMS);
                    return !list.isEmpty() && FoxEntityCopy.this.getItemBySlot(EquipmentSlotType.MAINHAND).isEmpty();
                }
            } else {
                return false;
            }
        }

        public void tick() {
            List<ItemEntity> list = FoxEntityCopy.this.level.getEntitiesOfClass(ItemEntity.class, FoxEntityCopy.this.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), ALLOWED_ITEMS);
            ItemStack itemstack = FoxEntityCopy.this.getItemBySlot(EquipmentSlotType.MAINHAND);
            if (itemstack.isEmpty() && !list.isEmpty()) {
                FoxEntityCopy.this.getNavigation().moveTo(list.get(0), (double)1.2F);
            }

        }

        public void start() {
            List<ItemEntity> list = FoxEntityCopy.this.level.getEntitiesOfClass(ItemEntity.class, FoxEntityCopy.this.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), ALLOWED_ITEMS);
            if (!list.isEmpty()) {
                FoxEntityCopy.this.getNavigation().moveTo(list.get(0), (double)1.2F);
            }

        }
    }

    class FindShelterGoal extends FleeSunGoal {
        private int interval = 100;

        public FindShelterGoal(double p_i50724_2_) {
            super(FoxEntityCopy.this, p_i50724_2_);
        }

        public boolean canUse() {
            if (!FoxEntityCopy.this.isSleeping() && this.mob.getTarget() == null) {
                if (FoxEntityCopy.this.level.isThundering()) {
                    return true;
                } else if (this.interval > 0) {
                    --this.interval;
                    return false;
                } else {
                    this.interval = 100;
                    BlockPos blockpos = this.mob.blockPosition();
                    return FoxEntityCopy.this.level.isDay() && FoxEntityCopy.this.level.canSeeSky(blockpos) && !((ServerWorld) FoxEntityCopy.this.level).isVillage(blockpos) && this.setWantedPos();
                }
            } else {
                return false;
            }
        }

        public void start() {
            FoxEntityCopy.this.clearStates();
            super.start();
        }
    }

    class FollowGoal extends FollowParentGoal {
        private final FoxEntityCopy fox;

        public FollowGoal(FoxEntityCopy p_i50735_2_, double p_i50735_3_) {
            super(p_i50735_2_, p_i50735_3_);
            this.fox = p_i50735_2_;
        }

        public boolean canUse() {
            return !this.fox.isDefending() && super.canUse();
        }

        public boolean canContinueToUse() {
            return !this.fox.isDefending() && super.canContinueToUse();
        }

        public void start() {
            this.fox.clearStates();
            super.start();
        }
    }

    class FollowTargetGoal extends Goal {
        public FollowTargetGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean canUse() {
            if (FoxEntityCopy.this.isSitting()) return false;
            if (FoxEntityCopy.this.isSleeping()) {
                return false;
            } else {
                LivingEntity livingentity = FoxEntityCopy.this.getTarget();
                return livingentity != null && livingentity.isAlive() && (STALKABLE_PREY.test(livingentity) | livingentity.equals(ignoreStalk)) && FoxEntityCopy.this.distanceToSqr(livingentity) > 36.0D && !FoxEntityCopy.this.isCrouching() && !FoxEntityCopy.this.isInterested() && !FoxEntityCopy.this.jumping;
            }
        }

        public void start() {
            FoxEntityCopy.this.setFaceplanted(false);
        }

        public void stop() {
            LivingEntity livingentity = FoxEntityCopy.this.getTarget();
            if (livingentity != null && isPathClear(FoxEntityCopy.this, livingentity)) {
                FoxEntityCopy.this.setIsInterested(true);
                FoxEntityCopy.this.setIsCrouching(true);
                FoxEntityCopy.this.getNavigation().stop();
                FoxEntityCopy.this.getLookControl().setLookAt(livingentity, (float) FoxEntityCopy.this.getMaxHeadYRot(), (float) FoxEntityCopy.this.getMaxHeadXRot());
            } else {
                FoxEntityCopy.this.setIsInterested(false);
                FoxEntityCopy.this.setIsCrouching(false);
            }

        }

        public void tick() {
            if (getOwner() != null)
            {
                if (getOwner().position().distanceTo(position()) > 20 & !FoxEntityCopy.this.isSitting())
                {
                    stop();
                }
            }
            LivingEntity livingentity = FoxEntityCopy.this.getTarget();
            FoxEntityCopy.this.getLookControl().setLookAt(livingentity, (float) FoxEntityCopy.this.getMaxHeadYRot(), (float) FoxEntityCopy.this.getMaxHeadXRot());
            if (FoxEntityCopy.this.distanceToSqr(livingentity) <= 36.0D) {
                FoxEntityCopy.this.setIsInterested(true);
                FoxEntityCopy.this.setIsCrouching(true);
                FoxEntityCopy.this.getNavigation().stop();
            } else {
                FoxEntityCopy.this.getNavigation().moveTo(livingentity, 1.5D);
            }

        }
    }

    public static class FoxData extends AgeableEntity.AgeableData {
        public final Type type;

        public FoxData(Type p_i50734_1_) {
            super(false);
            this.type = p_i50734_1_;
        }
    }

    class JumpGoal extends Goal {
        int countdown;

        public JumpGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return FoxEntityCopy.this.isFaceplanted();
        }

        public boolean canContinueToUse() {
            return this.canUse() && this.countdown > 0;
        }

        public void start() {
            this.countdown = 40;
        }

        public void stop() {
            FoxEntityCopy.this.setFaceplanted(false);
        }

        public void tick() {
            --this.countdown;
        }
    }

    public class LookHelperController extends LookController {
        public LookHelperController() {
            super(FoxEntityCopy.this);
        }

        public void tick() {
            if (!FoxEntityCopy.this.isSleeping()) {
                super.tick();
            }

        }

        protected boolean resetXRotOnTick() {
            return !FoxEntityCopy.this.isPouncing() && !FoxEntityCopy.this.isCrouching() && !FoxEntityCopy.this.isInterested() & !FoxEntityCopy.this.isFaceplanted();
        }
    }

    class MateGoal extends BreedGoal {
        public MateGoal(double p_i50738_2_) {
            super(FoxEntityCopy.this, p_i50738_2_);
        }

        public void start() {
            ((FoxEntityCopy)this.animal).clearStates();
            ((FoxEntityCopy)this.partner).clearStates();
            super.start();
        }

        protected void breed() {
            ServerWorld serverworld = (ServerWorld)this.level;
            FoxEntityCopy foxentity = (FoxEntityCopy)this.animal.getBreedOffspring(serverworld, this.partner);
            final net.minecraftforge.event.entity.living.BabyEntitySpawnEvent event = new net.minecraftforge.event.entity.living.BabyEntitySpawnEvent(animal, partner, foxentity);
            final boolean cancelled = net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
            foxentity = (FoxEntityCopy) event.getChild();
            if (cancelled) {
                //Reset the "inLove" state for the animals
                this.animal.setAge(6000);
                this.partner.setAge(6000);
                this.animal.resetLove();
                this.partner.resetLove();
                return;
            }
            if (foxentity != null) {
                ServerPlayerEntity serverplayerentity = this.animal.getLoveCause();
                ServerPlayerEntity serverplayerentity1 = this.partner.getLoveCause();
                ServerPlayerEntity serverplayerentity2 = serverplayerentity;
                if (serverplayerentity != null) {
                    foxentity.addTrustedUUID(serverplayerentity.getUUID());
                } else {
                    serverplayerentity2 = serverplayerentity1;
                }

                if (serverplayerentity1 != null && serverplayerentity != serverplayerentity1) {
                    foxentity.addTrustedUUID(serverplayerentity1.getUUID());
                }

                if (serverplayerentity2 != null) {
                    serverplayerentity2.awardStat(Stats.ANIMALS_BRED);
                    CriteriaTriggers.BRED_ANIMALS.trigger(serverplayerentity2, this.animal, this.partner, foxentity);
                }

                this.animal.setAge(6000);
                this.partner.setAge(6000);
                this.animal.resetLove();
                this.partner.resetLove();
                foxentity.setAge(-24000);
                foxentity.moveTo(this.animal.getX(), this.animal.getY(), this.animal.getZ(), 0.0F, 0.0F);
                serverworld.addFreshEntityWithPassengers(foxentity);
                this.level.broadcastEntityEvent(this.animal, (byte)18);
                if (this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                    this.level.addFreshEntity(new ExperienceOrbEntity(this.level, this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1));
                }

            }
        }
    }

    class MoveHelperController extends MovementController {
        public MoveHelperController() {
            super(FoxEntityCopy.this);
        }

        public void tick() {
            if (FoxEntityCopy.this.canMove()) {
                super.tick();
            }

        }
    }

    class PanicGoal extends net.minecraft.entity.ai.goal.PanicGoal {
        public PanicGoal(double p_i50729_2_) {
            super(FoxEntityCopy.this, p_i50729_2_);
        }

        public boolean canUse() {
            return !FoxEntityCopy.this.isDefending() && super.canUse();
        }
    }

    public class OwnerHurtMobGoal extends TargetGoal
    {
        protected final TameableEntity tameAnimal;
        protected LivingEntity ownerLastHurt;
        protected int timestamp;

        public OwnerHurtMobGoal(TameableEntity entity) {
            super(entity, false);
            this.tameAnimal = entity;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (this.tameAnimal.isTame() && !this.tameAnimal.isOrderedToSit()) {
                LivingEntity livingentity = this.tameAnimal.getOwner();
                if (livingentity == null) {
                    return false;
                } else {
                    this.ownerLastHurt = livingentity.getLastHurtMob();
                    int i = livingentity.getLastHurtMobTimestamp();
                    return i != this.timestamp && this.canAttack(this.ownerLastHurt, EntityPredicate.DEFAULT) && this.tameAnimal.wantsToAttack(this.ownerLastHurt, livingentity);
                }
            } else {
                return false;
            }
        }

        @Override
        public void start() {
            FoxEntityCopy.this.setTarget(this.ownerLastHurt);
            FoxEntityCopy.this.ignoreStalk = this.ownerLastHurt;
            this.mob.setTarget(this.ownerLastHurt);
            LivingEntity livingentity = this.tameAnimal.getOwner();
            if (livingentity != null) {
                this.timestamp = livingentity.getLastHurtMobTimestamp();
            }

            super.start();
        }
    }

    public class PounceGoal extends net.minecraft.entity.ai.goal.JumpGoal {
        public boolean canUse() {
            if (!FoxEntityCopy.this.isFullyCrouched()) {
                return false;
            } else {
                LivingEntity livingentity = FoxEntityCopy.this.getTarget();
                if (livingentity != null && livingentity.isAlive()) {
                    if (livingentity.getMotionDirection() != livingentity.getDirection()) {
                        return false;
                    } else {
                        boolean flag = isPathClear(FoxEntityCopy.this, livingentity);
                        if (!flag) {
                            FoxEntityCopy.this.getNavigation().createPath(livingentity, 0);
                            FoxEntityCopy.this.setIsCrouching(false);
                            FoxEntityCopy.this.setIsInterested(false);
                        }

                        return flag;
                    }
                } else {
                    return false;
                }
            }
        }

        public boolean canContinueToUse() {
            LivingEntity livingentity = FoxEntityCopy.this.getTarget();
            if (livingentity != null && livingentity.isAlive()) {
                double d0 = FoxEntityCopy.this.getDeltaMovement().y;
                return (!(d0 * d0 < (double)0.05F) || !(Math.abs(FoxEntityCopy.this.xRot) < 15.0F) || !FoxEntityCopy.this.onGround) && !FoxEntityCopy.this.isFaceplanted();
            } else {
                return false;
            }
        }

        public boolean isInterruptable() {
            return false;
        }

        public void start() {
            FoxEntityCopy.this.setJumping(true);
            FoxEntityCopy.this.setIsPouncing(true);
            FoxEntityCopy.this.setIsInterested(false);
            LivingEntity livingentity = FoxEntityCopy.this.getTarget();
            FoxEntityCopy.this.getLookControl().setLookAt(livingentity, 60.0F, 30.0F);
            Vector3d vector3d = (new Vector3d(livingentity.getX() - FoxEntityCopy.this.getX(), livingentity.getY() - FoxEntityCopy.this.getY(), livingentity.getZ() - FoxEntityCopy.this.getZ())).normalize();
            FoxEntityCopy.this.setDeltaMovement(FoxEntityCopy.this.getDeltaMovement().add(vector3d.x * 0.8D, 0.9D, vector3d.z * 0.8D));
            FoxEntityCopy.this.getNavigation().stop();
        }

        public void stop() {
            FoxEntityCopy.this.setIsCrouching(false);
            FoxEntityCopy.this.crouchAmount = 0.0F;
            FoxEntityCopy.this.crouchAmountO = 0.0F;
            FoxEntityCopy.this.setIsInterested(false);
            FoxEntityCopy.this.setIsPouncing(false);
        }

        public void tick() {
            LivingEntity livingentity = FoxEntityCopy.this.getTarget();
            if (livingentity != null) {
                FoxEntityCopy.this.getLookControl().setLookAt(livingentity, 60.0F, 30.0F);
            }

            if (!FoxEntityCopy.this.isFaceplanted()) {
                Vector3d vector3d = FoxEntityCopy.this.getDeltaMovement();
                if (vector3d.y * vector3d.y < (double)0.03F && FoxEntityCopy.this.xRot != 0.0F) {
                    FoxEntityCopy.this.xRot = MathHelper.rotlerp(FoxEntityCopy.this.xRot, 0.0F, 0.2F);
                } else {
                    double d0 = Math.sqrt(Entity.getHorizontalDistanceSqr(vector3d));
                    double d1 = Math.signum(-vector3d.y) * Math.acos(d0 / vector3d.length()) * (double)(180F / (float)Math.PI);
                    FoxEntityCopy.this.xRot = (float)d1;
                }
            }

            if (livingentity != null && FoxEntityCopy.this.distanceTo(livingentity) <= 2.0F) {
                FoxEntityCopy.this.doHurtTarget(livingentity);
            } else if (FoxEntityCopy.this.xRot > 0.0F && FoxEntityCopy.this.onGround && (float) FoxEntityCopy.this.getDeltaMovement().y != 0.0F && FoxEntityCopy.this.level.getBlockState(FoxEntityCopy.this.blockPosition()).is(Blocks.SNOW)) {
                FoxEntityCopy.this.xRot = 60.0F;
                FoxEntityCopy.this.setTarget((LivingEntity)null);
                FoxEntityCopy.this.setFaceplanted(true);
            }

        }
    }

    class RevengeGoal extends NearestAttackableTargetGoal<LivingEntity> {
        @Nullable
        private LivingEntity trustedLastHurtBy;
        private LivingEntity trustedLastHurt;
        private int timestamp;

        public RevengeGoal(Class<LivingEntity> p_i50743_2_, boolean p_i50743_3_, boolean p_i50743_4_, @Nullable Predicate<LivingEntity> p_i50743_5_) {
            super(FoxEntityCopy.this, p_i50743_2_, 10, p_i50743_3_, p_i50743_4_, p_i50743_5_);
        }

        public boolean canUse() {
            if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
                return false;
            } else {
                for(UUID uuid : FoxEntityCopy.this.getTrustedUUIDs()) {
                    if (uuid != null && FoxEntityCopy.this.level instanceof ServerWorld) {
                        Entity entity = ((ServerWorld) FoxEntityCopy.this.level).getEntity(uuid);
                        if (entity instanceof LivingEntity) {
                            LivingEntity livingentity = (LivingEntity)entity;
                            this.trustedLastHurt = livingentity;
                            this.trustedLastHurtBy = livingentity.getLastHurtByMob();
                            int i = livingentity.getLastHurtByMobTimestamp();
                            return i != this.timestamp && this.canAttack(this.trustedLastHurtBy, this.targetConditions);
                        }
                    }
                }

                return false;
            }
        }

        public void start() {
            this.setTarget(this.trustedLastHurtBy);
            this.target = this.trustedLastHurtBy;
            if (this.trustedLastHurt != null) {
                this.timestamp = this.trustedLastHurt.getLastHurtByMobTimestamp();
            }

            FoxEntityCopy.this.playSound(SoundEvents.FOX_AGGRO, 1.0F, 1.0F);
            FoxEntityCopy.this.setDefending(true);
            FoxEntityCopy.this.wakeUp();
            super.start();
        }
    }

    class SitAndLookGoal extends BaseGoal {
        private double relX;
        private double relZ;
        private int lookTime;
        private int looksRemaining;

        public SitAndLookGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean canUse() {
            return FoxEntityCopy.this.getLastHurtByMob() == null && FoxEntityCopy.this.getRandom().nextFloat() < 0.02F && !FoxEntityCopy.this.isSleeping() && FoxEntityCopy.this.getTarget() == null && FoxEntityCopy.this.getNavigation().isDone() && !this.alertable() && !FoxEntityCopy.this.isPouncing() && !FoxEntityCopy.this.isCrouching();
        }

        public boolean canContinueToUse() {
            return this.looksRemaining > 0;
        }

        public void start() {
            this.resetLook();
            this.looksRemaining = 2 + FoxEntityCopy.this.getRandom().nextInt(3);
            FoxEntityCopy.this.setSitting(true);
            FoxEntityCopy.this.getNavigation().stop();
        }

        public void stop() {
            FoxEntityCopy.this.setSitting(false);
        }

        public void tick() {

            FoxEntityCopy.this.getLookControl().setLookAt(FoxEntityCopy.this.getX() + this.relX, FoxEntityCopy.this.getEyeY(), FoxEntityCopy.this.getZ() + this.relZ, (float) FoxEntityCopy.this.getMaxHeadYRot(), (float) FoxEntityCopy.this.getMaxHeadXRot());
        }

        private void resetLook() {
            double d0 = (Math.PI * 2D) * FoxEntityCopy.this.getRandom().nextDouble();
            this.relX = Math.cos(d0);
            this.relZ = Math.sin(d0);
            this.lookTime = 80 + FoxEntityCopy.this.getRandom().nextInt(20);
        }
    }

    class SleepGoal extends BaseGoal {
        private int countdown = FoxEntityCopy.this.random.nextInt(140);

        public SleepGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        public boolean canUse() {
            if (FoxEntityCopy.this.xxa == 0.0F && FoxEntityCopy.this.yya == 0.0F && FoxEntityCopy.this.zza == 0.0F) {
                return this.canSleep() || FoxEntityCopy.this.isSleeping();
            } else {
                return false;
            }
        }

        public boolean canContinueToUse() {
            return this.canSleep();
        }

        private boolean canSleep() {
            if (this.countdown > 0) {
                --this.countdown;
                return false;
            } else {
                if (FoxEntityCopy.this.isSitting())
                {
                    if (!this.hasShelter())
                    {
                        FoxEntityCopy.this.wakeUp();
                    }
                    return this.hasShelter();
                }
                return (FoxEntityCopy.this.level.isDay() && this.hasShelter() && !this.alertable());
            }
        }

        public void stop() {
            this.countdown = 100+FoxEntityCopy.this.random.nextInt(140);
            FoxEntityCopy.this.clearStates();
        }

        public void start() {
            FoxEntityCopy.this.setSitting(true);
            FoxEntityCopy.this.setIsCrouching(false);
            FoxEntityCopy.this.setIsInterested(false);
            FoxEntityCopy.this.setJumping(false);
            FoxEntityCopy.this.setSleeping(true);
            FoxEntityCopy.this.getNavigation().stop();
            FoxEntityCopy.this.getMoveControl().setWantedPosition(FoxEntityCopy.this.getX(), FoxEntityCopy.this.getY(), FoxEntityCopy.this.getZ(), 0.0D);
        }
    }

    class StrollGoal extends MoveThroughVillageAtNightGoal {
        public StrollGoal(int p_i50726_2_, int p_i50726_3_) {
            super(FoxEntityCopy.this, p_i50726_3_);
        }

        public void start() {
            FoxEntityCopy.this.clearStates();
            super.start();
        }

        public boolean canUse() {
            return super.canUse() && this.canFoxMove();
        }

        public boolean canContinueToUse() {
            return super.canContinueToUse() && this.canFoxMove();
        }

        private boolean canFoxMove() {
            boolean flag = true;
            if (getOwner() != null)
            {
                if (getOwner().position().distanceTo(position()) > 20)
                {
                    flag = false;
                }
            }
            return flag && !FoxEntityCopy.this.isSleeping() && !FoxEntityCopy.this.isSitting() && !FoxEntityCopy.this.isDefending() && FoxEntityCopy.this.getTarget() == null;
        }
    }

    class SwimGoal extends net.minecraft.entity.ai.goal.SwimGoal {
        public SwimGoal() {
            super(FoxEntityCopy.this);
        }

        public void start() {
            super.start();
            FoxEntityCopy.this.clearStates();
        }

        public boolean canUse() {
            return FoxEntityCopy.this.isInWater() && FoxEntityCopy.this.getFluidHeight(FluidTags.WATER) > 0.25D || FoxEntityCopy.this.isInLava();
        }
    }

    public static enum Type {
        RED(0, "red", Biomes.TAIGA, Biomes.TAIGA_HILLS, Biomes.TAIGA_MOUNTAINS, Biomes.GIANT_TREE_TAIGA, Biomes.GIANT_SPRUCE_TAIGA, Biomes.GIANT_TREE_TAIGA_HILLS, Biomes.GIANT_SPRUCE_TAIGA_HILLS),
        SNOW(1, "snow", Biomes.SNOWY_TAIGA, Biomes.SNOWY_TAIGA_HILLS, Biomes.SNOWY_TAIGA_MOUNTAINS);

        private static final Type[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(Type::getId)).toArray((p_221084_0_) -> {
            return new Type[p_221084_0_];
        });
        private static final Map<String, Type> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(Type::getName, (p_221081_0_) -> {
            return p_221081_0_;
        }));
        private final int id;
        private final String name;
        private final List<RegistryKey<Biome>> biomes;

        private Type(int p_i241911_3_, String p_i241911_4_, RegistryKey<Biome>... p_i241911_5_) {
            this.id = p_i241911_3_;
            this.name = p_i241911_4_;
            this.biomes = Arrays.asList(p_i241911_5_);
        }

        public String getName() {
            return this.name;
        }

        public int getId() {
            return this.id;
        }

        public static Type byName(String p_221087_0_) {
            return BY_NAME.getOrDefault(p_221087_0_, RED);
        }

        public static Type byId(int p_221080_0_) {
            if (p_221080_0_ < 0 || p_221080_0_ > BY_ID.length) {
                p_221080_0_ = 0;
            }

            return BY_ID[p_221080_0_];
        }

        public static Type byBiome(Optional<RegistryKey<Biome>> p_242325_0_) {
            return p_242325_0_.isPresent() && SNOW.biomes.contains(p_242325_0_.get()) ? SNOW : RED;
        }
    }

    class WatchGoal extends LookAtGoal {
        public WatchGoal(MobEntity p_i50733_2_, Class<? extends LivingEntity> p_i50733_3_, float p_i50733_4_) {
            super(p_i50733_2_, p_i50733_3_, p_i50733_4_);
        }

        public boolean canUse() {
            return super.canUse() && !FoxEntityCopy.this.isFaceplanted() && !FoxEntityCopy.this.isInterested();
        }

        public boolean canContinueToUse() {
            return super.canContinueToUse() && !FoxEntityCopy.this.isFaceplanted() && !FoxEntityCopy.this.isInterested();
        }
    }
}
