package com.codinglitch.ctweaks.util;

import net.minecraft.world.item.Item;

import java.awt.*;

public class StewIngredient {
    private Item ingredient;
    private Color color;
    private int foodValue;
    private float saturation;

    public StewIngredient(Item ingredient, Color color, int foodValue, float saturation)
    {
        this.ingredient = ingredient;
        this.color = color;
        this.foodValue = foodValue;
        this.saturation = saturation;
    }

    public Item getIngredient() {
        return ingredient;
    }

    public Color getColor() {
        return color;
    }

    public int getFoodValue() {
        return foodValue;
    }

    public float getSaturation() {
        return saturation;
    }
}
