package com.runal.client;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class AutoGGAchievementController {

    private static final String ACHIEVEMENT_MARKER = "has completed the achievement ";

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!AutoGGAchievementState.INSTANCE.isEnabled()) return;
            if (overlay) return;

            if (!shouldTrigger(message)) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.getConnection() == null) return;

            String response = AutoGGAchievementState.INSTANCE.response.trim();
            if (response.isEmpty()) return;

            mc.getConnection().sendChat(response);
        });
    }

    private static boolean shouldTrigger(Component message) {
        String fullText = message.getString().replaceAll("\\p{Zs}", " ");

        // Same guards as the item-find Auto GG - a guild-chat "[G]" line could otherwise spoof
        // this by typing the same phrase, and genuine broadcasts always carry the "|" bar.
        if (fullText.contains("[G]")) return false;
        if (!fullText.contains("|")) return false;

        return fullText.contains(ACHIEVEMENT_MARKER);
    }
}
