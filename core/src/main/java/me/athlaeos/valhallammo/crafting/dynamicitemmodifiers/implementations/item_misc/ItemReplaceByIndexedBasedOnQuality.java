package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.*;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.*;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemReplaceByIndexedBasedOnQuality extends DynamicItemModifier implements ResultChangingModifier {
    private final Map<Integer, String> items = new HashMap<>();
    private String currentItem = null;
    private int currentQuality = 0;
    private String skillToScaleWith = "SMITHING";

    public ItemReplaceByIndexedBasedOnQuality(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        int skill = switch (skillToScaleWith) {
            case "SMITHING" -> SmithingItemPropertyManager.getQuality(context.getItem().getMeta());
            case "ALCHEMY" -> AlchemyItemPropertyManager.getQuality(context.getItem().getMeta());
            case "ENCHANTING" -> (int) (AccumulativeStatManager.getCachedStats("ENCHANTING_QUALITY", context.getCrafter(), 10000, true) * (1 + AccumulativeStatManager.getCachedStats("ENCHANTING_FRACTION_QUALITY", context.getCrafter(), 10000, true)));
            default -> ProfileCache.getOrCache(context.getCrafter(), SkillRegistry.getSkill(skillToScaleWith).getProfileType()).getLevel();
        };
        List<Integer> sortedQualities = new ArrayList<>(items.keySet());
        sortedQualities.sort(Comparator.comparingInt(i -> i));
        int highestItem = 0;
        for (Integer i : sortedQualities) {
            if (i > skill) break;
            highestItem = i;
        }
        String item = items.get(highestItem);
        if (item == null) return;
        ItemStack customItem = CustomItemRegistry.getProcessedItem(item, context.getCrafter());
        if (ItemUtils.isEmpty(customItem)) return;
        context.getItem().setItem(customItem);
        context.getItem().setMeta(ItemUtils.getItemMeta(customItem));
    }

    @Override
    public ItemStack getNewResult(ModifierContext context) {
        int skill = switch (skillToScaleWith) {
            case "SMITHING" -> SmithingItemPropertyManager.getQuality(context.getItem().getMeta());
            case "ALCHEMY" -> AlchemyItemPropertyManager.getQuality(context.getItem().getMeta());
            case "ENCHANTING" -> (int) (AccumulativeStatManager.getCachedStats("ENCHANTING_QUALITY", context.getCrafter(), 10000, true) * (1 + AccumulativeStatManager.getCachedStats("ENCHANTING_FRACTION_QUALITY", context.getCrafter(), 10000, true)));
            default -> ProfileCache.getOrCache(context.getCrafter(), SkillRegistry.getSkill(skillToScaleWith).getProfileType()).getLevel();
        };
        List<Integer> sortedQualities = new ArrayList<>(items.keySet());
        sortedQualities.sort(Comparator.comparingInt(i -> i));
        int highestItem = 0;
        for (Integer i : sortedQualities) {
            if (i > skill) break;
            highestItem = i;
        }
        String customItem = items.get(highestItem);
        return customItem == null ? context.getItem().get() : CustomItemRegistry.getProcessedItem(customItem, context.getCrafter());
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        List<CustomItem> items = new ArrayList<>(CustomItemRegistry.getItems().values());
        if (items.isEmpty()) return;
        if (button == 6){
            if (e.isShiftClick()) currentItem = null;
            else {
                CustomItem i = this.currentItem == null ? null : CustomItemRegistry.getItems().get(this.currentItem);
                int currentItem = i == null ? -1 : items.indexOf(i);
                if (e.isLeftClick()) this.currentItem = items.get(Math.max(0, Math.min(items.size() - 1, currentItem + 1))).getId();
                else this.currentItem = items.get(Math.max(0, Math.min(items.size() - 1, currentItem - 1))).getId();
            }
        }
        if (button == 7) currentQuality = Math.max(0, currentQuality + ((e.isShiftClick() ? 10 : 1) * (e.isLeftClick() ? 1 : -1)));
        if (button == 8) {
            if (currentItem == null) this.items.remove(currentQuality);
            else this.items.put(currentQuality, currentItem);
        }
        if (button == 16){
            List<String> skills = new ArrayList<>(SkillRegistry.getAllSkillsByType().keySet());
            skills.sort(Comparator.comparingInt(s -> SkillRegistry.getSkill(s).getSkillTreeMenuOrderPriority()));
            int currentSkill = skills.indexOf(skillToScaleWith);
            if (e.isLeftClick()) {
                if (currentSkill + 1 >= skills.size()) currentSkill = 0;
                else currentSkill++;
            } else {
                if (currentSkill - 1 < 0) currentSkill = skills.size() - 1;
                else currentSkill--;
            }
            skillToScaleWith = skills.get(currentSkill);
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        List<String> lore = new ArrayList<>();
        if (items.isEmpty()) lore.add("&cNo items");
        else {
            List<Integer> sortedQualities = new ArrayList<>(items.keySet());
            sortedQualities.sort(Comparator.comparingInt(i -> i));
            for (int quality : sortedQualities){
                String item = items.get(quality);
                if (item == null) continue;
                lore.add("&e" + quality + " &7>> &e" + item);
            }
        }

        return new Pair<>(6,
                new ItemBuilder(Material.ENDER_CHEST)
                        .name("&fWhich item?")
                        .lore("&fItem set to &e" + items,
                                "&7If the smithing quality of the item",
                                "&7has exceeded " + currentQuality + ", it is",
                                "&7replaced with " + currentItem,
                                "&6Click to cycle",
                                "&cShift-Click to select 'nothing'")
                        .get()).map(Set.of(
                new Pair<>(7,
                        new ItemBuilder(Material.NETHER_STAR)
                                .name("&fAt which quality should the item appear?")
                                .lore("&fQuality set to &e" + currentQuality,
                                        "&7If the smithing quality of the item",
                                        "&7has exceeded " + currentQuality + ", it is",
                                        "&7replaced with " + currentItem,
                                        "&6Click to increase/decrease by 1",
                                        "&6Shift-Click to do so by 10")
                                .get()),
                new Pair<>(8,
                        new ItemBuilder(Material.EMERALD)
                                .name("&fAdd Selection")
                                .lore("&fItem set to &e" + items,
                                        "&7If the smithing quality of the item",
                                        "&7has exceeded " + currentQuality + ", it is",
                                        "&7replaced with " + currentItem,
                                        "&6Click to cycle")
                                .get()),
                new Pair<>(18,
                        new ItemBuilder(Material.CHEST)
                                .name("&fCurrent Setup")
                                .lore("&fThe item transforms accordingly:")
                                .appendLore(lore)
                                .get()),
                new Pair<>(16, new ItemBuilder(Material.BOOK)
                        .name("&fSkill to use")
                        .lore(String.format("&fSet to: &e%s", StringUtils.toPascalCase(skillToScaleWith)),
                                "&fSets which skill should be used",
                                "&fto define the scaling's strength",
                                "&eSmithing and Alchemy are exceptions",
                                "&fwhere they base %rating% off of the",
                                "&fitem's quality instead of player skill",
                                "&6Click to cycle").get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.ENDER_CHEST).get();
    }

    @Override
    public String getDisplayName() {
        return "&dReplace Item by Custom Item at specific smithing qualities";
    }

    @Override
    public String getDescription() {
        return "&fReplaces the item by one of the item in the custom item registry (/val items) at specific smithing quality intervals";
    }

    @Override
    public String getActiveDescription() {
        return "&fReplaces the item by one of the item in the custom item registry (/val items) at specific smithing quality intervals";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setItems(Map<Integer, String> items) {
        this.items.clear();
        this.items.putAll(items);
    }

    @Override
    public DynamicItemModifier copy() {
        ItemReplaceByIndexedBasedOnQuality m = new ItemReplaceByIndexedBasedOnQuality(getName());
        m.setItems(this.items);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length < 1) return "One argument is expected: the items. Formatted like quality:item;quality2:item2;...";
        Map<Integer, String> items = new HashMap<>();
        for (String entry : args[0].split(";")){
            String[] split = entry.split(":");
            if (split.length != 2) return "Invalid substring given: '" + entry + "' is not valid. It must be formatted like quality:item";
            int quality = Catch.catchOrElse(() -> Integer.parseInt(split[0]), -1);
            String item = split[1];
            if (quality < 0) return "Invalid quality number '" + split[0] + "' given, must be a number.";
            items.put(quality, item);
        }
        setItems(items);
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<qualities:items>");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
