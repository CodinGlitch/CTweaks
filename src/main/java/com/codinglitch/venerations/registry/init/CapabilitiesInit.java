package com.codinglitch.venerations.registry.init;

import com.codinglitch.venerations.registry.capabilities.DeathFear;
import com.codinglitch.venerations.registry.capabilities.DeathFearStorage;
import com.codinglitch.venerations.registry.capabilities.IDeathFear;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilitiesInit {

    public static void initCapabilities()
    {
        CapabilityManager.INSTANCE.register(IDeathFear.class, new DeathFearStorage(), DeathFear::new);
    }
}
