package me.athlaeos.valhallammo.item.throwable_weapon_animations;

import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public abstract class ThrowableWeaponAnimation {
    private final String name;

    public ThrowableWeaponAnimation(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract void tick(ThrownItem item);
    public abstract ThrownItem throwItem(Player thrower, ItemBuilder thrownItem, EquipmentSlot fromHand);
}
