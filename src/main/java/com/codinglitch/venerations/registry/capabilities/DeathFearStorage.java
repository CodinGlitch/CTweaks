package com.codinglitch.venerations.registry.capabilities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class DeathFearStorage implements Capability.IStorage<IDeathFear> {
    @Override
    public INBT writeNBT(Capability<IDeathFear> capability, IDeathFear instance, Direction side) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("fear", instance.getFear());
        nbt.putInt("counter", instance.getFearCounter());
        nbt.putInt("maxcounter", instance.getMaxFearCounter());
        return nbt;
    }

    @Override
    public void readNBT(Capability<IDeathFear> capability, IDeathFear instance, Direction side, INBT nbt) {
        CompoundNBT compound = (CompoundNBT) nbt;
        instance.setFear(compound.getString("fear"));
        instance.setFearCounter(compound.getInt("counter"));
        instance.setMaxFearCounter(compound.getInt("maxcounter"));
    }
}
