package com.codinglitch.venerations.registry;

import com.codinglitch.venerations.registry.init.CapabilitiesInit;
import com.codinglitch.venerations.util.network.CTweaksPacketHandler;

public class CommonInit {
    public static void init()
    {
        CapabilitiesInit.initCapabilities();
        CTweaksPacketHandler.init();
    }
}
