package com.runal.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;

public class LowHealthWarning {

    private static boolean enabled = true;
    private static int tickCounter = 0;

    public static String warningMode = "Sound";
    public static boolean lowHpEnabled = true;
    public static float lowHpThreshold = 0.25f;
    public static String lowHpTitle = "LOW HP";
    public static boolean midHpEnabled = true;
    public static float midHpThreshold = 0.50f;
    public static String midHpTitle = "MID HP";
    public static boolean soundEnabled = true;
    public static float soundVolume = 1.0f;
    public static int lowTitleX = 400;
    public static int lowTitleY = 40;
    public static int midTitleX = 400;
    public static int midTitleY = 90;

    public static boolean isEnabled() { return enabled; }
    public static void toggle() { enabled = !enabled; }

    public static String getActiveTitleKind() {
        if (!enabled) return null;
        if (!"Title".equals(warningMode) && !"Both".equals(warningMode)) return null;

        LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
        if (player == null) return null;

        float healthPercent = player.getHealth() / player.getMaxHealth();
        if (lowHpEnabled && healthPercent <= lowHpThreshold) return "low";
        if (midHpEnabled && healthPercent <= midHpThreshold) return "mid";
        return null;
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!enabled) return;
            LocalPlayer player = client.player;
            if (player == null) return;

            float healthPercent = player.getHealth() / player.getMaxHealth();
            if (lowHpEnabled && healthPercent <= lowHpThreshold) {
                tickCounter++;
                if (soundEnabled && tickCounter >= 5) {
                    player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), soundVolume, 1.6f);
                    tickCounter = 0;
                }
            } else if (midHpEnabled && healthPercent <= midHpThreshold) {
                tickCounter++;
                if (soundEnabled && tickCounter >= 10) {
                    player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), soundVolume, 0.6f);
                    tickCounter = 0;
                }
            } else {
                tickCounter = 0;
            }
        });
    }
}
