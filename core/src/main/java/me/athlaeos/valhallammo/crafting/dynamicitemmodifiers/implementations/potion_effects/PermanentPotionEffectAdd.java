package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_effects;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.PermanentPotionEffects;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.version.PotionEffectMappings;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PermanentPotionEffectAdd extends DynamicItemModifier {
    private final String effect;
    private int amplifier = 0;
    private final Material icon;

    public PermanentPotionEffectAdd(String name, String attribute, Material icon) {
        super(name);
        this.effect = attribute;
        this.icon = icon;
    }

    @Override
    public void processItem(ModifierContext context) {
        PotionEffectType potionEffectType = Catch.catchOrElse(() -> PotionEffectMappings.getEffect(effect).getPotionEffectType(), null);
        if (potionEffectType == null) return;
        List<PotionEffect> effects = PermanentPotionEffects.getPermanentPotionEffects(context.getItem().getMeta());
        effects.add(new PotionEffect(potionEffectType, 0, amplifier));
        PermanentPotionEffects.setPermanentPotionEffects(context.getItem().getMeta(), effects);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) amplifier = amplifier + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 3 : 1));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        PotionEffectType potionEffectType = Catch.catchOrElse(() -> PotionEffectMappings.getEffect(effect).getPotionEffectType(), null);
        if (potionEffectType == null) return new HashMap<>();
        return new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&dHow strong should this effect be?")
                        .lore("&f" + effect.toLowerCase(java.util.Locale.US).replace("_", " ") + " " + StringUtils.toRoman(Math.max(0, amplifier) + 1),
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 3")
                        .get()).map(Set.of());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(icon).get();
    }

    @Override
    public String getDisplayName() {
        PotionEffectType potionEffectType = Catch.catchOrElse(() -> PotionEffectMappings.getEffect(effect).getPotionEffectType(), null);
        if (potionEffectType == null) return "&cThis effect doesn't exist!";
        return "&fAdd Permanent Potion Effect: " + effect;
    }

    @Override
    public String getDescription() {
        PotionEffectType potionEffectType = Catch.catchOrElse(() -> PotionEffectMappings.getEffect(effect).getPotionEffectType(), null);
        if (potionEffectType == null) return "&8";
        return "&fAdds " + effect + " as permanent effect to the item. ";
    }

    @Override
    public String getActiveDescription() {
        PotionEffectType potionEffectType = Catch.catchOrElse(() -> PotionEffectMappings.getEffect(effect).getPotionEffectType(), null);
        if (potionEffectType == null) return "&8";
        return "&fAdds " + effect + " " + StringUtils.toRoman(Math.max(0, amplifier) + 1) + " as permanent effect to the item. ";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.VANILLA_POTION_EFFECTS.id());
    }

    public void setAmplifier(int amplifier) {
        this.amplifier = amplifier;
    }

    @Override
    public DynamicItemModifier copy() {
        PermanentPotionEffectAdd m = new PermanentPotionEffectAdd(getName(), effect, icon);
        m.setAmplifier(this.amplifier);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument expected: an amplifier";
        try {
            amplifier = Integer.parseInt(args[0]);
        } catch (IllegalArgumentException ignored){
            return "One argument expected: an amplifier. It was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<amplifier>");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
