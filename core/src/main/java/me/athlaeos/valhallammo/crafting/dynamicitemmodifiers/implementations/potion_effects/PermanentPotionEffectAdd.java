package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_effects;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.*;
import me.athlaeos.valhallammo.gui.implementations.DynamicModifierMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.PermanentPotionEffects;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTriggerRegistry;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.Comparator;

public class PermanentPotionEffectAdd extends DynamicItemModifier {
    private final String effect;
    private double amplifier = 0;
    private int duration = 200;
    private String condition = "constant";
    private final Material icon;
    private final double smallStep;
    private final double bigStep;
    private int cooldown = 0;
    private boolean cdrAffected = false;
    private String configuredCondition = null;

    public PermanentPotionEffectAdd(String name, String attribute, Material icon, double smallStep, double bigStep) {
        super(name);
        this.effect = attribute.toUpperCase(Locale.US);
        this.icon = icon;
        this.smallStep = smallStep;
        this.bigStep = bigStep;
    }

    @Override
    public void processItem(ModifierContext context) {
        if (condition == null) condition = "constant";
        EffectTrigger.ConfigurableTrigger configurableTrigger = EffectTriggerRegistry.getTrigger(this.condition) instanceof EffectTrigger.ConfigurableTrigger ct ? ct : null;
        String errorMessage = configurableTrigger == null ? null : (this.configuredCondition == null ? "&cUnconfigured configurable effect" : configurableTrigger.isValid(this.configuredCondition));
        if (errorMessage != null) {
            failedRecipe(context.getItem(), errorMessage);
            return;
        }
        PotionEffectWrapper baseWrapper = Catch.catchOrElse(() -> PotionEffectRegistry.getEffect(this.effect), null);
        if (baseWrapper == null) return;
        baseWrapper.setAmplifier(this.amplifier);
        baseWrapper.setDuration(this.duration);
        Map<String, List<PotionEffectWrapper>> effects = PermanentPotionEffects.getPermanentPotionEffects(context.getItem().getMeta());
        List<PotionEffectWrapper> existingEffects = effects.getOrDefault(configurableTrigger == null ? this.condition : this.configuredCondition, new ArrayList<>());
        existingEffects.add(baseWrapper);
        effects.put(configurableTrigger == null ? this.condition : this.configuredCondition, existingEffects);
        PermanentPotionEffects.setPermanentPotionEffects(context.getItem().getMeta(), effects);

        if (cooldown > 0){
            EffectTrigger.CooldownProperties properties = new EffectTrigger.CooldownProperties(cdrAffected, cooldown);
            EffectTriggerRegistry.setCooldownProperties(context.getItem(), properties);
        }
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {

    }

    @Override
    public void onButtonPress(InventoryClickEvent e, DynamicModifierMenu menu, int button) {
        if (button == 1) cooldown = Math.max(0, cooldown + ((e.isShiftClick() ? 10 : 1) * (e.isLeftClick() ? 1000 : -1000)));
        else if (button == 3) cdrAffected = !cdrAffected;
        else if (button == 7){
            EffectTrigger.ConfigurableTrigger configurableTrigger = EffectTriggerRegistry.getTrigger(this.condition) instanceof EffectTrigger.ConfigurableTrigger ct ? ct : null;
            if (configurableTrigger == null) return;
            e.getWhoClicked().closeInventory();
            Utils.sendMessage(e.getWhoClicked(), "&a" + configurableTrigger.getUsage());
            Questionnaire questionnaire = new Questionnaire((Player) e.getWhoClicked(), null, null,
                    new Question("&fHow should this effect be configured? (type in chat, or 'cancel' to cancel)", s -> {
                        String error = configurableTrigger.isValid(s);
                        Utils.sendMessage(e.getWhoClicked(), error);
                        return error == null;
                    }, "")
            ) {
                @Override
                public Action<Player> getOnFinish() {
                    if (getQuestions().isEmpty()) return super.getOnFinish();
                    Question question = getQuestions().get(0);
                    if (question.getAnswer() == null) return super.getOnFinish();
                    return (p) -> {
                        String answer = question.getAnswer().replace(" ", "_");
                        if (!answer.contains("cancel")) configuredCondition = answer;
                        menu.open();
                    };
                }
            };
            Questionnaire.startQuestionnaire((Player) e.getWhoClicked(), questionnaire);
        }
        if (button == 11) duration = Math.max(0, duration + ((e.isShiftClick() ? 30 : 1) * (e.isLeftClick() ? 20 : -20)));
        else if (button == 12) amplifier = amplifier + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? bigStep : smallStep));
        else if (button == 13) {
            if (condition == null) condition = "constant";
            List<String> conditions = new ArrayList<>(EffectTriggerRegistry.getRegisteredTriggers().keySet());
            conditions.sort(Comparator.comparing(s -> s));
            int currentCondition = conditions.indexOf(condition);
            if (e.isLeftClick()) {
                if (currentCondition + 1 >= conditions.size()) currentCondition = 0;
                else currentCondition++;
            } else {
                if (currentCondition - 1 < 0) currentCondition = conditions.size() - 1;
                else currentCondition--;
            }
            condition = conditions.get(currentCondition);
            configuredCondition = null;
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        if (condition == null) condition = "constant";
        PotionEffectWrapper wrapper = Catch.catchOrElse(() -> PotionEffectRegistry.getEffect(effect), null);
        if (wrapper == null) return new HashMap<>();
        String effect = wrapper.isVanilla() ? (this.effect.toLowerCase(java.util.Locale.US).replace("_", " ") + " " +StringUtils.toRoman(Math.max(0, (int) amplifier) + 1) + " " + StringUtils.toTimeStamp(duration, 20)) :
                wrapper.getFormattedEffectName(amplifier >= 0, amplifier, duration);
        EffectTrigger trigger = EffectTriggerRegistry.getTrigger(condition);
        String condition = trigger instanceof EffectTrigger.ConfigurableTrigger ? (this.configuredCondition == null ? "&c" + this.condition : this.configuredCondition) : this.condition;
        Map<Integer, ItemStack> buttons = new HashMap<>(new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&dHow strong should this effect be?")
                        .lore("&f" + effect,
                                "&fTrigger type: &e" + condition.replace("_", " "),
                                (cooldown > 0 ? "&fHas a cooldown of " + StringUtils.toTimeStamp(cooldown, 1000) : "&fHas no cooldown"),
                                cdrAffected ? "&fReduced by Cooldown Reduction" : "&fUnaffected by Cooldown Reduction",
                                "&6Click to add/subtract 0.01",
                                "&6Shift-Click to add/subtract 0.25")
                        .get()).map(Set.of(
                new Pair<>(11,
                        new ItemBuilder(Material.PAPER)
                                .name("&dHow long should this effect last?")
                                .lore("&f" + effect,
                                        "&fTrigger type: &e" + condition.replace("_", " "),
                                        (cooldown > 0 ? "&fHas a cooldown of " + StringUtils.toTimeStamp(cooldown, 1000) : "&fHas no cooldown"),
                                        cdrAffected ? "&fReduced by Cooldown Reduction" : "&fUnaffected by Cooldown Reduction",
                                        "&6Click to add/subtract 1 second",
                                        "&6Shift-Click to add/subtract 30 seconds")
                                .get()),
                new Pair<>(13,
                        new ItemBuilder(Material.PAPER)
                                .name("&dWhen should this effect trigger?")
                                .lore("&f" + effect,
                                        "&fTrigger type: &e" + condition.replace("_", " "),
                                        (cooldown > 0 ? "&fHas a cooldown of " + StringUtils.toTimeStamp(cooldown, 1000) : "&fHas no cooldown"),
                                        cdrAffected ? "&fReduced by Cooldown Reduction" : "&fUnaffected by Cooldown Reduction",
                                        "&6Click to cycle")
                                .get()),
                new Pair<>(1,
                        new ItemBuilder(Material.CLOCK)
                                .name("&dEffect Cooldown")
                                .lore("&f" + effect,
                                        "&fTrigger type: &e" + condition.replace("_", " "),
                                        (cooldown > 0 ? "&fHas a cooldown of " + StringUtils.toTimeStamp(cooldown, 1000) : "&fHas no cooldown"),
                                        cdrAffected ? "&fReduced by Cooldown Reduction" : "&fUnaffected by Cooldown Reduction",
                                        "&6Click to add/subtract 1 second",
                                        "&6Shift-Click to add/subtract 30 seconds")
                                .get())
        )));
        if (cooldown > 0){
            buttons.put(3, new ItemBuilder(Material.PAPER)
                    .name("&dShould cooldown be reducible?")
                    .lore("&f" + effect,
                            "&fTrigger type: &e" + condition.replace("_", " "),
                            (cooldown > 0 ? "&fHas a cooldown of " + StringUtils.toTimeStamp(cooldown, 1000) : "&fHas no cooldown"),
                            cdrAffected ? "&fReduced by Cooldown Reduction" : "&fUnaffected by Cooldown Reduction",
                            "&6Click to toggle")
                    .get());
        }
        if (trigger instanceof EffectTrigger.ConfigurableTrigger configurableTrigger){
            String errorMessage = this.configuredCondition == null ? "&cNot configured, click to config" : configurableTrigger.isValid(this.configuredCondition);
            new Pair<>(7,
                    new ItemBuilder(Material.PAPER)
                            .name("&dConfigure Trigger")
                            .lore(StringUtils.separateStringIntoLines((errorMessage == null ? configurableTrigger.asLore(this.configuredCondition) : errorMessage), 40))
                            .appendLore("&6Click to configure")
                            .get());
        }
        return buttons;
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(icon).get();
    }

    @Override
    public String getDisplayName() {
        PotionEffectWrapper wrapper = Catch.catchOrElse(() -> PotionEffectRegistry.getEffect(this.effect), null);
        if (wrapper == null) return "&cThis effect doesn't exist!";
        return "&fAdd Permanent Potion Effect: &e" + effect;
    }

    @Override
    public String getDescription() {
        PotionEffectWrapper wrapper = Catch.catchOrElse(() -> PotionEffectRegistry.getEffect(this.effect), null);
        if (wrapper == null) return "&8";
        return "&fAdds &e" + effect + " &fto the item, triggering under certain circumstances. ";
    }

    @Override
    public String getActiveDescription() {
        if (condition == null) condition = "constant";
        PotionEffectWrapper wrapper = Catch.catchOrElse(() -> PotionEffectRegistry.getEffect(this.effect), null);
        if (wrapper == null) return "&8";
        String effect = wrapper.isVanilla() ? (this.effect.toLowerCase(java.util.Locale.US).replace("_", " ") + " " + StringUtils.toRoman(Math.max(0, (int) amplifier) + 1) + " " + StringUtils.toTimeStamp(duration, 20)) :
                wrapper.getFormattedEffectName(amplifier > 0, amplifier, duration);

        return "&fAdds &e" + effect + "&f to the item, with trigger type &e" + (configuredCondition == null ? condition : configuredCondition).replace("_", " ");
    }

    @Override
    public Collection<String> getCategories() {
        PotionEffectWrapper wrapper = Catch.catchOrElse(() -> PotionEffectRegistry.getEffect(this.effect), null);
        if (wrapper == null) return new HashSet<>();
        return Set.of(wrapper.isVanilla() ? ModifierCategoryRegistry.VANILLA_POTION_EFFECTS.id() : ModifierCategoryRegistry.CUSTOM_POTION_EFFECTS.id());
    }

    public void setAmplifier(double amplifier) {
        this.amplifier = amplifier;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setCdrAffected(boolean cdrAffected) {
        this.cdrAffected = cdrAffected;
    }

    public void setConfiguredCondition(String configuredCondition) {
        this.configuredCondition = configuredCondition;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    @Override
    public DynamicItemModifier copy() {
        PermanentPotionEffectAdd m = new PermanentPotionEffectAdd(getName(), effect, icon, this.smallStep, this.bigStep);
        m.setAmplifier(this.amplifier);
        m.setDuration(this.duration);
        m.setCondition(this.condition);
        m.setPriority(this.getPriority());
        m.setCooldown(this.cooldown);
        m.setCdrAffected(this.cdrAffected);
        m.setConfiguredCondition(this.configuredCondition);
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 5) return "Five arguments expected: an amplifier, a duration, a trigger condition, a cooldown, and a boolean";
        try {
            amplifier = Double.parseDouble(args[0]);
            duration = Integer.parseInt(args[1]);
            condition = args[2];
            configuredCondition = args[2];
            cooldown = Integer.parseInt(args[3]);
            cdrAffected = args[4].equalsIgnoreCase("true");
            if (EffectTriggerRegistry.getTrigger(condition) == null) return "Invalid trigger condition given";
        } catch (IllegalArgumentException ignored){
            return "Five arguments expected: an amplifier, a duration, a trigger condition, a cooldown, and a boolean. Amplifier or duration or cooldown were not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<amplifier>");
        if (currentArg == 1) return List.of("<duration_in_ticks>");
        if (currentArg == 2) return new ArrayList<>(EffectTriggerRegistry.getRegisteredTriggers().keySet());
        if (currentArg == 3) return List.of("<cooldown_in_milliseconds>");
        if (currentArg == 4) return List.of("<true_if_cooldown_reduction_affected_false_otherwise>");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 5;
    }
}
