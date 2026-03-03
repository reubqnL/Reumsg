package me.reumsg;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Reumsg extends JavaPlugin implements Listener, CommandExecutor {

    // mini message instance
    private final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        saveDefaultConfig(); // gotta have that config

        getServer().getPluginManager().registerEvents(this, this);

        // registering the command. if this is null the plugin is toast anyway lol
        if (getCommand("reumsg") != null) {
            getCommand("reumsg").setExecutor(this);
        }

        getLogger().info("Reumsg system loaded up. Let's get it.");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.joinMessage(null); // begone vanilla message
        Player p = e.getPlayer();

        // getting ID 1 as default join msg.
        // i should probably make this configurable later lol
        String txt = getConfig().getString("messages.1.text", "<white>Welcome %player%!");
        boolean bld = getConfig().getBoolean("messages.1.bold", false);
        boolean itl = getConfig().getBoolean("messages.1.italic", false);

        // sandwich the tags in if they are enabled
        if (bld) txt = "<bold>" + txt;
        if (itl) txt = "<italic>" + txt;

        // ping everyone. maybe i'll make this a toggle later if it gets annoying.
        for (Player all : Bukkit.getOnlinePlayers()) {
            all.playSound(all.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1f);
        }

        Bukkit.broadcast(format(p, txt));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String lbl, @NotNull String[] args) {
        if (!(s instanceof Player p)) {
            s.sendMessage("go use a client, console.");
            return true;
        }

        // handle the ID system: /reumsg id=1 <message>
        if (args.length >= 2 && args[0].contains("id=")) {
            String id = args[0].split("=")[1]; // just split it, cleaner than replace

            // check for style toggle: /reumsg id=1 bold true
            if (args.length == 3 && (args[1].equalsIgnoreCase("bold") || args[1].equalsIgnoreCase("italic"))) {
                String key = args[1].toLowerCase();
                boolean toggle = Boolean.parseBoolean(args[2]);

                getConfig().set("messages." + id + "." + key, toggle);
                saveConfig();
                p.sendMessage(mm.deserialize("<green>Updated " + key + " for ID " + id));
                return true;
            }

            // just setting the raw text
            String finalMsg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            getConfig().set("messages." + id + ".text", finalMsg);
            saveConfig();

            p.sendMessage(mm.deserialize("<green>Set text for ID " + id));
            return true;
        }

        // quick reload command cause
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            p.sendMessage(mm.deserialize("<green>Reumsg config reloaded!"));
            return true;
        }

        // the help menu
        p.sendMessage(mm.deserialize("<newline><gradient:aqua:blue><bold>--- REUMSG EDIT MODE ---</bold></gradient>"));
        p.sendMessage(mm.deserialize("<gray>» <white>/reumsg id=1 <msg> <dark_gray>- set text"));
        p.sendMessage(mm.deserialize("<gray>» <white>/reumsg id=1 bold true/false <dark_gray>- toggle bold"));
        p.sendMessage(mm.deserialize("<gray>» <white>/reumsg id=1 italic true/false <dark_gray>- toggle italic"));
        p.sendMessage(mm.deserialize("<gray>» <white>/reumsg reload <dark_gray>- refresh config"));
        p.sendMessage(mm.deserialize("<newline><white>Tip: ID 1 is the join message."));
        p.sendMessage(mm.deserialize("<gradient:aqua:blue><bold>------------------------</bold></gradient><newline>"));

        return true;
    }

    // helper for the messy stuff
    private Component format(Player p, String raw) {
        String out = raw.replace("%player%", p.getName());

        // try to use papi if the server has it. if not, whatever.
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            out = PlaceholderAPI.setPlaceholders(p, out);
        }

        return mm.deserialize(out);
    }
}