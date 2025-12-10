package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.entity.LivingEntity;

public class WhileInWorld implements EffectTrigger.ConstantConfigurableTrigger{
    private final boolean inverted;
    public WhileInWorld(boolean inverted){
        this.inverted = inverted;
    }

    @Override
    public boolean shouldTrigger(LivingEntity entity, String arg) {
        String args = getArg(arg);
        return inverted != entity.getWorld().getName().equalsIgnoreCase(args);
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public String id() {
        return "while_" + (inverted ? "not_" : "") + "in_world_";
    }

    @Override
    public void onRegister() {
        // do nothing
    }

    @Override
    public String isValid(String arg) {
        return null;
    }

    @Override
    public String getUsage() {
        return "<worldname>";
    }

    @Override
    public String asLore(String rawID) {
        String args = getArg(rawID);
        return "&fTriggers only while " + (inverted ? "not " : "") + "in " + args;
    }
}