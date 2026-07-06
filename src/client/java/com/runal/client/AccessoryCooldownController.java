package com.runal.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import java.util.LinkedHashMap;
import java.util.Map;

public class AccessoryCooldownController {

    private record AbilityInfo(String itemName, int cooldownSeconds) {
    }

    private static final Map<String, AbilityInfo> ABILITIES = new LinkedHashMap<>();

    static {
        ABILITIES.put("Necrotic Power", new AbilityInfo("Shaman Lord Cloak", 100));
        ABILITIES.put("Poseidon's Blessing", new AbilityInfo("Poseidon's Blessing", 150));
        ABILITIES.put("Arcane Surge", new AbilityInfo("Ascendant Heart", 75));
        ABILITIES.put("Divine Link", new AbilityInfo("Elysian Cross", 120));
    }

    private static int tickCounter = 0;

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            handleMessage(message.getString());
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (tickCounter >= 20) {
                tickCounter = 0;
                AccessoryCooldownState.tickDown();
            }
        });
    }

    private static void handleMessage(String text) {
        for (Map.Entry<String, AbilityInfo> entry : ABILITIES.entrySet()) {
            if (text.contains(entry.getKey())) {
                AbilityInfo info = entry.getValue();
                AccessoryCooldownState.start(info.itemName(), info.cooldownSeconds());
            }
        }
    }
}
