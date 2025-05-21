package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.EquippableWrapper;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class Equippable extends DynamicItemModifier {
    private String model = null;
    private EquipmentSlot slot = EquipmentSlot.HEAD;
    private String cameraOverlay = null;

    public Equippable(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (model == null) return;
        EquippableWrapper wrapper = ValhallaMMO.getNms().getEquippable(context.getItem().getMeta());
        if (wrapper == null) wrapper = new EquippableWrapper(model, slot, cameraOverlay, null, null);
        else wrapper = new EquippableWrapper(model, slot, cameraOverlay == null ? wrapper.cameraOverlayKey() : cameraOverlay, wrapper.equipSound(), wrapper.allowedTypes());
        ValhallaMMO.getNms().setEquippable(context.getItem().getMeta(),
                wrapper.modelKey(),
                wrapper.slot(),
                wrapper.cameraOverlayKey(),
                wrapper.equipSound(),
                wrapper.allowedTypes() == null ? null : new ArrayList<>(wrapper.allowedTypes()));
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12){
            ItemStack cursor = e.getCursor();
            if (ItemUtils.isEmpty(cursor)) model = null;
            else {
                ItemMeta cursorMeta = cursor.getItemMeta();
                EquippableWrapper wrapper = ValhallaMMO.getNms().getEquippable(cursorMeta);
                if (wrapper == null) e.getWhoClicked().sendMessage(Utils.chat("&cItem has no custom model"));
                else model = wrapper.modelKey();
            }
        } else if (button == 18){
            ItemStack cursor = e.getCursor();
            if (ItemUtils.isEmpty(cursor)) cameraOverlay = null;
            else {
                ItemMeta cursorMeta = cursor.getItemMeta();
                EquippableWrapper wrapper = ValhallaMMO.getNms().getEquippable(cursorMeta);
                if (wrapper == null) e.getWhoClicked().sendMessage(Utils.chat("&cItem has no custom camera overlay"));
                else cameraOverlay = wrapper.cameraOverlayKey();
            }
        } else if (button == 16){
            slot = switch(slot){
                case HEAD -> EquipmentSlot.CHEST;
                case CHEST -> EquipmentSlot.LEGS;
                case LEGS -> EquipmentSlot.FEET;
                default -> EquipmentSlot.HEAD;
            };
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.DIAMOND_CHESTPLATE)
                        .name("&fWhich model should the item have?")
                        .lore("&fCurrently set to &e" + (model == null ? "&cnothing" : model),
                                "&fDetermines the visual appearance",
                                "&fof the item when worn",
                                "&6Click with another item to",
                                "&6copy the armor model of the item over")
                        .get()).map(Set.of(
                new Pair<>(18,
                        new ItemBuilder(Material.CARVED_PUMPKIN)
                                .name("&fWhich overlay should the item have?")
                                .lore("&fCurrently set to &e" + (cameraOverlay == null ? "&cnothing" : cameraOverlay),
                                        "&fDetermines the camera overlay",
                                        "&fof the item when worn",
                                        "&6Click with another item to",
                                        "&6copy the camera overlay of the item over")
                                .get()),
                new Pair<>(16,
                        new ItemBuilder(Material.ARMOR_STAND)
                                .name("&fWhich slot should the item be wearable in?")
                                .lore("&fCurrently set to &e" + slot,
                                        "&fDetermines the slot in which",
                                        "&fthe item can be worn",
                                        "&6Click to cycle options")
                                .get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.DIAMOND_CHESTPLATE).get();
    }

    @Override
    public String getDisplayName() {
        return "&aSet Equippable";
    }

    @Override
    public String getDescription() {
        return "&fAllows the item to be wearable, or its worn model to change";
    }

    @Override
    public String getActiveDescription() {
        return model == null ? "&cRemoves equippable properties" : "&fSets the item to be equippable, changing its model to " + model + " and wearable in the " + slot + " slot. " + (cameraOverlay == null ? "" : "&eApplies the " + cameraOverlay + " camera overlay when worn");
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setCameraOverlay(String cameraOverlay) {
        this.cameraOverlay = cameraOverlay;
    }

    public void setSlot(EquipmentSlot slot) {
        this.slot = slot;
    }

    @Override
    public DynamicItemModifier copy() {
        Equippable m = new Equippable(getName());
        m.setModel(this.model);
        m.setSlot(this.slot);
        m.setCameraOverlay(this.cameraOverlay);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 3)
            return "You must enter armor model key, the slot, and the camera overlay key. Use 'reset' for options you don't want";
        try {
            model = args[0].equalsIgnoreCase("reset") ? null : args[0];
            slot = EquipmentSlot.valueOf(args[1]);
            cameraOverlay = args[2].equalsIgnoreCase("reset") ? null : args[2];
        } catch (IllegalArgumentException ignored){
            return "Invalid slot. It may be HEAD, CHEST, LEGS, or FEET";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<model>", "minecraft:", "reset");
        if (currentArg == 1) return List.of("HEAD", "CHEST", "LEGS", "FEET");
        if (currentArg == 2) return List.of("<cameraOverlay>", "minecraft:", "reset");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 3;
    }
}
