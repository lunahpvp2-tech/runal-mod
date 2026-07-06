package com.runal.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DungeonTrackerController {
    private static final String[] ROOM_CYCLE = {
            "Normal", "Normal", "Normal", "Normal", "Parkour",
            "Normal", "Normal", "Normal", "Boss", "Treasure",
    };

    private static final Pattern ROOM_PATTERN = Pattern.compile("^(.+?)\\s*[-–—]\\s*Room (\\d+)$");
    private static final long TIMEOUT_MS = 60_000L;

    private static int tickCounter = 0;

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            handleMessage(stripDecoration(message.getString().trim()));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (tickCounter >= 20) {
                tickCounter = 0;
                if (DungeonTrackerState.dungeonName != null
                        && System.currentTimeMillis() - DungeonTrackerState.lastMessageMs >= TIMEOUT_MS) {
                    DungeonTrackerState.dungeonName = null;
                }
            }
        });
    }

    private static void handleMessage(String text) {
        Matcher matcher = ROOM_PATTERN.matcher(text);
        if (!matcher.matches()) return;

        DungeonTrackerState.dungeonName = matcher.group(1).trim();
        DungeonTrackerState.currentRoom = Integer.parseInt(matcher.group(2));
        DungeonTrackerState.lastMessageMs = System.currentTimeMillis();
    }

    public static int roomsUntil(String type) {
        int currentRoom = DungeonTrackerState.currentRoom;
        for (int offset = 0; offset < ROOM_CYCLE.length; offset++) {
            int idx = (currentRoom - 1 + offset) % ROOM_CYCLE.length;
            if (ROOM_CYCLE[idx].equals(type)) return offset;
        }
        return -1;
    }

    private static String stripDecoration(String text) {
        String cleaned = text.replaceAll("[\\p{So}\\p{Co}]", "");
        cleaned = cleaned.replaceAll("\\p{Zs}", " ");
        return cleaned.trim();
    }
}
