package me.athlaeos.valhallammo.hooks.lapi;

import me.athlaeos.lapi.placeholder.StringPlaceholder;
import me.athlaeos.lapi.placeholder.placeholders.ListWrapper;
import me.athlaeos.lapi.utils.ItemBuilder;
import me.athlaeos.lapi.utils.StringUtils;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class GenericListWrapper extends ListWrapper {
    private final BiPredicate<Player, ItemBuilder> canApply;
    private final List<String> format;
    private final String placeholder;

    public GenericListWrapper(StringPlaceholder parentPlaceholder, String formatPath, String placeholder, BiPredicate<Player, ItemBuilder> canApply) {
        super(parentPlaceholder);
        this.canApply = canApply;
        this.format = ConfigManager.getConfig("lapi_configuration.yml").get().getStringList(formatPath);
        this.placeholder = placeholder;
    }

    @Override
    protected boolean shouldInsert(Player player, ItemBuilder itemBuilder) {
        return canApply.test(player, itemBuilder);
    }

    @Override
    public List<String> parse(Player player, ItemBuilder itemBuilder) {
        return shouldInsert(player, itemBuilder) ?
                StringUtils.setListPlaceholder(format, placeholder, List.of(parentPlaceholder.parse(player, itemBuilder))) :
                new ArrayList<>();
    }
}
