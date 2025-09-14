package me.athlaeos.valhallammo.trading.menu;

import me.athlaeos.valhallammo.trading.dom.MerchantData;

import java.util.UUID;

public interface MerchantMenu {
    UUID getMerchantID();

    MerchantData getData();

    void onClose();
    void onOpen();
}
