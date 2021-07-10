package com.codinglitch.ctweaks.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class DisableConfig {
    public static ForgeConfigSpec.BooleanValue axe_crit;
    public static ForgeConfigSpec.BooleanValue trauma;
    public static ForgeConfigSpec.BooleanValue tame_fox;
    public static ForgeConfigSpec.BooleanValue mount_polar_bear;
    public static ForgeConfigSpec.BooleanValue rotten_variant;
    public static ForgeConfigSpec.BooleanValue scythes;
    public static ForgeConfigSpec.BooleanValue enchantingpatch;

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
                .comment("Disable tameable foxes (will require world restart if you have found a fox)")
                .comment("(else kill the fox before disabling)")
                .define("features.tame_fox", false);

        mount_polar_bear = common
                .comment("Disable mountable polar bears (will require world restart if you have found a polar bear)")
                .comment("(else kill the polar bear before disabling)")
                .define("features.mount_polar_bear", false);

        rotten_variant = common
                .comment("Disable the rotten variant of crops")
                .define("features.rotten", false);

        scythes = common
                .comment("Disable scythes")
                .define("features.scythes", false);

        enchantingpatch = common
                .comment("Disable the enchanting patch with anvils and enchanting tables")
                .define("features.enchanting", false);
    }
}
