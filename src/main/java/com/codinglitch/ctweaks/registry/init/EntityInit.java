package com.codinglitch.ctweaks.registry.init;

import com.codinglitch.ctweaks.registry.entities.FoxEntityModified;
import com.codinglitch.ctweaks.registry.tileentities.ModifiedJukeboxTileEntity;
import com.codinglitch.ctweaks.util.ReferenceC;
import net.minecraft.block.Blocks;
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
    public static final DeferredRegister<EntityType<?>> ENTITIES_OVERRIDE_VANILLA = DeferredRegister.create(ForgeRegistries.ENTITIES, "minecraft");

    public static void registerBus()
    {
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<EntityType<FoxEntityModified>> FOX_MODIFIED = ENTITIES.register("fox",
            () -> EntityType.Builder.of(FoxEntityModified::new, EntityClassification.CREATURE)
                            .sized(0.6F, 0.7F)
                            .clientTrackingRange(8)
                            .immuneTo(Blocks.SWEET_BERRY_BUSH)
                    .build(new ResourceLocation(ReferenceC.MODID, "fox").toString()
                    )
    );
}
