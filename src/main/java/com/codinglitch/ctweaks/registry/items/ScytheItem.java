package com.codinglitch.ctweaks.registry.items;

import net.minecraft.block.Block;
import net.minecraft.item.IItemTier;
import net.minecraft.item.TieredItem;
import net.minecraft.item.ToolItem;

import java.util.Set;

public class ScytheItem extends ToolItem {
    public ScytheItem(float damage, float speed, IItemTier tier, Set<Block> diggables, Properties properties) {
        super(damage, speed, tier, diggables, properties);
    }
}
