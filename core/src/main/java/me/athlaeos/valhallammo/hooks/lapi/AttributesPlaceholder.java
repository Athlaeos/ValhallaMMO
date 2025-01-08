package me.athlaeos.valhallammo.hooks.lapi;

import me.athlaeos.lapi.placeholder.ListPlaceholder;
import me.athlaeos.lapi.placeholder.StringPlaceholder;
import me.athlaeos.lapi.utils.ItemBuilder;
import me.athlaeos.lapi.utils.StringUtils;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.item.item_attributes.implementations.AttributeDisplayWrapper;
import org.bukkit.entity.Player;

import java.util.*;

public class AttributesPlaceholder extends ListPlaceholder {
    private final Map<String, StringPlaceholder> exceptions = new HashMap<>();
    private final List<String> format;

    public AttributesPlaceholder(){
        format = ConfigManager.getConfig("lapi_configuration.yml").get().getStringList("item_stats.format");
    }

    public Map<String, StringPlaceholder> getExceptions() {
        return exceptions;
    }

    @Override
    public List<String> parse(Player player, ItemBuilder itemBuilder) {
        Map<String, AttributeWrapper> stats = ItemAttributesRegistry.getStats(itemBuilder.getMeta(), false);
        if (stats.isEmpty()) return new ArrayList<>();
        Collection<String> inserted = new HashSet<>();
        List<String> formatted = new ArrayList<>();
        for (String line : format){
            if (line.contains("%stat_")){
                boolean anyInline = false;
                String newLine = line;
                for (String stat : org.apache.commons.lang3.StringUtils.substringsBetween(line, "%stat_", "%")){
                    stat = stat.toUpperCase(Locale.US);
                    AttributeWrapper wrapper = stats.get(stat);
                    if (!(wrapper instanceof AttributeDisplayWrapper d) || d.isHidden() || inserted.contains(stat) || !line.contains("%stat_" + stat.toLowerCase(Locale.US) + "%")) {
                        newLine = newLine.replace("%stat_" + stat.toLowerCase(Locale.US) + "%", "").trim();
                        continue;
                    }
                    inserted.add(stat);
                    if (exceptions.containsKey(stat)) newLine = newLine.replace("%stat_" + stat.toLowerCase(Locale.US) + "%", exceptions.get(stat).parse(player, itemBuilder));
                    else newLine = newLine.replace("%stat_" + stat.toLowerCase(Locale.US) + "%", d.getLoreDisplay());
                    anyInline = true;
                }
                if (anyInline) formatted.add(newLine); // if any of the attributes are present, add the line. otherwise skip it
            } else formatted.add(line);
        }
        List<String> otherStats = stats.values().stream().filter(s -> !inserted.contains(s.getAttribute()) && s instanceof AttributeDisplayWrapper d && !d.isHidden()).map(AttributeWrapper::getLoreDisplay).toList();
        return StringUtils.setListPlaceholder(formatted, "%stats%", otherStats);
    }

    @Override
    public String getIdentifier() {
        return "valhallammo";
    }

    @Override
    public String getPlaceholder() {
        return "item_stats";
    }
}
