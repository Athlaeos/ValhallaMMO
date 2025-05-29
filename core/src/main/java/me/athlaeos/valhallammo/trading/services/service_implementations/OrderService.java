package me.athlaeos.valhallammo.trading.services.service_implementations;

import me.athlaeos.valhallammo.trading.services.Service;
import me.athlaeos.valhallammo.trading.services.ServiceRegistry;

public class OrderService extends Service {
    private long baseOrderTime = 12000;
    private long orderTimeBonusPerItem = 500;
    private long orderTimeBonusPerTrade = 12000;
    private int bulkMinimumOrdersForDiscount = 10;
    private float bulkDiscountPerItem = 0.01F;
    private float bulkMaxDiscount = 0.2F;

    public OrderService(String id) {
        super(id, ServiceRegistry.SERVICE_TYPE_ORDERING);
    }

    public long getBaseOrderTime() { return baseOrderTime; }
    public long getOrderTimeBonusPerItem() { return orderTimeBonusPerItem; }
    public long getOrderTimeBonusPerTrade() { return orderTimeBonusPerTrade; }
    public int getBulkMinimumOrdersForDiscount() { return bulkMinimumOrdersForDiscount; }
    public float getBulkDiscountPerItem() { return bulkDiscountPerItem; }
    public float getBulkMaxDiscount() { return bulkMaxDiscount; }

    public void setBaseOrderTime(long baseOrderTime) { this.baseOrderTime = baseOrderTime; }
    public void setOrderTimeBonusPerItem(long orderTimeBonusPerItem) { this.orderTimeBonusPerItem = orderTimeBonusPerItem; }
    public void setOrderTimeBonusPerTrade(long orderTimeBonusPerTrade) { this.orderTimeBonusPerTrade = orderTimeBonusPerTrade; }
    public void setBulkMinimumOrdersForDiscount(int bulkMinimumOrdersForDiscount) { this.bulkMinimumOrdersForDiscount = bulkMinimumOrdersForDiscount; }
    public void setBulkDiscountPerItem(float bulkDiscountPerItem) { this.bulkDiscountPerItem = bulkDiscountPerItem; }
    public void setBulkMaxDiscount(float bulkMaxDiscount) { this.bulkMaxDiscount = bulkMaxDiscount; }
}
