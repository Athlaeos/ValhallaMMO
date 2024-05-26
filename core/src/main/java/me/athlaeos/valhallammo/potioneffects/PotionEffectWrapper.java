package me.athlaeos.valhallammo.potioneffects;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_effects.PermanentPotionEffectAdd;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_effects.PotionEffectAdd;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.version.PotionEffectMappings;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public abstract class PotionEffectWrapper {
    protected final StatFormat format;
    protected final String effect;
    protected double amplifier;
    protected long duration;
    protected int charges;
    protected final boolean removable;
    protected final boolean isVanilla;
    protected final boolean isInstant;
    protected final PotionEffectType vanillaEffect;

    public PotionEffectWrapper(String effect, boolean removable, boolean instant, StatFormat format){
        this.effect = effect;
        this.amplifier = 0;
        this.duration = 0;
        this.charges = -1;
        this.removable = removable;
        this.vanillaEffect = Catch.catchOrElse(() -> PotionEffectMappings.getEffect(effect).getPotionEffectType(), null);
        this.isVanilla = vanillaEffect != null;
        this.isInstant = instant;
        this.format = format;
    }

    public PotionEffectWrapper addModifier(Material icon, double smallIncrement, double bigIncrement){
        ModifierRegistry.register(new PotionEffectAdd("potion_effect_add_" + newToOldEffectMappings.getOrDefault(effect, effect).toLowerCase(), effect, smallIncrement, bigIncrement, icon));
        if (isVanilla) {
            ModifierRegistry.register(new PermanentPotionEffectAdd("permanent_effect_add_" + newToOldEffectMappings.getOrDefault(effect, effect).toLowerCase(), effect, icon));
        }
        return this;
    }

    public PotionEffectWrapper addModifier(Material icon){
        return addModifier(icon, 0.01, 0.1);
    }

    public abstract void onApply(ItemMeta potion);
    public abstract void onInflict(LivingEntity entity, LivingEntity causedBy, double amplifier, int duration, double intensity);

    public abstract void onRemove(ItemMeta potion);
    public abstract void onExpire(LivingEntity entity);

    public abstract EffectClass getClassification(double amplifier);

    public String getEffect() { return effect; }
    public double getAmplifier() { return amplifier; }
    public long getDuration() { return duration; }
    public boolean isRemovable() { return removable; }
    public boolean isVanilla() { return isVanilla; }
    public PotionEffectType getVanillaEffect() { return vanillaEffect; }
    public boolean isInstant() { return isInstant; }
    public StatFormat getFormat() { return format; }
    public int getCharges() { return charges; }

    public String getPotionName(){
        return TranslationManager.getTranslation("potion_name_" + effect.toLowerCase());
    }
    public String getEffectName(){
        return TranslationManager.getTranslation("effect_name_" + effect.toLowerCase());
    }
    public String getEffectIcon(){
        return TranslationManager.getTranslation("stat_icon_" + effect.toLowerCase());
    }
    public abstract boolean isSingleUse();

    public PotionEffectWrapper setAmplifier(double amplifier) { this.amplifier = amplifier; return this; }
    public PotionEffectWrapper setDuration(long duration) { this.duration = duration; return this; }
    public PotionEffectWrapper setCharges(int charges) { this.charges = charges; return this; }

    public abstract PotionEffectWrapper copy();

    public static String prefix(boolean positive){
        return TranslationManager.getTranslation("stat_potion_" + (positive ? "positive" : "negative") + "_prefix");
    }

    public String getTrimmedEffectName(){
        return getEffectName().replace("%icon%", "").replace("%duration%", "").replace("%value%", "").trim();
    }

    public String getFormattedEffectName(boolean positive, double value, long duration){
        return getEffectName().replace("%icon%", getEffectIcon() + prefix(positive))
                .replace("%value%", format == null ? "" :  format.format(value + (isVanilla ? 1 : 0)))
                .replace("%duration%", String.format("(%s)", StringUtils.toTimeStamp(duration, 20)))
                .trim();
    }

    // in 1.20.5 many effects were renamed to have a more consistent naming convention. This map records the exceptions so that old
    // modifiers may still work on newer versions
    private static final Map<String, String> newToOldEffectMappings = new HashMap<>();
    static {
        newToOldEffectMappings.put("SWIFTNESS", "SPEED");
        newToOldEffectMappings.put("SLOWNESS", "SLOW");
        newToOldEffectMappings.put("HASTE", "FAST_DIGGING");
        newToOldEffectMappings.put("MINING_FATIGUE", "SLOW_DIGGING");
        newToOldEffectMappings.put("STRENGTH", "INCREASE_DAMAGE");
        newToOldEffectMappings.put("INSTANT_HEALTH", "HEAL");
        newToOldEffectMappings.put("INSTANT_DAMAGE", "HARM");
        newToOldEffectMappings.put("JUMP_BOOST", "JUMP");
        newToOldEffectMappings.put("NAUSEA", "CONFUSION");
        newToOldEffectMappings.put("RESISTANCE", "DAMAGE_RESISTANCE");
    }
}
