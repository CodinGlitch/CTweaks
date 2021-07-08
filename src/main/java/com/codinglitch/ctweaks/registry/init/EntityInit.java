package com.codinglitch.ctweaks.registry.init;

import com.codinglitch.ctweaks.registry.entities.FoxEntityModified;
import com.codinglitch.ctweaks.registry.entities.PolarBearEntityModified;
import com.codinglitch.ctweaks.registry.tileentities.ModifiedJukeboxTileEntity;
import com.codinglitch.ctweaks.util.ReferenceC;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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

    public static final RegistryObject<EntityType<FoxEntityModified>> FOX_MODIFIED = register("fox",
            EntityType.Builder.of(FoxEntityModified::new, EntityClassification.CREATURE)
                    .sized(0.6F, 0.7F)
                    .clientTrackingRange(8)
                    .immuneTo(Blocks.SWEET_BERRY_BUSH)
    );

    public static final RegistryObject<EntityType<PolarBearEntityModified>> POLAR_BEAR_MODIFIED = register("polar_bear",
            EntityType.Builder.of(PolarBearEntityModified::new, EntityClassification.CREATURE)
                    .sized(1.4F, 1.4F)
                    .clientTrackingRange(10)
    );
}
