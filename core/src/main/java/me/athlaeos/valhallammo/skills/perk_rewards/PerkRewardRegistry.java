package me.athlaeos.valhallammo.skills.perk_rewards;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.block.BlockInteractConversions;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.recipetypes.ValhallaKeyedRecipe;
import me.athlaeos.valhallammo.dom.BiAction;
import me.athlaeos.valhallammo.playerstats.profiles.ResetType;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.*;
import me.athlaeos.valhallammo.skills.perk_rewards.implementations.*;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.properties.StatProperties;
import me.athlaeos.valhallammo.skills.skills.implementations.AlchemySkill;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PerkRewardRegistry {
    private static final Map<String, PerkReward> registry = new HashMap<>();

    static {
        for (Profile profile : ProfileRegistry.getRegisteredProfiles().values()){
            String skill = profile.getSkillType().getSimpleName().toLowerCase(java.util.Locale.US).replace("skill", "");
            if (profile.getSkillType() == null) continue;
            for (String s : profile.getAllStatNames()) {
                StatProperties properties = profile.getNumberStatProperties().get(s);
                if (properties != null && properties.generatePerkRewards()) {
                    if (profile.intStatNames().contains(s)) {
                        register(new ProfileIntAdd(skill + "_" + s + "_add", s, profile.getClass()));
                        register(new ProfileIntSet(skill + "_" + s + "_set", s, profile.getClass()));
                    } else if (profile.floatStatNames().contains(s)) {
                        register(new ProfileFloatAdd(skill + "_" + s + "_add", s, profile.getClass()));
                        register(new ProfileFloatSet(skill + "_" + s + "_set", s, profile.getClass()));
                    } else if (profile.doubleStatNames().contains(s)) {
                        register(new ProfileDoubleAdd(skill + "_" + s + "_add", s, profile.getClass()));
                        register(new ProfileDoubleSet(skill + "_" + s + "_set", s, profile.getClass()));
                    }
                }
                if (profile.shouldBooleanStatHavePerkReward(s)){
                    register(new ProfileBooleanSet(skill + "_" + s + "_set", s, profile.getClass()));
                    register(new ProfileBooleanToggle(skill + "_" + s + "_toggle", s, profile.getClass()));
                }
            }
        }

        for (ResetType type : ResetType.values()){
            register(new ProgressReset("reset_" + type.toString().toLowerCase(java.util.Locale.US), type));
        }

        BiAction<String, Player> forget = (s, p) -> ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
            ValhallaKeyedRecipe recipe = CustomRecipeRegistry.getAllKeyedRecipesByName().get(s);
            if (recipe == null) return;
            p.undiscoverRecipe(recipe.getKey());
        });
        BiAction<String, Player> discover = (s, p) -> ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
            ValhallaKeyedRecipe recipe = CustomRecipeRegistry.getAllKeyedRecipesByName().get(s);
            if (recipe == null) return;
            p.discoverRecipe(recipe.getKey());
        });

        register(new ProfileStringListAdd("perks_unlocked_add", "unlockedPerks", PowerProfile.class));
        register(new ProfileStringListRemove("perks_unlocked_remove", "unlockedPerks", PowerProfile.class));
        register(new ProfileStringListAdd("perks_fake_unlock_add", "fakeUnlockedPerks", PowerProfile.class));
        register(new ProfileStringListRemove("perks_fake_unlock_remove", "fakeUnlockedPerks", PowerProfile.class));
        register(new ProfileStringListClear("perks_fake_unlock_clear", "fakeUnlockedPerks", PowerProfile.class));
        register(new ProfileStringListAdd("perks_locked_add", "permanentlyLockedPerks", PowerProfile.class));
        register(new ProfileStringListClear("perks_locked_clear", "permanentlyLockedPerks", PowerProfile.class));
        register(new ProfileStringListAdd("recipes_unlock", "unlockedRecipes", PowerProfile.class, discover, forget));
        register(new ProfileStringListRemove("recipes_lock", "unlockedRecipes", PowerProfile.class, forget, discover));
        register(new ProfileStringListFill("recipes_unlock_all", "unlockedRecipes", PowerProfile.class, CustomRecipeRegistry::getAllRecipes));
        register(new ProfileStringListClear("recipes_lock_all", "unlockedRecipes", PowerProfile.class));
        register(new ProfileStringListAdd("enchanting_add_elemental_type", "elementalDamageTypes", EnchantingProfile.class));
        register(new ProfileStringSetSingle("enchanting_set_elemental_type", "elementalDamageTypes", EnchantingProfile.class));
        register(new ProfileStringListRemove("enchanting_remove_elemental_type", "elementalDamageTypes", EnchantingProfile.class));
        register(new ProfileStringListClear("enchanting_clear_elemental_type", "elementalDamageTypes", EnchantingProfile.class));
        register(new ProfileStringListAdd("lightarmor_add_immune_effect", "setImmunePotionEffects", LightArmorProfile.class));
        register(new ProfileStringListRemove("lightarmor_remove_immune_effect", "setImmunePotionEffects", LightArmorProfile.class));
        register(new ProfileStringListClear("lightarmor_clear_immune_effects", "setImmunePotionEffects", LightArmorProfile.class));
        register(new ProfileStringListAdd("heavyarmor_add_immune_effect", "setImmunePotionEffects", HeavyArmorProfile.class));
        register(new ProfileStringListRemove("heavyarmor_remove_immune_effect", "setImmunePotionEffects", HeavyArmorProfile.class));
        register(new ProfileStringListClear("heavyarmor_clear_immune_effects", "setImmunePotionEffects", HeavyArmorProfile.class));
        register(new ProfileStringListAdd("mining_veinminerblocks_add", "veinMinerValidBlocks", MiningProfile.class));
        register(new ProfileStringListRemove("mining_veinminerblocks_remove", "veinMinerValidBlocks", MiningProfile.class));
        register(new ProfileStringListClear("mining_veinminerblocks_clear", "veinMinerValidBlocks", MiningProfile.class));
        register(new ProfileStringListAdd("mining_unbreakableblocks_add", "unbreakableBlocks", MiningProfile.class));
        register(new ProfileStringListRemove("mining_unbreakableblocks_remove", "unbreakableBlocks", MiningProfile.class));
        register(new ProfileStringListClear("mining_unbreakableblocks_clear", "unbreakableBlocks", MiningProfile.class));
        register(new ProfileStringSetSingle("mining_emptyhandtooltype_set", "emptyHandToolMaterial", MiningProfile.class));
        register(new ProfileStringListClear("mining_emptyhandtooltype_remove", "emptyHandToolMaterial", MiningProfile.class));
        register(new ProfileStringListAdd("woodcutting_treecapitatorblocks_add", "treeCapitatorValidBlocks", WoodcuttingProfile.class));
        register(new ProfileStringListFill("woodcutting_treecapitatorblocks_add_all", "treeCapitatorValidBlocks", WoodcuttingProfile.class, () -> Tag.LOGS.getValues().stream().map(Material::toString).collect(Collectors.toSet())));
        register(new ProfileStringListRemove("woodcutting_treecapitatorblocks_remove", "treeCapitatorValidBlocks", WoodcuttingProfile.class));
        register(new ProfileStringListClear("woodcutting_treecapitatorblocks_remove", "treeCapitatorValidBlocks", WoodcuttingProfile.class));
        register(new ProfileStringListClear("woodcutting_treecapitatorblocks_clear", "treeCapitatorValidBlocks", WoodcuttingProfile.class));
        register(new ProfileStringListAdd("alchemy_transmutations_unlock", "unlockedTransmutations", AlchemyProfile.class));
        register(new ProfileStringListFill("alchemy_transmutations_unlock_all", "unlockedTransmutations", AlchemyProfile.class, () -> AlchemySkill.getTransmutations().keySet()));
        register(new ProfileStringListRemove("alchemy_transmutations_lock", "unlockedTransmutations", AlchemyProfile.class));
        register(new ProfileStringListClear("alchemy_transmutations_lock_all", "unlockedTransmutations", AlchemyProfile.class));
        register(new ProfileStringListAdd("block_conversions_unlock", "unlockedBlockConversions", PowerProfile.class));
        register(new ProfileStringListFill("block_conversions_unlock_all", "unlockedBlockConversions", PowerProfile.class, () -> BlockInteractConversions.getConversions().keySet()));
        register(new ProfileStringListRemove("block_conversions_lock", "unlockedBlockConversions", PowerProfile.class));
        register(new ProfileStringListClear("block_conversions_lock_all", "unlockedBlockConversions", PowerProfile.class));
        register(new ProfileStringListAdd("permanent_effects_add", "permanentPotionEffects", PowerProfile.class));
        register(new ProfileStringListRemove("permanent_effects_remove", "permanentPotionEffects", PowerProfile.class));
        register(new ProfileStringListClear("permanent_effects_clear", "permanentPotionEffects", PowerProfile.class));
    }

    public static void register(PerkReward reward){
        registry.put(reward.getName(), reward);
    }

    public static PerkReward createReward(String name, Object argument){
        try {
            boolean persist = name.startsWith("p:");
            if (persist) name = name.replaceFirst("p:", "");
            if (registry.get(name) == null) throw new IllegalArgumentException("Perk with name " + name + " is used but does not exist");
            PerkReward modifier = registry.get(name).clone();
            modifier.parseArgument(argument);
            modifier.setPersistent(persist);
            return modifier;
        } catch (CloneNotSupportedException ex){
            ValhallaMMO.logSevere("Could not clone PerkReward, notify plugin author");
            return null;
        }
    }

    public static Map<String, PerkReward> getRegisteredRewards() {
        return registry;
    }
}
