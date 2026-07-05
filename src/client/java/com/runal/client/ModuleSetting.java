package com.runal.client;

public interface ModuleSetting {
    String getLabel();
    String getDisplayValue();
    void onClick();

    default String getConfigKey() {
        return getLabel().toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
    }

    default String serialize() {
        return getDisplayValue();
    }

    default void deserialize(String value) {
    }

    /**
     * Restores this setting to the value it had when constructed.
     * No-op by default - override for settings that hold a persisted value.
     */
    default void resetToDefault() {
    }
}
