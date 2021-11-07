package net.monkeyfunky.devteam.signinput;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
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
                if (packet.getClass().getName().contains("PacketPlayInUpdateSign") && SignInput.getInstance().getMap().containsKey(player.getUniqueId())) {
                    Field field = packet.getClass().getDeclaredField("b");

                    String[] strings = (String[]) field.get(packet);

                    BiConsumer<Player, String> consumer = SignInput.getInstance().getConsumer(player);
                    new InputGUI.Result(player, strings[2]).then(consumer);
                    SignInput.getInstance().removeMap(player.getUniqueId());
                }

                super.channelRead(ctx, packet);
            }
        };

        try {
            Class<?> playerClass = Class.forName("org.bukkit.craftbukkit." + SignInput.getServerVersion() + ".entity.CraftPlayer");
            Object craftPlayer = playerClass.cast(player);
            Object handle = playerClass.getMethod("getHandle").invoke(craftPlayer);

            Field playerConnectionField = handle.getClass().getDeclaredField("playerConnection");
            Object playerConnection = playerConnectionField.get(handle);

            Field networkManagerField = playerConnection.getClass().getDeclaredField("networkManager");
            Object networkManager = networkManagerField.get(playerConnection);

            Field channelField = networkManager.getClass().getField("channel");
            Channel channel = (Channel) channelField.get(networkManager);

            final ChannelPipeline pipeline = channel.pipeline();

            pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();

        try {
            Class<?> playerClass = Class.forName("org.bukkit.craftbukkit." + SignInput.getServerVersion() + ".entity.CraftPlayer");
            Object craftPlayer = playerClass.cast(player);
            Object handle = playerClass.getMethod("getHandle").invoke(craftPlayer);

            Field playerConnectionField = handle.getClass().getDeclaredField("playerConnection");
            Object playerConnection = playerConnectionField.get(handle);

            Field networkManagerField = playerConnection.getClass().getDeclaredField("networkManager");
            Object networkManager = networkManagerField.get(playerConnection);

            Field channelField = networkManager.getClass().getField("channel");
            final Channel channel = (Channel) channelField.get(networkManager);

            channel.eventLoop().submit(() -> channel.pipeline().remove(event.getPlayer().getName()));
            SignInput.getInstance().removeMap(event.getPlayer().getUniqueId());
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }
}
