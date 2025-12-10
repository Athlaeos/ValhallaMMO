package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.dom.MoonPhase;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.entity.LivingEntity;

import java.util.Locale;

public class WhileMoonPhase implements EffectTrigger.ConstantTrigger{
    private final MoonPhase phase;
    public WhileMoonPhase(MoonPhase phase){
        this.phase = phase;
    }

    @Override
    public boolean shouldTrigger(LivingEntity entity) {
        MoonPhase currentPhase = MoonPhase.getPhase(entity.getWorld());
        return currentPhase == phase;
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public String id() {
        return "while_moon_phase_" + phase.toString().toLowerCase(Locale.US);
    }

    @Override
    public void onRegister() {
        // do nothing
    }
}