package com.runal.client;

public class AutoGGAchievementState {
    public static final AutoGGAchievementState INSTANCE = new AutoGGAchievementState();

    private boolean enabled = false;
    public String response = "gg";

    private AutoGGAchievementState() {}

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }
}
