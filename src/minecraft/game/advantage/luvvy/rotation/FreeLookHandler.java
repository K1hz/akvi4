package minecraft.game.advantage.luvvy.rotation;

import com.google.common.eventbus.Subscribe;
import minecraft.system.AG;
import minecraft.game.transactions.EventCamera;
import minecraft.game.transactions.EventRotate;
import minecraft.game.advantage.advisee.IMinecraft;
import lombok.Getter;
import net.minecraft.util.math.MathHelper;

public class FreeLookHandler implements IMinecraft {
    public FreeLookHandler() {
        AG.getInst().getEventBus().register(this);
    }

    @Getter
    private static boolean active;
    @Getter
    private static float freeYaw, freePitch;

    @Subscribe
    public void onLook(EventRotate event) {
        if (active) {
            rotateTowards(event.getYaw(), event.getPitch());
            event.cancel();
        }
    }

    @Subscribe
    public void onCamera(EventCamera event) {
        if (active) {
            event.yaw = freeYaw;
            event.pitch = freePitch;
        } else {
            freeYaw = event.yaw;
            freePitch = event.pitch;
        }
    }

    public static void setActive(boolean state) {
        if (active != state) {
            active = state;
            resetRotation();
        }
    }

    private void rotateTowards(double yaw, double pitch) {
        double d0 = pitch * 0.15D;
        double d1 = yaw * 0.15D;
        freePitch = (float) ((double) freePitch + d0);
        freeYaw = (float) ((double) freeYaw + d1);
        freePitch = MathHelper.clamp(freePitch, -90.0F, 90.0F);
    }

    private static void resetRotation() {
        mc.player.rotationYaw = freeYaw;
        mc.player.rotationPitch = freePitch;
    }
}
