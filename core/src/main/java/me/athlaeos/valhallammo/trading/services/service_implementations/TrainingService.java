package me.athlaeos.valhallammo.trading.services.service_implementations;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.trading.dom.MerchantLevel;
import me.athlaeos.valhallammo.trading.services.Service;
import me.athlaeos.valhallammo.trading.services.ServiceRegistry;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainingService extends Service {
    private String skillToLevel = "SMITHING";
    private final Map<MerchantLevel, Integer> limitPerLevel = new HashMap<>(Map.of(
            MerchantLevel.NOVICE, 15,
            MerchantLevel.APPRENTICE, 30,
            MerchantLevel.JOURNEYMAN, 45,
            MerchantLevel.EXPERT, 60,
            MerchantLevel.MASTER, 75
    ));
    private double expStep = 1000; // every `expStep` experience required to level up, an additional `cost` is required
    private ItemStack cost = new ItemStack(Material.EMERALD);
    private IngredientChoice costChoice = new MaterialChoice();
    private double skillExpPerCost = 100; // the trading skill experience the player gets for every `cost` they spend
    private int buttonMainPosition = 12;
    private List<Integer> buttonFunctionalPositions = new ArrayList<>(List.of(12, 13, 14, 21, 22, 23, 30, 31, 32));
    private ItemStack button = new ItemBuilder(Material.LIME_DYE)
            .name("&fTrain %skill%")
            .lore("&7Spend &aemeralds &7to gain",
                    "&7%skill% &7experience, up to &eLV%maxlevel%")
            .data(9199500)
            .get();

    public TrainingService(String id) {
        super(id, ServiceRegistry.SERVICE_TYPE_ORDERING);
    }

    public void setSkillToLevel(String skillToLevel) { this.skillToLevel = skillToLevel; }
    public void setExpStep(double expStep) { this.expStep = expStep; }
    public void setCost(ItemStack cost) { this.cost = cost; }
    public void setCostChoice(IngredientChoice costChoice) { this.costChoice = costChoice; }
    public void setSkillExpPerCost(double skillExpPerCost) { this.skillExpPerCost = skillExpPerCost; }
    public void setButtonMainPosition(int buttonMainPosition) { this.buttonMainPosition = buttonMainPosition; }
    public void setButtonFunctionalPositions(List<Integer> buttonFunctionalPositions) { this.buttonFunctionalPositions = buttonFunctionalPositions; }
    public void setButton(ItemStack button) { this.button = button; }

    public String getSkillToLevel() { return skillToLevel; }
    public Map<MerchantLevel, Integer> getLimitPerLevel() { return limitPerLevel; }
    public double getExpStep() { return expStep; }
    public ItemStack getCost() { return cost; }
    public IngredientChoice getCostChoice() { return costChoice; }
    public double getSkillExpPerCost() { return skillExpPerCost; }
    public int getButtonMainPosition() { return buttonMainPosition; }
    public List<Integer> getButtonFunctionalPositions() { return buttonFunctionalPositions; }
    public ItemStack getButton() { return button; }
}
