package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.version.ConventionUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class FlagVanillaAdd extends DynamicItemModifier {
    private String flag;

    public FlagVanillaAdd(String name, String flag) {
        super(name);
        this.flag = flag;
    }

    @Override
    public void processItem(ModifierContext context) {
        correctFlag();
        context.getItem().flag(ItemFlag.valueOf(flag));
        PotionEffectRegistry.updateEffectLore(context.getItem().getMeta());
    }

    private void correctFlag(){
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) &&
                (flag == null || flag.equalsIgnoreCase("HIDE_POTION_EFFECTS"))) flag = "HIDE_ADDITIONAL_TOOLTIP";
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getModifierIcon() {
        correctFlag();
        return new ItemBuilder(switch (flag){
            case "HIDE_DYE" -> Material.RED_DYE;
            case "HIDE_DESTROYS" -> Material.GOLDEN_PICKAXE;
            case "HIDE_ENCHANTS" -> Material.ENCHANTED_BOOK;
            case "HIDE_PLACED_ON" -> Material.BRICKS;
            case "HIDE_ATTRIBUTES", "HIDE_ADDITIONAL_TOOLTIP" -> Material.PAPER;
            case "HIDE_UNBREAKABLE" -> Material.DIAMOND_PICKAXE;
            case "HIDE_POTION_EFFECTS" -> Material.POTION;
            case "HIDE_ARMOR_TRIM" -> Material.BRICK;
            default -> Material.BARRIER;
        }).flag(ConventionUtils.getHidePotionEffectsFlag()).get();
    }

    @Override
    public String getDisplayName() {
        correctFlag();
        return "&7Add Vanilla Item Flag: &e" + StringUtils.toPascalCase(flag.toLowerCase(java.util.Locale.US).replace("_", " "));
    }

    @Override
    public String getDescription() {
        correctFlag();
        return "&fAdds the " + StringUtils.toPascalCase(flag.toLowerCase(java.util.Locale.US).replace("_", " ")) + " item flag to the item.";
    }

    @Override
    public String getActiveDescription() {
        correctFlag();
        return "&fAdds the " + StringUtils.toPascalCase(flag.toLowerCase(java.util.Locale.US).replace("_", " ")) + " item flag to the item.";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_FLAGS.id());
    }

    @Override
    public DynamicItemModifier copy() {
        correctFlag();
        FlagVanillaAdd m = new FlagVanillaAdd(getName(), flag);
        m.setPriority(this.getPriority());
        return m;
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
