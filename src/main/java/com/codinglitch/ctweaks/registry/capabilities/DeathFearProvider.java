package com.codinglitch.ctweaks.registry.capabilities;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DeathFearProvider implements ICapabilitySerializable<INBT> {
    @CapabilityInject(IDeathFear.class)
    public static final Capability<IDeathFear> capability = null;

    private LazyOptional<IDeathFear> instance = LazyOptional.of(capability::getDefaultInstance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return capability.orEmpty(cap, instance);
    }

    @Override
    public INBT serializeNBT() {
        return capability.getStorage().writeNBT(capability, instance.orElseThrow(() -> new IllegalArgumentException("")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        capability.getStorage().readNBT(capability, instance.orElseThrow(() -> new IllegalArgumentException("")), null, nbt);
    }

}
