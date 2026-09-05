package com.rankedtiers.party;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks parties and pending invites. Each player belongs to at most one party.
 */
public class PartyManager {

    private final Map<UUID, Party> partyByMember = new HashMap<>();
    // invitee -> inviter
    private final Map<UUID, UUID> pendingInvites = new HashMap<>();
    private final String prefix;

    public PartyManager(String prefix) {
        this.prefix = prefix;
    }

    public Party getParty(UUID uuid) {
        return partyByMember.get(uuid);
    }

    public Party createParty(Player leader) {
        Party party = new Party(leader.getUniqueId());
        partyByMember.put(leader.getUniqueId(), party);
        return party;
    }

    public void invite(Player inviter, Player target) {
        Party party = partyByMember.computeIfAbsent(inviter.getUniqueId(), k -> createParty(inviter));
        if (!party.isLeader(inviter.getUniqueId())) {
            inviter.sendMessage(Component.text(prefix + "Only the party leader can invite."));
            return;
        }
        pendingInvites.put(target.getUniqueId(), inviter.getUniqueId());
        target.sendMessage(Component.text(prefix + inviter.getName() + " invited you to their party. Type /party accept"));
        inviter.sendMessage(Component.text(prefix + "Invited " + target.getName() + "."));
    }

    public void acceptInvite(Player player) {
        UUID inviterId = pendingInvites.remove(player.getUniqueId());
        if (inviterId == null) {
            player.sendMessage(Component.text(prefix + "You have no pending party invite."));
            return;
        }
        Party party = partyByMember.get(inviterId);
        if (party == null) {
            player.sendMessage(Component.text(prefix + "That party no longer exists."));
            return;
        }
        party.addMember(player.getUniqueId());
        partyByMember.put(player.getUniqueId(), party);
        player.sendMessage(Component.text(prefix + "Joined the party."));
    }

    public void leaveParty(Player player) {
        Party party = partyByMember.remove(player.getUniqueId());
        if (party == null) {
            player.sendMessage(Component.text(prefix + "You are not in a party."));
            return;
        }
        party.removeMember(player.getUniqueId());
        if (party.isLeader(player.getUniqueId()) || party.size() == 0) {
            // Disband: remove remaining members' mapping too.
            for (UUID member : party.getMembers()) {
                partyByMember.remove(member);
            }
        }
        player.sendMessage(Component.text(prefix + "You left the party."));
    }
}
