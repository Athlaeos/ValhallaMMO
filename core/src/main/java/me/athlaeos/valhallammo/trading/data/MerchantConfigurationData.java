package me.athlaeos.valhallammo.trading.data;

import me.athlaeos.valhallammo.trading.dom.MerchantConfiguration;
import me.athlaeos.valhallammo.trading.dom.MerchantTrade;
import me.athlaeos.valhallammo.trading.dom.MerchantType;

import java.util.Collection;

@SuppressWarnings("all")
public class MerchantConfigurationData {
    private final Collection<MerchantTrade> registeredMerchantTrades;
    private final Collection<MerchantType> registeredMerchantTypes;
    private final Collection<MerchantConfiguration> merchantConfigurations;

    public MerchantConfigurationData(Collection<MerchantTrade> registeredMerchantTrades,
                                     Collection<MerchantType> registeredMerchantTypes,
                                     Collection<MerchantConfiguration> merchantConfigurations){
        this.registeredMerchantTrades = registeredMerchantTrades;
        this.registeredMerchantTypes = registeredMerchantTypes;
        this.merchantConfigurations = merchantConfigurations;
    }

    public Collection<MerchantConfiguration> getMerchantConfigurations() { return merchantConfigurations; }
    public Collection<MerchantTrade> getRegisteredMerchantTrades() { return registeredMerchantTrades; }
    public Collection<MerchantType> getRegisteredMerchantTypes() { return registeredMerchantTypes; }
}
