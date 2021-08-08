package com.codinglitch.ctweaks.registry.items;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.DiggerItem;

import java.util.Set;

import net.minecraft.world.item.Item.Properties;

public class ScytheItem extends DiggerItem {
    public ScytheItem(float damage, float speed, Tier tier, Properties properties) {
        super(damage, speed, tier, BlockTags.MINEABLE_WITH_HOE, properties);
    }
}
