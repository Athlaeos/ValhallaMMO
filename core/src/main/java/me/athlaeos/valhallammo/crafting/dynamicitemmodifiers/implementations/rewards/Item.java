package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.rewards;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class Item extends DynamicItemModifier {
    private final List<ItemStack> rewards = new ArrayList<>();
    private ItemStack currentReward = new ItemStack(Material.GOLD_INGOT);

    public Item(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (!context.shouldExecuteUsageMechanics()) return;
        Map<ItemStack, Integer> compressed = ItemUtils.compressStacks(rewards);
        Map<ItemStack, Integer> afterMultiplication = new HashMap<>();
        for (ItemStack item : compressed.keySet()){
            afterMultiplication.put(item, compressed.get(item) * context.getTimesExecuted());
        }
        List<ItemStack> decompressed = ItemUtils.decompressStacks(afterMultiplication);
        decompressed.forEach(i -> ItemUtils.addItem(context.getCrafter(), i, true));
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            ItemStack cursor = e.getCursor();
            if (!ItemUtils.isEmpty(cursor)) {
                currentReward = cursor.clone();
            }
        } else if (button == 17){
            if (e.isShiftClick()) rewards.clear();
            else rewards.add(currentReward);
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        Map<ItemStack, Integer> compressedItems = ItemUtils.compressStacks(rewards);
        List<String> tagLore = compressedItems.isEmpty() ? List.of("&cNothing") : compressedItems.entrySet().stream().map(t ->
                String.format("&e%d&7x&e%s", t.getValue(), ItemUtils.getItemName(ItemUtils.getItemMeta(t.getKey()))))
                .collect(Collectors.toList());
        return new Pair<>(12,
                new ItemBuilder(currentReward)
                        .name("&eWhat should the new item be?")
                        .lore("&6Click with another item to",
                                "&6copy it over.",
                                "&fSet to " + ItemUtils.getItemName(ItemUtils.getItemMeta(currentReward)))
                        .get()).map(Set.of(
                new Pair<>(17,
                        new ItemBuilder(Material.STRUCTURE_VOID)
                                .name("&fConfirm Item")
                                .lore("&fCurrently selected: &e" + ItemUtils.getItemName(ItemUtils.getItemMeta(currentReward)),
                                        "&6Click to add selected item to",
                                        "&6the list.",
                                        "&6Shift-Click to clear list",
                                        "&fCurrent rewards:")
                                .appendLore(tagLore)
                                .get())
        ));
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.CHEST).get();
    }

    @Override
    public String getDisplayName() {
        return "&eItem Rewards";
    }

    @Override
    public String getDescription() {
        return "&fGives the player one or several items";
    }

    @Override
    public String getActiveDescription() {
        return "&fGives the player the following items: /n&e" + (rewards.isEmpty() ? List.of("&cNothing") : rewards.stream().map(t ->
                "&e" + ItemUtils.getItemName(ItemUtils.getItemMeta(t)))
                .collect(Collectors.joining(", ")));
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.REWARDS.id());
    }

    public void setCurrentReward(ItemStack currentReward) {
        this.currentReward = currentReward;
    }

    @Override
    public DynamicItemModifier copy() {
        Item m = new Item(getName());
        m.setCurrentReward(this.currentReward);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument is expected: a material(or 'hand') representing the item to add";
        try {
            if (args[0].equalsIgnoreCase("hand") && !(executor instanceof Player)) return "This argument requires you to be a player for usage";
            ItemStack item = args[0].equalsIgnoreCase("hand") && executor instanceof Player p ?
                    p.getInventory().getItemInMainHand() :
                    new ItemStack(Material.valueOf(args[0]));
            if (!ItemUtils.isEmpty(item)) rewards.add(item);
        } catch (IllegalArgumentException ignored){
            return "One argument is expected: a material(or 'hand') representing the item to add. No valid material was provided";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return Arrays.stream(Material.values()).map(Object::toString).collect(Collectors.toList());
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
