package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.AlchemyProfile;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AlchemySkill extends Skill implements Listener {
    private final boolean quickEmptyPotions;
    private final Collection<Material> validCombiningItems = new HashSet<>();
    private final NamespacedKey COMBINATIONS_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "alchemy_combinations");

    public AlchemySkill(String type) {
        super(type);
        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/alchemy.yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/alchemy_progression.yml").get();

        loadCommonConfig(skillConfig, progressionConfig);

        quickEmptyPotions = skillConfig.getBoolean("quick_empty_potions");

        validCombiningItems.addAll(ItemUtils.getMaterialList(skillConfig.getStringList("valid_combining_items")));

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
            amount *= (1 + AccumulativeStatManager.getStats("ALCHEMY_EXP_GAIN", p, true));
        }
        super.addEXP(p, amount, silent, reason);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPotionCombine(InventoryClickEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getWhoClicked().getWorld().getName()) || e.isCancelled() || !e.isRightClick() || !Timer.isCooldownPassed(e.getWhoClicked().getUniqueId(), "delay_combining_attempts")) return;
        if (!(e.getClickedInventory() instanceof PlayerInventory) || !e.isRightClick()) return; // player inventory must be right-clicked
        Timer.setCooldown(e.getWhoClicked().getUniqueId(), 500, "delay_combining_attempts"); // setting cooldown between attempts so this can't be spammed with some macro
        if (ItemUtils.isEmpty(e.getCurrentItem()) || ItemUtils.isEmpty(e.getCursor())) return; // neither items must be empty
        if (e.getCurrentItem().getType() != e.getCursor().getType()) return; // must be the same type
        if (!validCombiningItems.contains(e.getCursor().getType())) return; // must be a valid item for combining

        AlchemyProfile profile = ProfileCache.getOrCache((Player) e.getWhoClicked(), AlchemyProfile.class);
        if (!profile.isPotionCombiningUnlocked()) return;

        ItemBuilder clicked = new ItemBuilder(e.getCurrentItem());
        ItemBuilder cursor = new ItemBuilder(e.getCursor());
        int clickedCombinations = ItemUtils.getPDCInt(COMBINATIONS_KEY, clicked.getMeta(), 0);
        int cursorCombinations = ItemUtils.getPDCInt(COMBINATIONS_KEY, cursor.getMeta(), 0);
        if (clickedCombinations + cursorCombinations + 1 > profile.getPotionCombiningMaxCombinations()) return; // combining the two potions would exceed the max allowed combinations

        Map<String, PotionEffectWrapper> clickedWrappers = PotionEffectRegistry.getStoredEffects(clicked.getMeta(), false);
        Map<String, PotionEffectWrapper> cursorWrappers = PotionEffectRegistry.getStoredEffects(cursor.getMeta(), false);
        if (clickedWrappers.isEmpty() || cursorWrappers.isEmpty()) return; // both items must have effects or there's no point in combining them


        Map<String, PotionEffectWrapper> combinedEffects = new HashMap<>(); // combines the effects of the two, prioritizing the greater amplifier
        for (PotionEffectWrapper wrapper : cursorWrappers.values()){
            PotionEffectWrapper clickedEquivalent = clickedWrappers.get(wrapper.getEffect());
            if (clickedEquivalent != null)
                combinedEffects.put(wrapper.getEffect(), clickedEquivalent.getAmplifier() > wrapper.getAmplifier() ? clickedEquivalent : wrapper);
            else combinedEffects.put(wrapper.getEffect(), wrapper);
        }
        for (PotionEffectWrapper wrapper : clickedWrappers.values()){
            PotionEffectWrapper cursorEquivalent = cursorWrappers.get(wrapper.getEffect());
            if (cursorEquivalent != null)
                combinedEffects.put(wrapper.getEffect(), cursorEquivalent.getAmplifier() > wrapper.getAmplifier() ? cursorEquivalent : wrapper);
            else combinedEffects.put(wrapper.getEffect(), wrapper);
        }

        if (combinedEffects.size() == clickedWrappers.size()) return; // clicked item has the same amount of wrapper before and after, and therefore no effect was combined.

        for (PotionEffectWrapper wrapper : combinedEffects.values()){
            if (wrapper.isVanilla()) {
                wrapper.setAmplifier(((1 + wrapper.getAmplifier()) * profile.getPotionCombiningAmplifierMultiplier()) - 1);
            } else {
                wrapper.setAmplifier(wrapper.getAmplifier() * profile.getPotionCombiningAmplifierMultiplier());
            }
            wrapper.setDuration((int) Math.floor(wrapper.getDuration() * profile.getPotionCombiningDurationMultiplier()));
            combinedEffects.put(wrapper.getEffect(), wrapper);
        }

        PotionEffectRegistry.setDefaultStoredEffects(clicked.getMeta(), combinedEffects);
        PotionEffectRegistry.setActualStoredEffects(clicked.getMeta(), combinedEffects);
        clicked.intTag(COMBINATIONS_KEY, clickedCombinations + cursorCombinations + 1);
        PotionEffectRegistry.updateItemName(clicked.getMeta(), false, true);
        e.setCurrentItem(clicked.get());
        e.setCancelled(true);
        e.getWhoClicked().getWorld().playSound(e.getWhoClicked().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1F, 1F);
        e.getWhoClicked().getWorld().playSound(e.getWhoClicked().getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1F, 1F);

        if (cursor.getItem().getAmount() == 1) e.getWhoClicked().setItemOnCursor(null);
        else e.getCursor().setAmount(e.getCursor().getAmount() - 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCauldronInteract(PlayerInteractEvent e){
        if (!quickEmptyPotions) return;
        Block b = e.getClickedBlock();
        if (b != null && (b.getType() == Material.CAULDRON || b.getType().toString().equals("WATER_CAULDRON"))){
            ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
            if (ItemUtils.isEmpty(hand) || hand.getType() != Material.POTION) return;
            hand.setType(Material.GLASS_BOTTLE);
            e.getClickedBlock().getWorld().playSound(b.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_BREWING_STAND_BREW, 1F, 1F);
            e.setCancelled(true);
        }
    }
}
