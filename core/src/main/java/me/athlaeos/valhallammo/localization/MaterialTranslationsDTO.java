package me.athlaeos.valhallammo.localization;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MaterialTranslationsDTO {
    private final Map<Material, String> materialTranslations = new LinkedHashMap<>();

    public Map<Material, String> getMaterialTranslations() {
        return materialTranslations;
    }
}
