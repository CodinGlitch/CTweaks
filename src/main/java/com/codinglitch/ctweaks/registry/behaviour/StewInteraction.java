package com.codinglitch.ctweaks.registry.behaviour;

import com.codinglitch.ctweaks.config.BehaviourConfig;
import com.codinglitch.ctweaks.registry.blockentities.StewCauldronBlockEntity;
import com.codinglitch.ctweaks.registry.init.BlocksInit;
import com.codinglitch.ctweaks.registry.init.ItemsInit;
import com.codinglitch.ctweaks.util.SoundsC;
import com.codinglitch.ctweaks.util.StewIngredient;
import com.codinglitch.ctweaks.util.StewUtils;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.awt.*;
import java.util.Map;

public interface StewInteraction {
    Map<Item, CauldronInteraction> STEW = CauldronInteraction.newInteractionMap();

    static void init() {
        StewUtils.addIngredient(Items.POTATO, new Color(0xE2C568), 1, 0.2f);
        StewUtils.addIngredient(Items.CARROT, new Color(0xF3AB37), 1, 0.2f);
        StewUtils.addIngredient(Items.BEETROOT, new Color(0xC12121), 1, 0.15f);
        StewUtils.addIngredient(Items.RED_MUSHROOM, new Color(0xE52B2B), 1, 0.2f);
        StewUtils.addIngredient(Items.BROWN_MUSHROOM, new Color(0xA37043), 1, 0.2f);
        StewUtils.addIngredient(Items.WARPED_FUNGUS, new Color(0x299DA7), 2, 0.25f);
        StewUtils.addIngredient(Items.CRIMSON_FUNGUS, new Color(0x821A1A), 2, 0.25f);

        STEW.put(Items.BOWL, (state, level, blockPos, player, interactionHand, itemStack) -> {
            if (!level.isClientSide) {
                BlockEntity blockEntity = level.getBlockEntity(blockPos);
                if (blockEntity != null) {
                    if (blockEntity instanceof StewCauldronBlockEntity cauldron) {
                        if (cauldron.stewFinished) {
                            player.awardStat(Stats.FILL_CAULDRON);
                            LayeredCauldronBlock.lowerFillLevel(state, level, blockPos);
                            level.playSound(null, blockPos, SoundsC.sponge.get(), SoundSource.BLOCKS, 1, 0.8f+level.random.nextFloat()/4);

                            int[] items = new int[3];

                            for (int i = 0; i < cauldron.ingredients.size(); i++) {
                                items[i] = Item.getId(cauldron.ingredients.get(i));
                            }

                            ItemStack item = new ItemStack(ItemsInit.STEW.get());
                            CompoundTag tag = item.getOrCreateTag();
                            tag.putIntArray("Ingredients", items);

                            player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, item));
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
            }
            return InteractionResult.CONSUME;
        });
    }

    static void registerIngredients(StewIngredient...ingredients)
    {
        for (StewIngredient ingredient : ingredients) {
            registerIngredient(ingredient);
        }
    }

    static void registerIngredient(StewIngredient ingredient)
    {
        put(ingredient.getIngredient(), STEW);
        put(ingredient.getIngredient(), CauldronInteraction.EMPTY);
    }

    static void put(Item item, Map<Item, CauldronInteraction> map)
    {
        map.put(item, (state, level, blockPos, player, interactionHand, itemStack) -> {
            if (state.hasProperty(LayeredCauldronBlock.LEVEL))
                if (state.getValue(LayeredCauldronBlock.LEVEL) == 3) return InteractionResult.CONSUME;

            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity != null)
            {
                if (blockEntity instanceof StewCauldronBlockEntity cauldron)
                {
                    if (cauldron.stewFinished) return InteractionResult.FAIL;
                    if (cauldron.time > 0) return InteractionResult.FAIL;

                    int fluidLevel = state.getValue(LayeredCauldronBlock.LEVEL)+1;

                    if (!level.isClientSide) level.setBlockAndUpdate(blockPos, state.cycle(LayeredCauldronBlock.LEVEL));
                    if (!level.isClientSide) itemStack.shrink(1);

                    cauldron.time = BehaviourConfig.cook_time.get();
                    cauldron.ingredients.add(item);

                    if (fluidLevel == 3) {
                        cauldron.stewFinished = true;
                    }

                    return InteractionResult.SUCCESS;
                }
            }

            level.setBlockAndUpdate(blockPos, BlocksInit.STEW_CAULDRON.get().defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1));
            if (!level.isClientSide) itemStack.shrink(1);

            blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity != null) {
                if (blockEntity instanceof StewCauldronBlockEntity cauldron) {
                    cauldron.ingredients.add(item);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });
    }
}
