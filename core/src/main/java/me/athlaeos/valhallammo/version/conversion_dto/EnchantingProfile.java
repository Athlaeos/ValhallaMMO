package me.athlaeos.valhallammo.version.conversion_dto;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EnchantingProfile extends Profile implements Serializable {
    private static final NamespacedKey enchantingProfileKey = ValhallaMMO.key("valhalla_profile_enchanting");

    private float vanillaenchantmentamplifychance = 0F; // chance for vanilla enchantments to be amplified (or weakened)
    private int maxcustomenchantmentsallowed = 0; // amount of custom enchantments one is allowed to add to an item
    private float lapissavechance = 0F; // chance for lapis lazuli to be refunded to the player
    private float exprefundchance = 0F; // chance for experience to be refunded to the player
    private float exprefundfraction = 0F; // amount of experience refunded if chance procs, capped at 1.0
    private float vanillaexpgainmultiplier = 1F; // multiplier of the player's experience gained

    private int enchantingquality_general = 0; // general enchanting skill
    private int enchantingquality_vanilla = 0; // vanilla enchanting skill
    private int enchantingquality_custom = 0; // custom enchanting skill
    private int anvilquality = 0; // custom anvil enchantment combining skill (higher skill = higher max lv enchantment combining)

    private boolean cantransferenchantments = false; // if true, adding a plain book to a grindstone transfers the enchantments of the item to the book
    private float sharpnessbonus = 0F; // multiplier bonus for Sharpness' effectiveness, for example 0.5 will increase the damage of Sharpness I from 1 to 1.5
    private float smitebonus = 0F; // same for sharpness, but for Smite
    private float boabonus = 0F; // same for sharpness, but for Bane of Arthropods
    private float fortunebonus = 0F; // provides an additional drop multiplier bonus per level
    private float efficiencybonus = 0F; // when implemented, provides a mining speed bonus for Efficiency's effectiveness. Efficiency I for wooden pickaxes for example provides +150% mining speed, if this value is 0.5 it will be +225%
    private float knockbackbonus = 0F; // provides an additional bonus to the effectiveness of Knockback
    private float lootingbonus = 0F; // provides an additional mob drop multiplier bonus per level
    private float fireaspectbonus = 0F; // provides an additional fire tick duration multiplier bonus per level
    private float powerbonus = 0F; // same for sharpness, but for Power
    private float flamebonus = 0F; // same for fire aspect, but for Flame
    private float blastprotectionbonus = 0F; // provides an additional blast protection multiplier bonus
    private float featherfallingbonus = 0F; // provides an additional feather falling multiplier bonus
    private float fireprotectionbonus = 0F; // provides an additional fire protection multiplier bonus
    private float projectileprotectionbonus = 0F; // provides an additional projectile protection multiplier bonus
    private float protectionbonus = 0F; // provides an additional protection multiplier bonus
    private float soulspeedbonus = 0F; // provides an additional movement speed multiplier bonus for soul speed
    private float thornsbonus = 0F; // provides an additional reflect damage multiplier bonus
    private float lurebonus = 0F; // provides additional fishing time reduction per level
    private float lotsbonus = 0F; // provides additional fishing tier per level
    private float impalingbonus = 0F; // same for sharpness, but for Impaling
    private float swiftsneakbonus = 0F; // same for soul speed, but for swift sneak

    private float environmentalenchantmentbonus = 0F; // general bonus describing non-combat enchantments like Fortune, Efficiency, Looting, Swift Sneak, Soul Speed. Not used in the plugin natively, but can be used for external plugins
    private float protectiveenchantmentbonus = 0F; // general bonus describing protective enchantments like Projectile Protection, Feather Falling, Protection, etc.
    private float offensiveenchantmentbonus = 0F; // general bonus describing offensive enchantments like Sharpness, Smite, Power, etc.

    private double enchantingexpmultiplier_general = 100D;
    private double enchantingexpmultiplier_custom = 100D;
    private double enchantingexpmultiplier_vanilla = 100D;


    @Override
    public Profile fetchProfile(Player p, DatabaseConnection conn) throws SQLException {
        PreparedStatement stmt = conn.getConnection().prepareStatement("SELECT * FROM profiles_enchanting WHERE owner = ?;");
        stmt.setString(1, p.getUniqueId().toString());
        ResultSet result = stmt.executeQuery();
        if (result.next()){
            EnchantingProfile profile = new EnchantingProfile(p);
            profile.setLevel(result.getInt("level"));
            profile.setExp(result.getDouble("exp"));
            profile.setLifetimeEXP(result.getDouble("exp_total"));
            result.getFloat("vanillaenchantmentamplifychance");
            if (result.wasNull()) return null;
            return profile;
        }
        return null;
    }

    public EnchantingProfile(Player owner){
        super(owner);
        if (owner == null) return;
        this.key = enchantingProfileKey;
    }

    @Override
    public NamespacedKey getKey() {
        return enchantingProfileKey;
    }

    @Override
    public EnchantingProfile clone() throws CloneNotSupportedException {
        return (EnchantingProfile) super.clone();
    }
}
