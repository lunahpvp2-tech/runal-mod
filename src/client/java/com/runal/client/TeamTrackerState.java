package com.runal.client;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TeamTrackerState {
    public static final TeamTrackerState INSTANCE = new TeamTrackerState();

    private static final String FILE_NAME = "runal_teammates.txt";

    private boolean enabled = false;
    public boolean glowEnabled = true;
    public int markerColor = 0xFF35D77A;

    private final Set<UUID> teammates = new HashSet<>();

    private TeamTrackerState() {}

    public boolean isEnabled() { return enabled; }
    public void toggle() { enabled = !enabled; }

    public boolean isTeammate(UUID id) { return teammates.contains(id); }

    public void setTeammate(UUID id, boolean value) {
        if (value) teammates.add(id); else teammates.remove(id);
        save();
    }

    public Set<UUID> getTeammates() { return Collections.unmodifiableSet(teammates); }

    private Path getPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    public void save() {
        StringBuilder sb = new StringBuilder();
        for (UUID id : teammates) sb.append(id).append('\n');
        try {
            Files.writeString(getPath(), sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        Path path = getPath();
        if (!Files.exists(path)) return;
        try {
            teammates.clear();
            for (String line : Files.readAllLines(path)) {
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    teammates.add(UUID.fromString(line));
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
