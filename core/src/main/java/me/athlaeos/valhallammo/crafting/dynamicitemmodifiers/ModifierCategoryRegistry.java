package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers;

import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.version.ConventionUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.util.HashMap;
import java.util.Map;

import static me.athlaeos.valhallammo.utility.Utils.oldOrNew;

public class ModifierCategoryRegistry {
    public static final ModifierCategory ALL = new ModifierCategory("ALL", 0, new ItemBuilder(Material.BOOKSHELF)
            .name("&fAll Modifiers")
            .lore("&fContains all modifiers.").get());
    public static final ModifierCategory CUSTOM_ATTRIBUTES = new ModifierCategory("CUSTOM_ATTRIBUTES", 1, new ItemBuilder(Material.GOLDEN_PICKAXE)
            .name("&6Custom Attributes")
            .lore("&fContains all modifiers adding,", "&fremoving, or scaling custom", "&fValhallaMMO attributes.").get());
    public static final ModifierCategory VANILLA_ATTRIBUTES = new ModifierCategory("VANILLA_ATTRIBUTES", 2, new ItemBuilder(Material.IRON_PICKAXE)
            .name("&eVanilla Attributes")
            .lore("&fContains all modifiers adding,", "&fremoving, or scaling vanilla", "&fattributes.").get());
    public static final ModifierCategory ITEM_MISC = new ModifierCategory("ITEM_MISC", 3, new ItemBuilder(Material.DIAMOND_PICKAXE)
            .name("&7Miscellaneous Item Properties")
            .lore("&fContains all modifiers that", "&fchange other item properties,", "&fsuch as model data, name, material,", "&fquality, etc.").get());
    public static final ModifierCategory CRAFTING_CONDITIONALS = new ModifierCategory("CRAFTING_CONDITIONALS", 4, new ItemBuilder(Material.REDSTONE_TORCH)
            .name("&aCrafting Conditionals")
            .lore("&fContains all modifiers adding", "&fconditions and item tags to", "&fprevent recipes from being", "&fcrafted under certain conditions").get());
    public static final ModifierCategory ITEM_FLAGS = new ModifierCategory("ITEM_FLAGS", 5, new ItemBuilder(Material.PAPER)
            .name("&7Item Flags")
            .lore("&fContains all modifiers adding", "&for removing item flags to", "&fitems. These usually hide", "&fspecific information of the item.").get());
    public static final ModifierCategory FOOD = new ModifierCategory("FOOD", 6, new ItemBuilder(Material.COOKED_BEEF)
            .name("&7Food Properties")
            .lore("&fContains all modifiers that", "&fchange the properties of food,", "&fsuch as nutrition, saturation,", "&ffood group, etc.").get());
    public static final ModifierCategory REWARDS = new ModifierCategory("REWARDS", 7, new ItemBuilder(Material.CHEST)
            .name("&bPlayer Rewards")
            .lore("&fContains all modifiers rewarding", "&fthe player for completing these", "&frecipes, such as money, EXP,", "&fitems, buffs, etc.").get());
    public static final ModifierCategory CUSTOM_POTION_EFFECTS = new ModifierCategory("CUSTOM_POTION_EFFECTS", 8, new ItemBuilder(Material.LINGERING_POTION)
            .name("&5Custom Potion Effects")
            .flag(ConventionUtils.getHidePotionEffectsFlag())
            .color(Color.fromRGB(128, 0, 255))
            .lore("&fContains all modifiers adding,", "&fremoving, or scaling custom", "&fValhallaMMO potion effects.").get());
    public static final ModifierCategory VANILLA_POTION_EFFECTS = new ModifierCategory("VANILLA_POTION_EFFECTS", 9, new ItemBuilder(Material.POTION)
            .name("&dVanilla Potion Effects")
            .flag(ConventionUtils.getHidePotionEffectsFlag())
            .color(Color.fromRGB(247, 0, 255))
            .lore("&fContains all modifiers adding,", "&fremoving, or scaling vanilla", "&fpotion effects.").get());
    public static final ModifierCategory POTION_MISC = new ModifierCategory("POTION_MISC", 10, new ItemBuilder(Material.DRAGON_BREATH)
            .name("&7Miscellaneous Potion Properties")
            .lore("&fContains all modifiers that", "&fchange other potion properties,", "&fsuch as color, quality, effect", "&finversions, etc.").get());
    public static final ModifierCategory POTION_CONDITIONALS = new ModifierCategory("POTION_CONDITIONALS", 11, new ItemBuilder(Material.BREWING_STAND)
            .name("&aPotion Conditionals")
            .lore("&fContains all modifiers adding", "&fconditions and item tags to", "&fprevent recipes from being", "&fbrewed under certain conditions").get());
    public static final ModifierCategory ENCHANTMENTS = new ModifierCategory("ENCHANTMENTS", 12, new ItemBuilder(Material.ENCHANTED_BOOK)
            .name("&dEnchantments")
            .lore("&fContains all modifiers adding,", "&fremoving, or scaling vanilla", "&fenchantments.").get());
    public static final ModifierCategory ENCHANTMENT_MISC = new ModifierCategory("ENCHANTMENT_MISC", 13, new ItemBuilder(Material.BOOK)
            .name("&7Miscellaneous Enchantment Properties")
            .lore("&fContains all modifiers that", "&fchange other enchantment-related", "&factions, such as giving item glints,",
                    "&fwiping enchantments, random enchanting,", "&fetc.").get());

    private static final Map<String, ModifierCategory> categories = new HashMap<>();

    static {
        register(ALL);
        register(CUSTOM_ATTRIBUTES);
        register(VANILLA_ATTRIBUTES);
        register(ITEM_MISC);
        register(CRAFTING_CONDITIONALS);
        register(ITEM_FLAGS);
        register(REWARDS);
        register(CUSTOM_POTION_EFFECTS);
        register(VANILLA_POTION_EFFECTS);
        register(POTION_MISC);
        register(ENCHANTMENTS);
        register(ENCHANTMENT_MISC);
        register(POTION_CONDITIONALS);
        register(FOOD);
    }

    public static void register(ModifierCategory c) {
        categories.put(c.id(), c);
    }

    public static Map<String, ModifierCategory> getCategories() {
        return new HashMap<>(categories);
    }

    public static ModifierCategory getCategory(String name){
        if (!categories.containsKey(name)) throw new IllegalArgumentException("Modifier category " + name + " doesn't exist");
        return categories.get(name);
    }
}
