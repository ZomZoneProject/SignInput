package net.monkeyfunky.devteam.signinput;

import com.google.common.base.Preconditions;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftSign;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

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

    public void open(Player player, BiConsumer<Player, String> consumer) {
        final BlockPosition blockPosition = new BlockPosition(player.getLocation().getBlockX(), 1, player.getLocation().getBlockZ());

        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(blockPosition, CraftMagicNumbers.getBlock(Material.OAK_SIGN, (byte) 0));
        sendPacket(player, packet);

        IChatBaseComponent[] components = CraftSign.sanitizeLines(lines);
        TileEntitySign sign = new TileEntitySign();
        sign.setPosition(blockPosition);
        sign.setColor(EnumColor.BLACK);

        for (int i = 0; i < components.length; i++)
            sign.a(i, components[i]);

        sendPacket(player, sign.getUpdatePacket());

        PacketPlayOutOpenSignEditor outOpenSignEditor = new PacketPlayOutOpenSignEditor(blockPosition);
        sendPacket(player, outOpenSignEditor);

        SignInput.getInstance().addMap(player.getUniqueId(), consumer);
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

    private void sendPacket(Player player, Packet<?> packet) {
        Preconditions.checkNotNull(player);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
