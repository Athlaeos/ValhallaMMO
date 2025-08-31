package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_effects;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.PermanentPotionEffects;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTriggerRegistry;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PermanentPotionEffectAdd extends DynamicItemModifier {
    private final String effect;
    private double amplifier = 0;
    private int duration = 200;
    private String condition = "constant";
    private final Material icon;
    private final double smallStep;
    private final double bigStep;

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
        PotionEffectWrapper baseWrapper = Catch.catchOrElse(() -> PotionEffectRegistry.getEffect(this.effect), null);
        if (baseWrapper == null) return;
        baseWrapper.setAmplifier(this.amplifier);
        baseWrapper.setDuration(this.duration);
        Map<String, List<PotionEffectWrapper>> effects = PermanentPotionEffects.getPermanentPotionEffects(context.getItem().getMeta());
        List<PotionEffectWrapper> existingEffects = effects.getOrDefault(this.condition, new ArrayList<>());
        existingEffects.add(baseWrapper);
        effects.put(this.condition, existingEffects);
        PermanentPotionEffects.setPermanentPotionEffects(context.getItem().getMeta(), effects);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
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
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        if (condition == null) condition = "constant";
        PotionEffectWrapper wrapper = Catch.catchOrElse(() -> PotionEffectRegistry.getEffect(effect), null);
        if (wrapper == null) return new HashMap<>();
        String effect = wrapper.isVanilla() ? (this.effect.toLowerCase(java.util.Locale.US).replace("_", " ") + " " +StringUtils.toRoman(Math.max(0, (int) amplifier) + 1) + " " + StringUtils.toTimeStamp(duration, 20)) :
                wrapper.getFormattedEffectName(amplifier >= 0, amplifier, duration);
        return new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&dHow strong should this effect be?")
                        .lore("&f" + effect,
                                "&fTrigger type: &e" + condition.replace("_", " "),
                                "&6Click to add/subtract 0.01",
                                "&6Shift-Click to add/subtract 0.25")
                        .get()).map(Set.of(
                new Pair<>(11,
                        new ItemBuilder(Material.PAPER)
                                .name("&dHow long should this effect last?")
                                .lore("&f" + effect,
                                        "&fTrigger type: &e" + condition.replace("_", " "),
                                        "&6Click to add/subtract 1 second",
                                        "&6Shift-Click to add/subtract 30 seconds")
                                .get()),
                new Pair<>(13,
                        new ItemBuilder(Material.PAPER)
                                .name("&dWhen should this effect trigger?")
                                .lore("&f" + effect,
                                        "&fTrigger type: &e" + condition.replace("_", " "),
                                        "&6Click to cycle")
                                .get())
        ));
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

        return "&fAdds &e" + effect + "&f to the item, with trigger type &e" + condition.replace("_", " ");
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

    @Override
    public DynamicItemModifier copy() {
        PermanentPotionEffectAdd m = new PermanentPotionEffectAdd(getName(), effect, icon, this.smallStep, this.bigStep);
        m.setAmplifier(this.amplifier);
        m.setDuration(this.duration);
        m.setCondition(this.condition);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 3) return "Three arguments expected: an amplifier, a duration, and a trigger condition";
        try {
            amplifier = Double.parseDouble(args[0]);
            duration = Integer.parseInt(args[1]);
            condition = args[2];
            if (EffectTriggerRegistry.getTrigger(condition) == null) return "Invalid trigger condition given";
        } catch (IllegalArgumentException ignored){
            return "Three arguments expected: an amplifier, a duration, and a trigger condition. Amplifier or duration were not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<amplifier>");
        if (currentArg == 1) return List.of("<duration_in_ticks>");
        if (currentArg == 2) return new ArrayList<>(EffectTriggerRegistry.getRegisteredTriggers().keySet());
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 3;
    }
}
