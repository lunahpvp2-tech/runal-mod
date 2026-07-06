package com.runal.client;

import java.util.LinkedHashMap;
import java.util.Map;

public class AccessoryCooldownState {
    public static boolean enabled = true;
    public static int nameColor = 0xFF9C6BFF;
    public static int valueColor = 0xFFFFFFFF;

    public static class ActiveCooldown {
        public final int totalSeconds;
        public int remainingSeconds;

        public ActiveCooldown(int totalSeconds) {
            this.totalSeconds = totalSeconds;
            this.remainingSeconds = totalSeconds;
        }
    }

    public static final Map<String, ActiveCooldown> active = new LinkedHashMap<>();

    public static void start(String itemName, int seconds) {
        active.put(itemName, new ActiveCooldown(seconds));
    }

    public static void tickDown() {
        active.values().removeIf(cooldown -> {
            cooldown.remainingSeconds--;
            return cooldown.remainingSeconds <= 0;
        });
    }
}
