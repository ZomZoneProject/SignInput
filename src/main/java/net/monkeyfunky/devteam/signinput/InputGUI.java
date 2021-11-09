package net.monkeyfunky.devteam.signinput;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;

/**
 * @author eight_y_88
 */

public class InputGUI {
    private final String[] lines;

    public InputGUI(String title) {
        lines = new String[4];
        lines[0] = title;
        lines[1] = "----------";
        lines[3] = "----------";
    }

    @SuppressWarnings("all")
    public void open(Player player, BiConsumer<Player, String> consumer) {
        try {
            Class<?> blockPositionClass = Class.forName("net.minecraft.server." + SignInput.getServerVersion() + ".BlockPosition");
            Class<?> iBlockDataClass = Class.forName("net.minecraft.server." + SignInput.getServerVersion() + ".IBlockData");
            Object blockPosition = blockPositionClass.getDeclaredConstructor(int.class, int.class, int.class).newInstance(player.getLocation().getBlockX(), 1, player.getLocation().getBlockZ());

            Class<?> packetPlayOutBlockChangeClass = Class.forName("net.minecraft.server." + SignInput.getServerVersion() + ".PacketPlayOutBlockChange");
            Class<?> craftMagicNumbersClass = Class.forName("org.bukkit.craftbukkit." + SignInput.getServerVersion() + ".util.CraftMagicNumbers");
            Method getBlockMethod = craftMagicNumbersClass.getDeclaredMethod("getBlock", Material.class, byte.class);
            Object packetPlayOutBlockChange = packetPlayOutBlockChangeClass.getDeclaredConstructor(blockPositionClass, iBlockDataClass).newInstance(blockPosition, getBlockMethod.invoke(null, Material.OAK_SIGN, (byte) 0));

            sendPacket(player, packetPlayOutBlockChange);

            Class<?> tileEntityClass = Class.forName("net.minecraft.server." + SignInput.getServerVersion() + ".TileEntity");
            Class<?> craftSignClass = Class.forName("org.bukkit.craftbukkit." + SignInput.getServerVersion() + ".block.CraftSign");
            Class<?> tileEntitySignClass = Class.forName("net.minecraft.server." + SignInput.getServerVersion() + ".TileEntitySign");
            Class<?> iChatComponentClass = Class.forName("net.minecraft.server." + SignInput.getServerVersion() + ".IChatBaseComponent");
            Method sanitizeLinesMethod = craftSignClass.getDeclaredMethod("sanitizeLines", new Class[]{String[].class});
            sanitizeLinesMethod.setAccessible(true);
            Object[] strings = new Object[]{lines};
            Object[] components = (Object[]) sanitizeLinesMethod.invoke(null, strings);
            Object tileEntitySign = tileEntitySignClass.getConstructor().newInstance();

            Method setPositionMethod = tileEntityClass.getDeclaredMethod("setPosition", blockPositionClass);
            setPositionMethod.invoke(tileEntitySign, blockPosition);

            Method aMethod = tileEntitySignClass.getDeclaredMethod("a", int.class, iChatComponentClass);
            for (int i = 0; i < 4; i++) {
                aMethod.invoke(tileEntitySign, i, components[i]);
            }

            Method getUpdatePacketMethod = tileEntitySignClass.getDeclaredMethod("getUpdatePacket");
            sendPacket(player, getUpdatePacketMethod.invoke(tileEntitySign));

            Class<?> packetPlayOutOpenSignEditorClass = Class.forName("net.minecraft.server." + SignInput.getServerVersion() + ".PacketPlayOutOpenSignEditor");
            Object packetPlayOutOpenSignEditor = packetPlayOutOpenSignEditorClass.getConstructor(blockPositionClass).newInstance(blockPosition);
            sendPacket(player, packetPlayOutOpenSignEditor);

            SignInput.getInstance().addMap(player.getUniqueId(), consumer);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public static class Result {
        private final Player player;
        private final String result;

        public Result(Player player, String result) {
            this.player = player;
            this.result = result;
        }

        public void then(BiConsumer<Player, String> consumer) {
            consumer.accept(player, result);
        }
    }

    private void sendPacket(Player player, Object packet) {
        Preconditions.checkNotNull(player);
        try {
            Class<?> packetClass = Class.forName("net.minecraft.server." + SignInput.getServerVersion() + ".Packet");
            Class<?> playerClass = Class.forName("org.bukkit.craftbukkit." + SignInput.getServerVersion() + ".entity.CraftPlayer");
            Object craftPlayer = playerClass.cast(player);
            Object handle = playerClass.getMethod("getHandle").invoke(craftPlayer);

            Field playerConnectionField = handle.getClass().getDeclaredField("playerConnection");
            Object playerConnection = playerConnectionField.get(handle);

            Method sendPacket = playerConnection.getClass().getDeclaredMethod("sendPacket", packetClass);
            sendPacket.invoke(playerConnection, packet);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }
}
