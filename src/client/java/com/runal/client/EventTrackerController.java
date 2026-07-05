package com.runal.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventTrackerController {

    private static final Pattern UPCOMING_PATTERN = Pattern.compile("^(.+) begins in (\\d+) minutes?$");
    private static final Pattern ONGOING_PATTERN = Pattern.compile("^(.+) is ongoing$");
    private static final Pattern FORMING_PATTERN = Pattern.compile("^(.+) is forming\\.*$");
    private static final Pattern APPEARED_PATTERN = Pattern.compile("^(.+) has appeared$");
    private static final Pattern DURATION_PATTERN = Pattern.compile("^Duration: (\\d+) minutes?$");
    private static final Pattern SPAWN_PATTERN = Pattern.compile("^Spawn: (\\d+) minutes?$");
    private static final Pattern GENERIC_MINUTES_PATTERN = Pattern.compile("^.*?\\b(\\d+) minutes?\\b.*$");

    private static final long PENDING_WINDOW_MS = 3000L;

    private static String pendingDurationFor;
    private static long pendingSince;
    private static String pendingNextPhaseFor;
    private static long pendingNextPhaseSince;

    private static int tickCounter = 0;

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            handleMessage(message.getString().trim());
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (tickCounter >= 20) {
                tickCounter = 0;
                EventTrackerState.tickDown();
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> EventTrackerState.clear());
    }

    private static void handleMessage(String rawText) {
        if (rawText.isEmpty()) return;
        String text = stripDecoration(rawText);
        if (text.isEmpty()) return;

        Integer minutesInLine = readMinutes(text);

        if (pendingDurationFor != null && System.currentTimeMillis() - pendingSince < PENDING_WINDOW_MS) {
            if (minutesInLine != null) {
                int seconds = minutesInLine * 60;
                EventTrackerState.put(pendingDurationFor, seconds);
                EventTrackerState.knownDurationSeconds.put(pendingDurationFor, seconds);
                pendingNextPhaseFor = pendingDurationFor;
                pendingNextPhaseSince = System.currentTimeMillis();
                pendingDurationFor = null;
                return;
            }
        }

        if (pendingNextPhaseFor != null && System.currentTimeMillis() - pendingNextPhaseSince < PENDING_WINDOW_MS) {
            if (minutesInLine != null) {
                EventTrackerState.setNextPhase(pendingNextPhaseFor, minutesInLine * 60);
                pendingNextPhaseFor = null;
                return;
            }
        }

        Matcher upcoming = UPCOMING_PATTERN.matcher(text);
        if (upcoming.matches()) {
            String name = upcoming.group(1);
            int minutes = Integer.parseInt(upcoming.group(2));
            EventTrackerState.put(name, minutes * 60);
            return;
        }

        Matcher ongoing = ONGOING_PATTERN.matcher(text);
        if (ongoing.matches()) {
            String name = ongoing.group(1);
            Integer known = EventTrackerState.knownDurationSeconds.get(name);
            EventTrackerState.put(name, known != null ? known : -1);
            pendingDurationFor = name;
            pendingSince = System.currentTimeMillis();
            return;
        }

        Matcher forming = FORMING_PATTERN.matcher(text);
        if (forming.matches()) {
            String name = forming.group(1);
            EventTrackerState.put(name, -1);
            pendingDurationFor = name;
            pendingSince = System.currentTimeMillis();
            return;
        }

        Matcher appeared = APPEARED_PATTERN.matcher(text);
        if (appeared.matches()) {
            String name = appeared.group(1);
            EventTrackerState.put(name, -1);
        }
    }

    private static Integer readMinutes(String text) {
        Matcher durationMatch = DURATION_PATTERN.matcher(text);
        if (durationMatch.matches()) return Integer.parseInt(durationMatch.group(1));

        Matcher spawnMatch = SPAWN_PATTERN.matcher(text);
        if (spawnMatch.matches()) return Integer.parseInt(spawnMatch.group(1));

        Matcher genericMinutes = GENERIC_MINUTES_PATTERN.matcher(text);
        if (genericMinutes.matches()) return Integer.parseInt(genericMinutes.group(1));

        return null;
    }

    private static String stripDecoration(String text) {
        String cleaned = text.replaceAll("\\p{So}", "").trim();
        cleaned = cleaned.replaceAll("^[\\s\\-=~*•●■□▪▫◆♦➤<>|]+", "");
        cleaned = cleaned.replaceAll("[\\s\\-=~*•●■□▪▫◆♦➤<>|]+$", "");
        cleaned = cleaned.replaceAll("\\.{2,}\\s*$", "");
        return cleaned.trim();
    }
}
