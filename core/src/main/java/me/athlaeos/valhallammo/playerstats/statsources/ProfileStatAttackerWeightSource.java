package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ProfileStatAttackerWeightSource implements AccumulativeStatSource, EvEAccumulativeStatSource {
    private final Class<? extends Profile> type;
    private final String stat;
    private final Class<? extends Number> numberType;
    private final Profile baseProfile;
    private final double def;
    private final WeightClass weightClass;
    private boolean negative = false;

    public ProfileStatAttackerWeightSource(Class<? extends Profile> type, String stat, WeightClass weightClass){
        this.type = type;
        this.stat = stat;
        this.weightClass = weightClass;
        baseProfile = ProfileRegistry.getBlankProfile(null, type);

        if (baseProfile.intStatNames().contains(stat)) {
            def = baseProfile.getDefaultInt(stat);
            numberType = Integer.class;
        } else if (baseProfile.floatStatNames().contains(stat)) {
            def = baseProfile.getDefaultFloat(stat);
            numberType = Float.class;
        } else if (baseProfile.doubleStatNames().contains(stat)) {
            def = baseProfile.getDefaultDouble(stat);
            numberType = Double.class;
        } else {
            def = 0;
            numberType = null;
        }

        if (numberType == null) {
            throw new IllegalArgumentException("ProfileStatAttackerWeightSource:" + type.getSimpleName() + " with stat " + stat +
                    " was initialized, but this profile type does not have such a stat");
        }
    }

    public ProfileStatAttackerWeightSource n(){
        this.negative = true;
        return this;
    }

    @Override
    public double fetch(Entity p, boolean use) {
        return 0;
    }

    public StatFormat getFormat(){
        return baseProfile.getNumberStatProperties().get(stat).getFormat();
    }

    @Override
    public double fetch(Entity victim, Entity attackedBy, boolean use) {
        if (attackedBy instanceof Player pl){
            EntityProperties properties = EntityCache.getAndCacheProperties(pl);

            if (weightClass != WeightClass.WEIGHTLESS){
                if (properties.getMainHand() == null || WeightClass.getWeightClass(properties.getMainHand().getMeta()) != weightClass) return def;
            } else {
                // weightless is handled differently
                // if the item has a defined weight class, but it's not weightless, invalid item
                if (properties.getMainHand() != null && WeightClass.hasDefinedWeightClass(properties.getMainHand().getMeta()) && WeightClass.getWeightClass(properties.getMainHand().getMeta()) != WeightClass.WEIGHTLESS) return def;
                // if the item has no defined weight class, the player must be unarmed
                else if (!EntityUtils.isUnarmed(pl)) return def;
            }

            Profile profile = ProfileCache.getOrCache(pl, type);
            String requiredPermission = SkillRegistry.isRegistered(profile.getSkillType()) ? SkillRegistry.getSkill(profile.getSkillType()).getRequiredPermission() : null;
            if (requiredPermission != null && !pl.hasPermission(requiredPermission)) return def;

            if (numberType.equals(Integer.class)) return (negative ? -1 : 1) * profile.getInt(stat);
            if (numberType.equals(Float.class)) return (negative ? -1 : 1) * profile.getFloat(stat);
            return (negative ? -1 : 1) * profile.getDouble(stat);
        }
        return def;
    }
}
