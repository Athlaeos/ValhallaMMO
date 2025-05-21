package me.athlaeos.valhallammo.hooks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.event.ValhallaLootPopulateEvent;
import me.athlaeos.valhallammo.event.ValhallaLootReplacementEvent;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.loot.ReplacementTable;
import me.athlaeos.valhallammo.loot.LootTable.VanillaLootPreservationType;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.lauriichan.spigot.justlootit.api.event.player.AsyncJLIPlayerLootProvidedEvent;
import me.lauriichan.spigot.justlootit.api.event.player.AsyncJLIPlayerVanillaLootProvidedEvent;

public class JustLootItHook extends PluginHook {

    private Listener listener;
    
    public JustLootItHook() {
        super("JustLootIt");
    }

    @Override
    public void whenPresent() {
        listener = new Listener() {
            @EventHandler(ignoreCancelled = true)
            public void onLootProvided(AsyncJLIPlayerLootProvidedEvent event) {
                if (ValhallaMMO.isWorldBlacklisted(event.entryLocation().getWorld().getName())) {
                    return;
                }
                LootTable lootTable = null;
                Block block;
                if (event.entryHolder() instanceof BlockState blockState) {
                    lootTable = LootTableRegistry.getLootTable(block = blockState.getBlock(), blockState.getType());
                } else {
                    block = null;
                }
                ReplacementTable replacementTable;
                if (lootTable == null && event instanceof AsyncJLIPlayerVanillaLootProvidedEvent vanillaEvent) {
                    lootTable = LootTableRegistry.getLootTable(vanillaEvent.lootTable().getKey());
                    replacementTable = LootTableRegistry.getReplacementTable(vanillaEvent.lootTable().getKey());
                } else {
                    replacementTable = null;
                }
                Player player = event.player().asBukkit();
                AttributeInstance luckInstance = player.getAttribute(Attribute.GENERIC_LUCK);
                double luck = luckInstance == null ? 0 : luckInstance.getValue();
                LootContext context = new LootContext.Builder(event.entryLocation()).killer(null).lootedEntity(player).lootingModifier(0).luck((float) luck).build();
                Inventory inventory = event.bukkitInventory();
                if (lootTable != null) {
                    if (block != null) {
                        event.scheduler().sync(() -> LootTableRegistry.setLootTable(block, null));
                    }
                    LootTable fLootTable = lootTable;
                    List<ItemStack> loot = event.scheduler().sync(() -> LootTableRegistry.getLoot(fLootTable, context, LootTable.LootType.CONTAINER)).join();
                    ValhallaLootPopulateEvent lootTableEvent = new ValhallaLootPopulateEvent(lootTable, context, loot);
                    event.scheduler().sync(() -> Bukkit.getPluginManager().callEvent(lootTableEvent)).join();
                    if (!lootTableEvent.isCancelled()) {
                        boolean skip = false;
                        if (lootTableEvent.getPreservationType() == VanillaLootPreservationType.CLEAR || lootTableEvent.getPreservationType() == VanillaLootPreservationType.CLEAR_UNLESS_EMPTY && !lootTableEvent.getDrops().isEmpty()) {
                            inventory.clear();
                        } else if (lootTableEvent.getDrops().isEmpty()) {
                            skip = true;
                        }
                        if (!skip) {
                            List<ItemStack> drops = new ArrayList<>(lootTableEvent.getDrops());
                            for (int i = 0; i < inventory.getSize() - drops.size(); i++) drops.add(null);
                            Collections.shuffle(drops);
                            for (int i = 0; i < drops.size(); i++) {
                                ItemStack drop = drops.get(i);
                                if (ItemUtils.isEmpty(drop)) continue;
                                if (i > inventory.getSize() - 1) break;
                                inventory.setItem(i, drop);
                            }
                        }
                    }
                }
                ReplacementTable globalTable = LootTableRegistry.getGlobalReplacementTable();
                event.scheduler().sync(() -> {
                    ValhallaLootReplacementEvent replacementEvent = new ValhallaLootReplacementEvent(replacementTable, context);
                    if (replacementTable != null) Bukkit.getPluginManager().callEvent(replacementEvent);
                    if (replacementTable == null || !replacementEvent.isCancelled()){
                        for (int i = 0; i < inventory.getSize(); i++){
                            ItemStack item = inventory.getItem(i);
                            if (ItemUtils.isEmpty(item)) continue;
                            ItemStack replacement = LootTableRegistry.getReplacement(replacementTable, context, LootTable.LootType.CONTAINER, item);
                            if (!ItemUtils.isEmpty(replacement)) item = replacement;
                            ItemStack globalReplacement = LootTableRegistry.getReplacement(globalTable, context, LootTable.LootType.CONTAINER, item);
                            if (!ItemUtils.isEmpty(globalReplacement)) item = globalReplacement;
                            if (!ItemUtils.isEmpty(item)) inventory.setItem(i, item);
                        }
                    }
                });
            }
        };
        Bukkit.getPluginManager().registerEvents(listener, ValhallaMMO.getInstance());
    }

}
