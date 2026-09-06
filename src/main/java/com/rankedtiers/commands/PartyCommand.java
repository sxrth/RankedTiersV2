package com.rankedtiers.commands;

import com.rankedtiers.RankedTiers;
import com.rankedtiers.gui.PartyMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyCommand implements CommandExecutor {

    private final RankedTiers plugin;

    public PartyCommand(RankedTiers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.openInventory(new PartyMenu().build(plugin.getPartyManager(), player));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> {
                plugin.getPartyManager().createParty(player);
                player.sendMessage(Component.text(plugin.getMessagePrefix() + "Party created."));
            }
            case "invite" -> {
                if (args.length < 2) {
                    player.sendMessage(Component.text(plugin.getMessagePrefix() + "Usage: /party invite <player>"));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    player.sendMessage(Component.text(plugin.getMessagePrefix() + "That player is not online."));
                    return true;
                }
                plugin.getPartyManager().invite(player, target);
            }
            case "accept" -> plugin.getPartyManager().acceptInvite(player);
            case "leave" -> plugin.getPartyManager().leaveParty(player);
            default -> player.sendMessage(Component.text(plugin.getMessagePrefix() + "Usage: /party <create|invite|accept|leave> [player]"));
        }
        return true;
    }
}
