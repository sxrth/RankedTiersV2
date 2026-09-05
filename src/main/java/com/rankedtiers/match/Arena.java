package com.rankedtiers.match;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Two spawn points ("position1" / "position2") a kit's matches take place between.
 * Stored in config as: world,x,y,z,yaw,pitch
 */
public class Arena {

    private final Location position1;
    private final Location position2;

    public Arena(Location position1, Location position2) {
        this.position1 = position1;
        this.position2 = position2;
    }

    public Location getPosition1() {
        return position1;
    }

    public Location getPosition2() {
        return position2;
    }

    public boolean isConfigured() {
        return position1 != null && position2 != null;
    }

    public static Arena fromConfigStrings(String pos1, String pos2) {
        return new Arena(parse(pos1), parse(pos2));
    }

    private static Location parse(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String[] parts = raw.split(",");
        if (parts.length < 4) return null;

        World world = Bukkit.getWorld(parts[0].trim());
        if (world == null) return null;

        double x = Double.parseDouble(parts[1].trim());
        double y = Double.parseDouble(parts[2].trim());
        double z = Double.parseDouble(parts[3].trim());
        float yaw = parts.length > 4 ? Float.parseFloat(parts[4].trim()) : 0f;
        float pitch = parts.length > 5 ? Float.parseFloat(parts[5].trim()) : 0f;

        return new Location(world, x, y, z, yaw, pitch);
    }
}
