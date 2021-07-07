package com.codinglitch.ctweaks.registry.tileentities;

import com.codinglitch.ctweaks.CTweaks;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

public class ModifiedJukeboxTileEntity extends JukeboxTileEntity {
    private final IItemHandlerModifiable inv = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() instanceof MusicDiscItem;
        }
    };

    private final LazyOptional<IItemHandler> capItemHandler = LazyOptional.of(() -> createCapabilityHandler());

    protected IItemHandler createCapabilityHandler() {
        return new CombinedInvWrapper(this.inv) {
            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (!simulate)
                {
                    stack.getItem().useOn(new ItemUseContext(level.getNearestPlayer(worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(), Double.MAX_VALUE, null), Hand.MAIN_HAND,
                            new BlockRayTraceResult(new Vector3d(0,0,0), Direction.UP, worldPosition, true)));
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
