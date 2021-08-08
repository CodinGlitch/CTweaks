package com.codinglitch.ctweaks.client.renderer;

import com.codinglitch.ctweaks.registry.entities.IllusionerGeyser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.EvokerFangsModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IllusionerGeyserRenderer extends EntityRenderer<IllusionerGeyser> {

    public IllusionerGeyserRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(IllusionerGeyser entity, float p_114529_, float p_114530_, PoseStack p_114531_, MultiBufferSource p_114532_, int p_114533_) {

    }

    public ResourceLocation getTextureLocation(IllusionerGeyser entity) {
        return null;
    }
}