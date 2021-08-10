package com.codinglitch.ctweaks.registry.blocks;

import com.codinglitch.ctweaks.registry.init.BlockProperties;
import com.codinglitch.ctweaks.registry.init.DamageSources;
import com.codinglitch.ctweaks.registry.init.ParticlesInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

import java.util.Random;

public class ElectricFence extends FenceBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty ELECTRIC_RUNNING = BlockProperties.ELECTRIC_RUNNING;

    public ElectricFence() {
        super(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundType.COPPER).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(POWERED, false)
                .setValue(ELECTRIC_RUNNING, false)
                .setValue(WATERLOGGED, false)
        );
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (state.getValue(ELECTRIC_RUNNING))
        {
            entity.hurt(DamageSources.ELECTRICITY, 1);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        super.tick(state, level, pos, random);
        boolean value = !state.getValue(POWERED);

        if (value)
        {
            level.setBlock(pos, state.setValue(POWERED, value), 3);
            level.getBlockTicks().scheduleTick(pos, state.getBlock(), 2);
            for (int i = 0; i < 3+random.nextInt(4); i++) {
                level.sendParticles(ParticlesInit.ELECTRICITY.get(),
                        (double)pos.getX() + random.nextDouble(),
                        (double)pos.getY() + random.nextDouble(),
                        (double)pos.getZ() + random.nextDouble(),
                        1,
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        }
        else
        {
            level.setBlock(pos, state.setValue(POWERED, value).setValue(ELECTRIC_RUNNING, true), 2);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        super.animateTick(state, level, pos, random);

        if (state.getValue(ELECTRIC_RUNNING))
        {
            if (level.getGameTime() % 5L == 0) {
                if (level.isClientSide)
                {
                    level.addParticle(ParticlesInit.ELECTRICITY.get(), false,
                            (double)pos.getX() + random.nextDouble(),
                            (double)pos.getY() + random.nextDouble(),
                            (double)pos.getZ() + random.nextDouble(),
                            0.0D,
                            0.0D,
                            0.0D
                    );
                }
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighbor, boolean b) {
        if (block instanceof LightningRodBlock || block instanceof ElectricFence)
        {
            BlockState neighborState = level.getBlockState(neighbor);
            if (neighborState.is(Blocks.AIR)) return;
            if (neighborState.getValue(POWERED))
            {
                level.getBlockTicks().scheduleTick(pos, state.getBlock(), 1);
            }
        }
    }

    @Override
    public boolean connectsTo(BlockState state, boolean b, Direction direction) {
        return state.getBlock() instanceof ElectricFence;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED, ELECTRIC_RUNNING);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState placedOn, boolean b) {
        if (!state.is(placedOn.getBlock())) {
            if (state.getValue(POWERED) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
                level.setBlock(pos, state.setValue(POWERED, false), 18);
            }
        }
    }
}
