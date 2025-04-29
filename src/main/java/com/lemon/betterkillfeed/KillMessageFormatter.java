package com.lemon.betterkillfeed;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.util.*;

/// Creates kill messages.
public class KillMessageFormatter {
    public static Text formatKillMessage(Text message, EventParser parser) {

        // Index 0 = death key. Index 1 = victim. Index 2 = killer, if applicable
        List<String> deathDetails = extractDeathCauses(message);
        BetterKillfeed.LOGGER.info("Found players: {}, size: {}",deathDetails.getFirst(), deathDetails.size());

        List<UUID> playerUuids = new ArrayList<>();

        for (String name : deathDetails) {
            getUuidByName(name).ifPresent(playerUuids::add);
        }


        parser.parseEvent(new KillEvent(playerUuids.getFirst(), Util.getMeasuringTimeMs()));

        CombatSession session = parser.getCombatSession(playerUuids.getFirst());

        Optional<Text> itemHoverText = extractItemHoverText(message);

        MutableText usingText;

        String killer = getKiller(deathDetails);

        Map<String, Float> assistData = new HashMap<>();

        for (UUID uuid : session.getDamageContributions().keySet()) {
            assistData.put(getNameByUuid(uuid), session.getDamageContributions().get(uuid) / session.getTotalDamage());
        }

        MutableText hoverText = Text.literal("");

        if (assistData.isEmpty()) {
            hoverText.append(
                    Text.literal("Environment: 100.0%")
            );
        }

        // build the assist hover tooltip
        for (Map.Entry<String, Float> entry : assistData.entrySet()) {
            String name = entry.getKey();
            float percentage = entry.getValue() * 100.0f;

            String formattedPercent = String.format("%.1f", percentage);

            if (name.equals(killer) || name.equals("Environment") && killer.equals("falling")) {
                hoverText.append(
                        Text.literal(name + ": " + formattedPercent + "%\n")
                                .styled(style -> style.withColor(Formatting.GOLD))
                );
            }
            else {
                hoverText.append(
                        Text.literal(name + ": " + formattedPercent + "%\n")
                                .styled(style -> style.withColor(Formatting.GRAY))
                );
            }
        }

        if (itemHoverText.isPresent()) {
            usingText = Text.literal(" using ")
                    .append(itemHoverText.get());
        }
        else {
            usingText = Text.literal("");
        }

        return buildMessage(usingText, deathDetails.get(1), killer, hoverText, session.totemPops);
    }

    public static void formatKillMessage(UUID victimUuid, EventParser parser) {
        CombatSession session = parser.getCombatSession(victimUuid);

        UUID killerUuid = session.getMostRecentAttacker();

        String victimName = getNameByUuid(victimUuid);
        String killerName = getNameByUuid(killerUuid);

        MutableText empty = Text.literal("");

        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(buildMessage(empty, victimName, killerName, empty, session.totemPops));
    }

    private static Text buildMessage(Text usingText, String victim, String killer, MutableText hoverText, int totemPops) {

        MutableText prefix = Text.literal("[DEATH] ")
                .styled(style -> style.withColor(Formatting.DARK_RED));

        MutableText victimText = Text.literal(victim)
                .styled(style -> style.withColor(Formatting.GOLD));

        MutableText diedToText = Text.literal(" died to ")
                .styled(style -> style.withColor(Formatting.GRAY));

        MutableText sourceText = Text.literal(killer)
                .styled(style -> style.withColor(Formatting.GOLD));

        MutableText poppedTextFirst = Text.literal(", popping ")
                .styled(style -> style.withColor(Formatting.GRAY));

        MutableText poppedNumber = Text.literal(String.valueOf(totemPops))
                .styled(style -> style.withColor(Formatting.GOLD));

        MutableText poppedTextLast = Text.literal(" totems.")
                .styled(style -> style.withColor(Formatting.GRAY));

        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText);

        MutableText assistsText = Text.literal(" [Assists]")
                .styled(style -> style
                        .withColor(Formatting.DARK_PURPLE)
                        .withHoverEvent(hoverEvent)
                );


        return prefix
                .append(victimText)
                .append(diedToText)
                .append(sourceText)
                .append(usingText)
                .append(poppedTextFirst)
                .append(poppedNumber)
                .append(poppedTextLast)
                .append(assistsText);
    }

    private static String getKiller(List<String> deathDetails) {
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
        return killer;
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

    private static String getNameByUuid(UUID uuid) {
        ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();

        for (PlayerListEntry entry : handler.getPlayerList()) {
            if (entry.getProfile().getId().equals(uuid)) {
                return entry.getProfile().getName();
            }
        }

        return "Environment";
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
