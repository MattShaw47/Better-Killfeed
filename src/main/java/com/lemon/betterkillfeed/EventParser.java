package com.lemon.betterkillfeed;

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

        if (combatSessions.containsKey(event.getVictim())) {
            combatSessions.get(event.getVictim()).addCombatEvent(event);
        }
        else {
            combatSessions.put(event.getVictim(), new CombatSession(event));
        }
    }
}
