package com.codinglitch.ctweaks.registry.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class ElectricityParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    ElectricityParticle(ClientLevel level, double xo, double yo, double zo, double xd, double yd, double zd, SpriteSet spriteSet) {
        super(level, xo, yo, zo, xd, yd, zd);
        this.friction = 0.96F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = spriteSet;
        this.quadSize *= 0.75F;
        this.hasPhysics = false;
        this.setSpriteFromAge(spriteSet);
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public int getLightColor(float num) {
        float f = ((float)this.age + num) / (float)this.lifetime;
        f = Mth.clamp(f, 0.0F, 1.0F);
        int i = super.getLightColor(num);
        int j = i & 255;
        int k = i >> 16 & 255;
        j = j + (int)(f * 15.0F * 16.0F);
        if (j > 240) {
            j = 240;
        }

        return j | k << 16;
    }

    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet p_172151_) {
            this.sprite = p_172151_;
        }

        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double xo, double yo, double zo, double xd, double yd, double zd) {
            ElectricityParticle electricityParticle = new ElectricityParticle(level, xo, yo, zo, xd, yd, zd, this.sprite);
            electricityParticle.setColor(1.0F, 0.9F, 1.0F);
            electricityParticle.setParticleSpeed((level.random.nextFloat()-0.5f)*0.2f, (level.random.nextFloat()-0.5f)*0.2f, (level.random.nextFloat()-0.5f)*0.2f);
            electricityParticle.setLifetime(level.random.nextInt(5) + 2);
            return electricityParticle;
        }
    }
}
