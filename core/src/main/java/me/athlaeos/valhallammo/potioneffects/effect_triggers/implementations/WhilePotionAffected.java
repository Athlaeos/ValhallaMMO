package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import me.athlaeos.valhallammo.version.PotionEffectMappings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class WhilePotionAffected implements EffectTrigger.ConstantConfigurableTrigger{
    private final boolean inverted;
    public WhilePotionAffected(boolean inverted){
        this.inverted = inverted;
    }

    @Override
    public boolean shouldTrigger(LivingEntity entity, String arg) {
        String args = getArg(arg);
        if (args.isEmpty()) return false;
        String[] effects = args.split("/");
        if (effects.length <= 1) return false;
        boolean all = effects[0].equalsIgnoreCase("all");
        Collection<String> toMatchOn = new HashSet<>(Set.of(Arrays.copyOfRange(effects, 1, effects.length)));
        Collection<String> totalEffects = new HashSet<>();
        for (PotionEffect effect : entity.getActivePotionEffects())
            totalEffects.add(effect.getType().getName());
        for (CustomPotionEffect effect : PotionEffectRegistry.getActiveEffects(entity).values())
            totalEffects.add(effect.getWrapper().getEffect());

        return inverted != (all ? toMatchOn.containsAll(totalEffects) : totalEffects.stream().anyMatch(toMatchOn::contains));
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public String id() {
        return "while_potion_" + (inverted ? "un" : "") + "affected_";
    }

    @Override
    public void onRegister() {
        // do nothing
    }

    @Override
    public String isValid(String arg) {
        String args = getArg(arg);
        String[] effects = args.split("/");
        if (effects.length <= 1) return "&cInsufficient arguments. Format is <any/all>/<effects separated by slashes>.";
        return null;
    }

    @Override
    public String getUsage() {
        return "\"<any/all>/<effects separated by slashes>\", such as \"any/HARM/HEAL/SLOW_MINING/CRIT_CHANCE\"";
    }

    @Override
    public String asLore(String rawID) {
        String args = getArg(rawID);
        String[] effects = args.split("/");
        if (effects.length <= 1) return "&cImproperly configured";
        return "&fHave " + (effects[0].equalsIgnoreCase("all") ? "all" : "any") + " of the following effects to trigger: " + String.join(", ", Arrays.copyOfRange(effects, 1, effects.length));
    }
}