package com.runal.client;

import java.util.Collections;
import java.util.List;

public interface Module {
    String getName();
    String getCategory();
    boolean isEnabled();
    void toggle();

    default List<ModuleSetting> getSettings() {
        return Collections.emptyList();
    }

    default void resetSettings() {
        for (ModuleSetting setting : getSettings()) {
            setting.resetToDefault();
        }
    }
}