package me.athlaeos.valhallammo.trading.happiness;

import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.happiness.sources.*;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

public class HappinessSourceRegistry {
    private static final Map<String, HappinessSource> sources = new HashMap<>();
    private static final Map<UUID, CacheEntry> cache = new HashMap<>();
    private static final Map<UUID, Map<UUID, CacheEntry>> relationalCache = new HashMap<>();
    private static final long CACHE_DURATION = 10000;
    private static final boolean feedback = CustomMerchantManager.getTradingConfig().getBoolean("happiness_feedback");
    private static final long feedbackDelay = CustomMerchantManager.getTradingConfig().getLong("happiness_feedback_delay");
    private static final Map<UUID, Map<UUID, Long>> feedbackMap = new HashMap<>();

    static {
        registerSource(new Brightness());
        registerSource(new Comfort());
        registerSource(new Company());
        registerSource(new Health());
        registerSource(new PlayerRelation());
        registerSource(new Security());
        registerSource(new Space());
        registerSource(new Stress());
        registerSource(new Trust());
    }

    public static float getHappiness(Player interactingPlayer, Entity entity, boolean sendMessage){
        CacheEntry cachedEntry;
        if (interactingPlayer != null) cachedEntry = relationalCache.getOrDefault(entity.getUniqueId(), new HashMap<>()).get(interactingPlayer.getUniqueId());
        else cachedEntry = cache.get(entity.getUniqueId());
        if (cachedEntry != null && cachedEntry.time + CACHE_DURATION > System.currentTimeMillis()) {
            if (feedback && sendMessage && interactingPlayer != null) trySendFeedback(cachedEntry.valuesPerSource, interactingPlayer, entity, false);
            return cachedEntry.value;
        }

        Map<String, Float> perSource = new HashMap<>();
        float happiness = 0;
        for (HappinessSource source : sources.values()){
            if (source.appliesTo(entity)) {
                float value = source.get(interactingPlayer, entity);
                happiness += value;
                perSource.put(source.id(), value);
            }
        }
        cachedEntry = new CacheEntry(entity.getUniqueId(), System.currentTimeMillis(), perSource, happiness);
        if (interactingPlayer != null){
            Map<UUID, CacheEntry> entries = relationalCache.getOrDefault(entity.getUniqueId(), new HashMap<>());
            entries.put(interactingPlayer.getUniqueId(), cachedEntry);
            relationalCache.put(entity.getUniqueId(), entries);
        } else cache.put(entity.getUniqueId(), cachedEntry);
        if (feedback && sendMessage && interactingPlayer != null) trySendFeedback(cachedEntry.valuesPerSource, interactingPlayer, entity, false);
        return happiness;
    }

    public static void trySendFeedback(Map<String, Float> valuesPerSource, Player interactingPlayer, Entity entity, boolean ignoreDelay){
        if (!ignoreDelay) {
            Map<UUID, Long> merchantDelays = feedbackMap.getOrDefault(entity.getUniqueId(), new HashMap<>());
            long canSendAt = merchantDelays.getOrDefault(interactingPlayer.getUniqueId(), 0L);
            if (canSendAt >= System.currentTimeMillis()) return;
            merchantDelays.put(interactingPlayer.getUniqueId(), System.currentTimeMillis() + feedbackDelay);
            feedbackMap.put(entity.getUniqueId(), merchantDelays);
        }
        List<HappinessSource> sorted = new ArrayList<>(sources.values());
        sorted.sort(Comparator.comparing(HappinessSource::id));
        for (HappinessSource source : sorted){
            String message = source.getHappinessStatus(valuesPerSource.getOrDefault(source.id(), 0F), interactingPlayer, entity);
            if (StringUtils.isEmpty(message)) continue;
            Utils.sendMessage(interactingPlayer, message);
        }
    }

    public static void registerSource(HappinessSource source){
        sources.put(source.id(), source);
    }

    public static Map<String, HappinessSource> getSources(){
        return new HashMap<>(sources);
    }

    private record CacheEntry(UUID uuid, Long time, Map<String, Float> valuesPerSource, float value){}

    public static String happyPrefix(){
        return TranslationManager.getTranslation("prefix_happy");
    }

    public static String neutralPrefix(){
        return TranslationManager.getTranslation("prefix_neutral");
    }

    public static String unhappyPrefix(){
        return TranslationManager.getTranslation("prefix_unhappy");
    }
}
