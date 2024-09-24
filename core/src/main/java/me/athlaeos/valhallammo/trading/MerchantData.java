package me.athlaeos.valhallammo.trading;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class MerchantData {
    private final Map<Integer, TradeData> trades = new HashMap<>();

    private class TradeData{
        private final String id;
        private final ItemStack item;
        private final double maxSales;
        private final double salesLeft;

        public TradeData(String id, ItemStack item, double maxSales, double salesLeft){
            this.id = id;
            this.item = item;
            this.maxSales = maxSales;
            this.salesLeft = salesLeft;
        }
    }
}
