package com.codinglitch.ctweaks.event;

import com.codinglitch.ctweaks.config.ClientConfig;
import com.codinglitch.ctweaks.registry.capabilities.DeathFearProvider;
import com.codinglitch.ctweaks.registry.capabilities.IDeathFear;
import com.codinglitch.ctweaks.util.ReferenceC;
import com.codinglitch.ctweaks.util.UtilityC;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEventHandler {
    public static final ResourceLocation vignette = new ResourceLocation(ReferenceC.MODID,"textures/vignette.png");
    private static float alpha = 0;

    @SubscribeEvent
    public static void renderHand(RenderHandEvent event)
    {
        LocalPlayer player = Minecraft.getInstance().player;

        event.getMatrixStack().translate(Math.sin((player.tickCount+event.getPartialTicks())*4 + 0.5f)*alpha/80,Math.sin((player.tickCount+event.getPartialTicks())*3)*alpha/40,0);
    }

    @SubscribeEvent
    public static void onRenderScreen(RenderGameOverlayEvent.Post event)
    {
        if (ClientConfig.trauma_effect.get()) return;
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder renderer = tessellator.getBuilder();

        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();

        Minecraft.getInstance().textureManager.bindForSetup(vignette);

        LocalPlayer player = Minecraft.getInstance().player;
        LazyOptional<IDeathFear> capability1 = player.getCapability(DeathFearProvider.capability);

        if (capability1.isPresent())
        {
            IDeathFear cap = capability1.orElseThrow(IllegalArgumentException::new);

            if (cap.getMaxFearCounter() != 0)
            {
                float reversed = 1-event.getPartialTicks();
                alpha = UtilityC.interpolate(alpha, ((float)cap.getFearCounter()+reversed)/((float)cap.getMaxFearCounter()+reversed), 0.02f);
            }
            else
            {
                alpha = UtilityC.interpolate(alpha, 0, 0.02f);
            }
        }

        renderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        renderer.vertex(0.0D, height, -1).color(1,1,1, alpha).uv(0.0F, 1.0F).endVertex();
        renderer.vertex(width, height, -1).color(1,1,1, alpha).uv(1.0F, 1.0F).endVertex();
        renderer.vertex(width, 0.0D, -1).color(1,1,1, alpha).uv(1.0F, 0.0F).endVertex();
        renderer.vertex(0.0D, 0.0D, -1).color(1,1,1, alpha).uv(0.0F, 0.0F).endVertex();
        tessellator.end();

        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }
}
