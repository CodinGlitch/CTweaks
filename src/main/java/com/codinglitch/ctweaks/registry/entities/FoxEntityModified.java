package com.codinglitch.ctweaks.registry.entities;

import com.codinglitch.ctweaks.util.ReferenceC;
import com.codinglitch.ctweaks.util.SoundsC;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.*;
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
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

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
