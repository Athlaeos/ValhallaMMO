package me.athlaeos.valhallammo.version;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.hooks.VaultHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class VaultTransaction {
    public static void addBalance(Player p, double amount){
        if (!ValhallaMMO.isHookFunctional(VaultHook.class)) return;
        Economy eco = ValhallaMMO.getHook(VaultHook.class).getEcon();
        eco.depositPlayer(p, amount);
    }

    public static double getBalance(Player p){
        if (!ValhallaMMO.isHookFunctional(VaultHook.class)) return Double.MAX_VALUE;
        Economy eco = ValhallaMMO.getHook(VaultHook.class).getEcon();
        return eco.getBalance(p);
    }

    public static boolean withdrawBalance(Player p, double amount){
        if (!ValhallaMMO.isHookFunctional(VaultHook.class)) return true;
        Economy eco = ValhallaMMO.getHook(VaultHook.class).getEcon();
        if (eco.getBalance(p) < amount) return false;
        eco.withdrawPlayer(p, amount);
        return true;
    }
}
