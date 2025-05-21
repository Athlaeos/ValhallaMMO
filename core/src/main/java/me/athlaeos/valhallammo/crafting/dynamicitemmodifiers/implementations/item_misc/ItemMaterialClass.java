package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.MaterialClass;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class ItemMaterialClass extends DynamicItemModifier {
    private MaterialClass materialClass = MaterialClass.WOOD;

    public ItemMaterialClass(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        MaterialClass.setMaterialType(context.getItem().getMeta(), materialClass);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            List<MaterialClass> weightClasses = Arrays.asList(MaterialClass.values());
            int currentClass = weightClasses.indexOf(materialClass);
            if (e.isLeftClick()) {
                if (currentClass + 1 >= weightClasses.size()) currentClass = 0;
                else currentClass++;
            } else {
                if (currentClass - 1 < 0) currentClass = weightClasses.size() - 1;
                else currentClass--;
            }
            materialClass = weightClasses.get(currentClass);
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(switch (materialClass){
                    case BOW -> Material.BOW;
                    case GOLD -> Material.GOLDEN_SWORD;
                    case IRON -> Material.IRON_SWORD;
                    case WOOD -> Material.WOODEN_SWORD;
                    case OTHER -> Material.NETHER_STAR;
                    case STONE -> Material.STONE_SWORD;
                    case DIAMOND -> Material.DIAMOND_SWORD;
                    case ENDERIC -> Material.ELYTRA;
                    case LEATHER -> Material.LEATHER;
                    case CROSSBOW -> Material.CROSSBOW;
                    case CHAINMAIL -> Material.CHAIN;
                    case NETHERITE -> Material.NETHERITE_SWORD;
                    case PRISMARINE -> Material.TRIDENT;
                })
                        .name("&eWhich material class should it be?")
                        .lore("&fMaterial class set to &e" + materialClass,
                                "&fMaterial classes affect which",
                                "&fsmithing exp multiplier and",
                                "&fquality values are used.")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.DIAMOND_BLOCK).get();
    }

    @Override
    public String getDisplayName() {
        return "&eMaterial Class";
    }

    @Override
    public String getDescription() {
        return "&fChanges the material class of an item.";
    }

    @Override
    public String getActiveDescription() {
        return "&fMaterial class set to &e" + materialClass;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setMaterialClass(MaterialClass materialClass) {
        this.materialClass = materialClass;
    }

    @Override
    public DynamicItemModifier copy() {
        ItemMaterialClass m = new ItemMaterialClass(getName());
        m.setMaterialClass(this.materialClass);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "You must indicate the material class to set the item to";
        try {
            materialClass = MaterialClass.valueOf(args[0]);
        } catch (IllegalArgumentException ignored){
            return "Invalid weight class, valid classes are: " + Arrays.stream(MaterialClass.values()).map(MaterialClass::toString).collect(Collectors.joining(", "));
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return Arrays.stream(MaterialClass.values()).map(MaterialClass::toString).toList();
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
