package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ProfileStatWeightSource implements AccumulativeStatSource {
    private final Class<? extends Profile> type;
    private final String stat;
    private final Class<? extends Number> numberType;
    private final Profile baseProfile;
    private final double def;
    private final WeightClass weightClass;
    private boolean negative = false;

    public ProfileStatWeightSource(Class<? extends Profile> type, String stat, WeightClass weightClass, boolean negative){
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
            throw new IllegalArgumentException("ProfileStatSource:" + type.getSimpleName() + " with stat " + stat +
                    " was initialized, but this profile type does not have such a stat");
        }
    }

    @Override
    public double fetch(Entity p, boolean use) {
        if (p instanceof Player pl){
            EntityProperties properties = EntityCache.getAndCacheProperties(pl);
            if (properties.getMainHand() == null || WeightClass.getWeightClass(properties.getMainHand().getMeta()) != weightClass) return def;
            Profile profile = ProfileCache.getOrCache(pl, type);
            if (numberType.equals(Integer.class)) return (negative ? -1 : 1) * profile.getInt(stat);
            if (numberType.equals(Float.class)) return (negative ? -1 : 1) * profile.getFloat(stat);
            return (negative ? -1 : 1) * profile.getDouble(stat);
        }
        return def;
    }

    public StatFormat getFormat(){
        return baseProfile.getNumberStatProperties().get(stat).getFormat();
    }
}
