package me.athlaeos.valhallammo.skills.perk_rewards;

import org.bukkit.entity.Player;

public interface MultipliableReward {
    void apply(Player player, int multiplyBy);

    void remove(Player player, int multiplyBy);
}
