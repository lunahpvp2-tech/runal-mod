package com.runal.client;

import java.util.UUID;

public class Waypoint {
    public static final String OVERWORLD = "overworld";
    public static final String NETHER = "the_nether";
    public static final String END = "the_end";

    public final UUID id;
    public String name;
    public int x, y, z;
    public int r = 53, g = 123, b = 228;
    public boolean enabled = true;
    public boolean overworld = true;
    public boolean nether = false;
    public boolean end = false;

    public Waypoint(UUID id, String name, int x, int y, int z) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int color() {
        return 0xFF000000 | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    public boolean isEnabledInDimension(String dimensionKey) {
        return switch (dimensionKey) {
            case OVERWORLD -> overworld;
            case NETHER -> nether;
            case END -> end;
            default -> false;
        };
    }

    public void setDimensionEnabled(String dimensionKey, boolean value) {
        switch (dimensionKey) {
            case OVERWORLD -> overworld = value;
            case NETHER -> nether = value;
            case END -> end = value;
        }
    }

    public String serialize() {
        String safeName = name.replace('\t', ' ').replace('\n', ' ');
        return String.join("\t",
                id.toString(), safeName,
                String.valueOf(x), String.valueOf(y), String.valueOf(z),
                String.valueOf(r), String.valueOf(g), String.valueOf(b),
                String.valueOf(enabled), String.valueOf(overworld), String.valueOf(nether), String.valueOf(end));
    }

    public static Waypoint deserialize(String line) {
        try {
            String[] parts = line.split("\t", -1);
            if (parts.length < 12) return null;
            Waypoint waypoint = new Waypoint(UUID.fromString(parts[0]), parts[1],
                    Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
            waypoint.r = Integer.parseInt(parts[5]);
            waypoint.g = Integer.parseInt(parts[6]);
            waypoint.b = Integer.parseInt(parts[7]);
            waypoint.enabled = Boolean.parseBoolean(parts[8]);
            waypoint.overworld = Boolean.parseBoolean(parts[9]);
            waypoint.nether = Boolean.parseBoolean(parts[10]);
            waypoint.end = Boolean.parseBoolean(parts[11]);
            return waypoint;
        } catch (Exception e) {
            return null;
        }
    }
}
