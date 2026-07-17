package com.runal.client;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class PartyState {
    public static final PartyState INSTANCE = new PartyState();

    private final Set<UUID> members = new LinkedHashSet<>();

    private PartyState() {
    }

    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public void add(UUID id) {
        members.add(id);
    }

    public void remove(UUID id) {
        members.remove(id);
    }

    public void clear() {
        members.clear();
    }
}
