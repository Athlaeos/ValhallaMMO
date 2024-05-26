package me.athlaeos.valhallammo.localization;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PluginTranslationDTO {
    private final Map<String, String> stringTranslations = new LinkedHashMap<>(); // Translations based on a string key to string value mapping
    // useful for replacing placeholders in things like items
    private final Map<String, List<String>> stringListTranslations = new LinkedHashMap<>(); // Translations based on a string key to a list of strings value mapping
    // useful for inserting lore or whenever the plugin requires several lines of translated stuff
    private final Map<Integer, String> stringIndex = new LinkedHashMap<>(); // Indexed lines of text that can be used within things like modifiers to
    // set a new item's lore (single line) or name
    private final Map<Integer, List<String>> stringListIndex = new LinkedHashMap<>(); // Indexed lists of text lines that can be used within things like
    // modifiers to set a new item's lore

    public Map<String, String> getStringTranslations() {
        return stringTranslations;
    }

    public Map<Integer, String> getStringIndex() {
        return stringIndex;
    }

    public Map<String, List<String>> getStringListTranslations() {
        return stringListTranslations;
    }

    public Map<Integer, List<String>> getStringListIndex() {
        return stringListIndex;
    }
}
