package com.codinglitch.ctweaks.registry.init;

import com.codinglitch.ctweaks.util.ReferenceC;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ParticlesInit {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ReferenceC.MODID);

    public static void registerBus()
    {
        PARTICLES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<SimpleParticleType> BUBBLE_PERSIST = PARTICLES.register("bubble_persist",
            () -> new SimpleParticleType(true)
    );
}
