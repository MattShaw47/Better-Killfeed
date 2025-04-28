package com.lemon.betterkillfeed;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BetterKillfeedClient implements ClientModInitializer {

    private static final String MOD_ID = "better-killfeed";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static final EventParser EVENT_PARSER = new EventParser();

    /// TODO:
    /// Track players alive / dead
    /// Track hits received / given
    ///     For given:
    ///         check if projectile was involved
    ///         nearby player melee attack based on proximity, swing animation, facing
    ///         tie break based on who swung most recently
    ///         fallback to "non-combat" source.
    /// detect totem pops
    /// detect killfeed entries
    /// store data into a seperate class
    /// render a killfeed gui upon buttonpress
    ///
    @Override
    public void onInitializeClient() {
        LOGGER.info("initializing " + MOD_ID);
    }

    /// Processes which should run every server tick (20 times per second)
    private void onServerTick(MinecraftClient client) {
        if (client.world == null) return;


        for (PlayerEntity player : client.world.getPlayers()) {

        }
    }

    public static void registerEvent(CombatEvent event) {
        EVENT_PARSER.parseEvent(event);
    }
}
