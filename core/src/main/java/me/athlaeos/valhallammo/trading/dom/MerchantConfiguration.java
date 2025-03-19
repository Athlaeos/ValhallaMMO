package me.athlaeos.valhallammo.trading.dom;

import org.bukkit.entity.Villager;

import java.util.Collection;
import java.util.HashSet;

public class MerchantConfiguration {
    private final Villager.Profession type;
    private final Collection<String> merchantTypes = new HashSet<>(); // should be exclusively filled with MerchantType id's

    public MerchantConfiguration(Villager.Profession type){
        this.type = type;
    }

    public Villager.Profession getType() {
        return type;
    }

    public Collection<String> getMerchantTypes() {
        return merchantTypes;
    }
}
