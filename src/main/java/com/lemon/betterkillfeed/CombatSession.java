package com.lemon.betterkillfeed;

import java.util.Map;
import java.util.UUID;

/// A combat session for a single player.
public class CombatSession {
    private int totemPops;
    private Map<UUID, Float> damageContributions;
    private long lastCombatTime;

    public CombatSession(CombatEvent firstEvent) {
        addCombatEvent(firstEvent);
    }

    public void addCombatEvent(CombatEvent combatEvent) {
        return;
    }
}
