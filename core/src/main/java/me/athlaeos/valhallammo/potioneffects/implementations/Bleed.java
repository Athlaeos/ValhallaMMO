package me.athlaeos.valhallammo.potioneffects.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.CombatType;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.potioneffects.EffectClass;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.utility.Bleeder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.ConventionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

public class Bleed extends PotionEffectWrapper {
    private final String defaultIcon;

    public Bleed(String effect, String defaultIcon) {
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
            int stacks = (int) Math.max(1, Math.floor(this.amplifier)); // the amount of stacks applied will be the first decimal number of the amplifier
            double damage = (this.amplifier - stacks) * 10; // the damage will be the decimals of the amplifier multiplied by 10
            // example: 2.05 will deal 2 stacks of bleed each doing 0.5 damage (0.05 * 10)
            ItemUtils.replaceOrAddLore(i,
                    translation
                            .replace("%icon%", "")
                            .replace("%value%", "")
                            .replace("%duration%", "").trim(),
                    Utils.chat(prefix +
                            (translation
                                    .replace("%icon%", getEffectIcon() + prefix)
                                    .replace("%value%", String.format("%s %.1f", StringUtils.toRoman(Math.max(1, stacks)), damage))
                                    .replace("%duration%", String.format("(%s)", StringUtils.toTimeStamp(duration, 20))) +
                                    ((this.charges <= 0) ? "" : charges)).trim()
                    )
            );
        }
    }

    @Override
    public void onInflict(LivingEntity p, LivingEntity causedBy, double amplifier, int duration, double intensity) {
        int stacks = (int) Math.max(1, Math.floor(amplifier)); // the amount of stacks applied will be the first decimal number of the amplifier
        double damage = (amplifier - stacks) * 10; // the damage will be the decimals of the amplifier multiplied by 10
        Bleeder.inflictBleed(p, causedBy, (int) (intensity * duration), damage, stacks, CombatType.RANGED);
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
        return new Bleed(getEffect(), defaultIcon).setCharges(charges).setDuration(duration).setAmplifier(amplifier);
    }

    private String prefix(){
        return TranslationManager.getTranslation("stat_potion_negative_prefix");
    }

    @Override
    public StatFormat getFormat() {
        return StatFormat.ROMAN;
    }
}
