package me.athlaeos.valhallammo.localization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TranslationManager {
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static PluginTranslationDTO pluginTranslations;
    private static PluginTranslationDTO defaultTranslations;
    private static MaterialTranslationsDTO materialTranslations;
    private static String language;

    /**
     * Returns the value mapped to the given key. Sends console a warning if no value is mapped and defaults to the key.
     */
    public static String getTranslation(String key){
        if (pluginTranslations == null) return "";
        if (!key.contains("<lang.") && !pluginTranslations.getStringTranslations().containsKey(key)) ValhallaMMO.logWarning("No translated value mapped for " + key);
        return translatePlaceholders(pluginTranslations.getStringTranslations().getOrDefault(key, key));
    }

    /**
     * Returns the raw value mapped to the given key. Can be null if no value is mapped
     */
    public static String getRawTranslation(String key){
        if (pluginTranslations == null) return "";
        if (!pluginTranslations.getStringTranslations().containsKey(key)) return null;
        return translatePlaceholders(pluginTranslations.getStringTranslations().get(key));
    }

    public static PluginTranslationDTO getDefaultTranslations() {
        return defaultTranslations;
    }

    public static List<String> getListTranslation(String key) {
        if (pluginTranslations == null) return new ArrayList<>();
        return pluginTranslations.getStringListTranslations().getOrDefault(key, new ArrayList<>());
    }

    public static String getIndexedString(int id){
        if (pluginTranslations == null) return "";
        return pluginTranslations.getStringIndex().getOrDefault(id, "invalid_id_" + id);
    }

    public static List<String> getIndexedStringList(int id){
        if (pluginTranslations == null) return new ArrayList<>();
        return pluginTranslations.getStringListIndex().getOrDefault(id, Collections.singletonList("invalid_id_" + id));
    }

    public static String getMaterialTranslation(Material m){
        if (materialTranslations == null) return "";
        return materialTranslations.getMaterialTranslations().getOrDefault(
                m.toString(), me.athlaeos.valhallammo.utility.StringUtils.toPascalCase(m.toString().replace("_", " "))
        );
    }

    public static MaterialTranslationsDTO getMaterialTranslations() {
        return materialTranslations;
    }

    public static String translatePlaceholders(String originalString){
        if (originalString == null) return null;
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
                newList.add(l);
            } else {
                // list has a line matching the placeholder format, placeholder is replaced with associated value
                List<String> placeholderList = getListTranslation(subString);
                if (placeholderList.isEmpty()){
                    newList.add(translatePlaceholders(l));
                } else {
                    // each line in the associated list is once again passed through the translation method
                    placeholderList.forEach(s -> newList.add(translatePlaceholders(s)));
                }
            }
        }
        return newList;
    }

    public static void load(String l){
        language = l;
        ValhallaMMO.logInfo("Loading ValhallaMMO config with language " + l + " selected");
        try (BufferedReader langReader = new BufferedReader(new FileReader(new File(ValhallaMMO.getInstance().getDataFolder(), "/languages/" + l + ".json"), StandardCharsets.UTF_8))) {
            pluginTranslations = gson.fromJson(langReader, PluginTranslationDTO.class);
        } catch (IOException exception){
            ValhallaMMO.logSevere(exception.getMessage());
            exception.printStackTrace();
        }
        try (BufferedReader matReader = new BufferedReader(new FileReader(new File(ValhallaMMO.getInstance().getDataFolder(), "/languages/materials/" + l + ".json"), StandardCharsets.UTF_8))){
            materialTranslations = gson.fromJson(matReader, MaterialTranslationsDTO.class);
        } catch (IOException exception){
            ValhallaMMO.logSevere(exception.getMessage());
            exception.printStackTrace();
        }
        int entriesAdded = 0;
        InputStream defaultStream = ValhallaMMO.getInstance().getClass().getResourceAsStream("/languages/en-us.json");
        if (defaultStream != null){
            try (InputStreamReader defaultReader = new InputStreamReader(defaultStream, StandardCharsets.UTF_8)){
                defaultTranslations = gson.fromJson(defaultReader, PluginTranslationDTO.class);

                for (String key : defaultTranslations.getStringTranslations().keySet()){
                    if (!pluginTranslations.getStringTranslations().containsKey(key)){
                        pluginTranslations.getStringTranslations().put(key, defaultTranslations.getStringTranslations().get(key));
                        if (entriesAdded == 0 && !l.equalsIgnoreCase("en-us")) ValhallaMMO.logWarning("Language file was outdated! New english entries added to /languages/" + l + ".json. Sorry for the spam, but if you don't use the default (en-us) be sure to keep track of and translate the following entries to your locale");
                        if (!l.equalsIgnoreCase("en-us")) ValhallaMMO.logWarning("string > " + key);
                        entriesAdded++;
                    }
                }
                for (String key : defaultTranslations.getStringListTranslations().keySet()){
                    if (!pluginTranslations.getStringListTranslations().containsKey(key)){
                        pluginTranslations.getStringListTranslations().put(key, defaultTranslations.getStringListTranslations().get(key));
                        if (entriesAdded == 0 && !l.equalsIgnoreCase("en-us")) ValhallaMMO.logWarning("Language file was outdated! New english entries added to /languages/" + l + ".json. Sorry for the spam, but if you don't use the default (en-us) be sure to keep track of and translate the following entries to your locale");
                        if (!l.equalsIgnoreCase("en-us")) ValhallaMMO.logWarning("list > " + key);
                        entriesAdded++;
                    }
                }
            } catch (IOException exception){
                ValhallaMMO.logSevere(exception.getMessage());
                exception.printStackTrace();
            }

            if (entriesAdded > 0) {
                try (FileWriter writer = new FileWriter(new File(ValhallaMMO.getInstance().getDataFolder(), "languages/" + l + ".json"), StandardCharsets.UTF_8)){
                    gson.toJson(pluginTranslations, writer);
                } catch (IOException exception){
                    ValhallaMMO.logSevere(exception.getMessage());
                    exception.printStackTrace();
                }
            }
        }
    }

    /**
     * Replaces any language placeholders in the display name and lore to their translated versions
     * @param iMeta the item to translate
     */
    public static void translateItemMeta(ItemMeta iMeta){
        boolean translated = false;
        if (iMeta == null) return;
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
        if (!translated) return;
        ItemUtils.reSetItemText(iMeta);
    }

    public static String getLanguage() {
        return language;
    }
}
