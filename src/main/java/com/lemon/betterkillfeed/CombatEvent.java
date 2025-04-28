package com.lemon.betterkillfeed;

import java.util.UUID;

public interface CombatEvent {
    UUID getVictim();
    long getTimestamp();
}
