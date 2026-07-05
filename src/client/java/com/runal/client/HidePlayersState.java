package com.runal.client;

public class HidePlayersState {
    public static final HidePlayersState INSTANCE = new HidePlayersState();
    private boolean enabled = false;
    private HidePlayersState() {}
    public boolean isEnabled() { return enabled; }
    public void toggle() { enabled = !enabled; }
}