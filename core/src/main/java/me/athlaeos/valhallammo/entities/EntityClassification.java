package me.athlaeos.valhallammo.entities;

import me.athlaeos.valhallammo.dom.Catch;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public enum EntityClassification {
    ALIVE("ALLAY", "AXOLOTL", "BAT", "BEE", "BLAZE", "CAT", "CAVE_SPIDER", "CHICKEN", "COD", "COW", "CREEPER",
            "DOLPHIN", "DONKEY", "ELDER_GUARDIAN", "ENDER_DRAGON", "ENDERMAN", "ENDERMITE", "EVOKER", "FOX",
            "FROG", "GHAST", "GLOW_SQUID", "GOAT", "GUARDIAN", "HOGLIN", "HORSE", "ILLUSIONER", "IRON_GOLEM",
            "LLAMA", "MAGMA_CUBE", "MULE", "MUSHROOM_COW", "OCELOT", "PANDA", "PARROT", "PIG", "PIGLIN",
            "PIGLIN_BRUTE", "PILLAGER", "PLAYER", "POLAR_BEAR", "PUFFERFISH", "RABBIT", "RAVAGER", "SALMON",
            "SHEEP", "SHULKER", "SILVERFISH", "SLIME", "SNOWMAN", "SPIDER", "SQUID", "STRIDER", "TADPOLE",
            "TRADER_LLAMA", "TROPICAL_FISH", "TURTLE", "VEX", "VILLAGER", "VINDICATOR", "WANDERING_TRADER",
            "WARDEN", "WITCH", "WOLF", "CAMEL", "SNIFFER", "ARMADILLO", "BREEZE", "CREAKING", "CREAKING_TRANSIENT"), // living entities
    UNALIVE("AREA_EFFECT_CLOUD", "ARMOR_STAND", "ARROW", "BOAT", "CHEST_BOAT", "DRAGON_FIREBALL", "DROPPED_ITEM",
            "EGG", "ENDER_CRYSTAL", "ENDER_PEARL", "ENDER_SIGNAL", "EVOKER_FANGS", "EXPERIENCE_ORB", "FALLING_BLOCK",
            "FIREBALL", "FIREWORK", "FISHING_HOOK", "GLOW_ITEM_FRAME", "ITEM_FRAME", "LEASH_HITCH", "LIGHTNING",
            "LLAMA_SPIT", "MARKER", "MINECART", "MINECART_CHEST", "MINECART_COMMAND", "MINECART_FURNACE",
            "MINECART_HOPPER", "MINECART_MOB_SPAWNER", "MINECART_TNT", "PAINTING", "PRIMED_TNT", "SHULKER_BULLET",
            "SMALL_FIREBALL", "SNOWBALL", "SPECTRAL_ARROW", "SPLASH_POTION", "THROWN_EXP_BOTTLE", "TRIDENT",
            "WITHER_SKULL", "UNKNOWN", "OAK_BOAT", "SPRUCE_BOAT", "BIRCH_BOAT", "DARK_OAK_BOAT",
            "ACACIA_BOAT", "JUNGLE_BOAT", "CHERRY_BOAT", "PALE_OAK_BOAT", "MANGROVE_BOAT", "BAMBOO_RAFT",
            "OAK_CHEST_BOAT", "SPRUCE_CHEST_BOAT", "BIRCH_CHEST_BOAT", "DARK_OAK_CHEST_BOAT",
            "ACACIA_CHEST_BOAT", "JUNGLE_CHEST_BOAT", "CHERRY_CHEST_BOAT", "PALE_OAK_CHEST_BOAT",
            "MANGROVE_CHEST_BOAT", "BAMBOO_CHEST_RAFT"), // entities that were never alive (e.g. POTION_EFFECT_CLOUD)
    UNDEAD("DROWNED", "GIANT", "HUSK", "PHANTOM", "SKELETON", "SKELETON_HORSE", "STRAY", "WITHER",
            "WITHER_SKELETON", "ZOGLIN", "ZOMBIE", "ZOMBIE_HORSE", "ZOMBIE_VILLAGER", "ZOMBIFIED_PIGLIN",
            "BOGGED"), // undead entities
    SCULK("WARDEN"), // sculk entities
    ARTHROPOD("BEE", "CAVE_SPIDER", "ENDERMITE", "SILVERFISH", "SPIDER"), // arthropod entities
    HOSTILE("BLAZE", "CAVE_SPIDER", "CREEPER", "DROWNED", "ELDER_GUARDIAN", "ENDER_DRAGON", "ENDERMITE",
            "EVOKER", "GHAST", "GUARDIAN", "HOGLIN", "HUSK", "ILLUSIONER", "MAGMA_CUBE", "PHANTOM", "PIGLIN_BRUTE",
            "PILLAGER", "RAVAGER", "SHULKER", "SILVERFISH", "SKELETON", "SLIME", "SPIDER", "STRAY", "VEX",
            "VINDICATOR", "WARDEN", "WITCH", "WITHER", "WITHER_SKELETON", "ZOGLIN", "ZOMBIE", "ZOMBIE_VILLAGER",
            "BREEZE", "BOGGED", "CREAKING"), // hostile entities
    NEUTRAL("BEE", "CAVE_SPIDER", "ENDERMAN", "GOAT", "IRON_GOLEM", "LLAMA", "PANDA", "PIGLIN", "POLAR_BEAR",
            "SNOWMAN", "SPIDER", "TRADER_LLAMA", "WOLF", "ZOMBIFIED_PIGLIN"), // neutral entities
    PASSIVE("ALLAY", "AXOLOTL", "BAT", "CAT", "CHICKEN", "COD", "COW", "DOLPHIN", "DONKEY", "FOX", "FROG",
            "GLOW_SQUID", "HORSE", "MULE", "MUSHROOM_COW", "OCELOT", "PARROT", "PIG", "POLAR_BEAR", "PUFFERFISH",
            "RABBIT", "SALMON", "SHEEP", "SKELETON_HORSE", "SQUID", "STRIDER", "TADPOLE", "TROPICAL_FISH",
            "TURTLE", "VILLAGER", "WANDERING_TRADER", "ZOMBIE_HORSE", "SNIFFER", "CAMEL", "ARMADILLO"), // passive entities
    FRIENDLY("ALLAY", "AXOLOTL", "CAT", "DOLPHIN", "DONKEY", "FOX", "HORSE", "LLAMA", "MULE", "PARROT",
            "PIGLIN", "SKELETON_HORSE", "SNOWMAN", "TRADER_LLAMA", "WOLF", "ZOMBIE_HORSE"), // friendly/tameable entities (e.g. ALLAY, WOLF, PARROT)
    VILLAGER("VILLAGER", "WANDERING_TRADER"), // villagers
    ILLAGER("EVOKER", "ILLUSIONER", "PILLAGER", "RAVAGER", "VEX", "VINDICATOR", "WITCH"), // illagers
    ANIMAL("AXOLOTL", "BAT", "BEE", "CAT", "CHICKEN", "COD", "COW", "DOLPHIN", "DONKEY", "FOX", "FROG",
            "GLOW_SQUID", "GOAT", "HORSE", "LLAMA", "MULE", "MUSHROOM_COW", "OCELOT", "PANDA", "PARROT", "PIG",
            "POLAR_BEAR", "PUFFERFISH", "RABBIT", "SALMON", "SHEEP", "SKELETON_HORSE", "SQUID", "STRIDER",
            "TADPOLE", "TRADER_LLAMA", "TROPICAL_FISH", "TURTLE", "WOLF", "ZOMBIE_HORSE", "ARMADILLO", "HOGLIN"), // animals
    OVERWORLD_NATIVE("ALLAY", "AXOLOTL", "BAT", "BEE", "CAT", "CAVE_SPIDER", "CHICKEN", "COD", "COW", "CREEPER",
            "DOLPHIN", "DONKEY", "DROWNED", "ELDER_GUARDIAN", "EVOKER", "FOX", "FROG", "GOAT", "GUARDIAN",
            "HORSE", "HUSK", "IRON_GOLEM", "LLAMA", "MULE", "MUSHROOM_COW", "OCELOT", "PANDA", "PARROT",
            "PHANTOM", "PIG", "PILLAGER", "PUFFERFISH", "RABBIT", "RAVAGER", "SALMON", "SHEEP", "SILVERFISH",
            "SKELETON", "SKELETON_HORSE", "SLIME", "SPIDER", "SQUID", "STRAY", "TADPOLE", "TRADER_LLAMA",
            "TROPICAL_FISH", "TURTLE", "VEX", "VILLAGER", "VINDICATOR", "WANDERING_TRADER", "WARDEN", "WITCH",
            "WOLF", "ZOMBIE", "ZOMBIE_VILLAGER", "ARMADILLO", "BREEZE", "BOGGED", "CREAKING", "CREAKING_TRANSIENT"), // creatures that spawn in the overworld
    NETHER_NATIVE("BLAZE", "GHAST", "HOGLIN", "MAGMA_CUBE", "PIGLIN", "PIGLIN_BRUTE", "SKELETON", "STRIDER",
            "WITHER_SKELETON", "ZOMBIFIED_PIGLIN"), // creatures that spawn in the nether
    END_NATIVE("ENDER_DRAGON", "ENDERMAN", "ENDERMITE", "SHULKER"), // creatures that spawn in the end
    BOSS("ELDER_GUARDIAN", "ENDER_DRAGON", "EVOKER", "WARDEN", "WITHER"), // (mini) bosses
    AQUATIC("AXOLOTL", "COD", "DOLPHIN", "DROWNED", "ELDER_GUARDIAN", "FROG", "GUARDIAN", "PUFFERFISH",
            "SALMON", "SQUID", "TADPOLE", "TROPICAL_FISH"), // waterborn entities
    AIRBORN("ALLAY", "BAT", "BEE", "BLAZE", "ENDER_DRAGON", "GHAST", "PHANTOM", "VEX", "WITHER", "BREEZE"), // airborn entities
    PROJECTILE("ARROW", "DRAGON_FIREBALL", "EGG", "ENDER_PEARL", "ENDER_SIGNAL", "FIREBALL", "FIREWORK",
            "LLAMA_SPIT", "SHULKER_BULLET", "SMALL_FIREBALL", "SNOWBALL", "SPECTRAL_ARROW", "SPLASH_POTION",
            "THROWN_EXP_BOTTLE", "TRIDENT", "WITHER_SKULL"), // projectiles
    RIDEABLE("BOAT", "CHEST_BOAT", "DONKEY", "HORSE", "LLAMA", "MINECART", "MULE", "SKELETON_HORSE",
            "STRIDER", "TRADER_LLAMA", "ZOMBIE_HORSE", "OAK_BOAT", "SPRUCE_BOAT", "BIRCH_BOAT", "DARK_OAK_BOAT",
            "ACACIA_BOAT", "JUNGLE_BOAT", "CHERRY_BOAT", "PALE_OAK_BOAT", "MANGROVE_BOAT", "BAMBOO_RAFT",
            "OAK_CHEST_BOAT", "SPRUCE_CHEST_BOAT", "BIRCH_CHEST_BOAT", "DARK_OAK_CHEST_BOAT",
            "ACACIA_CHEST_BOAT", "JUNGLE_CHEST_BOAT", "CHERRY_CHEST_BOAT", "PALE_OAK_CHEST_BOAT",
            "MANGROVE_CHEST_BOAT", "BAMBOO_CHEST_RAFT"), // entities that are naturally rideable by player (not through commands/API)
    STRUCTURE("ARMOR_STAND", "ENDER_CRYSTAL", "GLOW_ITEM_FRAME", "ITEM_FRAME", "PAINTING"), // structural entities (e.g. ENDER_CRYSTAL or ARMOR_STAND)
    UNNATURAL("GIANT", "ILLUSIONER", "ZOMBIE_HORSE"), // entities that do not spawn (e.g. GIANT or ILLUSIONER)
    BUILDABLE("IRON_GOLEM", "SNOWMAN", "WITHER"), // entities that can be spawned through structures (e.g. SNOW_GOLEM or IRON_GOLEM)
    EXPLODABLE("CREEPER", "DRAGON_FIREBALL", "ENDER_CRYSTAL", "FIREBALL", "FIREWORK", "MINECART_TNT",
            "PRIMED_TNT"), // entities that are capable of exploding
    OTHER("EVOKER_FANGS", "EXPERIENCE_ORB", "FALLING_BLOCK", "FISHING_HOOK", "LEASH_HITCH", "LIGHTNING",
            "MARKER", "PLAYER", "UNKNOWN"),
    NOT_FOUND; // entities that do not belong to a specific classification
    private final Set<String> types;

    EntityClassification(String... type) {
        types = new HashSet<>(Arrays.asList(type));
    }

    public Set<String> getTypes() {
        return types;
    }

    public static Set<EntityClassification> getMatchingClassifications(EntityType type) {
        Set<EntityClassification> classifications = new HashSet<>();
        for (EntityClassification classification : values()) {
            if (classification.getTypes().contains(type.toString())) classifications.add(classification);
        }
        if (classifications.isEmpty()) classifications.add(NOT_FOUND);
        return classifications;
    }

    public static boolean matchesClassification(EntityType type, EntityClassification classification) {
        return classification.getTypes().contains(type.toString());
    }

    public static Collection<EntityType> getEntityTypes(Predicate<EntityClassification> filter){
        Collection<EntityType> types = new HashSet<>();
        for (EntityClassification classification : values()){
            if (!filter.test(classification)) continue;
            for (String type : classification.getTypes()){
                EntityType t = Catch.catchOrElse(() -> EntityType.valueOf(type), null);
                if (t == null) continue;
                types.add(t);
            }
        }
        return types;
    }
}