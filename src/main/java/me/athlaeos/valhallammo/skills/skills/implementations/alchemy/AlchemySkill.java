package me.athlaeos.valhallammo.skills.skills.implementations.alchemy;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class AlchemySkill extends Skill implements Listener {
    private final boolean quickEmptyPotions;

    public AlchemySkill(String type) {
        super(type);
        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/alchemy.yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/alchemy_progression.yml").get();

        loadCommonConfig(skillConfig, progressionConfig);

        quickEmptyPotions = skillConfig.getBoolean("quick_empty_potions");

        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    @Override
    public boolean isLevelableSkill() {
        return true;
    }

    @Override
    public Class<? extends Profile> getProfileType() {
        return AlchemyProfile.class;
    }

    @Override
    public int getSkillTreeMenuOrderPriority() {
        return 15;
    }

    @Override
    public boolean isExperienceScaling() {
        return true;
    }

    @Override
    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        if (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) {
            amount *= AccumulativeStatManager.getStats("ALCHEMY_EXP_GAIN", p, true);
        }
        super.addEXP(p, amount, silent, reason);
    }

    @EventHandler
    public void onThrow(ProjectileLaunchEvent e){
        if (e.getEntity().getShooter() instanceof LivingEntity l){
            // thrown object speed mechanic
            if (!(e.getEntity() instanceof AbstractArrow)){
                double speedMultiplier = 1;// + AccumulativeStatManager.getCachedStats("ALCHEMY_POTION_VELOCITY", l, 10000, true);
                e.getEntity().setVelocity(e.getEntity().getVelocity().multiply(speedMultiplier));
            }

            // potion saving mechanic
            if (e.getEntity() instanceof ThrownPotion t){
                if (l instanceof Player p && p.getGameMode() != GameMode.CREATIVE){
                    if (false && Utils.proc(p, AccumulativeStatManager.getCachedStats("ALCHEMY_POTION_SAVE", p, 10000, true), false)){
                        p.getInventory().addItem(t.getItem());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onCauldronInteract(PlayerInteractEvent e){
        if (!quickEmptyPotions) return;
        Block b = e.getClickedBlock();
        if (b != null && (b.getType() == Material.CAULDRON || b.getType().toString().equals("WATER_CAULDRON"))){
            ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
            if (ItemUtils.isEmpty(hand) || hand.getType() != Material.POTION) return;
            hand.setType(Material.GLASS_BOTTLE);
            e.getClickedBlock().getWorld().playSound(b.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_BREWING_STAND_BREW, 1F, 1F);
        }
    }
}
