package com.codinglitch.ctweaks.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class DisableConfig {
    public static ForgeConfigSpec.BooleanValue axe_crit;
    public static ForgeConfigSpec.BooleanValue trauma;

    public static void init(ForgeConfigSpec.Builder common)
    {
        common.comment("Disable features");

        axe_crit = common
                .comment("Disable axe crit multiplier by height")
                .define("features.axe_crit", false);

        trauma = common
                .comment("Disable trauma after death (may require world restart)")
                .define("features.trauma", false);
    }
}
