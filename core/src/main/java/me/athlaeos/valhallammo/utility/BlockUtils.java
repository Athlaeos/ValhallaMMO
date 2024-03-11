package me.athlaeos.valhallammo.utility;

import com.jeff_media.customblockdata.CustomBlockData;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Action;
import me.athlaeos.valhallammo.dom.Catch;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.CaveVines;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockUtils {
    private static final Map<Material, Float> customBlockHardnesses = new HashMap<>();
    static {
        YamlConfiguration config = ConfigManager.getConfig("default_block_hardnesses.yml").get();
        ConfigurationSection section = config.getConfigurationSection("");
        if (section != null){
            for (String material : section.getKeys(false)){
                Material block = Catch.catchOrElse(() -> Material.valueOf(material), null);
                if (block == null) ValhallaMMO.logWarning("Material in default_block_hardnesses.yml is invalid: " + material);
                else customBlockHardnesses.put(block, (float) config.getDouble(material));
            }
        }
    }
    private static final NamespacedKey BLOCK_OWNER = new NamespacedKey(ValhallaMMO.getInstance(), "block_owner");
    private static final NamespacedKey CUSTOM_HARDNESS = new NamespacedKey(ValhallaMMO.getInstance(), "custom_hardness");

    public static void setCustomHardness(Block b, float hardness){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        customBlockData.set(CUSTOM_HARDNESS, PersistentDataType.FLOAT, hardness);
    }

    public static void setDefaultHardness(Material m, Float hardness){
        if (hardness == null) customBlockHardnesses.remove(m);
        else customBlockHardnesses.put(m, hardness);
    }

    public static float getHardness(Block b){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        return customBlockData.getOrDefault(CUSTOM_HARDNESS, PersistentDataType.FLOAT, customBlockHardnesses.getOrDefault(b.getType(), b.getType().getHardness()));
    }

    public static boolean hasCustomHardness(Block b){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        return customBlockData.has(CUSTOM_HARDNESS, PersistentDataType.FLOAT);
    }

    public static void removeCustomHardness(Block b){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        customBlockData.remove(CUSTOM_HARDNESS);
    }

    public static void setOwner(Block b, UUID owner){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        customBlockData.set(BLOCK_OWNER, PersistentDataType.STRING, owner.toString());
    }

    public static boolean hasOwnership(Block b){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        return customBlockData.has(BLOCK_OWNER, PersistentDataType.STRING);
    }

    public static void removeOwnership(Block b){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        customBlockData.remove(BLOCK_OWNER);
    }

    public static Player getOwner(Block b){
        PersistentDataContainer customBlockData = new CustomBlockData(b, ValhallaMMO.getInstance());
        String value = customBlockData.get(BLOCK_OWNER, PersistentDataType.STRING);
        if (value != null) return ValhallaMMO.getInstance().getServer().getPlayer(UUID.fromString(value));
        return null;
    }
    private static final int[][] offsets = new int[][]{
            {1, 0, 0},
            {-1, 0, 0},
            {0, 1, 0},
            {0, -1, 0},
            {0, 0, 1},
            {0, 0, -1}
    };

    public static Collection<Block> getBlocksTouching(Block start, int radiusX, int radiusY, int radiusZ, Predicate<Material> filter){
        Collection<Block> blocks = new HashSet<>();
        for(double x = start.getLocation().getX() - radiusX; x <= start.getLocation().getX() + radiusX; x++){
            for(double y = start.getLocation().getY() - radiusY; y <= start.getLocation().getY() + radiusY; y++){
                for(double z = start.getLocation().getZ() - radiusZ; z <= start.getLocation().getZ() + radiusZ; z++){
                    Location loc = new Location(start.getWorld(), x, y, z);
                    blocks.add(loc.getBlock());
                }
            }
        }
        if (filter == null) return blocks;
        return blocks.stream().filter(block -> {
            for (int[] offset : offsets){
                Location l = block.getLocation().add(offset[0], offset[1], offset[2]);
                if (filter.test(l.getBlock().getType())) return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    public static Collection<Block> getBlocksTouchingAnything(Block start, int radiusX, int radiusY, int radiusZ){
        return getBlocksTouching(start, radiusX, radiusY, radiusZ, (m) -> !m.isAir());
    }

    public static Collection<Block> getBlockVein(Block origin, int limit, Predicate<Block> filter, int[]... offsets){
        if (offsets.length == 0 || limit <= 0) return new ArrayList<>();
        Collection<Block> vein = new HashSet<>(Set.of(origin));
        Collection<Block> scanBlocks = new HashSet<>(Set.of(origin));
        getSurroundingBlocks(scanBlocks, vein, limit, filter, offsets);

        return vein;
    }

    private static void getSurroundingBlocks(Collection<Block> scanBlocks, Collection<Block> currentVein, int limit, Predicate<Block> filter, int[]... offsets){
        Collection<Block> newBlocksToScan = new HashSet<>();
        int prevSize = currentVein.size();
        if (currentVein.size() >= limit) return;

        for (Block b : scanBlocks){
            for (int[] offset : offsets){
                Block block = b.getLocation().add(offset[0], offset[1], offset[2]).getBlock();
                if (!filter.test(block) || currentVein.contains(block)) continue;
                currentVein.add(block);
                if (currentVein.size() >= limit) return;
                newBlocksToScan.add(block);
            }
        }
        if (newBlocksToScan.isEmpty() || prevSize == currentVein.size()) return;
        getSurroundingBlocks(newBlocksToScan, currentVein, limit, filter, offsets);
    }

    private static final Map<String, Collection<UUID>> blockAlteringPlayers = new HashMap<>();

    public static Map<String, Collection<UUID>> getBlockAlteringPlayers() {
        return blockAlteringPlayers;
    }

    public static void processBlocks(Player responsible, Collection<Block> blocks, Predicate<Player> validation, Action<Block> process, Action<Player> onFinish){
        if (process == null || blocks.isEmpty()) return;
        Block lastBlock = null;
        for (Block b : blocks){
            if (validation != null && !validation.test(responsible)) continue;
            process.act(b);
            lastBlock = b;
        }
        if (onFinish != null && lastBlock != null) onFinish.act(responsible);
    }

    private static final long PULSE_DELAY = 2L;
    public static void processBlocksPulse(Player responsible, Block origin, Collection<Block> blocks, Predicate<Player> validation, Action<Block> process, Action<Player> onFinish){
        Map<Double, List<Block>> sortedByDistance = new HashMap<>();
        for (Block b : blocks) {
            double distance = b.getLocation().distanceSquared(origin.getLocation());
            List<Block> existingBlocks = sortedByDistance.getOrDefault(distance, new ArrayList<>());
            existingBlocks.add(b);
            sortedByDistance.put(distance, existingBlocks);
        }
        int highest = 0;
        for (Double distance : sortedByDistance.keySet()){
            int time = (int) MathUtils.sqrt(distance);
            if (time > highest) highest = time;
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> processBlocks(responsible, sortedByDistance.get(distance), validation, process, null), time * PULSE_DELAY);
        }
        if (onFinish != null) {
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> onFinish.act(responsible), (highest * PULSE_DELAY) + 1);
        }
    }

    public static void processBlocksDelayed(Player responsible, Collection<Block> blocks, Predicate<Player> validation, Action<Block> process, Action<Player> onFinish){
        Iterator<Block> iterator = blocks.iterator();
        new BukkitRunnable(){
            @Override
            public void run() {
                if (iterator.hasNext()){
                    Block b = iterator.next();
                    if (!validation.test(responsible)) {
                        if (onFinish != null) onFinish.act(responsible);
                        cancel();
                    }
                    else process.act(b);
                } else {
                    if (onFinish != null) onFinish.act(responsible);
                    cancel();
                }
            }
        }.runTaskTimer(ValhallaMMO.getInstance(), 0L, 1L);
    }

    private static final Collection<Material> ageableExceptions = ItemUtils.getMaterialSet(Arrays.asList(
            "SUGAR_CANE", "BAMBOO", "CACTUS", "CHORUS_FLOWER", "KELP", "TWISTING_VINES", "WEEPING_VINES", "FROSTED_ICE", "MELON_STEM", "PUMPKIN_STEM"
    ));

    /**
     * Returns true if: <br>
     * - The block is Ageable and not in the list of exceptions, and of max age (fully grown)<br>
     * - The block is not ageable and not previously placed<br>
     * @param b the block to check if they're legible for farming rewards
     * @return true if legible, false if the block is not ageable and placed, or if block is ageable and not fully grown.
     */
    public static boolean canReward(Block b){
        BlockData data = b.getBlockData();
        if (data instanceof CaveVines c) return c.isBerries();
        if (!(data instanceof Ageable a) || ageableExceptions.contains(b.getType())) return !BlockStore.isPlaced(b);
        return a.getAge() >= a.getMaximumAge();
    }

    @SuppressWarnings("all")
    public static boolean canReward(BlockState b){
        BlockData data = b.getBlockData();
        if (data instanceof CaveVines c) return c.isBerries();
        if (!(data instanceof Ageable a) || ageableExceptions.contains(b.getType())) return !BlockStore.isPlaced(b.getBlock());
        return a.getAge() >= a.getMaximumAge();
    }

    public static void decayBlock(Block block){
        LeavesDecayEvent decayEvent = new LeavesDecayEvent(block);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(decayEvent);
        if (decayEvent.isCancelled()) return;

        block.breakNaturally();
    }

    private static final ItemStack stic = new ItemStack(Material.STICK);
    public static boolean hasDrops(Block b, Entity e, ItemStack item){
        return !b.getDrops(item == null ? stic : item, e).isEmpty();
    }
}
