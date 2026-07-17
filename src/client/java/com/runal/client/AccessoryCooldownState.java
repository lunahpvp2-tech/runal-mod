package com.runal.client;

import java.util.LinkedHashMap;
import java.util.Map;

public class AccessoryCooldownState {
    public static boolean enabled = true;
    public static int nameColor = 0xFF9C6BFF;
    public static int valueColor = 0xFFFFFFFF;

    public static class ActiveCooldown {
        public final int totalTicks;
        public int remainingTicks;

        public ActiveCooldown(int totalTicks) {
            this.totalTicks = totalTicks;
            this.remainingTicks = totalTicks;
        }

        public int remainingSecondsCeil() {
            return (remainingTicks + 19) / 20;
        }
    }

    public static final Map<String, ActiveCooldown> active = new LinkedHashMap<>();

    public static void start(String itemName, double seconds) {
        int ticks = Math.max(1, (int) Math.round(seconds * 20));
        active.put(itemName, new ActiveCooldown(ticks));
    }

    public static void tick() {
        active.values().removeIf(cooldown -> {
            cooldown.remainingTicks--;
            return cooldown.remainingTicks <= 0;
        });
    }
}
