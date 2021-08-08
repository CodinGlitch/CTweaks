package com.codinglitch.ctweaks.registry.init;

import com.codinglitch.ctweaks.registry.tileentities.ModifiedJukeboxBlockEntity;
import com.codinglitch.ctweaks.util.ReferenceC;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityInit {
    public static final DeferredRegister<BlockEntityType<?>> BLOCKENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, ReferenceC.MODID);

    public static void registerBus()
    {
        BLOCKENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<BlockEntityType<ModifiedJukeboxBlockEntity>> JUKEBOX_MODIFIED = BLOCKENTITIES.register("jukebox",
            () -> BlockEntityType.Builder.of(ModifiedJukeboxBlockEntity::new, Blocks.DAYLIGHT_DETECTOR).build(null)
    );


}
