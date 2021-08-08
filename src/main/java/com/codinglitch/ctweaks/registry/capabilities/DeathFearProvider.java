package com.codinglitch.ctweaks.registry.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DeathFearProvider implements ICapabilitySerializable<Tag> {
    @CapabilityInject(IDeathFear.class)
    public static final Capability<IDeathFear> capability = null;

    private LazyOptional<IDeathFear> instance = LazyOptional.of(DeathFear::new);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return capability.orEmpty(cap, instance);
    }

    @Override
    public Tag serializeNBT() {
        IDeathFear inst = instance.orElseThrow(() -> new IllegalArgumentException(""));
        CompoundTag nbt = new CompoundTag();
        nbt.putString("fear", inst.getFear());
        nbt.putInt("counter", inst.getFearCounter());
        nbt.putInt("maxcounter", inst.getMaxFearCounter());
        nbt.putLong("traumatizedwhen", inst.getTickWhenTraumatized());
        return nbt;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        IDeathFear inst = instance.orElseThrow(() -> new IllegalArgumentException(""));
        CompoundTag compound = (CompoundTag) nbt;
        inst.setFear(compound.getString("fear"));
        inst.setFearCounter(compound.getInt("counter"));
        inst.setMaxFearCounter(compound.getInt("maxcounter"));
        inst.setTickWhenTraumatized(compound.getLong("traumatizedwhen"));
    }

}
