package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import me.athlaeos.valhallammo.version.AttributeMappings;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;

public class WhileHealthThreshold implements EffectTrigger.ConstantConfigurableTrigger{
    @Override
    public boolean shouldTrigger(LivingEntity entity, String arg) {
        AttributeInstance maxHealthInstance = entity.getAttribute(AttributeMappings.MAX_HEALTH.getAttribute());
        if (maxHealthInstance == null) return false;
        double maxHealth = maxHealthInstance.getValue();
        double fraction = Math.max(0, Math.min(1, entity.getHealth() / maxHealth));
        String[] args = arg.split("_");
        if (args.length != 2) return false;
        double threshold = Catch.catchOrElse(() -> Double.parseDouble(args[1]), -1D);
        if (threshold < 0) return false;
        boolean above = args[0].equalsIgnoreCase("above");
        return above ? fraction >= threshold : fraction <= threshold;
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public String id() {
        return "while_health_";
    }

    @Override
    public void onRegister() {
        // do nothing
    }

    @Override
    public String isValid(String arg) {
        String[] args = getArg(arg).split("_");
        if (args.length != 2) return "&cInvalid argument format, must be <above/below>_<percentage>";
        if (!args[0].equals("above") && !args[0].equals("below")) return "&cInvalid, first arg must be above/below";
        double doubleValue = Catch.catchOrElse(() -> Double.parseDouble(args[1]), -1D);
        if (doubleValue <= 0 || doubleValue >= 1) return "&cInvalid, second arg must be a number between 0 and 1";
        return null;
    }

    @Override
    public String getUsage() {
        return "\"<above/below>_<percentage>\". <percentage> for the fraction of health to reach for effect to trigger. <above/below> if you want it to trigger when the entity's health is ABOVE or BELOW the <percentage>. " +
                "Example: say \"above_0.3\" for the effect to trigger only when the entity has more than 30% health. \"below\" would trigger the effect if the entity has less than 30% health";
    }

    @Override
    public String asLore(String rawID) {
        String args = getArg(rawID);
        if (args.isEmpty()) return "&cUnconfigured";
        if (isValid(rawID) != null) return "&cImproperly configured";
        String[] values = args.split("_");
        String percentage = String.format("%.0f%%", Double.parseDouble(values[1]) * 100);
        String explained = values[0].equals("above") ? "&aabove &f" + percentage + " HP" :
                ("&cbelow &f" + percentage + " HP");
        return "&fTriggers while " + explained;
    }
}
