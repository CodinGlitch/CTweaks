package com.codinglitch.ctweaks.registry.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class ModifiedJukebox extends JukeboxBlock {
    public ModifiedJukebox() {
        super(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.DIRT).strength(2.0F, 6.0F));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return super.newBlockEntity(pos, state);
    }
}
