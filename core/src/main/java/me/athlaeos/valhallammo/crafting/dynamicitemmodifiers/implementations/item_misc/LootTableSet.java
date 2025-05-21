package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class LootTableSet extends DynamicItemModifier {
    private String lootTable = null;
    private boolean freeSelection = false;
    private boolean repeatSelection = false;
    private Sound lootSound = null;

    public LootTableSet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        LootTableRegistry.setLootTable(context.getItem().getMeta(), LootTableRegistry.getLootTables().get(lootTable));
        LootTableRegistry.setFreeSelectionTable(context.getItem().getMeta(), freeSelection, repeatSelection);
        LootTableRegistry.setLootSound(context.getItem().getMeta(), lootSound);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        List<LootTable> tables = new ArrayList<>(LootTableRegistry.getLootTables().values());
        if (button == 12){
            if (e.isShiftClick()) lootTable = null;
            else {
                LootTable table = LootTableRegistry.getLootTables().get(lootTable);
                int currentTable = tables.indexOf(table);
                if (e.isLeftClick()) lootTable = tables.get(Math.max(0, Math.min(tables.size() - 1, currentTable + 1))).getKey();
                else lootTable = tables.get(Math.max(0, Math.min(tables.size() - 1, currentTable - 1))).getKey();
            }
        } else if (button == 16) freeSelection = !freeSelection;
        else if (button == 18) repeatSelection = !repeatSelection;
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.CHEST)
                        .name("&fWhich loot table?")
                        .lore("&fLoot table set to &e" + lootTable,
                                "&6Click to cycle",
                                "&6Shift-Click to remove")
                        .get()).map(Set.of(new Pair<>(16,
                new ItemBuilder(Material.GOLD_INGOT)
                        .name("&fAllow Free Selection?")
                        .lore("&fFree selection turned &e" + (freeSelection ? "&eon" : "&coff"),
                                "&fFree selection means the player,",
                                "&fwhen clicking the loot table item,",
                                "&fis shown a menu where they can freely",
                                "&fchoose which items from the loot table",
                                "&fthey want. See note for more info.",
                                "&6Click to toggle")
                        .get()),new Pair<>(18,
                new ItemBuilder(Material.CHEST)
                        .name("&fAllow Repeated Selection?")
                        .lore("&fRepeat selection turned &e" + (freeSelection ? "&eon" : "&coff"),
                                "&fIf repeat selectio is enabled,",
                                "&fthe player may choose the same ",
                                "&floot item several times. If not,",
                                "&fthe player may choose each once only.",
                                "&fOnly works if free selection is on.",
                                "&6Click to toggle")
                        .get()),new Pair<>(22,
                new ItemBuilder(Material.PAPER)
                        .name("&fWhat is free selection?")
                        .lore("&fWith free selection enabled, ",
                                "&fthe player is prompted a menu ",
                                "&fin which they may choose their loot.",
                                "&fThe amount of times they are allowed",
                                "&fto choose depends on the amount of",
                                "&frolls determined by the &efirst",
                                "&floot pool in the loot table.",
                                "&fGuaranteed drops will always be",
                                "&fgranted, but aside from that ",
                                "&findividual loot chance or weight",
                                "&fdoes not matter.")
                        .get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.CHEST).get();
    }

    @Override
    public String getDisplayName() {
        return "&dLoot Table";
    }

    @Override
    public String getDescription() {
        return "&fAdds a loot table to/removes it from the item. Placeable blocks get this loot table applied to them when placed, items gift the loot table when clicked (consumed afterwards)";
    }

    @Override
    public String getActiveDescription() {
        return lootTable == null ? "&fRemoving loot table from item" : "&fSetting the loot table to &e" + lootTable;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setFreeSelection(boolean freeSelection) {
        this.freeSelection = freeSelection;
    }

    public void setLootSound(Sound lootSound) {
        this.lootSound = lootSound;
    }

    public void setLootTable(String lootTable) {
        this.lootTable = lootTable;
    }

    public void setRepeatSelection(boolean repeatSelection) {
        this.repeatSelection = repeatSelection;
    }

    @Override
    public DynamicItemModifier copy() {
        LootTableSet m = new LootTableSet(getName());
        m.setLootSound(this.lootSound);
        m.setLootTable(this.lootTable);
        m.setFreeSelection(this.freeSelection);
        m.setRepeatSelection(this.repeatSelection);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length < 3) return "Three/four arguments are expected: the name of the loot table, a yes/no whether it should be a freely selectable loot table, a yes/no whether repeat picks are allowed, and optionally the sound the item makes when loot is claimed";
        lootTable = args[0];
        freeSelection = args[1].equalsIgnoreCase("yes");
        repeatSelection = args[2].equalsIgnoreCase("no");
        if (args.length >= 4) {
            try {
                lootSound = Sound.valueOf(args[3]);
            } catch (IllegalArgumentException ignored) {
                executor.sendMessage(Utils.chat("&cInvalid sound!"));
            }
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return new ArrayList<>(LootTableRegistry.getLootTables().keySet());
        if (currentArg == 1) return List.of("<select_freely?>", "yes", "no");
        if (currentArg == 2) return List.of("<select_repeatedly?>", "yes", "no");
        if (currentArg == 3) return Arrays.stream(Sound.values()).map(Sound::toString).collect(Collectors.toList());
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 4;
    }
}
