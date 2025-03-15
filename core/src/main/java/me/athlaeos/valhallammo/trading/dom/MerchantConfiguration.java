package me.athlaeos.valhallammo.trading.dom;

import org.bukkit.entity.Villager;

import java.util.Collection;
import java.util.HashSet;

public class MerchantConfiguration {
    private final String id;
    private final Villager.Profession type;
    private final Collection<String> merchantTypes = new HashSet<>(); // should be exclusively filled with MerchantType id's

    public MerchantConfiguration(String id, Villager.Profession type){
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public Villager.Profession getType() {
        return type;
    }

    public Collection<String> getMerchantTypes() {
        return merchantTypes;
    }
}
