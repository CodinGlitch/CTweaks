package com.codinglitch.ctweaks.client.renderer;

import com.codinglitch.ctweaks.client.layers.FoxCollarLayer;
import com.codinglitch.ctweaks.client.layers.FoxHeldItemLayerModified;
import com.codinglitch.ctweaks.client.model.FoxModelModified;
import com.codinglitch.ctweaks.registry.entities.FoxEntityModified;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.FoxRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.FoxHeldItemLayer;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.client.renderer.entity.model.FoxModel;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class FoxRendererModified extends MobRenderer<FoxEntityModified, FoxModelModified<FoxEntityModified>> {
    private static final ResourceLocation RED_FOX_TEXTURE = new ResourceLocation("textures/entity/fox/fox.png");
    private static final ResourceLocation RED_FOX_SLEEP_TEXTURE = new ResourceLocation("textures/entity/fox/fox_sleep.png");
    private static final ResourceLocation SNOW_FOX_TEXTURE = new ResourceLocation("textures/entity/fox/snow_fox.png");
    private static final ResourceLocation SNOW_FOX_SLEEP_TEXTURE = new ResourceLocation("textures/entity/fox/snow_fox_sleep.png");

    public FoxRendererModified(EntityRendererManager p_i50969_1_) {
        super(p_i50969_1_, new FoxModelModified<>(), 0.4F);
        this.addLayer(new FoxHeldItemLayerModified(this));
        this.addLayer(new FoxCollarLayer(this));
    }

    protected void setupRotations(FoxEntityModified p_225621_1_, MatrixStack p_225621_2_, float p_225621_3_, float p_225621_4_, float p_225621_5_) {
        super.setupRotations(p_225621_1_, p_225621_2_, p_225621_3_, p_225621_4_, p_225621_5_);
        if (p_225621_1_.isPouncing() || p_225621_1_.isFaceplanted()) {
            float f = -MathHelper.lerp(p_225621_5_, p_225621_1_.xRotO, p_225621_1_.xRot);
            p_225621_2_.mulPose(Vector3f.XP.rotationDegrees(f));
        }

    }

    public ResourceLocation getTextureLocation(FoxEntityModified p_110775_1_) {
        if (p_110775_1_.getFoxType() == FoxEntityModified.Type.RED) {
            return p_110775_1_.isSleeping() ? RED_FOX_SLEEP_TEXTURE : RED_FOX_TEXTURE;
        } else {
            return p_110775_1_.isSleeping() ? SNOW_FOX_SLEEP_TEXTURE : SNOW_FOX_TEXTURE;
        }
    }
}
