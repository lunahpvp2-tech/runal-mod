package com.runal.client;

import java.util.List;

public class SettingGroup implements ModuleSetting {
    private final String label;
    private final List<ModuleSetting> settings;
    private boolean expanded = true;

    public SettingGroup(String label, List<ModuleSetting> settings) {
        this.label = label;
        this.settings = settings;
    }

    public List<ModuleSetting> getSettings() { return settings; }
    public boolean isExpanded() { return expanded; }

    @Override
    public String getLabel() { return label; }

    @Override
    public String getDisplayValue() { return expanded ? "-" : "+"; }

    @Override
    public void onClick() { expanded = !expanded; }

    @Override
    public void resetToDefault() {
        for (ModuleSetting setting : settings) setting.resetToDefault();
    }
}
