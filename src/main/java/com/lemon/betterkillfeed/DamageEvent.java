package com.lemon.betterkillfeed;

import java.util.Optional;
import java.util.UUID;

public class DamageEvent implements CombatEvent {
    private final UUID victimUuid;
    private final float damageAmount;
    private final Optional<UUID> attackerUuid;
    private final long timestamp;

    public DamageEvent(UUID victimUuid, float damageAmount, UUID attackerUuid, long timestamp) {
        this.victimUuid = victimUuid;
        this.damageAmount = damageAmount;
        this.attackerUuid = Optional.ofNullable(attackerUuid);
        this.timestamp = timestamp;
    }

    @Override
    public UUID getVictim() {
        return victimUuid;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public float getDamageAmount() {
        return damageAmount;
    }

    public Optional<UUID> getAttackerUuid() {
        return attackerUuid;
    }
}
