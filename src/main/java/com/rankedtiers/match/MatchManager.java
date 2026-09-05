package com.rankedtiers.match;

import com.rankedtiers.data.DataStore;
import com.rankedtiers.data.PlayerData;
import com.rankedtiers.kit.Kit;
import com.rankedtiers.rating.RatingService;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles matchmaking queues per kit and the lifecycle of active matches.
 */
public class MatchManager {

    private final DataStore dataStore;
    private final RatingService ratingService;

    private final Map<String, Deque<UUID>> queues = new HashMap<>();
    private final Map<UUID, Match> activeMatches = new HashMap<>();
    private final String prefix;

    public MatchManager(DataStore dataStore, RatingService ratingService, String prefix) {
        this.dataStore = dataStore;
        this.ratingService = ratingService;
        this.prefix = prefix;
    }

    public boolean isQueued(UUID uuid) {
        return queues.values().stream().anyMatch(q -> q.contains(uuid));
    }

    public boolean isInMatch(UUID uuid) {
        return activeMatches.containsKey(uuid);
    }

    public Match getMatch(UUID uuid) {
        return activeMatches.get(uuid);
    }

    public void leaveAllQueues(UUID uuid) {
        queues.values().forEach(q -> q.remove(uuid));
    }

    /**
     * Adds a player to the queue for the given kit. If an opponent is already
     * waiting, immediately starts a match between them.
     */
    public void joinQueue(Player player, Kit kit) {
        if (isInMatch(player.getUniqueId()) || isQueued(player.getUniqueId())) {
            player.sendMessage(Component.text(prefix + "You are already queued or in a match."));
            return;
        }

        Deque<UUID> queue = queues.computeIfAbsent(kit.getName(), k -> new ArrayDeque<>());

        UUID opponentId = queue.pollFirst();
        if (opponentId == null || opponentId.equals(player.getUniqueId())) {
            queue.addLast(player.getUniqueId());
            player.sendMessage(Component.text(prefix + "Queued for " + kit.getName() + ". Waiting for an opponent..."));
            return;
        }

        Player opponent = player.getServer().getPlayer(opponentId);
        if (opponent == null || !opponent.isOnline()) {
            // Stale entry, requeue this player.
            queue.addLast(player.getUniqueId());
            player.sendMessage(Component.text(prefix + "Queued for " + kit.getName() + ". Waiting for an opponent..."));
            return;
        }

        startMatch(player, opponent, kit);
    }

    private void startMatch(Player a, Player b, Kit kit) {
        Match match = new Match(a, b, kit);
        activeMatches.put(a.getUniqueId(), match);
        activeMatches.put(b.getUniqueId(), match);

        Arena arena = kit.getArena();
        if (arena.isConfigured()) {
            a.teleport(arena.getPosition1());
            b.teleport(arena.getPosition2());
        }

        a.setGameMode(GameMode.SURVIVAL);
        b.setGameMode(GameMode.SURVIVAL);
        a.setHealth(20);
        b.setHealth(20);

        Component msg = Component.text(prefix + "Match found! " + a.getName() + " vs " + b.getName() + " (" + kit.getName() + ")");
        a.sendMessage(msg);
        b.sendMessage(msg);
    }

    /**
     * Ends the match a player was in, crediting the winner and applying the
     * rating change to both participants, then updating their tiers.
     */
    public void endMatch(UUID winnerId, UUID loserId) {
        Match match = activeMatches.remove(winnerId);
        activeMatches.remove(loserId);
        if (match == null) return;
        match.end();

        PlayerData winner = dataStore.get(winnerId);
        PlayerData loser = dataStore.get(loserId);

        winner.addWin();
        loser.addLoss();

        winner.setRating(ratingService.applyWin(winner.getRating()));
        loser.setRating(ratingService.applyLoss(loser.getRating()));

        winner.setTier(ratingService.tierFor(winner.getRating()));
        loser.setTier(ratingService.tierFor(loser.getRating()));

        dataStore.save(winner);
        dataStore.save(loser);

        Player winnerPlayer = org.bukkit.Bukkit.getPlayer(winnerId);
        Player loserPlayer = org.bukkit.Bukkit.getPlayer(loserId);
        if (winnerPlayer != null) {
            winnerPlayer.sendMessage(Component.text(prefix + "You won! New rating: " + winner.getRating() + " (" + winner.getTier() + ")"));
        }
        if (loserPlayer != null) {
            loserPlayer.sendMessage(Component.text(prefix + "You lost. New rating: " + loser.getRating() + " (" + loser.getTier() + ")"));
        }
    }

    /** Call when a player disconnects mid-match or mid-queue to keep state clean. */
    public void handleDisconnect(Player player) {
        leaveAllQueues(player.getUniqueId());
        Match match = activeMatches.get(player.getUniqueId());
        if (match != null) {
            UUID opponent = match.opponentOf(player.getUniqueId());
            if (opponent != null) {
                endMatch(opponent, player.getUniqueId());
            }
        }
    }

    public List<String> activeKitQueues() {
        return queues.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .map(e -> e.getKey() + " (" + e.getValue().size() + ")")
                .collect(Collectors.toList());
    }
}
