package me.athlaeos.valhallammo.dom;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.animations.Animation;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Comparator;

public class CustomDamageType implements Comparable<CustomDamageType>{
    private static final Map<String, CustomDamageType> damageToCustomTypeMapping = new HashMap<>();
    private static final Map<String, CustomDamageType> registeredTypes = new HashMap<>();
    public static void register(CustomDamageType type){
        registeredTypes.put(type.type, type);
    }
    public static CustomDamageType getCustomType(String damageCause){
        return damageToCustomTypeMapping.get(damageCause);
    }

    public static final CustomDamageType FIRE = new CustomDamageType("FIRE", "FIRE", "LAVA", "MELTING", "FIRE_TICK", "HOT_FLOOR", "DRYOUT")
            .additiveDamage("FIRE_DAMAGE_BONUS").multiplicativeDamage("FIRE_DAMAGE_DEALT")
            .resistance("FIRE_RESISTANCE")
            .immuneable()
            .indicator(ValhallaMMO.getPluginConfig().getString("damage_indicator_fire")).i("&f\uEE6B&c").register();
    public static final CustomDamageType MAGIC = new CustomDamageType("MAGIC", "MAGIC", "THORNS", "DRAGON_BREATH")
            .additiveDamage("MAGIC_DAMAGE_BONUS").multiplicativeDamage("MAGIC_DAMAGE_DEALT")
            .resistance("MAGIC_RESISTANCE")
            .immuneable()
            .indicator(ValhallaMMO.getPluginConfig().getString("damage_indicator_magic")).i("&f\uEE6C&d").register();
    public static final CustomDamageType PROJECTILE = new CustomDamageType("PROJECTILE", "PROJECTILE")
            .multiplicativeDamage("RANGED_DAMAGE_DEALT")
            .resistance("PROJECTILE_RESISTANCE")
            .indicator(ValhallaMMO.getPluginConfig().getString("damage_indicator_projectile")).i("&f\uEE05&c").register();
    public static final CustomDamageType EXPLOSION = new CustomDamageType("EXPLOSION", "ENTITY_EXPLOSION", "BLOCK_EXPLOSION")
            .additiveDamage("EXPLOSION_DAMAGE_BONUS").multiplicativeDamage("EXPLOSION_DAMAGE_DEALT")
            .resistance("EXPLOSION_RESISTANCE")
            .indicator(ValhallaMMO.getPluginConfig().getString("damage_indicator_explosion")).i("&f\uEE6A&6").register();
    public static final CustomDamageType POISON = new CustomDamageType("POISON", "POISON")
            .additiveDamage("POISON_DAMAGE_BONUS").multiplicativeDamage("POISON_DAMAGE_DEALT").setFatal(false)
            .resistance("POISON_RESISTANCE")
            .immuneable()
            .indicator(ValhallaMMO.getPluginConfig().getString("damage_indicator_poison")).i("&f\uEE6D&2").register();
    public static final CustomDamageType FALLING = new CustomDamageType("FALLING", "FALL", "FLY_INTO_WALL")
            .resistance("FALLING_RESISTANCE")
            .indicator(ValhallaMMO.getPluginConfig().getString("damage_indicator_falling")).i("&f\uEE98&c").register();
    public static final CustomDamageType MELEE = new CustomDamageType("MELEE", "CONTACT", "ENTITY_ATTACK", "ENTITY_SWEEP_ATTACK")
            .multiplicativeDamage("MELEE_DAMAGE_DEALT")
            .resistance("MELEE_RESISTANCE")
            .indicator(ValhallaMMO.getPluginConfig().getString("damage_indicator_melee")).i("&f\uEE54&c").register();
    public static final CustomDamageType BLEED = new CustomDamageType("BLEED", "BLEED")
            .resistance("BLEED_RESISTANCE")
            .immuneable()
            .indicator(ValhallaMMO.getPluginConfig().getString("damage_indicator_bleeding")).i("&f\uEE09&4").register();
    public static final CustomDamageType FREEZING = new CustomDamageType("FREEZING", "FREEZE")
            .additiveDamage("FREEZING_DAMAGE_BONUS").multiplicativeDamage("FREEZING_DAMAGE_DEALT")
            .resistance("FREEZING_RESISTANCE")
            .immuneable()
            .indicator(ValhallaMMO.getPluginConfig().getString("damage_indicator_freezing")).i("&f\uEE70&b").register();
    public static final CustomDamageType LIGHTNING = new CustomDamageType("LIGHTNING", "LIGHTNING")
            .additiveDamage("LIGHTNING_DAMAGE_BONUS").multiplicativeDamage("LIGHTNING_DAMAGE_DEALT")
            .resistance("LIGHTNING_RESISTANCE")
            .immuneable()
            .indicator(ValhallaMMO.getPluginConfig().getString("damage_indicator_lightning")).i("&f\uEE6F&e").register();
    public static final CustomDamageType BLUDGEONING = new CustomDamageType("BLUDGEONING", "FALLING_BLOCK", "BLUDGEONING")
            .additiveDamage("BLUDGEONING_DAMAGE_BONUS").multiplicativeDamage("BLUDGEONING_DAMAGE_DEALT").powerAttacks(true)
            .resistance("BLUDGEONING_RESISTANCE")
            .indicator(ValhallaMMO.getPluginConfig().getString("damage_indicator_bludgeoning")).i("&f\uEE6E&c").register();
    public static final CustomDamageType RADIANT = new CustomDamageType("RADIANT", "RADIANT")
            .additiveDamage("RADIANT_DAMAGE_BONUS").multiplicativeDamage("RADIANT_DAMAGE_DEALT")
            .resistance("RADIANT_RESISTANCE")
            .immuneable()
            .indicator(ValhallaMMO.getPluginConfig().getString("damage_indicator_radiant")).i("&f\uEE71&f").register();
    public static final CustomDamageType NECROTIC = new CustomDamageType("NECROTIC", "NECROTIC", "WITHER")
            .additiveDamage("NECROTIC_DAMAGE_BONUS").multiplicativeDamage("NECROTIC_DAMAGE_DEALT")
            .resistance("NECROTIC_RESISTANCE")
            .immuneable()
            .indicator(ValhallaMMO.getPluginConfig().getString("damage_indicator_necrotic")).i("&f\uEE72&7").register();

    public static Map<String, CustomDamageType> getRegisteredTypes() {
        return registeredTypes;
    }

    private final String type;
    private final Collection<String> subTypes = new HashSet<>();
    private Animation hurtAnimation = null;
    private String resistanceStatSource = null;
    private String additiveDamageStatSource = null;
    private String multiplicativeDamageStatSource = null;
    private String indicatorIcon = null;
    private String hardCodedIndicatorIcon = null;
    private boolean benefitsFromPowerAttacks = false;
    private boolean fatal = true;
    private boolean immuneable = false; // determines if damage events can be cancelled for this damage type with 100% resistance to it

    public CustomDamageType(String type, String... subTypes){
        this.type = type;
        this.subTypes.addAll(Set.of(subTypes));
        for (String cause : subTypes) damageToCustomTypeMapping.put(cause, this);
    }

    public CustomDamageType(String type, Collection<String> subTypes){
        this.type = type;
        this.subTypes.addAll(subTypes);
        for (String cause : subTypes) damageToCustomTypeMapping.put(cause, this);
    }

    public String getType() {
        return type;
    }

    public Collection<String> getSubTypes() {
        return subTypes;
    }

    public Animation getHurtAnimation() {
        return hurtAnimation;
    }

    public String getIndicatorIcon() {
        return indicatorIcon;
    }

    public String getHardCodedIndicatorIcon() {
        return hardCodedIndicatorIcon;
    }

    public void setHurtAnimation(Animation hurtAnimation) {
        this.hurtAnimation = hurtAnimation;
    }

    public String resistance() {
        return resistanceStatSource;
    }

    public String damageAdder() {
        return additiveDamageStatSource;
    }

    public String damageMultiplier() {
        return multiplicativeDamageStatSource;
    }

    public CustomDamageType resistance(String resistanceStatSource) {
        this.resistanceStatSource = resistanceStatSource;
        return this;
    }

    public CustomDamageType immuneable() {
        this.immuneable = true;
        return this;
    }

    public CustomDamageType additiveDamage(String additiveDamageStatSource) {
        this.additiveDamageStatSource = additiveDamageStatSource;
        return this;
    }

    public CustomDamageType multiplicativeDamage(String multiplicativeDamageStatSource) {
        this.multiplicativeDamageStatSource = multiplicativeDamageStatSource;
        return this;
    }

    public CustomDamageType indicator(String indicatorIcon) {
        this.indicatorIcon = indicatorIcon;
        return this;
    }

    private CustomDamageType i(String hardCodedIndicatorIcon) {
        this.hardCodedIndicatorIcon = hardCodedIndicatorIcon;
        return this;
    }

    public CustomDamageType powerAttacks(boolean benefitsFromPowerAttacks) {
        this.benefitsFromPowerAttacks = benefitsFromPowerAttacks;
        return this;
    }

    public CustomDamageType setFatal(boolean fatal) {
        this.fatal = fatal;
        return this;
    }

    public boolean isFatal() {
        return fatal;
    }

    public boolean isImmuneable() {
        return immuneable;
    }

    public boolean benefitsFromPowerAttacks() {
        return benefitsFromPowerAttacks;
    }

    public CustomDamageType register(){
        register(this);
        return this;
    }

    @Override
    public int compareTo(@NotNull CustomDamageType o) {
        return this.type.compareTo(o.getType());
    }
}
