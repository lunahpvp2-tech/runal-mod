package com.runal.client;

import net.minecraft.ChatFormatting;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AutoGGController {

    private static final String FOUND_MARKER = "has found ";

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!AutoGGState.INSTANCE.isEnabled()) return;
            if (overlay) return;

            if (!shouldTrigger(message)) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.getConnection() == null) return;

            String response = AutoGGState.INSTANCE.response.trim();
            if (response.isEmpty()) return;

            mc.getConnection().sendChat(response);
        });
    }

    private static boolean shouldTrigger(Component message) {
        List<StyledRun> runs = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        List<int[]> ranges = new ArrayList<>();

        message.visit((style, text) -> {
            int start = sb.length();
            sb.append(text);
            ranges.add(new int[]{start, sb.length()});
            runs.add(new StyledRun(text, style));
            return Optional.<Void>empty();
        }, Style.EMPTY);

        String fullText = sb.toString().replaceAll("\\p{Zs}", " ");

        // A guild-chat line always carries its "[G]" tag - reject those outright, whoever's
        // typing could say "has found" themselves. Otherwise, this server marks its own genuine
        // broadcasts with a "|" bar, so require both that and the actual find text.
        if (fullText.contains("[G]")) return false;
        if (!fullText.contains("|")) return false;

        int foundIdx = fullText.indexOf(FOUND_MARKER);
        if (foundIdx < 0) return false;
        int itemStart = foundIdx + FOUND_MARKER.length();

        String itemSegment = fullText.substring(itemStart);
        // &6 (gold) = legendary, &5 (dark purple) = epic, &d (light purple) = mythical.
        if (containsLegacyCode(itemSegment, '6')) return AutoGGState.INSTANCE.triggerLegendary;
        if (containsLegacyCode(itemSegment, '5')) return AutoGGState.INSTANCE.triggerEpic;
        if (containsLegacyCode(itemSegment, 'd')) return AutoGGState.INSTANCE.triggerMythical;

        Style itemStyle = null;
        for (int i = 0; i < ranges.size(); i++) {
            int[] range = ranges.get(i);
            if (itemStart >= range[0] && itemStart < range[1]) {
                itemStyle = runs.get(i).style;
                break;
            }
        }
        if (itemStyle == null) return false;

        if (isRarity(itemStyle, ChatFormatting.GOLD)) return AutoGGState.INSTANCE.triggerLegendary;
        if (isRarity(itemStyle, ChatFormatting.DARK_PURPLE)) return AutoGGState.INSTANCE.triggerEpic;
        if (isRarity(itemStyle, ChatFormatting.LIGHT_PURPLE)) return AutoGGState.INSTANCE.triggerMythical;
        return false;
    }

    private static boolean containsLegacyCode(String text, char colorCode) {
        String lower = text.toLowerCase();
        return lower.contains("§" + colorCode) && lower.contains("§l");
    }

    private static boolean isRarity(Style style, ChatFormatting expected) {
        return style.isBold()
                && style.getColor() != null
                && expected.getColor() != null
                && style.getColor().getValue() == expected.getColor();
    }

    private record StyledRun(String text, Style style) {
    }
}
