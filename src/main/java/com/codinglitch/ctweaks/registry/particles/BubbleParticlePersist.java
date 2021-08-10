package com.codinglitch.ctweaks.registry.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class BubbleParticlePersist extends TextureSheetParticle {
    BubbleParticlePersist(ClientLevel level, double xo, double yo, double zo, double xd, double yd, double zd) {
        super(level, xo, yo, zo);
        this.setSize(0.02F, 0.02F);
        this.quadSize *= this.random.nextFloat() * 0.6F + 0.2F;
        this.xd = xd * (double)0.2F + (Math.random() * 2.0D - 1.0D) * (double)0.02F;
        this.yd = yd * (double)0.2F + (Math.random() * 2.0D - 1.0D) * (double)0.02F;
        this.zd = zd * (double)0.2F + (Math.random() * 2.0D - 1.0D) * (double)0.02F;
        this.lifetime = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
        } else {
            this.yd += 0.002D;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.85F;
            this.yd *= 0.85F;
            this.zd *= 0.85F;
        }
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double xo, double yo, double zo, double xd, double yd, double zd) {
            BubbleParticlePersist bubbleparticle = new BubbleParticlePersist(level, xo, yo, zo, xd, yd, zd);
            bubbleparticle.pickSprite(this.sprite);
            return bubbleparticle;
        }
    }
}
