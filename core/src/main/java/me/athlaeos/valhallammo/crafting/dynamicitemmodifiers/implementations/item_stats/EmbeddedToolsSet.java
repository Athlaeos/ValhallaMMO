package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.MiningSpeed;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EmbeddedToolsSet extends DynamicItemModifier {
    private Collection<MiningSpeed.EmbeddedTool> embeddedTools = new HashSet<>();

    public EmbeddedToolsSet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (embeddedTools.isEmpty()) MiningSpeed.setEmbeddedTools(context.getItem().getMeta(), null);
        else {
            Collection<MiningSpeed.EmbeddedTool> tools = MiningSpeed.getEmbeddedTools(context.getItem().getMeta());
            tools.addAll(embeddedTools);
            MiningSpeed.setEmbeddedTools(context.getItem().getMeta(), tools);
        }
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            if (e.isLeftClick() && !ItemUtils.isEmpty(e.getCursor())) embeddedTools.add(new MiningSpeed.EmbeddedTool(e.getCursor()));
            else if (e.isRightClick() && !ItemUtils.isEmpty(e.getCursor())) embeddedTools.add(new MiningSpeed.EmbeddedTool(e.getCursor().getType()));
            else if (e.isShiftClick()) embeddedTools.clear();
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.CHEST)
                        .name("&dWhich embedded tools should be added?")
                        .lore(embeddedTools.isEmpty() ? List.of("&cEmbedded tools of item will", "&cbe cleared") :
                                embeddedTools.stream().map(e ->
                                        "&f> " + (e.getMaterial() == null ? ItemUtils.getItemName(e.getItem().getItemMeta()) : StringUtils.toPascalCase(e.getMaterial().toString().replace("_", " ")))
                                ).toList())
                        .appendLore("&6Left-Click with item to add",
                                "&6Right-Click with item to add type",
                                "&6Shift-Click to clear list")
                        .get()).map(Set.of(
                                new Pair<>(7, new ItemBuilder(Material.PAPER).name("&9Info")
                                        .lore("&fEmbedded tools you can view",
                                                "&fas making an item a sort of ",
                                                "&fSwiss army knife for tools.",
                                                "&fThe item contains each tool",
                                                "&fembedded in it, and when mining",
                                                "&fblocks it will select the most",
                                                "&foptimal tool from the list for",
                                                "&fthat block.",
                                                "&eHaving a type be given as an",
                                                "&eembedded tool will cause",
                                                "&ethat tool to be used with the same",
                                                "&eitem metadata as the item you ",
                                                "&eplaced it on. Essentially, it keeps ",
                                                "&eenchantments and stuff.",
                                                "&eHaving a full item be added as an",
                                                "&eembedded tool will cause that item",
                                                "&ealone to be used").get())));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.CHEST).get();
    }

    @Override
    public String getDisplayName() {
        return "&6Embedded tools";
    }

    @Override
    public String getDescription() {
        return "&fSets the embedded tools of the item. Embedded tools can cause one tool to behave as if it were several, useful in the creation of a Paxel for example";
    }

    @Override
    public String getActiveDescription() {
        return "&fSets " + embeddedTools.size() + " embedded tools to the item.";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setEmbeddedTools(Collection<MiningSpeed.EmbeddedTool> tools) {
        this.embeddedTools = tools;
    }

    @Override
    public DynamicItemModifier copy() {
        EmbeddedToolsSet m = new EmbeddedToolsSet(getName());
        m.setEmbeddedTools(new HashSet<>(this.embeddedTools));
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument expected: the material to embed in the tool or \"offhand\" to embed the offhanded item specifically. \"clear\" may be used to wipe embedded items";
        if (args[0].equalsIgnoreCase("offhand")){
            if (executor instanceof Player p && !ItemUtils.isEmpty(p.getInventory().getItemInOffHand())){
                embeddedTools.add(new MiningSpeed.EmbeddedTool(p.getInventory().getItemInOffHand()));
            } else return "You must hold something in your offhand to be able to embed it";
        } else if (args[0].equalsIgnoreCase("clear")){
            embeddedTools.clear();
        } else {
            Material material = Catch.catchOrElse(() -> Material.valueOf(args[0]), null);
            if (material == null) return "Invalid material given";
            embeddedTools.add(new MiningSpeed.EmbeddedTool(material));
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("clear", "offhand", "<tool_material>");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
