package com.runal.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class ArmorCooldownController {
    private static final int SCAN_INTERVAL_TICKS = 6;
    private static final EquipmentSlot[] SLOTS =
            {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private static int tickCounter = 0;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ArmorCooldownController::tick);
    }

    private static void tick(Minecraft mc) {
        if (!ArmorCooldownHudState.enabled || mc.player == null) return;

        tickCounter++;
        if (tickCounter < SCAN_INTERVAL_TICKS) return;
        tickCounter = 0;

        ArmorCooldownHudState.names.clear();
        ArmorCooldownHudState.percents.clear();
        ArmorCooldownHudState.secondsRemaining.clear();

        long nowTick = mc.player.tickCount;
        Set<String> activeKeys = new HashSet<>();

        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = mc.player.getItemBySlot(slot);
            if (stack.isEmpty() || !mc.player.getCooldowns().isOnCooldown(stack)) continue;

            float percent = mc.player.getCooldowns().getCooldownPercent(stack, 1.0f);
            String name = stack.getHoverName().getString();
            String key = "armor:" + name;
            activeKeys.add(key);

            Integer remainingTicks = CooldownDurationEstimator.estimateRemainingTicks(key, percent, nowTick);
            ArmorCooldownHudState.names.add(name);
            ArmorCooldownHudState.percents.add(Math.round(percent * 100f));
            ArmorCooldownHudState.secondsRemaining.add(remainingTicks != null ? (remainingTicks + 19) / 20 : null);
        }

        CooldownDurationEstimator.pruneExcept("armor:", activeKeys);
    }
}
