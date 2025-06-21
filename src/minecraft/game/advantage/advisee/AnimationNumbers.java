package minecraft.game.advantage.advisee;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class AnimationNumbers {
    public float displayedHealth = 0;
    public float previousHealth = 0;
    public float displayedBPS = 0;
    public float previousBPS = 0;


    public String FPSAnim() {
        int getFPS = Minecraft.getInstance().getDebugFPS();

        float animationSpeed = 0.025F;
        displayedHealth = MathHelper.lerp(animationSpeed, displayedHealth, getFPS);
        previousHealth = MathHelper.lerp(animationSpeed, previousHealth, displayedHealth);

        return String.valueOf((int) displayedHealth);
    }


    public int NumberAnim(float number) {
        float animationSpeed = 0.025F;
        displayedHealth = MathHelper.lerp(animationSpeed, displayedHealth, number);
        previousHealth = MathHelper.lerp(animationSpeed, previousHealth, displayedHealth);

        return (int) displayedHealth;
    }


    public String BPSAnim(Entity entity, boolean timerCheck) {

        double bps = getEntityBPS(entity, timerCheck);

        float animationSpeed = 0.025F;
        displayedBPS = MathHelper.lerp(animationSpeed, displayedBPS, (float) bps);
        previousBPS = MathHelper.lerp(animationSpeed, previousBPS, displayedBPS);

        return String.format("%.1f", displayedBPS * 40);
    }


    public static double getEntityBPS(Entity entity, boolean timerCheck) {
        return Math.hypot(entity.prevPosX - entity.getPosX(), entity.prevPosZ - entity.getPosZ()) * 20 * (timerCheck ? Minecraft.getInstance().timer.timerSpeed : 1);
    }
}
