package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SkillLevelFilter extends LootPredicate {
    private int from = 0;
    private final String skill;

    public SkillLevelFilter(Skill skill){
        this.skill = skill.getType();
    }

    @Override
    public String getKey() {
        return skill.toLowerCase(Locale.US) + "_level";
    }

    @Override
    public Material getIcon() {
        return Material.NETHER_STAR;
    }

    @Override
    public String getDisplayName() {
        return "&f" + StringUtils.toPascalCase(skill) + " Level";
    }

    @Override
    public String getDescription() {
        return "&fRequires a minimum of a skill level";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires " + StringUtils.toPascalCase(skill) + " to " + (isInverted() ? "&cNOT&f " : "") + "be above &e" + from;
    }

    @Override
    public LootPredicate createNew() {
        return new SkillLevelFilter(SkillRegistry.getSkill(skill));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(2,
                new ItemBuilder(Material.TNT)
                        .name("&eInvert Condition")
                        .lore(inverted ? "&cCondition is inverted" : "&aCondition not inverted",
                                "&fInverted conditions must &cnot &fpass",
                                "&fthis condition. ",
                                "&6Click to toggle")
                        .get()).map(Set.of(
                                new Pair<>(12,
                                        new ItemBuilder(Material.LIGHT)
                                                .name("&eSelect Skill Requirement")
                                                .lore("&eIs currently " + from,
                                                        "&fLevel must " + (isInverted() ? "&cNOT&f " : "") + "be above " + from,
                                                        "&6Click to add/subtract 1",
                                                        "&6Shift-Click to add/subtract 10")
                                                .get())));
    }

    public void setFrom(int from) {
        this.from = from;
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 2) inverted = !inverted;
        else if (button == 12) from += (e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1);
    }

    @Override
    public boolean test(LootContext context) {
        Skill skill = SkillRegistry.getSkill(this.skill);
        if (skill == null || context.getKiller() == null) return false;
        Profile profile = ProfileCache.getOrCache((Player) context.getKiller(), skill.getProfileType());
        return profile.getLevel() >= from != inverted;
    }
}
