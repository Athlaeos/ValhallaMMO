package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.attributes.*;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.crafting_conditionals.*;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.enchantment_misc.EnchantmentRandomize;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.enchantment_misc.EnchantmentsClear;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.enchantments.EnchantmentAdd;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.food.EffectNullification;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.food.FoodClassSet;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.food.FoodValueSet;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.food.SaturationValueSet;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc.*;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_conditionals.*;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_misc.*;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.rewards.Item;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.rewards.Money;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.rewards.SkillExperience;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.rewards.VanillaExperience;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModifierRegistry {
    private static final Map<String, DynamicItemModifier> modifiers = new HashMap<>();

    static {
        register(new ArrowBehaviorAntiGravity("arrow_behavior_antigrav"));
        register(new ArrowBehaviorDragonFireball("arrow_behavior_dragonball"));
        register(new ArrowBehaviorExplosive("arrow_behavior_explosive"));
        register(new ArrowBehaviorVeryExplosive("arrow_behavior_very_explosive"));
        register(new ArrowBehaviorImmunityRemoval("arrow_behavior_immunityvoid"));
        register(new ArrowBehaviorIncendiary("arrow_behavior_incendiary"));
        register(new ArrowBehaviorLargeFireball("arrow_behavior_largefireball"));
        register(new ArrowBehaviorLightning("arrow_behavior_lightning"));
        register(new ArrowBehaviorSmallFireball("arrow_behavior_smallfireball"));
        register(new ArrowBehaviorTeleporting("arrow_behavior_teleportation"));

        // All the attribute modifiers are registered in the attribute registry

        register(new CostMoney("cost_money"));
        register(new CostVanillaExperience("cost_experience"));
        register(new CounterCompareItem("counter_compare_item"));
        register(new CounterCompareNumber("counter_compare_number"));
        register(new CounterComparePlayer("counter_compare_player"));
        register(new CounterIncrement("counter_increment"));
        register(new CounterSetItemLimit("counter_set_item_limit"));
        register(new SmithingTagsAdd("smithing_tags_add"));
        register(new SmithingTagsCancelIfAbsent("smithing_tags_require"));
        register(new SmithingTagsCancelIfPresent("smithing_tags_forbid"));
        register(new SmithingTagsRemove("smithing_tags_remove"));
        register(new SmithingTagsRemoveAll("smithing_tags_clear"));

        register(new EnchantmentRandomize("enchant_randomly"));
        register(new EnchantmentsClear("enchantments_clear"));

        register(new EnchantmentAdd("enchant_add_flame", Enchantment.ARROW_FIRE, Material.FIRE_CHARGE));
        register(new EnchantmentAdd("enchant_add_power", Enchantment.ARROW_DAMAGE, Material.BOW));
        register(new EnchantmentAdd("enchant_add_infinity", Enchantment.ARROW_INFINITE, Material.SPECTRAL_ARROW));
        register(new EnchantmentAdd("enchant_add_punch", Enchantment.ARROW_KNOCKBACK, Material.PISTON));
        register(new EnchantmentAdd("enchant_add_curse_binding", Enchantment.BINDING_CURSE, Material.CHAIN));
        register(new EnchantmentAdd("enchant_add_channeling", Enchantment.CHANNELING, Material.TRIDENT));
        register(new EnchantmentAdd("enchant_add_sharpness", Enchantment.DAMAGE_ALL, Material.IRON_SWORD));
        register(new EnchantmentAdd("enchant_add_bane_of_arthropods", Enchantment.DAMAGE_ARTHROPODS, Material.WOODEN_SWORD));
        register(new EnchantmentAdd("enchant_add_smite", Enchantment.DAMAGE_UNDEAD, Material.GOLDEN_SWORD));
        register(new EnchantmentAdd("enchant_add_depth_strider", Enchantment.DEPTH_STRIDER, Material.COD));
        register(new EnchantmentAdd("enchant_add_efficiency", Enchantment.DIG_SPEED, Material.GOLDEN_PICKAXE));
        register(new EnchantmentAdd("enchant_add_unbreaking", Enchantment.DURABILITY, Material.DIAMOND));
        register(new EnchantmentAdd("enchant_add_fire_aspect", Enchantment.FIRE_ASPECT, Material.LAVA_BUCKET));
        register(new EnchantmentAdd("enchant_add_frost_walker", Enchantment.FROST_WALKER, Material.ICE));
        register(new EnchantmentAdd("enchant_add_impaling", Enchantment.IMPALING, Material.TRIDENT));
        register(new EnchantmentAdd("enchant_add_knockback", Enchantment.KNOCKBACK, Material.PISTON));
        register(new EnchantmentAdd("enchant_add_fortune", Enchantment.LOOT_BONUS_BLOCKS, Material.DIAMOND_PICKAXE));
        register(new EnchantmentAdd("enchant_add_looting", Enchantment.LOOT_BONUS_MOBS, Material.DIAMOND_SWORD));
        register(new EnchantmentAdd("enchant_add_loyalty", Enchantment.LOYALTY, Material.LEAD));
        register(new EnchantmentAdd("enchant_add_luck_of_the_sea", Enchantment.LUCK, Material.HEART_OF_THE_SEA));
        register(new EnchantmentAdd("enchant_add_lure", Enchantment.LURE, Material.FISHING_ROD));
        register(new EnchantmentAdd("enchant_add_mending", Enchantment.MENDING, Material.ANVIL));
        register(new EnchantmentAdd("enchant_add_multishot", Enchantment.MULTISHOT, Material.DISPENSER));
        register(new EnchantmentAdd("enchant_add_respiration", Enchantment.OXYGEN, Material.KELP));
        register(new EnchantmentAdd("enchant_add_piercing", Enchantment.PIERCING, Material.ARROW));
        register(new EnchantmentAdd("enchant_add_protection", Enchantment.PROTECTION_ENVIRONMENTAL, Material.EMERALD));
        register(new EnchantmentAdd("enchant_add_blast_protection", Enchantment.PROTECTION_EXPLOSIONS, Material.TNT));
        register(new EnchantmentAdd("enchant_add_feather_falling", Enchantment.PROTECTION_FALL, Material.LEATHER_BOOTS));
        register(new EnchantmentAdd("enchant_add_fire_protection", Enchantment.PROTECTION_FIRE, Material.MAGMA_CREAM));
        register(new EnchantmentAdd("enchant_add_projectile_protection", Enchantment.PROTECTION_PROJECTILE, Material.SHIELD));
        register(new EnchantmentAdd("enchant_add_quick_charge", Enchantment.QUICK_CHARGE, Material.CROSSBOW));
        register(new EnchantmentAdd("enchant_add_riptide", Enchantment.RIPTIDE, Material.FEATHER));
        register(new EnchantmentAdd("enchant_add_silk_touch", Enchantment.SILK_TOUCH, Material.STRING));
        register(new EnchantmentAdd("enchant_add_soul_speed", Enchantment.SOUL_SPEED, Material.SOUL_SAND));
        register(new EnchantmentAdd("enchant_add_sweeping_edge", Enchantment.SWEEPING_EDGE, Material.IRON_SWORD));
        register(new EnchantmentAdd("enchant_add_thorns", Enchantment.THORNS, Material.CACTUS));
        register(new EnchantmentAdd("enchant_add_curse_vanishing", Enchantment.VANISHING_CURSE, Material.BARRIER));
        register(new EnchantmentAdd("enchant_add_aqua_affinity", Enchantment.WATER_WORKER, Material.CONDUIT));

        register(new AmountAdd("amount_add"));
        register(new AmountRandomized("amount_randomized"));
        register(new AmountScale("amount_scale"));
        register(new AmountSet("amount_set"));
        register(new ColorDecimal("color_decimal"));
        register(new ColorRGB("color_rgb"));
        register(new CustomModelDataSet("custom_model_data"));
        register(new DisplayNameSet("rename"));
        register(new DurabilityRandomized("durability_randomize"));
        register(new DurabilityRepairNumeric("repair_number"));
        register(new DurabilityRepairScale("repair_scale"));
        register(new EquipmentClassSet("equipment_type"));
        Arrays.stream(CustomFlag.values()).forEach(f -> register(new FlagCustomAdd("flag_" + f.toString().toLowerCase(), f)));
        Arrays.stream(ItemFlag.values()).forEach(f -> register(new FlagVanillaAdd("flag_" + f.toString().toLowerCase(), f)));
        register(new ItemReplace("replace"));
        register(new ItemReplaceKeepingAmount("replace_keep_amount"));
        register(new ItemType("material"));
        register(new ItemUnbreakable("unbreakable"));
        register(new ItemWeightClass("weight_class"));
        register(new PlayerSignatureAdd("signature"));
        register(new SmithingNeutralQualitySet("smithing_neutral_quality_set"));
        register(new SmithingQualityAdd("smithing_quality_add"));
        register(new SmithingQualityMultiply("smithing_quality_multiply"));
        register(new SmithingQualityRandomized("smithing_quality_randomize"));
        register(new SmithingQualityScale("smithing_quality_scale"));
        register(new SmithingQualitySet("smithing_quality_set"));
        register(new ToolIdSet("tool_id"));
        register(new TransformItemMaterial("transform_netherite", Material.NETHERITE_CHESTPLATE, "NETHERITE"));
        register(new TransformItemMaterial("transform_diamond", Material.DIAMOND_CHESTPLATE, "DIAMOND"));
        register(new TransformItemMaterial("transform_iron", Material.IRON_CHESTPLATE, "IRON"));
        register(new TransformItemMaterial("transform_gold", Material.GOLDEN_CHESTPLATE, "GOLDEN"));
        register(new TransformItemMaterial("transform_chainmail", Material.CHAINMAIL_CHESTPLATE, "CHAINMAIL"));
        register(new TransformItemMaterial("transform_leather", Material.LEATHER_CHESTPLATE, "LEATHER"));

        register(new AlchemyTagsAdd("alchemy_tags_add"));
        register(new AlchemyTagsCancelIfAbsent("alchemy_tags_require"));
        register(new AlchemyTagsCancelIfPresent("alchemy_tags_forbid"));
        register(new AlchemyTagsRemove("alchemy_tags_remove"));
        register(new AlchemyTagsRemoveAll("alchemy_tags_clear"));
        PotionTypeForbidden.legalTypes.forEach(t -> register(new PotionTypeForbidden("potion_type_forbid_" + t.toString().toLowerCase(), t)));
        PotionTypeRequired.legalTypes.forEach(t -> register(new PotionTypeRequired("potion_type_require_" + t.toString().toLowerCase(), t)));
        register(new PotionTypeSet("potion_type_set"));

        register(new AlchemyQualityAdd("alchemy_quality_add"));
        register(new AlchemyQualityMultiply("alchemy_quality_multiply"));
        register(new AlchemyQualityRandomized("alchemy_quality_randomize"));
        register(new AlchemyQualityScale("alchemy_quality_scale"));
        register(new AlchemyQualitySet("alchemy_quality_set"));
        register(new ConversionEffect("invert_potion_effects"));
        register(new ConversionMilkToChocolateMilk("chocolate_milk_from_milk"));

        // All the potion effects are registered in the effect registry

        register(new Item("reward_item"));
        register(new Money("reward_money"));
        SkillRegistry.getAllSkills().values().forEach(s -> register(new SkillExperience("reward_" + s.getType().toLowerCase() + "_experience", s.getType())));
        register(new VanillaExperience("reward_vanilla_experience"));

        register(new EffectNullification("food_nullify_effects"));
        register(new FoodClassSet("food_type"));
        register(new FoodValueSet("food_value"));
        register(new SaturationValueSet("food_saturation"));
    }

    public static void register(DynamicItemModifier m) {
        modifiers.put(m.getName(), m);
    }

    public static Map<String, DynamicItemModifier> getModifiers() {
        return new HashMap<>(modifiers);
    }

    public static DynamicItemModifier createModifier(String name){
        if (!modifiers.containsKey(name)) throw new IllegalArgumentException("Modifier " + name + " doesn't exist");
        return modifiers.get(name).createNew();
    }
}
