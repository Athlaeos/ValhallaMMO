package me.athlaeos.valhallammo;

import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierPriority;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.crafting_conditionals.SmithingTagsAdd;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.crafting_conditionals.SmithingTagsLevelRequirement;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc.SmithingNeutralQualitySet;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc.SmithingQualityScale;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats.DefaultAttributeAdd;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats.DefaultAttributeScale;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats.DurabilityScale;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.rewards.SkillExperience;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.*;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicGridRecipe;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.CustomItem;
import me.athlaeos.valhallammo.item.CustomItemRegistry;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Scripts {
    private static final Map<String, Pair<ItemStack, IngredientChoice>> classifications = new HashMap<>();
    static {
        classifications.put("tools", new Pair<>(new ItemStack(Material.IRON_PICKAXE), new ToolChoice()));
        classifications.put("melee", new Pair<>(new ItemStack(Material.IRON_SWORD), new MeleeWeaponChoice()));
        classifications.put("weapons", new Pair<>(new ItemStack(Material.IRON_SWORD), new WeaponChoice()));
        classifications.put("ranged", new Pair<>(new ItemStack(Material.BOW), new ConfigurableMaterialsChoice().setValidChoices(Set.of(Material.BOW, Material.CROSSBOW))));
        classifications.put("armor", new Pair<>(new ItemStack(Material.IRON_CHESTPLATE), new ArmorChoice()));
        classifications.put("any", new Pair<>(new ItemStack(Material.IRON_PICKAXE), new ToolArmorChoice()));
        classifications.put("helmets", new Pair<>(new ItemStack(Material.IRON_HELMET), new SimilarMaterialsChoice()));
        classifications.put("chestplates", new Pair<>(new ItemStack(Material.IRON_CHESTPLATE), new SimilarMaterialsChoice()));
        classifications.put("leggings", new Pair<>(new ItemStack(Material.IRON_LEGGINGS), new SimilarMaterialsChoice()));
        classifications.put("boots", new Pair<>(new ItemStack(Material.IRON_BOOTS), new SimilarMaterialsChoice()));
    }

    public static void createUpgradeRecipe(String attribute, String on, int tag, int maxLevel, double value){
        String name = "tinker_upgrade_" + attribute.toLowerCase();
        DynamicGridRecipe recipe = new DynamicGridRecipe(name);
        recipe.setShapeless(true);
        recipe.setRequireValhallaTools(true);
        recipe.setTinker(true);
        recipe.setUnlockedForEveryone(true);

        Map<Integer, SlotEntry> items = new HashMap<>();
        Pair<ItemStack, IngredientChoice> matchDetails = classifications.get(on);
        if (matchDetails == null) {
            ValhallaMMO.getInstance().getServer().broadcastMessage("Failed");
            return;
        }
        items.put(4, new SlotEntry(matchDetails.getOne(), matchDetails.getTwo()));
        recipe.setItems(items);

        List<DynamicItemModifier> modifiers = new ArrayList<>();
        SmithingTagsLevelRequirement m1 = (SmithingTagsLevelRequirement) ModifierRegistry.createModifier("smithing_tags_require_with_levels");
        m1.setTags(Set.of(new SmithingTagsLevelRequirement.LevelRequirement(tag, maxLevel - 1, true), new SmithingTagsLevelRequirement.LevelRequirement(8, 1, false)));
        m1.setPriority(ModifierPriority.SOONEST);
        modifiers.add(m1);
        SmithingTagsAdd m2 = (SmithingTagsAdd) ModifierRegistry.createModifier("smithing_tags_add");
        m2.setPriority(ModifierPriority.SOON);
        m2.setNewTags(Map.of(tag, 1, 8, -1));
        modifiers.add(m2);
        DefaultAttributeAdd m3 = (DefaultAttributeAdd) ModifierRegistry.createModifier("attribute_add_" + attribute.toLowerCase());
        m3.setValue(value);
        m3.setHidden(false);
        m3.setAdd(true);
        m3.setPriority(ModifierPriority.NEUTRAL);
        modifiers.add(m3);

        DynamicItemModifier.sortModifiers(modifiers);
        recipe.setModifiers(modifiers);

        CustomRecipeRegistry.register(recipe, true);
        ValhallaMMO.getInstance().getServer().broadcastMessage("Recipe created");
    }

    // the following script was used to quickly transfer all custom items from recipes to the custom item registry, for ease of access ingame
    public static void convert(){
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
