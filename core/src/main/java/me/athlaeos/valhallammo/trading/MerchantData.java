package me.athlaeos.valhallammo.trading;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class MerchantData {
    private final Map<Integer, TradeData> trades = new HashMap<>(); // represents a

    private class TradeData{
        private final ItemStack item;
        private final ItemStack scalingCostItem;
        private final ItemStack optionalCostItem;
        private final double maxSales;
        private final double salesLeft;

        public TradeData(ItemStack item, ItemStack scalingCostItem, ItemStack optionalCostItem, double maxSales, double salesLeft){
            this.item = item;
            this.scalingCostItem = scalingCostItem;
            this.optionalCostItem = optionalCostItem;
            this.maxSales = maxSales;
            this.salesLeft = salesLeft;
        }
    }
}
