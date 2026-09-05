package com.rankedtiers.match;

import com.rankedtiers.kit.Kit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * A single active ranked duel between two players in a given kit.
 */
public class Match {

    private final UUID player1;
    private final UUID player2;
    private final Kit kit;
    private boolean active = true;

    public Match(Player player1, Player player2, Kit kit) {
        this.player1 = player1.getUniqueId();
        this.player2 = player2.getUniqueId();
        this.kit = kit;
    }

    public UUID getPlayer1() {
        return player1;
    }

    public UUID getPlayer2() {
        return player2;
    }

    public Kit getKit() {
        return kit;
    }

    public boolean isActive() {
        return active;
    }

    public void end() {
        this.active = false;
    }

    public boolean involves(UUID uuid) {
        return player1.equals(uuid) || player2.equals(uuid);
    }

    public UUID opponentOf(UUID uuid) {
        if (player1.equals(uuid)) return player2;
        if (player2.equals(uuid)) return player1;
        return null;
    }
}
