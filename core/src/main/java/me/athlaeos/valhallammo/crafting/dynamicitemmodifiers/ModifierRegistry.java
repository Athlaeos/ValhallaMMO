package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.food.*;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats.*;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.crafting_conditionals.*;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.enchantment_misc.EnchantmentRandomize;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.enchantment_misc.EnchantmentsClear;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.enchantments.EnchantmentAdd;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc.*;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_conditionals.*;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_effects.ScaleAmplifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_effects.ScaleDuration;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_misc.*;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.rewards.CommandsSet;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.rewards.Item;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.rewards.Money;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.rewards.VanillaExperience;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.version.EnchantmentMappings;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.util.Arrays;
import java.util.HashMap;
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

        register(new MiningExceptionsAdd("mining_exception_add"));
        register(new MiningSpeedSet("mining_speed"));
        register(new MiningHardnessTranslationsAdd("mining_hardness_translation_add"));
        register(new EmbeddedToolsSet("embed_tools"));

        register(new LootTableSet("loot_table"));
        register(new ItemDummyHelmet("dummy_helmet"));
        register(new PlayerHead("player_head"));
        register(new PotionBeltModifier("potion_belt"));

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
        register(new SmithingTagsLevelRequirement("smithing_tags_require_with_levels"));
        register(new SmithingTagsCancelIfPresent("smithing_tags_forbid"));
        register(new SmithingTagsRemove("smithing_tags_remove"));
        register(new SmithingTagsRemoveAll("smithing_tags_clear"));

        register(new EnchantmentRandomize("enchant_randomly"));
        register(new EnchantmentsClear("enchantments_clear"));

        register(new EnchantmentAdd("enchant_add_flame", EnchantmentMappings.FLAME.getEnchantment(), Material.FIRE_CHARGE));
        register(new EnchantmentAdd("enchant_add_power", EnchantmentMappings.POWER.getEnchantment(), Material.BOW));
        register(new EnchantmentAdd("enchant_add_infinity", EnchantmentMappings.INFINITY.getEnchantment(), Material.SPECTRAL_ARROW));
        register(new EnchantmentAdd("enchant_add_punch", EnchantmentMappings.PUNCH.getEnchantment(), Material.PISTON));
        register(new EnchantmentAdd("enchant_add_curse_binding", EnchantmentMappings.CURSE_OF_BINDING.getEnchantment(), Material.CHAIN));
        register(new EnchantmentAdd("enchant_add_channeling", EnchantmentMappings.CHANNELING.getEnchantment(), Material.TRIDENT));
        register(new EnchantmentAdd("enchant_add_sharpness", EnchantmentMappings.SHARPNESS.getEnchantment(), Material.IRON_SWORD));
        register(new EnchantmentAdd("enchant_add_bane_of_arthropods", EnchantmentMappings.BANE_OF_ARTHROPODS.getEnchantment(), Material.WOODEN_SWORD));
        register(new EnchantmentAdd("enchant_add_smite", EnchantmentMappings.SMITE.getEnchantment(), Material.GOLDEN_SWORD));
        register(new EnchantmentAdd("enchant_add_depth_strider", EnchantmentMappings.DEPTH_STRIDER.getEnchantment(), Material.COD));
        register(new EnchantmentAdd("enchant_add_efficiency", EnchantmentMappings.EFFICIENCY.getEnchantment(), Material.GOLDEN_PICKAXE));
        register(new EnchantmentAdd("enchant_add_unbreaking", EnchantmentMappings.UNBREAKING.getEnchantment(), Material.DIAMOND));
        register(new EnchantmentAdd("enchant_add_fire_aspect", EnchantmentMappings.FIRE_ASPECT.getEnchantment(), Material.LAVA_BUCKET));
        register(new EnchantmentAdd("enchant_add_frost_walker", EnchantmentMappings.FROST_WALKER.getEnchantment(), Material.ICE));
        register(new EnchantmentAdd("enchant_add_impaling", EnchantmentMappings.IMPALING.getEnchantment(), Material.TRIDENT));
        register(new EnchantmentAdd("enchant_add_knockback", EnchantmentMappings.KNOCKBACK.getEnchantment(), Material.PISTON));
        register(new EnchantmentAdd("enchant_add_fortune", EnchantmentMappings.FORTUNE.getEnchantment(), Material.DIAMOND_PICKAXE));
        register(new EnchantmentAdd("enchant_add_looting", EnchantmentMappings.LOOTING.getEnchantment(), Material.DIAMOND_SWORD));
        register(new EnchantmentAdd("enchant_add_loyalty", EnchantmentMappings.LOYALTY.getEnchantment(), Material.LEAD));
        register(new EnchantmentAdd("enchant_add_luck_of_the_sea", EnchantmentMappings.LUCK_OF_THE_SEA.getEnchantment(), Material.HEART_OF_THE_SEA));
        register(new EnchantmentAdd("enchant_add_lure", EnchantmentMappings.LURE.getEnchantment(), Material.FISHING_ROD));
        register(new EnchantmentAdd("enchant_add_mending", EnchantmentMappings.MENDING.getEnchantment(), Material.ANVIL));
        register(new EnchantmentAdd("enchant_add_multishot", EnchantmentMappings.MULTISHOT.getEnchantment(), Material.DISPENSER));
        register(new EnchantmentAdd("enchant_add_respiration", EnchantmentMappings.RESPIRATION.getEnchantment(), Material.KELP));
        register(new EnchantmentAdd("enchant_add_piercing", EnchantmentMappings.PIERCING.getEnchantment(), Material.ARROW));
        register(new EnchantmentAdd("enchant_add_protection", EnchantmentMappings.PROTECTION.getEnchantment(), Material.EMERALD));
        register(new EnchantmentAdd("enchant_add_blast_protection", EnchantmentMappings.BLAST_PROTECTION.getEnchantment(), Material.TNT));
        register(new EnchantmentAdd("enchant_add_feather_falling", EnchantmentMappings.FEATHER_FALLING.getEnchantment(), Material.LEATHER_BOOTS));
        register(new EnchantmentAdd("enchant_add_fire_protection", EnchantmentMappings.FIRE_PROTECTION.getEnchantment(), Material.MAGMA_CREAM));
        register(new EnchantmentAdd("enchant_add_projectile_protection", EnchantmentMappings.PROJECTILE_PROTECTION.getEnchantment(), Material.SHIELD));
        register(new EnchantmentAdd("enchant_add_quick_charge", EnchantmentMappings.QUICK_CHARGE.getEnchantment(), Material.CROSSBOW));
        register(new EnchantmentAdd("enchant_add_riptide", EnchantmentMappings.RIPTIDE.getEnchantment(), Material.FEATHER));
        register(new EnchantmentAdd("enchant_add_silk_touch", EnchantmentMappings.SILK_TOUCH.getEnchantment(), Material.STRING));
        register(new EnchantmentAdd("enchant_add_soul_speed", EnchantmentMappings.SOUL_SPEED.getEnchantment(), Material.SOUL_SAND));
        register(new EnchantmentAdd("enchant_add_sweeping_edge", EnchantmentMappings.SWEEPING_EDGE.getEnchantment(), Material.IRON_SWORD));
        register(new EnchantmentAdd("enchant_add_thorns", EnchantmentMappings.THORNS.getEnchantment(), Material.CACTUS));
        register(new EnchantmentAdd("enchant_add_curse_vanishing", EnchantmentMappings.CURSE_OF_VANISHING.getEnchantment(), Material.BARRIER));
        register(new EnchantmentAdd("enchant_add_aqua_affinity", EnchantmentMappings.AQUA_AFFINITY.getEnchantment(), Material.CONDUIT));
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5)){
            register(new EnchantmentAdd("enchant_add_breach", EnchantmentMappings.BREACH.getEnchantment(), Material.valueOf("MACE")));
            register(new EnchantmentAdd("enchant_add_density", EnchantmentMappings.DENSITY.getEnchantment(), Material.valueOf("MACE")));
            register(new EnchantmentAdd("enchant_add_wind_burst", EnchantmentMappings.WIND_BURST.getEnchantment(), Material.valueOf("MACE")));
        }
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_21)){
            register(new MaxStackSizeSet("max_stack_size"));
            register(new HideToolTip("tooltip_hide"));
            register(new FireResistant("fire_resistant_item"));
            register(new EnchantmentGlint("glint"));
            register(new MakeEdible("edible"));
        }
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_21_3)){
            register(new ToolTip("tooltip"));
            register(new ItemModel("model"));
            register(new Equippable("equippable"));
        }

        register(new AmountAdd("amount_add"));
        register(new AmountRandomized("amount_randomized"));
        register(new AmountScale("amount_scale"));
        register(new AmountSet("amount_set"));
        register(new ColorDecimal("color_decimal"));
        register(new ColorRGB("color_rgb"));
        register(new CustomModelDataSet("custom_model_data"));
        register(new CustomIDSet("custom_id"));
        register(new DisplayNameSet("rename"));
        register(new LoreSet("relore"));
        register(new PreventSwordSweeping("sweeping_removal"));
//        register(new ThrowableWeapon("throwable_weapon"));
        register(new DurabilityRandomized("durability_randomize"));
        register(new DurabilityRepairNumeric("repair_number"));
        register(new DurabilityRepairScale("repair_scale"));
        register(new DurabilityScale("durability_scale"));
        register(new DurabilityDefaultSet("durability_max_set"));
        register(new ItemEquipmentClass("equipment_type"));
        register(new ItemMaterialClass("material_type"));
        Arrays.stream(CustomFlag.values()).forEach(f -> register(new FlagCustomAdd("flag_" + f.toString().toLowerCase(java.util.Locale.US), f)));
        Arrays.stream(ItemFlag.values()).forEach(f -> {
            if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) && f.toString().equals("HIDE_ADDITIONAL_TOOLTIP"))
                register(new FlagVanillaAdd("flag_hide_potion_effects", f.toString()));
            else register(new FlagVanillaAdd("flag_" + f.toString().toLowerCase(java.util.Locale.US), f.toString()));
        });
        register(new CommandsSet("commands"));
        register(new ItemReplace("replace"));
        register(new ItemReplaceKeepingAmount("replace_keep_amount"));
        register(new ItemType("material"));
        register(new ItemUnbreakable("unbreakable"));
        register(new ItemWeightClass("weight_class"));
        register(new ItemReplaceByIndexed("replace_by_custom"));
        register(new ItemReplaceByIndexedBasedOnQuality("replace_by_custom_quality_based"));
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
        PotionTypeForbidden.legalTypes.forEach(t -> register(new PotionTypeForbidden("potion_type_forbid_" + t.toString().toLowerCase(java.util.Locale.US), t)));
        PotionTypeRequired.legalTypes.forEach(t -> register(new PotionTypeRequired("potion_type_require_" + t.toString().toLowerCase(java.util.Locale.US), t)));
        register(new PotionTypeSet("potion_type_set"));
        register(new ConversionTransmutationPotion("transmutation_potion"));
        register(new ScaleAmplifier("potion_amplifier_scale"));
        register(new ScaleDuration("potion_duration_scale"));
        register(new PotionEffectConvert("potion_effect_convert"));

        register(new AlchemyQualityAdd("alchemy_quality_add"));
        register(new AlchemyQualityMultiply("alchemy_quality_multiply"));
        register(new AlchemyQualityRandomized("alchemy_quality_randomize"));
        register(new AlchemyQualityScale("alchemy_quality_scale"));
        register(new AlchemyQualitySet("alchemy_quality_set"));
        register(new ConversionMilkToChocolateMilk("chocolate_milk_from_milk"));

        // All the potion effects are registered in the effect registry

        register(new Item("reward_item"));
        register(new Money("reward_money"));
        register(new VanillaExperience("reward_vanilla_experience"));

        register(new EffectNullification("food_nullify_effects"));
        register(new FoodClassSet("food_type"));
        register(new FoodValueSet("food_value"));
        register(new SaturationValueSet("food_saturation"));

        register(new BaitValueSet("bait_power"));

        register(new ArmorSetSet("armor_set"));

        register(new MerchantSmithingQualitySet("merchant_smithing_skill"));
        register(new MerchantAlchemyQualitySet("merchant_alchemy_skill"));
    }

    public static void register(DynamicItemModifier m) {
        modifiers.put(m.getName(), m);
    }

    public static Map<String, DynamicItemModifier> getModifiers() {
        return new HashMap<>(modifiers);
    }

    public static DynamicItemModifier createModifier(String name){
        if (!modifiers.containsKey(name)) throw new IllegalArgumentException("Modifier " + name + " doesn't exist");
        return modifiers.get(name).copy();
    }
}
