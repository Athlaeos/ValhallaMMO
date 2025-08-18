package me.athlaeos.valhallammo.trading.happiness.sources;

import me.athlaeos.valhallammo.dom.DayTime;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.happiness.HappinessSource;
import me.athlaeos.valhallammo.trading.happiness.HappinessSourceRegistry;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Brightness implements HappinessSource {
    private final int brightnessRequirement = CustomMerchantManager.getTradingConfig().getInt("brightness_requirement", 64);
    private final int darknessMax = CustomMerchantManager.getTradingConfig().getInt("darkness_max", 5);
    private final float brightnessHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.bright", 1);
    private final float darknessHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.dark", -5);
    private final String happy = TranslationManager.getTranslation("happiness_status_brightness_happy");
    private final String neutral = TranslationManager.getTranslation("happiness_status_brightness_neutral");
    private final String unhappy = TranslationManager.getTranslation("happiness_status_brightness_unhappy");

    @Override
    public String id() {
        return "BRIGHTNESS";
    }

    @Override
    public float get(Player contextPlayer, Entity entity) {
        Block b = entity.getLocation().getBlock();
        int lightLevel = b.getLightLevel();
        DayTime time = DayTime.getTime(entity.getWorld());
        byte skyExposure = b.getLightFromSky();
        if (time.isDay()) lightLevel = (byte) Math.max(lightLevel, skyExposure);

        if (lightLevel <= darknessMax) return darknessHappiness;
        else if (lightLevel >= brightnessRequirement) return brightnessHappiness;
        return 0;
    }

    @Override
    public String getHappinessStatus(float happiness, Player contextPlayer, Entity entity) {
        return (happiness > 0.001 ? happy : happiness < -0.001 ? unhappy : neutral)
                .replace("%prefix%", happiness > 0.001 ? HappinessSourceRegistry.happyPrefix() : happiness < -0.001 ? HappinessSourceRegistry.unhappyPrefix() : HappinessSourceRegistry.neutralPrefix())
                .replace("%happiness%", StringUtils.trimTrailingZeroes(String.format("%.1f", happiness)));
    }

    @Override
    public boolean appliesTo(Entity entity) {
        return entity instanceof LivingEntity;
    }
}
