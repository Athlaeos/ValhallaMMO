package me.athlaeos.valhallammo.potioneffects.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.potioneffects.EffectClass;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.function.Predicate;

public class CustomWrapper extends PotionEffectWrapper {
    private final boolean singleUse;
    private final Predicate<Double> isPositive;
    private final String defaultIcon;

    public CustomWrapper(String effect, Predicate<Double> isPositive, boolean removable, boolean singleUse, String defaultIcon, StatFormat format) {
        super(effect, removable, getOrDefault(effect).isInstant(), format);
        this.isPositive = isPositive;
        this.singleUse = singleUse;
        this.defaultIcon = defaultIcon;
    }
    public CustomWrapper(String effect, Predicate<Double> isPositive, String defaultIcon, StatFormat format) {
        super(effect, true, getOrDefault(effect).isInstant(), format);
        this.isPositive = isPositive;
        this.singleUse = false;
        this.defaultIcon = defaultIcon;
    }
    private static PotionEffectType getOrDefault(String effect){
        PotionEffectType type = PotionEffectType.getByName(effect);
        return (type == null) ? PotionEffectType.REGENERATION : type;
    }

    @Override
    public void onApply(ItemMeta i) {
        boolean customFlag = CustomFlag.hasFlag(i, CustomFlag.DISPLAY_ATTRIBUTES);
        boolean vanillaFlag = i.hasItemFlag(ItemFlag.HIDE_POTION_EFFECTS);
        // if vanilla, hide if either custom or vanilla flags are missing
        // if not vanilla, hide if vanilla flag is present unless custom flag is also present
        if ((isVanilla && (!customFlag || !vanillaFlag)) || (!isVanilla && (vanillaFlag && !customFlag))) onRemove(i);
        else {
            String translation = getEffectName();
            if (StringUtils.isEmpty(translation)) return;
            String prefix = prefix(isPositive.test(amplifier));
            long duration = this.duration;
            Material base = ItemUtils.getStoredType(i);
            if (base != null){
                if (base == Material.LINGERING_POTION) duration = (long) Math.floor(duration / 4D);
                else if (base == Material.TIPPED_ARROW) duration = (long) Math.floor(duration / 8D);
            }
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
                                    .replace("%value%", format.format(amplifier + (isVanilla ? 1 : 0)))
                                    .replace("%duration%", String.format("(%s)", StringUtils.toTimeStamp(duration, 20))) +
                                    ((this.charges <= 0) ? "" : charges)).trim()
                    )
            );
        }
    }

    @Override
    public void onInflict(LivingEntity entity) {

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
        return isPositive.test(amplifier) ? EffectClass.BUFF : EffectClass.DEBUFF;
    }

    @Override
    public boolean isSingleUse() {
        return singleUse;
    }

    @Override
    public String getEffectIcon() {
        return StringUtils.isEmpty(super.getEffectIcon()) ? ValhallaMMO.isResourcePackConfigForced() ? "&f" + defaultIcon : super.getEffectIcon() : super.getEffectIcon();
    }

    @Override
    public PotionEffectWrapper copy() {
        return new CustomWrapper(getEffect(), isPositive, isRemovable(), singleUse, defaultIcon, format);
    }

    private String prefix(boolean positive){
        return TranslationManager.getTranslation("stat_potion_" + (positive ? "positive" : "negative") + "_prefix");
    }
}
