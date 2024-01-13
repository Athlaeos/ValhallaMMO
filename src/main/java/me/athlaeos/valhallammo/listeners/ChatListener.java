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
