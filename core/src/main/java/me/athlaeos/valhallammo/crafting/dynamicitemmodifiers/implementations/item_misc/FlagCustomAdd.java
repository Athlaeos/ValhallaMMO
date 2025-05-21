package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class FlagCustomAdd extends DynamicItemModifier {
    private final CustomFlag flag;

    public FlagCustomAdd(String name, CustomFlag flag) {
        super(name);
        this.flag = flag;
    }

    @Override
    public void processItem(ModifierContext context) {
        context.getItem().flag(flag);
        PotionEffectRegistry.updateEffectLore(context.getItem().getMeta());
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
        return new ItemBuilder(switch (flag){
            case UNCRAFTABLE -> Material.BARRIER;
            case HIDE_TAGS -> Material.INK_SAC;
            case HIDE_QUALITY -> Material.COAL;
            case HIDE_DURABILITY -> Material.WOODEN_PICKAXE;
            case DISPLAY_ATTRIBUTES -> Material.NAME_TAG;
            case ATTRIBUTE_FOR_BOTH_HANDS -> Material.GOLDEN_SWORD;
            case ATTRIBUTE_FOR_HELMET -> Material.TURTLE_HELMET;
            case INFINITY_EXPLOITABLE -> Material.ARROW;
            case UNENCHANTABLE -> Material.ENCHANTING_TABLE;
            case TEMPORARY_POTION_DISPLAY -> Material.GLASS_BOTTLE;
            case UNMENDABLE -> Material.ANVIL;
        }).get();
    }

    @Override
    public String getDisplayName() {
        return "&7Add Custom Item Flag: &e" + StringUtils.toPascalCase(flag.toString().toLowerCase(java.util.Locale.US).replace("_", " "));
    }

    @Override
    public String getDescription() {
        return "&fAdds the " + StringUtils.toPascalCase(flag.toString().toLowerCase(java.util.Locale.US).replace("_", " ")) + " item flag to the item.";
    }

    @Override
    public String getActiveDescription() {
        return "&fAdds the " + StringUtils.toPascalCase(flag.toString().toLowerCase(java.util.Locale.US).replace("_", " ")) + " item flag to the item.";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_FLAGS.id());
    }

    @Override
    public DynamicItemModifier copy() {
        FlagCustomAdd m = new FlagCustomAdd(getName(), flag);
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
