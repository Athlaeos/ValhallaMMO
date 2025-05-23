package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.SmithingItemPropertyManager;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.dom.MerchantLevel;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MerchantSmithingQualitySet extends DynamicItemModifier {
    private final Map<MerchantLevel, Integer> qualityMap = new HashMap<>(Map.of(
            MerchantLevel.NOVICE, 30,
            MerchantLevel.APPRENTICE, 60,
            MerchantLevel.JOURNEYMAN, 90,
            MerchantLevel.EXPERT, 120,
            MerchantLevel.MASTER, 150
    ));

    public MerchantSmithingQualitySet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        MerchantData data = context.getOtherType(MerchantData.class);
        if (data == null) return;
        MerchantLevel level = CustomMerchantManager.getLevel(data);
        if (level == null) return;
        SmithingItemPropertyManager.setQuality(context.getItem().getMeta(), qualityMap.get(level));
    }

    @Override
    public boolean meetsRequirement(ModifierContext context) {
        return context.getOtherType(MerchantData.class) != null;
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 10) qualityMap.put(MerchantLevel.NOVICE, Math.max(0, qualityMap.getOrDefault(MerchantLevel.NOVICE, 30) + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1))));
        else if (button == 11) qualityMap.put(MerchantLevel.APPRENTICE, Math.max(0, qualityMap.getOrDefault(MerchantLevel.APPRENTICE, 60) + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1))));
        else if (button == 12) qualityMap.put(MerchantLevel.JOURNEYMAN, Math.max(0, qualityMap.getOrDefault(MerchantLevel.JOURNEYMAN, 90) + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1))));
        else if (button == 13) qualityMap.put(MerchantLevel.EXPERT, Math.max(0, qualityMap.getOrDefault(MerchantLevel.EXPERT, 120) + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1))));
        else if (button == 14) qualityMap.put(MerchantLevel.MASTER, Math.max(0, qualityMap.getOrDefault(MerchantLevel.MASTER, 150) + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1))));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(10,
                new ItemBuilder(Material.PAPER)
                        .name("&eWhat quality should novices get?")
                        .lore("&fApplies &e" + qualityMap.getOrDefault(MerchantLevel.NOVICE, 0) + "&f quality to the item",
                                "&fif the merchant is a NOVICE",
                                "",
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 10")
                        .get()).map(Set.of(
                new Pair<>(11,
                        new ItemBuilder(Material.PAPER)
                                .name("&eWhat quality should novices get?")
                                .lore("&fApplies &e" + qualityMap.getOrDefault(MerchantLevel.APPRENTICE, 0) + "&f quality to the item",
                                        "&fif the merchant is a APPRENTICE",
                                        "",
                                        "&6Click to add/subtract 1",
                                        "&6Shift-Click to add/subtract 10")
                                .get()),
                new Pair<>(12,
                        new ItemBuilder(Material.PAPER)
                                .name("&eWhat quality should novices get?")
                                .lore("&fApplies &e" + qualityMap.getOrDefault(MerchantLevel.JOURNEYMAN, 0) + "&f quality to the item",
                                        "&fif the merchant is a JOURNEYMAN",
                                        "",
                                        "&6Click to add/subtract 1",
                                        "&6Shift-Click to add/subtract 10")
                                .get()),
                new Pair<>(13,
                        new ItemBuilder(Material.PAPER)
                                .name("&eWhat quality should novices get?")
                                .lore("&fApplies &e" + qualityMap.getOrDefault(MerchantLevel.EXPERT, 0) + "&f quality to the item",
                                        "&fif the merchant is a EXPERT",
                                        "",
                                        "&6Click to add/subtract 1",
                                        "&6Shift-Click to add/subtract 10")
                                .get()),
                new Pair<>(14,
                        new ItemBuilder(Material.PAPER)
                                .name("&eWhat quality should novices get?")
                                .lore("&fApplies &e" + qualityMap.getOrDefault(MerchantLevel.MASTER, 0) + "&f quality to the item",
                                        "&fif the merchant is a MASTER",
                                        "",
                                        "&6Click to add/subtract 1",
                                        "&6Shift-Click to add/subtract 10")
                                .get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.EMERALD).get();
    }

    @Override
    public String getDisplayName() {
        return "&eMerchant Smithing Skill";
    }

    @Override
    public String getDescription() {
        return "&fSets the quality of the item based on the merchant's skill level. &cCan only be used in custom trades";
    }

    @Override
    public String getActiveDescription() {
        return "&fSets the quality of the item based on the merchant's skill level. &cCan only be used in custom trades";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setQualities(Map<MerchantLevel, Integer> values) {
        this.qualityMap.clear();
        this.qualityMap.putAll(values);
    }

    @Override
    public DynamicItemModifier copy() {
        MerchantSmithingQualitySet m = new MerchantSmithingQualitySet(getName());
        m.setQualities(this.qualityMap);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        return "This modifier cannot be used in a command context";
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
