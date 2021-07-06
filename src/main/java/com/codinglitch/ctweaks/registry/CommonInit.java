package com.codinglitch.ctweaks.registry;

import com.codinglitch.ctweaks.registry.init.CapabilitiesInit;
import com.codinglitch.ctweaks.util.network.CTweaksPacketHandler;

public class CommonInit {
    public static void init()
    {
        CapabilitiesInit.initCapabilities();
        CTweaksPacketHandler.init();
    }
}
