package me.athlaeos.valhallammo.utility;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

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

    public static double pitchDegrees(double x, double y, double z){
        return Math.toDegrees(pitchRadians(x, y, z));
    }

    public static double pitchDegrees(Vector v){
        return pitchDegrees(v.getX(), v.getY(), v.getZ());
    }

    public static double pitchRadians(Vector v){
        return pitchRadians(v.getX(), v.getY(), v.getZ());
    }

    public static double pitchRadians(double x, double y, double z){
        double xz = sqrt(x * x + z * z);
        return Math.atan2(-y, xz);
    }

    private static final double HAND_ANGLE = Math.PI / 4;
    private static final double HAND_RADIUS = .4;
    public static Vector getHandOffset(Player player, boolean hand) {
        double a = Math.toRadians(player.getEyeLocation().getYaw());
        double p = hand ? 1 : -1;
        double x = HAND_RADIUS * Math.cos(a + Math.PI / 2 + p * HAND_ANGLE);
        double z = HAND_RADIUS * Math.sin(a + Math.PI / 2 + p * HAND_ANGLE);

        return new Vector(x, 0d, z);
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

    public static Collection<Location> getRandomPointsInCircle(Location center, double radius, int amount, boolean includeCenter) {
        World world = center.getWorld();
        Collection<Location> locations = new HashSet<>();
        for(int i = 1;i < amount + 1; i++)
        {
            double theta = Utils.getRandom().nextDouble() * 2 * Math.PI;
            double x = (center.getX() + (radius * Math.cos(theta)));
            double z = (center.getZ() + (radius * Math.sin(theta)));
            locations.add(new Location(world, x, center.getY(), z));
        }
        if (includeCenter) locations.add(center);
        return locations;
    }

    public static Collection<Location> getEvenCircle(Location center, double radius, int amount, double addAngle){
        World world = center.getWorld();
        Collection<Location> locations = new HashSet<>();
        for (double i = 0; i < amount; ++i) {
            double angle = Math.toRadians(((i / amount) * 360d)) + Math.toRadians((addAngle * 360d));
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;

            locations.add(new Location(world, x, center.getY(), z));
        }
        return locations;
    }

    public static void transformExistingPoints(Location center, double yaw, double pitch, double roll, double scale, Collection<Location> points) {
        // Convert to radians
        yaw = Math.toRadians(yaw);
        pitch = Math.toRadians(pitch);
        roll = Math.toRadians(roll);

        // Store the values so we don't have to calculate them again for every single point.
        double cp = cos(pitch);
        double sp = sin(pitch);
        double cy = cos(yaw);
        double sy = sin(yaw);
        double cr = cos(roll);
        double sr = sin(roll);
        transformExistingPointsPredefined(center, cp, sp, cy, sy, cr, sr, 1, points);
    }

    public static void transformExistingPointsPredefined(Location center, double cp, double sp, double cy, double sy, double cr, double sr, double scale, Collection<Location> points) {
        double x, bx, y, by, z, bz;

        for (Location point : points) {
            x = point.getX() - center.getX();
            bx = x;
            y = point.getY() - center.getY();
            by = y;
            z = point.getZ() - center.getZ();
            bz = z;
            x = ((x * cy - bz * sy) * cr + by * sr) * scale;
            y = ((y * cp + bz * sp) * cr - bx * sr) * scale;
            z = ((z * cp - by * sp) * cy + bx * sy) * scale;
            point.setX((center.getX() + x));
            point.setY((center.getY() + y));
            point.setZ((center.getZ() + z));
        }
    }

    private static final Map<Double, Double> cosCache = new HashMap<>();
    public static double cos(double d){
        if (cosCache.containsKey(d)) return cosCache.get(d);
        double result = Math.cos(d);
        cosCache.put(d, result);
        return result;
    }

    private static final Map<Double, Double> sinCache = new HashMap<>();
    public static double sin(double d){
        if (sinCache.containsKey(d)) return sinCache.get(d);
        double result = Math.sin(d);
        sinCache.put(d, result);
        return result;
    }

    private static final Map<Double, Double> tanCache = new HashMap<>();
    public static double tan(double d){
        if (tanCache.containsKey(d)) return tanCache.get(d);
        double result = Math.tan(d);
        tanCache.put(d, result);
        return result;
    }

    private static final Map<Double, Double> radiansCache = new HashMap<>();
    public static double toRadians(double d){
        if (radiansCache.containsKey(d)) return radiansCache.get(d);
        double result = Math.toRadians(d);
        radiansCache.put(d, result);
        return result;
    }

    private static final Map<Double, Double> powCache = new HashMap<>();
    public static double pow(double d, double pow){
        if (powCache.containsKey(d)) return powCache.get(d);
        double result = Math.pow(d, pow);
        powCache.put(d, result);
        return result;
    }

    private static final Map<Double, Double> sqrtCache = new HashMap<>();
    public static double sqrt(double d){
        if (sqrtCache.containsKey(d)) return sqrtCache.get(d);
        double result = Math.sqrt(d);
        sqrtCache.put(d, result);
        return result;
    }

    public static Collection<Location> getCubeWithLines(Location center, int lineDensity, double radius){
        Collection<Location> square = new HashSet<>();

        Location p1 = new Location(center.getWorld(), center.getX()-radius, center.getY()-radius, center.getZ()-radius);
        Location p2 = new Location(center.getWorld(), center.getX()-radius, center.getY()-radius, center.getZ()+radius);
        Location p3 = new Location(center.getWorld(), center.getX()-radius, center.getY()+radius, center.getZ()-radius);
        Location p4 = new Location(center.getWorld(), center.getX()-radius, center.getY()+radius, center.getZ()+radius);
        Location p5 = new Location(center.getWorld(), center.getX()+radius, center.getY()-radius, center.getZ()-radius);
        Location p6 = new Location(center.getWorld(), center.getX()+radius, center.getY()-radius, center.getZ()+radius);
        Location p7 = new Location(center.getWorld(), center.getX()+radius, center.getY()+radius, center.getZ()-radius);
        Location p8 = new Location(center.getWorld(), center.getX()+radius, center.getY()+radius, center.getZ()+radius);

        square.addAll(getPointsInLine(p1, p2, lineDensity));
        square.addAll(getPointsInLine(p1, p3, lineDensity));
        square.addAll(getPointsInLine(p2, p4, lineDensity));
        square.addAll(getPointsInLine(p3, p4, lineDensity));
        square.addAll(getPointsInLine(p5, p6, lineDensity));
        square.addAll(getPointsInLine(p5, p7, lineDensity));
        square.addAll(getPointsInLine(p6, p8, lineDensity));
        square.addAll(getPointsInLine(p7, p8, lineDensity));
        square.addAll(getPointsInLine(p1, p5, lineDensity));
        square.addAll(getPointsInLine(p2, p6, lineDensity));
        square.addAll(getPointsInLine(p3, p7, lineDensity));
        square.addAll(getPointsInLine(p4, p8, lineDensity));

        return square;
    }

    public static List<Location> getPointsInLine(Location point1, Location point2, int amount){
        double xStep = (point1.getX() - point2.getX()) / amount;
        double yStep = (point1.getY() - point2.getY()) / amount;
        double zStep = (point1.getZ() - point2.getZ()) / amount;
        List<Location> points = new ArrayList<>();
        for (int i = 0; i < amount + 1; i++){
            points.add(new Location(
                    point1.getWorld(),
                    point1.getX() - xStep * i,
                    point1.getY() - yStep * i,
                    point1.getZ() - zStep * i));
        }
        return points;
    }

    public static int[][] getOffsetsBetweenPoints(int[] offset1, int[] offset2, int[]... append){
        int xOff = Math.abs(offset1[0] - offset2[0]) + 1;
        int yOff = Math.abs(offset1[1] - offset2[1]) + 1;
        int zOff = Math.abs(offset1[2] - offset2[2]) + 1;
        int arraySize = (Math.abs(xOff) * Math.abs(yOff) * Math.abs(zOff)) + append.length;
        int[][] offsets = new int[arraySize][3];
        int index = 0;
        for (int x = offset1[0]; x <= offset2[0]; x++){
            for (int y = offset1[1]; y <= offset2[1]; y++){
                for (int z = offset1[2]; z <= offset2[2]; z++){
                    offsets[index] = new int[]{x, y, z};
                    index++;
                }
            }
        }
        for (int i = 1; i <= append.length; i++){
            offsets[arraySize - i] = append[i];
        }

        return offsets;
    }
}
