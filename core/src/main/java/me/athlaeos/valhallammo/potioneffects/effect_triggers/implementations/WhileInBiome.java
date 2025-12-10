package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class WhileInBiome implements EffectTrigger.ConstantConfigurableTrigger{
    private final boolean inverted;
    public WhileInBiome(boolean inverted){
        this.inverted = inverted;
    }

    @Override
    public boolean shouldTrigger(LivingEntity entity, String arg) {
        String args = getArg(arg);
        if (args.isEmpty()) return false;
        String[] biomes = args.split("/");
        if (biomes.length == 0) return false;
        Collection<String> toMatchOn = new HashSet<>(Set.of(biomes));

        return inverted != toMatchOn.contains(entity.getLocation().getBlock().getBiome().toString());
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public String id() {
        return "while_"+ (inverted ? "not_" : "") + "in_biomes_";
    }

    @Override
    public void onRegister() {
        // do nothing
    }

    @Override
    public String isValid(String arg) {
        String args = getArg(arg);
        String[] effects = args.split("/");
        if (effects.length == 0) return "&cInsufficient arguments. Format is <biomes separated by slashes>. (look up \"spigot biome\" for a list of biomes)";
        return null;
    }

    @Override
    public String getUsage() {
        return "\"<biomes separated by slashes>\", such as \"BADLANDS/BEACH/DARK_FOREST/DEEP_LUKEWARM_OCEAN\" (look up \"spigot biome\" for a list of biomes)";
    }

    @Override
    public String asLore(String rawID) {
        String args = getArg(rawID);
        String[] biomes = args.split("/");
        if (biomes.length == 0) return "&cImproperly configured";
        return "&fBe in any of the following biomes to trigger: " + String.join(", ", biomes);
    }
}