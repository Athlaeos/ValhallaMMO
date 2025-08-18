package me.athlaeos.valhallammo.placeholder.placeholders;

import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.placeholder.Placeholder;
import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.implementations.GenericWrapper;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PotionEffectPlaceholder extends Placeholder {
    private final int index;
    public PotionEffectPlaceholder(String placeholder, int index) {
        super(placeholder);
        this.index = index;
    }

    @Override
    public String parse(String s, Player p) {
        List<CustomPotionEffect> activeEffects = new ArrayList<>(PotionEffectRegistry.getActiveEffects(p).values());
        activeEffects.sort(Comparator.comparingLong(CustomPotionEffect::getRemainingDuration).thenComparing(e -> e.getWrapper().getEffect()));

        if (index >= activeEffects.size()) return "";
        CustomPotionEffect effect = Catch.catchOrElse(() -> activeEffects.get(index), null);
        if (effect == null) return "";
        return effect.getWrapper().getFormattedEffectName(
                !(effect.getWrapper() instanceof GenericWrapper g) || g.getIsPositive().test(effect.getAmplifier()),
                effect.getAmplifier(),
                effect.getRemainingDuration() / 50
        );
    }
}
