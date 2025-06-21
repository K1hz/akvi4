package minecraft.game.advantage.luvvy.rotation;

import minecraft.game.advantage.advisee.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Random;

public class MultiPoints implements IMinecraft {
    private static final Random random = new Random();
    private static Vector3d lastTargetPoint = null;
    private static final double SMOOTH_FACTOR = 0.3;
    private static long lastUpdateTime = 0;
    private static Vector3d cachedVector = null;
    private static final long UPDATE_DELAY_MS = 500;

    public static Vector3d calculateVector(Entity entity, boolean useBoundingBox) {
        long currentTime = System.currentTimeMillis();


        if (cachedVector != null && (currentTime - lastUpdateTime) < UPDATE_DELAY_MS) {

        }

        double xOffset = random.nextDouble();
        double yOffset = random.nextDouble();
        double zOffset = random.nextDouble();

        double x = entity.getBoundingBox().minX + (entity.getBoundingBox().maxX - entity.getBoundingBox().minX) * xOffset;
        double y = entity.getBoundingBox().minY + (entity.getBoundingBox().maxY - entity.getBoundingBox().minY) * yOffset ;
        double z = entity.getBoundingBox().minZ + (entity.getBoundingBox().maxZ - entity.getBoundingBox().minZ) * zOffset;

        Vector3d targetPoint = new Vector3d(x, y, z);


        if (lastTargetPoint != null) {
            targetPoint = new Vector3d(
                    lerp(lastTargetPoint.x, targetPoint.x, SMOOTH_FACTOR),
                    lerp(lastTargetPoint.y, targetPoint.y, SMOOTH_FACTOR),
                    lerp(lastTargetPoint.z, targetPoint.z, SMOOTH_FACTOR)
            );
        }

        lastTargetPoint = targetPoint;
        lastUpdateTime = currentTime;

        cachedVector = new Vector3d(
                targetPoint.x - mc.player.getPosX(),
                targetPoint.y - (mc.player.getPosY() + mc.player.getEyeHeight()),
                targetPoint.z - mc.player.getPosZ()
        );

        return cachedVector;
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
