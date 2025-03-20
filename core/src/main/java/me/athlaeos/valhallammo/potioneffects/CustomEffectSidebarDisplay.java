package me.athlaeos.valhallammo.potioneffects;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.utility.Scheduling;
import me.athlaeos.valhallammo.utility.SideBarUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class CustomEffectSidebarDisplay implements CustomEffectDisplay{
    @Override
    public void start() {
        Scheduling.runTaskTimer(ValhallaMMO.getInstance(), () -> {
            for (UUID uuid : new HashSet<>(PotionEffectRegistry.affectedEntityTracker())){
                Entity e = ValhallaMMO.getInstance().getServer().getEntity(uuid);
                if (!(e instanceof Player p)) continue;
                if (!e.isValid()){
                    PotionEffectRegistry.affectedEntityTracker().remove(e.getUniqueId());
                    continue;
                }
                PowerProfile profile = ProfileCache.getOrCache(p, PowerProfile.class);
                if (profile.hidePotionEffectBar()) continue;

                Map<String, CustomPotionEffect> activeEffects = EntityCache.getAndCacheProperties(p).getActivePotionEffects()
                        .entrySet().stream().filter((ef) -> ef.getValue().getEffectiveUntil() == -1 || ef.getValue().getRemainingDuration() > 0)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                if (activeEffects.isEmpty()) {
                    PotionEffectRegistry.affectedEntityTracker().remove(e.getUniqueId());
                    SideBarUtils.hideSideBarFromPlayer(p, "valhalla_effects");
                } else {
                    List<String> effects = new ArrayList<>();
                    List<CustomPotionEffect> sortedEffects = new ArrayList<>(activeEffects.values());
                    sortedEffects.sort(Comparator.comparingLong(CustomPotionEffect::getRemainingDuration));
                    Collections.reverse(sortedEffects);
                    for (CustomPotionEffect effect : sortedEffects){
                        PotionEffectWrapper wrapper = effect.getWrapper();
                        if (wrapper.isVanilla || wrapper.isInstant) continue;
                        String prefix = PotionEffectWrapper.prefix(wrapper.getClassification(effect.getAmplifier()) == EffectClass.BUFF);
                        effects.add(Utils.chat(prefix +
                                (wrapper.getEffectName()
                                        .replace("%icon%", wrapper.getEffectIcon() + prefix)
                                        .replace("%value%", wrapper.format.format(effect.getAmplifier()))
                                        .replace("%duration%", String.format("(%s)", StringUtils.toTimeStamp(effect.getRemainingDuration(), 1000))))
                                        .trim()
                        ));
                    }
                    SideBarUtils.showSideBarToPlayer(p, "valhalla_effects", Utils.chat(TranslationManager.getTranslation("potion_effect_display")), effects, false);
                }
            }
        }, 1L, 20L);
    }
}
