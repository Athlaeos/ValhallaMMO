package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.item.attributes.AttributeWrapper;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TransformItemMaterial extends DynamicItemModifier {
    private final Map<EquipmentClass, Material> classToMaterialMapping = new HashMap<>();
    private final Material icon;
    private final String materialPrefix;

    public TransformItemMaterial(String name, Material icon, String materialPrefix) {
        super(name);
        this.icon = icon;
        this.materialPrefix = materialPrefix;
        map(EquipmentClass.HELMET, materialPrefix, "_HELMET");
        map(EquipmentClass.CHESTPLATE, materialPrefix, "_CHESTPLATE");
        map(EquipmentClass.LEGGINGS, materialPrefix, "_LEGGINGS");
        map(EquipmentClass.BOOTS, materialPrefix, "_BOOTS");
        map(EquipmentClass.SWORD, materialPrefix, "_SWORD");
        map(EquipmentClass.PICKAXE, materialPrefix, "_PICKAXE");
        map(EquipmentClass.AXE, materialPrefix, "_AXE");
        map(EquipmentClass.SHOVEL, materialPrefix, "_SHOVEL");
        map(EquipmentClass.HOE, materialPrefix, "_HOE");
    }

    private void map(EquipmentClass equipmentClass, String material, String equipment){
        Material m = ItemUtils.stringToMaterial(material + equipment, null);
        if (m == null) return;
        classToMaterialMapping.put(equipmentClass, m);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        EquipmentClass equipmentClass = EquipmentClass.getMatchingClass(outputItem.getMeta());
        Material transformTo = classToMaterialMapping.get(equipmentClass);
        if (equipmentClass == null || transformTo == null) return;
        outputItem.type(transformTo);
        for (AttributeWrapper wrapper : ItemAttributesRegistry.getVanillaStats(outputItem.getItem().getType()).values()){
            // The item's vanilla stats are updated to their vanilla values, any added custom attributes are left alone
            ItemAttributesRegistry.setStat(outputItem.getItem(), outputItem.getMeta(), wrapper.getAttribute(), wrapper.getValue(), true);
        }
        PotionEffectRegistry.updateEffectLore(outputItem.getMeta());
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) { }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(icon).get();
    }

    @Override
    public String getDisplayName() {
        return "&7Equipment Upgrade: &e" + StringUtils.toPascalCase(materialPrefix);
    }

    @Override
    public String getDescription() {
        return "&fChanges the equipment's material to " + materialPrefix.toLowerCase() + ".";
    }

    @Override
    public String getActiveDescription() {
        return "&fChanges the equipment's material to " + materialPrefix.toLowerCase() + ".";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    @Override
    public DynamicItemModifier createNew() {
        return new TransformItemMaterial(getName(), icon, materialPrefix);
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 0;
    }
}
