package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.EnchantingItemPropertyManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.dom.MerchantLevel;
import me.athlaeos.valhallammo.utility.Enchanter;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.*;

public class MerchantEnchantRandomly extends DynamicItemModifier {
    private int level = 30;
    private boolean includeTreasure = false;
    private final Map<MerchantLevel, Integer> qualityMap = new HashMap<>(Map.of(
            MerchantLevel.NOVICE, 30,
            MerchantLevel.APPRENTICE, 60,
            MerchantLevel.JOURNEYMAN, 90,
            MerchantLevel.EXPERT, 120,
            MerchantLevel.MASTER, 150
    ));

    public MerchantEnchantRandomly(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        MerchantData data = context.getOtherType(MerchantData.class);
        if (data == null || level == 0) return;
        MerchantLevel merchantLevel = CustomMerchantManager.getLevel(data);
        if (merchantLevel == null) return;

        Map<Enchantment, Integer> chosenEnchantments = Enchanter.getRandomEnchantments(context.getItem().getItem(), context.getItem().getMeta(), level, includeTreasure);
        if (chosenEnchantments.isEmpty()) return;
        if (context.getItem().getItem().getType() == Material.BOOK) context.getItem().type(Material.ENCHANTED_BOOK);
        int skill = (int) Math.round(qualityMap.get(merchantLevel) * (1 + AccumulativeStatManager.getCachedStats("TRADING_SKILL_MULTIPLIER", context.getCrafter(), 10000, true)));

        for (Enchantment e : chosenEnchantments.keySet()){
            int lv = chosenEnchantments.get(e);
            lv = EnchantingItemPropertyManager.getScaledLevel(e, skill, lv);
            if (context.getItem().getMeta() instanceof EnchantmentStorageMeta eMeta) eMeta.addStoredEnchant(e, lv, false);
            else context.getItem().enchant(e, lv);
        }
    }

    @Override
    public boolean meetsRequirement(ModifierContext context) {
        return context.getOtherType(MerchantData.class) != null;
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 6) {
            level = Math.max(1, level + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 5 : 1)));
        } else if (button == 8) {
            includeTreasure = !includeTreasure;
        }

        if (button == 15) qualityMap.put(MerchantLevel.NOVICE, Math.max(0, qualityMap.getOrDefault(MerchantLevel.NOVICE, 30) + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1))));
        else if (button == 16) qualityMap.put(MerchantLevel.APPRENTICE, Math.max(0, qualityMap.getOrDefault(MerchantLevel.APPRENTICE, 60) + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1))));
        else if (button == 17) qualityMap.put(MerchantLevel.JOURNEYMAN, Math.max(0, qualityMap.getOrDefault(MerchantLevel.JOURNEYMAN, 90) + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1))));
        else if (button == 18) qualityMap.put(MerchantLevel.EXPERT, Math.max(0, qualityMap.getOrDefault(MerchantLevel.EXPERT, 120) + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1))));
        else if (button == 19) qualityMap.put(MerchantLevel.MASTER, Math.max(0, qualityMap.getOrDefault(MerchantLevel.MASTER, 150) + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1))));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(15,
                new ItemBuilder(Material.PAPER)
                        .name("&eWhat quality should novices get?")
                        .lore("&fEnchants at &e" + qualityMap.getOrDefault(MerchantLevel.NOVICE, 0) + "&f enchantment skill",
                                "&fif the merchant is a NOVICE",
                                "",
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 10")
                        .get()).map(Set.of(
                new Pair<>(16,
                        new ItemBuilder(Material.PAPER)
                                .name("&eWhat quality should novices get?")
                                .lore("&fEnchants at &e" + qualityMap.getOrDefault(MerchantLevel.APPRENTICE, 0) + "&f enchantment skill",
                                        "&fif the merchant is a APPRENTICE",
                                        "",
                                        "&6Click to add/subtract 1",
                                        "&6Shift-Click to add/subtract 10")
                                .get()),
                new Pair<>(17,
                        new ItemBuilder(Material.PAPER)
                                .name("&eWhat quality should novices get?")
                                .lore("&fEnchants at &e" + qualityMap.getOrDefault(MerchantLevel.JOURNEYMAN, 0) + "&f enchantment skill",
                                        "&fif the merchant is a JOURNEYMAN",
                                        "",
                                        "&6Click to add/subtract 1",
                                        "&6Shift-Click to add/subtract 10")
                                .get()),
                new Pair<>(18,
                        new ItemBuilder(Material.PAPER)
                                .name("&eWhat quality should novices get?")
                                .lore("&fEnchants at &e" + qualityMap.getOrDefault(MerchantLevel.EXPERT, 0) + "&f enchantment skill",
                                        "&fif the merchant is a EXPERT",
                                        "",
                                        "&6Click to add/subtract 1",
                                        "&6Shift-Click to add/subtract 10")
                                .get()),
                new Pair<>(19,
                        new ItemBuilder(Material.PAPER)
                                .name("&eWhat quality should novices get?")
                                .lore("&fEnchants at &e" + qualityMap.getOrDefault(MerchantLevel.MASTER, 0) + "&f enchantment skill",
                                        "&fif the merchant is a MASTER",
                                        "",
                                        "&6Click to add/subtract 1",
                                        "&6Shift-Click to add/subtract 10")
                                .get()),
                new Pair<>(6,
                        new ItemBuilder(Material.ENCHANTED_BOOK)
                                .name("&eWhat level should be enchanted at?")
                                .lore("&fEnchanted at level &e" + level,
                                        "&6Click to add/subtract 1",
                                        "&6Shift-Click to add/subtract 10")
                                .get()),
                new Pair<>(8,
                        new ItemBuilder(Material.CHEST)
                                .name("&eInclude treasure enchantments?")
                                .lore(includeTreasure ? "&eYes" : "&eNo",
                                        "&fTreasure enchantments do not",
                                        "&fnaturally occur through enchanting.",
                                        "&fExamples: Mending, Curse of Binding.",
                                        "&fenchanting skill.",
                                        "&6Click to toggle")
                                .get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.EMERALD).get();
    }

    @Override
    public String getDisplayName() {
        return "&eMerchant Random Enchant";
    }

    @Override
    public String getDescription() {
        return "&fRandomly enchants the item based on the merchant's skill level. &cCan only be used in custom trades";
    }

    @Override
    public String getActiveDescription() {
        return "&fRandomly enchants the item based on the merchant's skill level. &cCan only be used in custom trades";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ENCHANTMENT_MISC.id());
    }

    public void setQualities(Map<MerchantLevel, Integer> values) {
        this.qualityMap.clear();
        this.qualityMap.putAll(values);
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setIncludeTreasure(boolean includeTreasure) {
        this.includeTreasure = includeTreasure;
    }

    @Override
    public DynamicItemModifier copy() {
        MerchantEnchantRandomly m = new MerchantEnchantRandomly(getName());
        m.setQualities(this.qualityMap);
        m.setPriority(this.getPriority());
        m.setIncludeTreasure(this.includeTreasure);
        m.setLevel(this.level);
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
