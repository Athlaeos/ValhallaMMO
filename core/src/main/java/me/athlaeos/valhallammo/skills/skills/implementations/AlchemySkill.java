package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.listeners.BrewingStandListener;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.AlchemyProfile;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.utility.BlockUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class AlchemySkill extends Skill implements Listener {
    private boolean quickEmptyPotions = true;
    private final Collection<Material> validCombiningItems = new HashSet<>();
    private final NamespacedKey COMBINATIONS_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "alchemy_combinations");

    private static final Map<String, Transmutation> transmutations = new HashMap<>();
    private final Map<Material, Transmutation> transmutationsByMaterial = new HashMap<>();
    private boolean transmutationFlash = true;
    private Sound transmutationSound = null;
    private static List<String> transmutationPotionLore = new ArrayList<>();
    private static String transmutationPotionName = null;
    private double qualityPotionExperienceMultiplier = 0;
    private double expMultiplierAutomated = 0.25;
    private double expMultiplierManual = 2;

    public AlchemySkill(String type) {
        super(type);
    }

    @Override
    public void loadConfiguration() {
        ValhallaMMO.getInstance().save("skills/alchemy_transmutations.yml");
        ValhallaMMO.getInstance().save("skills/alchemy_progression.yml");
        ValhallaMMO.getInstance().save("skills/alchemy.yml");

        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/alchemy.yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/alchemy_progression.yml").get();
        YamlConfiguration transmutationConfig = ConfigManager.getConfig("skills/alchemy_transmutations.yml").get();

        loadCommonConfig(skillConfig, progressionConfig);

        quickEmptyPotions = skillConfig.getBoolean("quick_empty_potions");
        transmutationFlash = skillConfig.getBoolean("transmutation_liquid_flash");
        transmutationSound = Catch.catchOrElse(() -> Sound.valueOf(skillConfig.getString("transmutation_sound")), null, "Invalid transmutation sound given in skills/alchemy.yml");

        validCombiningItems.addAll(ItemUtils.getMaterialSet(skillConfig.getStringList("valid_combining_items")));
        transmutationPotionLore = Utils.chat(TranslationManager.translateListPlaceholders(skillConfig.getStringList("transmutation_lore")));
        transmutationPotionName = Utils.chat(TranslationManager.translatePlaceholders(skillConfig.getString("transmutation_name")));

        qualityPotionExperienceMultiplier = progressionConfig.getDouble("experience.exp_multiplier_quality", 0.01);
        expMultiplierAutomated = progressionConfig.getDouble("experience.multiplier_automated");
        expMultiplierManual = progressionConfig.getDouble("experience.multiplier_manual");

        ConfigurationSection section = transmutationConfig.getConfigurationSection("transmutations");
        if (section != null){
            for (String name : section.getKeys(false)){
                Material from = Catch.catchOrElse(() -> Material.valueOf(transmutationConfig.getString("transmutations." + name + ".from")), null);
                Material to = Catch.catchOrElse(() -> Material.valueOf(transmutationConfig.getString("transmutations." + name + ".to")), null);
                if (from == null || to == null) continue;
                Transmutation transmutation = new Transmutation(name, from, to);
                transmutations.put(name, transmutation);
                transmutationsByMaterial.put(transmutation.from, transmutation);
            }
        }

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
    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_ALCHEMY)) return;
        if (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) {
            amount *= (1 + AccumulativeStatManager.getCachedStats("ALCHEMY_EXP_GAIN", p, 10000, true));
        }
        double multiplier = reason != PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION ? 1 : BrewingStandListener.isMarkedAutomatedBrewing(p) ? expMultiplierAutomated : expMultiplierManual;
        super.addEXP(p, multiplier * amount, silent, reason);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionCombine(InventoryClickEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getWhoClicked().getWorld().getName()) || !e.isRightClick() ||
                !Timer.isCooldownPassed(e.getWhoClicked().getUniqueId(), "delay_combining_attempts") ||
                WorldGuardHook.inDisabledRegion(e.getWhoClicked().getLocation(), (Player) e.getWhoClicked(), WorldGuardHook.VMMO_SKILL_ALCHEMY) ||
                !hasPermissionAccess((Player) e.getWhoClicked())) return;
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
                wrapper.setAmplifier(((1 + wrapper.getAmplifier()) * (1 + profile.getPotionCombiningAmplifierMultiplier())) - 1);
            } else {
                wrapper.setAmplifier(wrapper.getAmplifier() * (1 + profile.getPotionCombiningAmplifierMultiplier()));
            }
            wrapper.setDuration((int) Math.floor(wrapper.getDuration() * (1 + profile.getPotionCombiningDurationMultiplier())));
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCauldronInteract(PlayerInteractEvent e){
        if (!quickEmptyPotions || WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_ALCHEMY)) return;
        Block b = e.getClickedBlock();
        if (b != null && (b.getType() == Material.CAULDRON || b.getType().toString().equals("WATER_CAULDRON"))){
            ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
            if (ItemUtils.isEmpty(hand) || hand.getType() != Material.POTION) return;
            hand = new ItemStack(Material.GLASS_BOTTLE, hand.getAmount());
            e.getPlayer().getInventory().setItemInMainHand(hand);
            e.getClickedBlock().getWorld().playSound(b.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_BREWING_STAND_BREW, 1F, 1F);
            e.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileHitBlock(PotionSplashEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) ||
                e.getHitBlock() == null ||
                !(e.getEntity().getShooter() instanceof Player p) ||
                WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_ALCHEMY) ||
                !hasPermissionAccess(p)) return;
        ItemMeta potionMeta = ItemUtils.getItemMeta(e.getPotion().getItem());
        if (!isTransmutationPotion(potionMeta)) return;

        AlchemyProfile profile = ProfileCache.getOrCache(p, AlchemyProfile.class);
        if (profile.getUnlockedTransmutations().isEmpty() || profile.getTransmutationRadius() <= 0) return;
        Collection<Block> affectedBlocks = BlockUtils.getBlocksTouching(e.getHitBlock(), profile.getTransmutationRadius(), 1, profile.getTransmutationRadius(), Material::isAir);
        for (Block b : affectedBlocks){
            if (transmutationsByMaterial.containsKey(b.getType())){
                if (ValhallaMMO.isHookFunctional(WorldGuardHook.class) && !WorldGuardHook.canPlaceBlocks(b.getLocation(), p)) continue;
                b.setType(transmutationsByMaterial.get(b.getType()).to);
            }
        }
        if (transmutationFlash) e.getHitBlock().getWorld().spawnParticle(Particle.FLASH, e.getEntity().getLocation(), 0);
        if (transmutationSound != null) e.getHitBlock().getWorld().playSound(e.getHitBlock().getLocation(), transmutationSound, 1F, 1F);
    }

    private static final NamespacedKey TRANSMUTATION_POTION = new NamespacedKey(ValhallaMMO.getInstance(), "transmutation_potion");

    public static void setTransmutationPotion(ItemMeta meta, boolean set){
        if (set) {
            meta.getPersistentDataContainer().set(TRANSMUTATION_POTION, PersistentDataType.BYTE, (byte) 1);
            meta.setLore(transmutationPotionLore);
            if (transmutationPotionName != null) meta.setDisplayName(transmutationPotionName);
        } else meta.getPersistentDataContainer().remove(TRANSMUTATION_POTION);
    }

    public static boolean isTransmutationPotion(ItemMeta meta){
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(TRANSMUTATION_POTION, PersistentDataType.BYTE);
    }

    public static Map<String, Transmutation> getTransmutations() {
        return transmutations;
    }

    private record Transmutation(String key, Material from, Material to){}

    public double getQualityPotionExperienceMultiplier() {
        return qualityPotionExperienceMultiplier;
    }
}
