package me.athlaeos.valhallammo.crafting.craftanimations;

import me.athlaeos.valhallammo.crafting.craftanimations.implementations.CauldronBoilFinish;
import me.athlaeos.valhallammo.crafting.craftanimations.implementations.CauldronBoilProcess;
import me.athlaeos.valhallammo.crafting.craftanimations.implementations.ImmersiveCraftFinish;
import me.athlaeos.valhallammo.crafting.craftanimations.implementations.ImmersiveCraftProcess;

import java.util.HashMap;
import java.util.Map;

public class AnimationRegistry {
    public static CraftAnimation IMMERSIVE_CRAFT_PROCESS = new ImmersiveCraftProcess("generic_craft_process");
    public static CraftAnimation IMMERSIVE_CRAFT_FINISH = new ImmersiveCraftFinish("generic_craft_finish");
    public static CraftAnimation CAULDRON_BOIL_PROCESS = new CauldronBoilProcess("generic_boil_process");
    public static CraftAnimation CAULDRON_BOIL_FINISH = new CauldronBoilFinish("generic_boil_finish");

    private static final Map<String, CraftAnimation> animationRegistry = new HashMap<>();
    static{
        setAnimation(IMMERSIVE_CRAFT_PROCESS);
        setAnimation(IMMERSIVE_CRAFT_FINISH);
        setAnimation(CAULDRON_BOIL_PROCESS);
        setAnimation(CAULDRON_BOIL_FINISH);
    }

    public static void setAnimation(CraftAnimation animation){
        animationRegistry.put(animation.id(), animation);
    }

    public static CraftAnimation getAnimation(String id){
        return animationRegistry.get(id);
    }
}
