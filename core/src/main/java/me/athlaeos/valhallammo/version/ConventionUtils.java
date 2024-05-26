package me.athlaeos.valhallammo.version;

import org.bukkit.inventory.ItemFlag;

import static me.athlaeos.valhallammo.utility.Utils.oldOrNew;

public class ConventionUtils {
    private static final ItemFlag hidePotionEffectsFlag = ItemFlag.valueOf(oldOrNew("HIDE_POTION_EFFECTS", "HIDE_ADDITIONAL_TOOLTIP"));

    public static ItemFlag getHidePotionEffectsFlag(){
        return hidePotionEffectsFlag;
    }
}
