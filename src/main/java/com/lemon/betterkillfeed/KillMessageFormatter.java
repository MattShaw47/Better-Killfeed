package com.lemon.betterkillfeed;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/// Creates kill messages.
public class KillMessageFormatter {
    public static Text formatKillMessage(Text message, EventParser parser) {
        BetterKillfeed.LOGGER.info("Starting formatting");

        // Index 0 = death key. Index 1 = victim. Index 2 = killer, if applicable
        List<String> deathDetails = extractDeathCauses(message);
        BetterKillfeed.LOGGER.info("Found players: {}, size: {}",deathDetails.getFirst(), deathDetails.size());

        List<UUID> playerUuids = new ArrayList<>();

        for (String name : deathDetails) {
            getUuidByName(name).ifPresent(playerUuids::add);
        }

        CombatSession session = parser.getCombatSession(playerUuids.getFirst());

        Optional<Text> itemHoverText = extractItemHoverText(message);

        MutableText usingText;

        String killer;

        String deathKey = deathDetails.getFirst();

        if (deathDetails.size() == 3) {
            if (deathKey.contains("explosion")) {
                killer = deathDetails.getLast() + " using explosives";
            }
            else {
                killer = deathDetails.getLast();
            }
        }
        else if (deathKey.startsWith("death.fell")) {
            killer = "falling";
        }
        else if (deathKey.startsWith("death.attack.lava")) {
            killer = "lava";
        }
        else if (deathKey.startsWith("death.attack.onFire") || deathKey.startsWith("death.attack.inFire")) {
            killer = "fire";
        }
        else if (deathKey.startsWith("death.attack.explosion")) {
            killer = "explosion";
        }
        else if (deathKey.startsWith("death.attack.outOfWorld")) {
            killer = "void";
        }
        else if (deathKey.startsWith("death.attack.inWall")) {
            killer = "suffocation";
        }
        else {
            killer = "environment";
        }

        if (itemHoverText.isPresent()) {
            usingText = Text.literal(" using ")
                    .append(itemHoverText.get());
        }
        else {
            usingText = Text.literal("");
        }

        MutableText prefix = Text.literal("[DEATH] ")
                .styled(style -> style.withColor(Formatting.DARK_RED));

        MutableText victimText = Text.literal(deathDetails.get(1))
                .styled(style -> style.withColor(Formatting.GOLD));

        MutableText diedToText = Text.literal(" died to ")
                .styled(style -> style.withColor(Formatting.GRAY));

        MutableText sourceText = Text.literal(killer)
                .styled(style -> style.withColor(Formatting.GOLD));

        MutableText poppedTextFirst = Text.literal(", and popped ")
                .styled(style -> style.withColor(Formatting.GRAY));

        MutableText poppedNumber = Text.literal(String.valueOf(session.totemPops))
                .styled(style -> style.withColor(Formatting.GOLD));

        MutableText poppedTextLast = Text.literal(" totems.")
                .styled(style -> style.withColor(Formatting.GRAY));

        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                Text.literal("TEMP")
        );

        MutableText assistsText = Text.literal(" [Assists]")
                .styled(style -> style
                        .withColor(Formatting.DARK_PURPLE)
                        .withHoverEvent(hoverEvent)
                );

        Text killMessage = prefix
                .append(victimText)
                .append(diedToText)
                .append(sourceText)
                .append(usingText)
                .append(poppedTextFirst)
                .append(poppedNumber)
                .append(poppedTextLast)
                .append(assistsText);


        return killMessage;
    }

    private static Optional<UUID> getUuidByName(String name) {
        ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();

        PlayerListEntry entry = handler.getPlayerList().stream()
                .filter(e -> e.getProfile().getName().equals(name))
                .findFirst().orElse(null);

        if (entry != null) {
            return Optional.of(entry.getProfile().getId());
        }

        return Optional.empty();
    }

    private static List<String> extractDeathCauses(Text text) {
        List<String> causes = new ArrayList<>();

        if (text.getContent() instanceof TranslatableTextContent translatable) {
            causes.add(translatable.getKey());

            for (Object arg : translatable.getArgs()) {
                if (arg instanceof Text argText) {
                    causes.add(argText.getString());
                } else if (arg instanceof String str) {
                    causes.add(str);
                } else {
                    // Fallback
                    causes.add(arg.toString());
                }
            }
        }
        return causes;
    }

    private static Optional<Text> extractItemHoverText(Text deathMessage) {
        if (deathMessage.getContent() instanceof TranslatableTextContent translatable) {
            for (Object arg : translatable.getArgs()) {
                if (arg instanceof Text textArg) {
                    var hoverEvent = textArg.getStyle().getHoverEvent();
                    if (hoverEvent != null && hoverEvent.getAction() == HoverEvent.Action.SHOW_ITEM) {
                        return Optional.of(textArg);
                    }
                }
            }
        }
        return Optional.empty();
    }

}
