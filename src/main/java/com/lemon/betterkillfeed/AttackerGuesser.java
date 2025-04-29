package com.lemon.betterkillfeed;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.world.World;
import net.minecraft.util.math.Box;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

/// Given a damageevent, applies various datas to determine who the attacker is.
public class AttackerGuesser {
    public static final UUID ENVIRONMENT_UUID = new UUID(0L, 0L);

    public static UUID guessAttacker(PlayerEntity victim) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return ENVIRONMENT_UUID;

        // Expand victim's hitbox to find nearby entities
        Box searchBox = victim.getBoundingBox().expand(10.0);
        List<Entity> nearbyEntities = client.world.getOtherEntities(null, searchBox, (entity) -> true);

        // 1. Try to detect projectile attack
        for (Entity entity : nearbyEntities) {
            if (entity instanceof PersistentProjectileEntity projectile) {
                if (projectile.getBoundingBox().intersects(victim.getBoundingBox().expand(2.0))) {
                    if (projectile.getOwner() instanceof PlayerEntity attacker) {
                        return attacker.getUuid();
                    }
                }
            }
        }

        // 2. Try to detect melee attacker
        PlayerEntity bestCandidate = null;
        double bestDistance = Double.MAX_VALUE;

        for (Entity entity : nearbyEntities) {
            if (entity instanceof PlayerEntity player) {
                if (player == victim) continue; // Skip victim themselves

                double distance = player.distanceTo(victim);
                if (distance <= 5.0) {
                    if (player.handSwinging && player.handSwingTicks <= 5) { // Recently swung
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            bestCandidate = player;
                        }
                    }
                }
            }
        }

        if (bestCandidate != null) {
            return bestCandidate.getUuid();
        }

        // 3. No good guess -> default to environment
        return ENVIRONMENT_UUID;
    }
}

