package minecraft.game.advantage.luvvy;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@UtilityClass
public class PlayerSettingsModule {
	
	Minecraft mc = Minecraft.getInstance();

    private final Pattern NAME_REGEX = Pattern.compile("^[A-zА-я0-9_]{3,16}$");

    public boolean isNameValid(String name) {
        return NAME_REGEX.matcher(name).matches();
    }


    public static String stringEntityBps(Entity entity, boolean timerCheck) {
        return String.format("%.2f", Math.hypot(entity.prevPosX - entity.getPosX(), entity.prevPosZ - entity.getPosZ()) * 20 * (timerCheck ? mc.timer.timerSpeed : 1));
    }

    public static double getEntityBPS(Entity entity, boolean timerCheck) {
        return Math.hypot(entity.prevPosX - entity.getPosX(), entity.prevPosZ - entity.getPosZ()) * 20 * (timerCheck ? mc.timer.timerSpeed : 1);
    }
    public List<BlockPos> getCube(final BlockPos center, final float radiusXZ, final float radiusY) {
        List<BlockPos> positions = new ArrayList<>();
        int centerX = center.getX();
        int centerY = center.getY();
        int centerZ = center.getZ();

        for (int x = centerX - (int) radiusXZ; x <= centerX + radiusXZ; x++) {
            for (int z = centerZ - (int) radiusXZ; z <= centerZ + radiusXZ; z++) {
                for (int y = centerY - (int) radiusY; y < centerY + radiusY; y++) {
                    positions.add(new BlockPos(x, y, z));
                }
            }
        }

        return positions;
    }
    public static float calculateCorrectYawOffset(float yaw) {

        double xDiff = mc.player.getPosX() - mc.player.prevPosX;
        double zDiff = mc.player.getPosZ() - mc.player.prevPosZ;
        float distSquared = (float) (xDiff * xDiff + zDiff * zDiff);
        float renderYawOffset = mc.player.prevRenderYawOffset;
        float offset = renderYawOffset;
        float yawOffsetDiff;

        if (distSquared > 0.0) {
            offset = (float) MathHelper.atan2(zDiff, xDiff) * 180.0f / (float) Math.PI - 90.0f;
        }

        if (mc.player != null && mc.player.swingProgress > 0.0f) {
            offset = yaw;
        }


        yawOffsetDiff = MathHelper.wrapDegrees(yaw - (renderYawOffset + MathHelper.wrapDegrees(offset - renderYawOffset) * 0.3f));
        yawOffsetDiff = MathHelper.clamp(yawOffsetDiff, -45, 45);

        renderYawOffset = yaw - yawOffsetDiff;
        if (yawOffsetDiff * yawOffsetDiff > 2500.0f) {
            renderYawOffset += yawOffsetDiff * 0.3f;
        }

        return renderYawOffset;
    }
}
