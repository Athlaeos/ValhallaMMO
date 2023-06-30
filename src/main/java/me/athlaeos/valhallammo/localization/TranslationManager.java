package me.athlaeos.valhallammo.localization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TranslationManager {
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private static PluginTranslationDTO pluginTranslations;
    private static MaterialTranslationsDTO materialTranslations;
    private static String language;

    public static String getTranslation(String key){
        return pluginTranslations.getStringTranslations().getOrDefault(key, key);
    }

    public static List<String> getListTranslation(String key) {
        return pluginTranslations.getStringListTranslations().getOrDefault(key, Collections.singletonList(key));
    }

    public static String getIndexedString(int id){
        return pluginTranslations.getStringIndex().getOrDefault(id, "invalid_id_" + id);
    }

    public static List<String> getIndexedStringList(int id){
        return pluginTranslations.getStringListIndex().getOrDefault(id, Collections.singletonList("invalid_id_" + id));
    }

    public static String getMaterialTranslation(Material m){
        return materialTranslations.getMaterialTranslations().getOrDefault(
                m, me.athlaeos.valhallammo.utility.StringUtils.toPascalCase(m.toString().replace("_", " "))
        );
    }

    public static MaterialTranslationsDTO getMaterialTranslations() {
        return materialTranslations;
    }

    public static String translatePlaceholders(String originalString){
        String[] matches = StringUtils.substringsBetween(originalString, "<lang.", ">");
        if (matches == null) return originalString;
        for (String s : matches)
            originalString = originalString.replace("<lang." + s + ">", getTranslation(s));
        return originalString;
    }

    public static List<String> translateListPlaceholders(List<String> originalList){
        List<String> newList = new ArrayList<>();
        if (originalList == null) return newList;

        for (String l : originalList) {
            String subString = StringUtils.substringBetween(l, "<lang.", ">");
            if (subString == null) {
                // list does not contain placeholder match, string is added normally
                newList.add(Utils.chat(l));
            } else {
                // list has a line matching the placeholder format, placeholder is replaced with associated value
                List<String> placeholderList = getListTranslation(subString);
                if (placeholderList.isEmpty()){
                    newList.add(translatePlaceholders(Utils.chat(l)));
                } else {
                    // each line in the associated list is once again passed through the translation method
                    placeholderList.forEach(s -> newList.add(translatePlaceholders(Utils.chat(s))));
                }
            }
        }
        return newList;
    }

    public static void load(String l){
        try {
            language = l;
            BufferedReader langReader = new BufferedReader(
                    new FileReader(
                            new File(ValhallaMMO.getInstance().getDataFolder(), "/languages/" + l + ".json")
                    )
            );
            BufferedReader matReader = new BufferedReader(
                    new FileReader(
                            new File(ValhallaMMO.getInstance().getDataFolder(), "/languages/materials/" + l + ".json")
                    )
            );
            pluginTranslations = gson.fromJson(langReader, PluginTranslationDTO.class);
            materialTranslations = gson.fromJson(matReader, MaterialTranslationsDTO.class);
            ValhallaMMO.logInfo("Loading ValhallaMMO config with language " + l + " selected");
        } catch (IOException exception){
            ValhallaMMO.logSevere(exception.getMessage());
        }
    }

    /**
     * Replaces any language placeholders in the display name and lore to their translated versions
     * @param i the item to translate
     */
    public ItemStack translateItemStack(ItemStack i){
        if (ItemUtils.isEmpty(i)) return null;
        boolean translated = false;
        ItemMeta iMeta = i.getItemMeta();
        if (iMeta == null) return null;
        if (iMeta.hasDisplayName()){
            if (iMeta.getDisplayName().contains("<lang.")){
                iMeta.setDisplayName(Utils.chat(translatePlaceholders(iMeta.getDisplayName())));
                translated = true;
            }
        }
        if (iMeta.hasLore() && iMeta.getLore() != null){
            List<String> newLore = new ArrayList<>();
            if (iMeta.getLore().stream().anyMatch(s -> s.contains("<lang."))){
                for (String s : translateListPlaceholders(iMeta.getLore())){
                    newLore.add(Utils.chat(s));
                }
                translated = true;
            } else newLore = iMeta.getLore();

            iMeta.setLore(newLore);
        }
        if (!translated) return i;
        i.setItemMeta(iMeta);
        i = ItemUtils.reSetItemText(i);

        return i;
    }

    public static String getLanguage() {
        return language;
    }
}
