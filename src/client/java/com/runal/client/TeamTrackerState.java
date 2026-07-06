package com.runal.client;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TeamTrackerState {
    public static final TeamTrackerState INSTANCE = new TeamTrackerState();

    private static final String FILE_NAME = "runal_teammates.txt";

    private boolean enabled = false;
    public boolean glowEnabled = true;
    public int markerColor = 0xFF35D77A;

    private final Map<UUID, Integer> teammates = new LinkedHashMap<>();

    private TeamTrackerState() {}

    public boolean isEnabled() { return enabled; }
    public void toggle() { enabled = !enabled; }

    public boolean isTeammate(UUID id) { return teammates.containsKey(id); }

    public void setTeammate(UUID id, boolean value) {
        if (value) teammates.put(id, markerColor); else teammates.remove(id);
        save();
    }

    public int getTeammateColor(UUID id) {
        return teammates.getOrDefault(id, markerColor);
    }

    public void setTeammateColor(UUID id, int color) {
        if (!teammates.containsKey(id)) return;
        teammates.put(id, color);
        save();
    }

    public Set<UUID> getTeammates() { return Collections.unmodifiableSet(teammates.keySet()); }

    private Path getPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    public void save() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<UUID, Integer> entry : teammates.entrySet()) {
            sb.append(entry.getKey()).append(':').append(entry.getValue()).append('\n');
        }
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

                int separator = line.indexOf(':');
                String idPart = separator < 0 ? line : line.substring(0, separator);
                try {
                    UUID id = UUID.fromString(idPart);
                    int color = markerColor;
                    if (separator >= 0) {
                        try {
                            color = Integer.parseInt(line.substring(separator + 1));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    teammates.put(id, color);
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
