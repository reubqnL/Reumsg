package me.reumsg;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
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

public class Reumsg extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

        // register stuff
        if (getCommand("reumsg") != null) {
            getCommand("reumsg").setExecutor(this);
            getCommand("reumsg").setTabCompleter(this);
        }

        // FANCY COLORFUL TERMINAL TEXT
        String version = getDescription().getVersion();
        getLogger().info("\u001B[36m" +
                "\n  ____  _____ _   _ __  __ ____   ____ " +
                "\n |  _ \\| ____| | | |  \\/  / ___| / ___|" +
                "\n | |_) |  _| | | | | |\\/| \\___ \\| |  _ " +
                "\n |  _ <| |___| |_| | |  | |___) | |_| |" +
                "\n |_| \\_\\_____|\\___/|_|  |_|____/ \\____|" +
                "\n         \u001B[34mBY REUBQN - v" + version + " Loaded\u001B[0m");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.joinMessage(null); // begone vanilla trash
        Player p = e.getPlayer();

        // still using ID 1 as the main join msg
        String txt = getConfig().getString("messages.1.text", "<white>Welcome %player%!");

        if (getConfig().getBoolean("messages.1.bold")) txt = "<bold>" + txt;
        if (getConfig().getBoolean("messages.1.italic")) txt = "<italic>" + txt;

        for (Player all : Bukkit.getOnlinePlayers()) {
            all.playSound(all.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1f);
        }

        Bukkit.broadcast(format(p, txt));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String lbl, @NotNull String[] args) {
        if (!(s instanceof Player p)) {
            s.sendMessage("consoles cant see messages buddy");
            return true;
        }

        // logic: /reumsg <id> <something> (gemini helped a lot lol)
        if (args.length >= 2) {
            String id = args[0];

            // FORCE ID TO BE A NUMBER
            try {
                Integer.parseInt(id);
            } catch (NumberFormatException e) {
                p.sendMessage(mm.deserialize("<red>Error: The ID must be a number (like 1, 2, 3), not '" + id + "'!"));
                return true;
            }

            // load it up
            if (args[1].equalsIgnoreCase("load")) {
                if (!getConfig().contains("messages." + id)) {
                    p.sendMessage(mm.deserialize("<red>Bruh, ID '" + id + "' doesnt even exist."));
                    return true;
                }
                String saved = getConfig().getString("messages." + id + ".text");
                p.sendMessage(mm.deserialize("<green>ID " + id + " currently says: <reset>" + saved));
                return true;
            }

            // style toggles
            if (args.length == 3 && (args[1].equalsIgnoreCase("bold") || args[1].equalsIgnoreCase("italic"))) {
                getConfig().set("messages." + id + "." + args[1].toLowerCase(), Boolean.parseBoolean(args[2]));
                saveConfig();
                p.sendMessage(mm.deserialize("<green>Toggled " + args[1] + " for ID " + id));
                return true;
            }

            // saving text
            String finalMsg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            getConfig().set("messages." + id + ".text", finalMsg);
            saveConfig();

            p.sendMessage(mm.deserialize("<green>Saved new text for ID " + id));
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                p.sendMessage(mm.deserialize("<green>Configs fresh."));
                return true;
            }

            if (args[0].equalsIgnoreCase("preview")) {
                String txt = getConfig().getString("messages.1.text", "<white>Welcome %player%!");
                if (getConfig().getBoolean("messages.1.bold")) txt = "<bold>" + txt;
                if (getConfig().getBoolean("messages.1.italic")) txt = "<italic>" + txt;

                p.sendMessage(mm.deserialize("<gray>Preview of ID 1:"));
                p.sendMessage(format(p, txt));
                return true;
            }

            if (args[0].equalsIgnoreCase("placeholders")) {
                p.sendMessage(mm.deserialize("<aqua><bold>REUMSG TAGS:</bold>"));
                p.sendMessage(mm.deserialize("<gray>» <white>%player% <dark_gray>- your name"));
                p.sendMessage(mm.deserialize("<gray>» <white>%player_name% <dark_gray>- also your name"));
                p.sendMessage(mm.deserialize("<gray>» <white>MiniMessage: <italic><bold><red><gradient:color:color>"));
                return true;
            }
        }

        // help menu - Tip is gone!
        p.sendMessage(mm.deserialize("<newline><gradient:aqua:blue><bold>--- REUMSG EDIT MODE ---</bold></gradient>"));
        p.sendMessage(mm.deserialize("<gray>» <white>/reumsg <id> <msg> <dark_gray>- set text"));
        p.sendMessage(mm.deserialize("<gray>» <white>/reumsg <id> load <dark_gray>- preview what u saved"));
        p.sendMessage(mm.deserialize("<gray>» <white>/reumsg <id> bold <true/false> <dark_gray>- toggle bold"));
        p.sendMessage(mm.deserialize("<gray>» <white>/reumsg <id> italic <true/false> <dark_gray>- toggle italic"));
        p.sendMessage(mm.deserialize("<gray>» <white>/reumsg preview <dark_gray>- see current msg"));
        p.sendMessage(mm.deserialize("<gray>» <white>/reumsg placeholders <dark_gray>- tag list"));
        p.sendMessage(mm.deserialize("<gray>» <white>/reumsg reload <dark_gray>- refresh everything"));
        p.sendMessage(mm.deserialize("<gradient:aqua:blue><bold>------------------------</bold></gradient><newline>"));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("<id>");
            suggestions.add("reload");
            suggestions.add("preview");
            suggestions.add("placeholders");
        } else if (args.length == 2 && !args[0].equalsIgnoreCase("reload") && !args[0].equalsIgnoreCase("preview") && !args[0].equalsIgnoreCase("placeholders")) {
            suggestions.add("load");
            suggestions.add("bold");
            suggestions.add("italic");
            suggestions.add("msg");
        } else if (args.length == 3 && (args[1].equalsIgnoreCase("bold") || args[1].equalsIgnoreCase("italic"))) {
            suggestions.add("true");
            suggestions.add("false");
        }

        return suggestions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

    private Component format(Player p, String raw) {
        String out = raw.replace("%player%", p.getName()).replace("%player_name%", p.getName());
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            out = PlaceholderAPI.setPlaceholders(p, out);
        }
        return mm.deserialize(out);
    }
}