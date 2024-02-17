package me.athlaeos.valhallammo.loot.predicates;

import me.athlaeos.valhallammo.loot.LootTable;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.Map;
import java.util.function.Predicate;

public abstract class LootPredicate implements Predicate<LootContext> {
    protected boolean inverted = false;

    public boolean isInverted() {
        return inverted;
    }

    public abstract String getKey();
    public abstract Material getIcon();

    public abstract String getDisplayName();
    public abstract String getDescription();
    public abstract String getActiveDescription();
    public boolean isCompatibleWithLootType(LootTable.LootType type){
        return true;
    }

    public abstract LootPredicate createNew();

    /**
     * Up to 25 buttons are allowed to be registered. These buttons should configure the predicate using onButtonPress.<br>
     * The key should represent the position of the button in a 5x5 grid, just like with DynamicItemModifiers!
     * @return a map with buttons
     */
    public abstract Map<Integer, ItemStack> getButtons();
    public abstract void onButtonPress(InventoryClickEvent e, int button);
}
