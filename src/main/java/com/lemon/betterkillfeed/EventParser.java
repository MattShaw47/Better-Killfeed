package com.lemon.betterkillfeed;

import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/// Parses game events and keeps the list of CombatSessions up to date.
/// Triggers KillMessageFormatter upon a player death.
public class EventParser {

    private Map<UUID, CombatSession> combatSessions;

    public EventParser() {
        combatSessions = new HashMap<UUID, CombatSession>();
    }

    public void parseEvent(CombatEvent event) {
        BetterKillfeedClient.LOGGER.info("Parsing event: {}", event);

        if (combatSessions.containsKey(event.getVictim()) && !combatSessions.get(event.getVictim()).dead) {
            combatSessions.get(event.getVictim()).addCombatEvent(event);
        }
        else {
            combatSessions.put(event.getVictim(), new CombatSession(event));
        }
    }

    public void checkForExpiredSessions(long expireThreshold) {
         for (Map.Entry<UUID, CombatSession> entry : combatSessions.entrySet()) {
             if (Util.getMeasuringTimeMs() - entry.getValue().lastCombatTime >= expireThreshold || entry.getValue().dead) {
                 endCombatSession(entry.getKey());
             }
         }
    }

    public CombatSession getCombatSession(UUID victim) {
        if (combatSessions.containsKey(victim)) {
            return combatSessions.get(victim);
        }
        else {
            // Returns a combat session with a kill event using a 0 uuid.
            return new CombatSession(new KillEvent(new UUID(0, 0), Util.getMeasuringTimeMs()));
        }
    }

    private void endCombatSession(UUID uuid) {
        BetterKillfeedClient.LOGGER.info("Ending combat session for player: {}", uuid);
        combatSessions.remove(uuid);
    }
}
