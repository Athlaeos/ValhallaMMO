package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemEquipmentClass extends DynamicItemModifier {
    private EquipmentClass equipmentClass = EquipmentClass.SWORD;

    public ItemEquipmentClass(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        EquipmentClass.setEquipmentClass(context.getItem().getMeta(), equipmentClass);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            int currentRequirement = Arrays.asList(EquipmentClass.values()).indexOf(equipmentClass);
            if (e.isLeftClick()) {
                if (currentRequirement + 1 >= EquipmentClass.values().length) currentRequirement = 0;
                else currentRequirement++;
            } else {
                if (currentRequirement - 1 < 0) currentRequirement = EquipmentClass.values().length - 1;
                else currentRequirement--;
            }
            equipmentClass = EquipmentClass.values()[currentRequirement];
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(switch (equipmentClass){
                    case BOW -> Material.BOW;
                    case AXE -> Material.IRON_AXE;
                    case HOE -> Material.IRON_HOE;
                    case BOOTS -> Material.IRON_BOOTS;
                    case OTHER -> Material.PAPER;
                    case SWORD -> Material.IRON_SWORD;
                    case ELYTRA -> Material.ELYTRA;
                    case HELMET -> Material.IRON_HELMET;
                    case SHEARS -> Material.SHEARS;
                    case SHIELD -> Material.SHIELD;
                    case SHOVEL -> Material.IRON_SHOVEL;
                    case PICKAXE -> Material.IRON_PICKAXE;
                    case TRIDENT -> Material.TRIDENT;
                    case TRINKET -> Material.GOLD_NUGGET;
                    case CROSSBOW -> Material.CROSSBOW;
                    case LEGGINGS -> Material.IRON_LEGGINGS;
                    case CHESTPLATE -> Material.IRON_CHESTPLATE;
                    case FISHING_ROD -> Material.FISHING_ROD;
                    case FLINT_AND_STEEL -> Material.FLINT_AND_STEEL;
                })
                        .name("&eWhich equipment class should it be?")
                        .lore("&fEquipment class set to &e" + equipmentClass,
                                "&fOnly affects the type of equipment ",
                                "&fthe item is considered by the plugin.",
                                "&fDoes not affect usage mechanics.",
                                "&6Click to cycle")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.IRON_PICKAXE).get();
    }

    @Override
    public String getDisplayName() {
        return "&bEquipment Class";
    }

    @Override
    public String getDescription() {
        return "&fChanges the equipment class of an item.";
    }

    @Override
    public String getActiveDescription() {
        return "&fEquipment class set to &e" + equipmentClass;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setEquipmentClass(EquipmentClass equipmentClass) {
        this.equipmentClass = equipmentClass;
    }

    @Override
    public DynamicItemModifier copy() {
        ItemEquipmentClass m = new ItemEquipmentClass(getName());
        m.setEquipmentClass(this.equipmentClass);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "You must indicate the equipment class to set the item to";
        try {
            equipmentClass = EquipmentClass.valueOf(args[0]);
        } catch (IllegalArgumentException ignored){
            return "Invalid equipment class";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return Arrays.stream(EquipmentClass.values()).map(EquipmentClass::toString).toList();
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
