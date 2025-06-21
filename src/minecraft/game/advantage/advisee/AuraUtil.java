package minecraft.game.advantage.advisee;


import minecraft.game.operation.combat.DDATTACK;
import minecraft.system.AG;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;
import java.util.Optional;

import static net.minecraft.util.math.MathHelper.clamp;

public class AuraUtil implements IMinecraft {

    public static double getDistanceEyePos(Entity target) {
        Vector3d closestHitboxPoint = getClosestVec(target);
        return mc.player.getEyePosition(1.0f).distanceTo(closestHitboxPoint);
    }

    public static Vector3d getClosestVec(Entity entity) {
        Vector3d eyePosVec = mc.player.getEyePosition(1.0F);
        AxisAlignedBB boundingBox = entity.getBoundingBox();
        return new Vector3d(
                clamp(eyePosVec.getX(), boundingBox.minX, boundingBox.maxX),
                clamp(eyePosVec.getY(), boundingBox.minY, boundingBox.maxY),
                clamp(eyePosVec.getZ(), boundingBox.minZ, boundingBox.maxZ)
        );
    }

    public static Vector3d getBestVector(LivingEntity target) {
        double lastDistance = Double.MAX_VALUE;
        Vector3d bestVec = null;
        Vector3d hitVec = target.getPositionVec();
        AxisAlignedBB axisAlignedBB = target.getBoundingBox();


        Vector3d predictedPos = target.getPositionVec().add(target.getMotion().scale(1));
        predictedPos = predictedPos.subtract(0, 0.5 * 0.05 * 1 * 2, 0);


        double yExpand = clamp(mc.player.getPosYEye() - target.getPosYEye(), target.getHeight() / 2, target.getHeight())
                / (mc.player.isElytraFlying() ? 10 : !mc.gameSettings.keyBindJump.isKeyDown() && mc.player.isOnGround() ?
                target.isSneaking() ? 0.8F : 0.6f : 1F);

        double xWidth = (axisAlignedBB.maxX) - (axisAlignedBB.minX);
        double zWidth = (axisAlignedBB.maxZ) - (axisAlignedBB.minZ);


        for (float x = (float) -xWidth; x < xWidth; x += (float) (xWidth / 5f)) {
            for (float z = (float) -zWidth; z < zWidth; z += (float) (zWidth / 5f)) {
                hitVec = new Vector3d(
                        target.getPosX() + xWidth * x,
                        target.getPosY() + yExpand,
                        target.getPosZ() + zWidth * z);

                final double distance = mc.player.getPositionVec().distanceTo(hitVec);

                if (!isHitBoxNotVisible(hitVec) && rayTraceEntities(target, hitVec)  && distance < lastDistance) {
                    bestVec = hitVec;
                    lastDistance = distance;
                }
            }
        }

        DDATTACK kill = AG.getInst().getModuleManager().getDDATTACK();


        Vector3d finalVector = mc.player.getDistance(target) < 1.954f ? target.getPositionVec().add(0, yExpand, 0) : bestVec != null ? bestVec : hitVec;


        Vector3d returnedVector = mc.player.isElytraFlying() ? predictedPos : finalVector;
        return returnedVector.subtract(mc.player.getEyePosition(1)).normalize();
    }

    public static boolean rayTraceEntities(LivingEntity target, Vector3d hitVec) {
        List<Entity> entities = mc.world.getEntitiesInAABBexcluding(mc.player,
                new AxisAlignedBB(
                        Math.min(mc.player.getPosX(), hitVec.x),
                        Math.min(mc.player.getPosY(), hitVec.y),
                        Math.min(mc.player.getPosZ(), hitVec.z),
                        Math.max(mc.player.getPosX(), hitVec.x),
                        Math.max(mc.player.getPosY(), hitVec.y),
                        Math.max(mc.player.getPosZ(), hitVec.z)
                ),
                entity -> entity instanceof LivingEntity && entity != target
        );

        for (Entity entity : entities) {
            Optional<Vector3d> result = entity.getBoundingBox().rayTrace(mc.player.getEyePosition(1F), hitVec);
            if (result.isPresent()) {
                return true;
            }
        }

        return false;
    }

    public static boolean isHitBoxNotVisible(final Vector3d vec3d) {
        final RayTraceContext rayTraceContext = new RayTraceContext(
                mc.player.getEyePosition(1F),
                vec3d,
                RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE,
                mc.player
        );
        final BlockRayTraceResult blockHitResult = mc.world.rayTraceBlocks(rayTraceContext);
        return blockHitResult.getType() == RayTraceResult.Type.MISS;
    }

    public static double getStrictDistance(Entity entity) {
        return getDistanceEyePos(entity);
    }
}
