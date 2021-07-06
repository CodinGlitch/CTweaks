
package com.codinglitch.ctweaks.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class UtilityC {
    public static List<Item> registeredCatalysts = new ArrayList<>();

    public UtilityC() {
    }

    public static void registerCatalyst(Item item)
    {
        registeredCatalysts.add(item);
    }

    public static float interpolate(float from, float to, float normal) {
        return from + (to - from) * normal;
    }

    public static double interpolate(double from, double to, double normal) {
        return from + (to - from) * normal;
    }

    public static BlockPos interpolate(BlockPos from, BlockPos to, double normal) {
        return new BlockPos((double)from.getX() + (double)(to.getX() - from.getX()) * normal, (double)from.getY() + (double)(to.getY() - from.getY()) * normal, (double)from.getZ() + (double)(to.getZ() - from.getZ()) * normal);
    }

    public static Vector3d interpolate(Vector3d from, Vector3d to, double normal) {
        return new Vector3d(from.x + (to.x - from.x) * normal, from.y + (to.y - from.y) * normal, from.z + (to.z - from.z) * normal);
    }

    public static Color interpolate(Color from, Color to, double normal) {
        return new Color((int)((double)from.getRed() + (double)(to.getRed() - from.getRed()) * normal), (int)((double)from.getGreen() + (double)(to.getGreen() - from.getGreen()) * normal), (int)((double)from.getBlue() + (double)(to.getBlue() - from.getBlue()) * normal));
    }

    public static float clamp(float num, float min, float max) {
        return Math.min(max, Math.max(num, min));
    }

    public static class Constants {
        public static final double AABBPixel = 0.0625D;
    }

    public static class MathUtility {
        public MathUtility() {
        }

        public static float easeOutQuad(float normal) {
            return 1.0F - (1.0F - normal) * (1.0F - normal);
        }

        public static float easeOutCubic(float normal) {
            return (float)(1.0D - Math.pow(1.0F - normal, 3.0D));
        }

        public static float clamp(float num, float min, float max) {
            return Math.max(Math.min(num, max), min);
        }
    }

    public static class DamageSources {
        //public static DamageSource TENDRIL = new DamageSource("void_tendril");

        public DamageSources() {
        }
    }
}
