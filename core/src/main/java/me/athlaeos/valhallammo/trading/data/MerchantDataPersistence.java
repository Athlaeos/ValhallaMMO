package me.athlaeos.valhallammo.trading.data;

import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.utility.Callback;

import java.util.*;

public abstract class MerchantDataPersistence {
    protected final Map<UUID, MerchantData> allData = new HashMap<>();

    public Map<UUID, MerchantData> getAllData() {
        return new HashMap<>(allData);
    }

    public abstract void setData(UUID id, MerchantData data);

    public abstract void getData(UUID id, Callback<MerchantData> onFinish);

    /**
     * @return true if the data for this id is already held in memory, meaning
     * {@link #getData(UUID, Callback)} will invoke its callback synchronously.
     */
    public boolean isCached(UUID id){
        return allData.containsKey(id);
    }

    public abstract void saveAllData();

    public abstract void saveData(UUID id, MerchantData data);

    public abstract void removeData(UUID id);
}

