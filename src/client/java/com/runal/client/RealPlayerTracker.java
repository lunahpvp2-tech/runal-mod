package com.runal.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// This server spawns NPCs (via Skript) as fake Player-type entities with a real tab-list entry
// and a random UUID, so neither "is it a Player instance", "does it have a tab-list entry", nor
// "is its UUID version 4" can tell a real player apart from one of these NPCs. What a script can't
// easily fake is the vanilla keep-alive protocol: a real client's tab-list ping is recalculated by
// the server periodically and will change over time, while a scripted NPC's tab entry is written
// once and left alone. Treat a UUID as belonging to a genuine player only once we've observed its
// ping value actually change.
public class RealPlayerTracker {
    private static final Map<UUID, Integer> lastPing = new HashMap<>();
    private static final Set<UUID> verified = new HashSet<>();

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getConnection() == null) return;
            for (PlayerInfo info : client.getConnection().getListedOnlinePlayers()) {
                UUID id = info.getProfile().id();
                int ping = info.getLatency();
                Integer previous = lastPing.put(id, ping);
                if (previous != null && previous != ping) {
                    verified.add(id);
                }
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            lastPing.clear();
            verified.clear();
        });
    }

    public static boolean isVerifiedRealPlayer(UUID uuid) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && uuid.equals(mc.player.getUUID())) return true;
        return verified.contains(uuid);
    }
}
