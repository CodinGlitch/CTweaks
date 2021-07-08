package com.codinglitch.ctweaks.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static ForgeConfigSpec.BooleanValue trauma_effect;

    public static void init(ForgeConfigSpec.Builder client)
    {
        client.comment("Disable features");

        trauma_effect = client
                .comment("Disable trauma effects")
                .define("features.trauma_effect", false);
    }
}
