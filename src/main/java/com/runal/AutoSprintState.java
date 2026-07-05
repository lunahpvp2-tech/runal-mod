package com.runal;

public class AutoSprintState {
    public static final AutoSprintState INSTANCE = new AutoSprintState();

    private boolean enabled = false;

    private AutoSprintState() {}

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }

    public void setEnabled(boolean value) {
        enabled = value;
    }
}