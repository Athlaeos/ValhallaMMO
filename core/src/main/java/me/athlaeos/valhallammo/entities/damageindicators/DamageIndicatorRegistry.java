package me.athlaeos.valhallammo.entities.damageindicators;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.CustomDamageType;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.entities.damageindicators.implementations.DecentHologramsDPSIndicator;
import me.athlaeos.valhallammo.entities.damageindicators.implementations.TextDisplayDPSIndicator;
import me.athlaeos.valhallammo.hooks.DecentHologramsHook;
import org.bukkit.entity.LivingEntity;

import java.util.*;

public class DamageIndicatorRegistry {
    private static final List<DamageIndicatorStrategy> strategies = new ArrayList<>();
    private static final Collection<UUID> critIndicator = new HashSet<>();
    static {
        if (ValhallaMMO.isHookFunctional(DecentHologramsHook.class)) strategies.add(new DecentHologramsDPSIndicator());
        if (!MinecraftVersion.currentVersionOlderThan(MinecraftVersion.MINECRAFT_1_19)) strategies.add(new TextDisplayDPSIndicator());
    }

    public static boolean sendDamageIndicator(LivingEntity l, CustomDamageType damageType, double damage, double mitigated){
        for (DamageIndicatorStrategy s : strategies.stream().filter(s -> s.use(l)).toList()){
            return critIndicator.remove(l.getUniqueId()) ? s.sendCriticalDamage(l, damageType, damage, mitigated) : s.sendDamage(l, damageType, damage, mitigated);
        }
        return false;
    }

    public static void markCriticallyHit(LivingEntity l){
        critIndicator.add(l.getUniqueId());
    }

    public static List<DamageIndicatorStrategy> getStrategies() {
        return strategies;
    }

    /**
     * Registers a damage indicator strategy on the end of the list.
     * Strategies on the end of the list will have lowest priority
     * @param strategy the strategy to register
     */
    public static void registerStrategy(DamageIndicatorStrategy strategy){
        strategies.add(strategy);
    }
}
