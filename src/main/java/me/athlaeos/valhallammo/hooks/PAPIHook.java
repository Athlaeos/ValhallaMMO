package me.athlaeos.valhallammo.hooks;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.placeholder.PlaceholderRegistry;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PAPIHook extends PluginHook{

    public PAPIHook() {
        super("PlaceholderAPI");
    }

    public static String parse(Player player, String string){
        return PlaceholderAPI.setPlaceholders(player, string);
    }

    @Override
    public void whenPresent() {
        new PlaceholderExpansion() {
            @Override
            public @NotNull String getIdentifier() {
                return "valhallammo";
            }

            @Override
            public @NotNull String getAuthor() {
                return "Athlaeos";
            }

            @Override
            public @NotNull String getVersion() {
                return ValhallaMMO.getInstance().getDescription().getVersion();
            }

            @Override
            public boolean persist() {
                return true;
            }

            @Override
            public String onRequest(OfflinePlayer player, @NotNull String params) {
                return player != null && player.isOnline() ? PlaceholderRegistry.parse("%" + params + "%", (Player) player) : null;
            }
        }.register();
    }
}
