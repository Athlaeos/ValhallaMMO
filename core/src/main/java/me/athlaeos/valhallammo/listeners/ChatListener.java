package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.Scripts;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Question;
import me.athlaeos.valhallammo.dom.Questionnaire;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

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

//        String[] message = e.getMessage().split(" ");
//        if (message.length < 4) {
//            e.getPlayer().sendMessage(Utils.chat("&aattribute on tag maxlevel value"));
//            e.getPlayer().sendMessage(Utils.chat("&a" + String.join(", ", Set.of("tools", "melee", "weapons", "ranged", "armor", "any", "helmets", "chestplates", "leggings", "boots"))));
//            return;
//        }
//        String attribute = message[0];
//        String on = message[1];
//        int tag = Integer.parseInt(message[2]);
//        int maxLevel = Integer.parseInt(message[3]);
//        double value = Double.parseDouble(message[4]);
//        ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> Scripts.createUpgradeRecipe(attribute, on, tag, maxLevel, value));
    }
}
