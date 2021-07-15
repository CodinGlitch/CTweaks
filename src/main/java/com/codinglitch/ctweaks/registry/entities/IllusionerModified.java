package com.codinglitch.ctweaks.registry.entities;

import com.codinglitch.ctweaks.CTweaks;
import com.codinglitch.ctweaks.util.SoundsC;
import com.codinglitch.ctweaks.util.UtilityC;
import net.minecraft.advancements.criterion.UsedTotemTrigger;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.EvokerFangsEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BowItem;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.raid.RaidManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

public class IllusionerModified extends IllusionerEntity {
    public boolean enraged = false;

    public int ticksToTeleport = 0;
    public int comboCount = 0;

    public IllusionerModified(EntityType<? extends IllusionerEntity> entityType, World world) {
        super(entityType, world);
        this.xpReward = 10;
        setValue("clientSideIllusionOffsets", new Vector3d[2][4]);

        for(int i = 0; i < 4; ++i) {
            ((Vector3d[][])getValue("clientSideIllusionOffsets"))[0][i] = Vector3d.ZERO;
            ((Vector3d[][])getValue("clientSideIllusionOffsets"))[1][i] = Vector3d.ZERO;
        }

        this.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
    }

    private void setValue(String name, Object value)
    {
        ObfuscationReflectionHelper.setPrivateValue(IllusionerEntity.class, this, value, name);
    }

    private <T> T getValue(String name)
    {
        return ObfuscationReflectionHelper.getPrivateValue(IllusionerEntity.class, this, name);
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        boolean result = super.hurt(source, damage);
        if (getMainHandItem().getItem() != Items.TOTEM_OF_UNDYING & !enraged)
        {
            enraged = true;
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, this.getSoundSource(), 1.0F, 2.0F);

            this.heal(10);

            this.invulnerableTime = 200;

            setValue("clientSideIllusionOffsets", new Vector3d[2][8]);

            for(int i = 0; i < 8; ++i) {
                ((Vector3d[][])getValue("clientSideIllusionOffsets"))[0][i] = Vector3d.ZERO;
                ((Vector3d[][])getValue("clientSideIllusionOffsets"))[1][i] = Vector3d.ZERO;
            }
        }
        if (enraged)
        {
            double x = getX()+randomOffset(2,5);
            double y = getY() + 50;
            double z = getZ()+randomOffset(2,5);
            this.randomTeleport(x,y,z, true);
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, this.getSoundSource(), 1.0F, 1.4F);
        }
        return result;
    }

    public double randomOffset(double offset, double range)
    {
        double off = offset + getRandom().nextDouble() * range;
        return getRandom().nextInt(2) == 0 ? -off : off;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        Set<PrioritizedGoal> goals = ObfuscationReflectionHelper.getPrivateValue(GoalSelector.class, this.goalSelector, "availableGoals");

        Iterator<PrioritizedGoal> iterator = goals.iterator();

        Goal toRemove = null;

        while (iterator.hasNext())
        {
            PrioritizedGoal goal = iterator.next();
            if (goal.getGoal() instanceof RangedBowAttackGoal)
            {
                toRemove = goal.getGoal();
                break;
            }
        }

        if (toRemove != null) this.goalSelector.removeGoal(toRemove);

        this.goalSelector.addGoal(6, new StrafeGoal<>(this, 0.5D, 20, 15.0F));
        this.goalSelector.addGoal(3, new IllusionerModified.AttackSpellGoal());
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int i, boolean b) {
        super.dropCustomDeathLoot(source, i, b);
        ItemEntity itemEntity = new ItemEntity(level, getX(), getY(), getZ(), new ItemStack(Items.TOTEM_OF_UNDYING));
        level.addFreshEntity(itemEntity);
    }

    @Override
    public ILivingEntityData finalizeSpawn(IServerWorld world, DifficultyInstance difficultyInstance, SpawnReason reason, @Nullable ILivingEntityData livingEntityData, @Nullable CompoundNBT nbt) {
        ILivingEntityData data = super.finalizeSpawn(world, difficultyInstance, reason, livingEntityData, nbt);
        return data;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        int amount = enraged ? 8 : 4;
        int count = enraged ? 10 : 30;
        if (this.level.isClientSide && this.isInvisible()) {
            setValue("clientSideIllusionTicks", (int)getValue("clientSideIllusionTicks")-1);
            if ((int)getValue("clientSideIllusionTicks") < 0) {
                setValue("clientSideIllusionTicks", 0);
            }

            if (this.hurtTime != 1 && this.tickCount % 1200 != 0) {
                if (this.hurtTime == this.hurtDuration - 1) {
                    setValue("clientSideIllusionTicks", 3);

                    for(int k = 0; k < amount; ++k) {
                        ((Vector3d[][])getValue("clientSideIllusionOffsets"))[0][k] = ((Vector3d[][])getValue("clientSideIllusionOffsets"))[1][k];
                        ((Vector3d[][])getValue("clientSideIllusionOffsets"))[1][k] = new Vector3d(0.0D, 0.0D, 0.0D);
                    }
                }
            } else {
                setValue("clientSideIllusionTicks", 3);
                float f = -6.0F;
                int i = 13;

                for(int j = 0; j < amount; ++j) {
                    ((Vector3d[][])getValue("clientSideIllusionOffsets"))[0][j] = ((Vector3d[][])getValue("clientSideIllusionOffsets"))[1][j];
                    ((Vector3d[][])getValue("clientSideIllusionOffsets"))[1][j] = new Vector3d((double)(-6.0F + (float)this.random.nextInt(13)) * 0.5D, (double)Math.max(0, this.random.nextInt(6) - 4), (double)(-6.0F + (float)this.random.nextInt(13)) * 0.5D);
                }

                for(int l = 0; l < 16; ++l) {
                    this.level.addParticle(ParticleTypes.CLOUD, this.getRandomX(0.5D), this.getRandomY(), this.getZ(0.5D), 0.0D, 0.0D, 0.0D);
                }

                this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, this.getSoundSource(), 1.0F, 1.0F, false);
            }
        }

        if (ticksToTeleport > 0)
        {
            ticksToTeleport--;
        }

        if (!level.isClientSide)
        {
            if (ticksToTeleport == 0 & getTarget() != null)
            {
                if (enraged)
                {
                    for(int i = 0; i < 8; ++i) {
                        ((Vector3d[][])getValue("clientSideIllusionOffsets"))[0][i] = Vector3d.ZERO;
                        ((Vector3d[][])getValue("clientSideIllusionOffsets"))[1][i] = Vector3d.ZERO;
                    }
                }
                else
                {
                    for(int i = 0; i < 4; ++i) {
                        ((Vector3d[][])getValue("clientSideIllusionOffsets"))[0][i] = Vector3d.ZERO;
                        ((Vector3d[][])getValue("clientSideIllusionOffsets"))[1][i] = Vector3d.ZERO;
                    }
                }

                double x = getTarget().getX()+randomOffset(5,6);
                double y = getTarget().getY() + 50;
                double z = getTarget().getZ()+randomOffset(5,6);
                this.randomTeleport(x,y,z, false);
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, this.getSoundSource(), 1.0F, 1.4F);

                if (comboCount > 2 & comboCount < 8)
                {
                    ticksToTeleport = (int)randomOffset(8,4) + count;
                }
                else if (comboCount < 3)
                {
                    ticksToTeleport = (int)randomOffset(50,20) + count*2;
                }
                else
                {
                    ticksToTeleport = 200;
                    comboCount = -1;
                }
                comboCount++;
            }
        }
    }

    class AttackSpellGoal extends SpellcastingIllagerEntity.UseSpellGoal {
        private AttackSpellGoal() {
        }

        protected int getCastingTime() {
            return 40;
        }

        protected int getCastingInterval() {
            return 100;
        }

        protected void performSpellCasting() {
            LivingEntity livingentity = getTarget();
            double leastY = Math.min(livingentity.getY(), getY());
            double mostY = Math.max(livingentity.getY(), getY()) + 1.0D;
            float f = (float)MathHelper.atan2(livingentity.getZ() - getZ(), livingentity.getX() - getX());
            if (distanceToSqr(livingentity) < 15.0D) {
                for(int i = 0; i < 5; ++i) {
                    float f1 = f + (float)i * (float)Math.PI * 0.4F;
                    this.createSpellEntity(getX() + (double)MathHelper.cos(f1) * 1.5D, getZ() + (double)MathHelper.sin(f1) * 1.5D, leastY, mostY, f1, 0);
                }

                for(int k = 0; k < 16; ++k) {
                    float f2 = f + (float)k * (float)Math.PI * 2.0F / 8.0F + 1.2566371F;
                    this.createSpellEntity(getX() + (double)MathHelper.cos(f2) * (2.5D+((float)k/5)), getZ() + (double)MathHelper.sin(f2) * (2.5D+((float)k/5)), leastY, mostY, f2, 3+k/2);
                }
            } else {
                for(int l = 0; l < 16; ++l) {
                    double d2 = 1.25D * (double)(l + 1);
                    this.createSpellEntity(getX() + (double)MathHelper.cos(f) * d2, getZ() + (double)MathHelper.sin(f) * d2, leastY, mostY, f, l*2);
                }
            }

        }

        private void createSpellEntity(double x, double z, double leastY, double y, float yRotation, int warmupDelayTicks) {
            BlockPos blockpos = new BlockPos(x, y, z);
            boolean flag = false;
            double d0 = 0.0D;

            do {
                BlockPos blockpos1 = blockpos.below();
                BlockState blockstate = level.getBlockState(blockpos1);
                if (blockstate.isFaceSturdy(level, blockpos1, Direction.UP)) {
                    if (!level.isEmptyBlock(blockpos)) {
                        BlockState blockstate1 = level.getBlockState(blockpos);
                        VoxelShape voxelshape = blockstate1.getCollisionShape(level, blockpos);
                        if (!voxelshape.isEmpty()) {
                            d0 = voxelshape.max(Direction.Axis.Y);
                        }
                    }

                    flag = true;
                    break;
                }

                blockpos = blockpos.below();
            } while(blockpos.getY() >= MathHelper.floor(leastY) - 1);

            if (flag) {
                level.addFreshEntity(new IllusionerGeyser(level, x, (double)blockpos.getY() + d0, z, warmupDelayTicks, IllusionerModified.this));
            }

        }

        protected SoundEvent getSpellPrepareSound() {
            return SoundsC.illusioner_attack.get();
        }

        protected SpellcastingIllagerEntity.SpellType getSpell() {
            return SpellcastingIllagerEntity.SpellType.FANGS;
        }
    }

    public class StrafeGoal<T extends MonsterEntity> extends Goal
    {
        private final T mob;
        private final double speedModifier;
        private int attackIntervalMin;
        private final float attackRadiusSqr;
        private int attackTime = -1;
        private int seeTime;
        private boolean strafingClockwise;
        private boolean strafingBackwards;
        private int strafingTime = -1;

        public StrafeGoal(T p_i47515_1_, double p_i47515_2_, int p_i47515_4_, float p_i47515_5_) {
            this.mob = p_i47515_1_;
            this.speedModifier = p_i47515_2_;
            this.attackIntervalMin = p_i47515_4_;
            this.attackRadiusSqr = p_i47515_5_ * p_i47515_5_;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public void setMinAttackInterval(int p_189428_1_) {
            this.attackIntervalMin = p_189428_1_;
        }

        public boolean canUse() {
            return this.mob.getTarget() != null;
        }

        public boolean canContinueToUse() {
            return (this.canUse() || !this.mob.getNavigation().isDone());
        }

        public void start() {
            super.start();
        }

        public void stop() {
            super.stop();
            this.seeTime = 0;
            this.attackTime = -1;
            this.mob.stopUsingItem();
        }

        public void tick() {
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity != null) {
                double d0 = this.mob.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
                boolean flag = this.mob.getSensing().canSee(livingentity);
                boolean flag1 = this.seeTime > 0;
                if (flag != flag1) {
                    this.seeTime = 0;
                }

                if (flag) {
                    ++this.seeTime;
                } else {
                    --this.seeTime;
                }

                if (!(d0 > (double)this.attackRadiusSqr) && this.seeTime >= 20) {
                    this.mob.getNavigation().stop();
                    ++this.strafingTime;
                } else {
                    this.mob.getNavigation().moveTo(livingentity, this.speedModifier);
                    this.strafingTime = -1;
                }

                if (this.strafingTime >= 20) {
                    if ((double)this.mob.getRandom().nextFloat() < 0.3D) {
                        this.strafingClockwise = !this.strafingClockwise;
                    }

                    if ((double)this.mob.getRandom().nextFloat() < 0.3D) {
                        this.strafingBackwards = !this.strafingBackwards;
                    }

                    this.strafingTime = 0;
                }

                if (this.strafingTime > -1) {
                    if (d0 > (double)(this.attackRadiusSqr * 0.75F)) {
                        this.strafingBackwards = false;
                    } else if (d0 < (double)(this.attackRadiusSqr * 0.25F)) {
                        this.strafingBackwards = true;
                    }

                    this.mob.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
                    this.mob.lookAt(livingentity, 30.0F, 30.0F);
                } else {
                    this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
                }
            }
        }
    }
}
