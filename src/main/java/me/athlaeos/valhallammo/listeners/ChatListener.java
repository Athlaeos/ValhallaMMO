package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.Question;
import me.athlaeos.valhallammo.dom.Questionnaire;
import me.athlaeos.valhallammo.loot.LootEntry;
import me.athlaeos.valhallammo.loot.LootPool;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.loot.predicates.implementations.LuckFilter;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        if (e.getMessage().startsWith(".loot")){
            ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
                // .loot <table> <pool> <material> <qty> <c/w> <chance/weight base> <chance/weight per luck> <min luck>
                String[] args = e.getMessage().replace(".loot ", "").split(" ");
                if (args.length > 6){
                    LootTable table = LootTableRegistry.getLootTables().getOrDefault(args[0], new LootTable(args[0]));
                    LootPool pool = table.getPools().containsKey(args[1]) ? table.getPools().get(args[1]) : table.addPool(args[1]);
                    Material drop = Catch.catchOrElse(() -> Material.valueOf(args[2]), null);
                    if (drop == null) {
                        e.getPlayer().sendMessage(Utils.chat("&cwrong drop"));
                        return;
                    }
                    LootEntry entry = pool.addEntry(new ItemStack(drop));
                    entry.setQuantityMinFortuneBase(0);
                    entry.setQuantityMaxFortuneBase(0);
                    String[] qty = args[3].split("-");
                    Integer min = Catch.catchOrElse(() -> Integer.parseInt(qty[0]), null);
                    if (min == null) {
                        e.getPlayer().sendMessage(Utils.chat("&cwrong min qty"));
                        return;
                    }
                    Integer max = min;
                    if (qty.length > 1) max = Catch.catchOrElse(() -> Integer.parseInt(qty[1]), null);
                    if (max == null){
                        e.getPlayer().sendMessage(Utils.chat("&cwrong max qty"));
                        return;
                    }
                    entry.setBaseQuantityMin(min);
                    entry.setQuantityMaxFortuneBase(max);

                    boolean chanced = args[4].equalsIgnoreCase("c");
                    Double oddsBase = Catch.catchOrElse(() -> Double.parseDouble(args[5]), null);
                    if (oddsBase == null) {
                        e.getPlayer().sendMessage(Utils.chat("&cwrong odds base"));
                        return;
                    }
                    Double oddLuck = Catch.catchOrElse(() -> Double.parseDouble(args[6]), null);
                    if (oddLuck == null){
                        e.getPlayer().sendMessage(Utils.chat("&cwrong odds luck"));
                        return;
                    }
                    if (chanced) {
                        entry.setChance(oddsBase);
                        entry.setChanceQuality(oddLuck);
                    } else {
                        entry.setWeight(oddsBase);
                        entry.setWeightQuality(oddLuck);
                    }

                    if (args.length > 7){
                        Integer luckMin = Catch.catchOrElse(() -> Integer.parseInt(args[7]), null);
                        if (luckMin == null) {
                            e.getPlayer().sendMessage(Utils.chat("&cwrong luck min"));
                            return;
                        }
                        entry.setPredicateSelection(LootTable.PredicateSelection.ALL);
                        LuckFilter l = new LuckFilter();
                        l.setFrom(luckMin);
                        entry.getPredicates().add(l);
                    }

                    LootTableRegistry.registerLootTable(table);
                }
            });
        }

        Questionnaire questionaire = Questionnaire.getActiveQuestionnaire(e.getPlayer());
        if (questionaire != null){
            Question q = questionaire.nextQuestion();
            if (q != null) {
                e.setCancelled(true);
                ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
                    q.answer(e.getPlayer(), e.getMessage());
                    if (questionaire.allAnswered()) questionaire.finish();
                });
            }
        }
    }
}
