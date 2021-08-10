package com.codinglitch.ctweaks.registry.entities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class FoxModified extends FoxCopy {
    public FoxModified(EntityType<? extends FoxCopy> type, Level level) {
        super(type, level);
    }
}
