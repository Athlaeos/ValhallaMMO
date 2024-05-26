package me.athlaeos.valhallammo;

import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierPriority;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc.SmithingNeutralQualitySet;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc.SmithingQualityScale;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats.DefaultAttributeAdd;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats.DefaultAttributeScale;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats.DurabilityScale;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.rewards.SkillExperience;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicGridRecipe;
import me.athlaeos.valhallammo.item.CustomItem;
import me.athlaeos.valhallammo.item.CustomItemRegistry;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import org.bukkit.attribute.AttributeModifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class Scripts {
    // the following script was used to quickly transfer all custom items from recipes to the custom item registry, for ease of access ingame
    public static void execute(){
        Collection<DynamicGridRecipe> skillScalingRecipes = new HashSet<>();

        for (DynamicGridRecipe recipe : CustomRecipeRegistry.getGridRecipes().values()){
            if (!recipe.getName().contains("craft_")) continue;
            CustomItem item = CustomItemRegistry.register(recipe.getName().replace("craft_", ""), recipe.getResult());
            List<DynamicItemModifier> modifiers = new ArrayList<>(recipe.getModifiers().stream()
                    .filter(m -> {
                        if (!(m instanceof DefaultAttributeScale) &&
                                !(m instanceof SkillExperience) &&
                                !(m instanceof SmithingNeutralQualitySet) &&
                                !(m instanceof SmithingQualityScale) &&
                                !(m instanceof DurabilityScale)){
                            skillScalingRecipes.add(recipe);
                            return true;
                        } else return false;
                    }).map(DynamicItemModifier::copy).toList());

            if (modifiers.stream().noneMatch(m -> m instanceof DefaultAttributeAdd)){
                for (AttributeWrapper wrapper : ItemAttributesRegistry.getVanillaStats(recipe.getResult().getType()).values()){
                    DefaultAttributeAdd modifier = (DefaultAttributeAdd) ModifierRegistry.createModifier("attribute_add_" + wrapper.getAttribute().toLowerCase());
                    modifier.setPriority(ModifierPriority.NEUTRAL);
                    modifier.setValue(wrapper.getValue());
                    modifier.setOperation(AttributeModifier.Operation.ADD_NUMBER);
                    modifiers.add(0, modifier);
                }
            }

            item.setModifiers(modifiers);
        }

        for (DynamicGridRecipe recipe : skillScalingRecipes){
            CustomItem item = CustomItemRegistry.register(recipe.getName().replace("craft_", "") + "_scaling_with_skill", recipe.getResult());
            List<DynamicItemModifier> modifiers = new ArrayList<>(recipe.getModifiers().stream()
                    .filter(m -> !(m instanceof SkillExperience)).map(DynamicItemModifier::copy).toList());

            item.setModifiers(modifiers);
        }

        CustomItemRegistry.saveItems();
    }
}
