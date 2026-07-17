package com.runal.client;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartyController {
    private static final Pattern JOINED_PATTERN = Pattern.compile("^(.+?) has joined the party\\.$");
    private static final Pattern LEFT_PATTERN = Pattern.compile("^(.+?) has left your party!$");
    private static final Pattern KICKED_PATTERN = Pattern.compile("^Kicked (.+?) from the party!$");
    private static final Pattern DISBANDED_PATTERN = Pattern.compile("^The party you were in has disbanded!$");

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            handleMessage(message.getString().trim());
        });
    }

    private static void handleMessage(String text) {
        Matcher matcher;

        if ((matcher = JOINED_PATTERN.matcher(text)).matches()) {
            addByName(matcher.group(1).trim());
            return;
        }

        if ((matcher = LEFT_PATTERN.matcher(text)).matches()) {
            removeByName(matcher.group(1).trim());
            return;
        }

        if ((matcher = KICKED_PATTERN.matcher(text)).matches()) {
            removeByName(matcher.group(1).trim());
            return;
        }

        if (DISBANDED_PATTERN.matcher(text).matches()) {
            clearParty();
        }
    }

    private static void addByName(String name) {
        UUID id = resolveUuid(name);
        if (id == null) return;
        PartyState.INSTANCE.add(id);
        TeamTrackerState.INSTANCE.setTeammate(id, true);
    }

    private static void removeByName(String name) {
        UUID id = resolveUuid(name);
        if (id == null) return;
        PartyState.INSTANCE.remove(id);
        TeamTrackerState.INSTANCE.setTeammate(id, false);
    }

    private static void clearParty() {
        for (UUID id : PartyState.INSTANCE.getMembers()) {
            TeamTrackerState.INSTANCE.setTeammate(id, false);
        }
        PartyState.INSTANCE.clear();
    }

    private static UUID resolveUuid(String name) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) return null;
        for (PlayerInfo info : mc.getConnection().getListedOnlinePlayers()) {
            if (info.getProfile().name().equalsIgnoreCase(name)) return info.getProfile().id();
        }
        return null;
    }
}
