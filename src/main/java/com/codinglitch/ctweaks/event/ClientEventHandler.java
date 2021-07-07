package com.codinglitch.ctweaks.event;

import com.codinglitch.ctweaks.CTweaks;
import com.codinglitch.ctweaks.registry.capabilities.DeathFearProvider;
import com.codinglitch.ctweaks.registry.capabilities.IDeathFear;
import com.codinglitch.ctweaks.util.ReferenceC;
import com.codinglitch.ctweaks.util.UtilityC;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
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
        ClientPlayerEntity player = Minecraft.getInstance().player;

        event.getMatrixStack().translate(Math.sin((player.tickCount+event.getPartialTicks())*4 + 0.5f)*alpha/80,Math.sin((player.tickCount+event.getPartialTicks())*3)*alpha/40,0);
    }

    @SubscribeEvent
    public static void onRenderScreen(RenderGameOverlayEvent.Post event)
    {

        if (event.getType() != RenderGameOverlayEvent.ElementType.VIGNETTE) return;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder renderer = tessellator.getBuilder();

        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();

        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();

        RenderSystem.alphaFunc(516,0);

        Minecraft.getInstance().textureManager.bind(vignette);

        ClientPlayerEntity player = Minecraft.getInstance().player;
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

        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
        renderer.vertex(0.0D, height, -90).color(1,1,1, alpha).uv(0.0F, 1.0F).endVertex();
        renderer.vertex(width, height, -90).color(1,1,1, alpha).uv(1.0F, 1.0F).endVertex();
        renderer.vertex(width, 0.0D, -90).color(1,1,1, alpha).uv(1.0F, 0.0F).endVertex();
        renderer.vertex(0.0D, 0.0D, -90).color(1,1,1, alpha).uv(0.0F, 0.0F).endVertex();
        tessellator.end();

        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.defaultAlphaFunc();
    }
}
