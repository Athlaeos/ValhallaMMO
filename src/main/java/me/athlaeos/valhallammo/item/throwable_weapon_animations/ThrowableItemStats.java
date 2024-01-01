package me.athlaeos.valhallammo.item.throwable_weapon_animations;

public class ThrowableItemStats {
    private final String animationType;
    private final int cooldown;
    private final double gravityStrength, velocityDamageMultiplier, defaultVelocity, damageMultiplier;
    private final boolean infinity, returnsNaturally;
    public ThrowableItemStats(String type, int cooldown, double gravityStrength, double velocityDamageMultiplier,
                              double defaultVelocity, double damageMultiplier, boolean infinity, boolean returnsNaturally){
        this.animationType = type;
        this.gravityStrength = gravityStrength;
        this.velocityDamageMultiplier = velocityDamageMultiplier;
        this.damageMultiplier = damageMultiplier;
        this.defaultVelocity = defaultVelocity;
        this.cooldown = cooldown;
        this.infinity = infinity;
        this.returnsNaturally = returnsNaturally;
    }

    public double getDefaultVelocity() { return defaultVelocity; }
    public double getGravityStrength() { return gravityStrength; }
    public double getVelocityDamageMultiplier() { return velocityDamageMultiplier; }
    public String getAnimationType() { return animationType; }
    public boolean isInfinity() { return infinity; }
    public boolean returnsNaturally() { return returnsNaturally; }
    public int getCooldown() { return cooldown; }
    public double getDamageMultiplier() { return damageMultiplier; }
}
