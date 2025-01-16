package me.athlaeos.valhallammo.hooks.lapi;

import me.athlaeos.lapi.placeholder.StringPlaceholder;
import me.athlaeos.lapi.utils.ItemBuilder;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc.TimeStampAdd;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.localization.TranslationManager;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimeStampPlaceholder extends StringPlaceholder {
    @Override
    public String getIdentifier() {
        return "valhallammo";
    }

    @Override
    public String getPlaceholder() {
        return "timestamp";
    }

    @Override
    public String parse(Player player, ItemBuilder itemBuilder) {
        LocalDateTime date = TimeStampAdd.getTime(itemBuilder.getMeta());
        if (date == null) return "";
        ZoneOffset timeZone = Catch.catchOrElse(() -> ZoneOffset.of(TranslationManager.getTranslation("formatter_time_timezone")), ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TranslationManager.getTranslation("formatter_time"));
        return date.atOffset(timeZone).format(formatter);
    }
}
