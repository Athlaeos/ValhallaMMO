package me.athlaeos.valhallammo.potioneffects.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.potioneffects.EffectClass;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.PotionEffectMappings;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.function.Predicate;

public class GenericWrapper extends PotionEffectWrapper {
    private final boolean singleUse;
    private final Predicate<Double> isPositive;
    private final String defaultIcon;

    public GenericWrapper(String effect, Predicate<Double> isPositive, boolean removable, boolean singleUse, String defaultIcon, StatFormat format) {
        super(effect, removable, getOrDefault(effect).isInstant(), format);
        this.isPositive = isPositive;
        this.singleUse = singleUse;
        this.defaultIcon = defaultIcon;
    }
    public GenericWrapper(String effect, Predicate<Double> isPositive, String defaultIcon, StatFormat format) {
        super(effect, true, getOrDefault(effect).isInstant(), format);
        this.isPositive = isPositive;
        this.singleUse = false;
        this.defaultIcon = defaultIcon;
    }

    private static PotionEffectType getOrDefault(String effect){
        PotionEffectType type = PotionEffectMappings.getPotionEffectType(effect);
        return (type == null) ? PotionEffectType.REGENERATION : type;
    }

    @Override
    public void onApply(ItemBuilder i) {
        boolean customFlag = CustomFlag.hasFlag(i.getMeta(), CustomFlag.DISPLAY_ATTRIBUTES);
        boolean vanillaFlag = i.getMeta().hasItemFlag(MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) ? ItemFlag.valueOf("HIDE_ADDITIONAL_TOOLTIP") : ItemFlag.valueOf("HIDE_POTION_EFFECTS"));
        boolean temporaryCoatingDisplay = CustomFlag.hasFlag(i.getMeta(), CustomFlag.TEMPORARY_POTION_DISPLAY);
        // if vanilla, hide if either custom or vanilla flags are missing
        // if not vanilla, hide if vanilla flag is present unless custom flag is also present
        if (isVanilla && i instanceof PotionMeta && !customFlag ||
                isVanilla && !(i instanceof PotionMeta) && !customFlag && (!temporaryCoatingDisplay || charges == 0) ||
                !isVanilla && (!temporaryCoatingDisplay || charges == 0) && vanillaFlag && !customFlag) onRemove(i);
        else {
            String translation = getEffectName();
            if (StringUtils.isEmpty(translation)) return;
            String prefix = prefix(isPositive.test(amplifier));
            long duration = this.duration;
            Material base = i.getItem().getType();
            if (base == Material.LINGERING_POTION) duration = (long) Math.floor(duration / 4D);
            else if (base == Material.TIPPED_ARROW) duration = (long) Math.floor(duration / 8D);
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
    public void onInflict(LivingEntity entity, LivingEntity causedBy, double amplifier, int duration, double intensity) {

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
        return new GenericWrapper(getEffect(), isPositive, isRemovable(), singleUse, defaultIcon, format).setDuration(duration).setAmplifier(amplifier).setCharges(charges);
    }
}
