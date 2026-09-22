package dev.failxos.failrunes.commands;

import dev.failxos.failrunes.FailRunesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Tab completion for /runeadmin (subcommand, then player, then rune id). */
public final class RuneCompleter implements TabCompleter {
    private final FailRunesPlugin plugin;
    public RuneCompleter(FailRunesPlugin plugin) { this.plugin = plugin; }

    @Override public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) return filter(List.of("give", "tome", "unidentified", "gem", "wand", "identify", "setlevel", "reload", "debug", "list", "inspect"), args[0]);
        if (args.length == 2 && List.of("give", "tome", "unidentified", "gem", "wand", "inspect").contains(args[0].toLowerCase()))
            return filter(Bukkit.getOnlinePlayers().stream().map(p -> p.getName()).collect(Collectors.toList()), args[1]);
        if (args.length == 3 && args[0].equalsIgnoreCase("give"))
            return filter(plugin.runes().all().stream().map(r -> r.id()).collect(Collectors.toList()), args[2]);
        if (args.length == 3 && args[0].equalsIgnoreCase("tome"))
            return filter(List.of("common", "rare", "legendary", "mythical"), args[2]);
        return new ArrayList<>();
    }
    private List<String> filter(List<String> options, String prefix) {
        return options.stream().filter(o -> o.toLowerCase().startsWith(prefix.toLowerCase())).collect(Collectors.toList());
    }
}
