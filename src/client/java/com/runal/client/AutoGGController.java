package com.runal.client;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;

import java.util.regex.Pattern;

public class AutoGGController {

    private static final Pattern HAS_FOUND_PATTERN = Pattern.compile("^([A-Za-z0-9_]{1,16}) has found (.+)!$");

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!AutoGGState.INSTANCE.isEnabled()) return;
            if (overlay) return;

            boolean found = false;
            for (String line : message.getString().split("\n")) {
                if (HAS_FOUND_PATTERN.matcher(stripDecoration(line.trim())).matches()) {
                    found = true;
                    break;
                }
            }
            if (!found) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.getConnection() == null) return;

            String response = AutoGGState.INSTANCE.response.trim();
            if (response.isEmpty()) return;

            mc.getConnection().sendChat(response);
        });
    }

    private static String stripDecoration(String text) {
        return text.replaceAll("^[\\p{So}\\p{Co}\\s]+", "");
    }
}
