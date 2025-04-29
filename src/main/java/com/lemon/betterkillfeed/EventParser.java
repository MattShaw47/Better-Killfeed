package com.lemon.betterkillfeed;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/// Parses game events and keeps the list of CombatSessions up to date.
/// Triggers KillMessageFormatter upon a player death.
public class EventParser {
    private static final long DEATH_TIMEOUT_MS = 250;

    private final Map<UUID, Long> pendingDeaths = new HashMap<>();

    private final Map<UUID, Float> lastKnownHealth = new HashMap<UUID, Float>();

    private final Map<UUID, CombatSession> combatSessions;

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

         for (Map.Entry<UUID, Long> entry : pendingDeaths.entrySet()) {
             if (Util.getMeasuringTimeMs() - entry.getValue() > DEATH_TIMEOUT_MS) {
                 KillMessageFormatter.formatKillMessage(entry.getKey(), this);
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

    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        for (PlayerEntity player : client.world.getPlayers()) {
            UUID uuid = player.getUuid();
            float currentHealth = player.getHealth();

            if (lastKnownHealth.containsKey(uuid)) {
                float oldHealth = lastKnownHealth.get(uuid);

                if (currentHealth < oldHealth) {
                    BetterKillfeedClient.LOGGER.info("New health: {}", currentHealth);
                    float damageAmount = oldHealth - currentHealth;

                    UUID guessedAttacker = AttackerGuesser.guessAttacker(player);

                    BetterKillfeedClient.LOGGER.info("Detected damage. Victim: {}, Damage: {}, Attacker Guess: {}",
                            player.getName().getString(),damageAmount,guessedAttacker.toString());

                    parseEvent(new DamageEvent(player.getUuid(), damageAmount, guessedAttacker, Util.getMeasuringTimeMs()));
                }
            }

            lastKnownHealth.put(uuid, currentHealth);
        }
    }

    public void onPlayerDeathDetected(PlayerEntity victim) {
        pendingDeaths.put(victim.getUuid(), Util.getMeasuringTimeMs());
    }

    public void onDeathMessageIntecepted(UUID victimUuid) {
        pendingDeaths.remove(victimUuid);
    }

    private void endCombatSession(UUID uuid) {
        BetterKillfeedClient.LOGGER.info("Ending combat session for player: {}", uuid);
        combatSessions.remove(uuid);
    }
}
