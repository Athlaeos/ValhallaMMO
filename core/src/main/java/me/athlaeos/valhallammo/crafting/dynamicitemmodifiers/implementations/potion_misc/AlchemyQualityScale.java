package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.item.MaterialClass;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.potioneffects.EffectClass;
import me.athlaeos.valhallammo.item.AlchemyItemPropertyManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AlchemyQualityScale extends DynamicItemModifier {
    private double skillEfficiency = 1;
    private EffectClass effectClass = EffectClass.BUFF;

    public AlchemyQualityScale(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        double typeSkill = effectClass == null || effectClass == EffectClass.NEUTRAL ? 0 : AccumulativeStatManager.getCachedStats("ALCHEMY_QUALITY_" + effectClass, crafter, 10000, use);
        double generalSkill = AccumulativeStatManager.getCachedStats("ALCHEMY_QUALITY_GENERAL", crafter, 10000, use);

        AlchemyItemPropertyManager.setQuality(outputItem.getMeta(), (int) ((typeSkill + generalSkill) * skillEfficiency));
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 11)
            skillEfficiency = Math.max(0, skillEfficiency + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.1 : 0.01)));
        else if (button == 13){
            int currentType = Arrays.asList(EffectClass.values()).indexOf(effectClass);
            if (e.isLeftClick()) {
                if (currentType + 1 >= EffectClass.values().length) currentType = 0;
                else currentType++;
            } else {
                if (currentType - 1 < 0) currentType = EffectClass.values().length - 1;
                else currentType--;
            }
            effectClass = EffectClass.values()[currentType];
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(11,
                new ItemBuilder(Material.PAPER)
                        .name("&eHow much quality to set?")
                        .lore("&fSets the quality to &e" + StatFormat.PERCENTILE_BASE_1_P1.format(skillEfficiency),
                                "&fof the player's alchemy skill.",
                                "",
                                "&fFor example, 300 alchemy skill",
                                "&fis converted to &e" + (int) (Math.floor(skillEfficiency * 300)) + " &fquality",
                                "&6Click to add/subtract 1%",
                                "&6Shift-Click to add/subtract 10%")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.NETHER_STAR).get();
    }

    @Override
    public String getDisplayName() {
        return "&dAlchemy Quality (DYNAMIC)";
    }

    @Override
    public String getDescription() {
        return "&fSets an item's quality based on player skill.";
    }

    @Override
    public String getActiveDescription() {
        return "&fSets the item's quality to &e" + StatFormat.PERCENTILE_BASE_1_P1.format(skillEfficiency) + "&f of the player's skill";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.POTION_MISC.id());
    }

    public void setSkillEfficiency(double skillEfficiency) {
        this.skillEfficiency = skillEfficiency;
    }

    public void setEffectClass(EffectClass effectClass) {
        this.effectClass = effectClass;
    }

    @Override
    public DynamicItemModifier copy() {
        AlchemyQualityScale m = new AlchemyQualityScale(getName());
        m.setEffectClass(this.effectClass);
        m.setSkillEfficiency(this.skillEfficiency);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 2) return "One number is expected: a double.";
        try {
            skillEfficiency = StringUtils.parseDouble(args[0]);
        } catch (NumberFormatException ignored){
            return "One number is expected: a double. It was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<skill_quality_fraction>");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
