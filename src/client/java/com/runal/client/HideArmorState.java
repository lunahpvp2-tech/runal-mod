package com.runal.client;

import net.minecraft.world.entity.EquipmentSlot;

public class HideArmorState {
    public static final HideArmorState INSTANCE = new HideArmorState();
    private boolean enabled = false;
    public boolean hideForOthers = true;
    public boolean hideHelmet = true;
    public boolean hideChestplate = true;
    public boolean hideLeggings = true;
    public boolean hideBoots = true;

    private HideArmorState() {}
    public boolean isEnabled() { return enabled; }
    public void toggle() { enabled = !enabled; }

    public boolean shouldHide(EquipmentSlot slot) {
        if (!enabled) return false;
        return switch (slot) {
            case HEAD -> hideHelmet;
            case CHEST -> hideChestplate;
            case LEGS -> hideLeggings;
            case FEET -> hideBoots;
            default -> false;
        };
    }
}
