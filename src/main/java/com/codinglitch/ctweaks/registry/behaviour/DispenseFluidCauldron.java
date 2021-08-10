package com.codinglitch.ctweaks.registry.behaviour;

import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;

public class DispenseFluidCauldron extends DefaultDispenseItemBehavior {
    private static final DispenseFluidCauldron INSTANCE = new DispenseFluidCauldron();

    public static DispenseFluidCauldron getInstance()
    {
        return INSTANCE;
    }

    private DispenseFluidCauldron() {}

    private final DefaultDispenseItemBehavior dispenseBehavior = new DefaultDispenseItemBehavior();

    @Override
    @Nonnull
    public ItemStack execute(@Nonnull IBlockSource source, @Nonnull ItemStack stack)
    {
        if (FluidUtil.getFluidContained(stack).isPresent())
        {
            return dumpContainer(source, stack);
        }
        else
        {
            return fillContainer(source, stack);
        }
    }

    /**
     * Picks up fluid in front of a Dispenser and fills a container with it.
     */
    @Nonnull
    private ItemStack fillContainer(@Nonnull IBlockSource source, @Nonnull ItemStack stack)
    {
        World world = source.getLevel();
        Direction dispenserFacing = source.getBlockState().getValue(DispenserBlock.FACING);
        BlockPos blockpos = source.getPos().relative(dispenserFacing);

        FluidActionResult actionResult = FluidUtil.tryPickUpFluid(stack, null, world, blockpos, dispenserFacing.getOpposite());
        ItemStack resultStack = actionResult.getResult();

        if (!actionResult.isSuccess() || resultStack.isEmpty())
        {
            return super.execute(source, stack);
        }

        if (stack.getCount() == 1)
        {
            return resultStack;
        }
        else if (((DispenserTileEntity)source.getEntity()).addItem(resultStack) < 0)
        {
            this.dispenseBehavior.dispense(source, resultStack);
        }

        ItemStack stackCopy = stack.copy();
        stackCopy.shrink(1);
        return stackCopy;
    }

    /**
     * Drains a filled container and places the fluid in front of the Dispenser.
     */
    @Nonnull
    private ItemStack dumpContainer(IBlockSource source, @Nonnull ItemStack stack)
    {
        ItemStack singleStack = stack.copy();
        singleStack.setCount(1);
        IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(singleStack).orElse(null);
        if (fluidHandler == null)
        {
            return super.execute(source, stack);
        }

        FluidStack fluidStack = fluidHandler.drain(FluidAttributes.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
        Direction dispenserFacing = source.getBlockState().getValue(DispenserBlock.FACING);
        BlockPos blockpos = source.getPos().relative(dispenserFacing);
        FluidActionResult result = FluidUtil.tryPlaceFluid(null, source.getLevel(), Hand.MAIN_HAND, blockpos, stack, fluidStack);

        if (result.isSuccess())
        {
            ItemStack drainedStack = result.getResult();

            if (drainedStack.getCount() == 1)
            {
                return drainedStack;
            }
            else if (!drainedStack.isEmpty() && ((DispenserTileEntity)source.getEntity()).addItem(drainedStack) < 0)
            {
                this.dispenseBehavior.dispense(source, drainedStack);
            }

            ItemStack stackCopy = drainedStack.copy();
            stackCopy.shrink(1);
            return stackCopy;
        }
        else
        {
            return this.dispenseBehavior.dispense(source, stack);
        }
    }
}
