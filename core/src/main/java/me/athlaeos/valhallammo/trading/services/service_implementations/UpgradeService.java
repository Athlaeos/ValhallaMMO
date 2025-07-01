package me.athlaeos.valhallammo.trading.services.service_implementations;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.trading.dom.MerchantLevel;
import me.athlaeos.valhallammo.trading.services.Service;
import me.athlaeos.valhallammo.trading.services.ServiceRegistry;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class UpgradeService extends Service {
    private SlotEntry input = new SlotEntry(new ItemStack(Material.IRON_SWORD), new MaterialChoice()); // the target item to upgrade
    private SlotEntry cost = new SlotEntry(new ItemStack(Material.EMERALD), new MaterialChoice()); // the cost of the upgrade
    private List<DynamicItemModifier> modifiers = new ArrayList<>(); // the modifiers to apply on the input
    private ItemStack upgradeIcon = new ItemBuilder(Material.PAPER).name("&cReplace me!").lore("&7I'm just a placeholder icon!",
            "&eWhatever you want the upgrade", "&eicon to look like, put it here", "", "&cUse %item%, %target%, or %quantity%",
            "&cto substitute the item cost, item",
            "&cinput, and cost amount").get(); // the icon to use for the upgrade
    private final Collection<MerchantLevel> appearanceLevels = new HashSet<>(Set.of(MerchantLevel.NOVICE, MerchantLevel.APPRENTICE, MerchantLevel.JOURNEYMAN, MerchantLevel.EXPERT, MerchantLevel.MASTER)); // merchant levels at which this upgrade may appear
    private double skillExp = 500; // the trading skill experience the player gets for every `cost` they spend

    public UpgradeService(String id) {
        super(id, ServiceRegistry.SERVICE_TYPE_UPGRADING);
    }

    public void setCost(SlotEntry cost) { this.cost = cost; }
    public void setSkillExp(double skillExp) { this.skillExp = skillExp; }
    public void setInput(SlotEntry input) { this.input = input; }
    public void setModifiers(List<DynamicItemModifier> modifiers) { this.modifiers = modifiers; DynamicItemModifier.sortModifiers(this.modifiers); }
    public void setUpgradeIcon(ItemStack upgradeIcon) { this.upgradeIcon = upgradeIcon; }

    public SlotEntry getCost() { return cost; }
    public double getSkillExp() { return skillExp; }
    public List<DynamicItemModifier> getModifiers() { return modifiers; }
    public Collection<MerchantLevel> getAppearanceLevels() { return appearanceLevels; }
    public ItemStack getUpgradeIcon() { return upgradeIcon; }
    public SlotEntry getInput() { return input; }
}
