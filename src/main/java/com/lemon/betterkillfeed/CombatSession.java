package com.lemon.betterkillfeed;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/// A combat session for a single player.
public class CombatSession {
    private int totemPops;
    private Map<UUID, Float> damageContributions;
    public long lastCombatTime;
    public boolean dead;

    public CombatSession(CombatEvent firstEvent) {
        BetterKillfeedClient.LOGGER.info("Combat session started for player: {}", firstEvent.getVictim());
        damageContributions = new HashMap<UUID, Float>();
        totemPops = 0;
        addCombatEvent(firstEvent);
    }

    public void addCombatEvent(CombatEvent combatEvent) {
        if (combatEvent instanceof DamageEvent damageEvent) {
            damageContributions.put(damageEvent.getAttackerUuid(), damageEvent.getDamageAmount());
        }
        else if (combatEvent instanceof KillEvent killEvent) {
            dead = true;
        }
        else {
            totemPops++;
        }

        lastCombatTime = combatEvent.getTimestamp();
    }
}
