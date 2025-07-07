package me.athlaeos.valhallammo.potioneffects.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.potioneffects.EffectClass;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.ConventionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.meta.PotionMeta;

public class Milk extends PotionEffectWrapper {
    private final String defaultIcon;

    public Milk(String effect, String defaultIcon) {
        super(effect, false, true, null);
        this.defaultIcon = defaultIcon;
    }

    @Override
    public void onApply(ItemBuilder i) {
        boolean customFlag = CustomFlag.hasFlag(i.getMeta(), CustomFlag.DISPLAY_ATTRIBUTES);
        boolean vanillaFlag = i.getMeta().hasItemFlag(ConventionUtils.getHidePotionEffectsFlag());
        boolean temporaryCoatingDisplay = CustomFlag.hasFlag(i.getMeta(), CustomFlag.TEMPORARY_POTION_DISPLAY);
        // if vanilla, hide if either custom or vanilla flags are missing
        // if not vanilla, hide if vanilla flag is present unless custom flag is also present
        if ((isVanilla && i.getMeta() instanceof PotionMeta && (!customFlag || !vanillaFlag)) ||
                (!isVanilla && (!temporaryCoatingDisplay || charges == 0) && (vanillaFlag && !customFlag))) onRemove(i);
        else {
            String translation = getEffectName();
            if (StringUtils.isEmpty(translation)) return;
            String prefix = prefix();
            String charges = TranslationManager.getTranslation("potion_effect_charges_format")
                    .replace("%prefix%", prefix)
                    .replace("%charges_roman%", StringUtils.toRoman(this.charges))
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
                                    .replace("%duration%", "") +
                                    ((this.charges <= 0) ? "" : charges)).trim()
                    )
            );
        }
    }

    @Override
    public void onInflict(LivingEntity p, LivingEntity causedBy, double amplifier, int duration, double intensity) {
        PotionEffectRegistry.removePotionEffects(p, EntityPotionEffectEvent.Cause.MILK, eff -> eff.getWrapper().isRemovable());
        p.getActivePotionEffects().forEach(eff -> p.removePotionEffect(eff.getType()));
    }

    @Override
    public void onRemove(ItemBuilder i) {
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
        return EffectClass.BUFF;
    }

    @Override
    public boolean isSingleUse() {
        return false;
    }

    @Override
    public String getEffectIcon() {
        return StringUtils.isEmpty(super.getEffectIcon()) ? ValhallaMMO.isResourcePackConfigForced() ? "&f" + defaultIcon : super.getEffectIcon() : super.getEffectIcon();
    }

    @Override
    public PotionEffectWrapper copy() {
        return new Milk(getEffect(), defaultIcon).setCharges(charges);
    }

    private String prefix(){
        return TranslationManager.getTranslation("stat_potion_positive_prefix");
    }

    @Override
    public StatFormat getFormat() {
        return StatFormat.NONE;
    }
}
