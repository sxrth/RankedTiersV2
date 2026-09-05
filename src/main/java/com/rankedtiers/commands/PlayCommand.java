package com.rankedtiers.commands;

import com.rankedtiers.RankedTiers;
import com.rankedtiers.gui.QueueMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayCommand implements CommandExecutor {

    private final RankedTiers plugin;

    public PlayCommand(RankedTiers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (!player.hasPermission("rankedtiers.play")) {
            player.sendMessage(Component.text(plugin.getMessagePrefix() + "You don't have permission to queue."));
            return true;
        }

        QueueMenu menu = new QueueMenu();
        plugin.registerOpenQueueMenu(player, menu);
        player.openInventory(menu.build(plugin.getKitManager(), player));
        return true;
    }
}
