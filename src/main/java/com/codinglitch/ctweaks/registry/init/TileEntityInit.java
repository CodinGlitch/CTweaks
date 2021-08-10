package com.codinglitch.ctweaks.registry.init;

import com.codinglitch.ctweaks.registry.blocks.ModifiedJukebox;
import com.codinglitch.ctweaks.registry.tileentities.ModifiedJukeboxTileEntity;
import com.codinglitch.ctweaks.util.ReferenceC;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class TileEntityInit {
    public static final DeferredRegister<TileEntityType<?>> TILEENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ReferenceC.MODID);

    public static void registerBus()
    {
        TILEENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<TileEntityType<ModifiedJukeboxTileEntity>> JUKEBOX_MODIFIED = TILEENTITIES.register("jukebox",
            () -> TileEntityType.Builder.of(ModifiedJukeboxTileEntity::new, BlocksInit.JUKEBOX_MODIFIED.get()).build(null)
    );
}
