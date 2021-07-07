package com.codinglitch.ctweaks.config;

import com.codinglitch.ctweaks.CTweaks;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.io.File;

@Mod.EventBusSubscriber
public class CConfig {
    private static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec config;

    static
    {
        DisableConfig.init(builder);
        config = builder.build();
    }

    public static void initConfig(ForgeConfigSpec config, String path)
    {
        CTweaks.logger.info("Initializing configurations; "+path);
        final CommentedFileConfig file = CommentedFileConfig.builder(new File(path))
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();
        CTweaks.logger.info("Successfully initialized configurations; "+path);

        file.load();

        CTweaks.logger.info("Configuration loaded;  "+path);

        config.setConfig(file);
    }
}
