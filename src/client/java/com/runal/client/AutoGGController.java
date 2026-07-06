package com.runal.client;

import net.minecraft.ChatFormatting;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoGGController {

    private static final Pattern HAS_FOUND_PATTERN =
            Pattern.compile("^([A-Za-z0-9_]{1,16}) has found (.+)!$", Pattern.MULTILINE);

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
            String cleaned = normalize(text);
            int start = sb.length();
            sb.append(cleaned);
            ranges.add(new int[]{start, sb.length()});
            runs.add(new StyledRun(cleaned, style));
            return Optional.<Void>empty();
        }, Style.EMPTY);

        String fullText = sb.toString();
        Matcher matcher = HAS_FOUND_PATTERN.matcher(fullText);
        if (!matcher.find()) return false;

        // Some servers send legacy section-sign color codes embedded directly in the message
        // text instead of real styled components, in which case the Style-based check below
        // never sees a color at all. Check for that first since it's cheap and exact.
        String itemSegment = fullText.substring(matcher.start(2), matcher.end(2));
        if (containsLegacyCode(itemSegment, '5')) return AutoGGState.INSTANCE.triggerEpic;
        if (containsLegacyCode(itemSegment, '6')) return AutoGGState.INSTANCE.triggerLegendary;
        if (containsLegacyCode(itemSegment, 'd')) return AutoGGState.INSTANCE.triggerMythical;

        int itemStart = matcher.start(2);
        Style itemStyle = null;
        for (int i = 0; i < ranges.size(); i++) {
            int[] range = ranges.get(i);
            if (itemStart >= range[0] && itemStart < range[1]) {
                itemStyle = runs.get(i).style;
                break;
            }
        }
        if (itemStyle == null) return false;

        if (isRarity(itemStyle, ChatFormatting.DARK_PURPLE)) return AutoGGState.INSTANCE.triggerEpic;
        if (isRarity(itemStyle, ChatFormatting.GOLD)) return AutoGGState.INSTANCE.triggerLegendary;
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

    private static String normalize(String text) {
        String cleaned = text.replaceAll("[\\p{So}\\p{Co}]", "");
        return cleaned.replaceAll("\\p{Zs}", " ");
    }

    private record StyledRun(String text, Style style) {
    }
}
