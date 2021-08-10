package com.codinglitch.ctweaks.registry.blockentities;

import com.codinglitch.ctweaks.CTweaks;
import com.codinglitch.ctweaks.config.BehaviourConfig;
import com.codinglitch.ctweaks.registry.init.BlockEntityInit;
import com.codinglitch.ctweaks.registry.init.ParticlesInit;
import com.codinglitch.ctweaks.util.UtilityC;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class StewCauldronBlockEntity extends BlockEntity {
    private int ticksExisted = 0;
    public int time = 0;
    public List<Item> ingredients = new ArrayList<>();
    public boolean stewFinished = false;

    public StewCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.STEW_CAULDRON.get(), pos, state);
        this.time = BehaviourConfig.cook_time.get();
    }

    public static void tick(Level level, BlockPos blockPos, BlockState state, StewCauldronBlockEntity blockEntity)
    {
        blockEntity.ticksExisted++;

        boolean heated = false;
        boolean valid = UtilityC.isValidHeatSource(blockPos.below(), level);

        if (blockEntity.time > 0)
        {
            if (valid)
            {
                blockEntity.time--;
                heated = true;
            }
        }

        if (blockEntity.ticksExisted % (heated ? 2 : 10) == 0 & !level.isClientSide & valid)
        {
            ServerLevel serverlevel = (ServerLevel)level;
            int liquid_level = state.getValue(LayeredCauldronBlock.LEVEL);
            BlockPos pos = blockEntity.worldPosition;
            serverlevel.sendParticles(ParticlesInit.BUBBLE_PERSIST.get(),
                    (double)pos.getX() + 0.15f+level.random.nextFloat()*0.7f,
                    (double)pos.getY() + 0.3f+liquid_level*0.2f,
                    (double)pos.getZ() + 0.15f+level.random.nextFloat()*0.7f,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
            if (level.random.nextInt(80) == 0) {
                level.playSound(null, blockPos, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundSource.BLOCKS, 0.2F + level.random.nextFloat() * 0.2F, 0.9F + level.random.nextFloat() * 0.15F);
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        nbt = save(nbt);
        return nbt;
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        this.time = nbt.getInt("Time");
        this.stewFinished = nbt.getBoolean("StewFinished");
        int[] list = nbt.getIntArray("Ingredients");

        for (int id : list) {
            this.ingredients.add(Item.byId(id));
        }
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt = super.save(nbt);

        nbt.putInt("Time", this.time);
        nbt.putBoolean("StewFinished", this.stewFinished);
        int[] array = new int[ingredients.size()];
        for (int i = 0; i < ingredients.size(); i++) {
            array[i] = Item.getId(ingredients.get(i));
        }
        nbt.putIntArray("Ingredients", array);
        return nbt;
    }
}
