package minecraft.game.advantage.advisee;

import com.mojang.blaze3d.platform.GlStateManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;

import static minecraft.game.advantage.advisee.IMinecraft.mc;

@UtilityClass
public class AnimationMath {


    public static double deltaTime() {
        return mc.debugFPS > 0 ? Math.min(1.0 / mc.debugFPS, 1) : 1;
    }

    public float animationFPS(float animation, float target, float speedTarget) {
        float dif = (target - animation) / Math.max((float) mc.getDebugFPS(), 5) * 15;

        if (dif > 0) {
            dif = Math.max(speedTarget, dif);
            dif = Math.min(target - animation, dif);
        } else if (dif < 0) {
            dif = Math.min(-speedTarget, dif);
            dif = Math.max(target - animation, dif);
        }
        return animation + dif;
    }


    private static float alpha = 0.0f;
    private static final float speed = 8f; // Скорость изменения альфа-канала

    // Метод для плавного увеличения альфа-канала при открытии
    public static float fadeInAnimation() {
        if (alpha < 1.0f) {
            alpha += speed;
        }
        return Math.min(alpha, 1.0f); // Ограничение альфа-канала до 1.0
    }

    // Метод для плавного уменьшения альфа-канала при закрытии
    public static float fadeOutAnimation() {
        if (alpha > 0.0f) {
            alpha -= speed;
        }
        return Math.max(alpha, 0.0f); // Ограничение альфа-канала до 0.0
    }

    public static double easeInQuad(double t, double b, double c, double d) {
        t /= d;
        return c * t * t + b;
    }

    public static double easeOutQuad(double t, double b, double c, double d) {
        t /= d;
        return -c * t * (t - 2) + b;
    }

    public static void sizeAnimation(double width, double height, double scale) {
        GlStateManager.translated(width, height, 0);
        GlStateManager.scaled(scale, scale, scale);
        GlStateManager.translated(-width, -height, 0);
    }

    public static double easeInOutQuad(double t, double b, double c, double d) {
        t /= d / 2;
        if (t < 1) return c / 2 * t * t + b;
        t--;
        return -c / 2 * (t * (t - 2) - 1) + b;
    }

    public static double easeInCubic(double t, double b, double c, double d) {
        t /= d;
        return c * t * t * t + b;
    }

    public static double easeOutCubic(double t, double b, double c, double d) {
        t /= d;
        t--;
        return c * (t * t * t + 1) + b;
    }

    public static double easeInOutCubic(double t, double b, double c, double d) {
        t /= d / 2;
        if (t < 1) return c / 2 * t * t * t + b;
        t -= 2;
        return c / 2 * (t * t * t + 2) + b;
    }

    public static double easeInQuart(double t, double b, double c, double d) {
        t /= d;
        return c * t * t * t * t + b;
    }

    public static double easeOutQuart(double t, double b, double c, double d) {
        t /= d;
        t--;
        return -c * (t * t * t * t - 1) + b;
    }

    public static double easeInOutQuart(double t, double b, double c, double d) {
        t /= d / 2;
        if (t < 1) return c / 2 * t * t * t * t + b;
        t -= 2;
        return -c / 2 * (t * t * t * t - 2) + b;
    }

    public static double easeInQuint(double t, double b, double c, double d) {
        t /= d;
        return c * t * t * t * t * t + b;
    }

    public static double easeOutQuint(double t, double b, double c, double d) {
        t /= d;
        t--;
        return c * (t * t * t * t * t + 1) + b;
    }

    public static double easeInOutQuint(double t, double b, double c, double d) {
        t /= d / 2;
        if (t < 1) return c / 2 * t * t * t * t * t + b;
        t -= 2;
        return c / 2 * (t * t * t * t * t + 2) + b;
    }

    public static double easeInSine(double t, double b, double c, double d) {
        return -c * Math.cos(t / d * (Math.PI / 2)) + c + b;
    }

    public static double easeOutSine(double t, double b, double c, double d) {
        return c * Math.sin(t / d * (Math.PI / 2)) + b;
    }

    public static double easeInOutSine(double t, double b, double c, double d) {
        return -c / 2 * (Math.cos(Math.PI * t / d) - 1) + b;
    }

    public static double easeInExpo(double t, double b, double c, double d) {
        return c * Math.pow(2, 10 * (t / d - 1)) + b;
    }

    public static double easeOutExpo(double t, double b, double c, double d) {
        return c * (-Math.pow(2, -10 * t / d) + 1) + b;
    }

    public static double easeInOutExpo(double t, double b, double c, double d) {
        t /= d / 2;
        if (t < 1) return c / 2 * Math.pow(2, 10 * (t - 1)) + b;
        t--;
        return c / 2 * (-Math.pow(2, -10 * t) + 2) + b;
    }

    public static double easeOutBounce(double t, double b, double c, double d) {
        if ((t /= d) < (1 / 2.75)) {
            return c * (7.5625 * t * t) + b;
        } else if (t < (2 / 2.75)) {
            return c * (7.5625 * (t -= (1.5 / 2.75)) * t + .75) + b;
        } else if (t < (2.5 / 2.75)) {
            return c * (7.5625 * (t -= (2.25 / 2.75)) * t + .9375) + b;
        } else {
            return c * (7.5625 * (t -= (2.625 / 2.75)) * t + .984375) + b;
        }
    }

    public static double easeInBounce(double t, double b, double c, double d) {
        return c - easeOutBounce(d - t, 0, c, d) + b;
    }

    public static double easeInOutBounce(double t, double b, double c, double d) {
        if (t < d / 2) return easeInBounce(t * 2, 0, c, d) * .5 + b;
        else return easeOutBounce(t * 2 - d, 0, c, d) * .5 + c * .5 + b;
    }

    public static double easeInBack(double t, double b, double c, double d) {
        double s = 1.70158;
        return c * (t /= d) * t * ((s + 1) * t - s) + b;
    }

    public static double easeOutBack(double t, double b, double c, double d) {
        double s = 1.70158;
        return c * ((t = t / d - 1) * t * ((s + 1) * t + s) + 1) + b;
    }

    public static double easeInOutBack(double t, double b, double c, double d) {
        double s = 1.70158;
        if ((t /= d / 2) < 1) return c / 2 * (t * t * (((s *= (1.525)) + 1) * t - s)) + b;
        return c / 2 * ((t -= 2) * t * (((s *= (1.525)) + 1) * t + s) + 2) + b;
    }

    public static double easeInElastic(double t, double b, double c, double d) {
        double s = 1.70158;
        double p = 0;
        double a = c;
        if (t == 0) return b;
        if ((t /= d) == 1) return b + c;
        if (p == 0) p = d * .3;
        if (a < Math.abs(c)) {
            a = c;
            s = p / 4;
        } else {
            s = p / (2 * Math.PI) * Math.asin(c / a);
        }
        return -(a * Math.pow(2, 10 * (t -= 1)) * Math.sin((t * d - s) * (2 * Math.PI) / p)) + b;
    }

    public static double easeOutElastic(double t, double b, double c, double d) {
        double s = 1.70158;
        double p = 0;
        double a = c;
        if (t == 0) return b;
        if ((t /= d) == 1) return b + c;
        if (p == 0) p = d * .3;
        if (a < Math.abs(c)) {
            a = c;
            s = p / 4;
        } else {
            s = p / (2 * Math.PI) * Math.asin(c / a);
        }
        return a * Math.pow(2, -10 * t) * Math.sin((t * d - s) * (2 * Math.PI) / p) + c + b;
    }

    public static double easeInOutElastic(double t, double b, double c, double d) {
        double s = 1.70158;
        double p = 0;
        double a = c;
        if (t == 0) return b;
        if ((t /= d / 2) == 2) return b + c;
        if (p == 0) p = d * (.3 * 1.5);
        if (a < Math.abs(c)) {
            a = c;
            s = p / 4;
        } else {
            s = p / (2 * Math.PI) * Math.asin(c / a);
        }
        if (t < 1) return -.5 * (a * Math.pow(2, 10 * (t -= 1)) * Math.sin((t * d - s) * (2 * Math.PI) / p)) + b;
        return a * Math.pow(2, -10 * (t -= 1)) * Math.sin((t * d - s) * (2 * Math.PI) / p) * .5 + c + b;
    }

    public static double lerp(double start, double end, double speed) {
        double factor = MathHelper.clamp(speed * deltaTime(), 0, 1);
        return start + (end - start) * factor;
    }

    public static float animation(float animation, float target, float speedTarget) {
        float dif = MathHelper.clamp((target - animation) * 0.1f, -speedTarget, speedTarget);
        return animation + dif;
    }

    public static float calculateCompensation(float target, float current, float delta, double speed) {
        delta = MathHelper.clamp(delta, 1.0f, 16.0f);
        double step = speed * delta / 16.0;
        return (float) (Math.abs(current - target) < step ? target : current + Math.signum(target - current) * step);
    }
}
