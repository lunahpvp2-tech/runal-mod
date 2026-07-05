package com.runal.client;

public class ButtonModuleSetting implements ModuleSetting {
    private final String label;
    private final String display;
    private final Runnable action;

    public ButtonModuleSetting(String label, String display, Runnable action) {
        this.label = label;
        this.display = display;
        this.action = action;
    }

    @Override
    public String getLabel() { return label; }

    @Override
    public String getDisplayValue() { return display; }

    @Override
    public void onClick() { action.run(); }
}
