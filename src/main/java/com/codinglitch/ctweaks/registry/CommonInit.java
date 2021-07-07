package com.codinglitch.ctweaks.registry;

import com.codinglitch.ctweaks.registry.init.BlocksInit;
import com.codinglitch.ctweaks.registry.init.CapabilitiesInit;
import com.codinglitch.ctweaks.util.ReferenceC;
import com.codinglitch.ctweaks.util.SoundsC;
import com.codinglitch.ctweaks.util.network.CTweaksPacketHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Iterator;

public class CommonInit {
    public static void init()
    {
        CapabilitiesInit.initCapabilities();
        CTweaksPacketHandler.init();
    }
}
