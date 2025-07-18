package me.athlaeos.valhallammo.hooks;

import com.github.sachin.lootin.api.LootinInventoryOpenEvent;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.event.ValhallaLootPopulateEvent;
import me.athlaeos.valhallammo.event.ValhallaLootReplacementEvent;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.loot.ReplacementTable;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.Lootable;

import java.util.*;

public class LootinHook extends PluginHook implements Listener {
    public LootinHook() {
        super("Lootin");
    }

    @Override
    public void whenPresent() {
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    private static final Map<UUID, LootTable> preparedCustomTables = new HashMap<>();
    private static final Map<UUID, LootContext> preparedContexts = new HashMap<>();
    private static final Map<UUID, org.bukkit.loot.LootTable> preparedVanillaTables = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLootinRefill(LootinInventoryOpenEvent e){
        Lootable l = e.getLootable();
        if (!e.isRefill() || l == null || ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.isCancelled()) return;

        int originalSize = e.getItems().size();
        LootTable table = preparedCustomTables.get(e.getPlayer().getUniqueId());
        LootContext context = preparedContexts.get(e.getPlayer().getUniqueId());
        org.bukkit.loot.LootTable vanillaTable = preparedVanillaTables.get(e.getPlayer().getUniqueId());
        preparedCustomTables.remove(e.getPlayer().getUniqueId());
        preparedContexts.remove(e.getPlayer().getUniqueId());
        preparedVanillaTables.remove(e.getPlayer().getUniqueId());
        if (context == null) return;

        if (table != null) {
            // LootTableRegistry.setLootTable(b, null);
            List<ItemStack> loot = LootTableRegistry.getLoot(table, context, LootTable.LootType.CONTAINER);
            ValhallaLootPopulateEvent loottableEvent = new ValhallaLootPopulateEvent(table, context, loot);
            ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(loottableEvent);
            if (!loottableEvent.isCancelled()){
                boolean skip = false;
                switch (loottableEvent.getPreservationType()){
                    case CLEAR -> {
                        e.setItems(new ArrayList<>());
                        for (int i = 0; i < originalSize; i++) e.getItems().add(null);
                    }
                    case CLEAR_UNLESS_EMPTY -> {
                        if (!loottableEvent.getDrops().isEmpty()) {
                            e.setItems(new ArrayList<>());
                            for (int i = 0; i < originalSize; i++) e.getItems().add(null);
                        }
                    }
                    case KEEP -> {
                        if (loottableEvent.getDrops().isEmpty()) skip = true;
                    }
                }
                if (!skip){
                    List<ItemStack> drops = new ArrayList<>(loottableEvent.getDrops());
                    drops.addAll(e.getItems());
                    drops.removeIf(Objects::isNull);
                    for (int i = 0; i < originalSize - drops.size(); i++) drops.add(null);
                    drops = new ArrayList<>(drops.stream().limit(originalSize).toList());
                    Collections.shuffle(drops);
                    e.setItems(drops);
                }
            }
        }

        ReplacementTable replacementTable = vanillaTable == null ? null : LootTableRegistry.getReplacementTable(vanillaTable.getKey());
        ReplacementTable globalTable = LootTableRegistry.getGlobalReplacementTable();
        ValhallaLootReplacementEvent event = new ValhallaLootReplacementEvent(replacementTable, context);
        if (replacementTable != null) ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
        if (replacementTable == null || !event.isCancelled()){
            for (int i = 0; i < e.getItems().size(); i++){
                ItemStack item = e.getItems().get(i);
                if (ItemUtils.isEmpty(item)) continue;
                ItemStack replacement = LootTableRegistry.getReplacement(replacementTable, context, LootTable.LootType.CONTAINER, item);
                if (!ItemUtils.isEmpty(replacement)) item = replacement;
                ItemStack globalReplacement = LootTableRegistry.getReplacement(globalTable, context, LootTable.LootType.CONTAINER, item);
                if (!ItemUtils.isEmpty(globalReplacement)) item = globalReplacement;
                if (!ItemUtils.isEmpty(item)) e.getItems().set(i, item);
            }
        }
    }

    public static void prepareChestOpeningForLootin(Player player, org.bukkit.loot.LootTable vanillaTable, LootTable table, LootContext context){
        preparedCustomTables.put(player.getUniqueId(), table);
        preparedContexts.put(player.getUniqueId(), context);
        preparedVanillaTables.put(player.getUniqueId(), vanillaTable);
    }
}
