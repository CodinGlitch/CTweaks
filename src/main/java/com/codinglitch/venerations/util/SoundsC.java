
package com.codinglitch.venerations.util;

import com.codinglitch.venerations.CTweaks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class SoundsC {
    //public static SoundEvent offer;

    public SoundsC() {
    }

    public static void registerSounds() {
        //offer = registerSound("offer_item");
    }

    private static SoundEvent registerSound(String name) {
        SoundEvent soundevent = new SoundEvent(new ResourceLocation("venerations", name));
        soundevent.setRegistryName(name);
        ForgeRegistries.SOUND_EVENTS.register(soundevent);
        return soundevent;
    }
}
