package me.reumsg;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter; // needed for the tab magic
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// added TabCompleter here so the server knows we have suggestions
public class Reumsg extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

        if (getCommand("reumsg") != null) {
            getCommand("reumsg").setExecutor(this);
            getCommand("reumsg").setTabCompleter(this); // don't forget to register the completer!
        }

        getLogger().info("Reumsg system loaded up. Let's get it.");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.joinMessage(null);
        Player p = e.getPlayer();

        String txt = getConfig().getString("messages.1.text", "<white>Welcome %player%!");
        boolean bld = getConfig().getBoolean("messages.1.bold", false);
        boolean itl = getConfig().getBoolean("messages.1.italic", false);

        if (bld) txt = "<bold>" + txt;
        if (itl) txt = "<italic>" + txt;

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

        if (args.length >= 2 && args[0].contains("id=")) {
            String id = args[0].split("=")[1];

            if (args.length == 3 && (args[1].equalsIgnoreCase("bold") || args[1].equalsIgnoreCase("italic"))) {
                String key = args[1].toLowerCase();
                boolean toggle = Boolean.parseBoolean(args[2]);

                getConfig().set("messages." + id + "." + key, toggle);
                saveConfig();
                p.sendMessage(mm.deserialize("<green>Updated " + key + " for ID " + id));
                return true;
            }

            String finalMsg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            getConfig().set("messages." + id + ".text", finalMsg);
            saveConfig();

            p.sendMessage(mm.deserialize("<green>Set text for ID " + id));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            p.sendMessage(mm.deserialize("<green>Reumsg config reloaded!"));
            return true;
        }

        // updated help menu with your <id> and <true/false> placeholders
        p.sendMessage(mm.deserialize("<newline><gradient:aqua:blue><bold>--- REUMSG EDIT MODE ---</bold></gradient>"));
        p.sendMessage(mm.deserialize("<gray>» <white>/reumsg id=<id> <msg> <dark_gray>- set text"));
        p.sendMessage(mm.deserialize("<gray>» <white>/reumsg id=<id> bold <true/false> <dark_gray>- toggle bold"));
        p.sendMessage(mm.deserialize("<gray>» <white>/reumsg id=<id> italic <true/false> <dark_gray>- toggle italic"));
        p.sendMessage(mm.deserialize("<gray>» <white>/reumsg reload <dark_gray>- refresh config"));
        p.sendMessage(mm.deserialize("<newline><white>Tip: ID 1 is the join message."));
        p.sendMessage(mm.deserialize("<gradient:aqua:blue><bold>------------------------</bold></gradient><newline>"));

        return true;
    }

    // This is the "TAB" magic part.
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("id=1");
            completions.add("reload");
        } else if (args.length == 2 && args[0].startsWith("id=")) {
            completions.add("bold");
            completions.add("italic");
            completions.add("<message>");
        } else if (args.length == 3 && (args[1].equalsIgnoreCase("bold") || args[1].equalsIgnoreCase("italic"))) {
            completions.add("true");
            completions.add("false");
        }

        // basically just filters the list so if you type "b" it only shows "bold"
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

    private Component format(Player p, String raw) {
        String out = raw.replace("%player%", p.getName());

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            out = PlaceholderAPI.setPlaceholders(p, out);
        }

        return mm.deserialize(out);
    }
}