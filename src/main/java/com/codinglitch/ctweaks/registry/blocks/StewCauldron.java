package com.codinglitch.ctweaks.registry.blocks;

import com.codinglitch.ctweaks.registry.behaviour.StewInteraction;
import com.codinglitch.ctweaks.registry.blockentities.StewCauldronBlockEntity;
import com.codinglitch.ctweaks.registry.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class StewCauldron extends LayeredCauldronBlock implements EntityBlock {
    public static final Predicate<Biome.Precipitation> STEW = (p_153526_) -> false;

    public StewCauldron() {
        super(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.STONE).requiresCorrectToolForDrops().strength(2.0F).noOcclusion(), STEW, StewInteraction.STEW);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StewCauldronBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntityInit.STEW_CAULDRON.get(), StewCauldronBlockEntity::tick);
    }

    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> typeA, BlockEntityType<E> typeE, BlockEntityTicker<? super E> ticker) {
        return typeA == typeE ? (BlockEntityTicker<A>)ticker : null;
    }
}
