package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.MaterialClass;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.SmithingProfile;
import me.athlaeos.valhallammo.skills.ChunkEXPNerf;
import me.athlaeos.valhallammo.skills.skills.Skill;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SmithingSkill extends Skill implements Listener {
    private double durabilityToolEXPBonusPerStack = 0D;
    private int durabilityToolEXPBonusMaxStacks = 0;
    private double durabilityArmorEXPBonusPerStack = 0D;
    private int durabilityArmorEXPBonusMaxStacks = 0;
    private int durabilityChunkLimit = 0;

    private final Map<UUID, Map<Material, Integer>> playerMaterialDurabilityTakenStacks = new HashMap<>();

    public SmithingSkill(String type) {
        super(type);
    }

    @Override
    public void loadConfiguration() {
        ValhallaMMO.getInstance().save("skills/smithing_progression.yml");
        ValhallaMMO.getInstance().save("skills/smithing.yml");

        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/smithing.yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/smithing_progression.yml").get();

        this.durabilityToolEXPBonusPerStack = progressionConfig.getDouble("experience.durability_tools_exp_multiplier_stack", 0.01D);
        this.durabilityToolEXPBonusMaxStacks = progressionConfig.getInt("experience.durability_tools_exp_multiplier_maximum", 1000);
        this.durabilityArmorEXPBonusPerStack = progressionConfig.getDouble("experience.durability_armors_exp_multiplier_stack", 0.005D);
        this.durabilityArmorEXPBonusMaxStacks = progressionConfig.getInt("experience.durability_armors_exp_multiplier_maximum", 200);
        this.durabilityChunkLimit = progressionConfig.getInt("experience.durability_chunk_limit", 50);

        loadCommonConfig(skillConfig, progressionConfig);
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    @Override
    public boolean isLevelableSkill() {
        return true;
    }

    @Override
    public Class<? extends Profile> getProfileType() {
        return SmithingProfile.class;
    }

    @Override
    public int getSkillTreeMenuOrderPriority() {
        return 5;
    }

    @Override
    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_SMITHING)) return;
        if (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) {
            double multiplier = Math.max(0, 1 + AccumulativeStatManager.getCachedStats("SMITHING_EXP_GAIN_GENERAL", p, 10000, true));
            amount *= multiplier;
        }
        super.addEXP(p, amount, silent, reason);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageItem(PlayerItemDamageEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) ||
                WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_SMITHING) ||
                e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        incrementStacks(e.getPlayer(), e.getItem().getType());
    }

    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason, MaterialClass material) {
        double multiplier = material == null ? 1 : (1 + AccumulativeStatManager.getCachedStats("SMITHING_EXP_GAIN_" + material, p, 10000, true));
        amount *= multiplier;
        addEXP(p, amount, silent, reason);
    }

    public double getExperienceMultiplierFromDamage(Player crafter, Material crafted, boolean consumeStacks){
        boolean isArmor = EquipmentClass.isArmor(EquipmentClass.getMatchingClass(crafted));
        int stacks = getStacks(crafter, crafted);
        double multiplier = 1 + (isArmor ?
                Math.min(stacks, durabilityArmorEXPBonusMaxStacks) * durabilityArmorEXPBonusPerStack :
                Math.min(stacks, durabilityToolEXPBonusMaxStacks) * durabilityToolEXPBonusPerStack
        );
        if (consumeStacks) setStacks(crafter, crafted, stacks - Math.min(stacks, isArmor ? durabilityArmorEXPBonusMaxStacks : durabilityToolEXPBonusMaxStacks));
        return multiplier;
    }

    public int getStacks(Player crafter, Material crafted){
        Map<Material, Integer> stacks = playerMaterialDurabilityTakenStacks.getOrDefault(crafter.getUniqueId(), new HashMap<>());
        return stacks.getOrDefault(crafted, 0);
    }

    public void incrementStacks(Player crafter, Material forMaterial){
        if (ChunkEXPNerf.doesChunkEXPNerfApply(crafter.getLocation().getChunk(), crafter, "smithing_durability_damage_counter_" + forMaterial.toString().toLowerCase(java.util.Locale.US), durabilityChunkLimit)) return;
        ChunkEXPNerf.increment(crafter.getLocation().getChunk(), crafter, "smithing_durability_damage_counter_" + forMaterial.toString().toLowerCase(java.util.Locale.US));
        setStacks(crafter, forMaterial, getStacks(crafter, forMaterial) + 1);
    }

    public void setStacks(Player crafter, Material forMaterial, int stacks){
        Map<Material, Integer> currentStacks = playerMaterialDurabilityTakenStacks.getOrDefault(crafter.getUniqueId(), new HashMap<>());
        currentStacks.put(forMaterial, stacks);
        playerMaterialDurabilityTakenStacks.put(crafter.getUniqueId(), currentStacks);
    }
}
