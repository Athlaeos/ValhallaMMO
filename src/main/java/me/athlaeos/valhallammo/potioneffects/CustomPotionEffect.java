package me.athlaeos.valhallammo.potioneffects;

public class CustomPotionEffect {
    private final PotionEffectWrapper effect;
    private long effectiveUntil;
    private double amplifier;

    /**
     * Use this constructor when adding a plain regular potion effect with a normal duration.
     * The duration should be in game ticks
     * @param effect the effect
     * @param duration the duration (in game ticks) the potion effect should last. "Timer" starts on constructor init
     * @param amplifier the amplifier
     */
    public CustomPotionEffect(PotionEffectWrapper effect, int duration, double amplifier){
        this.effect = effect;
        this.effectiveUntil = System.currentTimeMillis() + (duration * 50L);
        this.amplifier = amplifier;
    }

    /**
     * Use this constructor when either removing a potion effect, or setting its duration to infinite (effectiveUntil = -1)
     * @param effect the effect
     * @param effectiveUntil until when the effect should be active (-1 if infinite, 0 to remove)
     * @param amplifier the amplifier
     */
    public CustomPotionEffect(PotionEffectWrapper effect, long effectiveUntil, double amplifier){
        this.effect = effect;
        this.effectiveUntil = effectiveUntil;
        this.amplifier = amplifier;
    }

    public PotionEffectWrapper getWrapper() { return effect; }
    public double getAmplifier() { return amplifier; }
    public long getEffectiveUntil() { return effectiveUntil; }
    public long getRemainingDuration() {
        return Math.max(0, effectiveUntil - System.currentTimeMillis());
    }

    public void setAmplifier(double amplifier) { this.amplifier = amplifier; }
    public void setEffectiveUntil(long effectiveUntil) { this.effectiveUntil = effectiveUntil; }
}
