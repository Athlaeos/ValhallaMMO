package me.athlaeos.valhallammo.block;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.listeners.CustomBreakSpeedListener;
import me.athlaeos.valhallammo.nms.NetworkHandler;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class BlockBreakNetworkHandlerImpl implements NetworkHandler {
    @Override
    public PacketStatus readBefore(Player player, Object packet) {
        if (player.getGameMode() == GameMode.CREATIVE) return PacketStatus.ALLOW;
        DigPacketInfo info = ValhallaMMO.getNms().readDiggingPacket(player, packet);
        if (info == null || info.getType() == DigPacketInfo.Type.INVALID) return PacketStatus.ALLOW;
        return PacketStatus.ALLOW;
    }

    @Override
    public void readAfter(Player player, Object packet) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        DigPacketInfo info = ValhallaMMO.getNms().readDiggingPacket(player, packet);
        if (info == null || info.getType() == DigPacketInfo.Type.INVALID) return;

        switch (info.getType()){
            case ABORT, STOP, INVALID -> CustomBreakSpeedListener.onStop(info);
            case START -> CustomBreakSpeedListener.onStart(info);
        }
    }
}
