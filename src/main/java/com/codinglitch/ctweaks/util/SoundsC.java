
package com.codinglitch.ctweaks.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class SoundsC {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ReferenceC.MODID);

    public SoundsC() {
    }

    public static void registerBus() {
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private static RegistryObject<SoundEvent> registerSound(String name) {
        SoundEvent soundevent = new SoundEvent(new ResourceLocation(ReferenceC.MODID, name));
        return SOUNDS.register(name, () -> soundevent);
    }

    public static RegistryObject<SoundEvent> fox_squeak = registerSound("fox_squeak");
    public static RegistryObject<SoundEvent> wet_sponge = registerSound("wet_sponge");
    public static RegistryObject<SoundEvent> sponge = registerSound("sponge");
    public static RegistryObject<SoundEvent> illusioner_attack = registerSound("illusioner_attack");
    public static RegistryObject<SoundEvent> illusioner_geyser = registerSound("illusioner_geyser");
}
