package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.throwable_weapon_animations.ThrowableItemStats;
import me.athlaeos.valhallammo.item.throwable_weapon_animations.ThrowableWeaponAnimationRegistry;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ThrowableWeapon extends DynamicItemModifier {
    private String animationType = "vertical_spin";
    private int cooldown = 0;
    private double gravityStrength = 1D, velocityDamageMultiplier = 0.3D, defaultVelocity = 3D, damageMultiplier = 1D;
    private boolean infinity = false, returnsNaturally = false;

    public ThrowableWeapon(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        ThrowableWeaponAnimationRegistry.setItemStats(outputItem.getMeta(), new ThrowableItemStats(animationType, cooldown,
                gravityStrength, velocityDamageMultiplier, defaultVelocity, damageMultiplier, infinity, returnsNaturally));
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 7) {
            List<String> animationTypes = new ArrayList<>(ThrowableWeaponAnimationRegistry.getAnimations().keySet());
            int currentType = animationTypes.indexOf(animationType);
            if (e.isLeftClick()) {
                if (currentType + 1 >= animationTypes.size()) currentType = 0;
                else currentType++;
            } else {
                if (currentType - 1 < 0) currentType = animationTypes.size() - 1;
                else currentType--;
            }
            animationType = animationTypes.get(currentType);
        } else if (button == 10){
            cooldown = Math.max(0, cooldown + ((e.isRightClick() ? -1 : 1) * (e.isShiftClick() ? 20 : 2)));
        } else if (button == 11){
            gravityStrength = Math.max(0, gravityStrength + ((e.isRightClick() ? -1 : 1) * (e.isShiftClick() ? 0.1 : 0.01)));
        } else if (button == 12){
            velocityDamageMultiplier = Math.max(0, velocityDamageMultiplier + ((e.isRightClick() ? -1 : 1) * (e.isShiftClick() ? 0.1 : 0.01)));
        } else if (button == 13){
            defaultVelocity = Math.max(0, defaultVelocity + ((e.isRightClick() ? -1 : 1) * (e.isShiftClick() ? 1 : 0.1)));
        } else if (button == 14){
            damageMultiplier = Math.max(0, damageMultiplier + ((e.isRightClick() ? -1 : 1) * (e.isShiftClick() ? 0.1 : 0.01)));
        } else if (button == 16){
            infinity = !infinity;
        } else if (button == 18){
            returnsNaturally = !returnsNaturally;
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(10,
                new ItemBuilder(Material.CLOCK)
                        .name("&eWhat should the cooldown be?")
                        .lore("&fSet to &e" + StatFormat.TIME_SECONDS_BASE_20_P1.format(cooldown),
                                "&6Click to increase/decrease by 0.1s",
                                "&6Shift-Click to increase/decrease by 1s")
                        .get()).map(Set.of(
                new Pair<>(11,
                        new ItemBuilder(Material.ENDER_PEARL)
                                .name("&eWhat should the gravity be?")
                                .lore("&fSet to &e" + StatFormat.PERCENTILE_BASE_1_P1.format(gravityStrength),
                                        "&fDetermines how fast the thrown weapon should",
                                        "&fbe attracted to the ground",
                                        "&6Click to increase/decrease by 1%",
                                        "&6Shift-Click to increase/decrease by 10%")
                                .get()),
                new Pair<>(12,
                        new ItemBuilder(Material.SPECTRAL_ARROW)
                                .name("&eWhat should the velocity-damage scale be?")
                                .lore("&fSet to &e" + StatFormat.PERCENTILE_BASE_1_P1.format(velocityDamageMultiplier),
                                        "&fDetermines how much weaker/stronger the",
                                        "&fweapon's damage should be relative to its",
                                        "&fstarting velocity. The given stat is equal",
                                        "&fto the damage buff if the weapon hits at 2x velocity.",
                                        "&e+40% velocity for example will do " + (StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1.format(0.4 * velocityDamageMultiplier)) + " damage",
                                        "&6Click to increase/decrease by 1%",
                                        "&6Shift-Click to increase/decrease by 10%")
                                .get()),
                new Pair<>(13,
                        new ItemBuilder(Material.ARROW)
                                .name("&eWhat should base velocity be?")
                                .lore("&fSet to &e" + StatFormat.FLOAT_P2.format(defaultVelocity),
                                        "&fDetermines the starting velocity of",
                                        "&fthe weapon. For reference, a shot arrow",
                                        "&ffrom a fully-drawn bow will have a velocity",
                                        "&fof 3",
                                        "&6Click to increase/decrease by 0.1",
                                        "&6Shift-Click to increase/decrease by 1")
                                .get()),
                new Pair<>(14,
                        new ItemBuilder(Material.IRON_AXE)
                                .name("&eWhat should the damage conversion be?")
                                .lore("&fSet to &e" + StatFormat.PERCENTILE_BASE_1_P1.format(damageMultiplier),
                                        "&fDetermines what fraction of the weapon's",
                                        "&fbase damage should be converted as thrown",
                                        "&fdamage. A 9 damage axe for example will do",
                                        "&f" + StatFormat.FLOAT_P1.format(9 * damageMultiplier) + " damage when thrown",
                                        "&6Click to increase/decrease by 1%",
                                        "&6Shift-Click to increase/decrease by 10%")
                                .get()),
                new Pair<>(16,
                        new ItemBuilder(Material.ENCHANTED_BOOK)
                                .name("&eShould it be infinite?")
                                .lore("&fSet to &e" + infinity,
                                        "&fDetermines if the thrown item stays",
                                        "&fin your inventory after throwing.",
                                        "&fThrown items do not drop themselves",
                                        "&6Click to toggle")
                                .get()),
                new Pair<>(18,
                        new ItemBuilder(Material.TRIDENT)
                                .name("&eShould the item return naturally?")
                                .lore("&fSet to &e" + returnsNaturally,
                                        "&fDetermines if the thrown item should",
                                        "&ffly back to the thrower after having",
                                        "&fhit something.",
                                        "&fApplying the Loyalty enchantment also",
                                        "&fhas this effect",
                                        "&6Click to toggle")
                                .get()),
                new Pair<>(7,
                        new ItemBuilder(Material.PAINTING)
                                .name("&eWhat should the animation be?")
                                .lore("&fSet to &e" + animationType,
                                        "&fDetermines the flight pattern of",
                                        "&fthe weapon",
                                        "&6Click to cycle")
                                .get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.DIAMOND_AXE).get();
    }

    @Override
    public String getDisplayName() {
        return "&eThrown Weapon";
    }

    @Override
    public String getDescription() {
        return "&fMakes the weapon throwable.";
    }

    @Override
    public String getActiveDescription() {
        return "&fMakes the item throwable.";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setAnimationType(String animationType) { this.animationType = animationType; }
    public void setCooldown(int cooldown) { this.cooldown = cooldown; }
    public void setDamageMultiplier(double damageMultiplier) { this.damageMultiplier = damageMultiplier; }
    public void setDefaultVelocity(double defaultVelocity) { this.defaultVelocity = defaultVelocity; }
    public void setGravityStrength(double gravityStrength) { this.gravityStrength = gravityStrength; }
    public void setInfinity(boolean infinity) { this.infinity = infinity; }
    public void setReturnsNaturally(boolean returnsNaturally) { this.returnsNaturally = returnsNaturally; }
    public void setVelocityDamageMultiplier(double velocityDamageMultiplier) { this.velocityDamageMultiplier = velocityDamageMultiplier; }

    @Override
    public DynamicItemModifier copy() {
        ThrowableWeapon m = new ThrowableWeapon(getName());
        m.setAnimationType(this.animationType);
        m.setCooldown(this.cooldown);
        m.setDamageMultiplier(this.damageMultiplier);
        m.setDefaultVelocity(this.defaultVelocity);
        m.setGravityStrength(this.gravityStrength);
        m.setInfinity(this.infinity);
        m.setReturnsNaturally(this.returnsNaturally);
        m.setVelocityDamageMultiplier(this.velocityDamageMultiplier);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 8) return "Missing some arguments to make the item throwable";
        try {
            animationType = args[0];
            cooldown = Integer.parseInt(args[1]);
            damageMultiplier = StringUtils.parseDouble(args[2]);
            defaultVelocity = StringUtils.parseDouble(args[3]);
            gravityStrength = StringUtils.parseDouble(args[4]);
            velocityDamageMultiplier = StringUtils.parseDouble(args[5]);
            infinity = Boolean.parseBoolean(args[6]);
            returnsNaturally = Boolean.parseBoolean(args[7]);
        } catch (IllegalArgumentException ignored){
            return "Invalid argument(s) given";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return new ArrayList<>(ThrowableWeaponAnimationRegistry.getAnimations().keySet());
        if (currentArg == 1) return List.of("<cooldown_in_ticks>", "10", "20", "100", "200");
        if (currentArg == 2) return List.of("<damage_multiplier>", "1", "1.5", "2");
        if (currentArg == 3) return List.of("<default_velocity>", "3", "10", "30");
        if (currentArg == 4) return List.of("<gravity>", "0", "0.5", "1", "2");
        if (currentArg == 5) return List.of("<velocity_damage>", "0", "1", "2", "3");
        if (currentArg == 6) return List.of("<infinity>", "true", "false");
        if (currentArg == 7) return List.of("<returning>", "true", "false");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 8;
    }
}
