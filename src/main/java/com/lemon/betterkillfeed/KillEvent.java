package com.lemon.betterkillfeed;

import java.util.UUID;

public class KillEvent implements CombatEvent {
    private final UUID uuid;
    private final long timestamp;

    public KillEvent(UUID uuid, long timestamp) {
        this.uuid = uuid;
        this.timestamp = timestamp;
    }

    @Override
    public UUID getVictim() {
        return uuid;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
