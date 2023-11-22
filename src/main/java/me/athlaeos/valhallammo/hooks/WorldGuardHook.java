package me.athlaeos.valhallammo.hooks;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

public class WorldGuardHook extends PluginHook{
    public static String VMMO_CRAFTING_CRAFTINGTABLE = "vmmo-crafting-craftingtable";
    public static String VMMO_CRAFTING_BREWING = "vmmo-crafting-brewing";
    public static String VMMO_CRAFTING_SMITHING = "vmmo-crafting-smithing";
    public static String VMMO_CRAFTING_FURNACE = "vmmo-crafting-furnace";
    public static String VMMO_CRAFTING_BLASTFURNACE = "vmmo-crafting-blastfurnace";
    public static String VMMO_CRAFTING_CAMPFIRE = "vmmo-crafting-campfire";
    public static String VMMO_CRAFTING_SMOKER = "vmmo-crafting-smoker";
    public static String VMMO_CRAFTING_IMMERSIVE = "vmmo-crafting-immersive";
    public static String VMMO_CRAFTING_CAULDRON = "vmmo-crafting-cauldron";
    public static String VMMO_CRAFTING_ALLOY = "vmmo-crafting-alloy";
    public static String VMMO_SKILL_POWER = "vmmo-skill-power";
    public static String VMMO_SKILL_SMITHING = "vmmo-skill-smithing";
    public static String VMMO_SKILL_ENCHANTING = "vmmo-skill-enchanting";
    public static String VMMO_SKILL_ALCHEMY = "vmmo-skill-alchemy";
    public static String VMMO_SKILL_LIGHTWEAPONS = "vmmo-skill-lightweapons";
    public static String VMMO_SKILL_HEAVYWEAPONS = "vmmo-skill-heavyweapons";
    public static String VMMO_SKILL_ARCHERY = "vmmo-skill-archery";
    public static String VMMO_SKILL_LIGHTARMOR = "vmmo-skill-lightarmor";
    public static String VMMO_SKILL_HEAVYARMOR = "vmmo-skill-heavyarmor";
    public static String VMMO_SKILL_MINING = "vmmo-skill-mining";
    public static String VMMO_SKILL_FARMING = "vmmo-skill-farming";
    public static String VMMO_SKILL_LANDSCAPING = "vmmo-skill-landscaping";
    public static String VMMO_COMBAT_CRIT = "vmmo-combat-crit";
    public static String VMMO_COMBAT_BLEED = "vmmo-combat-bleed";
    public static String VMMO_COMBAT_PARRY = "vmmo-combat-parry";
    public static String VMMO_COMBAT_REFLECT = "vmmo-combat-reflect";
    public static String VMMO_COMBAT_WEAPONCOATING = "vmmo-combat-weaponcoating";
    public static String VMMO_COMBAT_POTIONIMMUNITY = "vmmo-combat-potionimmunity";
    public static String VMMO_COMBAT_OVERHEADBLOW = "vmmo-combat-overheadblow";
    public static String VMMO_COMBAT_CHARGEDSHOT = "vmmo-combat-chargedshot";
    public static String VMMO_COMBAT_ADRENALINE = "vmmo-combat-adrenaline";
    public static String VMMO_COMBAT_RAGE = "vmmo-combat-rage";
    public static String VMMO_ABILITIES_OVERDRIVE = "vmmo-abilities-overdrive";
    public static String VMMO_ABILITIES_VEINMINER = "vmmo-abilities-veinminer";
    public static String VMMO_ABILITIES_VEINFARMER = "vmmo-abilities-veinfarmer";
    public static String VMMO_ABILITIES_TREECAPITATOR = "vmmo-abilities-treecapitator";
    public static String VMMO_ABILITIES_BLOCKCONVERSIONS = "vmmo-abilities-blockconversions";
    public static String VMMO_MECHANICS_ARMORSLOW = "vmmo-mechanics-armorslow";
    public static String VMMO_FEATURES_GLOBALBUFF = "vmmo-features-globalbuff";
    public static String VMMO_LOOT_FISHING = "vmmo-loot-fishing";
    public static String VMMO_LOOT_BLOCK = "vmmo-loot-block";
    public static String VMMO_LOOT_ENTITY = "vmmo-loot-entity";
    public static String VMMO_PARTY_ITEMSHARING = "vmmo-party-itemsharing";
    public static String VMMO_PARTY_EXPSHARING = "vmmo-party-expsharing";
    public static String VMMO_PARTY_CHAT = "vmmo-party-chat";

    public WorldGuardHook() {
        super("WorldGuard");
        if (!isPresent()) return;
        WorldGuardWrapper.registerFlag(VMMO_CRAFTING_CRAFTINGTABLE);
        WorldGuardWrapper.registerFlag(VMMO_CRAFTING_BREWING);
        WorldGuardWrapper.registerFlag(VMMO_CRAFTING_SMITHING);
        WorldGuardWrapper.registerFlag(VMMO_CRAFTING_FURNACE);
        WorldGuardWrapper.registerFlag(VMMO_CRAFTING_BLASTFURNACE);
        WorldGuardWrapper.registerFlag(VMMO_CRAFTING_CAMPFIRE);
        WorldGuardWrapper.registerFlag(VMMO_CRAFTING_SMOKER);
        WorldGuardWrapper.registerFlag(VMMO_CRAFTING_IMMERSIVE);
        WorldGuardWrapper.registerFlag(VMMO_CRAFTING_CAULDRON);
        WorldGuardWrapper.registerFlag(VMMO_CRAFTING_ALLOY);
        WorldGuardWrapper.registerFlag(VMMO_SKILL_POWER);
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
        WorldGuardWrapper.registerFlag(VMMO_SKILL_LANDSCAPING);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_CRIT);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_BLEED);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_PARRY);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_REFLECT);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_WEAPONCOATING);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_POTIONIMMUNITY);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_OVERHEADBLOW);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_CHARGEDSHOT);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_ADRENALINE);
        WorldGuardWrapper.registerFlag(VMMO_COMBAT_RAGE);
        WorldGuardWrapper.registerFlag(VMMO_ABILITIES_OVERDRIVE);
        WorldGuardWrapper.registerFlag(VMMO_ABILITIES_VEINMINER);
        WorldGuardWrapper.registerFlag(VMMO_ABILITIES_VEINFARMER);
        WorldGuardWrapper.registerFlag(VMMO_ABILITIES_TREECAPITATOR);
        WorldGuardWrapper.registerFlag(VMMO_ABILITIES_BLOCKCONVERSIONS);
        WorldGuardWrapper.registerFlag(VMMO_MECHANICS_ARMORSLOW);
        WorldGuardWrapper.registerFlag(VMMO_FEATURES_GLOBALBUFF);
        WorldGuardWrapper.registerFlag(VMMO_LOOT_FISHING);
        WorldGuardWrapper.registerFlag(VMMO_LOOT_BLOCK);
        WorldGuardWrapper.registerFlag(VMMO_LOOT_ENTITY);
        WorldGuardWrapper.registerFlag(VMMO_PARTY_ITEMSHARING);
        WorldGuardWrapper.registerFlag(VMMO_PARTY_EXPSHARING);
        WorldGuardWrapper.registerFlag(VMMO_PARTY_CHAT);
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
