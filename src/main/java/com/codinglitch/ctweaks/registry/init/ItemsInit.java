package com.codinglitch.ctweaks.registry.init;

import com.codinglitch.ctweaks.registry.items.ScytheItem;
import com.codinglitch.ctweaks.util.ReferenceC;
import net.minecraft.block.Block;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemTier;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
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

    public static final RegistryObject<Item> WILTED_WHEAT = register(ITEMS, "wilted_wheat", () -> new Item(new Item.Properties().tab(ItemGroup.TAB_FOOD)));
    public static final RegistryObject<Item> CRUMBLING_CARROT = register(ITEMS, "crumbling_carrot", () -> new Item(new Item.Properties().tab(ItemGroup.TAB_FOOD)));

    public static final RegistryObject<Item> WOODEN_SCYTHE = register(ITEMS, "wooden_scythe", () ->
            new ScytheItem(
                    1,
                    -1.6f,
                    ItemTier.WOOD,
                    new HashSet<>(),
                    new Item.Properties().tab(ItemGroup.TAB_TOOLS)
            )
    );
    public static final RegistryObject<Item> STONE_SCYTHE = register(ITEMS, "stone_scythe", () ->
            new ScytheItem(
                    1,
                    -1.6f,
                    ItemTier.STONE,
                    new HashSet<>(),
                    new Item.Properties().tab(ItemGroup.TAB_TOOLS)
            )
    );
    public static final RegistryObject<Item> IRON_SCYTHE = register(ITEMS, "iron_scythe", () ->
            new ScytheItem(
                    1,
                    -1.7f,
                    ItemTier.IRON,
                    new HashSet<>(),
                    new Item.Properties().tab(ItemGroup.TAB_TOOLS)
            )
    );
    public static final RegistryObject<Item> GOLDEN_SCYTHE = register(ITEMS, "golden_scythe", () ->
            new ScytheItem(
                    2,
                    -1.5f,
                    ItemTier.GOLD,
                    new HashSet<>(),
                    new Item.Properties().tab(ItemGroup.TAB_TOOLS)
            )
    );
    public static final RegistryObject<Item> DIAMOND_SCYTHE = register(ITEMS, "diamond_scythe", () ->
            new ScytheItem(
                    1,
                    -1.8f,
                    ItemTier.DIAMOND,
                    new HashSet<>(),
                    new Item.Properties().tab(ItemGroup.TAB_TOOLS)
            )
    );
    public static final RegistryObject<Item> NETHERITE_SCYTHE = register(ITEMS, "netherite_scythe", () ->
            new ScytheItem(
                    1,
                    -2f,
                    ItemTier.NETHERITE,
                    new HashSet<>(),
                    new Item.Properties().tab(ItemGroup.TAB_TOOLS)
            )
    );
}
