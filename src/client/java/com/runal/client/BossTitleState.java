package com.runal.client;

public class BossTitleState {
    public static boolean enabled = true;
    public static int x = Integer.MIN_VALUE;
    public static int y = Integer.MIN_VALUE;
    public static int textColor = 0xFFF2C230;
    public static float scale = 2.5f;

    public static String currentText;
    public static String currentBossName;
    public static int displayTicksRemaining;

    public static String lastBossName;
    public static long lastBossMessageMs;

    public static void ensureDefaultPosition(int screenWidth, int screenHeight) {
        if (x == Integer.MIN_VALUE) x = screenWidth / 2;
        if (y == Integer.MIN_VALUE) y = screenHeight / 2;
    }
}
