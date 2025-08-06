package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.EquippableWrapper;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.ConventionUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Colorable;

import java.util.*;

public class FancyPantsOrEquippable extends DynamicItemModifier {
    private Material newMaterial = Material.IRON_CHESTPLATE;
    private String model = null;
    private EquipmentSlot slot = EquipmentSlot.HEAD;
    private String cameraOverlay = null;

    private Material oldMaterial = Material.LEATHER_CHESTPLATE;
    private int red = 0;
    private int green = 0;
    private int blue = 0;

    public FancyPantsOrEquippable(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_21_3)) {
            if (model == null) return;
            context.getItem().type(newMaterial);
            EquippableWrapper wrapper = ValhallaMMO.getNms().getEquippable(context.getItem().getMeta());
            if (wrapper == null) wrapper = new EquippableWrapper(model, slot, cameraOverlay, null, null);
            else wrapper = new EquippableWrapper(model, slot, cameraOverlay == null ? wrapper.cameraOverlayKey() : cameraOverlay, wrapper.equipSound(), wrapper.allowedTypes());
            ValhallaMMO.getNms().setEquippable(context.getItem().getMeta(),
                    wrapper.modelKey(),
                    wrapper.slot(),
                    wrapper.cameraOverlayKey(),
                    wrapper.equipSound(),
                    wrapper.allowedTypes() == null ? null : new ArrayList<>(wrapper.allowedTypes()));
        } else {
            context.getItem().type(oldMaterial);
            context.getItem().color(Color.fromRGB(red, green, blue));
        }
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 5) {
            if (!ItemUtils.isEmpty(e.getCursor())) oldMaterial = e.getCursor().getType();
        } else if (button == 7 || button == 8 || button == 9) {
            ItemStack cursor = e.getCursor();
            if (!ItemUtils.isEmpty(cursor)) {
                ItemMeta meta = cursor.getItemMeta();
                if (meta instanceof Colorable c && c.getColor() != null) {
                    Color color = c.getColor().getColor();
                    red = color.getRed();
                    green = color.getGreen();
                    blue = color.getBlue();
                }
            } else {
                if (button == 7)
                    red = Math.min(255, Math.max(0, red + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 25 : 1))));
                else if (button == 8)
                    green = Math.min(255, Math.max(0, green + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 25 : 1))));
                else
                    blue = Math.min(255, Math.max(0, blue + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 25 : 1))));
            }
        } else if (button == 15) {
            if (!ItemUtils.isEmpty(e.getCursor())) newMaterial = e.getCursor().getType();
        } else if (button == 17){
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
        } else if (button == 19){
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
        String hex = Utils.rgbToHex(red, green, blue);
        return new Pair<>(17,
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
                new Pair<>(19,
                        new ItemBuilder(Material.ARMOR_STAND)
                                .name("&fWhich slot should the item be wearable in?")
                                .lore("&fCurrently set to &e" + slot,
                                        "&fDetermines the slot in which",
                                        "&fthe item can be worn",
                                        "&6Click to cycle options")
                                .get()),
                new Pair<>(15,
                        new ItemBuilder(Material.ARMOR_STAND)
                                .name("&fWhat should the item be in 1.21.3+?")
                                .lore("&fCurrently set to &e" + newMaterial,
                                        "&fIf the server is running a minecraft",
                                        "&fversion compatible with the equippable",
                                        "&fcomponent, allowing for proper custom",
                                        "&farmors, then the item type is set to",
                                        "&f" + newMaterial,
                                        "&6Click with item to copy type")
                                .get()),
                new Pair<>(5,
                        new ItemBuilder(Material.ARMOR_STAND)
                                .name("&fWhat should the item be in below 1.21.3?")
                                .lore("&fCurrently set to &e" + oldMaterial,
                                        "&fIf the server is running a minecraft",
                                        "&fversion &cincompatible&f with the equippable",
                                        "&fcomponent, and instead uses the FancyPants",
                                        "&fshader for custom armors, then the material",
                                        "&fis set to " + oldMaterial,
                                        "&cThis should generally be leather armor",
                                        "&cbecause FancyPants relies on it being",
                                        "&ccolorable",
                                        "&6Click with item to copy type")
                                .get()),
                new Pair<>(7,
                        new ItemBuilder(Material.POTION)
                                .name("&cHow red should it be?")
                                .lore("&6Click with another item to copy",
                                        "&6its custom model data over.",
                                        "&fSet to &" + hex + hex,
                                        "&c" + red + "&7| &a" + green + "&7| &b" + blue,
                                        "&6Click to add/subtract 1000000",
                                        "&6Shift-Click to add/subtract 100000")
                                .color(Color.fromRGB(red, green, blue))
                                .flag(ConventionUtils.getHidePotionEffectsFlag())
                                .get()),
                new Pair<>(8,
                        new ItemBuilder(Material.POTION)
                                .name("&aHow green should it be?")
                                .lore("&6Click with another item to copy",
                                        "&6its custom model data over.",
                                        "&fSet to &" + hex + hex,
                                        "&c" + red + "&7| &a" + green + "&7| &b" + blue,
                                        "&6Click to add/subtract 10000",
                                        "&6Shift-Click to add/subtract 1000")
                                .color(Color.fromRGB(red, green, blue))
                                .flag(ConventionUtils.getHidePotionEffectsFlag())
                                .get()),
                new Pair<>(9,
                        new ItemBuilder(Material.POTION)
                                .name("&bHow blue should it be?")
                                .lore("&6Click with another item to copy",
                                        "&6its custom model data over.",
                                        "&fSet to &" + hex + hex,
                                        "&c" + red + "&7| &a" + green + "&7| &b" + blue,
                                        "&6Click to add/subtract 25",
                                        "&6Shift-Click to add/subtract 1")
                                .color(Color.fromRGB(red, green, blue))
                                .flag(ConventionUtils.getHidePotionEffectsFlag())
                                .get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.LEATHER_CHESTPLATE).get();
    }

    @Override
    public String getDisplayName() {
        return "&aCustom Armor: &eFancyPants or EquippableComponent";
    }

    @Override
    public String getDescription() {
        return "&fChanges the item type depending on version. If FancyPants is required for custom armors, color the leather armor. If not, use EquippableComponent";
    }

    @Override
    public String getActiveDescription() {
        return "&fChanges the item type depending on version. If FancyPants is required for custom armors, color the leather armor. If not, use EquippableComponent";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setNewMaterial(Material newMaterial) {
        this.newMaterial = newMaterial;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public void setOldMaterial(Material oldMaterial) {
        this.oldMaterial = oldMaterial;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public void setCameraOverlay(String cameraOverlay) {
        this.cameraOverlay = cameraOverlay;
    }

    public void setSlot(EquipmentSlot slot) {
        this.slot = slot;
    }

    @Override
    public DynamicItemModifier copy() {
        FancyPantsOrEquippable m = new FancyPantsOrEquippable(getName());
        m.setModel(this.model);
        m.setSlot(this.slot);
        m.setCameraOverlay(this.cameraOverlay);
        m.setPriority(this.getPriority());
        m.setBlue(this.blue);
        m.setGreen(this.green);
        m.setRed(this.red);
        m.setNewMaterial(this.newMaterial);
        m.setOldMaterial(this.oldMaterial);
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        return "This modifier is too complex to be used with a command";
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
