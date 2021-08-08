package com.codinglitch.ctweaks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SnowGolemRenderer;
import net.minecraft.world.entity.animal.SnowGolem;

public class GreaterSnowManRenderer extends SnowGolemRenderer {

    public GreaterSnowManRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SnowGolem entity, float f, float f1, PoseStack matrixStack, MultiBufferSource buffer, int i) {
        matrixStack.scale(2,2,2);
        super.render(entity, f, f1, matrixStack, buffer, i);
    }
}
