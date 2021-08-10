package com.codinglitch.ctweaks.util;

import com.codinglitch.ctweaks.registry.behaviour.StewInteraction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.util.Constants;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StewUtils {
    private static IngredientData ingredientData = new IngredientData();

    public static List<Item> getIngredients(ItemStack itemStack) {
        return getIngredients(itemStack.getTag());
    }

    public static List<Item> getIngredients(CompoundTag tag) {
        if (tag == null) return new ArrayList<>();

        int[] list = tag.getIntArray("Ingredients");
        List<Item> ret = new ArrayList<>();
        for (int id : list) {
            ret.add(Item.byId(id));
        }
        return ret;
    }

    public static Color getIngredientColor(Item item)
    {
        return ingredientData.getIngredient(item).getColor();
    }

    public static void addIngredient(Item item, int color, int foodValue, float saturation)
    {
        addIngredient(item, new Color(color), foodValue, saturation);
    }

    public static void addIngredient(Item item, Color color, int foodValue, float saturation)
    {
        StewIngredient ingredient = new StewIngredient(item, color, foodValue, saturation);
        ingredientData.addIngredient(ingredient);
        StewInteraction.registerIngredient(ingredient);
    }

    public static Color calculateColors(List<Item> ingredients)
    {
        Color color = null;
        for (Item ingredient : ingredients) {
            Color toColor = getIngredientColor(ingredient);
            color = color != null ? UtilityC.interpolate(color, toColor, 0.5) : toColor;
        }
        return color == null ? Color.WHITE : color;
    }

    public static int calculateFoodValue(ItemStack itemStack)
    {
        return calculateFoodValue(ingredientData.getIngredients(getIngredients(itemStack)));
    }

    public static int calculateFoodValue(List<StewIngredient> ingredients)
    {
        int foodValue = 0;
        for (StewIngredient ingredient : ingredients) {
            foodValue += ingredient.getFoodValue();
        }
        return foodValue;
    }

    public static float calculateSaturation(ItemStack itemStack)
    {
        return calculateSaturation(ingredientData.getIngredients(getIngredients(itemStack)));
    }

    public static float calculateSaturation(List<StewIngredient> ingredients)
    {
        float foodValue = 0;
        for (StewIngredient ingredient : ingredients) {
            foodValue += ingredient.getSaturation();
        }
        return foodValue;
    }
}
