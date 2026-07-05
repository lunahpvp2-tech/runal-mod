package com.runal.client;

public class BossTitleState {
    public static boolean enabled = true;
    // Sentinel: not yet positioned. Resolved to the actual screen center on first render/drag,
    // since "center of the screen" depends on the player's current window size.
    public static int x = Integer.MIN_VALUE;
    public static int y = Integer.MIN_VALUE;
    public static int textColor = 0xFFF2C230;
    public static float scale = 2.5f;

    public static String currentText;
    public static int displayTicksRemaining;

    public static void ensureDefaultPosition(int screenWidth, int screenHeight) {
        if (x == Integer.MIN_VALUE) x = screenWidth / 2;
        if (y == Integer.MIN_VALUE) y = screenHeight / 2;
    }
}
