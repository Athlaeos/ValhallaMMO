package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_effects;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierScalingPresets;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.dom.Scaling;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.SmithingItemPropertyManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.item.AlchemyItemPropertyManager;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class ScaleDuration extends DynamicItemModifier {
    private String presetScaling = null;
    private String commandScaling = null;
    private Scaling.ScalingMode mode = Scaling.ScalingMode.MULTIPLIER;
    private Double lowerBound = null;
    private Double upperBound = null;
    private double amplifier = 2;
    private double skillRange = 300;
    private double rangeOffset = 0;
    private double minimum = 1;
    private String skillToScaleWith = "ALCHEMY";

    private double skillEfficiency = 1;
    private double minimumValue = 0;

    public ScaleDuration(String name) {
        super(name);
    }

    private String buildScaling(){
        return String.format("(%.2f/%.2f) * (%%rating%% - %.2f) + %.2f", amplifier, skillRange, rangeOffset, minimum);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        Scaling scaling;
        if (presetScaling != null) scaling = ModifierScalingPresets.getScalings().get(presetScaling);
        else if (commandScaling != null) scaling = new Scaling(commandScaling, mode, lowerBound, upperBound);
        else scaling = new Scaling(buildScaling(), mode, lowerBound, upperBound);
        if (scaling == null) {
            failedRecipe(outputItem, "&cRecipe scaling wrongly configured, contact admin");
            return;
        }
        int skill;
        switch (skillToScaleWith) {
            case "SMITHING" -> skill = SmithingItemPropertyManager.getQuality(outputItem.getMeta());
            case "ALCHEMY" -> skill = AlchemyItemPropertyManager.getQuality(outputItem.getMeta());
            case "ENCHANTING" -> {
                skill = (int) AccumulativeStatManager.getCachedStats("ENCHANTING_QUALITY", crafter, 10000, true);
                skill = (int) (skill * (1 + AccumulativeStatManager.getCachedStats("ENCHANTING_FRACTION_QUALITY", crafter, 10000, true)));
            }
            default -> {
                Skill s = SkillRegistry.getSkill(skillToScaleWith);
                Profile profile = ProfileCache.getOrCache(crafter, s.getProfileType());
                skill = profile.getLevel();
            }
        }

        int finalQuality = (int) Math.round(skillEfficiency * skill);
        AlchemyItemPropertyManager.applyAttributeScaling(outputItem.getMeta(), scaling, finalQuality, true, minimumValue, 0);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        switch (button){
            case 0 -> {
                List<String> scalings = new ArrayList<>(ModifierScalingPresets.getScalings().keySet());
                int currentScaling = presetScaling == null ? -1 : scalings.indexOf(presetScaling);
                if (e.isLeftClick()) {
                    if (currentScaling + 1 >= scalings.size()) currentScaling = 0;
                    else currentScaling++;
                } else {
                    if (currentScaling - 1 < 0) currentScaling = scalings.size() - 1;
                    else currentScaling--;
                }
                presetScaling = scalings.get(currentScaling);
            }
            case 2 -> mode = (mode == Scaling.ScalingMode.MULTIPLIER ? Scaling.ScalingMode.ADD_ON_DEFAULT : Scaling.ScalingMode.MULTIPLIER);
            case 9 -> {
                if (e.getClick() == ClickType.MIDDLE) upperBound = null;
                else if (lowerBound != null) upperBound = Math.max(lowerBound, (upperBound == null ? 0 : upperBound) + ((e.isRightClick() ? -1 : 1) * (e.isShiftClick() ? 0.25 : 0.01)));
                else upperBound = (upperBound == null ? 0 : upperBound) + ((e.isRightClick() ? -1 : 1) * (e.isShiftClick() ? 0.25 : 0.01));
            }
            case 10 -> amplifier = amplifier + ((e.isRightClick() ? -1 : 1) * (mode == Scaling.ScalingMode.MULTIPLIER ? (e.isShiftClick() ? 0.25 : 0.01) : (e.isShiftClick() ? 1200 : 20)));
            case 11 -> skillRange = skillRange + ((e.isRightClick() ? -1 : 1) * (e.isShiftClick() ? 25 : 1));
            case 12 -> rangeOffset = rangeOffset + ((e.isRightClick() ? -1 : 1) * (e.isShiftClick() ? 10 : 1));
            case 13 -> minimum = minimum + ((e.isRightClick() ? -1 : 1) * (mode == Scaling.ScalingMode.MULTIPLIER ? (e.isShiftClick() ? 0.25 : 0.01) : (e.isShiftClick() ? 1200 : 20)));
            case 14 -> {
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
            case 19 -> {
                if (e.getClick() == ClickType.MIDDLE) lowerBound = null;
                else if (upperBound != null) lowerBound = Math.min(upperBound, (lowerBound == null ? 0 : lowerBound) + ((e.isRightClick() ? -1 : 1) * (e.isShiftClick() ? 0.25 : 0.01)));
                else lowerBound = (lowerBound == null ? 0 : lowerBound) + ((e.isRightClick() ? -1 : 1) * (e.isShiftClick() ? 0.25 : 0.01));
            }
            case 21 -> skillEfficiency = Math.max(0, skillEfficiency + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.1 : 0.01)));
            case 23 -> minimumValue = Math.max(0, minimumValue + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.1 : 0.01)));
        }
        if (button != 0 && button != 21 && button != 23 && getButtons().containsKey(button)) presetScaling = null;
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(21,
                new ItemBuilder(Material.NETHER_STAR)
                        .name("&fSkill Efficiency")
                        .lore(String.format("&fCurrent Scaling: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getExpression() : buildScaling()),
                                String.format("&fSkill efficiency: %.0f%%", skillEfficiency * 100),
                                String.format("&fMinimum fraction of duration: &e%.2fx", minimumValue),
                                "&fSkill efficiency determines how much",
                                String.format("&fof the player's &e%s&f skill is used ", StringUtils.toPascalCase(skillToScaleWith)),
                                "&fin the formula.",
                                "&6Click to add/subtract 1%",
                                "&6Shift-Click to add/subtract 10%")
                        .get()).map(
                Set.of(
                        new Pair<>(23, new ItemBuilder(Material.PAPER)
                                .name("&fMinimum fraction")
                                .lore(String.format("&fCurrent Scaling: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getExpression() : buildScaling()),
                                        String.format("&fCurrent Mode: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getScalingType() : mode),
                                        "&fThe resulting duration will always be at",
                                        String.format("&fleast &e%.2fx&f of its default", minimumValue),
                                        "&fvalue.",
                                        "&6Click to add/subtract 0.01",
                                        "&6Shift-Click to add/subtract 0.1").get()),
                        new Pair<>(0, new ItemBuilder(Material.BOOK)
                                .name("&fPreset Scalings")
                                .lore(String.format("&fCurrent Scaling: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getExpression() : buildScaling()),
                                        String.format("&fCurrent Mode: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getScalingType() : mode),
                                        "&fSets the duration to scale with a",
                                        "&fpreconfigured scaling formula.",
                                        "&fClicking another button removes",
                                        "&fthis scaling in favor of the custom",
                                        "&fone",
                                        "&6Click to cycle").get()),
                        new Pair<>(2, new ItemBuilder(Material.REDSTONE_TORCH)
                                .name("&fScaling Mode")
                                .lore(String.format("&fCurrent Scaling: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getExpression() : buildScaling()),
                                        String.format("&fCurrent Mode: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getScalingType() : mode),
                                        "&fSets the scaling behavior.",
                                        "&fADD_ON_DEFAULT adds the result of",
                                        "&fthe formula to the default duration.",
                                        "&fMULTIPLIER multiplies the default duration",
                                        "&fwith the result of the formula.",
                                        "&6Click to cycle").get()),
                        new Pair<>(9, new ItemBuilder(Material.LIME_DYE)
                                .name("&fUpper Bound")
                                .lore(String.format("&fCurrent Scaling: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getExpression() : buildScaling()),
                                        String.format("&fCurrent Mode: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getScalingType() : mode),
                                        String.format("&fSet to: %s", upperBound == null ? "none" : String.format("%.2f", upperBound)),
                                        "&fSets the maximum value that can",
                                        "&fbe produced with the formula.",
                                        "&6Click to add/subtract 0.01",
                                        "&6Shift-Click to add/subtract 0.25",
                                        "&cMiddle-Click to remove").get()),
                        new Pair<>(10, new ItemBuilder(Material.GLOWSTONE_DUST)
                                .name("&fAmplifier")
                                .lore(String.format("&fCurrent Scaling: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getExpression() : buildScaling()),
                                        String.format("&fCurrent Mode: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getScalingType() : mode),
                                        String.format("&fSet to: %.2f", amplifier),
                                        "&fSets how much the formula will",
                                        "&fscale up over the course of",
                                        String.format("&e%.0f &f%s skill", skillRange, StringUtils.toPascalCase(skillToScaleWith)),
                                        "&6Click to add/subtract " + (mode == Scaling.ScalingMode.MULTIPLIER ? "0.01" : "20"),
                                        "&6Shift-Click to add/subtract " + (mode == Scaling.ScalingMode.MULTIPLIER ? "0.25" : "1200")).get()),
                        new Pair<>(11, new ItemBuilder(Material.REDSTONE)
                                .name("&fSkill Range")
                                .lore(String.format("&fCurrent Scaling: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getExpression() : buildScaling()),
                                        String.format("&fCurrent Mode: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getScalingType() : mode),
                                        String.format("&fSet to: %.0f", skillRange),
                                        "&fSets the range of skill required ",
                                        String.format("&ffor the formula to go up by %.2f", amplifier),
                                        "&6Click to add/subtract 1",
                                        "&6Shift-Click to add/subtract 25").get()),
                        new Pair<>(12, new ItemBuilder(Material.PISTON)
                                .name("&fOffset")
                                .lore(String.format("&fCurrent Scaling: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getExpression() : buildScaling()),
                                        String.format("&fCurrent Mode: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getScalingType() : mode),
                                        String.format("&fSet to: %.0f", rangeOffset),
                                        "&fMoves the pivot point to a",
                                        "&fdifferent skill point.",
                                        "&aKeep in mind negative values equate",
                                        "&ato higher skill requirements.",
                                        "&fFor example, for -50 that means",
                                        String.format("&f50 skill is required to reach %.2f", minimum),
                                        "&6Click to add/subtract 1",
                                        "&6Shift-Click to add/subtract 10").get()),
                        new Pair<>(13, new ItemBuilder(Material.BOOK)
                                .name("&fMinimum (Pivot Point)")
                                .lore(String.format("&fCurrent Scaling: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getExpression() : buildScaling()),
                                        String.format("&fCurrent Mode: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getScalingType() : mode),
                                        String.format("&fSet to: %.0f", minimum),
                                        "&fSets the baseline value the",
                                        "&fformula will produce. ",
                                        String.format("&f%.0f skill is required to reach %.2f", -rangeOffset, minimum),
                                        "&6Click to add/subtract " + (mode == Scaling.ScalingMode.MULTIPLIER ? "0.01" : "20"),
                                        "&6Shift-Click to add/subtract " + (mode == Scaling.ScalingMode.MULTIPLIER ? "0.25" : "1200")).get()),
                        new Pair<>(14, new ItemBuilder(Material.BOOK)
                                .name("&fSkill to use")
                                .lore(String.format("&fCurrent Scaling: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getExpression() : buildScaling()),
                                        String.format("&fCurrent Mode: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getScalingType() : mode),
                                        String.format("&fSet to: %s", StringUtils.toPascalCase(skillToScaleWith)),
                                        "&fSets which skill should be used",
                                        "&fto define the scaling's strength",
                                        "&eSmithing and Alchemy are exceptions",
                                        "&fwhere they base %rating% off of the",
                                        "&fitem's quality instead of player skill",
                                        "&6Click to cycle").get()),
                        new Pair<>(19, new ItemBuilder(Material.LIME_DYE)
                                .name("&fLower Bound")
                                .lore(String.format("&fCurrent Scaling: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getExpression() : buildScaling()),
                                        String.format("&fCurrent Mode: &e%s", presetScaling != null ? ModifierScalingPresets.getScalings().get(presetScaling).getScalingType() : mode),
                                        String.format("&fSet to: %s", lowerBound == null ? "none" : String.format("%.2f", lowerBound)),
                                        "&fSets the minimum value that can",
                                        "&fbe produced with the formula.",
                                        "&6Click to add/subtract 0.01",
                                        "&6Shift-Click to add/subtract 0.25",
                                        "&cMiddle-Click to remove").get())
                )
        );
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.REDSTONE).get();
    }

    @Override
    public String getDisplayName() {
        return "&cScale Potion Duration";
    }

    @Override
    public String getDescription() {
        return "&fScales the item's duration according to a defined formula";
    }

    @Override
    public String getActiveDescription() {
        return String.format("&fScales the item's duration according to the formula %s, using the mode %s. %s%s",
                buildScaling(), mode,
                lowerBound == null ? "" : "&fResult cannot go below " + lowerBound + ". ",
                upperBound == null ? "" : "&fResult cannot go above " + upperBound + ".");
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.POTION_MISC.id());
    }

    public void setSkillEfficiency(double skillEfficiency) {
        this.skillEfficiency = skillEfficiency;
    }

    public void setLowerBound(Double lowerBound) {
        this.lowerBound = lowerBound;
    }

    public void setUpperBound(Double upperBound) {
        this.upperBound = upperBound;
    }

    public void setAmplifier(double amplifier) {
        this.amplifier = amplifier;
    }

    public void setCommandScaling(String commandScaling) {
        this.commandScaling = commandScaling;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public void setMinimumValue(double minimumValue) {
        this.minimumValue = minimumValue;
    }

    public void setMode(Scaling.ScalingMode mode) {
        this.mode = mode;
    }

    public void setPresetScaling(String presetScaling) {
        this.presetScaling = presetScaling;
    }

    public void setRangeOffset(double rangeOffset) {
        this.rangeOffset = rangeOffset;
    }

    public void setSkillRange(double skillRange) {
        this.skillRange = skillRange;
    }

    public void setSkillToScaleWith(String skillToScaleWith) {
        this.skillToScaleWith = skillToScaleWith;
    }

    public double getRangeOffset() {
        return rangeOffset;
    }

    public String getPresetScaling() {
        return presetScaling;
    }

    @Override
    public DynamicItemModifier copy() {
        ScaleDuration m = new ScaleDuration(getName());
        m.setPresetScaling(this.presetScaling);
        m.setAmplifier(this.amplifier);
        m.setCommandScaling(this.commandScaling);
        m.setMinimum(this.minimum);
        m.setLowerBound(this.lowerBound);
        m.setMinimumValue(this.minimumValue);
        m.setMode(this.mode);
        m.setRangeOffset(this.rangeOffset);
        m.setSkillEfficiency(this.skillEfficiency);
        m.setSkillRange(this.skillRange);
        m.setSkillToScaleWith(this.skillToScaleWith);
        m.setUpperBound(this.upperBound);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 6) return "Six arguments are expected: two doubles, a formula, a mode, and a lower- and upper bound";
        try {
            skillEfficiency = StringUtils.parseDouble(args[0]);
            minimumValue = StringUtils.parseDouble(args[1]);
            commandScaling = args[2];
            mode = Scaling.ScalingMode.valueOf(args[3]);
            lowerBound = Catch.catchOrElse(() -> StringUtils.parseDouble(args[4]), null);
            upperBound = Catch.catchOrElse(() -> StringUtils.parseDouble(args[5]), null);
        } catch (IllegalArgumentException ignored){
            return "Six arguments are expected: two doubles, a formula, a mode, and a lower- and upper bound. At least one was not a number, or the mode could be incorrect";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<skill_efficiency>");
        if (currentArg == 1) return List.of("<minimum_fraction>");
        if (currentArg == 2) return List.of("<expression>");
        if (currentArg == 3) return Arrays.stream(Scaling.ScalingMode.values()).map(Scaling.ScalingMode::toString).collect(Collectors.toList());
        if (currentArg == 4) return List.of("<lower_bound_or_none>");
        if (currentArg == 5) return List.of("<upper_bound_or_none>");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 6;
    }
}
