package com.codinglitch.ctweaks.registry.blocks;

import com.codinglitch.ctweaks.registry.tileentities.ModifiedJukeboxTileEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class ModifiedJukebox extends JukeboxBlock {
    public ModifiedJukebox() {
        super(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.DIRT).strength(2.0F, 6.0F));
    }

    @Override
    public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
        return new ModifiedJukeboxTileEntity();
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ModifiedJukeboxTileEntity();
    }
}
