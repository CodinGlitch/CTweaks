package com.codinglitch.ctweaks.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class DisableConfig {
    public static ForgeConfigSpec.BooleanValue axe_crit;
    public static ForgeConfigSpec.BooleanValue trauma;
    public static ForgeConfigSpec.BooleanValue tame_fox;
    public static ForgeConfigSpec.BooleanValue mount_polar_bear;

    public static void init(ForgeConfigSpec.Builder common)
    {
        common.comment("Disable features");

        axe_crit = common
                .comment("Disable axe crit multiplier by height")
                .define("features.axe_crit", false);

        trauma = common
                .comment("Disable trauma after death (may require world restart)")
                .define("features.trauma", false);

        tame_fox = common
                .comment("Disable tameable foxes (will require world restart)")
                .define("features.tame_fox", false);

        mount_polar_bear = common
                .comment("Disable mountable polar bears (will require world restart)")
                .define("features.mount_polar_bear", false);
    }
}
