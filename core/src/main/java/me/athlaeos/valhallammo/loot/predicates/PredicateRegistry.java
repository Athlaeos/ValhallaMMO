package me.athlaeos.valhallammo.loot.predicates;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.loot.predicates.implementations.*;
import org.bukkit.Material;
import org.bukkit.block.Biome;

import java.util.HashMap;
import java.util.Map;

public class PredicateRegistry {
    private static final Map<String, LootPredicate> predicates = new HashMap<>();

    static {
        register(new BiomeFilter(Material.HEART_OF_THE_SEA, "All Oceans", Biome.OCEAN, Biome.DEEP_OCEAN, Biome.WARM_OCEAN, Biome.LUKEWARM_OCEAN, Biome.DEEP_LUKEWARM_OCEAN, Biome.COLD_OCEAN, Biome.DEEP_COLD_OCEAN, Biome.FROZEN_OCEAN, Biome.DEEP_FROZEN_OCEAN));
        register(new BiomeFilter(Material.BLUE_WOOL, "Regular Oceans", Biome.OCEAN, Biome.DEEP_OCEAN));
        register(new BiomeFilter(Material.BRAIN_CORAL, "Warm Oceans", Biome.WARM_OCEAN, Biome.LUKEWARM_OCEAN, Biome.DEEP_LUKEWARM_OCEAN));
        register(new BiomeFilter(Material.BLUE_ICE, "Frozen Oceans", Biome.FROZEN_OCEAN, Biome.DEEP_FROZEN_OCEAN));
        register(new BiomeFilter(Material.ICE, "Cold Oceans", Biome.COLD_OCEAN, Biome.DEEP_COLD_OCEAN));
        register(new BiomeFilter(Material.MYCELIUM, "Mushroom Islands", Biome.MUSHROOM_FIELDS));
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20)) register(new BiomeFilter(Material.STONE, "All Highlands", Biome.JAGGED_PEAKS, Biome.FROZEN_PEAKS, Biome.STONY_PEAKS, Biome.MEADOW, Biome.CHERRY_GROVE, Biome.GROVE, Biome.SNOWY_SLOPES, Biome.WINDSWEPT_HILLS, Biome.WINDSWEPT_GRAVELLY_HILLS, Biome.WINDSWEPT_FOREST));
        else register(new BiomeFilter(Material.STONE, "All Highlands", Biome.JAGGED_PEAKS, Biome.FROZEN_PEAKS, Biome.STONY_PEAKS, Biome.MEADOW, Biome.GROVE, Biome.SNOWY_SLOPES, Biome.WINDSWEPT_HILLS, Biome.WINDSWEPT_GRAVELLY_HILLS, Biome.WINDSWEPT_FOREST));
        register(new BiomeFilter(Material.POWDER_SNOW_BUCKET, "Peaks", Biome.JAGGED_PEAKS, Biome.FROZEN_PEAKS, Biome.STONY_PEAKS));
        register(new BiomeFilter(Material.BEEHIVE, "Meadow", Biome.MEADOW));
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20)) register(new BiomeFilter(Material.CHERRY_LEAVES, "Cherry Blossom", Biome.CHERRY_GROVE));
        register(new BiomeFilter(Material.GRAVEL, "Hills", Biome.WINDSWEPT_HILLS, Biome.WINDSWEPT_GRAVELLY_HILLS, Biome.WINDSWEPT_FOREST));
        register(new BiomeFilter(Material.SNOW, "Snowy Hills", Biome.GROVE, Biome.SNOWY_SLOPES));
        register(new BiomeFilter(Material.OAK_WOOD, "All Woodlands", Biome.FOREST, Biome.FLOWER_FOREST, Biome.TAIGA, Biome.OLD_GROWTH_PINE_TAIGA, Biome.OLD_GROWTH_SPRUCE_TAIGA, Biome.SNOWY_TAIGA, Biome.BIRCH_FOREST, Biome.OLD_GROWTH_BIRCH_FOREST, Biome.DARK_FOREST, Biome.JUNGLE, Biome.SPARSE_JUNGLE, Biome.BAMBOO_JUNGLE));
        register(new BiomeFilter(Material.OAK_LOG, "Forest", Biome.FOREST, Biome.FLOWER_FOREST));
        register(new BiomeFilter(Material.BIRCH_LOG, "Birch Forest", Biome.BIRCH_FOREST, Biome.OLD_GROWTH_BIRCH_FOREST));
        register(new BiomeFilter(Material.SPRUCE_LOG, "Taiga", Biome.TAIGA, Biome.OLD_GROWTH_PINE_TAIGA, Biome.OLD_GROWTH_SPRUCE_TAIGA, Biome.SNOWY_TAIGA));
        register(new BiomeFilter(Material.JUNGLE_LOG, "Jungle", Biome.JUNGLE, Biome.SPARSE_JUNGLE, Biome.BAMBOO_JUNGLE));
        register(new BiomeFilter(Material.DARK_OAK_LOG, "Dark Oak Forest", Biome.DARK_FOREST));
        register(new BiomeFilter(Material.ROSE_BUSH, "Flower Forest", Biome.FLOWER_FOREST));
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20)) register(new BiomeFilter(Material.WATER_BUCKET, "All Wetlands", Biome.RIVER, Biome.FROZEN_RIVER, Biome.SWAMP, Biome.MANGROVE_SWAMP, Biome.BEACH, Biome.SNOWY_BEACH, Biome.STONY_SHORE));
        else register(new BiomeFilter(Material.WATER_BUCKET, "All Wetlands", Biome.RIVER, Biome.FROZEN_RIVER, Biome.SWAMP, Biome.BEACH, Biome.SNOWY_BEACH, Biome.STONY_SHORE));
        register(new BiomeFilter(Material.CLAY, "River", Biome.RIVER, Biome.FROZEN_RIVER));
        register(new BiomeFilter(Material.SLIME_BALL, "Swamp", Biome.SWAMP, Biome.MANGROVE_SWAMP));
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20)) register(new BiomeFilter(Material.MANGROVE_ROOTS, "Mangrove Swamp", Biome.MANGROVE_SWAMP));
        register(new BiomeFilter(Material.TURTLE_EGG, "Beaches", Biome.BEACH, Biome.SNOWY_BEACH));
        register(new BiomeFilter(Material.GRANITE, "Stony Shore", Biome.STONY_SHORE));
        register(new BiomeFilter(Material.GRASS_BLOCK, "All Flatlands", Biome.PLAINS, Biome.SUNFLOWER_PLAINS, Biome.SNOWY_PLAINS, Biome.ICE_SPIKES));
        register(new BiomeFilter(Material.SUNFLOWER, "Plains", Biome.PLAINS, Biome.SUNFLOWER_PLAINS));
        register(new BiomeFilter(Material.SNOW, "Snowy Plains", Biome.SNOWY_PLAINS));
        register(new BiomeFilter(Material.PACKED_ICE, "Ice Spikes", Biome.ICE_SPIKES));
        register(new BiomeFilter(Material.CACTUS, "All Arid Lands", Biome.DESERT, Biome.SAVANNA, Biome.SAVANNA_PLATEAU, Biome.WINDSWEPT_SAVANNA, Biome.BADLANDS, Biome.WOODED_BADLANDS, Biome.ERODED_BADLANDS));
        register(new BiomeFilter(Material.SAND, "Desert", Biome.DESERT));
        register(new BiomeFilter(Material.ACACIA_LOG, "Savanna", Biome.SAVANNA, Biome.SAVANNA_PLATEAU, Biome.WINDSWEPT_SAVANNA));
        register(new BiomeFilter(Material.TERRACOTTA, "Badlands", Biome.BADLANDS, Biome.ERODED_BADLANDS, Biome.WOODED_BADLANDS));

        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_19)) register(new BiomeFilter(Material.IRON_ORE, "All Caves", Biome.DEEP_DARK, Biome.DRIPSTONE_CAVES, Biome.LUSH_CAVES));
        else register(new BiomeFilter(Material.IRON_ORE, "All Caves", Biome.DRIPSTONE_CAVES, Biome.LUSH_CAVES));
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_19)) register(new BiomeFilter(Material.SCULK, "Deep Dark", Biome.DEEP_DARK));
        register(new BiomeFilter(Material.DRIPSTONE_BLOCK, "Dripstone Caves", Biome.DRIPSTONE_CAVES));
        register(new BiomeFilter(Material.MOSS_BLOCK, "Lush Caves", Biome.LUSH_CAVES));

        register(new BiomeFilter(Material.GLOWSTONE, "All Nether Biomes", Biome.NETHER_WASTES, Biome.SOUL_SAND_VALLEY, Biome.CRIMSON_FOREST, Biome.WARPED_FOREST, Biome.BASALT_DELTAS));
        register(new BiomeFilter(Material.NETHERRACK, "Nether Wastes", Biome.NETHER_WASTES));
        register(new BiomeFilter(Material.SOUL_SAND, "Soul Sand Valley", Biome.SOUL_SAND_VALLEY));
        register(new BiomeFilter(Material.NETHER_WART_BLOCK, "Crimson Forest", Biome.CRIMSON_FOREST));
        register(new BiomeFilter(Material.WARPED_WART_BLOCK, "Warped Forest", Biome.WARPED_FOREST));
        register(new BiomeFilter(Material.BASALT, "Basalt Deltas", Biome.BASALT_DELTAS));

        register(new BiomeFilter(Material.END_STONE, "The End", Biome.THE_END, Biome.SMALL_END_ISLANDS, Biome.END_MIDLANDS, Biome.END_HIGHLANDS, Biome.END_BARRENS));
        register(new BiomeFilter(Material.END_CRYSTAL, "The End (Main Island)", Biome.THE_END));
        register(new BiomeFilter(Material.PURPUR_BLOCK, "The End (End Cities)", Biome.END_MIDLANDS, Biome.END_HIGHLANDS));
        register(new BiomeFilter(Material.CHORUS_FLOWER, "The End (Chorus Plants)", Biome.END_HIGHLANDS));

        register(new BlockMaterialFilter());
        register(new BlockSurroundedMaterialFilter());
        register(new ContextEntityKillerFilter());
        register(new DamageTypeDeathCauseFilter());
        register(new DayFilter());
        register(new KilledWhileOnFireFilter());
        register(new KilledWhileParriedFilter());
        register(new LightFilter());
        register(new LuckFilter());
        register(new MinedWithEquipmentTypeFilter());
        register(new MinedWithExplosionFilter());
        register(new MinedWithFireAspectFilter());
        register(new MinedWithFortuneFilter());
        register(new MinedWithPreferredToolFilter());
        register(new MinedWithSilkTouchFilter());
        register(new MinedWithTaggedToolFilter());
        register(new MinedWithToolFilter());
        register(new MoonPhaseFilter());
        if (ValhallaMMO.isHookFunctional(WorldGuardHook.class)) register(new RegionFilter());
        register(new SkyLightFilter());
        register(new TimePeriodFilter());
        register(new WorldFilter());
        register(new EntityLevelFilter());
        register(new BabyFilter());
        register(new NearbyStructureFilter());
    }

    public static void register(LootPredicate predicate){
        predicates.put(predicate.getKey(), predicate);
    }

    public static LootPredicate createPredicate(String name){
        if (!predicates.containsKey(name)) throw new IllegalArgumentException("Loot Predicate " + name + " doesn't exist");
        return predicates.get(name).createNew();
    }

    public static Map<String, LootPredicate> getPredicates() {
        return predicates;
    }
}
