package minecraft.game.advantage.advisee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import minecraft.game.advantage.luvvy.rotation.FreeLookHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2f;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rotation implements IMinecraft {
    private float yaw, pitch;

    public Rotation(Entity entity) {
        yaw = entity.rotationYaw;
        pitch = entity.rotationPitch;
    }

    public float getDelta(Rotation target) {
        float yawDelta = MathHelper.wrapDegrees(target.getYaw() - this.yaw);
        float pitchDelta = target.getPitch() - this.pitch;
        return (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
    }

    public double getDeltaDouble(Rotation target) {
        double yawDelta = MathHelper.wrapDegrees(target.getYaw() - yaw);
        double pitchDelta = MathHelper.wrapDegrees(target.getPitch() - pitch);
        return Math.hypot(yawDelta, pitchDelta);
    }

    public static Vector2f camera() {
        return new Vector2f(cameraYaw(), cameraPitch());
    }

    public static float cameraYaw() {
        return MathHelper.wrapDegrees(mc.gameRenderer.getActiveRenderInfo().getYaw() + (mc.gameRenderer.getActiveRenderInfo().isThirdPersonReverse() ? 180 : 0));
    }
    public static Rotation getReal() {
        return new Rotation(FreeLookHandler.getFreeYaw(), FreeLookHandler.getFreePitch());
    }
    public static float cameraPitch() {
        return (mc.gameRenderer.getActiveRenderInfo().isThirdPersonReverse() ? -1 : 1) * mc.gameRenderer.getActiveRenderInfo().getPitch();
    }
}
