package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class WhileOnBlock implements EffectTrigger.ConstantConfigurableTrigger{
    private final boolean inverted;
    public WhileOnBlock(boolean inverted){
        this.inverted = inverted;
    }

    @Override
    public boolean shouldTrigger(LivingEntity entity, String arg) {
        String args = getArg(arg);
        if (args.isEmpty()) return false;
        String[] blocks = args.split("/");
        if (blocks.length == 0) return false;
        Collection<String> toMatchOn = new HashSet<>(Set.of(blocks));
        Block below = entity.getLocation().getBlock().getRelative(BlockFace.DOWN);

        return inverted != toMatchOn.contains(below.getType().toString());
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public String id() {
        return "while_"+ (inverted ? "not_" : "") + "on_blocks_";
    }

    @Override
    public void onRegister() {
        // do nothing
    }

    @Override
    public String isValid(String arg) {
        String args = getArg(arg);
        String[] effects = args.split("/");
        if (effects.length == 0) return "&cInsufficient arguments. Format is <blocks separated by slashes>.";
        return null;
    }

    @Override
    public String getUsage() {
        return "\"<blocks separated by slashes>\", such as \"GRASS_BLOCK/COBBLESTONE/WHITE_CONCRETE\"";
    }

    @Override
    public String asLore(String rawID) {
        String args = getArg(rawID);
        String[] blocks = args.split("/");
        if (blocks.length == 0) return "&cImproperly configured";
        return "&fBe on any of the following blocks to trigger: " + String.join(", ", blocks);
    }
}