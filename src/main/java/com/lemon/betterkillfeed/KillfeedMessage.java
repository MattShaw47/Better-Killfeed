package com.lemon.betterkillfeed;

import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class KillfeedMessage {
    public final Text message;
    public final List<UUID> players;
    public final long timestamp;

    public KillfeedMessage(Text message, UUID player, long timestamp) {
        this.message = message;
        this.players = Collections.singletonList(player);
        this.timestamp = timestamp;
    }
}
