package me.athlaeos.valhallammo.version;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.skills.skills.implementations.*;
import me.athlaeos.valhallammo.version.conversion_dto.AlphaDatabaseAdapter;
import me.athlaeos.valhallammo.version.conversion_dto.AlphaPDCAdapter;
import me.athlaeos.valhallammo.version.conversion_dto.DatabaseConnection;
import me.athlaeos.valhallammo.version.conversion_dto.Profile;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class AlphaToBetaConversionHandler implements Listener {
    private static final boolean enabled = ConfigManager.getConfig("alpha_conversion.yml").get().getBoolean("enabled");
    private static final double expConversion = ConfigManager.getConfig("alpha_conversion.yml").get().getDouble("exp_conversion");
    private static final double expConversionFishing = ConfigManager.getConfig("alpha_conversion.yml").get().getDouble("exp_conversion_fishing");
    private static final double expConversionFarming = ConfigManager.getConfig("alpha_conversion.yml").get().getDouble("exp_conversion_farming");
    private static final double expConversionWoodcutting = ConfigManager.getConfig("alpha_conversion.yml").get().getDouble("exp_conversion_woodcutting");
    private static final double expConversionDigging = ConfigManager.getConfig("alpha_conversion.yml").get().getDouble("exp_conversion_digging");
    private static final boolean convertItems = ConfigManager.getConfig("alpha_conversion.yml").get().getBoolean("item_conversion");
    private static final boolean convertUnusualAttributes = ConfigManager.getConfig("alpha_conversion.yml").get().getBoolean("item_conversion_attributes");
    private final AlphaDatabaseAdapter adapter = new AlphaDatabaseAdapter(new DatabaseConnection(), new AlphaPDCAdapter());

    private final Map<String, NamespacedKey> alphaKeyMappings = new HashMap<>();
    private final Map<String, Class<? extends Skill>> alphaToBetaSkillMappings = new HashMap<>();

    {
        alphaKeyMappings.put("ALCHEMY", new NamespacedKey(ValhallaMMO.getInstance(), "valhalla_profile_alchemy"));
        alphaKeyMappings.put("ARCHERY", new NamespacedKey(ValhallaMMO.getInstance(), "valhalla_profile_archery"));
        alphaKeyMappings.put("ENCHANTING", new NamespacedKey(ValhallaMMO.getInstance(), "valhalla_profile_enchanting"));
        alphaKeyMappings.put("FARMING", new NamespacedKey(ValhallaMMO.getInstance(), "valhalla_profile_farming"));
        alphaKeyMappings.put("HEAVYARMOR", new NamespacedKey(ValhallaMMO.getInstance(), "valhalla_profile_heavy_armor"));
        alphaKeyMappings.put("HEAVYWEAPONS", new NamespacedKey(ValhallaMMO.getInstance(), "valhalla_profile_heavy_weapons"));
        alphaKeyMappings.put("LANDSCAPING", new NamespacedKey(ValhallaMMO.getInstance(), "valhalla_profile_landscaping"));
        alphaKeyMappings.put("LIGHTARMOR", new NamespacedKey(ValhallaMMO.getInstance(), "valhalla_profile_light_armor"));
        alphaKeyMappings.put("LIGHTWEAPONS", new NamespacedKey(ValhallaMMO.getInstance(), "valhalla_profile_light_weapons"));
        alphaKeyMappings.put("MINING", new NamespacedKey(ValhallaMMO.getInstance(), "valhalla_profile_mining"));
        alphaKeyMappings.put("SMITHING", new NamespacedKey(ValhallaMMO.getInstance(), "valhalla_profile_smithing"));

        alphaToBetaSkillMappings.put("ALCHEMY", AlchemySkill.class);
        alphaToBetaSkillMappings.put("ARCHERY", ArcherySkill.class);
        alphaToBetaSkillMappings.put("ENCHANTING", EnchantingSkill.class);
        alphaToBetaSkillMappings.put("FARMING", FarmingSkill.class);
        alphaToBetaSkillMappings.put("HEAVYARMOR", HeavyArmorSkill.class);
        alphaToBetaSkillMappings.put("HEAVYWEAPONS", HeavyWeaponsSkill.class);
        alphaToBetaSkillMappings.put("LANDSCAPING", WoodcuttingSkill.class);
        alphaToBetaSkillMappings.put("LIGHTARMOR", LightArmorSkill.class);
        alphaToBetaSkillMappings.put("LIGHTWEAPONS", LightWeaponsSkill.class);
        alphaToBetaSkillMappings.put("MINING", MiningSkill.class);
        alphaToBetaSkillMappings.put("SMITHING", SmithingSkill.class);
    }

    /**
     * Checks if the server used ValhallaMMO alpha in the past, and it does this by checking
     * if the server has the tutorial_book.yml file.
     * @return true if the server supposedly used alpha before
     */
    public static boolean shouldConvert(){
        YamlConfiguration config = ConfigManager.getConfig("tutorial_book.yml").get();
        return enabled && config.getString("title") != null;
    }

    private static final NamespacedKey CONVERTED_FROM_ALPHA_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "converted_from_alpha");

    @EventHandler(priority = EventPriority.LOWEST)
    public void expConversion(PlayerJoinEvent e){
        if (expConversion <= 0 || e.getPlayer().getPersistentDataContainer().has(CONVERTED_FROM_ALPHA_KEY, PersistentDataType.BYTE)) return;
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            if (!e.getPlayer().isOnline()) return;
            ValhallaMMO.logInfo("Player " + e.getPlayer().getName() + " had alpha data, converting to beta");
            for (String profileType : alphaKeyMappings.keySet()){
                Profile profile = adapter.getProfile(e.getPlayer(), profileType);
                NamespacedKey key = alphaKeyMappings.get(profileType);
                Class<? extends Skill> skillClass = alphaToBetaSkillMappings.get(profileType);
                if (profile == null || key == null || skillClass == null) continue;
                switch (profileType){
                    case "LANDSCAPING" -> {
                        SkillRegistry.getSkill(DiggingSkill.class).addEXP(e.getPlayer(), expConversionDigging * profile.getLifetimeEXP(), false, PlayerSkillExperienceGainEvent.ExperienceGainReason.ALPHA_REFUND);
                        SkillRegistry.getSkill(WoodcuttingSkill.class).addEXP(e.getPlayer(), expConversionWoodcutting * profile.getLifetimeEXP(), false, PlayerSkillExperienceGainEvent.ExperienceGainReason.ALPHA_REFUND);
                    }
                    case "FARMING" -> {
                        SkillRegistry.getSkill(FishingSkill.class).addEXP(e.getPlayer(), expConversionFishing * profile.getLifetimeEXP(), false, PlayerSkillExperienceGainEvent.ExperienceGainReason.ALPHA_REFUND);
                        SkillRegistry.getSkill(FarmingSkill.class).addEXP(e.getPlayer(), expConversionFarming * profile.getLifetimeEXP(), false, PlayerSkillExperienceGainEvent.ExperienceGainReason.ALPHA_REFUND);
                    }
                    default -> SkillRegistry.getSkill(skillClass).addEXP(e.getPlayer(), expConversionFarming * profile.getLifetimeEXP(), false, PlayerSkillExperienceGainEvent.ExperienceGainReason.ALPHA_REFUND);
                }
                e.getPlayer().getPersistentDataContainer().remove(key);
            }
            e.getPlayer().getPersistentDataContainer().set(CONVERTED_FROM_ALPHA_KEY, PersistentDataType.BYTE, (byte) 1);
        }, 20L);
    }

}
