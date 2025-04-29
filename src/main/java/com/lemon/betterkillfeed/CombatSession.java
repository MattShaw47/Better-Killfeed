package com.lemon.betterkillfeed;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/// A combat session for a single player.
public class CombatSession {
    public int totemPops;
    private Map<UUID, Float> damageContributions;
    public long lastCombatTime;
    public boolean dead;
    private float totalDamage;

    public CombatSession(CombatEvent firstEvent) {
        BetterKillfeedClient.LOGGER.info("Combat session started for player: {}", firstEvent.getVictim());
        damageContributions = new HashMap<UUID, Float>();
        totemPops = 0;
        addCombatEvent(firstEvent);
    }

    public void addCombatEvent(CombatEvent combatEvent) {
        if (combatEvent instanceof DamageEvent damageEvent) {
            if (damageContributions.containsKey(damageEvent.getAttackerUuid())) {
                damageContributions.put(damageEvent.getAttackerUuid(), damageEvent.getDamageAmount() + damageContributions.get(damageEvent.getAttackerUuid()));
            }
            else {
                damageContributions.put(damageEvent.getAttackerUuid(), damageEvent.getDamageAmount());
            }

            totalDamage += damageEvent.getDamageAmount();
        }
        else if (combatEvent instanceof KillEvent killEvent) {
            dead = true;
        }
        else {
            totemPops++;
        }

        lastCombatTime = combatEvent.getTimestamp();
    }

    public Map<UUID, Float> getDamageContributions() {
        return damageContributions;
    }

    public float getTotalDamage() {
        return totalDamage;
    }
}
