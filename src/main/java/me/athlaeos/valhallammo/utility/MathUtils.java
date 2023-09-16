package me.athlaeos.valhallammo.utility;

import org.bukkit.Location;

import java.util.Collection;
import java.util.HashSet;

public class MathUtils {
    public static Collection<Location> getRandomPointsInArea(Location center, double radius, int count){
        Collection<Location> locations = new HashSet<>();
        double minX = center.getX() - radius;
        double minY = center.getY() - radius;
        double minZ = center.getZ() - radius;
        double maxX = center.getX() + radius;
        double maxY = center.getY() + radius;
        double maxZ = center.getZ() + radius;
        for (int i = 0; i < count; i++){
            double randomX = minX + (maxX - minX) * Utils.getRandom().nextDouble();
            double randomY = minY + (maxY - minY) * Utils.getRandom().nextDouble();
            double randomZ = minZ + (maxZ - minZ) * Utils.getRandom().nextDouble();
            locations.add(new Location(center.getWorld(), randomX, randomY, randomZ));
        }
        return locations;
    }
    public static Collection<Location> getRandomPointsInPlane(Location center, double radius, int count){
        Collection<Location> locations = new HashSet<>();
        double minX = center.getX() - radius;
        double minZ = center.getZ() - radius;
        double maxX = center.getX() + radius;
        double maxZ = center.getZ() + radius;
        for (int i = 0; i < count; i++){
            double randomX = minX + (maxX - minX) * Utils.getRandom().nextDouble();
            double randomZ = minZ + (maxZ - minZ) * Utils.getRandom().nextDouble();
            locations.add(new Location(center.getWorld(), randomX, center.getY(), randomZ));
        }
        return locations;
    }
}
