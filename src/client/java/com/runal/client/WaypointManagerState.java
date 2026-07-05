package com.runal.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WaypointManagerState {
    public static final WaypointManagerState INSTANCE = new WaypointManagerState();

    private static final String FILE_NAME = "runal_waypoints.txt";

    private final List<Waypoint> waypoints = new ArrayList<>();
    public boolean showBeams = true;
    private boolean enabled = true;

    private WaypointManagerState() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public Waypoint create(String name, int x, int y, int z, String dimensionKey) {
        Waypoint waypoint = new Waypoint(UUID.randomUUID(), name, x, y, z);
        waypoint.overworld = false;
        waypoint.setDimensionEnabled(dimensionKey, true);
        waypoints.add(waypoint);
        save();
        return waypoint;
    }

    public void remove(Waypoint waypoint) {
        waypoints.remove(waypoint);
        save();
    }

    public static String currentDimensionKey(Minecraft mc) {
        if (mc.level == null) return Waypoint.OVERWORLD;
        var dimension = mc.level.dimension();
        if (dimension.equals(Level.NETHER)) return Waypoint.NETHER;
        if (dimension.equals(Level.END)) return Waypoint.END;
        return Waypoint.OVERWORLD;
    }

    private Path getPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    public void save() {
        StringBuilder sb = new StringBuilder();
        for (Waypoint waypoint : waypoints) sb.append(waypoint.serialize()).append('\n');
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
            waypoints.clear();
            for (String line : Files.readAllLines(path)) {
                line = line.trim();
                if (line.isEmpty()) continue;
                Waypoint waypoint = Waypoint.deserialize(line);
                if (waypoint != null) waypoints.add(waypoint);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
