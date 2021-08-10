package com.codinglitch.ctweaks.registry;

import com.codinglitch.ctweaks.registry.behaviour.StewInteraction;
import com.codinglitch.ctweaks.registry.init.CapabilitiesInit;
import com.codinglitch.ctweaks.util.SoundsC;
import com.codinglitch.ctweaks.util.network.CTweaksPacketHandler;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.gameevent.GameEvent;

public class CommonInit {
    public static void init()
    {
        CapabilitiesInit.initCapabilities();
        CTweaksPacketHandler.init();

        StewInteraction.init();

        CauldronInteraction.WATER.put(Items.SPONGE, (state, level, blockPos, player, interactionHand, itemStack) -> {
            if (!level.isClientSide) {
                player.awardStat(Stats.USE_CAULDRON);
                level.setBlockAndUpdate(blockPos, Blocks.CAULDRON.defaultBlockState());
                level.gameEvent(null, GameEvent.FLUID_PICKUP, blockPos);
                level.playSound(null, blockPos, SoundsC.sponge.get(), SoundSource.BLOCKS, 1, 0.8f+level.random.nextFloat()/4);
                player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.WET_SPONGE)));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });
        CauldronInteraction.EMPTY.put(Items.WET_SPONGE, (state, level, blockPos, player, interactionHand, itemStack) -> {
            if (!level.isClientSide) {
                player.awardStat(Stats.FILL_CAULDRON);
                level.setBlockAndUpdate(blockPos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
                level.gameEvent(null, GameEvent.FLUID_PLACE, blockPos);
                level.playSound(null, blockPos, SoundsC.wet_sponge.get(), SoundSource.BLOCKS, 1, 0.8f+level.random.nextFloat()/4);
                player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.SPONGE)));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });


    }
}
