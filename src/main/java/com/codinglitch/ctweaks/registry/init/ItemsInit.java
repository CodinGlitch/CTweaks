package com.codinglitch.ctweaks.registry.init;

import com.codinglitch.ctweaks.registry.items.ScytheItem;
import com.codinglitch.ctweaks.registry.items.StewItem;
import com.codinglitch.ctweaks.util.ReferenceC;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
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

    public static final RegistryObject<Item> WILTED_WHEAT = register(ITEMS, "wilted_wheat", () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_FOOD)));
    public static final RegistryObject<Item> CRUMBLING_CARROT = register(ITEMS, "crumbling_carrot", () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_FOOD)));

    public static final RegistryObject<Item> WOODEN_SCYTHE = register(ITEMS, "wooden_scythe", () ->
            new ScytheItem(
                    1,
                    -1.6f,
                    Tiers.WOOD,
                    new Item.Properties().tab(CreativeModeTab.TAB_TOOLS)
            )
    );
    public static final RegistryObject<Item> STONE_SCYTHE = register(ITEMS, "stone_scythe", () ->
            new ScytheItem(
                    1,
                    -1.6f,
                    Tiers.STONE,
                    new Item.Properties().tab(CreativeModeTab.TAB_TOOLS)
            )
    );
    public static final RegistryObject<Item> IRON_SCYTHE = register(ITEMS, "iron_scythe", () ->
            new ScytheItem(
                    1,
                    -1.7f,
                    Tiers.IRON,
                    new Item.Properties().tab(CreativeModeTab.TAB_TOOLS)
            )
    );
    public static final RegistryObject<Item> GOLDEN_SCYTHE = register(ITEMS, "golden_scythe", () ->
            new ScytheItem(
                    2,
                    -1.5f,
                    Tiers.GOLD,
                    new Item.Properties().tab(CreativeModeTab.TAB_TOOLS)
            )
    );
    public static final RegistryObject<Item> DIAMOND_SCYTHE = register(ITEMS, "diamond_scythe", () ->
            new ScytheItem(
                    1,
                    -1.8f,
                    Tiers.DIAMOND,
                    new Item.Properties().tab(CreativeModeTab.TAB_TOOLS)
            )
    );
    public static final RegistryObject<Item> NETHERITE_SCYTHE = register(ITEMS, "netherite_scythe", () ->
            new ScytheItem(
                    1,
                    -2f,
                    Tiers.NETHERITE,
                    new Item.Properties().tab(CreativeModeTab.TAB_TOOLS)
            )
    );

    public static final RegistryObject<Item> STEW = register(ITEMS, "stew", StewItem::new);
}
