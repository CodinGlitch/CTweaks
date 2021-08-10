package com.codinglitch.ctweaks.client.layers;

import com.codinglitch.ctweaks.client.model.FoxModelModified;
import com.codinglitch.ctweaks.registry.entities.FoxEntityModified;
import com.codinglitch.ctweaks.util.ReferenceC;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.FoxModel;
import net.minecraft.client.renderer.entity.model.WolfModel;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FoxCollarLayer extends LayerRenderer<FoxEntityModified, FoxModelModified<FoxEntityModified>> {

    private static final ResourceLocation FOX_COLLAR_LOCATION = new ResourceLocation(ReferenceC.MODID, "textures/entity/fox_collar.png");

    public FoxCollarLayer(IEntityRenderer<FoxEntityModified, FoxModelModified<FoxEntityModified>> foxModel) {
        super(foxModel);
    }

    public void render(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int p_225628_3_, FoxEntityModified foxEntity, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
        if (foxEntity.isTame() && !foxEntity.isInvisible()) {
            float[] afloat = foxEntity.getCollarColor().getTextureDiffuseColors();
            renderColoredCutoutModel(this.getParentModel(), FOX_COLLAR_LOCATION, matrixStack, renderTypeBuffer, p_225628_3_, foxEntity, afloat[0], afloat[1], afloat[2]);
        }
    }
}
