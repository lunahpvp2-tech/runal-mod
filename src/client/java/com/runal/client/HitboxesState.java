package com.runal.client;

public class HitboxesState {
    public static final HitboxesState INSTANCE = new HitboxesState();

    private boolean enabled = false;
    public int playerColor = 0xB02A2D34;
    public int entityColor = 0xB0D6483C;
    public float lineWidth = 2.0f;

    private HitboxesState() {}

    public boolean isEnabled() { return enabled; }
    public void toggle() { enabled = !enabled; }
}
