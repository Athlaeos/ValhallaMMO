package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class FlagVanillaAdd extends DynamicItemModifier {
    private final ItemFlag flag;

    public FlagVanillaAdd(String name, ItemFlag flag) {
        super(name);
        this.flag = flag;
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        outputItem.flag(flag);
        PotionEffectRegistry.updateEffectLore(outputItem.getMeta());
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
            case HIDE_DYE -> Material.RED_DYE;
            case HIDE_DESTROYS -> Material.GOLDEN_PICKAXE;
            case HIDE_ENCHANTS -> Material.ENCHANTED_BOOK;
            case HIDE_PLACED_ON -> Material.BRICKS;
            case HIDE_ATTRIBUTES -> Material.PAPER;
            case HIDE_UNBREAKABLE -> Material.DIAMOND_PICKAXE;
            case HIDE_POTION_EFFECTS -> Material.POTION;
        }).flag(ItemFlag.HIDE_POTION_EFFECTS).get();
    }

    @Override
    public String getDisplayName() {
        return "&7Add Vanilla Item Flag: &e" + StringUtils.toPascalCase(flag.toString().toLowerCase().replace("_", " "));
    }

    @Override
    public String getDescription() {
        return "&fAdds the " + StringUtils.toPascalCase(flag.toString().toLowerCase().replace("_", " ")) + " item flag to the item.";
    }

    @Override
    public String getActiveDescription() {
        return "&fAdds the " + StringUtils.toPascalCase(flag.toString().toLowerCase().replace("_", " ")) + " item flag to the item.";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_FLAGS.id());
    }

    @Override
    public DynamicItemModifier createNew() {
        return new FlagVanillaAdd(getName(), flag);
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
