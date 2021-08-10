
package com.codinglitch.ctweaks.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UtilityC {
    public static List<Item> registeredCatalysts = new ArrayList<>();

    public UtilityC() {
    }

    public static boolean isValidHeatSource(BlockPos pos, Level level)
    {
        return isValidHeatSource(level.getBlockState(pos));
    }

    public static boolean isValidHeatSource(BlockState state)
    {
        return isValidHeatSource(state.getBlock());
    }

    public static boolean isValidHeatSource(Block block)
    {
        if (block.equals(Blocks.LAVA)) return true;
        if (block.equals(Blocks.LAVA_CAULDRON)) return true;
        if (block.equals(Blocks.FIRE)) return true;
        if (block.equals(Blocks.MAGMA_BLOCK)) return true;
        if (block.equals(Blocks.CAMPFIRE)) return true;
        if (block.equals(Blocks.SOUL_CAMPFIRE)) return true;
        return false;
    }

    public static int getLevelCostFromItems(ItemStack left, ItemStack right)
    {
        ItemStack itemstack = left;
        int cost = 1;
        int i = 0;
        int j = 0;
        int k = 0;
        if (itemstack.isEmpty()) {
            cost = 0;
        } else {
            ItemStack itemstack1 = itemstack.copy();
            ItemStack itemstack2 = right;
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack1);
            j = j + itemstack.getBaseRepairCost() + (itemstack2.isEmpty() ? 0 : itemstack2.getBaseRepairCost());
            boolean flag = false;

            if (!itemstack2.isEmpty()) {
                flag = itemstack2.getItem() == Items.ENCHANTED_BOOK && !EnchantedBookItem.getEnchantments(itemstack2).isEmpty();
                if (itemstack1.isDamageableItem() && itemstack1.getItem().isValidRepairItem(itemstack, itemstack2)) {
                    int l2 = Math.min(itemstack1.getDamageValue(), itemstack1.getMaxDamage() / 4);
                    if (l2 <= 0) {
                        cost = 0;
                        return cost;
                    }

                    int i3;
                    for(i3 = 0; l2 > 0 && i3 < itemstack2.getCount(); ++i3) {
                        int j3 = itemstack1.getDamageValue() - l2;
                        itemstack1.setDamageValue(j3);
                        ++i;
                        l2 = Math.min(itemstack1.getDamageValue(), itemstack1.getMaxDamage() / 4);
                    }
                } else {
                    if (!flag && (itemstack1.getItem() != itemstack2.getItem() || !itemstack1.isDamageableItem())) {
                        cost = 0;
                        return cost;
                    }

                    if (itemstack1.isDamageableItem() && !flag) {
                        int l = itemstack.getMaxDamage() - itemstack.getDamageValue();
                        int i1 = itemstack2.getMaxDamage() - itemstack2.getDamageValue();
                        int j1 = i1 + itemstack1.getMaxDamage() * 12 / 100;
                        int k1 = l + j1;
                        int l1 = itemstack1.getMaxDamage() - k1;
                        if (l1 < 0) {
                            l1 = 0;
                        }

                        if (l1 < itemstack1.getDamageValue()) {
                            itemstack1.setDamageValue(l1);
                            i += 2;
                        }
                    }

                    Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(itemstack2);
                    boolean flag2 = false;
                    boolean flag3 = false;

                    for(Enchantment enchantment1 : map1.keySet()) {
                        if (enchantment1 != null) {
                            int i2 = map.getOrDefault(enchantment1, 0);
                            int j2 = map1.get(enchantment1);
                            j2 = i2 == j2 ? j2 + 1 : Math.max(j2, i2);
                            boolean flag1 = enchantment1.canEnchant(itemstack);
                            if (itemstack.getItem() == Items.ENCHANTED_BOOK) {
                                flag1 = true;
                            }

                            for(Enchantment enchantment : map.keySet()) {
                                if (enchantment != enchantment1 && !enchantment1.isCompatibleWith(enchantment)) {
                                    flag1 = false;
                                    ++i;
                                }
                            }

                            if (!flag1) {
                                flag3 = true;
                            } else {
                                flag2 = true;
                                if (j2 > enchantment1.getMaxLevel()) {
                                    j2 = enchantment1.getMaxLevel();
                                }

                                map.put(enchantment1, j2);
                                int k3 = 0;
                                switch(enchantment1.getRarity()) {
                                    case COMMON:
                                        k3 = 1;
                                        break;
                                    case UNCOMMON:
                                        k3 = 2;
                                        break;
                                    case RARE:
                                        k3 = 4;
                                        break;
                                    case VERY_RARE:
                                        k3 = 8;
                                }

                                if (flag) {
                                    k3 = Math.max(1, k3 / 2);
                                }

                                i += k3 * j2;
                                if (itemstack.getCount() > 1) {
                                    i = 40;
                                }
                            }
                        }
                    }

                    if (flag3 && !flag2) {
                        cost = 0;
                        return cost;
                    }
                }
            }


            if (flag && !itemstack1.isBookEnchantable(itemstack2)) itemstack1 = ItemStack.EMPTY;

            cost = j + i;
            if (i <= 0) {
                itemstack1 = ItemStack.EMPTY;
            }

            if (k == i && k > 0 && cost >= 40) {
                cost = 39;
            }

            if (cost >= 40) {
                itemstack1 = ItemStack.EMPTY;
            }
        }
        return cost;
    }

    public static int getExperienceFromLevel(int level)
    {
        if (level < 17)
        {
            return (int) (Math.pow(level, 2D) + (6f * level));
        }
        else if (level < 32)
        {
            return (int) (2.5f * Math.pow(level, 2D) - 40.5f * level + 360f);
        }
        else
        {
            return (int) (4.5f * Math.pow(level, 2D) - 162.5f * level + 2220f);
        }
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

    public static Vec3 interpolate(Vec3 from, Vec3 to, double normal) {
        return new Vec3(from.x + (to.x - from.x) * normal, from.y + (to.y - from.y) * normal, from.z + (to.z - from.z) * normal);
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
