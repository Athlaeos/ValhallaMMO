package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.food;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.item.FoodClass;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.FoodPropertyManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.ConventionUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class FoodClassSet extends DynamicItemModifier {
    private FoodClass foodClass = FoodClass.MEAT;

    public FoodClassSet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        FoodPropertyManager.setFoodClass(context.getItem().getMeta(), foodClass);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            int currentRequirement = Arrays.asList(FoodClass.values()).indexOf(foodClass);
            if (e.isLeftClick()) {
                if (currentRequirement + 1 >= FoodClass.values().length) currentRequirement = 0;
                else currentRequirement++;
            } else {
                if (currentRequirement - 1 < 0) currentRequirement = FoodClass.values().length - 1;
                else currentRequirement--;
            }
            foodClass = FoodClass.values()[currentRequirement];
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(switch (foodClass){
                    case FATS -> Material.WHITE_DYE;
                    case MEAT -> Material.BEEF;
                    case NUTS -> Material.COCOA_BEANS;
                    case DAIRY -> Material.MILK_BUCKET;
                    case FRUIT -> Material.APPLE;
                    case GRAIN -> Material.WHEAT;
                    case SWEET -> Material.HONEY_BOTTLE;
                    case MAGICAL -> Material.ENCHANTED_GOLDEN_APPLE;
                    case SEAFOOD -> Material.COD;
                    case SPOILED -> Material.ROTTEN_FLESH;
                    case BEVERAGE -> Material.WATER_BUCKET;
                    case ALCOHOLIC -> Material.POTION;
                    case SEASONING -> Material.SUGAR;
                    case VEGETABLE -> Material.CARROT;
                })
                        .name("&eWhich food class should it be?")
                        .lore("&fFood class set to &e" + foodClass,
                                "&fAffects the type of food the item ",
                                "&fis considered by the plugin, which",
                                "&faffects diminishing returns mechanics",
                                "&fand food category multipliers.",
                                "&6Click to cycle")
                        .flag(ConventionUtils.getHidePotionEffectsFlag()).color(Utils.hexToRgb("#5E2C04"))
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.PAPER).get();
    }

    @Override
    public String getDisplayName() {
        return "&bFood Class";
    }

    @Override
    public String getDescription() {
        return "&fChanges the food class of an item.";
    }

    @Override
    public String getActiveDescription() {
        return "&fFood class set to &e" + foodClass;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.FOOD.id());
    }

    public void setFoodClass(FoodClass foodClass) {
        this.foodClass = foodClass;
    }

    @Override
    public DynamicItemModifier copy() {
        FoodClassSet m = new FoodClassSet(getName());
        m.setFoodClass(this.foodClass);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "You must indicate the food class to set the item to";
        try {
            foodClass = FoodClass.valueOf(args[0]);
        } catch (IllegalArgumentException ignored){
            return "Invalid equipment class";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return Arrays.stream(FoodClass.values()).map(FoodClass::toString).toList();
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
