package com.codinglitch.ctweaks.registry.init;

import com.codinglitch.ctweaks.util.ReferenceC;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ItemsInit {
    public static final DeferredRegister<Item> OVERRIDE_VANILLA_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "minecraft");
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ReferenceC.MODID);

    public static void registerBus()
    {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        OVERRIDE_VANILLA_ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static <T extends Item > RegistryObject<T> register(DeferredRegister<Item> reg, String name, Supplier<T> item)
    {
        return reg.register(name, item);
    }
}
