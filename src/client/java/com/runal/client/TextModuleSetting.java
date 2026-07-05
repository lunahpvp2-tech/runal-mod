package com.runal.client;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class TextModuleSetting implements ModuleSetting {
    private final String label;
    private final Supplier<String> getter;
    private final Consumer<String> setter;
    private final String defaultValue;
    private boolean editing;

    public TextModuleSetting(String label, Supplier<String> getter, Consumer<String> setter) {
        this.label = label;
        this.getter = getter;
        this.setter = setter;
        this.defaultValue = getter.get();
    }

    public boolean isEditing() { return editing; }
    public void stopEditing() { editing = false; }

    public void append(char c) {
        if (!editing || Character.isISOControl(c)) return;
        setter.accept(getter.get() + c);
        ModuleConfig.save();
    }

    public void backspace() {
        String value = getter.get();
        if (!value.isEmpty()) {
            setter.accept(value.substring(0, value.length() - 1));
            ModuleConfig.save();
        }
    }

    @Override
    public String getLabel() { return label; }

    @Override
    public String getDisplayValue() { return editing ? getter.get() + "_" : getter.get(); }

    @Override
    public void onClick() { editing = true; }

    @Override
    public String serialize() { return getter.get(); }

    @Override
    public void deserialize(String value) { setter.accept(value); }

    @Override
    public void resetToDefault() {
        editing = false;
        setter.accept(defaultValue);
    }
}
