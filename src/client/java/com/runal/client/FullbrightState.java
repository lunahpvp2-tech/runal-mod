package com.runal.client;

public class FullbrightState {
    public static final FullbrightState INSTANCE = new FullbrightState();

    private boolean enabled = false;

    private FullbrightState() {}

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }
}