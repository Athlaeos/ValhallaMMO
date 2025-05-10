package me.athlaeos.valhallammo.hooks;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

public class WorldGuardHook extends PluginHook{
    public static final String VMMO_CRAFTING_CRAFTINGTABLE = "vmmo-crafting-craftingtable";
    public static final String VMMO_CRAFTING_BREWING = "vmmo-crafting-brewing";
    public static final String VMMO_CRAFTING_SMITHING = "vmmo-crafting-smithing";
    public static final String VMMO_CRAFTING_FURNACE = "vmmo-crafting-furnace";
    public static final String VMMO_CRAFTING_CAMPFIRE = "vmmo-crafting-campfire";
    public static final String VMMO_CRAFTING_IMMERSIVE = "vmmo-crafting-immersive";
    public static final String VMMO_CRAFTING_CAULDRON = "vmmo-crafting-cauldron";
    public static final String VMMO_CRAFTING_ALLOY = "vmmo-crafting-alloy";
    public static final String VMMO_SKILL_SMITHING = "vmmo-skill-smithing";
    public static final String VMMO_SKILL_TRADING = "vmmo-skill-trading";
    public static final String VMMO_SKILL_ENCHANTING = "vmmo-skill-enchanting";
    public static final String VMMO_SKILL_ALCHEMY = "vmmo-skill-alchemy";
    public static final String VMMO_SKILL_LIGHTWEAPONS = "vmmo-skill-lightweapons";
    public static final String VMMO_SKILL_HEAVYWEAPONS = "vmmo-skill-heavyweapons";
    public static final String VMMO_SKILL_ARCHERY = "vmmo-skill-archery";
    public static final String VMMO_SKILL_LIGHTARMOR = "vmmo-skill-lightarmor";
    public static final String VMMO_SKILL_HEAVYARMOR = "vmmo-skill-heavyarmor";
    public static final String VMMO_SKILL_MINING = "vmmo-skill-mining";
    public static final String VMMO_SKILL_FARMING = "vmmo-skill-farming";
    public static final String VMMO_SKILL_WOODCUTTING = "vmmo-skill-woodcutting";
    public static final String VMMO_SKILL_FISHING = "vmmo-skill-fishing";
    public static final String VMMO_SKILL_DIGGING = "vmmo-skill-digging";
    public static final String VMMO_COMBAT_CRIT = "vmmo-combat-crit";
    public static final String VMMO_COMBAT_BLEED = "vmmo-combat-bleed";
    public static final String VMMO_COMBAT_PARRY = "vmmo-combat-parry";
    public static final String VMMO_COMBAT_REFLECT = "vmmo-combat-reflect";
    public static final String VMMO_COMBAT_WEAPONCOATING = "vmmo-combat-weaponcoating";
    public static final String VMMO_COMBAT_POTIONIMMUNITY = "vmmo-combat-potionimmunity";
    public static final String VMMO_COMBAT_POWERATTACK = "vmmo-combat-powerattack";
    public static final String VMMO_COMBAT_CHARGEDSHOT = "vmmo-combat-chargedshot";
    public static final String VMMO_COMBAT_ADRENALINE = "vmmo-combat-adrenaline";
    public static final String VMMO_COMBAT_RAGE = "vmmo-combat-rage";
    public static final String VMMO_ABILITIES_DRILLING = "vmmo-abilities-drilling";
    public static final String VMMO_ABILITIES_VEINMINER = "vmmo-abilities-veinminer";
    public static final String VMMO_ABILITIES_VEINFARMER = "vmmo-abilities-veinfarmer";
    public static final String VMMO_ABILITIES_TREECAPITATOR = "vmmo-abilities-treecapitator";
    public static final String VMMO_ABILITIES_BLOCKCONVERSIONS = "vmmo-abilities-blockconversions";
    public static final String VMMO_PARTY_ITEMSHARING = "vmmo-party-itemsharing";
    public static final String VMMO_PARTY_EXPSHARING = "vmmo-party-expsharing";
    public static final String VMMO_DOUBLE_JUMPING = "vmmo-double-jumping";

    public WorldGuardHook() {
        super("WorldGuard");
        if (!isPresent()) return;
        WorldGuardWrapper.registerFlag(VMMO_CRAFTING_CRAFTINGTABLE);
        WorldGuardWrapper.registerFlag(VMMO_CRAFTING_BREWING);
        WorldGuardWrapper.registerFlag(VMMO_CRAFTING_SMITHING);
        WorldGuardWrapper.registerFlag(VMMO_SKILL_TRADING);
        WorldGuardWrapper.registerFlag(VMMO_CRAFTING_FURNACE);
        WorldGuardWrapper.registerFlag(VMMO_CRAFTING_CAMPFIRE);
        WorldGuardWrapper.registerFlag(VMMO_CRAFTING_IMMERSIVE);
        WorldGuardWrapper.registerFlag(VMMO_CRAFTING_CAULDRON);
        WorldGuardWrapper.registerFlag(VMMO_CRAFTING_ALLOY);
        WorldGuardWrapper.registerFlag(VMMO_SKILL_SMITHING);
        WorldGuardWrapper.registerFlag(VMMO_SKILL_ENCHANTING);
        WorldGuardWrapper.registerFlag(VMMO_SKILL_ALCHEMY);
        WorldGuardWrapper.registerFlag(VMMO_SKILL_LIGHTWEAPONS);
        WorldGuardWrapper.registerFlag(VMMO_SKILL_HEAVYWEAPONS);
        WorldGuardWrapper.registerFlag(VMMO_SKILL_ARCHERY);
        WorldGuardWrapper.registerFlag(VMMO_SKILL_LIGHTARMOR);
        WorldGuardWrapper.registerFlag(VMMO_SKILL_HEAVYARMOR);
        WorldGuardWrapper.registerFlag(VMMO_SKILL_MINING);
        WorldGuardWrapper.registerFlag(VMMO_SKILL_FARMING);
        WorldGuardWrapper.registerFlag(VMMO_SKILL_WOODCUTTING);
        WorldGuardWrapper.registerFlag(VMMO_SKILL_DIGGING);
        WorldGuardWrapper.registerFlag(VMMO_SKILL_FISHING);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_CRIT);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_BLEED);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_PARRY);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_REFLECT);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_WEAPONCOATING);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_POTIONIMMUNITY);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_POWERATTACK);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_CHARGEDSHOT);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_ADRENALINE);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_RAGE);
        WorldGuardWrapper.registerFlag(VMMO_ABILITIES_DRILLING);
        WorldGuardWrapper.registerFlag(VMMO_ABILITIES_VEINMINER);
        WorldGuardWrapper.registerFlag(VMMO_ABILITIES_VEINFARMER);
        WorldGuardWrapper.registerFlag(VMMO_ABILITIES_TREECAPITATOR);
        WorldGuardWrapper.registerFlag(VMMO_ABILITIES_BLOCKCONVERSIONS);
        WorldGuardWrapper.registerFlag(VMMO_PARTY_ITEMSHARING);
        WorldGuardWrapper.registerFlag(VMMO_PARTY_EXPSHARING);
        WorldGuardWrapper.registerFlag(VMMO_DOUBLE_JUMPING);
    }

    public static boolean inDisabledRegion(Location l, Player p, String flag){
        if (ValhallaMMO.isHookFunctional(WorldGuardHook.class)){
            return WorldGuardWrapper.inDisabledRegion(l, p, flag);
        }
        return false;
    }

    public static boolean canPlaceBlocks(Location l, Player p){
        return WorldGuardWrapper.canPlaceBlocks(l, p);
    }

    public static boolean inDisabledRegion(Location l, String flag){
        return inDisabledRegion(l, null, flag);
    }

    public static Collection<String> getRegions(){
        return WorldGuardWrapper.getRegions();
    }

    public static boolean isInRegion(Location l, String region){
        return WorldGuardWrapper.isInRegion(l, region);
    }

    @Override
    public void whenPresent() {
    }
}
