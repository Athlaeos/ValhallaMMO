package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Question;
import me.athlaeos.valhallammo.dom.Questionnaire;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

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
