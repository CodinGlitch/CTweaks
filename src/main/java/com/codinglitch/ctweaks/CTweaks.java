package com.codinglitch.ctweaks;

import com.codinglitch.ctweaks.client.renderer.GreaterSnowManRenderer;
import com.codinglitch.ctweaks.client.renderer.IllusionerGeyserRenderer;
import com.codinglitch.ctweaks.config.CConfig;
import com.codinglitch.ctweaks.registry.CommonInit;
import com.codinglitch.ctweaks.registry.blockentities.StewCauldronBlockEntity;
import com.codinglitch.ctweaks.registry.entities.GreaterSnowGolem;
import com.codinglitch.ctweaks.registry.entities.IllusionerModified;
import com.codinglitch.ctweaks.registry.entities.PolarBearEntityModified;
import com.codinglitch.ctweaks.registry.init.*;
import com.codinglitch.ctweaks.registry.particles.BubbleParticlePersist;
import com.codinglitch.ctweaks.util.SoundsC;
import com.codinglitch.ctweaks.util.StewUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.IllusionerRenderer;
import net.minecraft.client.renderer.entity.PolarBearRenderer;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

@Mod(
        value = "ctweaks"
)
public class CTweaks {
    public static CTweaks instance;
    public static final Logger logger = LogManager.getLogger();

    public CTweaks() {
        instance = this;

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CConfig.config);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CConfig.client_config);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        MinecraftForge.EVENT_BUS.register(this);

        BlocksInit.registerBus();
        ItemsInit.registerBus();
        BlockEntityInit.registerBus();
        EntityInit.registerBus();
        ParticlesInit.registerBus();
        SoundsC.registerBus();
    }

    private void setup(FMLCommonSetupEvent event)
    {
        CommonInit.init();
        ComposterBlock.COMPOSTABLES.put(Items.ROTTEN_FLESH.asItem(), 2);
        ComposterBlock.COMPOSTABLES.put(Items.BAMBOO.asItem(), 2);
        ComposterBlock.COMPOSTABLES.put(Items.BEEF.asItem(), 3);
        ComposterBlock.COMPOSTABLES.put(Items.PORKCHOP.asItem(), 3);
        ComposterBlock.COMPOSTABLES.put(Items.CHICKEN.asItem(), 3);
        ComposterBlock.COMPOSTABLES.put(Items.MUTTON.asItem(), 2);
        ComposterBlock.COMPOSTABLES.put(Items.TROPICAL_FISH.asItem(), 2);
        ComposterBlock.COMPOSTABLES.put(Items.SALMON.asItem(), 2);
        ComposterBlock.COMPOSTABLES.put(Items.COD.asItem(), 2);
        ComposterBlock.COMPOSTABLES.put(Items.RABBIT.asItem(), 2);
        ComposterBlock.COMPOSTABLES.put(Items.LEATHER.asItem(), 3);
        ComposterBlock.COMPOSTABLES.put(Items.RABBIT_HIDE.asItem(), 2);
        ComposterBlock.COMPOSTABLES.put(Items.RABBIT_FOOT.asItem(), 2);
        ComposterBlock.COMPOSTABLES.put(Items.COOKED_PORKCHOP.asItem(), 3);
        ComposterBlock.COMPOSTABLES.put(Items.COOKED_CHICKEN.asItem(), 3);
        ComposterBlock.COMPOSTABLES.put(Items.COOKED_MUTTON.asItem(), 3);
        ComposterBlock.COMPOSTABLES.put(Items.COOKED_SALMON.asItem(), 3);
        ComposterBlock.COMPOSTABLES.put(Items.COOKED_COD.asItem(), 2);
        ComposterBlock.COMPOSTABLES.put(Items.COOKED_RABBIT.asItem(), 2);
        ComposterBlock.COMPOSTABLES.put(Items.CHORUS_FLOWER.asItem(), 3);
        ComposterBlock.COMPOSTABLES.put(Items.CHORUS_FRUIT.asItem(), 3);
        ComposterBlock.COMPOSTABLES.put(Items.CHORUS_PLANT.asItem(), 3);
        ComposterBlock.COMPOSTABLES.put(Items.POPPED_CHORUS_FRUIT.asItem(), 3);
        ComposterBlock.COMPOSTABLES.put(Items.EGG.asItem(), 2);
        ComposterBlock.COMPOSTABLES.put(Items.PUFFERFISH.asItem(), 2);

        Raid.RaiderType.create("illusioner", EntityInit.ILLUSIONER_MODIFIED.get(), new int[]{0, 0, 0, 0, 1, 0, 2, 3});
    }

    private void doClientStuff(FMLClientSetupEvent event) {
        EntityRenderers.register(EntityInit.POLAR_BEAR_MODIFIED.get(), PolarBearRenderer::new);
        EntityRenderers.register(EntityInit.ILLUSIONER_MODIFIED.get(), IllusionerRenderer::new);
        EntityRenderers.register(EntityInit.GREATER_SNOW_GOLEM.get(), GreaterSnowManRenderer::new);
        EntityRenderers.register(EntityInit.ILLUSIONER_GEYSER.get(), IllusionerGeyserRenderer::new);
    }

    private void enqueueIMC(InterModEnqueueEvent event)
    {
    }

    private void processIMC(InterModProcessEvent event)
    {
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(EntityInit.POLAR_BEAR_MODIFIED.get(), PolarBearEntityModified.createAttributes().build());
            event.put(EntityInit.ILLUSIONER_MODIFIED.get(), IllusionerModified.createAttributes().build());
            event.put(EntityInit.GREATER_SNOW_GOLEM.get(), GreaterSnowGolem.createAttributes().build());
        }
        @SubscribeEvent
        public static void registerParticle(ParticleFactoryRegisterEvent event) {
            Minecraft.getInstance().particleEngine.register(ParticlesInit.BUBBLE_PERSIST.get(), BubbleParticlePersist.Provider::new);
        }
        @SubscribeEvent
        public static void registerBlockColors(ColorHandlerEvent.Block event){
            event.getBlockColors().register((state, blockAndTintGetter, pos, index) -> {
                if (pos != null & blockAndTintGetter != null)
                {
                    BlockEntity blockEntity = blockAndTintGetter.getBlockEntity(pos);
                    if (blockEntity instanceof StewCauldronBlockEntity cauldron)
                    {
                        Color color = StewUtils.calculateColors(cauldron.ingredients);
                        return color.getRGB();
                    }
                }
                return 0xeeaa55;
            }, BlocksInit.STEW_CAULDRON.get());
        }
        @SubscribeEvent
        public static void registerItemColors(ColorHandlerEvent.Item event){
            event.getItemColors().register((itemStack, index) -> index > 0 ? -1 : StewUtils.calculateColors(StewUtils.getIngredients(itemStack)).getRGB(), ItemsInit.STEW.get());
        }
    }
}
