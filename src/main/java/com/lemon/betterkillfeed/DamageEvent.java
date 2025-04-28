package com.lemon.betterkillfeed;

import java.util.Optional;
import java.util.UUID;

public class DamageEvent implements CombatEvent {
    private final UUID victimUuid;
    private final float damageAmount;
    private UUID attackerUuid;
    private final long timestamp;

    public DamageEvent(UUID victimUuid, float damageAmount, long timestamp) {
        this.victimUuid = victimUuid;
        this.damageAmount = damageAmount;
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

    public UUID getAttackerUuid() {
        return attackerUuid;
    }

    public void setAttackerUuid(UUID attackerUuid) {
        this.attackerUuid = attackerUuid;
    }
}
