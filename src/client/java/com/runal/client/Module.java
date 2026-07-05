package com.runal.client;

import java.util.Collections;
import java.util.List;

public interface Module {
    String getName();
    String getCategory();
    boolean isEnabled();
    void toggle();

    /**
     * Optional sub-settings shown when the module's row is right-clicked and expanded.
     * Empty by default - override to expose extra controls.
     */
    default List<ModuleSetting> getSettings() {
        return Collections.emptyList();
    }

    /**
     * Resets every setting (recursively, including groups) back to its constructed default.
     */
    default void resetSettings() {
        for (ModuleSetting setting : getSettings()) {
            setting.resetToDefault();
        }
    }
}