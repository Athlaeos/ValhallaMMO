package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_misc;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.version.ConventionUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PotionEffectConvert extends DynamicItemModifier {
    private static final List<String> types = new ArrayList<>();

    private final Map<String, Effect> conversions = new HashMap<>();
    private String selectedFrom = "SPEED";
    private double value = 0.1;
    private long duration = 1800;
    private String selectedTo = "SLOW";

    public PotionEffectConvert(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        Map<String, PotionEffectWrapper> effects = PotionEffectRegistry.getStoredEffects(context.getItem().getMeta(), true);
        for (String from : conversions.keySet()){
            if (effects.remove(from) == null) continue;
            PotionEffectRegistry.removeEffect(context.getItem().getMeta(), from);
            PotionEffectWrapper to = PotionEffectRegistry.getEffect(conversions.get(from).effect);
            to.setAmplifier(conversions.get(from).amplifier);
            to.setDuration(conversions.get(from).duration);
            effects.put(to.getEffect(), to);
            PotionEffectRegistry.addDefaultEffect(context.getItem().getMeta(), to);
        }
        PotionEffectRegistry.updateItemName(context.getItem().getMeta(), true, false);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            PotionEffectWrapper effectTo = PotionEffectRegistry.getEffect(this.selectedTo);
            value = value + ((e.isLeftClick() ? 1 : -1) * (effectTo.isVanilla() ? (e.isShiftClick() ? 3 : 1) : (e.isShiftClick() ? 0.25 : 0.01)));
            if (effectTo.isVanilla()) value = Math.max(0, value);
        }
        else if (button == 17) duration = Math.max(0, duration + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 300 : 20)));
        else if (button == 11) {
            if (types.isEmpty()) populate();
            int currentIndex = selectedFrom == null ? -1 : types.indexOf(selectedFrom);
            currentIndex = Math.max(0, Math.min(types.size() - 1, currentIndex + (e.isLeftClick() ? 1 : -1)));
            selectedFrom = types.get(currentIndex);
        } else if (button == 7) {
            if (types.isEmpty()) populate();
            int currentIndex = selectedTo == null ? -1 : types.indexOf(selectedTo);
            currentIndex = Math.max(0, Math.min(types.size() - 1, currentIndex + (e.isLeftClick() ? 1 : -1)));
            selectedTo = types.get(currentIndex);
            PotionEffectWrapper effectTo = PotionEffectRegistry.getEffect(selectedTo);
            if (effectTo.isVanilla()) value = Math.max(0, value);
        } else if (button == 14){
            if (e.isShiftClick()) conversions.clear();
            else conversions.put(selectedFrom, new Effect(selectedTo, value, duration));
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        String infoFrom;
        if (types.isEmpty()) populate();
        if (types.isEmpty()) infoFrom = "&cNo potion effects found(???)";
        else {
            if (selectedFrom == null) selectedFrom = types.get(0);
            int currentIndex = types.indexOf(selectedFrom);
            String before = currentIndex <= 0 ? "" : types.get(currentIndex - 1) + " > &e";
            String after = currentIndex + 1 >= types.size() ? "" : "&f > " + types.get(currentIndex + 1);
            infoFrom = "&f" + before + selectedFrom + after;
        }
        String infoTo;
        if (types.isEmpty()) infoTo = "&cNo potion effects found(???)";
        else {
            if (selectedTo == null) selectedTo = types.get(0);
            int currentIndex = types.indexOf(selectedTo);
            String before = currentIndex <= 0 ? "" : types.get(currentIndex - 1) + " > &e";
            String after = currentIndex + 1 >= types.size() ? "" : "&f > " + types.get(currentIndex + 1);
            infoTo = "&f" + before + selectedTo + after;
        }
        PotionEffectWrapper effectFrom = PotionEffectRegistry.getEffect(this.selectedFrom);
        PotionEffectWrapper effectTo = PotionEffectRegistry.getEffect(this.selectedTo);
        String effectDescription = effectFrom.getTrimmedEffectName() + " &f>>> " + effectTo.getFormattedEffectName(true, value, duration);
        return new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&dHow strong should this effect be?")
                        .lore("&f" + effectDescription,
                                "&6Click to add/subtract 0.01",
                                "&6Shift-Click to add/subtract 0.25")
                        .get()).map(Set.of(
                new Pair<>(17,
                        new ItemBuilder(Material.PAPER)
                                .name("&dHow long should the duration be?")
                                .lore("&f" + effectDescription,
                                        "&6Click to add/subtract 1 second",
                                        "&6Shift-Click to add/subtract 15 seconds")
                                .get()),
                new Pair<>(11,
                        new ItemBuilder(Material.FERMENTED_SPIDER_EYE)
                                .name("&dWhat effects should be converted?")
                                .lore("&e" + infoFrom,
                                        "&6Click to cycle")
                                .get()),
                new Pair<>(7,
                        new ItemBuilder(Material.POTION)
                                .name("&dWhat should" + effectFrom.getEffectName() + " be converted to?")
                                .lore("&e" + infoTo,
                                        "&6Click to cycle")
                                .flag(ConventionUtils.getHidePotionEffectsFlag())
                                .get()),
                new Pair<>(14,
                        new ItemBuilder(Material.STRUCTURE_VOID)
                                .name("&dAdd conversion")
                                .lore("&fCurrently selected &f" + effectDescription)
                                .appendLore(conversions.keySet().stream().map(e -> {
                                    PotionEffectWrapper from = PotionEffectRegistry.getEffect(e);
                                    Effect details = conversions.get(e);
                                    PotionEffectWrapper to = PotionEffectRegistry.getEffect(details.effect);
                                    return "&f> " + from.getTrimmedEffectName() + " &f>>> " + to.getFormattedEffectName(true, details.amplifier, details.duration);
                                }).toList())
                                .appendLore("&6Click to add", "&cShift-Click to clear")
                                .get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.FERMENTED_SPIDER_EYE).get();
    }

    @Override
    public String getDisplayName() {
        return "&dConvert Effects";
    }

    @Override
    public String getDescription() {
        return "&fConverts potion effects from one type to another";
    }

    @Override
    public String getActiveDescription() {
        PotionEffectWrapper effect = PotionEffectRegistry.getEffect(this.selectedTo);
        return "&fConverts " + selectedFrom + " to " + effect.getEffect().replace("_", " ") + " " + effect.getFormat().format(value + (effect.isVanilla() ? 1 : 0)) + " &f(" + StringUtils.toTimeStamp(duration, 20) + ")";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.POTION_MISC.id());
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setSelectedFrom(String selectedFrom) {
        this.selectedFrom = selectedFrom;
    }

    public void setSelectedTo(String selectedTo) {
        this.selectedTo = selectedTo;
    }

    @Override
    public DynamicItemModifier copy() {
        PotionEffectConvert m = new PotionEffectConvert(getName());
        m.setSelectedFrom(this.selectedFrom);
        m.setSelectedTo(this.selectedTo);
        m.setDuration(this.duration);
        m.setValue(this.value);
        m.setPriority(this.getPriority());
        m.conversions.putAll(this.conversions);
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        return "This modifier is too complex for commands, sorry!";
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }

    private static class Effect{
        private String effect;
        private double amplifier;
        private long duration;
        public Effect(String effect, double amplifier, long duration){
            this.effect = effect;
            this.amplifier = amplifier;
            this.duration = duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public void setAmplifier(double amplifier) {
            this.amplifier = amplifier;
        }

        public void setEffect(String effect) {
            this.effect = effect;
        }
    }

    private static void populate(){
        types.clear();
        types.addAll(new ArrayList<>(PotionEffectRegistry.getRegisteredEffects().values().stream().map(PotionEffectWrapper::getEffect).toList()));
        types.sort(Comparator.comparing((String s) -> PotionEffectRegistry.getRegisteredEffects().get(s).isVanilla()).thenComparing((String s) -> s));
    }
}
