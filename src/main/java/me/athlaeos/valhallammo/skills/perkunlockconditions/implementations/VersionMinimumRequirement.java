package me.athlaeos.valhallammo.skills.perkunlockconditions.implementations;

import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.skills.perkunlockconditions.UnlockCondition;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class VersionMinimumRequirement implements UnlockCondition {
    private MinecraftVersion version;

    @Override
    public void initCondition(Object value) {
        if (value instanceof String s){
            version = Catch.catchOrElse(() -> MinecraftVersion.valueOf(s), null, "Invalid minimum version given: " + s);
        }
    }

    @Override
    public String getValuePlaceholder() {
        return "version_at_least";
    }

    @Override
    public boolean canUnlock(Player p, boolean forceTrue) {
        return version != null && MinecraftVersion.currentVersionNewerThan(version);
    }

    @Override
    public UnlockCondition createInstance() {
        return new VersionMinimumRequirement();
    }

    @Override
    public String getFailedConditionMessage() {
        return TranslationManager.getTranslation("perk_requirement_warning_outdated");
    }

    @Override
    public String getFailurePlaceholder() {
        return "warning_version";
    }

    @Override
    public List<String> getConditionMessages() {
        return new ArrayList<>();
    }
}
