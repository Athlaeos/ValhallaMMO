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
            new QuickTrade("armorer_buy_iron_helmet", ProfessionWrapper.ARMORER, MerchantLevel.JOURNEYMAN, "3x W25 15-25%-30xEMERALD for 1xIRON_HELMET G12.5 EXP=1500 ENCH=15 VIL=15 ORD=8"),
            new QuickTrade("armorer_buy_iron_chestplate", ProfessionWrapper.ARMORER, MerchantLevel.JOURNEYMAN, "3x W25 24-25%-48xEMERALD for 1xIRON_CHESTPLATE G12.5 EXP=2400 ENCH=24 VIL=24 ORD=8"),
            new QuickTrade("armorer_buy_iron_leggings", ProfessionWrapper.ARMORER, MerchantLevel.JOURNEYMAN, "3x W25 21-25%-42xEMERALD for 1xIRON_LEGGINGS G12.5 EXP=21 ENCH=21 VIL=21 ORD=8"),
            new QuickTrade("armorer_buy_iron_boots", ProfessionWrapper.ARMORER, MerchantLevel.JOURNEYMAN, "3x W25 12-25%-24xEMERALD for 1xIRON_BOOTS G12.5 EXP=1200 ENCH=12 VIL=12 ORD=8"),
            new QuickTrade("armorer_sell_diamond", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "12x 100% 1-10%-2xDIAMOND for 1xEMERALD NOGIFT EXP=400 ENCH=4 VIL=4 ORD=0"),
            new QuickTrade("armorer_buy_diamond_helmet", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "2x W25 25-25%-40xEMERALD for 1xDIAMOND_HELMET G5 EXP=2500 ENCH=25 VIL=25 ORD=4"),
            new QuickTrade("armorer_buy_diamond_chestplate", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "2x W25 40-25%-64xEMERALD for 1xDIAMOND_CHESTPLATE G5 EXP=4000 ENCH=40 VIL=40 ORD=4"),
            new QuickTrade("armorer_buy_diamond_leggings", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "2x W25 35-25%-54xEMERALD for 1xDIAMOND_LEGGINGS G5 EXP=3500 ENCH=35 VIL=35 ORD=4"),
            new QuickTrade("armorer_buy_diamond_boots", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "2x W25 20-25%-32xEMERALD for 1xDIAMOND_BOOTS G5 EXP=2000 ENCH=20 VIL=20 ORD=4"),
            new QuickTrade("armorer_buy_netherite_scrap", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "1x 100% 32-25%-64xEMERALD for 1xNETHERITE_SCRAP NOGIFT EXP=3200 ENCH=32 VIL=32 ORD=2"),
            new QuickTrade("armorer_buy_royal_diamond_helmet", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "E1x W25 10-0%-10xEMERALD_BLOCK for 1xDIAMOND_HELMET NOGIFT EXP=10000 ENCH=100 VIL=100 ORD=2"),
            new QuickTrade("armorer_buy_royal_diamond_chestplate", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "E1x W25 16-0%-16xEMERALD_BLOCK for 1xDIAMOND_CHESTPLATE NOGIFT EXP=16000 ENCH=160 VIL=160 ORD=2"),
            new QuickTrade("armorer_buy_royal_diamond_leggings", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "E1x W25 14-0%-14xEMERALD_BLOCK for 1xDIAMOND_LEGGINGS NOGIFT EXP=14000 ENCH=140 VIL=140 ORD=2"),
            new QuickTrade("armorer_buy_royal_diamond_boots", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "E1x W25 8-0%-8xEMERALD_BLOCK for 1xDIAMOND_BOOTS NOGIFT EXP=8000 ENCH=80 VIL=80 ORD=2"),

            new QuickTrade("butcher_buy_rabbit_stew", ProfessionWrapper.BUTCHER, MerchantLevel.NOVICE, "12x 100% 1-10%-2xEMERALD for 1xRABBIT_STEW G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("butcher_sell_raw_rabbit", ProfessionWrapper.BUTCHER, MerchantLevel.NOVICE, "12x W33 3-10%-6xRABBIT for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("butcher_sell_raw_chicken", ProfessionWrapper.BUTCHER, MerchantLevel.NOVICE, "12x W33 4-10%-8xCHICKEN for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("butcher_sell_raw_pork", ProfessionWrapper.BUTCHER, MerchantLevel.NOVICE, "12x W33 6-10%-12xPORKCHOP for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("butcher_sell_coal", ProfessionWrapper.BUTCHER, MerchantLevel.APPRENTICE, "12x 100% 12-10%-24xCOAL for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("butcher_buy_cooked_chicken", ProfessionWrapper.BUTCHER, MerchantLevel.APPRENTICE, "12x W33 1-10%-2xEMERALD for 8xCOOKED_CHICKEN G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("butcher_buy_cooked_pork", ProfessionWrapper.BUTCHER, MerchantLevel.APPRENTICE, "12x W33 1-10%-2xEMERALD for 6xCOOKED_PORKCHOP G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("butcher_buy_cooked_beef", ProfessionWrapper.BUTCHER, MerchantLevel.APPRENTICE, "12x W33 1-10%-2xEMERALD for 6xCOOKED_BEEF G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("butcher_buy_cleaver", ProfessionWrapper.BUTCHER, MerchantLevel.JOURNEYMAN, "2x 100% 8-25%-16xEMERALD for 1xIRON_SWORD G5 EXP=400 ENCH=4 VIL=4 ORD=4"),
            new QuickTrade("butcher_sell_raw_mutton", ProfessionWrapper.BUTCHER, MerchantLevel.JOURNEYMAN, "12x W50 8-10%-16xBEEF for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("butcher_sell_raw_beef", ProfessionWrapper.BUTCHER, MerchantLevel.JOURNEYMAN, "12x W50 6-10%-12xMUTTON for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("butcher_sell_dried_kelp_block", ProfessionWrapper.BUTCHER, MerchantLevel.EXPERT, "12x 100% 6-10%-12xDRIED_KELP_BLOCK for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("butcher_buy_luxurious_stew", ProfessionWrapper.BUTCHER, MerchantLevel.EXPERT, "12x W25 4-25%-8xEMERALD for 1xGOLDEN_APPLE G5 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("butcher_buy_tender_steak", ProfessionWrapper.BUTCHER, MerchantLevel.EXPERT, "12x W25 4-25%-8xEMERALD for 1xGOLDEN_APPLE G5 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("butcher_buy_stuffed_hen", ProfessionWrapper.BUTCHER, MerchantLevel.EXPERT, "12x W25 4-25%-8xEMERALD for 1xGOLDEN_APPLE G5 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("butcher_buy_glazed_ribs", ProfessionWrapper.BUTCHER, MerchantLevel.EXPERT, "12x W25 4-25%-8xEMERALD for 1xGOLDEN_APPLE G5 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("butcher_sell_sweet_berries", ProfessionWrapper.BUTCHER, MerchantLevel.MASTER, "12x 100% 12-10%-24xSWEET_BERRIES for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("butcher_buy_battle_burrito", ProfessionWrapper.BUTCHER, MerchantLevel.MASTER, "E6x W25 8-25%-16xEMERALD for 1xGOLDEN_APPLE NOGIFT EXP=800 ENCH=8 VIL=8 ORD=12"),
            new QuickTrade("butcher_buy_aegis_stroganoff", ProfessionWrapper.BUTCHER, MerchantLevel.MASTER, "E6x W25 8-25%-16xEMERALD for 1xGOLDEN_APPLE NOGIFT EXP=800 ENCH=8 VIL=8 ORD=12"),
            new QuickTrade("butcher_buy_wagyu_steak", ProfessionWrapper.BUTCHER, MerchantLevel.MASTER, "E6x W25 8-25%-16xEMERALD for 1xGOLDEN_APPLE NOGIFT EXP=800 ENCH=8 VIL=8 ORD=12"),
            new QuickTrade("butcher_buy_titanic_goulash", ProfessionWrapper.BUTCHER, MerchantLevel.MASTER, "E6x W25 8-25%-16xEMERALD for 1xGOLDEN_APPLE NOGIFT EXP=800 ENCH=8 VIL=8 ORD=12"),

            new QuickTrade("cleric_sell_rotten_flesh", ProfessionWrapper.CLERIC, MerchantLevel.NOVICE, "12x 100% 32-10%-64xROTTEN_FLESH for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("cleric_buy_redstone_dust", ProfessionWrapper.CLERIC, MerchantLevel.NOVICE, "12x 100% 1-10%-2xEMERALD for 8xREDSTONE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("cleric_buy_brewing_stand", ProfessionWrapper.CLERIC, MerchantLevel.NOVICE, "12x W25 4-10%-8xEMERALD for 1xBREWING_STAND G25 EXP=400 ENCH=4 VIL=4 ORD=8"),
            new QuickTrade("cleric_buy_nether_wart", ProfessionWrapper.CLERIC, MerchantLevel.NOVICE, "12x W25 2-10%-4xEMERALD for 4xNETHER_WART G10 EXP=300 ENCH=3 VIL=3 ORD=4"),
            new QuickTrade("cleric_buy_blaze_powder", ProfessionWrapper.CLERIC, MerchantLevel.NOVICE, "12x W25 4-10%-8xEMERALD for 2xBLAZE_POWDER G10 EXP=400 ENCH=4 VIL=4 ORD=4"),
            new QuickTrade("cleric_buy_glass_bottles", ProfessionWrapper.CLERIC, MerchantLevel.NOVICE, "12x W25 2-10%-4xEMERALD for 6xGLASS_BOTTLE G25 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("cleric_sell_gold_ingots", ProfessionWrapper.CLERIC, MerchantLevel.APPRENTICE, "12x W25 3-10%-6xGOLD_INGOT for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("cleric_buy_lapis_lazuli", ProfessionWrapper.CLERIC, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 4xLAPIS_LAZULI G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("cleric_sell_spider_eyes", ProfessionWrapper.CLERIC, MerchantLevel.APPRENTICE, "12x W25 8-10%-16xSPIDER_EYE for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("cleric_buy_fermented_spider_eye", ProfessionWrapper.CLERIC, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 1xFERMENTED_SPIDER_EYE G25 EXP=200 ENCH=2 VIL=16 ORD=16"),
            new QuickTrade("cleric_sell_rabbit_feet", ProfessionWrapper.CLERIC, MerchantLevel.JOURNEYMAN, "12x W25 2-10%-4xRABBIT_FOOT for 1xEMERALD NOGIFT EXP=400 ENCH=4 VIL=4 ORD=0"),
            new QuickTrade("cleric_buy_glowstone_dust", ProfessionWrapper.CLERIC, MerchantLevel.JOURNEYMAN, "12x W25 2-10%-4xEMERALD for 8xGLOWSTONE_DUST G25 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("cleric_buy_glistering_melon_slice", ProfessionWrapper.CLERIC, MerchantLevel.JOURNEYMAN, "12x W25 1-10%-2xEMERALD for 2xGLISTERING_MELON_SLICE NOGIFT EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("cleric_buy_golden_carrots", ProfessionWrapper.CLERIC, MerchantLevel.JOURNEYMAN, "12x W25 1-10%-2xEMERALD for 2xGOLDEN_CARROT G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("cleric_sell_glass_bottles", ProfessionWrapper.CLERIC, MerchantLevel.EXPERT, "12x 100% 6-10%-12xGLASS_BOTTLE for 1xEMERALD NOGIFT EXP=400 ENCH=4 VIL=4 ORD=0"),
            new QuickTrade("cleric_buy_ender_pearl", ProfessionWrapper.CLERIC, MerchantLevel.EXPERT, "12x 100% 6-10%-12xEMERALD for 1xENDER_PEARL G15 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("cleric_sell_turtle_scute", ProfessionWrapper.CLERIC, MerchantLevel.EXPERT, "12x W25 2-10%-4xSCUTE for 1xEMERALD NOGIFT EXP=400 ENCH=4 VIL=4 ORD=0"),
            new QuickTrade("cleric_buy_ghast_tear", ProfessionWrapper.CLERIC, MerchantLevel.EXPERT, "12x W25 4-10%-8xEMERALD for 1xGHAST_TEAR G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("cleric_buy_pollen_stalks", ProfessionWrapper.CLERIC, MerchantLevel.EXPERT, "12x W25 8-10%-16xEMERALD for 1xWHEAT G5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("cleric_sell_nether_wart", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "12x 100% 24-10%-48xNETHER_WART for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("cleric_buy_experience_bottles", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "12x 100% 1-10%-2xEMERALD for 4xEXPERIENCE_BOTTLE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("cleric_buy_bustling_fungus", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "E12x W15 4-10%-8xEMERALD for 1xWHEAT G5 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("cleric_buy_steelblossom", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "E12x W15 8-10%-16xEMERALD for 1xWHEAT G5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("cleric_buy_shimmerleaf", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "E12x W15 8-10%-16xEMERALD for 1xWHEAT G5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("cleric_buy_noxious_spores", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "E12x W15 4-10%-8xEMERALD for 1xWHEAT G5 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("cleric_buy_knightreed", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "E12x W15 4-10%-8xEMERALD for 1xWHEAT G5 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("cleric_buy_tutor_lotus", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "E12x W15 32-10%-64xEMERALD for 1xWHEAT G5 EXP=3200 ENCH=32 VIL=32 ORD=16"),

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
            new QuickTrade("farmer_buy_enchanted_golden_apple", ProfessionWrapper.FARMER, MerchantLevel.MASTER, "12x 100% 16-0%-16xEMERALD_BLOCK for 1xENCHANTED_GOLDEN_APPLE G2 EXP=16000 ENCH=160 VIL=160 ORD=16"),
            new QuickTrade("farmer_buy_golden_apple_pie", ProfessionWrapper.FARMER, MerchantLevel.MASTER, "E12x W33 8-10%-16xEMERALD for 1xGOLDEN_APPLE G5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("farmer_buy_lembas_bread", ProfessionWrapper.FARMER, MerchantLevel.MASTER, "E12x W33 8-10%-16xEMERALD for 1xGOLDEN_APPLE G5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("farmer_buy_churro", ProfessionWrapper.FARMER, MerchantLevel.MASTER, "E12x W33 4-10%-8xEMERALD for 1xGOLDEN_APPLE G10 EXP=400 ENCH=4 VIL=4 ORD=16"),

            new QuickTrade("fisherman_sell_string", ProfessionWrapper.FISHERMAN, MerchantLevel.NOVICE, "12x 100% 16-10%-32xSTRING for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("fisherman_sell_coal", ProfessionWrapper.FISHERMAN, MerchantLevel.NOVICE, "12x 100% 12-10%-24xCOAL for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("fisherman_buy_cod_bucket", ProfessionWrapper.FISHERMAN, MerchantLevel.NOVICE, "12x W50 2-10%-4xEMERALD for 1xCOD_BUCKET G25 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("fisherman_buy_cooked_cod", ProfessionWrapper.FISHERMAN, MerchantLevel.NOVICE, "12x W50 1-10%-2xEMERALD for 6xCOOKED_COD G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_sell_cod", ProfessionWrapper.FISHERMAN, MerchantLevel.APPRENTICE, "12x 100% 12-10%-24xCOD for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fisherman_sell_salmon", ProfessionWrapper.FISHERMAN, MerchantLevel.APPRENTICE, "12x 100% 12-10%-24xSALMON for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fisherman_buy_tropical_fish_bucket", ProfessionWrapper.FISHERMAN, MerchantLevel.APPRENTICE, "12x W25 4-10%-8xEMERALD for 1xTROPICAL_FISH_BUCKET G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("fisherman_buy_salmon_bucket", ProfessionWrapper.FISHERMAN, MerchantLevel.APPRENTICE, "12x W25 2-10%-4xEMERALD for 1xSALMON_BUCKET G15 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("fisherman_buy_cooked_salmon", ProfessionWrapper.FISHERMAN, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 6xCOOKED_SALMON G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_buy_pufferfish_bucket", ProfessionWrapper.FISHERMAN, MerchantLevel.APPRENTICE, "12x W25 4-10%-8xEMERALD for 1xPUFFERFISH_BUCKET G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("fisherman_sell_tropical_fish", ProfessionWrapper.FISHERMAN, MerchantLevel.JOURNEYMAN, "12x 100% 4-10%-8xTROPICAL_FISH for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fisherman_sell_pufferfish", ProfessionWrapper.FISHERMAN, MerchantLevel.JOURNEYMAN, "12x 100% 6-10%-12xPUFFERFISH for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fisherman_sell_boat", ProfessionWrapper.FISHERMAN, MerchantLevel.JOURNEYMAN, "12x W25 1-0%-1xOAK_BOAT for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("fisherman_sell_ink_sacs", ProfessionWrapper.FISHERMAN, MerchantLevel.JOURNEYMAN, "12x W25 8-10%-16xINK_SAC for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fisherman_buy_fishing_rod_t1", ProfessionWrapper.FISHERMAN, MerchantLevel.JOURNEYMAN, "3x W25 4-25%-8xEMERALD for 1xFISHING_ROD G10 EXP=400 ENCH=4 VIL=4 ORD=8"),
            new QuickTrade("fisherman_buy_turtle_scute", ProfessionWrapper.FISHERMAN, MerchantLevel.JOURNEYMAN, "12x W25 4-10%-8xEMERALD for 1xSCUTE G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("fisherman_buy_prismarine_crystals", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "12x 100% 1-10%-2xEMERALD for 8xPRISMARINE_CRYSTALS G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_buy_prismarine_shards", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "12x 100% 1-10%-2xEMERALD for 32xPRISMARINE_SHARD G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_buy_nautilus_shell", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "12x W25 8-10%-16xEMERALD for 1xNAUTILUS_SHELL G10 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("fisherman_buy_bait", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "12x W25 1-10%-2xEMERALD for 6xORANGE_DYE G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_buy_fishing_rod_t2", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "3x W25 16-10%-32xEMERALD for 1xFISHING_ROD G10 EXP=1600 ENCH=16 VIL=16 ORD=8"),
            new QuickTrade("fisherman_buy_cooked_lionfish", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "12x W25 8-10%-16xEMERALD for 1xGOLDEN_APPLE G10 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("fisherman_buy_heart_of_the_sea", ProfessionWrapper.FISHERMAN, MerchantLevel.MASTER, "1x 100% 64-0%-64xEMERALD for 1xHEART_OF_THE_SEA G5 EXP=6400 ENCH=64 VIL=64 ORD=3"),
            new QuickTrade("fisherman_buy_caviar", ProfessionWrapper.FISHERMAN, MerchantLevel.MASTER, "E12x W25 8-10%-16xEMERALD for 1xGOLDEN_APPLE G15 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("fisherman_buy_salmon_en_croute", ProfessionWrapper.FISHERMAN, MerchantLevel.MASTER, "E12x W25 4-10%-8xEMERALD_BLOCK for 1xGOLDEN_APPLE G2 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("fisherman_buy_poached_lobster", ProfessionWrapper.FISHERMAN, MerchantLevel.MASTER, "E12x W25 4-10%-8xEMERALD for 1xGOLDEN_APPLE G5 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("fisherman_buy_uni_sushi", ProfessionWrapper.FISHERMAN, MerchantLevel.MASTER, "E12x W25 8-10%-16xEMERALD for 1xGOLDEN_APPLE G10 EXP=800 ENCH=8 VIL=8 ORD=16"),

            new QuickTrade("fletcher_sell_sticks", ProfessionWrapper.FLETCHER, MerchantLevel.NOVICE, "12x 100% 32-10%-64xSTICK for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("fletcher_buy_flint_arrows", ProfessionWrapper.FLETCHER, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 16xARROW NOGIFT EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fletcher_buy_flint", ProfessionWrapper.FLETCHER, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 8xFLINT G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fletcher_buy_feathers", ProfessionWrapper.FLETCHER, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 8xFEATHER G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fletcher_buy_string", ProfessionWrapper.FLETCHER, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fletcher_sell_flint", ProfessionWrapper.FLETCHER, MerchantLevel.APPRENTICE, "12x 100% 16-10%-32xFLINT for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fletcher_buy_bow_t1", ProfessionWrapper.FLETCHER, MerchantLevel.APPRENTICE, "12x W25 2-10%-4xEMERALD for 1xBOW G15 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("fletcher_buy_copper_arrows", ProfessionWrapper.FLETCHER, MerchantLevel.APPRENTICE, "12x W25 2-10%-4xEMERALD for 1xARROW G15 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("fletcher_buy_crossbow_t1", ProfessionWrapper.FLETCHER, MerchantLevel.APPRENTICE, "12x W25 4-10%-8xEMERALD for 1xCROSSBOW G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("fletcher_sell_string", ProfessionWrapper.FLETCHER, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fletcher_sell_feathers", ProfessionWrapper.FLETCHER, MerchantLevel.JOURNEYMAN, "12x 100% 16-10%-32xFEATHER for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fletcher_buy_bow_t2", ProfessionWrapper.FLETCHER, MerchantLevel.JOURNEYMAN, "12x W25 6-10%-12xEMERALD for 1xBOW G10 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("fletcher_buy_crossbow_t2", ProfessionWrapper.FLETCHER, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xEMERALD for 1xCROSSBOW G10 EXP=1200 ENCH=12 VIL=12 ORD=16"),
            new QuickTrade("fletcher_buy_golden_arrows", ProfessionWrapper.FLETCHER, MerchantLevel.JOURNEYMAN, "12x W25 8-10%-16xEMERALD for 16xARROW G10 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("fletcher_sell_tripwire_hooks", ProfessionWrapper.FLETCHER, MerchantLevel.JOURNEYMAN, "3x W25 8-25%-16xTRIPWIRE_HOOK for 1xEMERALD NOGIFT EXP=400 ENCH=4 VIL=4 ORD=0"),
            new QuickTrade("fletcher_sell_targets", ProfessionWrapper.FLETCHER, MerchantLevel.EXPERT, "12x 100% 4-10%-8xTARGET for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fletcher_buy_iron_arrows", ProfessionWrapper.FLETCHER, MerchantLevel.EXPERT, "12x W25 8-10%-16xEMERALD for 16xARROW G7.5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("fletcher_buy_bow_t3", ProfessionWrapper.FLETCHER, MerchantLevel.EXPERT, "12x W25 24-10%-48xEMERALD for 1xBOW G7.5 EXP=2400 ENCH=24 VIL=24 ORD=16"),
            new QuickTrade("fletcher_buy_crossbow_t3", ProfessionWrapper.FLETCHER, MerchantLevel.EXPERT, "12x W25 36-10%-64xEMERALD for 1xCROSSBOW G7.5 EXP=3600 ENCH=36 VIL=36 ORD=16"),
            new QuickTrade("fletcher_buy_tipped_arrows_wither", ProfessionWrapper.FLETCHER, MerchantLevel.EXPERT, "12x W5 6-10%-12xEMERALD for 4xTIPPED_ARROW G3 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("fletcher_buy_tipped_arrows_poison", ProfessionWrapper.FLETCHER, MerchantLevel.EXPERT, "12x W5 6-10%-12xEMERALD for 4xTIPPED_ARROW G3 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("fletcher_buy_tipped_arrows_instant_damage", ProfessionWrapper.FLETCHER, MerchantLevel.EXPERT, "12x W5 6-10%-12xEMERALD for 4xTIPPED_ARROW G3 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("fletcher_buy_tipped_arrows_radiant_damage", ProfessionWrapper.FLETCHER, MerchantLevel.EXPERT, "12x W5 6-10%-12xEMERALD for 4xTIPPED_ARROW G3 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("fletcher_buy_tipped_arrows_antiheal", ProfessionWrapper.FLETCHER, MerchantLevel.EXPERT, "12x W5 6-10%-12xEMERALD for 4xTIPPED_ARROW G3 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("fletcher_buy_ender_arrow", ProfessionWrapper.FLETCHER, MerchantLevel.MASTER, "4x 100% 8-10%-16xEMERALD for 1xARROW G5 EXP=800 ENCH=8 VIL=8 ORD=8"),
            new QuickTrade("fletcher_buy_royal_bow", ProfessionWrapper.FLETCHER, MerchantLevel.MASTER, "E12x W20 32-10%-64xEMERALD for 1xBOW G5 EXP=3200 ENCH=32 VIL=32 ORD=16"),
            new QuickTrade("fletcher_buy_royal_crossbow", ProfessionWrapper.FLETCHER, MerchantLevel.MASTER, "E12x W20 32-10%-64xEMERALD for 1xCROSSBOW G5 EXP=3200 ENCH=32 VIL=32 ORD=16"),
            new QuickTrade("fletcher_buy_incendiary_arrows", ProfessionWrapper.FLETCHER, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 1xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),
            new QuickTrade("fletcher_buy_explosive_arrows", ProfessionWrapper.FLETCHER, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 2xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),
            new QuickTrade("fletcher_buy_unholy_arrows", ProfessionWrapper.FLETCHER, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 4xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),

            new QuickTrade("leatherworker_sell_leather", ProfessionWrapper.LEATHERWORKER, MerchantLevel.NOVICE, "12x 100% 32-10%-64xSTICK for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("leatherworker_buy_leather_helmet_t1", ProfessionWrapper.LEATHERWORKER, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 16xARROW NOGIFT EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("leatherworker_buy_leather_chestplate_t1", ProfessionWrapper.LEATHERWORKER, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 8xFLINT G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("leatherworker_buy_leather_leggings_t1", ProfessionWrapper.LEATHERWORKER, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 8xFEATHER G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("leatherworker_buy_leather_boots_t1", ProfessionWrapper.LEATHERWORKER, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("leatherworker_sell_flint", ProfessionWrapper.LEATHERWORKER, MerchantLevel.APPRENTICE, "12x 100% 16-10%-32xFLINT for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("leatherworker_buy_leather_helmet_t2", ProfessionWrapper.LEATHERWORKER, MerchantLevel.APPRENTICE, "12x W25 2-10%-4xEMERALD for 1xBOW G15 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("leatherworker_buy_leather_chestplate_t2", ProfessionWrapper.LEATHERWORKER, MerchantLevel.APPRENTICE, "12x W25 2-10%-4xEMERALD for 1xARROW G15 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("leatherworker_buy_leather_leggings_t2", ProfessionWrapper.LEATHERWORKER, MerchantLevel.APPRENTICE, "12x W25 4-10%-8xEMERALD for 1xCROSSBOW G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("leatherworker_buy_leather_boots_t2", ProfessionWrapper.LEATHERWORKER, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("leatherworker_sell_rabbit_hide", ProfessionWrapper.LEATHERWORKER, MerchantLevel.JOURNEYMAN, "12x 100% 16-10%-32xFEATHER for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("leatherworker_buy_saddle", ProfessionWrapper.LEATHERWORKER, MerchantLevel.JOURNEYMAN, "12x W25 6-10%-12xEMERALD for 1xBOW G10 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("leatherworker_buy_leather_horse_armor", ProfessionWrapper.LEATHERWORKER, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xEMERALD for 1xCROSSBOW G10 EXP=1200 ENCH=12 VIL=12 ORD=16"),
            new QuickTrade("leatherworker_buy_iron_horse_armor", ProfessionWrapper.LEATHERWORKER, MerchantLevel.JOURNEYMAN, "12x W25 8-10%-16xEMERALD for 16xARROW G10 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("leatherworker_buy_golden_horse_armor", ProfessionWrapper.LEATHERWORKER, MerchantLevel.JOURNEYMAN, "3x W25 8-25%-16xTRIPWIRE_HOOK for 1xEMERALD NOGIFT EXP=400 ENCH=4 VIL=4 ORD=0"),
            new QuickTrade("leatherworker_sell_turtle_scute", ProfessionWrapper.LEATHERWORKER, MerchantLevel.EXPERT, "12x 100% 4-10%-8xTARGET for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("leatherworker_buy_bundle", ProfessionWrapper.LEATHERWORKER, MerchantLevel.EXPERT, "12x W25 8-10%-16xEMERALD for 16xARROW G7.5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("leatherworker_buy_turtle_helmet", ProfessionWrapper.LEATHERWORKER, MerchantLevel.EXPERT, "12x W25 24-10%-48xEMERALD for 1xBOW G7.5 EXP=2400 ENCH=24 VIL=24 ORD=16"),
            new QuickTrade("leatherworker_buy_turtle_chestplate", ProfessionWrapper.LEATHERWORKER, MerchantLevel.EXPERT, "12x W25 36-10%-64xEMERALD for 1xCROSSBOW G7.5 EXP=3600 ENCH=36 VIL=36 ORD=16"),
            new QuickTrade("leatherworker_buy_turtle_leggings", ProfessionWrapper.LEATHERWORKER, MerchantLevel.EXPERT, "4x 100% 8-10%-16xEMERALD for 1xARROW G5 EXP=800 ENCH=8 VIL=8 ORD=8"),
            new QuickTrade("leatherworker_buy_turtle_boots", ProfessionWrapper.LEATHERWORKER, MerchantLevel.EXPERT, "E12x W20 32-10%-64xEMERALD for 1xBOW G5 EXP=3200 ENCH=32 VIL=32 ORD=16"),
            new QuickTrade("leatherworker_buy_diamond_horse_armor", ProfessionWrapper.LEATHERWORKER, MerchantLevel.MASTER, "12x W25 24-10%-48xEMERALD for 1xBOW G7.5 EXP=2400 ENCH=24 VIL=24 ORD=16"),
            new QuickTrade("leatherworker_buy_assassin_helmet", ProfessionWrapper.LEATHERWORKER, MerchantLevel.MASTER, "E12x W20 32-10%-64xEMERALD for 1xCROSSBOW G5 EXP=3200 ENCH=32 VIL=32 ORD=16"),
            new QuickTrade("leatherworker_buy_assassin_chestplate", ProfessionWrapper.LEATHERWORKER, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 1xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),
            new QuickTrade("leatherworker_buy_assassin_leggings", ProfessionWrapper.LEATHERWORKER, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 2xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),
            new QuickTrade("leatherworker_buy_assassin_boots", ProfessionWrapper.LEATHERWORKER, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 4xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),

            new QuickTrade("librarian_sell_paper", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "12x 100% 32-10%-64xSTICK for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("librarian_buy_bookshelf", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 16xARROW NOGIFT EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("librarian_buy_random_enchanted_book_t1", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 8xFLINT G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("librarian_buy_efficiency_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 8xFEATHER G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("librarian_buy_sharpness_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("librarian_buy_power_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("librarian_buy_smite_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("librarian_buy_bane_of_arthropods_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("librarian_buy_projectile_protection_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("librarian_buy_blast_protection_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("librarian_buy_fire_protection_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("librarian_sell_books", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x 100% 16-10%-32xFLINT for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_buy_lantern", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x W25 2-10%-4xEMERALD for 1xBOW G15 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("librarian_buy_piercing_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("librarian_buy_impaling_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("librarian_buy_protection_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("librarian_buy_fire_aspect_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_buy_knockback_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_buy_punch_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_buy_quick_charge_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_buy_lure_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_sell_ink_sacs", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "12x 100% 16-10%-32xFEATHER for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_buy_glass", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "12x W25 6-10%-12xEMERALD for 1xBOW G10 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("librarian_buy_random_enchanted_book_t2", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "12x W25 6-10%-12xEMERALD for 1xBOW G10 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("librarian_buy_aqua_affinity_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "12x W25 4-10%-8xEMERALD for 1xCROSSBOW G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("librarian_buy_feather_falling_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_buy_unbreaking_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("librarian_buy_sweeping_edge_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_buy_respiration_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_buy_depth_strider_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_buy_multishot_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_sell_book_and_quill", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "12x 100% 4-10%-8xTARGET for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_buy_compass", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "12x W25 8-10%-16xEMERALD for 16xARROW G7.5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("librarian_buy_clock", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "12x W25 8-10%-16xEMERALD for 16xARROW G7.5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("librarian_buy_fortune_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "12x W25 2-10%-4xEMERALD for 1xARROW G15 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("librarian_buy_looting_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_buy_luck_of_the_sea_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_buy_frost_walker_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "12x W25 24-10%-48xEMERALD for 1xBOW G7.5 EXP=2400 ENCH=24 VIL=24 ORD=16"),
            new QuickTrade("librarian_buy_channeling_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "12x W25 36-10%-64xEMERALD for 1xCROSSBOW G7.5 EXP=3600 ENCH=36 VIL=36 ORD=16"),
            new QuickTrade("librarian_buy_loyalty_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "4x 100% 8-10%-16xEMERALD for 1xARROW G5 EXP=800 ENCH=8 VIL=8 ORD=8"),
            new QuickTrade("librarian_buy_riptide_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "E12x W20 32-10%-64xEMERALD for 1xBOW G5 EXP=3200 ENCH=32 VIL=32 ORD=16"),
            new QuickTrade("librarian_buy_name_tag", ProfessionWrapper.LIBRARIAN, MerchantLevel.MASTER, "12x W25 24-10%-48xEMERALD for 1xBOW G7.5 EXP=2400 ENCH=24 VIL=24 ORD=16"),
            new QuickTrade("librarian_buy_random_enchanted_book_t3", ProfessionWrapper.LIBRARIAN, MerchantLevel.MASTER, "E12x W20 32-10%-64xEMERALD for 1xCROSSBOW G5 EXP=3200 ENCH=32 VIL=32 ORD=16"),
            new QuickTrade("librarian_buy_infinity_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.MASTER, "E12x W20 32-10%-64xEMERALD for 1xCROSSBOW G5 EXP=3200 ENCH=32 VIL=32 ORD=16"),
            new QuickTrade("librarian_buy_mending_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 1xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),
            new QuickTrade("librarian_buy_silk_touch_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 2xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),
            new QuickTrade("librarian_buy_thorns_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 4xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),
            // TODO create custom enchantments and add to enchanter exclusive trades
            // custom enchantments
            // Dreamgate (levelled) replaces parry. sneak right click (hold) sword into ground surface to create waypoint.
            // requires block placement permissions. sneak right click (hold) sword into sky (looking up) to teleport to waypoint.
            // cost scales with distance. enchantment level increases max distance. location is saved on sword

            // Resonance (levelled) replaces vein miner. right clicking pickaxe onto rock sends a resonating wave, briefly lighting up
            // all ores around. radius scales with level. consumes durability

            // Mjolnir (levelled) replaces overhead slam. right clicking right after jumping lunges player forward.
            // while in the air after lunging forward, hitting an entity deals a lot more damage in a larger aoe
            // lunge range increases with levels, radius and damage increase with height (up to limit)

            // Knocking (levelled) replaces tree capitator. shift mining a log block shakes the tree heavily, scanning the
            // tree for leaf blocks and generating drops for each leaf block that would be broken (as well as custom loot)
            // the player's loot luck increases with level. the drops are dropped down from the leaves but leave the tree and
            // its leaves intact. this cannot be repeated on the same tree for some time

            // Homing (levelled) replaces charged shot. firing charged shots do not benefit from charged shot stats.
            // charged shots fly much slower, are not affected by gravity, but home in on the nearest target to
            // the crosshair (within fov). right before hitting, the arrow's velocity is restored to its previous velocity.
            // homing strength increases with level

            // Philosopher's Stone (levelled) replaces divine harvest, requires transmutation. Divine harvest still harvests
            // all crops in a large field, but all the drops are converted to other items.
            // beetroots are converted to diamonds. wheat to emeralds. potatoes to iron. carrots to gold. nether warts to coal. cocoa to copper
            // conversion isn't 1:1 and depends on material

            // Aspect of Kali (levelled) replaces rage. Taking damage that would have killed you starts a timer, during
            // you are invulnerable and deal increased damage. your health bar now represents this timer, starting at full
            // but ticking down. you cannot heal during this period. you also cannot lose exhaustion during this time.
            // if you kill the entity that dealt the finishing blow to you during this time, the timer stops and you
            // remain at the health the timer set you at. if you fail to kill the killer, you simply run out of health
            // and die anyway. this utilizes a resurrection event. Duration and damage increase with levels.
            // a message should be sent telling you to kill your attacker

            // Aspect of Hermes (levelled) replaces adrenaline. Taking damage that would have killed you starts a timer.
            // you are teleported a distance away from the attacker, cleansed from all debuffs, bleeds, fire, and are granted
            // a speed boost. you stay at minimal health during this time and cannot heal. taking a single hit of damage
            // from the attacker kills you, but nothing else can harm you. if you take no damage from the attacker during
            // by the end of the timer, you heal to some percentage of your hp. teleport distance and speed increase with levels, duration decreases

            // Divine Judgement (levelled) replaces disarming. fully disarming the opponent instead equalizes the hp of the grappler
            // and the opponent. the grappler is then granted additional attack damage equal to some fraction of the difference in HP for some time
            // levels increase the damage dealt

            new QuickTrade("mason_sell_clay_ball", ProfessionWrapper.MASON, MerchantLevel.NOVICE, "12x 100% 32-10%-64xSTICK for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("mason_buy_stone", ProfessionWrapper.MASON, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 16xARROW NOGIFT EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_smooth_stone", ProfessionWrapper.MASON, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 8xFLINT G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_cobblestone", ProfessionWrapper.MASON, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 8xFEATHER G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_mossy_cobblestone", ProfessionWrapper.MASON, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_mossy_stone_bricks", ProfessionWrapper.MASON, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_bricks", ProfessionWrapper.MASON, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_mud", ProfessionWrapper.MASON, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_sell_stone", ProfessionWrapper.MASON, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_buy_deepslate", ProfessionWrapper.MASON, MerchantLevel.APPRENTICE, "12x 100% 16-10%-32xFLINT for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_buy_terracotta", ProfessionWrapper.MASON, MerchantLevel.APPRENTICE, "12x W25 2-10%-4xEMERALD for 1xBOW G15 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("mason_buy_andesite", ProfessionWrapper.MASON, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_granite", ProfessionWrapper.MASON, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_diorite", ProfessionWrapper.MASON, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_sandstone", ProfessionWrapper.MASON, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_buy_tuff", ProfessionWrapper.MASON, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_sell_granite", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_sell_andesite", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_sell_diorite", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "12x 100% 16-10%-32xFEATHER for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_buy_basalt", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "12x W25 6-10%-12xEMERALD for 1xBOW G10 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("mason_buy_calcite", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "12x W25 6-10%-12xEMERALD for 1xBOW G10 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("mason_buy_glass", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "12x W25 4-10%-8xEMERALD for 1xCROSSBOW G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("mason_buy_dripstone", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_buy_blackstone", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_red_sandstone", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_buy_nether_bricks", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_sell_nether_quartz", ProfessionWrapper.MASON, MerchantLevel.EXPERT, "12x 100% 4-10%-8xTARGET for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_buy_quartz_block", ProfessionWrapper.MASON, MerchantLevel.EXPERT, "12x W25 8-10%-16xEMERALD for 16xARROW G7.5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("mason_buy_tinted_glass", ProfessionWrapper.MASON, MerchantLevel.EXPERT, "12x W25 8-10%-16xEMERALD for 16xARROW G7.5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("mason_buy_amethyst_block", ProfessionWrapper.MASON, MerchantLevel.EXPERT, "12x W25 2-10%-4xEMERALD for 1xARROW G15 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("mason_buy_prismarine_blocks", ProfessionWrapper.MASON, MerchantLevel.EXPERT, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_buy_dark_prismarine_blocks", ProfessionWrapper.MASON, MerchantLevel.EXPERT, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_buy_prismarine_lanterns", ProfessionWrapper.MASON, MerchantLevel.EXPERT, "12x W25 24-10%-48xEMERALD for 1xBOW G7.5 EXP=2400 ENCH=24 VIL=24 ORD=16"),
            new QuickTrade("mason_buy_glowstone", ProfessionWrapper.MASON, MerchantLevel.EXPERT, "12x W25 36-10%-64xEMERALD for 1xCROSSBOW G7.5 EXP=3600 ENCH=36 VIL=36 ORD=16"),
            new QuickTrade("mason_buy_end_stone", ProfessionWrapper.MASON, MerchantLevel.MASTER, "12x W25 24-10%-48xEMERALD for 1xBOW G7.5 EXP=2400 ENCH=24 VIL=24 ORD=16"),
            new QuickTrade("mason_buy_purpur", ProfessionWrapper.MASON, MerchantLevel.MASTER, "E12x W20 32-10%-64xEMERALD for 1xCROSSBOW G5 EXP=3200 ENCH=32 VIL=32 ORD=16"),
            new QuickTrade("mason_buy_obsidian", ProfessionWrapper.MASON, MerchantLevel.MASTER, "E12x W20 32-10%-64xEMERALD for 1xCROSSBOW G5 EXP=3200 ENCH=32 VIL=32 ORD=16"),
            new QuickTrade("mason_buy_light", ProfessionWrapper.MASON, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 1xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),
            new QuickTrade("mason_buy_reinforced_deepslate", ProfessionWrapper.MASON, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 2xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),

            // TODO light placement and breaking mechanics


            new QuickTrade("shepherd_buy_shears", ProfessionWrapper.SHEPHERD, MerchantLevel.NOVICE, "12x 100% 32-10%-64xSTICK for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("shepherd_sell_white_wool", ProfessionWrapper.SHEPHERD, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 16xARROW NOGIFT EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("shepherd_sell_brown_wool", ProfessionWrapper.SHEPHERD, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 8xFLINT G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("shepherd_sell_black_wool", ProfessionWrapper.SHEPHERD, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 8xFEATHER G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("shepherd_sell_gray_wool", ProfessionWrapper.SHEPHERD, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("shepherd_sell_random_dye_common", ProfessionWrapper.SHEPHERD, MerchantLevel.APPRENTICE, "12x 100% 16-10%-32xFLINT for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("shepherd_buy_random_wool_1", ProfessionWrapper.SHEPHERD, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("shepherd_buy_random_wool_2", ProfessionWrapper.SHEPHERD, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("shepherd_buy_random_wool_3", ProfessionWrapper.SHEPHERD, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("shepherd_buy_random_wool_4", ProfessionWrapper.SHEPHERD, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("shepherd_sell_random_dye_common", ProfessionWrapper.SHEPHERD, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("shepherd_buy_random_dye_common_1", ProfessionWrapper.SHEPHERD, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("shepherd_buy_random_dye_common_2", ProfessionWrapper.SHEPHERD, MerchantLevel.JOURNEYMAN, "12x W25 6-10%-12xEMERALD for 1xBOW G10 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("shepherd_buy_random_dye_uncommon_1", ProfessionWrapper.SHEPHERD, MerchantLevel.JOURNEYMAN, "12x 100% 16-10%-32xFEATHER for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("shepherd_buy_random_dye_uncommon_2", ProfessionWrapper.SHEPHERD, MerchantLevel.JOURNEYMAN, "12x W25 6-10%-12xEMERALD for 1xBOW G10 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("shepherd_sell_random_dye_uncommon", ProfessionWrapper.SHEPHERD, MerchantLevel.EXPERT, "12x W25 4-10%-8xEMERALD for 1xCROSSBOW G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("shepherd_buy_field_masoned_pattern", ProfessionWrapper.SHEPHERD, MerchantLevel.EXPERT, "12x 100% 4-10%-8xTARGET for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("shepherd_buy_bordure_indented_pattern", ProfessionWrapper.SHEPHERD, MerchantLevel.EXPERT, "12x W25 8-10%-16xEMERALD for 16xARROW G7.5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("shepherd_buy_flower_charge_pattern", ProfessionWrapper.SHEPHERD, MerchantLevel.EXPERT, "12x W25 8-10%-16xEMERALD for 16xARROW G7.5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("shepherd_buy_creeper_charge_pattern", ProfessionWrapper.SHEPHERD, MerchantLevel.MASTER, "12x W25 24-10%-48xEMERALD for 1xBOW G7.5 EXP=2400 ENCH=24 VIL=24 ORD=16"),
            new QuickTrade("shepherd_buy_skull_charge_pattern", ProfessionWrapper.SHEPHERD, MerchantLevel.MASTER, "E12x W20 32-10%-64xEMERALD for 1xCROSSBOW G5 EXP=3200 ENCH=32 VIL=32 ORD=16"),
            new QuickTrade("shepherd_buy_mojang_pattern", ProfessionWrapper.SHEPHERD, MerchantLevel.MASTER, "E12x W20 32-10%-64xEMERALD for 1xCROSSBOW G5 EXP=3200 ENCH=32 VIL=32 ORD=16"),
            new QuickTrade("shepherd_buy_snout_pattern", ProfessionWrapper.SHEPHERD, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 1xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),
            new QuickTrade("shepherd_buy_flow_pattern", ProfessionWrapper.SHEPHERD, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 2xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),
            new QuickTrade("shepherd_buy_guster_pattern", ProfessionWrapper.SHEPHERD, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 2xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),
            new QuickTrade("shepherd_buy_globe_pattern", ProfessionWrapper.SHEPHERD, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 2xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),

            new QuickTrade("toolsmith_sell_coal", ProfessionWrapper.TOOLSMITH, MerchantLevel.NOVICE, "12x 100% 32-10%-64xSTICK for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 16xARROW NOGIFT EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 8xFLINT G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 8xFEATHER G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.APPRENTICE, "12x 100% 16-10%-32xFLINT for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.JOURNEYMAN, "12x W25 6-10%-12xEMERALD for 1xBOW G10 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.JOURNEYMAN, "12x 100% 16-10%-32xFEATHER for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.JOURNEYMAN, "12x W25 6-10%-12xEMERALD for 1xBOW G10 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.EXPERT, "12x W25 4-10%-8xEMERALD for 1xCROSSBOW G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.EXPERT, "12x 100% 4-10%-8xTARGET for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.EXPERT, "12x W25 8-10%-16xEMERALD for 16xARROW G7.5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.EXPERT, "12x W25 8-10%-16xEMERALD for 16xARROW G7.5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.MASTER, "12x W25 24-10%-48xEMERALD for 1xBOW G7.5 EXP=2400 ENCH=24 VIL=24 ORD=16"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.MASTER, "E12x W20 32-10%-64xEMERALD for 1xCROSSBOW G5 EXP=3200 ENCH=32 VIL=32 ORD=16"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.MASTER, "E12x W20 32-10%-64xEMERALD for 1xCROSSBOW G5 EXP=3200 ENCH=32 VIL=32 ORD=16"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 1xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 2xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 2xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),
            new QuickTrade("toolsmith_", ProfessionWrapper.TOOLSMITH, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 2xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16")
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
