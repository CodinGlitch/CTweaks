package com.codinglitch.ctweaks.util;

import net.minecraft.world.item.Item;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class IngredientData {
    List<StewIngredient> ingredients = new ArrayList<>();

    public StewIngredient getIngredient(Item item)
    {
        for (StewIngredient ingredient : ingredients) {
            if (ingredient.getIngredient().equals(item)) return ingredient;
        }
        return null;
    }

    public List<StewIngredient> getIngredients(List<Item> ingredients)
    {
        List<StewIngredient> stewIngredients = new ArrayList<>();
        for (Item ingredient : ingredients) {
            stewIngredients.add(getIngredient(ingredient));
        }
        return stewIngredients;
    }

    public void addIngredient(Item item, Color color, int foodValue, int saturation)
    {
        ingredients.add(new StewIngredient(item, color, foodValue, saturation));
    }

    public void addIngredient(StewIngredient ingredient)
    {
        ingredients.add(ingredient);
    }
}
