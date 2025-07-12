package me.athlaeos.valhallammo;

import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantLevel;
import me.athlaeos.valhallammo.trading.dom.MerchantTrade;
import me.athlaeos.valhallammo.trading.dom.MerchantType;
import me.athlaeos.valhallammo.trading.dom.ProfessionWrapper;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

public class Scripts implements Listener {
    private static final QuickTrade[] args = new QuickTrade[]{
            new QuickTrade("armorer_sell_lava_bucket", ProfessionWrapper.ARMORER, MerchantLevel.JOURNEYMAN, "6x 100% 1-0%-1xLAVA_BUCKET for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("armorer_buy_iron_helmet", ProfessionWrapper.ARMORER, MerchantLevel.JOURNEYMAN, "3x W25 15-25%-30xEMERALD for 1xIRON_HELMET G12.5 EXP=800 ENCH=16 VIL=6 ORD=8"),
            new QuickTrade("armorer_buy_iron_chestplate", ProfessionWrapper.ARMORER, MerchantLevel.JOURNEYMAN, "3x W25 24-25%-48xEMERALD for 1xIRON_CHESTPLATE G12.5 EXP=800 ENCH=16 VIL=6 ORD=8"),
            new QuickTrade("armorer_buy_iron_leggings", ProfessionWrapper.ARMORER, MerchantLevel.JOURNEYMAN, "3x W25 21-25%-42xEMERALD for 1xIRON_LEGGINGS G12.5 EXP=800 ENCH=16 VIL=6 ORD=8"),
            new QuickTrade("armorer_buy_iron_boots", ProfessionWrapper.ARMORER, MerchantLevel.JOURNEYMAN, "3x W25 12-25%-24xEMERALD for 1xIRON_BOOTS G12.5 EXP=800 ENCH=16 VIL=6 ORD=8"),
            new QuickTrade("armorer_sell_diamond", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "12x 100% 1-10%-2xDIAMOND for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=3 ORD=0"),
            new QuickTrade("armorer_buy_diamond_helmet", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "2x W25 25-25%-40xDIAMOND for 1xDIAMOND_HELMET G5 EXP=1600 ENCH=32 VIL=12 ORD=4"),
            new QuickTrade("armorer_buy_diamond_chestplate", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "2x W25 40-25%-64xEMERALD for 1xDIAMOND_CHESTPLATE G5 EXP=1600 ENCH=32 VIL=12 ORD=4"),
            new QuickTrade("armorer_buy_diamond_leggings", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "2x W25 35-25%-54xEMERALD for 1xDIAMOND_LEGGINGS G5 EXP=1600 ENCH=32 VIL=12 ORD=4"),
            new QuickTrade("armorer_buy_diamond_boots", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "2x W25 20-25%-32xEMERALD for 1xDIAMOND_BOOTS G5 EXP=1600 ENCH=32 VIL=12 ORD=4"),
            new QuickTrade("armorer_buy_netherite_scrap", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "1x 100% 32-25%-64xEMERALD for 1xNETHERITE_SCRAP NOGIFT EXP=1600 ENCH=32 VIL=12 ORD=2"),
            new QuickTrade("armorer_buy_royal_diamond_helmet", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "E1x W25 10-0%-10xEMERALD_BLOCK for 1xDIAMOND_HELMET NOGIFT EXP=3200 ENCH=50 VIL=32 ORD=0"),
            new QuickTrade("armorer_buy_royal_diamond_chestplate", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "E1x W25 16-0%-16xEMERALD_BLOCK for 1xDIAMOND_CHESTPLATE NOGIFT EXP=3200 ENCH=50 VIL=32 ORD=0"),
            new QuickTrade("armorer_buy_royal_diamond_leggings", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "E1x W25 14-0%-14xEMERALD_BLOCK for 1xDIAMOND_LEGGINGS NOGIFT EXP=3200 ENCH=50 VIL=32 ORD=0"),
            new QuickTrade("armorer_buy_royal_diamond_boots", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "E1x W25 8-0%-8xEMERALD_BLOCK for 1xDIAMOND_BOOTS NOGIFT EXP=3200 ENCH=50 VIL=32 ORD=0"),

            new QuickTrade("butcher_buy_rabbit_stew", ProfessionWrapper.BUTCHER, MerchantLevel.NOVICE, "12x 100% 1-10%-2xEMERALD for 1xRABBIT_STEW G25 EXP=100 ENCH=1 VIL=1 ORD=16"),
            new QuickTrade("butcher_sell_raw_rabbit", ProfessionWrapper.BUTCHER, MerchantLevel.NOVICE, "12x W33 3-10%-6xRABBIT for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("butcher_sell_raw_chicken", ProfessionWrapper.BUTCHER, MerchantLevel.NOVICE, "12x W33 4-10%-8xCHICKEN for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("butcher_sell_raw_pork", ProfessionWrapper.BUTCHER, MerchantLevel.NOVICE, "12x W33 6-10%-12xPORKCHOP for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("butcher_sell_coal", ProfessionWrapper.BUTCHER, MerchantLevel.APPRENTICE, "12x 100% 12-10%-24xCOAL for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("butcher_buy_cooked_chicken", ProfessionWrapper.BUTCHER, MerchantLevel.APPRENTICE, "12x W33 1-10%-2xEMERALD for 8xCOOKED_CHICKEN G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("butcher_buy_cooked_pork", ProfessionWrapper.BUTCHER, MerchantLevel.APPRENTICE, "12x W33 1-10%-2xEMERALD for 6xCOOKED_PORKCHOP G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("butcher_buy_cooked_beef", ProfessionWrapper.BUTCHER, MerchantLevel.APPRENTICE, "12x W33 1-10%-2xEMERALD for 6xCOOKED_BEEF G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("butcher_buy_cleaver", ProfessionWrapper.BUTCHER, MerchantLevel.JOURNEYMAN, "2x 100% 8-25%-16xEMERALD for 1xIRON_SWORD G5 EXP=400 ENCH=8 VIL=8 ORD=4"),
            new QuickTrade("butcher_sell_raw_mutton", ProfessionWrapper.BUTCHER, MerchantLevel.JOURNEYMAN, "12x W50 8-10%-16xBEEF for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("butcher_sell_raw_beef", ProfessionWrapper.BUTCHER, MerchantLevel.JOURNEYMAN, "12x W50 6-10%-12xMUTTON for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("butcher_sell_dried_kelp_block", ProfessionWrapper.BUTCHER, MerchantLevel.EXPERT, "12x 100% 6-10%-12xDRIED_KELP_BLOCK for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("butcher_buy_luxurious_stew", ProfessionWrapper.BUTCHER, MerchantLevel.EXPERT, "12x W25 4-25%-8xEMERALD for 1xGOLDEN_APPLE G5 EXP=400 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("butcher_buy_tender_steak", ProfessionWrapper.BUTCHER, MerchantLevel.EXPERT, "12x W25 4-25%-8xEMERALD for 1xGOLDEN_APPLE G5 EXP=400 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("butcher_buy_stuffed_hen", ProfessionWrapper.BUTCHER, MerchantLevel.EXPERT, "12x W25 4-25%-8xEMERALD for 1xGOLDEN_APPLE G5 EXP=400 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("butcher_buy_glazed_ribs", ProfessionWrapper.BUTCHER, MerchantLevel.EXPERT, "12x W25 4-25%-8xEMERALD for 1xGOLDEN_APPLE G5 EXP=400 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("butcher_sell_sweet_berries", ProfessionWrapper.BUTCHER, MerchantLevel.MASTER, "12x 100% 12-10%-24xSWEET_BERRIES for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("butcher_buy_battle_burrito", ProfessionWrapper.BUTCHER, MerchantLevel.MASTER, "E6x W25 8-25%-16xEMERALD for 1xGOLDEN_APPLE NOGIFT EXP=800 ENCH=16 VIL=16 ORD=16"),
            new QuickTrade("butcher_buy_aegis_stroganoff", ProfessionWrapper.BUTCHER, MerchantLevel.MASTER, "E6x W25 8-25%-16xEMERALD for 1xGOLDEN_APPLE NOGIFT EXP=800 ENCH=16 VIL=16 ORD=16"),
            new QuickTrade("butcher_buy_wagyu_steak", ProfessionWrapper.BUTCHER, MerchantLevel.MASTER, "E6x W25 8-25%-16xEMERALD for 1xGOLDEN_APPLE NOGIFT EXP=800 ENCH=16 VIL=16 ORD=16"),
            new QuickTrade("butcher_buy_titanic_goulash", ProfessionWrapper.BUTCHER, MerchantLevel.MASTER, "E6x W25 8-25%-16xEMERALD for 1xGOLDEN_APPLE NOGIFT EXP=800 ENCH=16 VIL=16 ORD=16"),

            new QuickTrade("cleric_sell_rotten_flesh", ProfessionWrapper.CLERIC, MerchantLevel.NOVICE, "12x 100% 32-10%-64xROTTEN_FLESH for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("cleric_buy_redstone_dust", ProfessionWrapper.CLERIC, MerchantLevel.NOVICE, "12x 100% 1-10%-2xEMERALD for 8xREDSTONE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("cleric_buy_brewing_stand", ProfessionWrapper.CLERIC, MerchantLevel.NOVICE, "12x W25 4-10%-8xEMERALD for 1xBREWING_STAND G25 EXP=200 ENCH=2 VIL=2 ORD=8"),
            new QuickTrade("cleric_buy_nether_wart", ProfessionWrapper.CLERIC, MerchantLevel.NOVICE, "12x W25 2-10%-4xEMERALD for 4xNETHER_WART G10 EXP=200 ENCH=2 VIL=2 ORD=4"),
            new QuickTrade("cleric_buy_blaze_powder", ProfessionWrapper.CLERIC, MerchantLevel.NOVICE, "12x W25 4-10%-8xEMERALD for 2xBLAZE_POWDER G10 EXP=200 ENCH=2 VIL=2 ORD=4"),
            new QuickTrade("cleric_buy_glass_bottles", ProfessionWrapper.CLERIC, MerchantLevel.NOVICE, "12x W25 2-10%-4xEMERALD for 6xGLASS_BOTTLE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("cleric_sell_gold_ingots", ProfessionWrapper.CLERIC, MerchantLevel.APPRENTICE, "12x W25 3-10%-6xGOLD_INGOT for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("cleric_buy_lapis_lazuli", ProfessionWrapper.CLERIC, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 4xLAPIS_LAZULI G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("cleric_sell_spider_eyes", ProfessionWrapper.CLERIC, MerchantLevel.APPRENTICE, "12x W25 8-10%-16xSPIDER_EYE for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("cleric_buy_fermented_spider_eye", ProfessionWrapper.CLERIC, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 1xFERMENTED_SPIDER_EYE G25 EXP=200 ENCH=2 VIL=16 ORD=16"),
            new QuickTrade("cleric_sell_rabbit_feet", ProfessionWrapper.CLERIC, MerchantLevel.JOURNEYMAN, "12x W25 2-10%-4xRABBIT_FOOT for 1xEMERALD NOGIFT EXP=400 ENCH=4 VIL=4 ORD=0"),
            new QuickTrade("cleric_buy_glowstone_dust", ProfessionWrapper.CLERIC, MerchantLevel.JOURNEYMAN, "12x W25 2-10%-4xEMERALD for 8xGLOWSTONE_DUST G25 EXP=200 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("cleric_buy_glistering_melon_slice", ProfessionWrapper.CLERIC, MerchantLevel.JOURNEYMAN, "12x W25 1-10%-2xEMERALD for 2xGLISTERING_MELON_SLICE NOGIFT EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("cleric_buy_golden_carrots", ProfessionWrapper.CLERIC, MerchantLevel.JOURNEYMAN, "12x W25 1-10%-2xEMERALD for 2xGOLDEN_CARROT G25 EXP=200 ENCH=2 VIL=16 ORD=16"),
            new QuickTrade("cleric_sell_glass_bottles", ProfessionWrapper.CLERIC, MerchantLevel.EXPERT, "12x 100% 6-10%-12xGLASS_BOTTLE for 1xEMERALD NOGIFT EXP=400 ENCH=4 VIL=4 ORD=0"),
            new QuickTrade("cleric_buy_ender_pearl", ProfessionWrapper.CLERIC, MerchantLevel.EXPERT, "12x 100% 6-10%-12xEMERALD for 1xENDER_PEARL G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("cleric_sell_turtle_scute", ProfessionWrapper.CLERIC, MerchantLevel.EXPERT, "12x W25 2-10%-4xSCUTE for 1xEMERALD NOGIFT EXP=400 ENCH=4 VIL=4 ORD=0"),
            new QuickTrade("cleric_buy_ghast_tear", ProfessionWrapper.CLERIC, MerchantLevel.EXPERT, "12x W25 4-10%-8xEMERALD for 1xGHAST_TEAR G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("cleric_buy_pollen_stalks", ProfessionWrapper.CLERIC, MerchantLevel.EXPERT, "12x W25 8-10%-16xEMERALD for 1xWHEAT G5 EXP=800 ENCH=8 VIL=16 ORD=16"),
            new QuickTrade("cleric_sell_nether_wart", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "12x 100% 24-10%-48xNETHER_WART for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("cleric_buy_experience_bottles", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "12x 100% 1-10%-2xEMERALD for 4xEXPERIENCE_BOTTLE G25 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("cleric_buy_bustling_fungus", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "E12x W15 4-10%-8xEMERALD for 1xWHEAT G5 EXP=400 ENCH=4 VIL=2 ORD=16"),
            new QuickTrade("cleric_buy_steelblossom", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "E12x W15 8-10%-16xEMERALD for 1xWHEAT G5 EXP=800 ENCH=8 VIL=16 ORD=16"),
            new QuickTrade("cleric_buy_shimmerleaf", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "E12x W15 8-10%-16xEMERALD for 1xWHEAT G5 EXP=800 ENCH=8 VIL=16 ORD=16"),
            new QuickTrade("cleric_buy_noxious_spores", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "E12x W15 4-10%-8xEMERALD for 1xWHEAT G5 EXP=400 ENCH=4 VIL=16 ORD=16"),
            new QuickTrade("cleric_buy_knightreed", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "E12x W15 4-10%-8xEMERALD for 1xWHEAT G5 EXP=400 ENCH=4 VIL=16 ORD=16"),
            new QuickTrade("cleric_buy_tutor_lotus", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "E12x W15 32-10%-64xEMERALD for 1xWHEAT G5 EXP=3200 ENCH=64 VIL=16 ORD=16"),

            new QuickTrade("farmer_buy_bread", ProfessionWrapper.FARMER, MerchantLevel.NOVICE, "12x 100% 1-10%-2xEMERALD for 6xBREAD G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_sell_wheat", ProfessionWrapper.FARMER, MerchantLevel.NOVICE, "12x W25 12-10%-24xWHEAT for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("farmer_sell_potatoes", ProfessionWrapper.FARMER, MerchantLevel.NOVICE, "12x W25 24-10%-48xPOTATO for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("farmer_sell_carrots", ProfessionWrapper.FARMER, MerchantLevel.NOVICE, "12x W25 24-10%-48xCARROT for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("farmer_sell_beetroot", ProfessionWrapper.FARMER, MerchantLevel.NOVICE, "12x W25 12-10%-24xBEETROOT for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("farmer_sell_pumpkin", ProfessionWrapper.FARMER, MerchantLevel.APPRENTICE, "12x 100% 4-10%-8xPUMPKIN for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("farmer_buy_pumpkin_pie", ProfessionWrapper.FARMER, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 4xPUMPKIN_PIE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_apples", ProfessionWrapper.FARMER, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 8xAPPLE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_cake", ProfessionWrapper.FARMER, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 1xCAKE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_mushroom_stew", ProfessionWrapper.FARMER, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 4xMUSHROOM_STEW G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_sell_melons", ProfessionWrapper.FARMER, MerchantLevel.JOURNEYMAN, "12x 100% 4-10%-8xMELON for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("farmer_buy_cookies", ProfessionWrapper.FARMER, MerchantLevel.JOURNEYMAN, "12x W25 1-10%-2xEMERALD for 12xCOOKIE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_regeneration_mushroom_stew", ProfessionWrapper.FARMER, MerchantLevel.JOURNEYMAN, "12x W25 1-10%-2xEMERALD for 2xMUSHROOM_STEW G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_night_vision_glow_berries", ProfessionWrapper.FARMER, MerchantLevel.JOURNEYMAN, "12x W25 1-10%-2xEMERALD for 4xGLOW_BERRIES G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_health_boost_beetroot_soup", ProfessionWrapper.FARMER, MerchantLevel.JOURNEYMAN, "12x W25 1-10%-2xEMERALD for 2xBEETROOT_SOUP G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_suspicious_stew_fire_resistance", ProfessionWrapper.FARMER, MerchantLevel.EXPERT, "12x W3 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_apple_pie", ProfessionWrapper.FARMER, MerchantLevel.EXPERT, "12x W56 4-10%-8xEMERALD for 1xGOLDEN_APPLE G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("farmer_buy_suspicious_stew_blindness", ProfessionWrapper.FARMER, MerchantLevel.EXPERT, "12x W7 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_suspicious_stew_saturation", ProfessionWrapper.FARMER, MerchantLevel.EXPERT, "12x W7 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_suspicious_stew_nausea", ProfessionWrapper.FARMER, MerchantLevel.EXPERT, "12x W7 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_suspicious_stew_jump_boost", ProfessionWrapper.FARMER, MerchantLevel.EXPERT, "12x W7 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_suspicious_stew_poison", ProfessionWrapper.FARMER, MerchantLevel.EXPERT, "12x W7 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_suspicious_stew_regeneration", ProfessionWrapper.FARMER, MerchantLevel.EXPERT, "12x W7 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_suspicious_stew_night_vision", ProfessionWrapper.FARMER, MerchantLevel.EXPERT, "12x W7 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_suspicious_stew_weakness", ProfessionWrapper.FARMER, MerchantLevel.EXPERT, "12x W7 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_suspicious_stew_wither", ProfessionWrapper.FARMER, MerchantLevel.EXPERT, "12x W7 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_golden_apple", ProfessionWrapper.FARMER, MerchantLevel.MASTER, "12x 100% 1-10%-2xEMERALD for 1xGOLDEN_APPLE G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_enchanted_golden_apple", ProfessionWrapper.FARMER, MerchantLevel.MASTER, "12x 100% 16-0%-16xEMERALD_BLOCK for 1xENCHANTED_GOLDEN_APPLE G2 EXP=12800 ENCH=128 VIL=128 ORD=16"),
            new QuickTrade("farmer_buy_golden_apple_pie", ProfessionWrapper.FARMER, MerchantLevel.MASTER, "E12x W33 8-10%-16xEMERALD for 1xGOLDEN_APPLE G5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("farmer_buy_lembas_bread", ProfessionWrapper.FARMER, MerchantLevel.MASTER, "E12x W33 8-10%-16xEMERALD for 1xGOLDEN_APPLE G5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("farmer_buy_churro", ProfessionWrapper.FARMER, MerchantLevel.MASTER, "E12x W33 4-10%-8xEMERALD for 1xGOLDEN_APPLE G10 EXP=400 ENCH=4 VIL=4 ORD=16"),

            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.NOVICE, "12x 100% 1-10%-2xEMERALD for 6xBREAD G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.NOVICE, "12x W25 12-10%-24xWHEAT for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.NOVICE, "12x W25 24-10%-48xPOTATO for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.NOVICE, "12x W25 24-10%-48xCARROT for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.NOVICE, "12x W25 12-10%-24xBEETROOT for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.APPRENTICE, "12x 100% 4-10%-8xPUMPKIN for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 4xPUMPKIN_PIE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 8xAPPLE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 1xCAKE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 4xMUSHROOM_STEW G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.JOURNEYMAN, "12x 100% 4-10%-8xMELON for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.JOURNEYMAN, "12x W25 1-10%-2xEMERALD for 12xCOOKIE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.JOURNEYMAN, "12x W25 1-10%-2xEMERALD for 2xMUSHROOM_STEW G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.JOURNEYMAN, "12x W25 1-10%-2xEMERALD for 4xGLOW_BERRIES G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.JOURNEYMAN, "12x W25 1-10%-2xEMERALD for 2xBEETROOT_SOUP G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "12x W3 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "12x W56 4-10%-8xEMERALD for 1xGOLDEN_APPLE G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "12x W7 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "12x W7 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "12x W7 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "12x W7 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "12x W7 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "12x W7 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "12x W7 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "12x W7 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "12x W7 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.MASTER, "12x 100% 1-10%-2xEMERALD for 1xGOLDEN_APPLE G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.MASTER, "12x 100% 16-0%-16xEMERALD_BLOCK for 1xENCHANTED_GOLDEN_APPLE G2 EXP=12800 ENCH=128 VIL=128 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.MASTER, "E12x W33 8-10%-16xEMERALD for 1xGOLDEN_APPLE G5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.MASTER, "E12x W33 8-10%-16xEMERALD for 1xGOLDEN_APPLE G5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("fisherman_", ProfessionWrapper.FISHERMAN, MerchantLevel.MASTER, "E12x W33 4-10%-8xEMERALD for 1xGOLDEN_APPLE G10 EXP=400 ENCH=4 VIL=4 ORD=16")
    };

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        if (!e.getPlayer().getName().equalsIgnoreCase("athlaeos") && !e.getPlayer().getName().equalsIgnoreCase("altlaeos")) return;
        if (e.getMessage().startsWith("runthing"))
            run();
    }

    public static void run(){
        for (ProfessionWrapper profession : ProfessionWrapper.values()){
            String id = String.format("%s_simple", profession.toString().toLowerCase());
            if (CustomMerchantManager.getMerchantType(id) == null)
                CustomMerchantManager.registerMerchantType(new MerchantType(id));
        }

        for (QuickTrade trade : args){
            if (CustomMerchantManager.getTrade(trade.id) != null) continue;
            String id = String.format("%s_simple", trade.profession.toString().toLowerCase());
            MerchantTrade t = parse(trade.id, trade.arg);
            CustomMerchantManager.registerTrade(t);

            MerchantType type = CustomMerchantManager.getMerchantType(id);
            type.addTrade(trade.level, t);
            System.out.println("added trade " + trade.id);
        }

        ValhallaMMO.getInstance().getServer().broadcast("Done!", "Done!");
    }

    private static MerchantTrade parse(String name, String arg){
        String[] args = arg.split(" ");
        boolean exclusive = args[0].startsWith("E");
        int quant = Integer.parseInt(args[0].replace("E", "").replace("x", ""));
        float weight = args[1].equalsIgnoreCase("100%") ? -1 : Float.parseFloat(args[1].replace("W", ""));

        Material priceItem = Material.valueOf(args[2].split("x")[1]);
        String[] priceArgs = args[2].split("x")[0].split("-");
        int priceLowest = Integer.parseInt(priceArgs[0]);
        float priceDemandStep = Float.parseFloat(priceArgs[1].replace("%", "")) / 100;
        int priceHighest = Integer.parseInt(priceArgs[2]);

        int resultQuantity = Integer.parseInt(args[4].split("x")[0]);
        Material resultItem = Material.valueOf(args[4].split("x")[1]);

        float giftWeight = args[5].equalsIgnoreCase("NOGIFT") ? 0 : Float.parseFloat(args[5].replace("G", ""));
        float skillExp = Float.parseFloat(args[6].replace("EXP=", ""));
        float enchantingExp = Float.parseFloat(args[7].replace("ENCH=", ""));
        int merchantExp = Integer.parseInt(args[8].replace("VIL=", ""));

        MerchantTrade trade = new MerchantTrade(name);
        trade.setMaxOrderCount(quant);
        trade.setSkillExp(skillExp);
        trade.setPriceRandomNegativeOffset(-1);
        trade.setPriceRandomPositiveOffset(1);
        trade.setEnchantingExperience(enchantingExp);
        trade.setDemandPriceMax(priceHighest - priceLowest);
        trade.setDemandPriceMultiplier(priceDemandStep);
        trade.setVillagerExperience(merchantExp);
        trade.setRefreshes(true);
        trade.setGiftWeight(giftWeight);
        trade.setScalingCostItem(new ItemStack(priceItem, priceLowest));
        trade.setWeight(weight);
        trade.setResult(new ItemStack(resultItem, resultQuantity));
        trade.setExclusive(exclusive);
        return trade;
    }

    private static class QuickTrade{
        private final String id;
        private final ProfessionWrapper profession;
        private final MerchantLevel level;
        private final String arg;

        private QuickTrade(String id, ProfessionWrapper profession, MerchantLevel level, String arg){
            this.id = id;
            this.profession = profession;
            this.level = level;
            this.arg = arg;
        }
    }
}
