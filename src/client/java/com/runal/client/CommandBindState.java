package com.runal.client;

public class CommandBindState {
    public static final CommandBindState INSTANCE = new CommandBindState();

    public static final int SLOT_COUNT = 5;

    public final String[] commands = new String[SLOT_COUNT];

    private CommandBindState() {
        for (int i = 0; i < SLOT_COUNT; i++) commands[i] = "";
    }
}
