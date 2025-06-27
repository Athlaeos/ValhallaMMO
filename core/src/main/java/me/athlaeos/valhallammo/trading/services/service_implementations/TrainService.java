package me.athlaeos.valhallammo.trading.services.service_implementations;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantLevel;
import me.athlaeos.valhallammo.trading.services.Service;
import me.athlaeos.valhallammo.trading.services.ServiceRegistry;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainService extends Service {
    private String skillToLevel = "SMITHING";
    private final Map<MerchantLevel, Integer> limitPerLevel = new HashMap<>(Map.of(
            MerchantLevel.NOVICE, 15,
            MerchantLevel.APPRENTICE, 30,
            MerchantLevel.JOURNEYMAN, 45,
            MerchantLevel.EXPERT, 60,
            MerchantLevel.MASTER, 75
    ));
    private double expStep = 750; // every `expStep` experience required to level up, an additional `cost` is required
    private SlotEntry cost = new SlotEntry(new ItemStack(Material.EMERALD), new MaterialChoice());
    private double skillExpPerCost = 100; // the trading skill experience the player gets for every `cost` they spend
    private int primaryButtonPosition = 3;
    private int rows = 3;
    private List<Integer> secondaryButtonPositions = new ArrayList<>(List.of(3, 4, 5, 12, 13, 14, 21, 22, 23));
    private ItemStack primaryButton = new ItemBuilder(ItemUtils.parseCustomModelItem(CustomMerchantManager.getTradingConfig().getString("service_button_buy_training_default_type", "LIME_DYE:9199500"), Material.LIME_DYE))
            .name(CustomMerchantManager.getTradingConfig().getString("service_button_buy_training_name"))
            .lore(CustomMerchantManager.getTradingConfig().getStringList("service_button_buy_training_description"))
            .get();

    public TrainService(String id) {
        super(id, ServiceRegistry.SERVICE_TYPE_TRAINING);
    }

    public void setSkillToLevel(String skillToLevel) { this.skillToLevel = skillToLevel; }
    public void setExpStep(double expStep) { this.expStep = expStep; }
    public void setCost(SlotEntry cost) { this.cost = cost; }
    public void setSkillExpPerCost(double skillExpPerCost) { this.skillExpPerCost = skillExpPerCost; }
    public void setPrimaryButtonPosition(int primaryButtonPosition) { this.primaryButtonPosition = primaryButtonPosition; }
    public void setSecondaryButtonPositions(List<Integer> secondaryButtonPositions) { this.secondaryButtonPositions = secondaryButtonPositions; }
    public void setPrimaryButton(ItemStack primaryButton) { this.primaryButton = primaryButton; }
    public void setRows(int rows) { this.rows = rows; }

    public String getSkillToLevel() { return skillToLevel; }
    public Map<MerchantLevel, Integer> getLimitPerLevel() { return limitPerLevel; }
    public double getExpStep() { return expStep; }
    public SlotEntry getCost() { return cost; }
    public double getSkillExpPerCost() { return skillExpPerCost; }
    public int getPrimaryButtonPosition() { return primaryButtonPosition; }
    public List<Integer> getSecondaryButtonPositions() { return secondaryButtonPositions; }
    public ItemStack getPrimaryButton() { return primaryButton; }
    public int getRows() { return rows; }
}
