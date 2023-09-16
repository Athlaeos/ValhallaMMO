package me.athlaeos.valhallammo.crafting.blockvalidations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.blockvalidations.implementations.*;
import me.athlaeos.valhallammo.localization.TranslationManager;
import org.bukkit.Material;

import java.util.*;

public class ValidationRegistry {
    private static final List<Validation> validations = new ArrayList<>();

    static {
        register(new Waterlogged());
        register(new NotWaterlogged());
        register(new HotBlockUnder());
        register(new CauldronRequiresLevel());
        register(new CauldronGainsLevel());
        register(new CauldronConsumesLevel());
        register(new CampfireUnlit());
        register(new CampfireLit());

        int quantity = ValhallaMMO.getPluginConfig().getInt("validation_surrounding_quantity", 20);
        register(new BlockSurrounded(Material.STONE_BRICKS, quantity, TranslationManager.getTranslation("validation_warning_not_surrounded_by_stone_brick")));
        register(new BlockSurrounded(Material.BRICKS, quantity, TranslationManager.getTranslation("validation_warning_not_surrounded_by_brick")));
        register(new BlockSurrounded(Material.DEEPSLATE_TILES, quantity, TranslationManager.getTranslation("validation_warning_not_surrounded_by_deepslate_brick")));
        register(new BlockSurrounded(Material.NETHER_BRICKS, quantity, TranslationManager.getTranslation("validation_warning_not_surrounded_by_nether_brick")));
        register(new BlockSurrounded(Material.POLISHED_BLACKSTONE_BRICKS, quantity, TranslationManager.getTranslation("validation_warning_not_surrounded_by_blackstone_brick")));
        register(new BlockSurrounded(Material.END_STONE_BRICKS, quantity, TranslationManager.getTranslation("validation_warning_not_surrounded_by_end_brick")));

        register(new BlockSurroundedTiered(quantity, TranslationManager.getTranslation("validation_warning_stone_or_better_required"), Material.STONE_BRICKS, Material.BRICKS, Material.DEEPSLATE_TILES, Material.NETHER_BRICK, Material.POLISHED_BLACKSTONE_BRICKS, Material.END_STONE_BRICKS));
        register(new BlockSurroundedTiered(quantity, TranslationManager.getTranslation("validation_warning_brick_or_better_required"), Material.BRICKS, Material.DEEPSLATE_TILES, Material.NETHER_BRICK, Material.POLISHED_BLACKSTONE_BRICKS, Material.END_STONE_BRICKS));
        register(new BlockSurroundedTiered(quantity, TranslationManager.getTranslation("validation_warning_deepslate_or_better_required"), Material.DEEPSLATE_TILES, Material.NETHER_BRICK, Material.POLISHED_BLACKSTONE_BRICKS, Material.END_STONE_BRICKS));
        register(new BlockSurroundedTiered(quantity, TranslationManager.getTranslation("validation_warning_nether_or_better_required"), Material.NETHER_BRICKS, Material.POLISHED_BLACKSTONE_BRICKS, Material.END_STONE_BRICKS));
        register(new BlockSurroundedTiered(quantity, TranslationManager.getTranslation("validation_warning_blackstone_or_better_required"), Material.POLISHED_BLACKSTONE_BRICKS, Material.END_STONE_BRICKS));
        register(new BlockSurroundedTiered(quantity, TranslationManager.getTranslation("validation_warning_end_or_better_required"), Material.END_STONE_BRICKS));

        int radius = ValhallaMMO.getPluginConfig().getInt("validation_vacinity_radius", 5);
        int height = ValhallaMMO.getPluginConfig().getInt("validation_vacinity_height", 2);
        register(new BlockInRange(Material.ANVIL, radius, height, TranslationManager.getTranslation("validation_warning_anvil_nearby_required")));
        register(new BlockInRange(Material.SMITHING_TABLE, radius, height, TranslationManager.getTranslation("validation_warning_smithingtable_nearby_required")));
        register(new BlockInRange(Material.CRAFTING_TABLE, radius, height, TranslationManager.getTranslation("validation_warning_craftingtable_nearby_required")));
        register(new BlockInRange(Material.FLETCHING_TABLE, radius, height, TranslationManager.getTranslation("validation_warning_fletchingtable_nearby_required")));
        register(new BlockInRange(Material.CAULDRON, radius, height, TranslationManager.getTranslation("validation_warning_cauldron_nearby_required")));
        register(new BlockInRange(Material.BREWING_STAND, radius, height, TranslationManager.getTranslation("validation_warning_brewingstand_nearby_required")));
        register(new BlockInRange(Material.GRINDSTONE, radius, height, TranslationManager.getTranslation("validation_warning_grindstone_nearby_required")));
        register(new BlockInRange(Material.STONECUTTER, radius, height, TranslationManager.getTranslation("validation_warning_stonecutter_nearby_required")));
        register(new BlockInRange(Material.CARTOGRAPHY_TABLE, radius, height, TranslationManager.getTranslation("validation_warning_cartographytable_nearby_required")));
        register(new BlockInRange(Material.LOOM, radius, height, TranslationManager.getTranslation("validation_warning_loom_nearby_required")));
        register(new BlockInRange(Material.LECTERN, radius, height, TranslationManager.getTranslation("validation_warning_lectern_nearby_required")));
        register(new BlockInRange(Material.COMPOSTER, radius, height, TranslationManager.getTranslation("validation_warning_composter_nearby_required")));
        register(new BlockInRange(Material.CAMPFIRE, radius, height, TranslationManager.getTranslation("validation_warning_campfire_nearby_required")));
        register(new BlockInRange(Material.SOUL_CAMPFIRE, radius, height, TranslationManager.getTranslation("validation_warning_soulcampfire_nearby_required")));
        register(new BlockInRange(Material.FURNACE, radius, height, TranslationManager.getTranslation("validation_warning_furnace_nearby_required")));
        register(new BlockInRange(Material.SMOKER, radius, height, TranslationManager.getTranslation("validation_warning_smoker_nearby_required")));
        register(new BlockInRange(Material.BLAST_FURNACE, radius, height, TranslationManager.getTranslation("validation_warning_blastfurnace_nearby_required")));
        register(new BlockInRange(Material.ENCHANTING_TABLE, radius, height, TranslationManager.getTranslation("validation_warning_enchantmenttable_nearby_required")));
        register(new BlockInRange(Material.BEACON, radius, height, TranslationManager.getTranslation("validation_warning_beacon_nearby_required")));
        register(new BlockInRange(Material.CANDLE, radius, height, TranslationManager.getTranslation("validation_warning_candle_nearby_required")));
        register(new BlockInRange(Material.WATER, Material.WATER_BUCKET, radius, height, TranslationManager.getTranslation("validation_warning_water_nearby_required")));
        register(new BlockInRange(Material.LAVA, Material.LAVA_BUCKET, radius, height, TranslationManager.getTranslation("validation_warning_lava_nearby_required")));
    }

    public static void register(Validation v){
        validations.add(v);
    }

    public static List<Validation> getValidations() {
        return validations;
    }
    public static List<Validation> getValidations(Material block) {
        return validations.stream().filter(v -> v.isCompatible(block)).toList();
    }
    public static Validation getValidation(String id){
        if (id == null) return null;
        return validations.stream().filter(v -> v.id().equals(id)).findAny().orElse(null);
    }
}
