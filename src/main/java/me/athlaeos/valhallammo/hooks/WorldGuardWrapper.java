package me.athlaeos.valhallammo.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;

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
            LocalPlayer worldguardPlayer = p == null ? null : WorldGuardPlugin.inst().wrapPlayer(p);
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();

            Flag<?> fuzzyFlag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), flag);
            if (fuzzyFlag instanceof StateFlag){
                return !query.testState(BukkitAdapter.adapt(l), worldguardPlayer, (StateFlag) fuzzyFlag); // testState returns true if allowed
            }
        }
        return false;
    }

    public static boolean canPlaceBlocks(Location l, Player p){
        if (l.getWorld() == null) return true;
        if (ValhallaMMO.isHookFunctional(WorldGuardHook.class)){
            LocalPlayer worldguardPlayer = p == null ? null : WorldGuardPlugin.inst().wrapPlayer(p);
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();

            return !query.testState(BukkitAdapter.adapt(l), worldguardPlayer, Flags.BUILD);
        }
        return true;
    }

    public static Collection<String> getRegions(){
        Collection<String> regions = new HashSet<>();
        if (ValhallaMMO.isHookFunctional(WorldGuardHook.class)){
            for (World w : ValhallaMMO.getInstance().getServer().getWorlds()){
                RegionManager worldManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(w));
                if (worldManager == null) continue;
                regions.addAll(worldManager.getRegions().keySet());
            }
        }
        return regions;
    }

    public static boolean isInRegion(Location l, String region){
        if (l.getWorld() == null) return false;
        if (ValhallaMMO.isHookFunctional(WorldGuardHook.class)){
            RegionManager worldManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(l.getWorld()));
            if (worldManager == null) return false;
            return worldManager.getApplicableRegions(BlockVector3.at(l.getX(), l.getY(), l.getZ()))
                    .getRegions().stream().map(ProtectedRegion::getId).anyMatch(id -> id.equals(region));
        }
        return false;
    }

    public static boolean inDisabledRegion(Location l, String flag){
        return inDisabledRegion(l, null, flag);
    }
}
