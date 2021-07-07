package com.codinglitch.ctweaks;

import com.codinglitch.ctweaks.client.renderer.FoxRendererModified;
import com.codinglitch.ctweaks.config.CConfig;
import com.codinglitch.ctweaks.registry.CommonInit;
import com.codinglitch.ctweaks.registry.entities.FoxEntityModified;
import com.codinglitch.ctweaks.registry.init.BlocksInit;
import com.codinglitch.ctweaks.registry.init.EntityInit;
import com.codinglitch.ctweaks.registry.init.ItemsInit;
import com.codinglitch.ctweaks.registry.init.TileEntityInit;
import com.codinglitch.ctweaks.util.ReferenceC;
import com.codinglitch.ctweaks.util.SoundsC;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;

@Mod(
        value = "ctweaks"
)
public class CTweaks {
    public static CTweaks instance;
    public static final Logger logger = LogManager.getLogger();

    public CTweaks() {
        instance = this;

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CConfig.config);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        MinecraftForge.EVENT_BUS.register(this);

        BlocksInit.registerBus();
        ItemsInit.registerBus();
        TileEntityInit.registerBus();
        EntityInit.registerBus();
        SoundsC.registerBus();
    }

    private void setup(FMLCommonSetupEvent event)
    {
        CommonInit.init();
        ComposterBlock.COMPOSTABLES.put(Items.ROTTEN_FLESH.asItem(), 2);
    }

    private void doClientStuff(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityInit.FOX_MODIFIED.get(), FoxRendererModified::new);
    }

    private void enqueueIMC(InterModEnqueueEvent event)
    {
    }

    private void processIMC(InterModProcessEvent event)
    {
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(RegistryEvent.Register<Block> event) {

        }

        @SubscribeEvent
        public static void onAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(EntityInit.FOX_MODIFIED.get(), FoxEntityModified.createAttributes().build());
        }
    }
}
