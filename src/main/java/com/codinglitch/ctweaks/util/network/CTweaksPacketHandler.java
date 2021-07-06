package com.codinglitch.ctweaks.util.network;

import com.codinglitch.ctweaks.registry.capabilities.DeathFearProvider;
import com.codinglitch.ctweaks.registry.capabilities.IDeathFear;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class CTweaksPacketHandler {
    public static int discriminator = 99;

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("ctweaks", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init()
    {
        INSTANCE.registerMessage(discriminator, SyncFear.class, SyncFear::encode, SyncFear::new, SyncFear::handle);
        discriminator++;
    }

    public static class SyncFear
    {
        private String fear = "";
        private int counter = 0;
        private int maxCounter = 0;

        SyncFear(final PacketBuffer packetBuffer) {
            this.fear = packetBuffer.readUtf();
            this.counter = packetBuffer.readInt();
            this.maxCounter = packetBuffer.readInt();
        }

        public SyncFear(String fear, int counter, int maxcounter) {
            this.fear = fear;
            this.counter = counter;
            this.maxCounter = maxcounter;
        }

        void encode(final PacketBuffer packetBuffer) {
            packetBuffer.writeUtf(fear);
            packetBuffer.writeInt(counter);
            packetBuffer.writeInt(maxCounter);
        }


        public static void handle(SyncFear msg, Supplier<NetworkEvent.Context> ctx) {
            NetworkEvent.Context context = ctx.get();
            if (context.getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                ctx.get().enqueueWork(() -> {
                    ClientPlayerEntity player = Minecraft.getInstance().player;
                    LazyOptional<IDeathFear> capability1 = player.getCapability(DeathFearProvider.capability);

                    if (capability1.isPresent())
                    {
                        IDeathFear cap = capability1.orElseThrow(IllegalArgumentException::new);
                        cap.setFear(msg.fear);
                        cap.setFearCounter(msg.counter);
                        cap.setMaxFearCounter(msg.maxCounter);
                    }
                });
                ctx.get().setPacketHandled(true);
            }
        }
    }
}
