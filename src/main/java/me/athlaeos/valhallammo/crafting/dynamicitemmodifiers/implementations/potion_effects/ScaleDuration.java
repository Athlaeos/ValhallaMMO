package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_effects;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.dom.Scaling;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.item.AlchemyItemPropertyManager;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ScaleDuration extends DynamicItemModifier {
    private double skillEfficiency = 0.5;
    private double minimumValue = 0;

    private boolean usePredefined = false;
    private String selectedScaling = null;

    // The scaling defined by these three parameters is defined by ((amplifyBy - startAt)/skillRange) * %quality% + startAt;
    private double amplifyBy = 3;
    private double skillRange = 300;
    private double startAt = 0.5;
    private boolean multiplier = true;

    public ScaleDuration(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        int quality = AlchemyItemPropertyManager.getQuality(outputItem.getMeta());
        int finalQuality = (int) Math.round(skillEfficiency * quality);
        Scaling scaling;
        if (usePredefined){
            AlchemyItemPropertyManager.ScalingPreset preset = AlchemyItemPropertyManager.getCustomScaling(selectedScaling);
            if (preset == null) {
                failedRecipe(outputItem, "&cINVALID SCALING (notify admin)");
                return;
            }
            scaling = preset.scaling();
        } else {
            String preparedScaling = String.format("(%.2f/%.2f) * %%rating%% + %.2f", amplifyBy - startAt, skillRange, startAt);
            scaling = new Scaling(preparedScaling, multiplier ? Scaling.ScalingMode.MULTIPLIER : Scaling.ScalingMode.ADD_ON_DEFAULT, minimumValue, null);
        }

        AlchemyItemPropertyManager.applyAttributeScaling(outputItem.getMeta(), finalQuality, scaling, true, 0, minimumValue);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 6) skillEfficiency = Math.max(0, skillEfficiency + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.1 : 0.01)));
        else if (button == 8) minimumValue = Math.max(0, minimumValue + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.1 : 0.01)));
        else if (button == 12) usePredefined = !usePredefined;
        else if (button == 16){
            if (usePredefined){
                List<String> scalings = new ArrayList<>(AlchemyItemPropertyManager.getCustomScalings().keySet());
                int currentType = Math.max(0, scalings.indexOf(selectedScaling));
                if (e.isLeftClick()) {
                    if (currentType + 1 >= scalings.size()) currentType = 0;
                    else currentType++;
                } else {
                    if (currentType - 1 < 0) currentType = scalings.size() - 1;
                    else currentType--;
                }
                selectedScaling = scalings.get(currentType);
            } else {
                amplifyBy = amplifyBy + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? (multiplier ? 0.1 : 0.25) : 0.01));
            }
        } else if (button == 17 && !usePredefined) skillRange = Math.max(0, skillRange + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 25 : 1)));
        else if (button == 18 && !usePredefined) startAt = startAt + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.1 : 0.01));
        else if (button == 22 && !usePredefined) multiplier = !multiplier;
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        Set<Pair<Integer, ItemStack>> buttons = new HashSet<>(Set.of(
                new Pair<>(8,
                        new ItemBuilder(Material.PAPER)
                                .name("&fMinimum fraction")
                                .lore(String.format("&fSkill efficiency: %.0f%%", skillEfficiency * 100),
                                        String.format("&fMinimum fraction of stat: &e%.2fx", minimumValue),
                                        "&fThe resulting duration will always be at",
                                        "&fleast the given fraction of its default",
                                        "&fvalue, rounded up",
                                        "&6Click to add/subtract 0.01",
                                        "&6Shift-Click to add/subtract 0.1")
                                .get()
                ),
                new Pair<>(12,
                        new ItemBuilder(Material.PAPER)
                                .name("&fUse predefined scaling?")
                                .lore(usePredefined ? "&eYes" : "&eNo",
                                        "&fPredefined scalings are defined in",
                                        "&fskills/alchemy.yml. If off, a more",
                                        "&fspecific scaling can be defined.",
                                        "&6Click to toggle")
                                .get()
                )
        ));
        if (!usePredefined){
            buttons.add(new Pair<>(16,
                    new ItemBuilder(Material.PAPER)
                            .name("&fDefine Scaling: &e" + (multiplier ? "Multiplier" : "Adder"))
                            .lore(String.format("&e%.2f-%.2f from 0-%.0f quality", startAt, amplifyBy - startAt, skillRange),
                                    String.format("&f(%.2f/%.0f) * skill %s %.2f", amplifyBy - startAt, skillRange, startAt < 0 ? "-" : "+", Math.abs(startAt)),
                                    "&fThe " + (multiplier ? "multiplier" : "adder") + " defines up to how",
                                    "&fmuch the duration is " + (multiplier ? "multiplied" : "increased"),
                                    "&fover the course of the defined",
                                    "&fquality",
                                    "&6Click to add/subtract 0.01",
                                    "&6Shift-Click to add/subtract " + (multiplier ? "0.1" : "0.25"))
                            .get()
            ));
            buttons.add(new Pair<>(17,
                    new ItemBuilder(Material.PAPER)
                            .name("&fDefine Scaling: &eSkill Range")
                            .lore(String.format("&e%.2f-%.2f from 0-%.0f quality", startAt, amplifyBy - startAt, skillRange),
                                    String.format("&f(%.2f/%.0f) * skill %s %.2f", amplifyBy - startAt, skillRange, startAt < 0 ? "-" : "+", Math.abs(startAt)),
                                    "&fThe skill range defines how much",
                                    "&fskill is required to increase the",
                                    String.format("&fbase stat from %s%.2f to %s%.2f", multiplier ? "x" : startAt > 0 ? "+" : "", startAt, multiplier ? "x" : (amplifyBy - startAt) > 0 ? "+" : "", amplifyBy - startAt),
                                    "&6Click to add/subtract 1",
                                    "&6Shift-Click to add/subtract 25")
                            .get()
            ));
            buttons.add(new Pair<>(18,
                    new ItemBuilder(Material.PAPER)
                            .name("&fDefine Scaling: &eStarting Value")
                            .lore(String.format("&e%.2f-%.2f from 0-%.0f quality", startAt, amplifyBy - startAt, skillRange),
                                    String.format("&f(%.2f/%.0f) * skill %s %.2f", amplifyBy - startAt, skillRange, startAt < 0 ? "-" : "+", Math.abs(startAt)),
                                    "&fDefines the minimum value the ",
                                    "&fformula will produce at 0 quality",
                                    "&6Click to add/subtract 0.01",
                                    "&6Shift-Click to add/subtract 0.1")
                            .get()
            ));
            buttons.add(new Pair<>(22,
                    new ItemBuilder(Material.PAPER)
                            .name("&fDefine Scaling: &eMultiply/Add")
                            .lore(String.format("&e%.2f-%.2f from 0-%.0f quality", startAt, amplifyBy - startAt, skillRange),
                                    String.format("&f(%.2f/%.0f) * skill %s %.2f", amplifyBy - startAt, skillRange, startAt < 0 ? "-" : "+", Math.abs(startAt)),
                                    "&e" + (multiplier ? "Multiply base value" : "Add to base value"),
                                    "&fDefines whether the formula should",
                                    "&fproduce a multiplier applied on the",
                                    "&fpotion's amplifier, or a number added",
                                    "&fto it.",
                                    "&6Click to toggle")
                            .get()
            ));
        } else {
            AlchemyItemPropertyManager.ScalingPreset scaling = AlchemyItemPropertyManager.getCustomScaling(selectedScaling);
            buttons.add(new Pair<>(16,
                    new ItemBuilder(Material.PAPER)
                            .name("&fSelect Scaling: &e" + (scaling == null ? "&cNone Selected" : selectedScaling))
                            .lore("&f" + (scaling == null ? "&cNo scaling" : scaling.scaling().getExpression()))
                            .appendLore((scaling == null ? new ArrayList<>() : StringUtils.separateStringIntoLines(scaling.description(), 40)))
                            .appendLore("&6Click to cycle")
                            .get()
            ));
        }
        return new Pair<>(6,
                new ItemBuilder(Material.NETHER_STAR)
                        .name("&fSkill Efficiency")
                        .lore(String.format("&fSkill efficiency: %.0f%%", skillEfficiency * 100),
                                String.format("&fMinimum fraction of stat: &e%.2fx", minimumValue),
                                "&fSkill efficiency determines how much",
                                "&fof the player's skill is used in the",
                                "&fformula.",
                                "&6Click to add/subtract 1%",
                                "&6Shift-Click to add/subtract 10%")
                        .get()).map(buttons);
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.REDSTONE).get();
    }

    @Override
    public String getDisplayName() {
        return "&cScale Duration";
    }

    @Override
    public String getDescription() {
        return "&fScales the durations of the potion's effects based on player skill";
    }

    @Override
    public String getActiveDescription() {
        return String.format("&fScales the potion's durations based on &e%.0f%%&f the player's alchemy skill, to at least &e%.2fx&f the stat's default amount", skillEfficiency * 100, minimumValue);
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.POTION_MISC.id());
    }

    @Override
    public DynamicItemModifier createNew() {
        return new ScaleDuration(getName());
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 6) return "Six arguments are expected: five doubles and a yes/no answer";

        try {
            skillEfficiency = Double.parseDouble(args[0]);
            minimumValue = Double.parseDouble(args[1]);

            startAt = Double.parseDouble(args[2]);
            amplifyBy = Double.parseDouble(args[3]);
            skillRange = Double.parseDouble(args[4]);
            multiplier = args[5].equalsIgnoreCase("yes");
        } catch (NumberFormatException ignored){
            return "Six arguments are expected: five doubles and a yes/no answer. At least one was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<skill_efficiency>");
        if (currentArg == 1) return List.of("<minimum_fraction>");
        if (currentArg == 2) return List.of("<minimum_result>");
        if (currentArg == 3) return List.of("<lategame_result>");
        if (currentArg == 4) return List.of("<lategame_skill>");
        if (currentArg == 5) return List.of("<multiply>", "yes", "no");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 6;
    }
}
