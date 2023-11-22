package me.athlaeos.valhallammo.animations;

import me.athlaeos.valhallammo.animations.implementations.*;

import java.util.HashMap;
import java.util.Map;

public class AnimationRegistry {
    public static Animation BLOCK_PARTICLE_PUFF = new BlockParticlePuff("block_particle_puff");
    public static Animation BLOCK_SPARKS_CRAFTSOUND = new BlockSparksCraftSound("block_sparks_craftsound");
    public static Animation BLOCK_BUBBLES = new BlockBubbles("block_bubbles");
    public static Animation BLOCK_SPARKS_EXTINGUISH = new BlockSparksExtinguishSound("block_sparks_extinguish");
    public static Animation ENTITY_SPARK_FLASH = new EntitySparkFlash("entity_spark_flash");
    public static Animation ENTITY_FLASH = new EntityFlash("entity_flash");
    public static Animation HIT_ELECTRIC = new HitElectric("hit_electric");
    public static Animation HIT_EXPLOSION = new HitExplosionBurst("hit_explosion");
    public static Animation HIT_FIRE = new HitFireFlame("hit_fire");
    public static Animation HIT_FREEZING = new HitFreezingPuff("hit_freezing");
    public static Animation HIT_MAGIC = new HitMagicPuff("hit_magic");
    public static Animation HIT_NECROTIC = new HitNecroticPuff("hit_necrotic");
    public static Animation HIT_POISON = new HitPoisonPuff("hit_poison");
    public static Animation HIT_RADIANT = new HitRadiantFlash("hit_radiant");
    public static Animation CHARGED_SHOT_ACTIVATION = new ChargedShotActivation("charged_shot_activation");
    public static Animation CHARGED_SHOT_AMMO = new ChargedShotAmmo("charged_shot_ammo");
    public static Animation CHARGED_SHOT_FIRE = new ChargedShotFire("charged_shot_fire");
    public static Animation CHARGED_SHOT_SONIC_BOOM = new ChargedShotSonicBoom("charged_shot_sonic_boom");
    public static Animation DRILLING_ACTIVE = new DrillingActive("drilling_active");
    public static Animation MULTI_JUMP = new MultiJump("multi_jump");
    public static Animation ELEMENTAL_BLADE_ACTIVATION = new ElementalBladeActivation("elemental_blade_activation");
    public static Animation ELEMENTAL_BLADE_EXPIRATION = new ElementalBladeExpiration("elemental_blade_expiration");

    private static final Map<String, Animation> animationRegistry = new HashMap<>();
    static{
        register(BLOCK_PARTICLE_PUFF);
        register(BLOCK_SPARKS_CRAFTSOUND);
        register(BLOCK_BUBBLES);
        register(BLOCK_SPARKS_EXTINGUISH);
        register(ENTITY_SPARK_FLASH);
        register(ENTITY_FLASH);
        register(HIT_ELECTRIC);
        register(HIT_EXPLOSION);
        register(HIT_FIRE);
        register(HIT_FREEZING);
        register(HIT_MAGIC);
        register(HIT_NECROTIC);
        register(HIT_POISON);
        register(HIT_RADIANT);
        register(CHARGED_SHOT_ACTIVATION);
        register(CHARGED_SHOT_AMMO);
        register(CHARGED_SHOT_FIRE);
        register(CHARGED_SHOT_SONIC_BOOM);
        register(DRILLING_ACTIVE);
        register(MULTI_JUMP);
        register(ELEMENTAL_BLADE_ACTIVATION);
        register(ELEMENTAL_BLADE_EXPIRATION);
    }

    public static void register(Animation animation){
        animationRegistry.put(animation.id(), animation);
    }

    public static Animation getAnimation(String id){
        return animationRegistry.get(id);
    }
}
