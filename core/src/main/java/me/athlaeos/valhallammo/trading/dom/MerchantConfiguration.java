package me.athlaeos.valhallammo.trading.dom;

import java.util.Collection;
import java.util.HashSet;

public class MerchantConfiguration {
    private final ProfessionWrapper type;
    private final Collection<String> merchantTypes = new HashSet<>(); // should be exclusively filled with MerchantType id's

    public MerchantConfiguration(ProfessionWrapper type){
        this.type = type;
    }

    public ProfessionWrapper getType() {
        return type;
    }

    public Collection<String> getMerchantTypes() {
        return merchantTypes;
    }
}
