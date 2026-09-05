package com.rankedtiers.listeners;

import com.rankedtiers.RankedTiers;
import com.rankedtiers.match.Match;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

/**
 * Ends a ranked match when one of its two players dies, crediting the
 * survivor with the win. Also enforces each kit's block place/break rules
 * while a match is in progress.
 */
public class CombatListener implements Listener {

    private final RankedTiers plugin;

    public CombatListener(RankedTiers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        UUID victimId = victim.getUniqueId();

        Match match = plugin.getMatchManager().getMatch(victimId);
        if (match == null || !match.isActive()) return;

        UUID winnerId = match.opponentOf(victimId);
        if (winnerId != null) {
            plugin.getMatchManager().endMatch(winnerId, victimId);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Match match = plugin.getMatchManager().getMatch(event.getPlayer().getUniqueId());
        if (match != null && !match.getKit().allowsBlockPlace()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Match match = plugin.getMatchManager().getMatch(event.getPlayer().getUniqueId());
        if (match != null && !match.getKit().allowsBlockBreak()) {
            event.setCancelled(true);
        }
    }
}
