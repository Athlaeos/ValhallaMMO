package me.athlaeos.valhallammo.event;

import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class ValhallaUpdatedStatsEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    private final Class<? extends Profile> loadedProfile;

    public ValhallaUpdatedStatsEvent(@NotNull Player who, Class<? extends Profile> loadedProfile) {
        super(who);
        this.loadedProfile = loadedProfile;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Class<? extends Profile> getLoadedProfile() {
        return loadedProfile;
    }
}
