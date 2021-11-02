package net.monkeyfunky.devteam.signinput;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.server.v1_16_R3.PacketPlayInUpdateSign;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.function.BiConsumer;

public class EventListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception
            {
                if (packet instanceof PacketPlayInUpdateSign)
                {
                    PacketPlayInUpdateSign inUpdateSign = (PacketPlayInUpdateSign) packet;
                    if (SignInput.getInstance().getMap().containsKey(player.getUniqueId()))
                    {
                        BiConsumer<Player, String> consumer = SignInput.getInstance().getConsumer(player);
                        new InputGUI.Result(player, inUpdateSign.c()[2]).then(consumer);
                        SignInput.getInstance().removeMap(player.getUniqueId());
                    }
                }
                super.channelRead(ctx, packet);
            }
        };
        final ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
        pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        final Channel channel = ((CraftPlayer) event.getPlayer()).getHandle().playerConnection.networkManager.channel;
        channel.eventLoop().submit(() -> channel.pipeline().remove(event.getPlayer().getName()));
        SignInput.getInstance().removeMap(event.getPlayer().getUniqueId());
    }
}
