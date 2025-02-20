package me.athlaeos.valhallammo.block;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.MiningSpeed;
import me.athlaeos.valhallammo.listeners.CustomBreakSpeedListener;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.MiningProfile;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.utility.BlockUtils;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.MathUtils;
import me.athlaeos.valhallammo.version.AttributeMappings;
import me.athlaeos.valhallammo.version.EnchantmentMappings;
import me.athlaeos.valhallammo.version.PotionEffectMappings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Much of this was referenced from asangarin's breaker plugin
 */

public class DigPacketInfo {
    private final Player digger;
    private final Location l;
    private final Type type;

    public DigPacketInfo(Player digger, int x, int y, int z, Type type){
        this.digger = digger;
        this.l = new Location(digger.getWorld(), x, y, z);
        this.type = type;
    }

    public Location getLocation() {
        return l;
    }

    public Player getDigger() {
        return digger;
    }

    public Type getType() {
        return type;
    }

    public int id(){
        return ((l.getBlockX() & 0xFFF) << 20) | ((l.getBlockZ() & 0xFFF) << 8) | (l.getBlockY() & 0xFF);
    }

    public Block getBlock(){
        return l.getBlock();
    }

    public boolean finished(){
        return type == Type.ABORT || type == Type.STOP;
    }

    private static final Map<UUID, BlockCache> blockSpecificSpeedCache = new HashMap<>();
    private static final Map<UUID, Float> cachedMultiplier = new HashMap<>();
    private static final Collection<UUID> cachedSwimmingMiners = new HashSet<>();
    private static final Collection<UUID> cachedAirMiners = new HashSet<>();
    public static void resetMinerCache(UUID uuid){
        cachedMultiplier.remove(uuid);
        cachedSwimmingMiners.remove(uuid);
        cachedAirMiners.remove(uuid);
        blockSpecificSpeedCache.remove(uuid);
    }
    public static void resetBlockSpecificCache(UUID uuid){
        blockSpecificSpeedCache.remove(uuid);
    }

    public static float damage(Player digger, Block b){
        if (digger == null || b == null || b.getType().isAir()) return 0;
        ItemBuilder tool = EntityCache.getAndCacheProperties(digger).getMainHand();
        if (tool == null) {
            MiningProfile profile = ProfileCache.getOrCache(digger, MiningProfile.class);
            // replace empty hand tool only if tool power of that item would be > 1 (valid tool for block)
            if (profile.getEmptyHandTool() != null && ValhallaMMO.getNms().toolPower(profile.getEmptyHandTool().getItem(), b) > 1) tool = profile.getEmptyHandTool();
        }
        if (tool != null && !tool.getEmbeddedTools().isEmpty()) {
            ItemBuilder optimalTool = MiningSpeed.getOptimalEmbeddedTool(tool.getEmbeddedTools(), tool.getMeta(), b);
            if (optimalTool != null) tool = optimalTool;
        }
        float hardness = tool == null ? BlockUtils.getHardness(b) : MiningSpeed.getHardness(tool.getMeta(), b);
        if (hardness < 0 || hardness > 100000) return 0;
        EntityProperties properties = EntityCache.getAndCacheProperties(digger);
        Map<Material, Material> hardnessTranslations = tool == null ? new HashMap<>() : MiningSpeed.getHardnessTranslations(tool.getMeta());
        // changing tool power to be equal to that if the tool mined a different type of block
        float toolStrength = !hardnessTranslations.isEmpty() && hardnessTranslations.containsKey(b.getType()) ?
                ValhallaMMO.getNms().toolPower(tool.getItem(), hardnessTranslations.get(b.getType())) :
                ValhallaMMO.getNms().toolPower(tool == null ? null : tool.getItem(), b);

        boolean canHarvest = BlockUtils.hasDrops(b, digger, tool == null ? null : tool.getItem());
        float baseMultiplier = 1;
        if (toolStrength > 1){
            // preferred tool for block
            baseMultiplier = tool == null ? 1F : (float) MiningSpeed.getMultiplier(tool.getMeta(), b.getType());

            int efficiency = tool == null ? 0 : tool.getItem().getEnchantmentLevel(EnchantmentMappings.EFFICIENCY.getEnchantment());
            if (efficiency > 0) baseMultiplier += (float) (MathUtils.pow(efficiency, 2) + 1);
        }

        boolean canSwimMine = cachedSwimmingMiners.contains(digger.getUniqueId());
        boolean canAirMine = cachedAirMiners.contains(digger.getUniqueId());
        float additionalMultiplier = 1;
        if (cachedMultiplier.containsKey(digger.getUniqueId())) {
            additionalMultiplier = cachedMultiplier.get(digger.getUniqueId());
        } else {
            additionalMultiplier += (float) AccumulativeStatManager.getCachedStats("DIG_SPEED", digger, 10000, true);

            PotionEffect haste = digger.getPotionEffect(PotionEffectMappings.HASTE.getPotionEffectType());
            if (haste != null) additionalMultiplier += (0.2F * haste.getAmplifier());

            PotionEffect fatigue = digger.getPotionEffect(PotionEffectMappings.MINING_FATIGUE.getPotionEffectType());
            // mining fatigue with amplifier -1 is inconsistent pre-1.20.5, so to compromise we're going to ignore the mining speed slow from Mining Fatigue I
            if (fatigue != null && fatigue.getAmplifier() > (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) ? -1 : 0) && fatigue.getAmplifier() < 5) {
                additionalMultiplier *= (float) MathUtils.pow(0.3, Math.min(fatigue.getAmplifier() + 1, 4));
            }

            PowerProfile profile = ProfileCache.getOrCache(digger, PowerProfile.class);

            canSwimMine = properties.getCombinedEnchantments().getOrDefault(EnchantmentMappings.AQUA_AFFINITY.getEnchantment(), 0) > 0 ||
                    digger.hasPotionEffect(PotionEffectType.CONDUIT_POWER) || profile.hasAquaAffinity();

            cachedMultiplier.put(digger.getUniqueId(), additionalMultiplier);
            if (canSwimMine) cachedSwimmingMiners.add(digger.getUniqueId());
            if (profile.hasAerialAffinity()) cachedAirMiners.add(digger.getUniqueId());

            Attribute miningSpeed = AttributeMappings.BLOCK_BREAK_SPEED.getAttribute();
            if (miningSpeed != null && CustomBreakSpeedListener.isFatigued(digger))
                additionalMultiplier *= (float) -EntityUtils.getUniqueAttributeValue(digger, CustomBreakSpeedListener.FATIGUE_MODIFIER_UUID, "valhalla_mining_speed_nullifier", miningSpeed);
            // if on newer versions the player's mining speed is nullified by having an opposite mining speed modifier added to prevent cracks from appearing the normal way.
            // during actual mining speed calculations this nullification is then removed.
            // this is in an attempt to keep mining speed attributes working
        }
        baseMultiplier *= additionalMultiplier;

        BlockCache cachedBlockBonus = blockSpecificSpeedCache.get(digger.getUniqueId());
        if (cachedBlockBonus == null || !sameLocation(b, cachedBlockBonus.block)) {
            cachedBlockBonus = new BlockCache(b, 1 + AccumulativeStatManager.getStats("BLOCK_SPECIFIC_DIG_SPEED", digger, true));
            blockSpecificSpeedCache.put(digger.getUniqueId(), cachedBlockBonus);
        }
        baseMultiplier *= (float) cachedBlockBonus.value;

        if (isInWater(digger) && !canSwimMine) baseMultiplier /= 5;
        if (!EntityUtils.isOnGround(digger) && !canAirMine) baseMultiplier /= 5;

        float damage = baseMultiplier / hardness;

        if (canHarvest) damage /= 30;
        else damage /= 100;
        return Math.max(0, damage);
    }

    private static boolean isInWater(Player p){
        return p.getEyeLocation().getBlock().getType() == Material.WATER || p.getEyeLocation().getBlock().getType() == Material.LAVA;
    }

    public static Type fromName(String name) {
        return switch (name.toUpperCase(java.util.Locale.US)) {
            case "START_DESTROY_BLOCK" -> Type.START;
            case "STOP_DESTROY_BLOCK" -> Type.STOP;
            case "ABORT_DESTROY_BLOCK" -> Type.ABORT;
            default -> Type.INVALID;
        };
    }


    public enum Type{
        START, STOP, ABORT, INVALID
    }

    private static boolean sameLocation(Block b1, Block b2){
        return b1.getWorld().getName().equals(b2.getWorld().getName()) && b1.getX() == b2.getX() && b1.getY() == b2.getY() && b1.getZ() == b2.getZ();
    }

    private static record BlockCache(Block block, double value){}
}
