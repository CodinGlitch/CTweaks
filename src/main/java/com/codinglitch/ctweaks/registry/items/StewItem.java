package com.codinglitch.ctweaks.registry.items;

import com.codinglitch.ctweaks.util.StewUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nullable;
import java.util.List;

public class StewItem extends BowlFoodItem {
    public StewItem() {
        super((new Item.Properties()).stacksTo(4).tab(CreativeModeTab.TAB_FOOD).food(Foods.MUSHROOM_STEW));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        if (entity instanceof Player player)
        {
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)player, itemStack);
            }
            player.getFoodData().eat(StewUtils.calculateFoodValue(itemStack),StewUtils.calculateSaturation(itemStack));
            if (player == null || !player.getAbilities().instabuild) {
                itemStack.shrink(1);
                if (itemStack.isEmpty()) {
                    return new ItemStack(Items.BOWL);
                }

                if (player != null) {
                    player.getInventory().add(new ItemStack(Items.BOWL));
                }
            }
        }
        return itemStack;
    }

    @Override
    public Component getName(ItemStack itemStack) {
        MutableComponent component = new TextComponent("");
        List<Item> ingredients = StewUtils.getIngredients(itemStack);
        if (!ingredients.isEmpty())
        {
            component = new TranslatableComponent(ingredients.get(0).getDescriptionId()).append(" ");
        }
        return component.append(new TranslatableComponent("item.ctweaks.stew"));
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level p_42989_, List<Component> components, TooltipFlag tooltipFlag) {
        components.add(new TranslatableComponent("ctweaks.stew.ingredients"));
        List<Item> ingredients = StewUtils.getIngredients(itemStack);

        MutableComponent component = null;
        for (int i = 0; i < ingredients.size(); i++) {
            Item ingredient = ingredients.get(i);

            if (component == null) {
                component = new TranslatableComponent(ingredient.getDescriptionId()).setStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE)));
            }
            else
            {
                component.append(new TranslatableComponent(ingredient.getDescriptionId()));
            }

            if (i != ingredients.size()-1) {
                component.append(", ");
            }
        }
        if (component != null) components.add(component);
    }
}
