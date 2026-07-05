package com.runal.client;

public class AutoGGState {
    public static final AutoGGState INSTANCE = new AutoGGState();

    private boolean enabled = false;
    public String response = "gg";

    private AutoGGState() {}

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }
}
