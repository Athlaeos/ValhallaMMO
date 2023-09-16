package me.athlaeos.valhallammo.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardWrapper {
    public static void registerFlag(String s){
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag(s, true);
            registry.register(flag);
        } catch (Exception e) {
            Flag<?> existing = registry.get(s);
            if (!(existing instanceof StateFlag)) {
                ValhallaMMO.logWarning("Failed to register flag " + s);
                e.printStackTrace();
            }
        }
    }

    public static boolean inDisabledRegion(Location l, Player p, String flag){
        if (ValhallaMMO.isHookFunctional(WorldGuardHook.class)){
            LocalPlayer worldguardPlayer = WorldGuardPlugin.inst().wrapPlayer(p);
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();

            Flag<?> fuzzyFlag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), flag);
            if (fuzzyFlag instanceof StateFlag){
                return !query.testState(BukkitAdapter.adapt(l), worldguardPlayer, (StateFlag) fuzzyFlag); // testState returns true if allowed
            }
        }
        return false;
    }

    public static boolean inDisabledRegion(Location l, String flag){
        return inDisabledRegion(l, null, flag);
    }
}
