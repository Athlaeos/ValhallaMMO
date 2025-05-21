package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class LoreSet extends DynamicItemModifier {
    private List<String> lore = new ArrayList<>();
    private int mode = 0;

    public LoreSet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (lore.isEmpty()) context.getItem().lore(lore);
        else {
            switch (mode) {
                case 0 -> context.getItem().prependLore(lore);
                case 1 -> context.getItem().appendLore(lore);
                case 2 -> context.getItem().lore(lore);
            }
        }
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            if (e.isShiftClick()) lore.clear();
            else {
                ItemStack cursor = e.getCursor();
                if (ItemUtils.isEmpty(cursor)) {
                    mode = Math.max(0, Math.min(2, mode + (e.isLeftClick() ? 1 : -1)));
                } else {
                    ItemMeta meta = cursor.getItemMeta();
                    if (meta != null && meta.hasLore() && meta.getLore() != null) lore = new ArrayList<>(meta.getLore());
                    else lore.clear();
                }
            }
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.INK_SAC)
                        .name("&eWhat should the lore be?")
                        .lore(mode == 0 ? "&fThe following is added to the start" : mode == 1 ? "&fThe following is added to the end" : "&fThe lore will be set to",
                                "&8&m                                 ")
                        .appendLore(lore.isEmpty() ? List.of("&cLore is cleared") : lore)
                        .appendLore(
                                "&8&m                                 ",
                                "&6Click with another item",
                                "&6to copy its lore over.",
                                "&6Or shift-click to reset",
                                "&6the lore back to nothing.",
                                "&eClick to set the mode"
                        ).get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.WRITABLE_BOOK).get();
    }

    @Override
    public String getDisplayName() {
        return "&dLore";
    }

    @Override
    public String getDescription() {
        return "&fChanges the lore of the item";
    }

    @Override
    public String getActiveDescription() {
        return "&fChanges the lore of the item to " + (lore.isEmpty() ? "be cleared" : "/n" + String.join("/n", lore));
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    @Override
    public DynamicItemModifier copy() {
        LoreSet m = new LoreSet(getName());
        m.setLore(new ArrayList<>(this.lore));
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 2) return "You must indicate the new lore of the item, or 'null' to clear it, along with the mode. 0 = prepend, 1 = append, 2 = replace";
        if (args[0].equalsIgnoreCase("null")) lore.clear();
        else lore.addAll(Arrays.stream(args[0].split("/n")).map(l -> l.replace("/_", " ")).toList());
        Integer mode = Catch.catchOrElse(() -> Integer.parseInt(args[1]), null);
        if (mode == null || mode < 0 || mode > 2) return "Invalid/absent mode, 0 = prepend before lore, 1 = append after lore, 2 = replace lore";
        this.mode = mode;
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<lore>", "use-/n-for-new-lines", "use-/_-for-spaces");
        if (currentArg == 1) return List.of("<mode>", "0-for-prepend", "1-for-append", "2-for-replace");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 2;
    }
}
