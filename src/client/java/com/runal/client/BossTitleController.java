package com.runal.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Shows a boss's chat line (e.g. "Shaman Kerax [400] : Flame Prison!") as a big title on
// screen, reading just the part after " : ". Only server-sent game messages trigger this
// (ClientReceiveMessageEvents.GAME, same as EventTrackerController) so a player can't fake one by
// typing a boss's name in chat.
public class BossTitleController {
    private static final String[] BOSS_NAMES = {
            "Shaman Kerax",
            "Beast of Winds",
            "Shadow Assassin Nyx",
            "Great Paladin",
            "The Pharaoh",
            "The Shadow",
            "The Nightmarrow",
            "Ashen Beast",
            "Maelstrom",
            "Spider Queen",
            "Flame Captain Aron",
            "Plebelin",
            "Mushroom Amalgamation",
            "Natuir",
            "Angel",
            "Flame Lord",
            "Harbringer of Storms",
            "Ruinwing",
    };

    private static final int MAX_WORDS = 5;

    private static final int DISPLAY_TICKS = 4 * 20;

    private static final Pattern BOSS_LINE_PATTERN = buildPattern();

    private static Pattern buildPattern() {
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < BOSS_NAMES.length; i++) {
            if (i > 0) names.append('|');
            names.append(Pattern.quote(BOSS_NAMES[i]));
        }
        // HP shown in the brackets changes as the boss takes damage, so match any bracket
        // content rather than the specific values above. The server also prefixes these lines
        // with tags like "[A] " before the boss name, so allow any number of those too. Spacing
        // around the "] :" separator isn't consistent between bosses (some send "[750]:" with no
        // space), so treat all whitespace there as optional/variable rather than a fixed literal.
        return Pattern.compile("^(?:\\[[^\\]]*]\\s*)*(?:" + names + ")\\s*\\[[^\\]]*]\\s*:\\s*(.+)$");
    }

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            handleMessage(message.getString().trim());
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (BossTitleState.displayTicksRemaining > 0) {
                BossTitleState.displayTicksRemaining--;
                if (BossTitleState.displayTicksRemaining == 0) {
                    BossTitleState.currentText = null;
                }
            }
        });
    }

    private static void handleMessage(String text) {
        if (text.isEmpty()) return;

        Matcher matcher = BOSS_LINE_PATTERN.matcher(text);
        if (!matcher.matches()) return;

        BossTitleState.currentText = firstWords(matcher.group(1), MAX_WORDS);
        BossTitleState.displayTicksRemaining = DISPLAY_TICKS;
    }

    private static String firstWords(String text, int maxWords) {
        String[] words = text.trim().split("\\s+");
        if (words.length <= maxWords) return text;
        return String.join(" ", java.util.Arrays.copyOf(words, maxWords));
    }
}
