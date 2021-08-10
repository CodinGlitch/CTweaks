package com.codinglitch.ctweaks.registry.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModifiedJukeboxBlockEntity extends JukeboxBlockEntity {
    private final IItemHandlerModifiable inv = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() instanceof RecordItem;
        }
    };

    private final LazyOptional<IItemHandler> capItemHandler = LazyOptional.of(() -> createCapabilityHandler());

    public ModifiedJukeboxBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    protected IItemHandler createCapabilityHandler() {
        return new CombinedInvWrapper(this.inv) {
            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (!simulate)
                {
                    stack.getItem().useOn(new UseOnContext(level.getNearestPlayer(worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(), Double.MAX_VALUE, null), InteractionHand.MAIN_HAND,
                            new BlockHitResult(new Vec3(0,0,0), Direction.UP, worldPosition, true)));
                    setRecord(stack);
                }

                return getRecord();
            }

            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                ItemStack stack = getRecord().copy();
                if (!simulate)
                    clearContent();
                level.levelEvent(1010, worldPosition, 0);
                return stack;
            }

            @Nonnull
            @Override
            public ItemStack getStackInSlot(int slot) {
                return getRecord();
            }


        };
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, capItemHandler);
    }

    @Override
    public ItemStack getRecord() {
        return super.getRecord();
    }
}
