package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ProfileStatDefenderArmorWeightSetSource implements AccumulativeStatSource, EvEAccumulativeStatSource {
    private final Class<? extends Profile> type;
    private final String stat;
    private final String qtyStat;
    private final Class<? extends Number> numberType;
    private final Class<? extends Number> quantityType;
    private final Profile baseProfile;
    private final double def;
    private final WeightClass weightClass;
    private boolean negative = false;

    public ProfileStatDefenderArmorWeightSetSource(Class<? extends Profile> type, String stat, String qtyStat, WeightClass weightClass){
        this.type = type;
        this.stat = stat;
        this.qtyStat = qtyStat;
        this.weightClass = weightClass;
        baseProfile = ProfileRegistry.getBlankProfile((UUID) null, type);

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

        if (baseProfile.intStatNames().contains(qtyStat)) {
            quantityType = Integer.class;
        } else if (baseProfile.floatStatNames().contains(qtyStat)) {
            quantityType = Float.class;
        } else if (baseProfile.doubleStatNames().contains(qtyStat)) {
            quantityType = Double.class;
        } else {
            quantityType = null;
        }

        if (numberType == null) {
            throw new IllegalArgumentException("ProfileStatAttackerWeightSource:" + type.getSimpleName() + " with stat " + stat +
                    " was initialized, but this profile type does not have such a stat");
        }
        if (quantityType == null) {
            throw new IllegalArgumentException("ProfileStatAttackerWeightSource:" + type.getSimpleName() + " with stat " + qtyStat +
                    " was initialized, but this profile type does not have such a stat");
        }
    }

    public ProfileStatDefenderArmorWeightSetSource n(){
        this.negative = true;
        return this;
    }

    @Override
    public double fetch(Entity p, boolean use) {
        return fetch(p, null, use);
    }

    public StatFormat getFormat(){
        return baseProfile.getNumberStatProperties().get(stat).getFormat();
    }

    @Override
    public double fetch(Entity victim, Entity attackedBy, boolean use) {
        if (victim instanceof Player pl){
            EntityProperties properties = EntityCache.getAndCacheProperties(pl);
            int quantity = switch(weightClass){
                case LIGHT -> properties.getLightArmorCount();
                case HEAVY -> properties.getHeavyArmorCount();
                case WEIGHTLESS -> properties.getWeightlessArmorCount();
                case NONE -> properties.getNoArmorCount();
            };
            Profile profile = ProfileCache.getOrCache(pl, type);
            String requiredPermission = SkillRegistry.isRegistered(profile.getSkillType()) ? SkillRegistry.getSkill(profile.getSkillType()).getRequiredPermission() : null;
            if (requiredPermission != null && !pl.hasPermission(requiredPermission)) return def;
            int required = -1;
            if (quantityType.equals(Integer.class)) required = profile.getInt(qtyStat);
            if (quantityType.equals(Float.class)) required = (int) profile.getFloat(qtyStat);
            if (quantityType.equals(Double.class)) required = (int) profile.getDouble(qtyStat);
            if (required < 0 || quantity < required) return 0;

            if (numberType.equals(Integer.class)) return (negative ? -1 : 1) * profile.getInt(stat);
            if (numberType.equals(Float.class)) return (negative ? -1 : 1) * profile.getFloat(stat);
            return (negative ? -1 : 1) * profile.getDouble(stat);
        }
        return def;
    }
}
