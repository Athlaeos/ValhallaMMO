package me.athlaeos.valhallammo.trading.happiness.sources;

import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.happiness.HappinessSource;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Brightness implements HappinessSource {
    private static final Map<UUID, Map<UUID, Long>> lastHarmed = new HashMap<>();

    private final int brightnessRequirement = CustomMerchantManager.getTradingConfig().getInt("brightness_requirement", 64);
    private final int darknessMax = CustomMerchantManager.getTradingConfig().getInt("darkness_max", 5);
    private final float brightnessHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.bright", 1);
    private final float darknessHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.dark", -5);

    @Override
    public String id() {
        return "BRIGHTNESS";
    }

    @Override
    public float get(Player contextPlayer, Entity entity) {
        int lightLevel = entity.getLocation().getBlock().getLightLevel();
        if (lightLevel <= darknessMax) return darknessHappiness;
        else if (lightLevel >= brightnessRequirement) return brightnessHappiness;
        return 0;
    }

    @Override
    public boolean appliesTo(Entity entity) {
        return entity instanceof LivingEntity;
    }
}
