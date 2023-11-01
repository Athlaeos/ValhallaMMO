package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.ItemSkillRequirements;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class MainHandPenalty implements AccumulativeStatSource {
    private final String statPenalty;

    public MainHandPenalty(String statPenalty){
        this.statPenalty = statPenalty;
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof Player p){
            ItemBuilder mainHand = ItemUtils.isEmpty(p.getInventory().getItemInMainHand()) ? null : new ItemBuilder(p.getInventory().getItemInMainHand());
            ItemBuilder offHand = ItemUtils.isEmpty(p.getInventory().getItemInOffHand()) ? null : new ItemBuilder(p.getInventory().getItemInOffHand());
            ItemBuilder used = ItemUtils.usedMainHand(mainHand, offHand) ? mainHand : offHand;
            if (used == null) return 0;
            return ItemSkillRequirements.getPenalty(p, used.getMeta(), statPenalty);
        }
        return 0;
    }
}
