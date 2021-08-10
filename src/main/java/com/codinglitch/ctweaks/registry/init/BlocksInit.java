package com.codinglitch.ctweaks.registry.init;

import com.codinglitch.ctweaks.registry.blocks.ModifiedJukebox;
import com.codinglitch.ctweaks.util.ReferenceC;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class BlocksInit {
    public static final DeferredRegister<Block> OVERRIDE_VANILLA_BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "minecraft");
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ReferenceC.MODID);

    public static <T extends Block> RegistryObject<T> registerWithoutItem(DeferredRegister<Block> reg, String name, Supplier<T> block)
    {
        return reg.register(name, block);
    }

    public static <T extends Block> RegistryObject<T> register(DeferredRegister<Block> reg, DeferredRegister<Item> reg1, String name, Supplier<T> block)
    {
        RegistryObject<T> registryObject = registerWithoutItem(reg, name, block);
        ItemsInit.register(reg1, name, () -> new BlockItem(registryObject.get(), new Item.Properties().tab(ItemGroup.TAB_MISC)));
        return registryObject;
    }

    public static void registerBus()
    {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        OVERRIDE_VANILLA_BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<Block> JUKEBOX_MODIFIED = register(OVERRIDE_VANILLA_BLOCKS, ItemsInit.OVERRIDE_VANILLA_ITEMS, "jukebox", () -> new ModifiedJukebox());
}
