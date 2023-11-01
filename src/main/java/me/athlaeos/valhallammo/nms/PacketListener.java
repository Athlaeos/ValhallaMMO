package me.athlaeos.valhallammo.nms;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PacketListener implements Listener {

    private final NetworkHandler handler;
    private final Map<UUID, Channel> channels = new HashMap<>();

    public PacketListener(NetworkHandler handler){
        this.handler = handler;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        addChannel(e.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        closeChannel(e.getPlayer());
    }

    public void addChannel(Player p){
        ChannelDuplexHandler duplexHandler = new ChannelDuplexHandler(){
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception{
                if (handler.readBefore(p, packet) == NetworkHandler.PacketStatus.DENY) return;
                super.channelRead(ctx, packet);
                handler.readAfter(p, packet);
            }

            @Override
            public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception{
                if (handler.writeBefore(p, packet) == NetworkHandler.PacketStatus.DENY) return;
                super.write(ctx, packet, promise);
                handler.writeAfter(p, packet);
            }
        };

        Channel channel = ValhallaMMO.getNms().channel(p);
        channels.put(p.getUniqueId(), channel);
        channel.pipeline().addBefore("packet_handler", "valhalla_" + p.getUniqueId(), duplexHandler);
    }

    public void closeChannel(Player p){
        Channel channel = channels.get(p.getUniqueId());
        if (channel == null) return;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove("valhalla_" + p.getUniqueId());
            return null;
        });
    }

    public void addAll(){
        for (Player p : ValhallaMMO.getInstance().getServer().getOnlinePlayers())
            addChannel(p);
    }

    public void closeAll(){
        for (Player p : ValhallaMMO.getInstance().getServer().getOnlinePlayers())
            closeChannel(p);
    }

    public void sendPacket(Player p, Object packet){
        Channel channel = channels.get(p.getUniqueId());
        if (channel == null) return;
        channel.pipeline().writeAndFlush(packet);
    }
}
