package com.rankedtiers.commands;

import com.rankedtiers.RankedTiers;
import com.rankedtiers.data.PlayerData;
import com.rankedtiers.gui.KitEditorMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class AdminCommand implements CommandExecutor {

    private final RankedTiers plugin;

    public AdminCommand(RankedTiers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rankedtiers.admin")) {
            sender.sendMessage(Component.text(plugin.getMessagePrefix() + "You don't have permission."));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text(plugin.getMessagePrefix() + "Usage: /rtadmin <reload|kits|setpos1|setpos2|stats|cosmetic> [args]"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getKitManager().reload();
                plugin.getRatingService().reload();
                plugin.getCosmeticsManager().reload();
                plugin.getLobbyItemsManager().reload();
                sender.sendMessage(Component.text(plugin.getMessagePrefix() + "Configuration reloaded."));
            }
            case "kits" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can open the kit editor.");
                    return true;
                }
                player.openInventory(new KitEditorMenu().build(plugin.getKitManager()));
            }
            case "setpos1", "setpos2" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can set positions.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(Component.text(plugin.getMessagePrefix() + "Usage: /rtadmin " + args[0] + " <kit>"));
                    return true;
                }
                setPosition(player, args[1], args[0].equalsIgnoreCase("setpos1"));
            }
            case "stats" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text(plugin.getMessagePrefix() + "Usage: /rtadmin stats <player>"));
                    return true;
                }
                OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[1]);
                PlayerData data = plugin.getDataStore().get(target.getUniqueId());
                sender.sendMessage(Component.text(plugin.getMessagePrefix() + args[1] + ": " + data.getTier()
                        + " (" + data.getRating() + " rating, " + data.getWins() + "W/" + data.getLosses() + "L)"));
            }
            case "cosmetic" -> handleCosmetic(sender, args);
            default -> sender.sendMessage(Component.text(plugin.getMessagePrefix() + "Unknown subcommand."));
        }
        return true;
    }

    /**
     * In-game configuration of which rank unlocks which trim, so admins never
     * have to hand-edit config.yml. Backed by the same `cosmetics.patterns`
     * / `cosmetics.materials` sections the plugin already reads from.
     *
     * Usage:
     *   /rtadmin cosmetic lock <pattern|material> <id> <rank>
     *   /rtadmin cosmetic unlock <pattern|material> <id>   (removes requirement - open to everyone)
     *   /rtadmin cosmetic list <pattern|material>
     */
    private void handleCosmetic(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text(plugin.getMessagePrefix()
                    + "Usage: /rtadmin cosmetic <lock|unlock|list> <pattern|material> [id] [rank]"));
            return;
        }

        String action = args[1].toLowerCase();

        if (action.equals("list")) {
            if (args.length < 3 || !isValidCategory(args[2])) {
                sender.sendMessage(Component.text(plugin.getMessagePrefix() + "Usage: /rtadmin cosmetic list <pattern|material>"));
                return;
            }
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("cosmetics." + categoryKey(args[2]));
            if (section == null || section.getKeys(false).isEmpty()) {
                sender.sendMessage(Component.text(plugin.getMessagePrefix() + "No " + args[2] + " entries configured."));
                return;
            }
            for (String id : section.getKeys(false)) {
                sender.sendMessage(Component.text(plugin.getMessagePrefix() + id + " -> " + section.getString(id)));
            }
            return;
        }

        if (action.equals("lock")) {
            if (args.length < 5 || !isValidCategory(args[2])) {
                sender.sendMessage(Component.text(plugin.getMessagePrefix() + "Usage: /rtadmin cosmetic lock <pattern|material> <id> <rank>"));
                return;
            }
            String category = categoryKey(args[2]);
            String id = args[3];
            String rank = args[4];

            String canonical = plugin.getRatingService().canonicalTierName(rank);
            if (canonical == null) {
                sender.sendMessage(Component.text(plugin.getMessagePrefix() + "'" + rank
                        + "' is not one of your configured ranks. Check the 'tiers' section in config.yml."));
                return;
            }

            plugin.getConfig().set("cosmetics." + category + "." + id, canonical);
            plugin.saveConfig();
            plugin.getCosmeticsManager().reload();

            sender.sendMessage(Component.text(plugin.getMessagePrefix()
                    + "'" + id + "' (" + args[2] + ") now requires rank " + canonical + "."));
            return;
        }

        if (action.equals("unlock")) {
            if (args.length < 4 || !isValidCategory(args[2])) {
                sender.sendMessage(Component.text(plugin.getMessagePrefix() + "Usage: /rtadmin cosmetic unlock <pattern|material> <id>"));
                return;
            }
            String category = categoryKey(args[2]);
            String id = args[3];

            plugin.getConfig().set("cosmetics." + category + "." + id, null);
            plugin.saveConfig();
            plugin.getCosmeticsManager().reload();

            sender.sendMessage(Component.text(plugin.getMessagePrefix() + "'" + id + "' (" + args[2] + ") removed from the unlock table."));
            return;
        }

        sender.sendMessage(Component.text(plugin.getMessagePrefix() + "Usage: /rtadmin cosmetic <lock|unlock|list> <pattern|material> [id] [rank]"));
    }

    private boolean isValidCategory(String value) {
        return value.equalsIgnoreCase("pattern") || value.equalsIgnoreCase("material");
    }

    private String categoryKey(String value) {
        return value.equalsIgnoreCase("pattern") ? "patterns" : "materials";
    }

    /**
     * Writes the player's current standing location into config.yml under
     * kits.&lt;kit&gt;.position1/2, then reloads kits so it takes effect.
     */
    private void setPosition(Player player, String kitName, boolean isFirst) {
        ConfigurationSection kitSection = plugin.getConfig().getConfigurationSection("kits." + kitName);
        if (kitSection == null) {
            player.sendMessage(Component.text(plugin.getMessagePrefix() + "No such kit: " + kitName));
            return;
        }

        Location loc = player.getLocation();
        String value = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ()
                + "," + loc.getYaw() + "," + loc.getPitch();

        kitSection.set(isFirst ? "position1" : "position2", value);
        plugin.saveConfig();
        plugin.getKitManager().reload();

        player.sendMessage(Component.text(plugin.getMessagePrefix()
                + (isFirst ? "Position 1" : "Position 2") + " for " + kitName + " set to your location."));
    }
}
