package me.athlaeos.valhallammo.nms;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.block.DigPacketInfo;
import me.athlaeos.valhallammo.listeners.CustomBreakSpeedListener;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class BlockBreakNetworkHandlerImpl implements NetworkHandler{
    @Override
    public PacketStatus readBefore(Player player, Object packet) {
        if (player.getGameMode() == GameMode.CREATIVE) return PacketStatus.ALLOW;
        DigPacketInfo info = ValhallaMMO.getNms().readDiggingPacket(player, packet);
        if (info == null || info.getType() == DigPacketInfo.Type.INVALID) return PacketStatus.ALLOW;
        CustomBreakSpeedListener.onStart(info);
        return PacketStatus.ALLOW;
    }

    @Override
    public void readAfter(Player player, Object packet) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        DigPacketInfo info = ValhallaMMO.getNms().readDiggingPacket(player, packet);
        if (info == null || info.getType() == DigPacketInfo.Type.INVALID) return;

        CustomBreakSpeedListener.onStop(info);
    }
}
