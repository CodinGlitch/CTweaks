package com.codinglitch.ctweaks.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class BehaviourConfig {
    public static ForgeConfigSpec.IntValue cook_time;
    public static void init(ForgeConfigSpec.Builder common)
    {
        common.comment("Behaviour settings");

        cook_time = common
                .comment("The cooking time for stew cauldrons (in ticks)")
                .defineInRange("behaviour.cook_time", 200,0, 10000);
    }
}
