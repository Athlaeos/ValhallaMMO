package me.athlaeos.valhallammo.trading.happiness.sources;

import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.happiness.HappinessSource;
import me.athlaeos.valhallammo.version.AttributeMappings;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Health implements HappinessSource {
    private final float damageForWounded = (float) CustomMerchantManager.getTradingConfig().getDouble("woundedness_max");
    private final float healedHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.healed", 1);
    private final float damagedHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.wounded", -1);

    @Override
    public String id() {
        return "HEALTH";
    }

    @Override
    public float get(Player contextPlayer, Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) return 0;
        AttributeInstance health = livingEntity.getAttribute(AttributeMappings.MAX_HEALTH.getAttribute());
        if (health == null) return 0;
        return livingEntity.getHealth() >= health.getValue() - damageForWounded ? healedHappiness : damagedHappiness;
    }

    @Override
    public boolean appliesTo(Entity entity) {
        return entity instanceof LivingEntity;
    }
}
