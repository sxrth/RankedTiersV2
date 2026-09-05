package com.rankedtiers.party;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A simple leader + members party used for group queueing and party chat.
 */
public class Party {

    private final UUID leader;
    private final Set<UUID> members = new LinkedHashSet<>();

    public Party(UUID leader) {
        this.leader = leader;
        this.members.add(leader);
    }

    public UUID getLeader() {
        return leader;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public boolean isLeader(UUID uuid) {
        return leader.equals(uuid);
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public int size() {
        return members.size();
    }
}
