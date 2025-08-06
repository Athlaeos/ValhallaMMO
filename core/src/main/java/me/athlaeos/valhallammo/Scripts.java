package me.athlaeos.valhallammo;

import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierPriority;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc.ItemReplaceByIndexed;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc.ItemReplaceByIndexedBasedOnQuality;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc.SmithingNeutralQualitySet;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc.SmithingQualityScale;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats.DefaultAttributeScale;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats.DurabilityScale;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.rewards.SkillExperience;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicGridRecipe;
import me.athlaeos.valhallammo.item.CustomItem;
import me.athlaeos.valhallammo.item.CustomItemRegistry;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scripts implements Listener {
    private static final QuickTrade[] args = new QuickTrade[]{
            new QuickTrade("generic_sell_coal", ProfessionWrapper.ARMORER, MerchantLevel.JOURNEYMAN, "6x 100% 8-0%-16xCOAL for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("armorer_buy_leather_to_chainmail_helmet_t1", ProfessionWrapper.ARMORER, MerchantLevel.NOVICE, "3x W25 3-25%-6xEMERALD for 1xLEATHER_HELMET G25 EXP=200 ENCH=2 VIL=2 ORD=4 DYNREF=0:leather_helmet_scaling|125:chainmail_helmet_scaling"),
            new QuickTrade("armorer_buy_leather_to_chainmail_chestplate_t1", ProfessionWrapper.ARMORER, MerchantLevel.NOVICE, "3x W25 6-25%-12xEMERALD for 1xLEATHER_CHESTPLATE G25 EXP=200 ENCH=2 VIL=2 ORD=4 DYNREF=0:leather_chestplate_scaling|125:chainmail_chestplate_scaling"),
            new QuickTrade("armorer_buy_leather_to_chainmail_leggings_t1", ProfessionWrapper.ARMORER, MerchantLevel.NOVICE, "3x W25 5-25%-10xEMERALD for 1xLEATHER_LEGGINGS G25 EXP=200 ENCH=2 VIL=2 ORD=4 DYNREF=0:leather_leggings_scaling|125:chainmail_leggings_scaling"),
            new QuickTrade("armorer_buy_leather_to_chainmail_boots_t1", ProfessionWrapper.ARMORER, MerchantLevel.NOVICE, "3x W25 2-25%-4xEMERALD for 1xLEATHER_BOOTS G25 EXP=200 ENCH=2 VIL=2 ORD=4 DYNREF=0:leather_boots_scaling|125:chainmail_boots_scaling"),
            new QuickTrade("generic_sell_iron", ProfessionWrapper.ARMORER, MerchantLevel.JOURNEYMAN, "6x 100% 4-10%-8xIRON_INGOT for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("generic_buy_bell", ProfessionWrapper.ARMORER, MerchantLevel.JOURNEYMAN, "3x 100% 16-25%-32xEMERALD for 1xBELL G15 EXP=200 ENCH=2 VIL=2 ORD=6"),
            new QuickTrade("armorer_buy_leather_to_chainmail_helmet_t2", ProfessionWrapper.ARMORER, MerchantLevel.APPRENTICE, "3x W25 5-25%-10xEMERALD for 1xCHAINMAIL_HELMET G15 EXP=200 ENCH=2 VIL=2 ORD=4 DYNREF=0:leather_helmet_scaling|125:chainmail_helmet_scaling"),
            new QuickTrade("armorer_buy_leather_to_chainmail_chestplate_t2", ProfessionWrapper.ARMORER, MerchantLevel.APPRENTICE, "3x W25 8-25%-16xEMERALD for 1xCHAINMAIL_CHESTPLATE G15 EXP=200 ENCH=2 VIL=2 ORD=4 DYNREF=0:leather_chestplate_scaling|125:chainmail_chestplate_scaling"),
            new QuickTrade("armorer_buy_leather_to_chainmail_leggings_t2", ProfessionWrapper.ARMORER, MerchantLevel.APPRENTICE, "3x W25 7-25%-14xEMERALD for 1xCHAINMAIL_LEGGINGS G15 EXP=200 ENCH=2 VIL=2 ORD=4 DYNREF=0:leather_leggings_scaling|125:chainmail_leggings_scaling"),
            new QuickTrade("armorer_buy_leather_to_chainmail_boots_t2", ProfessionWrapper.ARMORER, MerchantLevel.APPRENTICE, "3x W25 4-25%-8xEMERALD for 1xCHAINMAIL_BOOTS G15 EXP=200 ENCH=2 VIL=2 ORD=4 DYNREF=0:leather_boots_scaling|125:chainmail_boots_scaling"),
            new QuickTrade("armorer_sell_lava_bucket", ProfessionWrapper.ARMORER, MerchantLevel.JOURNEYMAN, "6x 100% 1-0%-1xLAVA_BUCKET for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("armorer_buy_enchanted_iron_helmet", ProfessionWrapper.ARMORER, MerchantLevel.JOURNEYMAN, "3x W25 10-25%-20xEMERALD for 1xIRON_HELMET G10 EXP=1500 ENCH=15 VIL=15 ORD=8 ITEMREF=iron_helmet_scaling"),
            new QuickTrade("armorer_buy_enchanted_iron_chestplate", ProfessionWrapper.ARMORER, MerchantLevel.JOURNEYMAN, "3x W25 16-25%-32xEMERALD for 1xIRON_CHESTPLATE G10 EXP=2400 ENCH=24 VIL=24 ORD=8 ITEMREF=iron_chestplate_scaling"),
            new QuickTrade("armorer_buy_enchanted_iron_leggings", ProfessionWrapper.ARMORER, MerchantLevel.JOURNEYMAN, "3x W25 14-25%-28xEMERALD for 1xIRON_LEGGINGS G10 EXP=21 ENCH=21 VIL=21 ORD=8 ITEMREF=iron_leggings_scaling"),
            new QuickTrade("armorer_buy_enchanted_iron_boots", ProfessionWrapper.ARMORER, MerchantLevel.JOURNEYMAN, "3x W25 8-25%-16xEMERALD for 1xIRON_BOOTS G10 EXP=1200 ENCH=12 VIL=12 ORD=8 ITEMREF=iron_boots_scaling"),
            new QuickTrade("generic_sell_diamond", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "6x 100% 1-10%-2xDIAMOND for 1xEMERALD NOGIFT EXP=400 ENCH=4 VIL=4 ORD=0"),
            new QuickTrade("armorer_buy_enchanted_diamond_helmet", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "2x W25 25-25%-40xEMERALD for 1xDIAMOND_HELMET G5 EXP=2500 ENCH=25 VIL=25 ORD=4 ITEMREF=diamond_helmet_scaling"),
            new QuickTrade("armorer_buy_enchanted_diamond_chestplate", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "2x W25 40-25%-64xEMERALD for 1xDIAMOND_CHESTPLATE G5 EXP=4000 ENCH=40 VIL=40 ORD=4 ITEMREF=diamond_chestplate_scaling"),
            new QuickTrade("armorer_buy_enchanted_diamond_leggings", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "2x W25 35-25%-54xEMERALD for 1xDIAMOND_LEGGINGS G5 EXP=3500 ENCH=35 VIL=35 ORD=4 ITEMREF=diamond_leggings_scaling"),
            new QuickTrade("armorer_buy_enchanted_diamond_boots", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "2x W25 20-25%-32xEMERALD for 1xDIAMOND_BOOTS G5 EXP=2000 ENCH=20 VIL=20 ORD=4 ITEMREF=diamond_boots_scaling"),
            new QuickTrade("generic_buy_netherite_scrap", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "1x 100% 32-25%-64xEMERALD for 1xNETHERITE_SCRAP NOGIFT EXP=3200 ENCH=32 VIL=32 ORD=2"),
            new QuickTrade("armorer_buy_royal_diamond_helmet", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "E1x W25 10-0%-10xEMERALD_BLOCK for 1xDIAMOND_HELMET NOGIFT EXP=10000 ENCH=100 VIL=100 ORD=2 ITEMREF=royal_diamond_helmet"),
            new QuickTrade("armorer_buy_royal_diamond_chestplate", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "E1x W25 16-0%-16xEMERALD_BLOCK for 1xDIAMOND_CHESTPLATE NOGIFT EXP=16000 ENCH=160 VIL=160 ORD=2 ITEMREF=royal_diamond_chestplate"),
            new QuickTrade("armorer_buy_royal_diamond_leggings", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "E1x W25 14-0%-14xEMERALD_BLOCK for 1xDIAMOND_LEGGINGS NOGIFT EXP=14000 ENCH=140 VIL=140 ORD=2 ITEMREF=royal_diamond_leggings"),
            new QuickTrade("armorer_buy_royal_diamond_boots", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "E1x W25 8-0%-8xEMERALD_BLOCK for 1xDIAMOND_BOOTS NOGIFT EXP=8000 ENCH=80 VIL=80 ORD=2 ITEMREF=royal_diamond_boots"),

            new QuickTrade("butcher_buy_rabbit_stew", ProfessionWrapper.BUTCHER, MerchantLevel.NOVICE, "6x 100% 1-10%-2xEMERALD for 1xRABBIT_STEW G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("butcher_sell_raw_rabbit", ProfessionWrapper.BUTCHER, MerchantLevel.NOVICE, "6x W33 3-10%-6xRABBIT for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("butcher_sell_raw_chicken", ProfessionWrapper.BUTCHER, MerchantLevel.NOVICE, "6x W33 4-10%-8xCHICKEN for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("butcher_sell_raw_pork", ProfessionWrapper.BUTCHER, MerchantLevel.NOVICE, "6x W33 6-10%-12xPORKCHOP for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("generic_sell_coal", ProfessionWrapper.BUTCHER, MerchantLevel.APPRENTICE, ""),
            new QuickTrade("butcher_buy_cooked_chicken", ProfessionWrapper.BUTCHER, MerchantLevel.APPRENTICE, "6x W33 1-10%-2xEMERALD for 8xCOOKED_CHICKEN G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("butcher_buy_cooked_pork", ProfessionWrapper.BUTCHER, MerchantLevel.APPRENTICE, "6x W33 1-10%-2xEMERALD for 6xCOOKED_PORKCHOP G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("butcher_buy_cooked_beef", ProfessionWrapper.BUTCHER, MerchantLevel.APPRENTICE, "6x W33 1-10%-2xEMERALD for 6xCOOKED_BEEF G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("butcher_buy_cleaver", ProfessionWrapper.BUTCHER, MerchantLevel.JOURNEYMAN, "2x 100% 4-25%-8xEMERALD for 1xIRON_SWORD G5 EXP=400 ENCH=4 VIL=4 ORD=4 ITEMREF=cleaver"),
            new QuickTrade("butcher_sell_raw_mutton", ProfessionWrapper.BUTCHER, MerchantLevel.JOURNEYMAN, "6x W50 8-10%-16xBEEF for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("butcher_sell_raw_beef", ProfessionWrapper.BUTCHER, MerchantLevel.JOURNEYMAN, "6x W50 6-10%-12xMUTTON for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("butcher_sell_dried_kelp_block", ProfessionWrapper.BUTCHER, MerchantLevel.EXPERT, "6x 100% 6-10%-12xDRIED_KELP_BLOCK for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("butcher_buy_luxurious_stew", ProfessionWrapper.BUTCHER, MerchantLevel.EXPERT, "4x W25 4-25%-8xEMERALD for 1xGOLDEN_APPLE G5 EXP=400 ENCH=4 VIL=4 ORD=16 ITEMREF=luxurious_stew"),
            new QuickTrade("butcher_buy_tender_steak", ProfessionWrapper.BUTCHER, MerchantLevel.EXPERT, "4x W25 4-25%-8xEMERALD for 1xGOLDEN_APPLE G5 EXP=400 ENCH=4 VIL=4 ORD=16 ITEMREF=tender_steak"),
            new QuickTrade("butcher_buy_stuffed_hen", ProfessionWrapper.BUTCHER, MerchantLevel.EXPERT, "4x W25 4-25%-8xEMERALD for 1xGOLDEN_APPLE G5 EXP=400 ENCH=4 VIL=4 ORD=16 ITEMREF=stuffed_hen"),
            new QuickTrade("butcher_buy_glazed_ribs", ProfessionWrapper.BUTCHER, MerchantLevel.EXPERT, "4x W25 4-25%-8xEMERALD for 1xGOLDEN_APPLE G5 EXP=400 ENCH=4 VIL=4 ORD=16 ITEMREF=glazed_ribs"),
            new QuickTrade("butcher_sell_sweet_berries", ProfessionWrapper.BUTCHER, MerchantLevel.MASTER, "6x 100% 12-10%-24xSWEET_BERRIES for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("butcher_buy_battle_burrito", ProfessionWrapper.BUTCHER, MerchantLevel.MASTER, "E4x W25 8-25%-16xEMERALD for 1xGOLDEN_APPLE NOGIFT EXP=800 ENCH=8 VIL=8 ORD=12 ITEMREF=battle_burrito"),
            new QuickTrade("butcher_buy_aegis_stroganoff", ProfessionWrapper.BUTCHER, MerchantLevel.MASTER, "E4x W25 8-25%-16xEMERALD for 1xGOLDEN_APPLE NOGIFT EXP=800 ENCH=8 VIL=8 ORD=12 ITEMREF=aegis_stroganoff"),
            new QuickTrade("butcher_buy_wagyu_steak", ProfessionWrapper.BUTCHER, MerchantLevel.MASTER, "E4x W25 8-25%-16xEMERALD for 1xGOLDEN_APPLE NOGIFT EXP=800 ENCH=8 VIL=8 ORD=12 ITEMREF=wagyu_steak"),
            new QuickTrade("butcher_buy_titanic_goulash", ProfessionWrapper.BUTCHER, MerchantLevel.MASTER, "E4x W25 8-25%-16xEMERALD for 1xGOLDEN_APPLE NOGIFT EXP=800 ENCH=8 VIL=8 ORD=12 ITEMREF=titanic_goulash"),

            new QuickTrade("cleric_sell_rotten_flesh", ProfessionWrapper.CLERIC, MerchantLevel.NOVICE, "6x 100% 32-10%-64xROTTEN_FLESH for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("cleric_buy_redstone_dust", ProfessionWrapper.CLERIC, MerchantLevel.NOVICE, "6x 100% 1-10%-2xEMERALD for 8xREDSTONE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("cleric_buy_brewing_stand", ProfessionWrapper.CLERIC, MerchantLevel.NOVICE, "6x W25 4-10%-8xEMERALD for 1xBREWING_STAND G25 EXP=400 ENCH=4 VIL=4 ORD=8"),
            new QuickTrade("cleric_buy_nether_wart", ProfessionWrapper.CLERIC, MerchantLevel.NOVICE, "2x W25 2-10%-4xEMERALD for 4xNETHER_WART G10 EXP=300 ENCH=3 VIL=3 ORD=4"),
            new QuickTrade("cleric_buy_blaze_powder", ProfessionWrapper.CLERIC, MerchantLevel.NOVICE, "2x W25 4-10%-8xEMERALD for 2xBLAZE_POWDER G10 EXP=400 ENCH=4 VIL=4 ORD=4"),
            new QuickTrade("cleric_buy_glass_bottles", ProfessionWrapper.CLERIC, MerchantLevel.NOVICE, "6x W25 2-10%-4xEMERALD for 6xGLASS_BOTTLE G25 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("cleric_sell_gold", ProfessionWrapper.CLERIC, MerchantLevel.APPRENTICE, "6x W25 3-10%-6xGOLD_INGOT for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("cleric_buy_lapis_lazuli", ProfessionWrapper.CLERIC, MerchantLevel.APPRENTICE, "6x W25 1-10%-2xEMERALD for 4xLAPIS_LAZULI G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("cleric_sell_spider_eyes", ProfessionWrapper.CLERIC, MerchantLevel.APPRENTICE, "6x W25 8-10%-16xSPIDER_EYE for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("cleric_buy_fermented_spider_eye", ProfessionWrapper.CLERIC, MerchantLevel.APPRENTICE, "6x W25 1-10%-2xEMERALD for 1xFERMENTED_SPIDER_EYE G25 EXP=200 ENCH=2 VIL=16 ORD=16"),
            new QuickTrade("cleric_sell_rabbit_feet", ProfessionWrapper.CLERIC, MerchantLevel.JOURNEYMAN, "6x W25 2-10%-4xRABBIT_FOOT for 1xEMERALD NOGIFT EXP=400 ENCH=4 VIL=4 ORD=0"),
            new QuickTrade("cleric_buy_glowstone_dust", ProfessionWrapper.CLERIC, MerchantLevel.JOURNEYMAN, "6x W25 2-10%-4xEMERALD for 8xGLOWSTONE_DUST G25 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("cleric_buy_glistering_melon_slice", ProfessionWrapper.CLERIC, MerchantLevel.JOURNEYMAN, "6x W25 1-10%-2xEMERALD for 2xGLISTERING_MELON_SLICE NOGIFT EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("cleric_buy_golden_carrots", ProfessionWrapper.CLERIC, MerchantLevel.JOURNEYMAN, "6x W25 1-10%-2xEMERALD for 2xGOLDEN_CARROT G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("cleric_sell_glass_bottles", ProfessionWrapper.CLERIC, MerchantLevel.EXPERT, "6x 100% 6-10%-12xGLASS_BOTTLE for 1xEMERALD NOGIFT EXP=400 ENCH=4 VIL=4 ORD=0"),
            new QuickTrade("cleric_buy_ender_pearl", ProfessionWrapper.CLERIC, MerchantLevel.EXPERT, "4x 100% 6-10%-12xEMERALD for 1xENDER_PEARL G15 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("cleric_sell_turtle_scute", ProfessionWrapper.CLERIC, MerchantLevel.EXPERT, "6x W25 2-10%-4xSCUTE for 1xEMERALD NOGIFT EXP=400 ENCH=4 VIL=4 ORD=0"),
            new QuickTrade("cleric_buy_ghast_tear", ProfessionWrapper.CLERIC, MerchantLevel.EXPERT, "6x W25 4-10%-8xEMERALD for 1xGHAST_TEAR G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("cleric_buy_pollen_stalks", ProfessionWrapper.CLERIC, MerchantLevel.EXPERT, "6x W25 8-10%-16xEMERALD for 1xWHEAT G5 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("cleric_sell_nether_wart", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "6x 100% 24-10%-48xNETHER_WART for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("cleric_buy_experience_bottles", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "6x 100% 1-10%-2xEMERALD for 4xEXPERIENCE_BOTTLE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("cleric_buy_bustling_fungus", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "E4x W18 4-10%-8xEMERALD for 1xWHEAT G5 EXP=400 ENCH=4 VIL=4 ORD=16 ITEMREF=bustling_fungus"),
            new QuickTrade("cleric_buy_steelblossom", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "E4x W18 8-10%-16xEMERALD for 1xWHEAT G5 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=steelblossom"),
            new QuickTrade("cleric_buy_shimmerleaf", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "E4x W18 8-10%-16xEMERALD for 1xWHEAT G5 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=shimmerleaf"),
            new QuickTrade("cleric_buy_noxious_spores", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "E4x W18 4-10%-8xEMERALD for 1xWHEAT G5 EXP=400 ENCH=4 VIL=4 ORD=16 ITEMREF=noxious_spores"),
            new QuickTrade("cleric_buy_knightreed", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "E4x W18 4-10%-8xEMERALD for 1xWHEAT G5 EXP=400 ENCH=4 VIL=4 ORD=16 ITEMREF=knightreed"),
            new QuickTrade("cleric_buy_tutor_lotus", ProfessionWrapper.CLERIC, MerchantLevel.MASTER, "E2x W10 32-10%-64xEMERALD for 1xWHEAT G5 EXP=3200 ENCH=32 VIL=32 ORD=16 ITEMREF=tutor_lotus"),

            new QuickTrade("farmer_buy_bread", ProfessionWrapper.FARMER, MerchantLevel.NOVICE, "6x 100% 1-10%-2xEMERALD for 6xBREAD G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_sell_wheat", ProfessionWrapper.FARMER, MerchantLevel.NOVICE, "6x W25 12-10%-24xWHEAT for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("farmer_sell_potatoes", ProfessionWrapper.FARMER, MerchantLevel.NOVICE, "6x W25 24-10%-48xPOTATO for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("farmer_sell_carrots", ProfessionWrapper.FARMER, MerchantLevel.NOVICE, "6x W25 24-10%-48xCARROT for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("farmer_sell_beetroot", ProfessionWrapper.FARMER, MerchantLevel.NOVICE, "6x W25 12-10%-24xBEETROOT for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("farmer_sell_pumpkin", ProfessionWrapper.FARMER, MerchantLevel.APPRENTICE, "6x 100% 4-10%-8xPUMPKIN for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("farmer_buy_pumpkin_pie", ProfessionWrapper.FARMER, MerchantLevel.APPRENTICE, "6x W25 1-10%-2xEMERALD for 4xPUMPKIN_PIE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_apples", ProfessionWrapper.FARMER, MerchantLevel.APPRENTICE, "6x W25 1-10%-2xEMERALD for 8xAPPLE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_cake", ProfessionWrapper.FARMER, MerchantLevel.APPRENTICE, "3x W25 1-10%-2xEMERALD for 1xCAKE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_mushroom_stew", ProfessionWrapper.FARMER, MerchantLevel.APPRENTICE, "6x W25 1-10%-2xEMERALD for 4xMUSHROOM_STEW G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_sell_melons", ProfessionWrapper.FARMER, MerchantLevel.JOURNEYMAN, "6x 100% 4-10%-8xMELON for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("farmer_buy_cookies", ProfessionWrapper.FARMER, MerchantLevel.JOURNEYMAN, "6x W25 1-10%-2xEMERALD for 12xCOOKIE G25 EXP=200 ENCH=2 VIL=2 ORD=16 ITEMREF=cookies_fast"),
            new QuickTrade("farmer_buy_regeneration_mushroom_stew", ProfessionWrapper.FARMER, MerchantLevel.JOURNEYMAN, "4x W25 1-10%-2xEMERALD for 2xMUSHROOM_STEW G25 EXP=200 ENCH=2 VIL=2 ORD=16 ITEMREF=mushroom_stew_regeneration"),
            new QuickTrade("farmer_buy_night_vision_glow_berries", ProfessionWrapper.FARMER, MerchantLevel.JOURNEYMAN, "4x W25 1-10%-2xEMERALD for 4xGLOW_BERRIES G25 EXP=200 ENCH=2 VIL=2 ORD=16 ITEMREF=glow_berries_night_vision"),
            new QuickTrade("farmer_buy_health_boost_beetroot_soup", ProfessionWrapper.FARMER, MerchantLevel.JOURNEYMAN, "4x W25 1-10%-2xEMERALD for 2xBEETROOT_SOUP G25 EXP=200 ENCH=2 VIL=2 ORD=16 ITEMREF=beetroot_soup_health_boost"),
            new QuickTrade("farmer_buy_apple_pie", ProfessionWrapper.FARMER, MerchantLevel.EXPERT, "4x 100% 4-10%-8xEMERALD for 1xGOLDEN_APPLE G15 EXP=400 ENCH=4 VIL=4 ORD=16 ITEMREF=apple_pie"),
            new QuickTrade("farmer_buy_random_suspicious_stew", ProfessionWrapper.FARMER, MerchantLevel.EXPERT, "6x W25 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_random_suspicious_stew", ProfessionWrapper.FARMER, MerchantLevel.EXPERT, "6x W25 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_random_suspicious_stew", ProfessionWrapper.FARMER, MerchantLevel.EXPERT, "6x W25 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_random_suspicious_stew", ProfessionWrapper.FARMER, MerchantLevel.EXPERT, "6x W25 1-10%-2xEMERALD for 1xSUSPICIOUS_STEW G3 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_golden_apple", ProfessionWrapper.FARMER, MerchantLevel.MASTER, "4x 100% 1-10%-2xEMERALD for 1xGOLDEN_APPLE G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("farmer_buy_enchanted_golden_apple", ProfessionWrapper.FARMER, MerchantLevel.MASTER, "4x 100% 16-0%-16xEMERALD_BLOCK for 1xENCHANTED_GOLDEN_APPLE G2 EXP=16000 ENCH=160 VIL=160 ORD=16"),
            new QuickTrade("farmer_buy_golden_apple_pie", ProfessionWrapper.FARMER, MerchantLevel.MASTER, "E4x W33 8-10%-16xEMERALD for 1xGOLDEN_APPLE G5 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=golden_apple_pie"),
            new QuickTrade("farmer_buy_lembas_bread", ProfessionWrapper.FARMER, MerchantLevel.MASTER, "E4x W33 8-10%-16xEMERALD for 1xGOLDEN_APPLE G5 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=lembas_bread"),
            new QuickTrade("farmer_buy_churro", ProfessionWrapper.FARMER, MerchantLevel.MASTER, "E4x W33 4-10%-8xEMERALD for 1xGOLDEN_APPLE G10 EXP=400 ENCH=4 VIL=4 ORD=16 ITEMREF=churro"),

            new QuickTrade("fisherman_sell_string", ProfessionWrapper.FISHERMAN, MerchantLevel.NOVICE, "6x 100% 16-10%-32xSTRING for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("generic_sell_coal", ProfessionWrapper.FISHERMAN, MerchantLevel.NOVICE, ""),
            new QuickTrade("fisherman_buy_cod_bucket", ProfessionWrapper.FISHERMAN, MerchantLevel.NOVICE, "4x W50 2-10%-4xEMERALD for 1xCOD_BUCKET G25 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("fisherman_buy_cooked_cod", ProfessionWrapper.FISHERMAN, MerchantLevel.NOVICE, "6x W50 1-10%-2xEMERALD for 6xCOOKED_COD G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_sell_cod", ProfessionWrapper.FISHERMAN, MerchantLevel.APPRENTICE, "6x 100% 12-10%-24xCOD for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fisherman_sell_salmon", ProfessionWrapper.FISHERMAN, MerchantLevel.APPRENTICE, "6x 100% 12-10%-24xSALMON for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fisherman_buy_tropical_fish_bucket", ProfessionWrapper.FISHERMAN, MerchantLevel.APPRENTICE, "4x W25 4-10%-8xEMERALD for 1xTROPICAL_FISH_BUCKET G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("fisherman_buy_salmon_bucket", ProfessionWrapper.FISHERMAN, MerchantLevel.APPRENTICE, "4x W25 2-10%-4xEMERALD for 1xSALMON_BUCKET G15 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("fisherman_buy_cooked_salmon", ProfessionWrapper.FISHERMAN, MerchantLevel.APPRENTICE, "6x W25 1-10%-2xEMERALD for 6xCOOKED_SALMON G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_buy_pufferfish_bucket", ProfessionWrapper.FISHERMAN, MerchantLevel.APPRENTICE, "4x W25 4-10%-8xEMERALD for 1xPUFFERFISH_BUCKET G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("fisherman_sell_tropical_fish", ProfessionWrapper.FISHERMAN, MerchantLevel.JOURNEYMAN, "6x 100% 4-10%-8xTROPICAL_FISH for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fisherman_sell_pufferfish", ProfessionWrapper.FISHERMAN, MerchantLevel.JOURNEYMAN, "6x 100% 6-10%-12xPUFFERFISH for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fisherman_sell_boat", ProfessionWrapper.FISHERMAN, MerchantLevel.JOURNEYMAN, "6x W25 1-0%-1xOAK_BOAT for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("fisherman_sell_ink_sacs", ProfessionWrapper.FISHERMAN, MerchantLevel.JOURNEYMAN, "6x W25 8-10%-16xINK_SAC for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fisherman_buy_fishing_rod_t1", ProfessionWrapper.FISHERMAN, MerchantLevel.JOURNEYMAN, "3x W25 4-25%-8xEMERALD for 1xFISHING_ROD G10 EXP=400 ENCH=4 VIL=4 ORD=8"),
            new QuickTrade("fisherman_buy_turtle_scute", ProfessionWrapper.FISHERMAN, MerchantLevel.JOURNEYMAN, "6x W25 4-10%-8xEMERALD for 1xSCUTE G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("fisherman_buy_prismarine_crystals", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "6x 100% 1-10%-2xEMERALD for 8xPRISMARINE_CRYSTALS G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_buy_prismarine_shards", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "6x 100% 1-10%-2xEMERALD for 32xPRISMARINE_SHARD G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fisherman_buy_nautilus_shell", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "4x W25 8-10%-16xEMERALD for 1xNAUTILUS_SHELL G10 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("fisherman_buy_bait", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "6x W25 1-10%-2xEMERALD for 6xORANGE_DYE G15 EXP=200 ENCH=2 VIL=2 ORD=16 ITEMREF=fishing_bait"),
            new QuickTrade("fisherman_buy_fishing_rod_t2", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "3x W25 16-10%-32xEMERALD for 1xFISHING_ROD G10 EXP=1600 ENCH=16 VIL=16 ORD=8"),
            new QuickTrade("fisherman_buy_cooked_lionfish", ProfessionWrapper.FISHERMAN, MerchantLevel.EXPERT, "4x W25 8-10%-16xEMERALD for 1xGOLDEN_APPLE G10 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=cooked_lionfish"),
            new QuickTrade("fisherman_buy_heart_of_the_sea", ProfessionWrapper.FISHERMAN, MerchantLevel.MASTER, "1x 100% 64-0%-64xEMERALD for 1xHEART_OF_THE_SEA G5 EXP=6400 ENCH=64 VIL=64 ORD=3"),
            new QuickTrade("fisherman_buy_caviar", ProfessionWrapper.FISHERMAN, MerchantLevel.MASTER, "E4x W25 8-10%-16xEMERALD for 1xGOLDEN_APPLE G15 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=caviar"),
            new QuickTrade("fisherman_buy_salmon_en_croute", ProfessionWrapper.FISHERMAN, MerchantLevel.MASTER, "E4x W25 4-10%-8xEMERALD_BLOCK for 1xGOLDEN_APPLE G2 EXP=400 ENCH=4 VIL=4 ORD=16 ITEMREF=salmon_en_croute"),
            new QuickTrade("fisherman_buy_poached_lobster", ProfessionWrapper.FISHERMAN, MerchantLevel.MASTER, "E4x W25 4-10%-8xEMERALD for 1xGOLDEN_APPLE G5 EXP=400 ENCH=4 VIL=4 ORD=16 ITEMREF=poached_lobster"),
            new QuickTrade("fisherman_buy_uni_sushi", ProfessionWrapper.FISHERMAN, MerchantLevel.MASTER, "E4x W25 8-10%-16xEMERALD for 1xGOLDEN_APPLE G10 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=uni_sushi"),

            new QuickTrade("fletcher_sell_sticks", ProfessionWrapper.FLETCHER, MerchantLevel.NOVICE, "6x 100% 32-10%-64xSTICK for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("fletcher_buy_flint_arrows", ProfessionWrapper.FLETCHER, MerchantLevel.NOVICE, "6x W25 1-10%-2xEMERALD for 16xARROW NOGIFT EXP=200 ENCH=2 VIL=2 ORD=16 ITEMREF=flint_arrow"),
            new QuickTrade("fletcher_buy_flint", ProfessionWrapper.FLETCHER, MerchantLevel.NOVICE, "6x W25 1-10%-2xEMERALD for 8xFLINT G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fletcher_buy_feathers", ProfessionWrapper.FLETCHER, MerchantLevel.NOVICE, "6x W25 1-10%-2xEMERALD for 8xFEATHER G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("fletcher_buy_string", ProfessionWrapper.FLETCHER, MerchantLevel.NOVICE, "6x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("generic_sell_flint", ProfessionWrapper.FLETCHER, MerchantLevel.APPRENTICE, "6x 100% 8-10%-16xFLINT for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fletcher_buy_bow_t1", ProfessionWrapper.FLETCHER, MerchantLevel.APPRENTICE, "3x W25 2-10%-4xEMERALD for 1xBOW G15 EXP=300 ENCH=3 VIL=3 ORD=16 ITEMREF=bow_scaling"),
            new QuickTrade("fletcher_buy_copper_arrows", ProfessionWrapper.FLETCHER, MerchantLevel.APPRENTICE, "6x W25 2-10%-4xEMERALD for 1xARROW G15 EXP=300 ENCH=3 VIL=3 ORD=16 ITEMREF=copper_arrow"),
            new QuickTrade("fletcher_buy_crossbow_t1", ProfessionWrapper.FLETCHER, MerchantLevel.APPRENTICE, "3x W25 4-10%-8xEMERALD for 1xCROSSBOW G15 EXP=400 ENCH=4 VIL=4 ORD=16 ITEMREF=crossbow_scaling"),
            new QuickTrade("fletcher_sell_string", ProfessionWrapper.FLETCHER, MerchantLevel.APPRENTICE, "6x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fletcher_sell_feathers", ProfessionWrapper.FLETCHER, MerchantLevel.JOURNEYMAN, "6x 100% 16-10%-32xFEATHER for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fletcher_buy_bow_t2", ProfessionWrapper.FLETCHER, MerchantLevel.JOURNEYMAN, "3x W25 6-10%-12xEMERALD for 1xBOW G10 EXP=600 ENCH=6 VIL=6 ORD=16 ITEMREF=bow_scaling"),
            new QuickTrade("fletcher_buy_crossbow_t2", ProfessionWrapper.FLETCHER, MerchantLevel.JOURNEYMAN, "3x W25 12-10%-24xEMERALD for 1xCROSSBOW G10 EXP=1200 ENCH=12 VIL=12 ORD=16 ITEMREF=crossbow_scaling"),
            new QuickTrade("fletcher_buy_golden_arrows", ProfessionWrapper.FLETCHER, MerchantLevel.JOURNEYMAN, "6x W25 8-10%-16xEMERALD for 16xARROW G10 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=golden_arrow"),
            new QuickTrade("fletcher_sell_tripwire_hooks", ProfessionWrapper.FLETCHER, MerchantLevel.JOURNEYMAN, "6x W25 8-25%-16xTRIPWIRE_HOOK for 1xEMERALD NOGIFT EXP=400 ENCH=4 VIL=4 ORD=0"),
            new QuickTrade("fletcher_sell_targets", ProfessionWrapper.FLETCHER, MerchantLevel.EXPERT, "6x 100% 4-10%-8xTARGET for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("fletcher_buy_iron_arrows", ProfessionWrapper.FLETCHER, MerchantLevel.EXPERT, "6x W25 8-10%-16xEMERALD for 16xARROW G7.5 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=iron_arrow"),
            new QuickTrade("fletcher_buy_bow_t3", ProfessionWrapper.FLETCHER, MerchantLevel.EXPERT, "2x W25 24-10%-48xEMERALD for 1xBOW G7.5 EXP=2400 ENCH=24 VIL=24 ORD=16 ITEMREF=bow_scaling"),
            new QuickTrade("fletcher_buy_crossbow_t3", ProfessionWrapper.FLETCHER, MerchantLevel.EXPERT, "2x W25 36-10%-64xEMERALD for 1xCROSSBOW G7.5 EXP=3600 ENCH=36 VIL=36 ORD=16 ITEMREF=crossbow_scaling"),
            new QuickTrade("fletcher_buy_random_tipped_arrows", ProfessionWrapper.FLETCHER, MerchantLevel.EXPERT, "6x W5 6-10%-12xEMERALD for 4xTIPPED_ARROW G3 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("fletcher_buy_random_tipped_arrows", ProfessionWrapper.FLETCHER, MerchantLevel.EXPERT, ""),
            new QuickTrade("fletcher_buy_random_tipped_arrows", ProfessionWrapper.FLETCHER, MerchantLevel.EXPERT, ""),
            new QuickTrade("fletcher_buy_random_tipped_arrows", ProfessionWrapper.FLETCHER, MerchantLevel.EXPERT, ""),
            new QuickTrade("fletcher_buy_random_tipped_arrows", ProfessionWrapper.FLETCHER, MerchantLevel.EXPERT, ""),
            new QuickTrade("fletcher_buy_ender_arrow", ProfessionWrapper.FLETCHER, MerchantLevel.MASTER, "2x 100% 8-25%-16xEMERALD for 1xARROW G5 EXP=800 ENCH=8 VIL=8 ORD=8"),
            new QuickTrade("fletcher_buy_royal_bow", ProfessionWrapper.FLETCHER, MerchantLevel.MASTER, "E1x W20 8-25%-16xEMERALD_BLOCK for 1xBOW G5 EXP=3200 ENCH=32 VIL=32 ORD=16 ITEMREF=royal_bow_scaling"),
            new QuickTrade("fletcher_buy_royal_crossbow", ProfessionWrapper.FLETCHER, MerchantLevel.MASTER, "E1x W20 8-25%-16xEMERALD_BLOCK for 1xCROSSBOW G5 EXP=3200 ENCH=32 VIL=32 ORD=16 ITEMREF=royal_crossbow_scaling"),
            new QuickTrade("fletcher_buy_incendiary_arrows", ProfessionWrapper.FLETCHER, MerchantLevel.MASTER, "E2x W20 16-25%-32xEMERALD for 1xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16 ITEMREF=incendiary_arrows"),
            new QuickTrade("fletcher_buy_explosive_arrows", ProfessionWrapper.FLETCHER, MerchantLevel.MASTER, "E2x W20 16-25%-32xEMERALD for 2xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16 ITEMREF=explosive_arrows"),
            new QuickTrade("fletcher_buy_unholy_arrows", ProfessionWrapper.FLETCHER, MerchantLevel.MASTER, "E2x W20 16-25%-32xEMERALD for 4xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16 ITEMREF=unholy_arrows"),

            new QuickTrade("leatherworker_sell_leather", ProfessionWrapper.LEATHERWORKER, MerchantLevel.NOVICE, "6x 100% 4-10%-8xLEATHER for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("leatherworker_buy_leather_helmet_t1", ProfessionWrapper.LEATHERWORKER, MerchantLevel.NOVICE, "3x W25 2-25%-2xEMERALD for 1xLEATHER_HELMET G25 EXP=200 ENCH=2 VIL=2 ORD=16 ITEMREF=leather_helmet_scaling"),
            new QuickTrade("leatherworker_buy_leather_chestplate_t1", ProfessionWrapper.LEATHERWORKER, MerchantLevel.NOVICE, "3x W25 4-25%-2xEMERALD for 1xLEATHER_CHESTPLATE G25 EXP=200 ENCH=2 VIL=2 ORD=16 ITEMREF=leather_chestplate_scaling"),
            new QuickTrade("leatherworker_buy_leather_leggings_t1", ProfessionWrapper.LEATHERWORKER, MerchantLevel.NOVICE, "3x W25 3-25%-2xEMERALD for 1xLEATHER_LEGGINGS G25 EXP=200 ENCH=2 VIL=2 ORD=16 ITEMREF=leather_leggings_scaling"),
            new QuickTrade("leatherworker_buy_leather_boots_t1", ProfessionWrapper.LEATHERWORKER, MerchantLevel.NOVICE, "3x W25 2-25%-2xEMERALD for 1xLEATHER_BOOTS G25 EXP=200 ENCH=2 VIL=2 ORD=16 ITEMREF=leather_boots_scaling"),
            new QuickTrade("generic_sell_flint", ProfessionWrapper.LEATHERWORKER, MerchantLevel.APPRENTICE, ""),
            new QuickTrade("leatherworker_buy_leather_helmet_t2", ProfessionWrapper.LEATHERWORKER, MerchantLevel.APPRENTICE, "3x W25 5-25%-10xEMERALD for 1xLEATHER_HELMET G15 EXP=200 ENCH=2 VIL=2 ORD=16 ITEMREF=leather_helmet_scaling"),
            new QuickTrade("leatherworker_buy_leather_chestplate_t2", ProfessionWrapper.LEATHERWORKER, MerchantLevel.APPRENTICE, "3x W25 8-25%-16xEMERALD for 1xLEATHER_CHESTPLATE G15 EXP=200 ENCH=2 VIL=2 ORD=16 ITEMREF=leather_chestplate_scaling"),
            new QuickTrade("leatherworker_buy_leather_leggings_t2", ProfessionWrapper.LEATHERWORKER, MerchantLevel.APPRENTICE, "3x W25 7-25%-14xEMERALD for 1xLEATHER_LEGGINGS G15 EXP=200 ENCH=2 VIL=2 ORD=16 ITEMREF=leather_leggings_scaling"),
            new QuickTrade("leatherworker_buy_leather_boots_t2", ProfessionWrapper.LEATHERWORKER, MerchantLevel.APPRENTICE, "3x W25 4-25%-8xEMERALD for 1xLEATHER_BOOTS G15 EXP=200 ENCH=2 VIL=2 ORD=16 ITEMREF=leather_boots_scaling"),
            new QuickTrade("leatherworker_sell_rabbit_hide", ProfessionWrapper.LEATHERWORKER, MerchantLevel.JOURNEYMAN, "6x 100% 3-10%-6xRABBIT_HIDE for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("leatherworker_buy_saddle", ProfessionWrapper.LEATHERWORKER, MerchantLevel.JOURNEYMAN, "6x W25 4-10%-8xEMERALD for 1xSADDLE G15 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("leatherworker_buy_leather_horse_armor", ProfessionWrapper.LEATHERWORKER, MerchantLevel.JOURNEYMAN, "6x W25 4-10%-8xEMERALD for 1xLEATHER_HORSE_ARMOR G10 EXP=1200 ENCH=12 VIL=12 ORD=16"),
            new QuickTrade("leatherworker_buy_iron_horse_armor", ProfessionWrapper.LEATHERWORKER, MerchantLevel.JOURNEYMAN, "6x W25 6-10%-12xEMERALD for 1xIRON_HORSE_ARMOR G10 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("leatherworker_buy_golden_horse_armor", ProfessionWrapper.LEATHERWORKER, MerchantLevel.JOURNEYMAN, "6x W25 8-25%-16xEMERALD for 1xGOLDEN_HORSE_ARMOR G10 EXP=400 ENCH=4 VIL=4 ORD=0"),
            new QuickTrade("leatherworker_sell_turtle_scute", ProfessionWrapper.LEATHERWORKER, MerchantLevel.EXPERT, "6x 100% 4-10%-8xSCUTE for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("leatherworker_buy_bundle", ProfessionWrapper.LEATHERWORKER, MerchantLevel.EXPERT, "6x 100% 8-10%-16xEMERALD for 1xBUNDLE G10 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("leatherworker_buy_turtle_helmet", ProfessionWrapper.LEATHERWORKER, MerchantLevel.EXPERT, "2x W25 15-25%-30xEMERALD for 1xTURTLE_HELMET G5 EXP=2400 ENCH=24 VIL=24 ORD=16 ITEMREF=turtle_helmet"),
            new QuickTrade("leatherworker_buy_turtle_chestplate", ProfessionWrapper.LEATHERWORKER, MerchantLevel.EXPERT, "2x W25 24-25%-48xEMERALD for 1xLEATHER_CHESTPLATE G5 EXP=3600 ENCH=36 VIL=36 ORD=16 ITEMREF=turtle_chestplate"),
            new QuickTrade("leatherworker_buy_turtle_leggings", ProfessionWrapper.LEATHERWORKER, MerchantLevel.EXPERT, "2x W25 21-25%-42xEMERALD for 1xLEATHER_LEGGINGS G5 EXP=800 ENCH=8 VIL=8 ORD=8 ITEMREF=turtle_leggings"),
            new QuickTrade("leatherworker_buy_turtle_boots", ProfessionWrapper.LEATHERWORKER, MerchantLevel.EXPERT, "2x W25 12-25%-24xEMERALD for 1xLEATHER_BOOTS G5 EXP=3200 ENCH=32 VIL=32 ORD=16 ITEMREF=turtle_boots"),
            new QuickTrade("leatherworker_buy_diamond_horse_armor", ProfessionWrapper.LEATHERWORKER, MerchantLevel.MASTER, "4x 100% 16-25%-32xEMERALD for 1xDIAMOND_HORSE_ARMOR G5 EXP=2400 ENCH=24 VIL=24 ORD=16"),
            new QuickTrade("leatherworker_buy_assassin_helmet", ProfessionWrapper.LEATHERWORKER, MerchantLevel.MASTER, "E1x W25 3-10%-6xEMERALD_BLOCK for 1xLEATHER_HELMET G2.5 EXP=3200 ENCH=32 VIL=32 ORD=16 ITEMREF=assassin_helmet"),
            new QuickTrade("leatherworker_buy_assassin_chestplate", ProfessionWrapper.LEATHERWORKER, MerchantLevel.MASTER, "E1x W25 5-10%-10xEMERALD_BLOCK for 1xLEATHER_CHESTPLATE G2.5 EXP=1600 ENCH=16 VIL=16 ORD=16 ITEMREF=assassin_chestplate"),
            new QuickTrade("leatherworker_buy_assassin_leggings", ProfessionWrapper.LEATHERWORKER, MerchantLevel.MASTER, "E1x W25 4-10%-8xEMERALD_BLOCK for 1xLEATHER_LEGGINGS G2.5 EXP=1600 ENCH=16 VIL=16 ORD=16 ITEMREF=assassin_leggings"),
            new QuickTrade("leatherworker_buy_assassin_boots", ProfessionWrapper.LEATHERWORKER, MerchantLevel.MASTER, "E1x W25 2-10%-4xEMERALD_BLOCK for 1xLEATHER_BOOTS G2.5 EXP=1600 ENCH=16 VIL=16 ORD=16 ITEMREF=assassin_boots"),

            new QuickTrade("librarian_sell_paper", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "6x 100% 24-10%-48xPAPER for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("librarian_buy_bookshelf", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "6x 100% 4-10%-8xEMERALD for 1xBOOKSHELF NOGIFT EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("librarian_buy_random_enchanted_book_t1", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "6x W25 5-10%-10xEMERALD for 1xBOOK G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("librarian_buy_random_enchanted_book_t1", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, ""),
            new QuickTrade("librarian_buy_random_enchanted_book_t1", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, ""),
            new QuickTrade("librarian_buy_random_enchanted_book_t1", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, ""),
//            new QuickTrade("librarian_buy_efficiency_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 8xFEATHER G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
//            new QuickTrade("librarian_buy_sharpness_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
//            new QuickTrade("librarian_buy_power_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
//            new QuickTrade("librarian_buy_smite_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
//            new QuickTrade("librarian_buy_bane_of_arthropods_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
//            new QuickTrade("librarian_buy_projectile_protection_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
//            new QuickTrade("librarian_buy_blast_protection_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
//            new QuickTrade("librarian_buy_fire_protection_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.NOVICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("librarian_sell_books", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x 100% 8-10%-16xBOOK for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_buy_lantern", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x 100% 2-10%-4xEMERALD for 4xLANTERN G25 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("librarian_buy_random_enchanted_book_t2", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "6x W25 10-10%-20xEMERALD for 1xBOOK G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("librarian_buy_random_enchanted_book_t2", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, ""),
            new QuickTrade("librarian_buy_random_enchanted_book_t2", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, ""),
            new QuickTrade("librarian_buy_random_enchanted_book_t2", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, ""),
//            new QuickTrade("librarian_buy_piercing_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
//            new QuickTrade("librarian_buy_impaling_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
//            new QuickTrade("librarian_buy_protection_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
//            new QuickTrade("librarian_buy_fire_aspect_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
//            new QuickTrade("librarian_buy_knockback_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
//            new QuickTrade("librarian_buy_punch_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
//            new QuickTrade("librarian_buy_quick_charge_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
//            new QuickTrade("librarian_buy_lure_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.APPRENTICE, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_sell_ink_sacs", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "6x 100% 6-10%-12xINK_SAC for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_buy_glass", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "6x 100% 2-10%-4xEMERALD for 8xGLASS G10 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("librarian_buy_random_enchanted_book_t3", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "6x W25 15-15%-30xEMERALD for 1xBOOK G10 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("librarian_buy_random_enchanted_book_t3", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, ""),
            new QuickTrade("librarian_buy_random_enchanted_book_t3", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, ""),
            new QuickTrade("librarian_buy_random_enchanted_book_t3", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, ""),
//            new QuickTrade("librarian_buy_aqua_affinity_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "12x W25 4-10%-8xEMERALD for 1xCROSSBOW G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
//            new QuickTrade("librarian_buy_feather_falling_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
//            new QuickTrade("librarian_buy_unbreaking_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "12x W25 1-10%-2xEMERALD for 4xSTRING G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
//            new QuickTrade("librarian_buy_sweeping_edge_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
//            new QuickTrade("librarian_buy_respiration_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
//            new QuickTrade("librarian_buy_depth_strider_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
//            new QuickTrade("librarian_buy_multishot_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.JOURNEYMAN, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_sell_book_and_quill", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "6x 100% 2-10%-4xEMERALD for 1xWRITABLE_BOOK G25 EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("librarian_buy_compass", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "6x 100% 2-10%-4xEMERALD for 1xCOMPASS G15 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("librarian_buy_clock", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "6x 100% 2-10%-4xEMERALD for 1xCLOCK G15 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("librarian_buy_random_enchanted_book_t4", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "6x W25 20-20%-40xEMERALD for 1xBOOK G7.5 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("librarian_buy_random_enchanted_book_t4", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, ""),
            new QuickTrade("librarian_buy_random_enchanted_book_t4", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, ""),
            new QuickTrade("librarian_buy_random_enchanted_book_t4", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, ""),
//            new QuickTrade("librarian_buy_fortune_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "12x W25 2-10%-4xEMERALD for 1xARROW G15 EXP=300 ENCH=3 VIL=3 ORD=16"),
//            new QuickTrade("librarian_buy_looting_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
//            new QuickTrade("librarian_buy_luck_of_the_sea_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "12x W25 12-10%-24xSTRING for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
//            new QuickTrade("librarian_buy_frost_walker_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "12x W25 24-10%-48xEMERALD for 1xBOW G7.5 EXP=2400 ENCH=24 VIL=24 ORD=16"),
//            new QuickTrade("librarian_buy_channeling_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "12x W25 36-10%-64xEMERALD for 1xCROSSBOW G7.5 EXP=3600 ENCH=36 VIL=36 ORD=16"),
//            new QuickTrade("librarian_buy_loyalty_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "4x 100% 8-10%-16xEMERALD for 1xARROW G5 EXP=800 ENCH=8 VIL=8 ORD=8"),
//            new QuickTrade("librarian_buy_riptide_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.EXPERT, "E12x W20 32-10%-64xEMERALD for 1xBOW G5 EXP=3200 ENCH=32 VIL=32 ORD=16"),
            new QuickTrade("librarian_buy_name_tag", ProfessionWrapper.LIBRARIAN, MerchantLevel.MASTER, "6x 100% 4-10%-8xEMERALD for 1xNAME_TAG G10 EXP=2400 ENCH=24 VIL=24 ORD=16"),
            new QuickTrade("librarian_buy_random_enchanted_book_t5", ProfessionWrapper.LIBRARIAN, MerchantLevel.MASTER, "6x W25 25-25%-50xEMERALD for 1xBOOK G5 EXP=3200 ENCH=32 VIL=32 ORD=16"),
            new QuickTrade("librarian_buy_random_enchanted_book_t5", ProfessionWrapper.LIBRARIAN, MerchantLevel.MASTER, ""),
            new QuickTrade("librarian_buy_random_enchanted_book_t5", ProfessionWrapper.LIBRARIAN, MerchantLevel.MASTER, ""),
            new QuickTrade("librarian_buy_random_enchanted_book_t5", ProfessionWrapper.LIBRARIAN, MerchantLevel.MASTER, ""),
//            new QuickTrade("librarian_buy_infinity_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.MASTER, "E12x W20 32-10%-64xEMERALD for 1xCROSSBOW G5 EXP=3200 ENCH=32 VIL=32 ORD=16"),
//            new QuickTrade("librarian_buy_mending_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 1xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),
//            new QuickTrade("librarian_buy_silk_touch_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 2xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),
//            new QuickTrade("librarian_buy_thorns_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.MASTER, "E12x W20 16-10%-32xEMERALD for 4xARROW G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),
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

            new QuickTrade("mason_sell_clay_ball", ProfessionWrapper.MASON, MerchantLevel.NOVICE, "6x 100% 32-10%-64xCLAY_BALL for 1xEMERALD NOGIFT EXP=100 ENCH=1 VIL=1 ORD=0"),
            new QuickTrade("mason_buy_stone", ProfessionWrapper.MASON, MerchantLevel.NOVICE, "6x W15 1-10%-2xEMERALD for 16xSTONE NOGIFT EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_smooth_stone", ProfessionWrapper.MASON, MerchantLevel.NOVICE, "6x W15 1-10%-2xEMERALD for 8xSMOOTH_STONE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_cobblestone", ProfessionWrapper.MASON, MerchantLevel.NOVICE, "6x W15 1-10%-2xEMERALD for 24xCOBBLESTONE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_mossy_cobblestone", ProfessionWrapper.MASON, MerchantLevel.NOVICE, "6x W15 1-10%-2xEMERALD for 8xMOSSY_COBBLESTONE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_mossy_stone_bricks", ProfessionWrapper.MASON, MerchantLevel.NOVICE, "6x W15 1-10%-2xEMERALD for 8xMOSSY_STONE_BRICKS G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_bricks", ProfessionWrapper.MASON, MerchantLevel.NOVICE, "6x W15 1-10%-2xEMERALD for 8xBRICKS G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_mud", ProfessionWrapper.MASON, MerchantLevel.NOVICE, "6x W15 1-10%-2xEMERALD for 8xMUD G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_sell_stone", ProfessionWrapper.MASON, MerchantLevel.APPRENTICE, "6x 100% 32-10%-64xSTONE for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_buy_cobbled_deepslate", ProfessionWrapper.MASON, MerchantLevel.APPRENTICE, "6x W15 1-10%-2xEMERALD for 16xCOBBLED_DEEPSLATE G15 EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_buy_terracotta", ProfessionWrapper.MASON, MerchantLevel.APPRENTICE, "6x W15 1-10%-2xEMERALD for 16xTERRACOTTA G15 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("mason_buy_terracotta_random_color", ProfessionWrapper.MASON, MerchantLevel.APPRENTICE, "6x W15 1-10%-2xEMERALD for 12xTERRACOTTA G15 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("mason_buy_andesite", ProfessionWrapper.MASON, MerchantLevel.APPRENTICE, "6x W15 1-10%-2xEMERALD for 24xANDESITE G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_granite", ProfessionWrapper.MASON, MerchantLevel.APPRENTICE, "6x W15 1-10%-2xEMERALD for 24xGRANITE G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_diorite", ProfessionWrapper.MASON, MerchantLevel.APPRENTICE, "6x W15 1-10%-2xEMERALD for 24xDIORITE G15 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_sandstone", ProfessionWrapper.MASON, MerchantLevel.APPRENTICE, "6x W15 1-10%-2xEMERALD for 16xSANDSTONE G15 EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_buy_tuff", ProfessionWrapper.MASON, MerchantLevel.APPRENTICE, "6x W15 1-10%-2xEMERALD for 16xTUFF G15 EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_sell_granite", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "6x 100% 32-10%-64xGRANITE for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_sell_andesite", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "6x 100% 32-10%-64xANDESITE for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_sell_diorite", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "6x 100% 32-10%-64xDIORITE for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_buy_basalt", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "6x W15 1-10%-2xEMERALD for 16xBASALT G10 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("mason_buy_calcite", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "6x W15 1-10%-2xEMERALD for 16xCALCITE G10 EXP=600 ENCH=6 VIL=6 ORD=16"),
            new QuickTrade("mason_buy_glass", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "6x W15 1-10%-2xEMERALD for 8xGLASS G15 EXP=400 ENCH=4 VIL=4 ORD=16"),
            new QuickTrade("mason_buy_dripstone", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "6x W15 1-10%-2xEMERALD for 16xDRIPSTONE_BLOCK NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_buy_blackstone", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "6x W15 1-10%-2xEMERALD for 16xBLACKSTONE G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("mason_buy_red_sandstone", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "6x W15 1-10%-2xEMERALD for 16xRED_SANDSTONE NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_buy_nether_bricks", ProfessionWrapper.MASON, MerchantLevel.JOURNEYMAN, "6x W15 1-10%-2xEMERALD for 12xNETHER_BRICKS NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_sell_nether_quartz", ProfessionWrapper.MASON, MerchantLevel.EXPERT, "6x 100% 24-10%-48xQUARTZ for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_buy_quartz_block", ProfessionWrapper.MASON, MerchantLevel.EXPERT, "6x W15 1-10%-2xEMERALD for 8xQUARTZ_BLOCK G10 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("mason_buy_tinted_glass", ProfessionWrapper.MASON, MerchantLevel.EXPERT, "6x W15 1-10%-2xEMERALD for 8xTINTED_GLASS G10 EXP=800 ENCH=8 VIL=8 ORD=16"),
            new QuickTrade("mason_buy_amethyst_block", ProfessionWrapper.MASON, MerchantLevel.EXPERT, "6x W15 1-10%-2xEMERALD for 8xAMETHYST_BLOCK G10 EXP=300 ENCH=3 VIL=3 ORD=16"),
            new QuickTrade("mason_buy_prismarine", ProfessionWrapper.MASON, MerchantLevel.EXPERT, "6x W15 1-10%-2xEMERALD for 8xPRISMARINE G10 EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_buy_prismarine_bricks", ProfessionWrapper.MASON, MerchantLevel.EXPERT, "6x W15 1-10%-2xEMERALD for 8xPRISMARINE_BRICKS G10 EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_buy_dark_prismarine", ProfessionWrapper.MASON, MerchantLevel.EXPERT, "6x W15 1-10%-2xEMERALD for 1xDARK_PRISMARINE G10 EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("mason_buy_prismarine_lanterns", ProfessionWrapper.MASON, MerchantLevel.EXPERT, "6x W15 1-10%-2xEMERALD for 6xSEA_LANTERN G10 EXP=2400 ENCH=24 VIL=24 ORD=16"),
            new QuickTrade("mason_buy_glowstone", ProfessionWrapper.MASON, MerchantLevel.EXPERT, "6x W25 36-10%-64xEMERALD for 1xGLOWSTONE G10 EXP=3600 ENCH=36 VIL=36 ORD=16"),
            new QuickTrade("mason_buy_end_stone", ProfessionWrapper.MASON, MerchantLevel.MASTER, "6x 100% 1-10%-2xEMERALD for 16xEND_STONE G10 EXP=2400 ENCH=24 VIL=24 ORD=16"),
            new QuickTrade("mason_buy_purpur", ProfessionWrapper.MASON, MerchantLevel.MASTER, "E6x W25 1-10%-2xEMERALD for 16xPURPUR_BLOCK G10 EXP=3200 ENCH=32 VIL=32 ORD=16"),
            new QuickTrade("mason_buy_obsidian", ProfessionWrapper.MASON, MerchantLevel.MASTER, "E6x W25 2-10%-4xEMERALD for 4xOBSIDIAN G5 EXP=3200 ENCH=32 VIL=32 ORD=16"),
            new QuickTrade("mason_buy_light", ProfessionWrapper.MASON, MerchantLevel.MASTER, "E6x W25 8-10%-16xEMERALD for 2xLIGHT G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),
            new QuickTrade("mason_buy_reinforced_deepslate", ProfessionWrapper.MASON, MerchantLevel.MASTER, "E6x W25 16-10%-32xEMERALD for 2xREINFORCED_DEEPSLATE G5 EXP=1600 ENCH=16 VIL=16 ORD=16"),

            // TODO light placement and breaking mechanics

            new QuickTrade("shepherd_buy_shears", ProfessionWrapper.SHEPHERD, MerchantLevel.NOVICE, "6x 100% 2-10%-4xEMERALD for 1xSHEARS G25 EXP=100 ENCH=1 VIL=1 ORD=0 ITEMREF=shears_scaling"),
            new QuickTrade("shepherd_sell_white_wool", ProfessionWrapper.SHEPHERD, MerchantLevel.NOVICE, "6x W25 12-10%-24xWHITE_WOOL for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("shepherd_sell_gray_wool", ProfessionWrapper.SHEPHERD, MerchantLevel.NOVICE, "6x W25 12-10%-24xGRAY_WOOL for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("shepherd_sell_black_wool", ProfessionWrapper.SHEPHERD, MerchantLevel.NOVICE, "6x W25 12-10%-24xBLACK_WOOL for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("shepherd_sell_brown_wool", ProfessionWrapper.SHEPHERD, MerchantLevel.NOVICE, "6x W25 12-10%-24xBROWN_WOOL for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("shepherd_sell_random_dye_common", ProfessionWrapper.SHEPHERD, MerchantLevel.APPRENTICE, "6x 100% 10-10%-20xWHITE_DYE for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("shepherd_buy_random_dyed_wool", ProfessionWrapper.SHEPHERD, MerchantLevel.APPRENTICE, "6x W25 2-10%-4xEMERALD for 16xWHITE_WOOL G25 EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("shepherd_buy_random_dyed_wool", ProfessionWrapper.SHEPHERD, MerchantLevel.APPRENTICE, ""),
            new QuickTrade("shepherd_buy_random_dyed_wool", ProfessionWrapper.SHEPHERD, MerchantLevel.APPRENTICE, ""),
            new QuickTrade("shepherd_buy_random_dyed_wool", ProfessionWrapper.SHEPHERD, MerchantLevel.APPRENTICE, ""),
            new QuickTrade("shepherd_sell_random_dye_uncommon", ProfessionWrapper.SHEPHERD, MerchantLevel.JOURNEYMAN, "6x W10 8-10%-16xRED_DYE for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("shepherd_buy_random_dye_common", ProfessionWrapper.SHEPHERD, MerchantLevel.JOURNEYMAN, "6x W25 1-10%-2xEMERALD for 8xWHITE_DYE G25 EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("shepherd_buy_random_dye_common", ProfessionWrapper.SHEPHERD, MerchantLevel.JOURNEYMAN, ""),
            new QuickTrade("shepherd_buy_random_dye_uncommon", ProfessionWrapper.SHEPHERD, MerchantLevel.JOURNEYMAN, "6x W25 2-10%-4xEMERALD for 8xRED_DYE G25 EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("shepherd_buy_random_dye_uncommon", ProfessionWrapper.SHEPHERD, MerchantLevel.JOURNEYMAN, ""),
            new QuickTrade("shepherd_sell_random_dye_rare", ProfessionWrapper.SHEPHERD, MerchantLevel.EXPERT, "6x 100% 6-10%-12xBLUE_DYE for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=2 ORD=16"),
            new QuickTrade("shepherd_buy_random_banner", ProfessionWrapper.SHEPHERD, MerchantLevel.EXPERT, "6x W20 2-25%-4xEMERALD for 1xWHITE_BANNER G15 EXP=200 ENCH=2 VIL=2 ORD=0"),
            new QuickTrade("shepherd_buy_phoenix_banner", ProfessionWrapper.SHEPHERD, MerchantLevel.EXPERT, "6x W20 2-25%-4xEMERALD for 1xWHITE_BANNER G10 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=banner_phoenix"),
            new QuickTrade("shepherd_buy_hearts_banner", ProfessionWrapper.SHEPHERD, MerchantLevel.EXPERT, "6x W20 2-25%-4xEMERALD for 1xWHITE_BANNER G10 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=banner_hearts"),
            new QuickTrade("shepherd_buy_mountain_banner", ProfessionWrapper.SHEPHERD, MerchantLevel.EXPERT, "6x W20 2-25%-4xEMERALD for 1xWHITE_BANNER G10 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=banner_mountain"),
            new QuickTrade("shepherd_buy_dragon_banner", ProfessionWrapper.SHEPHERD, MerchantLevel.EXPERT, "6x W20 2-25%-4xEMERALD for 1xWHITE_BANNER G10 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=banner_dragon"),
            new QuickTrade("shepherd_buy_mojang_pattern", ProfessionWrapper.SHEPHERD, MerchantLevel.MASTER, "2x 100% 8-25%-16xEMERALD for 1xMOJANG_BANNER_PATTERN G5 EXP=3200 ENCH=32 VIL=32 ORD=16"),
            new QuickTrade("shepherd_buy_bird_banner", ProfessionWrapper.SHEPHERD, MerchantLevel.EXPERT, "E6x W25 2-25%-4xEMERALD for 1xWHITE_BANNER G10 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=banner_dragon"),
            new QuickTrade("shepherd_buy_sauron_banner", ProfessionWrapper.SHEPHERD, MerchantLevel.EXPERT, "E6x W25 2-25%-4xEMERALD for 1xWHITE_BANNER G10 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=banner_dragon"),
            new QuickTrade("shepherd_buy_arctic_fox_banner", ProfessionWrapper.SHEPHERD, MerchantLevel.EXPERT, "E6x W25 2-25%-4xEMERALD for 1xWHITE_BANNER G10 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=banner_dragon"),
            new QuickTrade("shepherd_buy_warden_banner", ProfessionWrapper.SHEPHERD, MerchantLevel.EXPERT, "E6x W25 2-25%-4xEMERALD for 1xWHITE_BANNER G10 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=banner_dragon"),

            new QuickTrade("generic_sell_coal", ProfessionWrapper.TOOLSMITH, MerchantLevel.NOVICE, ""),
            new QuickTrade("toolsmith_buy_stone_to_iron_axe_t1", ProfessionWrapper.TOOLSMITH, MerchantLevel.NOVICE, "3x W25 3-25%-6xEMERALD for 1xSTONE_AXE G25 EXP=200 ENCH=2 VIL=2 ORD=16 DYNREF=0:stone_axe_scaling|125:iron_pickaxe_scaling"),
            new QuickTrade("toolsmith_buy_stone_to_iron_shovel_t1", ProfessionWrapper.TOOLSMITH, MerchantLevel.NOVICE, "3x W25 1-25%-2xEMERALD for 1xSTONE_SHOVEL G25 EXP=200 ENCH=2 VIL=2 ORD=16 DYNREF=0:stone_shovel_scaling|125:iron_shovel_scaling"),
            new QuickTrade("toolsmith_buy_stone_to_iron_pickaxe_t1", ProfessionWrapper.TOOLSMITH, MerchantLevel.NOVICE, "3x W25 3-25%-6xEMERALD for 1xSTONE_PICKAXE G25 EXP=200 ENCH=2 VIL=2 ORD=16 DYNREF=0:stone_pickaxe_scaling|125:iron_pickaxe_scaling"),
            new QuickTrade("toolsmith_buy_stone_to_iron_hoe_t1", ProfessionWrapper.TOOLSMITH, MerchantLevel.NOVICE, "3x W25 2-25%-4xEMERALD for 1xSTONE_HOE G25 EXP=200 ENCH=2 VIL=2 ORD=16 DYNREF=0:stone_hoe_scaling|125:iron_hoe_scaling"),
            new QuickTrade("generic_sell_iron", ProfessionWrapper.TOOLSMITH, MerchantLevel.APPRENTICE, ""),
            new QuickTrade("generic_buy_bell", ProfessionWrapper.TOOLSMITH, MerchantLevel.APPRENTICE, ""),
            new QuickTrade("toolsmith_buy_stone_to_iron_axe_t2", ProfessionWrapper.TOOLSMITH, MerchantLevel.APPRENTICE, "3x W25 6-25%-12xEMERALD for 1xSTONE_AXE G15 EXP=200 ENCH=2 VIL=2 ORD=16 DYNREF=0:stone_axe_scaling|125:iron_pickaxe_scaling"),
            new QuickTrade("toolsmith_buy_stone_to_iron_shovel_t2", ProfessionWrapper.TOOLSMITH, MerchantLevel.APPRENTICE, "3x W25 2-25%-4xEMERALD for 1xSTONE_SHOVEL G15 EXP=200 ENCH=2 VIL=2 ORD=16 DYNREF=0:stone_shovel_scaling|125:iron_shovel_scaling"),
            new QuickTrade("toolsmith_buy_stone_to_iron_pickaxe_t2", ProfessionWrapper.TOOLSMITH, MerchantLevel.APPRENTICE, "3x W25 6-25%-12xEMERALD for 1xSTONE_PICKAXE G15 EXP=200 ENCH=2 VIL=2 ORD=16 DYNREF=0:stone_pickaxe_scaling|125:iron_pickaxe_scaling"),
            new QuickTrade("toolsmith_buy_stone_to_iron_hoe_t2", ProfessionWrapper.TOOLSMITH, MerchantLevel.APPRENTICE, "3x W25 4-25%-8xEMERALD for 1xSTONE_HOE G15 EXP=200 ENCH=2 VIL=2 ORD=16 DYNREF=0:stone_hoe_scaling|125:iron_hoe_scaling"),
            new QuickTrade("generic_sell_flint", ProfessionWrapper.TOOLSMITH, MerchantLevel.JOURNEYMAN, ""),
            new QuickTrade("toolsmith_buy_enchanted_iron_axe", ProfessionWrapper.TOOLSMITH, MerchantLevel.JOURNEYMAN, "3x W25 12-25%-24xEMERALD for 1xIRON_AXE G10 EXP=200 ENCH=2 VIL=2 ORD=16 ITEMREF=iron_axe_scaling"),
            new QuickTrade("toolsmith_buy_enchanted_iron_shovel", ProfessionWrapper.TOOLSMITH, MerchantLevel.JOURNEYMAN, "3x W25 4-25%-8xEMERALD for 1xIRON_SHOVEL G10 EXP=200 ENCH=2 VIL=2 ORD=16 ITEMREF=iron_shovel_scaling"),
            new QuickTrade("toolsmith_buy_enchanted_iron_pickaxe", ProfessionWrapper.TOOLSMITH, MerchantLevel.JOURNEYMAN, "3x W25 12-25%-24xEMERALD for 1xIRON_PICKAXE G10 EXP=200 ENCH=2 VIL=2 ORD=16 ITEMREF=iron_pickaxe_scaling"),
            new QuickTrade("toolsmith_buy_enchanted_iron_hoe", ProfessionWrapper.TOOLSMITH, MerchantLevel.JOURNEYMAN, "3x W25 8-25%-16xEMERALD for 1xIRON_HOE G10 EXP=200 ENCH=2 VIL=2 ORD=16 ITEMREF=iron_hoe_scaling"),
            new QuickTrade("generic_sell_diamond", ProfessionWrapper.TOOLSMITH, MerchantLevel.EXPERT, ""),
            new QuickTrade("toolsmith_buy_enchanted_diamond_axe", ProfessionWrapper.TOOLSMITH, MerchantLevel.EXPERT, "2x W25 24-25%-48xEMERALD for 1xDIAMOND_AXE G5 EXP=200 ENCH=2 VIL=2 ORD=0 ITEMREF=diamond_axe_scaling"),
            new QuickTrade("toolsmith_buy_enchanted_diamond_shovel", ProfessionWrapper.TOOLSMITH, MerchantLevel.EXPERT, "2x W25 8-25%-16xEMERALD for 1xDIAMOND_SHOVEL G5 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=diamond_shovel_scaling"),
            new QuickTrade("toolsmith_buy_enchanted_diamond_pickaxe", ProfessionWrapper.TOOLSMITH, MerchantLevel.EXPERT, "2x W25 24-25%-48xEMERALD for 1xDIAMOND_PICKAXE G5 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=diamond_pickaxe_scaling"),
            new QuickTrade("toolsmith_buy_enchanted_diamond_hoe", ProfessionWrapper.TOOLSMITH, MerchantLevel.EXPERT, "2x W25 16-25%-32xEMERALD for 1xDIAMOND_HOE G5 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=diamond_hoe_scaling"),
            new QuickTrade("generic_buy_netherite_scrap", ProfessionWrapper.TOOLSMITH, MerchantLevel.MASTER, ""),
            new QuickTrade("toolsmith_royal_diamond_axe", ProfessionWrapper.TOOLSMITH, MerchantLevel.MASTER, "E1x W25 9-25%-18xEMERALD_BLOCK for 1xDIAMOND_AXE NOGIFT EXP=3200 ENCH=32 VIL=32 ORD=16 ITEMREF=royal_diamond_axe"),
            new QuickTrade("toolsmith_royal_diamond_pickaxe", ProfessionWrapper.TOOLSMITH, MerchantLevel.MASTER, "E1x W25 3-25%-6xEMERALD_BLOCK for 1xDIAMOND_SHOVEL NOGIFT EXP=3200 ENCH=32 VIL=32 ORD=16 ITEMREF=royal_diamond_shovel"),
            new QuickTrade("toolsmith_royal_diamond_shovel", ProfessionWrapper.TOOLSMITH, MerchantLevel.MASTER, "E1x W25 9-25%-18xEMERALD_BLOCK for 1xDIAMOND_PICKAXE NOGIFT EXP=1600 ENCH=16 VIL=16 ORD=16 ITEMREF=royal_diamond_pickaxe"),
            new QuickTrade("toolsmith_royal_diamond_hoe", ProfessionWrapper.TOOLSMITH, MerchantLevel.MASTER, "E1x W25 6-25%-12xEMERALD_BLOCK for 1xDIAMOND_HOE NOGIFT EXP=1600 ENCH=16 VIL=16 ORD=16 ITEMREF=royal_diamond_hoe"),

            new QuickTrade("generic_sell_coal", ProfessionWrapper.WEAPONSMITH, MerchantLevel.NOVICE, ""),
            new QuickTrade("weaponsmith_buy_random_stone_to_iron_heavy_weapon_t1", ProfessionWrapper.WEAPONSMITH, MerchantLevel.NOVICE, "3x W25 5-25%-10xEMERALD for 1xSTONE_AXE G25 EXP=200 ENCH=2 VIL=2 ORD=16 DYNREF=0:random_stone_heavy_weapon|125:random_iron_heavy_weapon"),
            new QuickTrade("weaponsmith_buy_random_stone_to_iron_heavy_weapon_t1", ProfessionWrapper.WEAPONSMITH, MerchantLevel.NOVICE, ""),
            new QuickTrade("weaponsmith_buy_random_stone_to_iron_light_weapon_t1", ProfessionWrapper.WEAPONSMITH, MerchantLevel.NOVICE, "3x W25 5-25%-10xEMERALD for 1xSTONE_SWORD G25 EXP=200 ENCH=2 VIL=2 ORD=16 DYNREF=0:random_stone_light_weapon|125:random_iron_light_weapon"),
            new QuickTrade("weaponsmith_buy_random_stone_to_iron_light_weapon_t1", ProfessionWrapper.WEAPONSMITH, MerchantLevel.NOVICE, ""),
            new QuickTrade("generic_sell_iron", ProfessionWrapper.WEAPONSMITH, MerchantLevel.APPRENTICE, ""),
            new QuickTrade("generic_buy_bell", ProfessionWrapper.WEAPONSMITH, MerchantLevel.APPRENTICE, ""),
            new QuickTrade("weaponsmith_buy_random_stone_to_iron_heavy_weapon_t2", ProfessionWrapper.WEAPONSMITH, MerchantLevel.APPRENTICE, "3x W25 8-25%-16xEMERALD for 1xSTONE_AXE G15 EXP=200 ENCH=2 VIL=2 ORD=0 DYNREF=0:random_stone_heavy_weapon|125:random_iron_heavy_weapon"),
            new QuickTrade("weaponsmith_buy_random_stone_to_iron_heavy_weapon_t2", ProfessionWrapper.WEAPONSMITH, MerchantLevel.APPRENTICE, ""),
            new QuickTrade("weaponsmith_buy_random_stone_to_iron_light_weapon_t2", ProfessionWrapper.WEAPONSMITH, MerchantLevel.APPRENTICE, "3x W25 8-25%-16xEMERALD for 1xSTONE_SWORD G15 EXP=200 ENCH=2 VIL=2 ORD=0 DYNREF=0:random_stone_light_weapon|125:random_iron_light_weapon"),
            new QuickTrade("weaponsmith_buy_random_stone_to_iron_light_weapon_t2", ProfessionWrapper.WEAPONSMITH, MerchantLevel.APPRENTICE, ""),
            new QuickTrade("generic_sell_flint", ProfessionWrapper.WEAPONSMITH, MerchantLevel.JOURNEYMAN, ""),
            new QuickTrade("weaponsmith_buy_random_enchanted_iron_heavy_weapon", ProfessionWrapper.WEAPONSMITH, MerchantLevel.JOURNEYMAN, "3x W25 12-25%-24xEMERALD for 1xIRON_AXE G10 EXP=600 ENCH=6 VIL=6 ORD=16 ITEMREF=random_iron_heavy_weapon"),
            new QuickTrade("weaponsmith_buy_random_enchanted_iron_heavy_weapon", ProfessionWrapper.WEAPONSMITH, MerchantLevel.JOURNEYMAN, ""),
            new QuickTrade("weaponsmith_buy_random_enchanted_iron_light_weapon", ProfessionWrapper.WEAPONSMITH, MerchantLevel.JOURNEYMAN, "3x W25 12-25%-24xEMERALD for 1xIRON_SWORD G10 EXP=600 ENCH=6 VIL=6 ORD=16 ITEMREF=random_iron_light_weapon"),
            new QuickTrade("weaponsmith_buy_random_enchanted_iron_light_weapon", ProfessionWrapper.WEAPONSMITH, MerchantLevel.JOURNEYMAN, ""),
            new QuickTrade("generic_sell_diamond", ProfessionWrapper.WEAPONSMITH, MerchantLevel.EXPERT, ""),
            new QuickTrade("weaponsmith_buy_random_enchanted_diamond_heavy_weapon", ProfessionWrapper.WEAPONSMITH, MerchantLevel.EXPERT, "2x W25 32-25%-64xEMERALD for 1xDIAMOND_AXE G5 EXP=200 ENCH=2 VIL=2 ORD=0 ITEMREF=random_diamond_heavy_weapon"),
            new QuickTrade("weaponsmith_buy_random_enchanted_diamond_heavy_weapon", ProfessionWrapper.WEAPONSMITH, MerchantLevel.EXPERT, ""),
            new QuickTrade("weaponsmith_buy_random_enchanted_diamond_light_weapon", ProfessionWrapper.WEAPONSMITH, MerchantLevel.EXPERT, "2x W25 32-25%-64xEMERALD for 1xDIAMOND_SWORD G5 EXP=800 ENCH=8 VIL=8 ORD=16 ITEMREF=random_diamond_light_weapon"),
            new QuickTrade("weaponsmith_buy_random_enchanted_diamond_light_weapon", ProfessionWrapper.WEAPONSMITH, MerchantLevel.EXPERT, ""),
            new QuickTrade("generic_buy_netherite_scrap", ProfessionWrapper.WEAPONSMITH, MerchantLevel.MASTER, ""),
            new QuickTrade("weaponsmith_buy_royal_diamond_sword", ProfessionWrapper.WEAPONSMITH, MerchantLevel.MASTER, "E1x W50 12-25%-24xEMERALD for 1xDIAMOND_SWORD G5 EXP=3200 ENCH=32 VIL=32 ORD=16 ITEMREF=royal_diamond_sword"),
            new QuickTrade("weaponsmith_buy_royal_diamond_axe", ProfessionWrapper.WEAPONSMITH, MerchantLevel.MASTER, "E1x W50 12-25%-24xEMERALD for 1xDIAMOND_AXE G5 EXP=3200 ENCH=32 VIL=32 ORD=16 ITEMREF=royal_diamond_axe")
    };

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        if (!e.getPlayer().getName().equalsIgnoreCase("athlaeos") && !e.getPlayer().getName().equalsIgnoreCase("altlaeos")) return;
        if (e.getMessage().startsWith("runthing"))
            run();
    }

    private static final Map<ProfessionWrapper, Map<MerchantLevel, Integer>> typeCounter = new HashMap<>();

    public static void run(){
        for (ProfessionWrapper profession : ProfessionWrapper.values()){
            String id = String.format("%s_simple", profession.toString().toLowerCase());
            if (CustomMerchantManager.getMerchantType(id) == null) {
                CustomMerchantManager.registerMerchantType(new MerchantType(id));
                CustomMerchantManager.getMerchantConfiguration(profession.getProfession()).getMerchantTypes().add(id);
            }
        }

        for (QuickTrade trade : args){
            Map<MerchantLevel, Integer> counter = typeCounter.getOrDefault(trade.profession, new HashMap<>());
            int prio = counter.getOrDefault(trade.level, 0);
            counter.put(trade.level, prio + 1);
            typeCounter.put(trade.profession, counter);
            String id = String.format("%s_simple", trade.profession.toString().toLowerCase());
            MerchantTrade t = parse(trade.id, trade.arg);
            t.setPriority(prio);
            CustomMerchantManager.registerTrade(t);

            MerchantType type = CustomMerchantManager.getMerchantType(id);
            type.addTrade(trade.level, t);
        }

        for (DynamicGridRecipe recipe : CustomRecipeRegistry.getGridRecipes().values()){
            if (!isGoodRecipe(recipe.getName())) continue;
            System.out.println("converting recipe " + recipe.getName());
            if (!recipe.getName().contains("arrow")) {
                DynamicItemModifier qualitySetModifier = recipe.getModifiers().stream().filter(m -> m instanceof SmithingQualityScale).map(DynamicItemModifier::copy).findFirst().orElse(null);
                if (qualitySetModifier == null) {
                    System.out.println(recipe.getName() + " did not have quality scaling");
                    continue;
                }
                qualitySetModifier.setPriority(ModifierPriority.SOONEST);
                // not scaling
                String scalingItemID = String.format("%s_scaling", recipe.getName().replace("craft_", ""));
                CustomItem scalingItem = CustomItemRegistry.getItem(scalingItemID);
                if (scalingItem == null) scalingItem = new CustomItem(scalingItemID, new ItemStack(Material.BARRIER));
                scalingItem.setItem(recipe.getResult());

                List<DynamicItemModifier> filteredRecipeModifiers = new ArrayList<>(recipe.getModifiers().stream().map(DynamicItemModifier::copy).toList());
                filteredRecipeModifiers.removeIf(m -> m instanceof SkillExperience || m instanceof SmithingQualityScale);
                scalingItem.setModifiers(filteredRecipeModifiers);

                List<DynamicItemModifier> attributeScalingAndExperienceModifiers = new ArrayList<>(recipe.getModifiers().stream().filter(m -> m instanceof DefaultAttributeScale || m instanceof DurabilityScale || m instanceof SkillExperience).map(DynamicItemModifier::copy).toList());
                attributeScalingAndExperienceModifiers.add(qualitySetModifier);
                ItemReplaceByIndexed modifier = (ItemReplaceByIndexed) ModifierRegistry.createModifier("replace_by_custom");
                modifier.setItem(scalingItemID);
                modifier.setPriority(ModifierPriority.SOON);
                attributeScalingAndExperienceModifiers.add(modifier);
                recipe.setModifiers(attributeScalingAndExperienceModifiers);
            }
        }
    }

    private static boolean isGoodRecipe(String name){
        return name.startsWith("craft_wooden_") ||
                name.startsWith("craft_leather_") ||
                name.startsWith("craft_stone_") ||
                name.startsWith("craft_chainmail_") ||
                name.startsWith("craft_iron_") ||
                name.startsWith("craft_gold_") ||
                name.startsWith("craft_diamond_");
    }

    private static MerchantTrade parse(String name, String arg){
        MerchantTrade existing = CustomMerchantManager.getTrade(name);
        if (existing != null) return existing;

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

        boolean isBuyTrade = args[2].contains("EMERALD");
        int multiplier = (args[2].contains("EMERALD_BLOCK") ? 10 : 1) * (args[2].contains("EMERALD") ? priceLowest + 1 : -1);
        float giftWeight = args[5].equalsIgnoreCase("NOGIFT") ? 0 : Float.parseFloat(args[5].replace("G", ""));
        float skillExp = multiplier > 1 ? multiplier * 100 : Float.parseFloat(args[6].replace("EXP=", ""));
        float enchantingExp = multiplier > 1 ? multiplier : Float.parseFloat(args[7].replace("ENCH=", ""));
        int merchantExp = multiplier > 1 ? multiplier : Integer.parseInt(args[8].replace("VIL=", ""));
        String itemRef = args.length > 10 && args[10].startsWith("ITEMREF=") ? args[10].replaceFirst("ITEMREF=", "") : null;
        int orderCount = 0;
        if (isBuyTrade) orderCount = 2 * quant;

        Map<Integer, String> dynRefs = new HashMap<>();
        if (args.length > 10 && args[10].startsWith("DYNREF=")) {
            String[] split = args[10].replace("DYNREF=", "").split("/");
            for (String s : split){
                String[] a = s.split(":");
                dynRefs.put(Integer.parseInt(a[0]), a[1]);
            }
        }

        MerchantTrade trade = new MerchantTrade(name);
        trade.setMaxUses(quant);
        trade.setSkillExp(skillExp);
        trade.setMaxOrderCount(orderCount);

        int negativeOffset = Math.round(priceLowest * -0.2F);
        int positiveOffset = Math.round(priceLowest * 0.2F);
        trade.setPriceRandomNegativeOffset(negativeOffset);
        trade.setPriceRandomPositiveOffset(positiveOffset);
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

        if (itemRef != null){
            if (CustomItemRegistry.getItem(itemRef) == null) CustomItemRegistry.register(itemRef, new ItemStack(Material.BARRIER));
            ItemReplaceByIndexed modifier = (ItemReplaceByIndexed) ModifierRegistry.createModifier("replace_by_custom");
            modifier.setItem(itemRef);
            modifier.setPriority(ModifierPriority.NEUTRAL);
            trade.getModifiers().add(modifier);
        } else if (!dynRefs.isEmpty()){
            ItemReplaceByIndexedBasedOnQuality modifier = (ItemReplaceByIndexedBasedOnQuality) ModifierRegistry.createModifier("replace_by_custom_quality_based");
            for (Integer quality : dynRefs.keySet()){
                String item = dynRefs.get(quality);
                if (CustomItemRegistry.getItem(item) == null) CustomItemRegistry.register(item, new ItemStack(Material.BARRIER));
            }
            modifier.setItems(dynRefs);
            modifier.setPriority(ModifierPriority.NEUTRAL);
            trade.getModifiers().add(modifier);
        }

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
