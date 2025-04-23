package dev.nighter.teaSafeDrop;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final TeaSafeDrop plugin;

    public CommandHandler(TeaSafeDrop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Display plugin info if no arguments
            showPluginInfo(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("reload")) {
            // Check permission
            if (!sender.hasPermission("teasafedrop.reload")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to reload the plugin.");
                return true;
            }

            // Reload the plugin config
            Scheduler.runTaskAsync(() -> {
                plugin.loadConfig();
                sender.sendMessage(ChatColor.GREEN + "TeaSafeDrop configuration reloaded successfully!");
            });

            return true;
        } else if (subCommand.equals("help")) {
            showHelp(sender);
            return true;
        }

        // Unknown command
        sender.sendMessage(ChatColor.RED + "Unknown command. Type " + ChatColor.YELLOW + "/" + label + " help" + ChatColor.RED + " for help.");
        return true;
    }

    private void showPluginInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== " + ChatColor.GOLD + "TeaSafeDrop v" + plugin.getDescription().getVersion() + ChatColor.GREEN + " ===");
        sender.sendMessage(ChatColor.YELLOW + "Plugin to protect items from explosions and fire");
        sender.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.WHITE + "/teasafedrop help" + ChatColor.YELLOW + " for commands");
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== " + ChatColor.GOLD + "TeaSafeDrop Help" + ChatColor.GREEN + " ===");
        sender.sendMessage(ChatColor.YELLOW + "/teasafedrop " + ChatColor.WHITE + "- Show plugin info");
        sender.sendMessage(ChatColor.YELLOW + "/teasafedrop help " + ChatColor.WHITE + "- Show this help menu");

        if (sender.hasPermission("teasafedrop.reload")) {
            sender.sendMessage(ChatColor.YELLOW + "/teasafedrop reload " + ChatColor.WHITE + "- Reload the plugin configuration");
        }

        sender.sendMessage(ChatColor.YELLOW + "/ts " + ChatColor.WHITE + "- Alias for /teasafedrop");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            subCommands.add("help");

            if (sender.hasPermission("teasafedrop.reload")) {
                subCommands.add("reload");
            }

            String partialCommand = args[0].toLowerCase();
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(partialCommand)) {
                    completions.add(subCommand);
                }
            }
        }

        return completions;
    }
}