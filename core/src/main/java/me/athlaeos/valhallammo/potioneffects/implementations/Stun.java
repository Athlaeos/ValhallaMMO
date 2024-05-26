package me.athlaeos.valhallammo.potioneffects.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.event.EntityStunEvent;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import me.athlaeos.valhallammo.potioneffects.EffectClass;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.ConventionUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class Stun extends PotionEffectWrapper {
    private static final Collection<PotionEffectWrapper> stunEffects = new HashSet<>();
    private static final int stunImmunityDuration;

    static {
        YamlConfiguration c = ConfigManager.getConfig("config.yml").reload().get();
        ConfigurationSection stunEffectSection = c.getConfigurationSection("stun_effects");
        stunImmunityDuration = c.getInt("stun_immunity_duration", 5000);
        if (stunEffectSection != null){
            for (String effect : stunEffectSection.getKeys(false)){
                PotionEffectWrapper wrapper = PotionEffectRegistry.getEffect(effect).setAmplifier(c.getDouble("stun_effects." + effect));
                stunEffects.add(wrapper);
            }
        }
    }

    private final String defaultIcon;

    public Stun(String effect, String defaultIcon) {
        super(effect, false, true, null);
        this.defaultIcon = defaultIcon;
    }

    @Override
    public void onApply(ItemMeta i) {
        boolean customFlag = CustomFlag.hasFlag(i, CustomFlag.DISPLAY_ATTRIBUTES);
        boolean vanillaFlag = i.hasItemFlag(ConventionUtils.getHidePotionEffectsFlag());
        boolean temporaryCoatingDisplay = CustomFlag.hasFlag(i, CustomFlag.TEMPORARY_POTION_DISPLAY);
        // if vanilla, hide if either custom or vanilla flags are missing
        // if not vanilla, hide if vanilla flag is present unless custom flag is also present
        if ((isVanilla && i instanceof PotionMeta && (!customFlag || !vanillaFlag)) ||
                (!isVanilla && (!temporaryCoatingDisplay || charges == 0) && (vanillaFlag && !customFlag))) onRemove(i);
        else {
            String translation = getEffectName();
            if (StringUtils.isEmpty(translation)) return;
            String prefix = prefix();
            String charges = TranslationManager.getTranslation("potion_effect_charges_format")
                    .replace("%prefix%", prefix)
                    .replace("%charges_roman%", this.charges >= 0 ? StringUtils.toRoman(this.charges) : "")
                    .replace("%charges_numeric%", String.valueOf(this.charges));
            ItemUtils.replaceOrAddLore(i,
                    translation
                            .replace("%icon%", "")
                            .replace("%value%", "")
                            .replace("%duration%", "").trim(),
                    Utils.chat(prefix +
                            (translation
                                    .replace("%icon%", getEffectIcon() + prefix)
                                    .replace("%value%", "")
                                    .replace("%duration%", String.format("(%s)", StringUtils.toTimeStamp(duration, 20))) +
                                    ((this.charges <= 0) ? "" : charges)).trim()
                    )
            );
        }
    }

    @Override
    public void onInflict(LivingEntity p, LivingEntity causedBy, double amplifier, int duration, double intensity) {
        stunTarget(p, causedBy, (int) (intensity * duration), false);
    }

    /**
     * Stuns a target, which really means all effects that make up the "stun" effect are applied. The stunning entity may
     * be null if no entity is responsible for stunning the target. The stunned entity will be granted stun immunity
     * after being stunned unless "force" is true
     * @param entity the entity to stun
     * @param causedBy the entity that stunned them.
     * @param duration the duration (in game ticks) to stun the target
     * @param force true if the entity should be stunned regardless of immunity, false otherwise
     */
    public static void stunTarget(LivingEntity entity, LivingEntity causedBy, int duration, boolean force){
        double durationMultiplier = force ? 1 : Math.max(0, 1 - AccumulativeStatManager.getRelationalStats("STUN_RESISTANCE", entity, causedBy, true));
        int newDuration = (int) Math.round(duration * durationMultiplier);
        EntityStunEvent event = new EntityStunEvent(entity, causedBy, newDuration);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()){
            if (!(event.getEntity() instanceof LivingEntity l) || (!force && !Timer.isCooldownPassed(entity.getUniqueId(), "stun_immunity"))) return;
            for (PotionEffectWrapper e : stunEffects){
                if (e.isVanilla()){
                    l.addPotionEffect(new PotionEffect(e.getVanillaEffect(), newDuration, (int) e.getAmplifier(), true, false));
                } else {
                    PotionEffectRegistry.addEffect(l, causedBy, new CustomPotionEffect(e, newDuration, e.getAmplifier()), false, 1, EntityPotionEffectEvent.Cause.ATTACK);
                }
            }
            EntityCache.resetPotionEffects((LivingEntity) event.getEntity()); // adding/removing an effect as a result of this method should reset the entity's potion effect cache
            Timer.setCooldown(entity.getUniqueId(), stunImmunityDuration * 50, "stun_immunity");
        }
    }

    public static void attemptStun(LivingEntity entity, LivingEntity causedBy){
        int stunDuration = ValhallaMMO.getPluginConfig().getInt("default_stun_duration", 0) + (int) AccumulativeStatManager.getCachedAttackerRelationalStats("STUN_DURATION_BONUS", entity, causedBy, 10000, true);
        stunTarget(entity, causedBy, stunDuration, false);
    }

    /**
     * Checks if the entity is stunned or not. An entity is considered stunned if they have all of the "stun" effects
     * @param entity the entity
     * @return true if they're considered stunned, false otherwise
     */
    public static boolean isStunned(LivingEntity entity){
        Map<String, CustomPotionEffect> activeEffects = PotionEffectRegistry.getActiveEffects(entity);
        return stunEffects.stream().map(PotionEffectWrapper::getEffect).allMatch(activeEffects::containsKey);
    }

    @Override
    public void onRemove(ItemMeta i) {
        String translation = getEffectName();
        if (StringUtils.isEmpty(translation)) return;
        ItemUtils.removeIfLoreContains(i, translation
                .replace("%icon%", "")
                .replace("%value%", "")
                .replace("%duration%", "").trim());
    }

    @Override
    public void onExpire(LivingEntity entity) {

    }

    @Override
    public EffectClass getClassification(double amplifier) {
        return EffectClass.DEBUFF;
    }

    @Override
    public String getEffectIcon() {
        return StringUtils.isEmpty(super.getEffectIcon()) ? ValhallaMMO.isResourcePackConfigForced() ? "&f" + defaultIcon : super.getEffectIcon() : super.getEffectIcon();
    }

    @Override
    public boolean isSingleUse() {
        return false;
    }

    @Override
    public PotionEffectWrapper copy() {
        return new Stun(getEffect(), defaultIcon).setCharges(charges).setDuration(duration);
    }

    private String prefix(){
        return TranslationManager.getTranslation("stat_potion_negative_prefix");
    }

    @Override
    public StatFormat getFormat() {
        return StatFormat.NONE;
    }
}
