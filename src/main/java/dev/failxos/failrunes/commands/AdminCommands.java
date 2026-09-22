package dev.failxos.failrunes.commands;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.Rune;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/** Requirement #60: /runeadmin give|tome|unidentified|gem|wand|identify|setlevel|reload|debug|list|inspect. */
public final class AdminCommands implements CommandExecutor {
    private final FailRunesPlugin plugin;
    public AdminCommands(FailRunesPlugin plugin) { this.plugin = plugin; }

    @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("failrunes.admin")) { sender.sendMessage(plugin.lang().get("message.no-permission")); return true; }
        if (args.length == 0) { sendUsage(sender); return true; }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadAll();
                sender.sendMessage(plugin.lang().get("message.admin-reloaded"));
            }
            case "debug" -> {
                plugin.toggleDebug();
                sender.sendMessage(plugin.lang().get("message.admin-debug-toggled", "state", plugin.debugEnabled()));
            }
            case "list" -> sender.sendMessage(plugin.lang().get("message.admin-list", "count", plugin.runes().size()));
            case "give" -> {
                if (args.length < 3) { sendUsage(sender); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                Rune r = plugin.runes().get(args[2]);
                if (target == null || r == null) { sender.sendMessage(plugin.lang().get("message.admin-not-found")); return true; }
                int level = args.length > 3 ? parse(args[3], 1) : 1;
                target.getInventory().addItem(plugin.itemFactory().runeItem(r, level));
                sender.sendMessage(plugin.lang().get("message.admin-given"));
            }
            case "tome" -> {
                if (args.length < 3) { sendUsage(sender); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                var rarity = dev.failxos.failrunes.api.RuneRarity.parse(args[2], null);
                if (target == null || rarity == null) { sender.sendMessage(plugin.lang().get("message.admin-not-found")); return true; }
                target.getInventory().addItem(plugin.itemFactory().tome(rarity, rarity.name().toLowerCase(), 0));
            }
            case "unidentified" -> {
                Player target = args.length > 1 ? Bukkit.getPlayerExact(args[1]) : (sender instanceof Player p ? p : null);
                if (target == null) { sender.sendMessage(plugin.lang().get("message.admin-not-found")); return true; }
                target.getInventory().addItem(plugin.itemFactory().unidentified("common", 1, 5));
            }
            case "gem" -> {
                if (args.length < 3) { sendUsage(sender); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { sender.sendMessage(plugin.lang().get("message.admin-not-found")); return true; }
                target.getInventory().addItem(plugin.itemFactory().luckyGem(parse(args[2], 1)));
            }
            case "wand" -> {
                Player target = args.length > 1 ? Bukkit.getPlayerExact(args[1]) : (sender instanceof Player p ? p : null);
                if (target == null) { sender.sendMessage(plugin.lang().get("message.admin-not-found")); return true; }
                int amount = args.length > 2 ? parse(args[2], 1) : 1;
                for (int i = 0; i < amount; i++) target.getInventory().addItem(plugin.itemFactory().tool("WAND", "Cleansing Wand", org.bukkit.Material.STICK, 1, List.of("item.wand.lore")));
            }
            case "inspect" -> {
                Player target = args.length > 1 ? Bukkit.getPlayerExact(args[1]) : (sender instanceof Player p ? p : null);
                if (target == null) { sender.sendMessage(plugin.lang().get("message.admin-not-found")); return true; }
                var runes = plugin.appliedRunes().read(target.getInventory().getItemInMainHand());
                sender.sendMessage(plugin.lang().get("message.admin-inspect", "runes", runes.toString()));
            }
            default -> sendUsage(sender);
        }
        return true;
    }
    private int parse(String s, int def) { try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; } }
    private void sendUsage(CommandSender s) {
        s.sendMessage("§7/runeadmin give|tome|unidentified|gem|wand|identify|setlevel|reload|debug|list|inspect");
    }
}
