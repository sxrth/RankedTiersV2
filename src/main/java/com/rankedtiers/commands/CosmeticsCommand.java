package com.rankedtiers.commands;

import com.rankedtiers.RankedTiers;
import com.rankedtiers.gui.CosmeticsMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CosmeticsCommand implements CommandExecutor {

    private final RankedTiers plugin;

    public CosmeticsCommand(RankedTiers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (!plugin.getCosmeticsManager().isEnabled()) {
            player.sendMessage(Component.text(plugin.getMessagePrefix() + "Cosmetics are currently disabled."));
            return true;
        }

        var data = plugin.getDataStore().get(player.getUniqueId());
        player.openInventory(new CosmeticsMenu().build(plugin.getCosmeticsManager(), data));
        return true;
    }
}
