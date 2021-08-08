package com.codinglitch.ctweaks.registry.init;

import com.codinglitch.ctweaks.registry.entities.*;
import com.codinglitch.ctweaks.util.ReferenceC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, ReferenceC.MODID);

    public static void registerBus()
    {
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.Builder<T> builder)
    {
        return ENTITIES.register(name, () -> builder.build(new ResourceLocation(ReferenceC.MODID, name).toString()));
    }

    public static final RegistryObject<EntityType<PolarBearEntityModified>> POLAR_BEAR_MODIFIED = register("polar_bear",
            EntityType.Builder.<PolarBearEntityModified>of(PolarBearEntityModified::new, MobCategory.CREATURE)
                    .sized(1.4F, 1.4F)
                    .clientTrackingRange(10)
    );

    public static final RegistryObject<EntityType<IllusionerModified>> ILLUSIONER_MODIFIED = register("illusioner",
            EntityType.Builder.<IllusionerModified>of(IllusionerModified::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
    );

    public static final RegistryObject<EntityType<GreaterSnowGolem>> GREATER_SNOW_GOLEM = register("greater_snow_golem",
            EntityType.Builder.<GreaterSnowGolem>of(GreaterSnowGolem::new, MobCategory.MISC)
                    .sized(0.7F, 2F)
                    .clientTrackingRange(8)
    );

    public static final RegistryObject<EntityType<IllusionerGeyser>> ILLUSIONER_GEYSER = register("illusioner_geyser",
            EntityType.Builder.<IllusionerGeyser>of(IllusionerGeyser::new, MobCategory.MISC)
                    .sized(0.5F, 0.8F)
                    .clientTrackingRange(6)
                    .updateInterval(2)
    );
}
