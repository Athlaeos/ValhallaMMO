package me.athlaeos.valhallammo.trading.happiness;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface HappinessSource {
    String id();

    /**
     * Should return the happiness of the given entity towards the given player, if relevant.
     * @param contextPlayer the player that may or may not be relevant in getting an entity's happiness
     * @param entity the entity to gather their happiness from
     * @return the happiness value of the entity
     */
    float get(Player contextPlayer, Entity entity);

    boolean appliesTo(Entity entity);
}
