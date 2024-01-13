package me.athlaeos.valhallammo.block;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.MiningSpeed;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.MiningProfile;
import me.athlaeos.valhallammo.utility.BlockUtils;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.MathUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

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

    public boolean sameLocation(Location l){
        return this.l.getX() == l.getBlockX() && this.l.getY() == l.getBlockY() && this.l.getZ() == l.getBlockZ();
    }

    public int id(){
        return ((l.getBlockX() & 0xFFF) << 20) | ((l.getBlockZ() & 0xFFF) << 8) | (l.getBlockY() & 0xFF);
    }

    public Block block(){
        return l.getBlock();
    }

    public boolean finished(){
        return type == Type.ABORT || type == Type.STOP;
    }

    public int breakTime(){
        Block b = block();
        if (digger == null || b == null) return -1;
        ItemBuilder tool = ItemUtils.isEmpty(digger.getInventory().getItemInMainHand()) ? null : new ItemBuilder(digger.getInventory().getItemInMainHand());
        if (tool == null) {
            MiningProfile profile = ProfileCache.getOrCache(digger, MiningProfile.class);
            // replace empty hand tool only if tool power of that item would be > 1 (valid tool for block)
            if (profile.getEmptyHandTool() != null && ValhallaMMO.getNms().toolPower(profile.getEmptyHandTool().getItem(), b) > 1) tool = profile.getEmptyHandTool();
        }
        float hardness = tool == null ? BlockUtils.getHardness(b) : MiningSpeed.getHardness(tool.getMeta(), b);
        if (hardness < 0 || hardness > 100000) return Integer.MAX_VALUE;
        EntityProperties properties = EntityCache.getAndCacheProperties(digger);
        Map<Material, Material> hardnessTranslations = tool == null ? new HashMap<>() : MiningSpeed.getHardnessTranslations(tool.getMeta());
        // changing tool power to be equal to that if the tool mined a different type of block
        float toolStrength = !hardnessTranslations.isEmpty() && hardnessTranslations.containsKey(b.getType()) ?
                ValhallaMMO.getNms().toolPower(tool.getItem(), hardnessTranslations.get(b.getType())) :
                ValhallaMMO.getNms().toolPower(tool == null ? null : tool.getItem(), b);

        double multiplier = 1;
        if (toolStrength > 1){
            // preferred tool for block
            multiplier = tool == null ? 1 : MiningSpeed.getMultiplier(tool.getMeta(), b.getType());

            int efficiency = tool == null ? 0 : tool.getItem().getEnchantmentLevel(Enchantment.DIG_SPEED);
            if (efficiency > 0) multiplier += MathUtils.pow(efficiency, 2) + 1;
        }

        PotionEffect haste = digger.getPotionEffect(PotionEffectType.FAST_DIGGING);
        if (haste != null) multiplier *= 0.2 * (haste.getAmplifier() + 1);

        multiplier *= (1 + AccumulativeStatManager.getCachedStats("DIG_SPEED", digger, 10000, true) + AccumulativeStatManager.getStats("BLOCK_SPECIFIC_DIG_SPEED", digger, true));

        PotionEffect fatigue = digger.getPotionEffect(PotionEffectType.SLOW_DIGGING);
        if (fatigue != null && fatigue.getAmplifier() != -1) multiplier *= MathUtils.pow(0.3, Math.min(fatigue.getAmplifier() + 1, 4));

        if (isInWater(digger) && properties.getCombinedEnchantments().getOrDefault(Enchantment.WATER_WORKER, 0) > 0) multiplier /= 5;
        //if (!EntityUtils.isOnGround(digger)) multiplier /= 5;
        if (digger.getFallDistance() > 1) multiplier /= 5;

        double damage = multiplier / hardness;

        if (toolStrength > 1) damage /= 30;
        else damage /= 100;
        if (damage > 1) return 0;
        if (damage <= 0) return Integer.MAX_VALUE;

        return (int) Math.ceil(1 / damage);

//        float newBreakTime = (originalBreakTime / 20) * 1.5F;
//        double damage = multiplier / newBreakTime;
//
//
//        return damage > 1 ? 1 : (int) Math.ceil(1 / damage);
    }

    private boolean isInWater(Player p){
        return p.getEyeLocation().getBlock().getType() == Material.WATER;
    }

    public static Type fromName(String name) {
        return switch (name.toUpperCase()) {
            case "START_DESTROY_BLOCK" -> Type.START;
            case "STOP_DESTROY_BLOCK" -> Type.STOP;
            case "ABORT_DESTROY_BLOCK" -> Type.ABORT;
            default -> Type.INVALID;
        };
    }


    public enum Type{
        START, STOP, ABORT, INVALID
    }
}
