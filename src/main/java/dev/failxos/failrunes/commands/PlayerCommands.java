package dev.failxos.failrunes.commands;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.gui.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Requirement #61: /runes /rune /infuser /runeinfo /runeprefs /prefs /damagehistory (aliases configurable in plugin.yml). */
public final class PlayerCommands implements CommandExecutor {
    private final FailRunesPlugin plugin;
    public PlayerCommands(FailRunesPlugin plugin) { this.plugin = plugin; }

    @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        switch (cmd.getName().toLowerCase()) {
            case "infuser", "runes", "rune" -> new InfuserMainGui(plugin, p).open(p);
            case "runeinfo" -> {
                if (args.length > 0 && args[0].equalsIgnoreCase("search"))
                    new RuneSearchGui(plugin, args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "").open(p);
                else new RuneInfoItemGui(plugin, p, p.getInventory().getItemInMainHand()).open(p);
            }
            case "runeprefs", "prefs" -> new PreferencesGui(plugin, p).open(p);
            case "damagehistory" -> new DamageHistoryGui(plugin, p).open(p);
            case "runecollection" -> new CollectionGui(plugin, p, 0).open(p);
            default -> { return false; }
        }
        return true;
    }
}
