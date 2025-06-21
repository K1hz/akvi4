package minecraft.game.advantage.figures;


import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector2f;

@UtilityClass
public class GCDSensFix {

    public static float getFixedRotation(float rot) {
        return getDeltaMouse(rot) * getGCDValue();
    }

    public float fixFPSrotation(float rot, float oldRot) {
        return correctRotation((float) MathSystem.interpolate(rot, oldRot, (double) 360 / Math.max(Minecraft.getInstance().getDebugFPS(), 5)));
    }

    public Vector2f correctionRotationHandler(float yaw, float pitch) {
        yaw -= (yaw % getGCDValue());
        pitch -= (pitch % getGCDValue());

        return new Vector2f(yaw,pitch);
    }

    public float correctRotation(float rot) {
        float gcd = getGCDValue();
        rot -= rot % gcd;

        return getFixedRotation(rot);
    }

    public static float getGCDValue() {
        double fixedsens = 0.31;
        double gcd = Math.pow(fixedsens * (double) 0.6F + (double) 0.2F, 3.0D) * 8.0D;

        return (float) (gcd * 0.15);
    }

    public static float getDeltaMouse(float delta) {
        return Math.round(delta / getGCDValue());
    }
}
