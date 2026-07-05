package com.runal.client;

public class HotbarSwapState {
    public static final HotbarSwapState INSTANCE = new HotbarSwapState();

    public static final int SLOT_COUNT = 5;
    public static final String ROW_TOP = "Top";
    public static final String ROW_MIDDLE = "Middle";
    public static final String ROW_BOTTOM = "Bottom";

    public final String[] rows = new String[SLOT_COUNT];

    private HotbarSwapState() {
        for (int i = 0; i < SLOT_COUNT; i++) rows[i] = ROW_BOTTOM;
    }
}
