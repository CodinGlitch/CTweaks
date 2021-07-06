package com.codinglitch.ctweaks.registry.init;

import com.codinglitch.ctweaks.registry.capabilities.DeathFear;
import com.codinglitch.ctweaks.registry.capabilities.DeathFearStorage;
import com.codinglitch.ctweaks.registry.capabilities.IDeathFear;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilitiesInit {

    public static void initCapabilities()
    {
        CapabilityManager.INSTANCE.register(IDeathFear.class, new DeathFearStorage(), DeathFear::new);
    }
}
