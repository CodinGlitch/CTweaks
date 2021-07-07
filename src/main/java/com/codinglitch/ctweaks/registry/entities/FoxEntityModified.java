package com.codinglitch.ctweaks.registry.entities;

import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;

public class FoxEntityModified extends FoxEntityCopy {
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
        }
        else
        {
            if (this.level.isClientSide) {
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

                    if ((!actionresulttype.consumesAction() || this.isBaby()) && this.isOwnedBy(player)) {
                        this.setOrderedToSit(!this.isOrderedToSit());
                        this.setSitting(!this.isSitting());
                        if (this.isSleeping())
                        {
                            this.stopSleeping();
                        }

                        this.jumping = false;
                        this.navigation.stop();
                        this.setTarget((LivingEntity)null);
                        return ActionResultType.SUCCESS;
                    }

                } else if (item == Items.SWEET_BERRIES) {
                    if (!player.abilities.instabuild) {
                        itemstack.shrink(1);
                    }

                    if (this.random.nextInt(3) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
                        this.tame(player);
                        this.navigation.stop();
                        this.setTarget((LivingEntity)null);
                        this.setOrderedToSit(true);
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
