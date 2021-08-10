package com.codinglitch.ctweaks.registry.init;

import com.codinglitch.ctweaks.registry.blocks.ElectricFence;
import com.codinglitch.ctweaks.registry.blocks.StewCauldron;
import com.codinglitch.ctweaks.registry.blocks.ModifiedJukebox;
import com.codinglitch.ctweaks.util.ReferenceC;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
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
        ItemsInit.register(reg1, name, () -> new BlockItem(registryObject.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
        return registryObject;
    }

    public static void registerBus()
    {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        OVERRIDE_VANILLA_BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<Block> JUKEBOX_MODIFIED = register(OVERRIDE_VANILLA_BLOCKS, ItemsInit.OVERRIDE_VANILLA_ITEMS, "jukebox", ModifiedJukebox::new);
    public static final RegistryObject<Block> STEW_CAULDRON = registerWithoutItem(BLOCKS, "stew_cauldron", StewCauldron::new);
    public static final RegistryObject<Block> ELECTRIC_FENCE = register(BLOCKS, ItemsInit.ITEMS, "electric_fence", ElectricFence::new);
}
