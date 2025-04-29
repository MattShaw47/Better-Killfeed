package com.lemon.betterkillfeed;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Objects;

public class BetterKillfeedClient implements ClientModInitializer {

    private static final String MOD_ID = "better-killfeed";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static final EventParser EVENT_PARSER = new EventParser();

    // Milliseconds between each assumed server tick. Decides how often onServerTick will run.
    private static final long SERVER_TICK_INTERVAL = 500L;

    private long accumMs = 0;

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

        ClientTickEvents.END_CLIENT_TICK.register(this::onServerTick);

        ServerMessageEvents.GAME_MESSAGE.register((server, message, sender) -> {
            if (message.getContent() instanceof TranslatableTextContent translatable
                    && translatable.getKey().startsWith("death.")) {

                LOGGER.info("CAUGHT DEATH MSG KEY: {}, NORMAL: {}", translatable.getKey(), message.getContent());

                Text newKillMessage = KillMessageFormatter.formatKillMessage(message, EVENT_PARSER);

                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(newKillMessage);

                EVENT_PARSER.checkForExpiredSessions(10000L);
            }
        });



    }

    /// Processes which should run continuously
    private void onServerTick(MinecraftClient client) {
        long now = Util.getMeasuringTimeMs();

        if (now - accumMs >= SERVER_TICK_INTERVAL) {
            accumMs = now;

            EVENT_PARSER.checkForExpiredSessions(10000L);
            EVENT_PARSER.onTick();
        }
    }

    public static void registerEvent(CombatEvent event) {
        EVENT_PARSER.parseEvent(event);
    }
}
