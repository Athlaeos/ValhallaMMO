package me.athlaeos.valhallammo.dom;

import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Collection;

public record EquippableWrapper(String modelKey, EquipmentSlot slot, String cameraOverlayKey, Sound equipSound, Collection<EntityType> allowedTypes){}
