package me.athlaeos.valhallammo.nms;

import org.bukkit.entity.Player;

public interface NetworkHandler {
    default void readAfter(Player player, Object packet) {}
    default void writeAfter(Player player, Object packet) {}
    default PacketStatus readBefore(Player player, Object packet) { return PacketStatus.ALLOW; }
    default PacketStatus writeBefore(Player player, Object packet) { return PacketStatus.ALLOW; }

    enum PacketStatus{
        ALLOW, DENY
    }
}
