package com.runal.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class DiscordPresenceController {
    private static final String CLIENT_ID = "1523235604106580019";
    private static final String LARGE_IMAGE_KEY = "runal_icon";
    private static final String SCEPTER_DISCORD = "https://discord.gg/98FWkw7VtD";
    private static final String RUNAL_DISCORD = "https://discord.gg/G9JrtKjQdh";
    private static final long RECONNECT_INTERVAL_MS = 15_000L;
    private static final long MIN_UPDATE_INTERVAL_MS = 15_000L;
    private static final long BOSS_FIGHT_HOLD_MS = 60_000L;

    private static final DiscordIpcClient client = new DiscordIpcClient();
    private static final AtomicBoolean running = new AtomicBoolean(true);

    private static volatile String pendingDetails;
    private static volatile String pendingState;
    private static String lastSentDetails;
    private static String lastSentState;
    private static long lastSendMs;
    private static long lastConnectAttemptMs;
    private static long sessionStartMs;

    public static void register() {
        sessionStartMs = System.currentTimeMillis();

        Thread thread = new Thread(DiscordPresenceController::ipcLoop, "Runal Discord RPC");
        thread.setDaemon(true);
        thread.start();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (DiscordPresenceState.enabled) updatePendingText();
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> {
            running.set(false);
            client.close();
        });
    }

    private static void updatePendingText() {
        pendingDetails = "Conquering the World";

        boolean fightingBoss = BossTitleState.lastBossName != null
                && System.currentTimeMillis() - BossTitleState.lastBossMessageMs < BOSS_FIGHT_HOLD_MS;
        if (fightingBoss) {
            pendingState = "Fighting " + BossTitleState.lastBossName + " in ScepterRPG";
            return;
        }

        if (!EventTrackerState.events.isEmpty()) {
            pendingState = EventTrackerState.events.values().iterator().next().name;
            return;
        }

        pendingState = null;
    }

    private static void ipcLoop() {
        while (running.get()) {
            if (!DiscordPresenceState.enabled) {
                if (client.isConnected()) client.close();
                sleep(1000);
                continue;
            }

            if (!client.isConnected()) {
                long now = System.currentTimeMillis();
                if (now - lastConnectAttemptMs < RECONNECT_INTERVAL_MS) {
                    sleep(500);
                    continue;
                }
                lastConnectAttemptMs = now;
                if (!client.connect(CLIENT_ID)) {
                    sleep(500);
                    continue;
                }
                lastSentDetails = null;
                lastSentState = null;
            }

            long now = System.currentTimeMillis();
            String details = pendingDetails;
            String state = pendingState;
            boolean changed = !equalsNullable(details, lastSentDetails) || !equalsNullable(state, lastSentState);
            boolean canSend = now - lastSendMs >= MIN_UPDATE_INTERVAL_MS;

            if (changed && canSend) {
                try {
                    client.sendActivity(buildActivityJson(details, state));
                    lastSentDetails = details;
                    lastSentState = state;
                    lastSendMs = now;
                } catch (IOException e) {
                    client.close();
                }
            }

            sleep(500);
        }
    }

    private static boolean equalsNullable(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String buildActivityJson(String details, String state) {
        JsonObject activity = new JsonObject();
        if (details != null) activity.addProperty("details", details);
        if (state != null) activity.addProperty("state", state);

        JsonObject timestamps = new JsonObject();
        timestamps.addProperty("start", sessionStartMs);
        activity.add("timestamps", timestamps);

        JsonObject assets = new JsonObject();
        assets.addProperty("large_image", LARGE_IMAGE_KEY);
        assets.addProperty("large_text", "Runal");
        activity.add("assets", assets);

        JsonArray buttons = new JsonArray();
        JsonObject scepterButton = new JsonObject();
        scepterButton.addProperty("label", "Play ScepterRPG");
        scepterButton.addProperty("url", SCEPTER_DISCORD);
        buttons.add(scepterButton);

        JsonObject runalButton = new JsonObject();
        runalButton.addProperty("label", "Use Runal");
        runalButton.addProperty("url", RUNAL_DISCORD);
        buttons.add(runalButton);
        activity.add("buttons", buttons);

        JsonObject args = new JsonObject();
        args.addProperty("pid", ProcessHandle.current().pid());
        args.add("activity", activity);

        JsonObject root = new JsonObject();
        root.addProperty("cmd", "SET_ACTIVITY");
        root.add("args", args);
        root.addProperty("nonce", UUID.randomUUID().toString());

        return root.toString();
    }
}
