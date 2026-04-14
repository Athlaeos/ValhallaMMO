package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.food;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.FoodClass;
import me.athlaeos.valhallammo.item.FoodPropertyManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.ConventionUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class FoodClassSet extends DynamicItemModifier {
    private FoodClass foodClass = null;
    private FoodClass currentFoodClass = FoodClass.MEAT;
    private Collection<FoodClass> foodClasses = new HashSet<>(Set.of(FoodClass.MEAT));

    public FoodClassSet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (foodClass != null) {
            foodClasses = new HashSet<>(Set.of(foodClass));
            foodClass = null;
        }
        FoodPropertyManager.setFoodClasses(context.getItem().getMeta(), foodClasses);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            if (e.isShiftClick()){
                if (e.isLeftClick()){
                    foodClasses.add(currentFoodClass);
                } else {
                    foodClasses.clear();
                }
            } else {
                int currentRequirement = Arrays.asList(FoodClass.values()).indexOf(currentFoodClass);
                if (e.isLeftClick()) {
                    if (currentRequirement + 1 >= FoodClass.values().length) currentRequirement = 0;
                    else currentRequirement++;
                } else {
                    if (currentRequirement - 1 < 0) currentRequirement = FoodClass.values().length - 1;
                    else currentRequirement--;
                }
                currentFoodClass = FoodClass.values()[currentRequirement];
            }
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(switch (currentFoodClass){
                    case FATS -> Material.WHITE_DYE;
                    case MEAT -> Material.COOKED_BEEF;
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
                    case RAW -> Material.BEEF;
                })
                        .name("&eSelect Food Class")
                        .lore("&fCurrently selected: &e" + currentFoodClass.toString().toLowerCase(Locale.US),
                                "&fCurrent list: ")
                        .appendLore(foodClasses.stream().map(f -> String.format("&e- %s", f.toString().toLowerCase(Locale.US))).toList())
                        .appendLore("",
                                "&fAffects the types of food the item ",
                                "&fis considered by the plugin, which",
                                "&faffects food related stats",
                                "&6Left-or-right click to cycle",
                                "&6Shift-left-click to &aadd " + currentFoodClass.toString().toLowerCase(Locale.US),
                                "&cShift-right-click to clear list")
                        .flag(ConventionUtils.getHidePotionEffectsFlag()).color(Utils.hexToRgb("#5E2C04"))
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.PAPER).get();
    }

    @Override
    public String getDisplayName() {
        return "&bFood Classes";
    }

    @Override
    public String getDescription() {
        return "&fChanges the food classes of an item.";
    }

    @Override
    public String getActiveDescription() {
        return "&fFood classes set to &e" + foodClasses.stream().map(f -> f.toString().toLowerCase(Locale.US)).collect(Collectors.joining(", "));
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.FOOD.id());
    }

    public void setFoodClass(FoodClass foodClass) {
        this.foodClass = foodClass;
    }

    public void setFoodClasses(Collection<FoodClass> foodClasses) {
        this.foodClasses = foodClasses;
    }

    public void setCurrentFoodClass(FoodClass currentFoodClass) {
        this.currentFoodClass = currentFoodClass;
    }

    @Override
    public DynamicItemModifier copy() {
        FoodClassSet m = new FoodClassSet(getName());
        m.setCurrentFoodClass(this.currentFoodClass);
        if (this.foodClass != null){
            this.foodClasses = new HashSet<>(Set.of(this.foodClass));
            m.setFoodClasses(new HashSet<>(Set.of(this.foodClass)));
            this.foodClass = null;
        } else {
            m.setFoodClasses(this.foodClasses);
        }
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "You must indicate the food classes to set the item to, separated by commas";
        String[] input = args[0].split(",");
        Collection<FoodClass> foodClasses = new HashSet<>();
        for (String i : input){
            try {
                foodClasses.add(FoodClass.valueOf(i));
            } catch (IllegalArgumentException ignored){
                return i + " is not a valid food class";
            }
        }
        this.foodClasses = foodClasses;
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
