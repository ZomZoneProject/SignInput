package net.monkeyfunky.devteam.signinput;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class SignInput extends JavaPlugin {
    private static SignInput instance;
    private static HashMap<UUID, BiConsumer<Player, String>> map;

    @Override
    public void onEnable() {
        instance = this;
        map = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
    }

    public static SignInput getInstance() {
        return instance;
    }

    public static void input(Player player, String title, BiConsumer<Player, String> consumer) {
        new InputGUI(title).open(player, consumer);
    }

    public HashMap<UUID, BiConsumer<Player, String>> getMap() {
        return map;
    }

    public void addMap(UUID uuid, BiConsumer<Player, String> consumer) {
        map.put(uuid, consumer);
    }

    public void removeMap(UUID uuid) {
        map.remove(uuid);
    }

    public BiConsumer<Player, String> getConsumer(Player player) {
        return map.get(player.getUniqueId());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("inputdebug") && sender instanceof Player) {
            input(((Player) sender), "debug", (CommandSender::sendMessage));
            return true;
        }
        return super.onCommand(sender, command, label, args);
    }
}
