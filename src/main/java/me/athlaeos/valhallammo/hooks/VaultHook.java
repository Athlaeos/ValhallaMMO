package me.athlaeos.valhallammo.hooks;

import me.athlaeos.valhallammo.ValhallaMMO;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook extends PluginHook{
    private Economy econ = null;

    public VaultHook() {
        super("Vault");
    }

    public boolean setupEconomy() {
        if (!isPresent()) return false;

        RegisteredServiceProvider<Economy> rsp = ValhallaMMO.getInstance().getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;

        econ = rsp.getProvider();
        return true;
    }

    public Economy getEcon() {
        return econ;
    }
}
