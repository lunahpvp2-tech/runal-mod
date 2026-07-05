package com.runal.client;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class ColorModuleSetting implements ModuleSetting {
    private final String label;
    private final IntSupplier getter;
    private final IntConsumer setter;
    private final List<ModuleSetting> channelSettings;
    private final int defaultColor;
    private boolean editing = false;

    public ColorModuleSetting(String label, IntSupplier getter, IntConsumer setter) {
        this.label = label;
        this.getter = getter;
        this.setter = setter;
        this.defaultColor = getter.getAsInt();
        this.channelSettings = List.of(
                new SliderModuleSetting("Red", 0f, 255f, 1f, () -> (float) ((getColor() >>> 16) & 255), v -> setChannel(16, Math.round(v))),
                new SliderModuleSetting("Green", 0f, 255f, 1f, () -> (float) ((getColor() >>> 8) & 255), v -> setChannel(8, Math.round(v))),
                new SliderModuleSetting("Blue", 0f, 255f, 1f, () -> (float) (getColor() & 255), v -> setChannel(0, Math.round(v))),
                new SliderModuleSetting("Alpha", 0f, 255f, 1f, () -> (float) ((getColor() >>> 24) & 255), v -> setChannel(24, Math.round(v)))
        );
    }

    public int getColor() { return getter.getAsInt(); }
    public boolean isEditing() { return editing; }
    public List<ModuleSetting> getChannelSettings() { return channelSettings; }

    public void setColor(int color) {
        setter.accept(color);
        ModuleConfig.save();
    }

    private void setChannel(int shift, int value) {
        int color = getColor();
        int clamped = Math.max(0, Math.min(255, value));
        int mask = ~(255 << shift);
        setColor((color & mask) | (clamped << shift));
    }

    @Override
    public String getLabel() { return label; }

    @Override
    public String getDisplayValue() { return String.format("#%08X", getColor()); }

    @Override
    public void onClick() { editing = !editing; }

    @Override
    public String serialize() { return String.valueOf(getColor()); }

    @Override
    public void deserialize(String value) {
        try { setter.accept((int) Long.parseLong(value)); }
        catch (NumberFormatException ignored) {}
    }

    @Override
    public void resetToDefault() { setter.accept(defaultColor); }
}
