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
            new QuickTrade("armorer_sell_diamond", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "12x 100% 1-10%-2xDIAMOND for 1xEMERALD NOGIFT EXP=200 ENCH=2 VIL=3"),
            new QuickTrade("armorer_diamond_helmet", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "2x W25 25-25%-40xDIAMOND for 1xDIAMOND_HELMET G5 EXP=1600 ENCH=32 VIL=12"),
            new QuickTrade("armorer_diamond_chestplate", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "2x W25 40-25%-64xEMERALD for 1xDIAMOND_CHESTPLATE G5 EXP=1600 ENCH=32 VIL=12"),
            new QuickTrade("armorer_diamond_leggings", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "2x W25 35-25%-54xEMERALD for 1xDIAMOND_LEGGINGS G5 EXP=1600 ENCH=32 VIL=12"),
            new QuickTrade("armorer_diamond_boots", ProfessionWrapper.ARMORER, MerchantLevel.EXPERT, "2x W25 20-25%-32xEMERALD for 1xDIAMOND_BOOTS G5 EXP=1600 ENCH=32 VIL=12"),
            new QuickTrade("armorer_buy_netherite_scrap", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "1x 100% 32-25%-64xEMERALD for 1xNETHERITE_SCRAP NOGIFT EXP=1600 ENCH=32 VIL=12"),
            new QuickTrade("armorer_royal_diamond_helmet", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "E1x W25 10-0%-10xEMERALD_BLOCK for 1xDIAMOND_HELMET NOGIFT EXP=3200 ENCH=50 VIL=32"),
            new QuickTrade("armorer_royal_diamond_chestplate", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "E1x W25 16-0%-16xEMERALD_BLOCK for 1xDIAMOND_CHESTPLATE NOGIFT EXP=3200 ENCH=50 VIL=32"),
            new QuickTrade("armorer_royal_diamond_leggings", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "E1x W25 14-0%-14xEMERALD_BLOCK for 1xDIAMOND_LEGGINGS NOGIFT EXP=3200 ENCH=50 VIL=32"),
            new QuickTrade("armorer_royal_diamond_boots", ProfessionWrapper.ARMORER, MerchantLevel.MASTER, "E1x W25 8-0%-8xEMERALD_BLOCK for 1xDIAMOND_BOOTS NOGIFT EXP=3200 ENCH=50 VIL=32")
    };

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
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

        float giftWeight = args[5].equalsIgnoreCase("nogift") ? 0 : Float.parseFloat(args[5]);
        float skillExp = Float.parseFloat(args[5].replace("EXP=", ""));
        float enchantingExp = Float.parseFloat(args[6].replace("ENCH=", ""));
        int merchantExp = Integer.parseInt(args[7].replace("VIL=", ""));

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
