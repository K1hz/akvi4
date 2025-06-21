package minecraft.game.advantage.make.color;

import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.operation.visual.Hud;
import minecraft.system.AG;
import lombok.experimental.UtilityClass;
import net.minecraft.util.math.MathHelper;

import com.mojang.blaze3d.systems.RenderSystem;

import minecraft.game.advantage.figures.MathSystem;
import minecraft.system.styles.StyleManager;

import java.awt.Color;

@UtilityClass
public class ColoringSystem {

    public final int green = new Color(64, 255, 64).getRGB();
    public final int yellow = new Color(255, 255, 64).getRGB();
    public final int orange = new Color(255, 128, 32).getRGB();
    public final int red = new Color(255, 64, 64).getRGB();

    public static int rgb(int r, int g, int b) {
        return 255 << 24 | r << 16 | g << 8 | b;
    }

    public static int rgb1(int r, int g, int b) {
        return 255 << 24 | r << 16 | g << 8 | b;
    }
    
    public int multDark(int c, float brpc) {
        return getColor((float) red(c) * brpc, (float) green(c) * brpc, (float) blue(c) * brpc, (float) alpha(c));
    }
    public float[] getRGBf(int c) {
        return new float[]{redf(c), greenf(c), bluef(c)};
    }
    public static int replAlpha(int n, int n2) {
        return ColoringSystem.getColor(ColoringSystem.red(n), ColoringSystem.green(n), ColoringSystem.blue(n), n2);
    }
    public static float[] getRGBAf(int c) {
        return new float[]{(float) red(c) / 255.F, (float) green(c) / 255.F, (float) blue(c) / 255.F, (float) alpha(c) / 255.F};
    }
    // old
    public static int getColorOLD(int index) {
        StyleManager styleManager = AG.getInst().getStyleManager();
        return ColoringSystem.gradient(styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB(), index * 16, 10);
    }

    public static int getColorOLD(int index, float mult) {
        StyleManager styleManager = AG.getInst().getStyleManager();
        return ColoringSystem.gradient(styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB(), (int) (index * mult), 10);
    }

    public static int getColorOLD(int firstColor, int secondColor, int index, float mult) {
        return ColoringSystem.gradient(firstColor, secondColor, (int) (index * mult), 10);
    }
    //new
    public static int getColor(int index) {
        StyleManager styleManager = AG.getInst().getStyleManager();
        return ColoringSystem.gradient(styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB(), index * 32, 2);
    }
    public static int getColorSLOW(int index) {
        StyleManager styleManager = AG.getInst().getStyleManager();
        return ColoringSystem.gradient(styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB(), index * 2, 999999999);
    }
    public int getColor123(int index) {
        StyleManager styleManager = AG.getInst().getStyleManager();
        return ColoringSystem.gradient(4, index, ColoringSystem.gradient(styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB()));
    }
    public static int getColorTest1(int index) {
        StyleManager styleManager = AG.getInst().getStyleManager();
        return ColoringSystem.gradient(styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB(), index * 44, 5);
    }
    public static int getColorTest2(int index) {
        StyleManager styleManager = AG.getInst().getStyleManager();
        return ColoringSystem.gradient(styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB(), index * 230, 6);
    }
    public static int getColorBlack(int index) {
        StyleManager styleManager = AG.getInst().getStyleManager();
        int themeColor = Hud.getColor(1);

        int red = (themeColor >> 16) & 0xFF;
        int green = (themeColor >> 8) & 0xFF;
        int blue = themeColor & 0xFF;


        int darkerRed = (int) (red * 0.5);
        int darkerGreen = (int) (green * 0.5);
        int darkerBlue = (int) (blue * 0.5);

        int darkerColor = (darkerRed << 16) | (darkerGreen << 8) | darkerBlue;

        return ColoringSystem.gradient(darkerColor, themeColor, index, 5);
    }
    public static int getColorTest3(int index) {
        StyleManager styleManager = AG.getInst().getStyleManager();
        return ColoringSystem.gradient(styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB(), index * 2, 6);
    }
    public static int getColorTest4(int index) {
        StyleManager styleManager = AG.getInst().getStyleManager();
        return ColoringSystem.gradient(styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB(), index * 2, 6);
    }

    public static int getColor(int index, float mult) {
        StyleManager styleManager = AG.getInst().getStyleManager();
        return ColoringSystem.gradient(styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB(), (int) (index * mult), 10);
    }

    public static int getColor(int firstColor, int secondColor, int index, float mult) {
        return ColoringSystem.gradient(firstColor, secondColor, (int) (index * mult), 10);
    }

    public static Vector4i pizdec() {
        return new Vector4i(ColoringSystem.setAlpha(ColoringSystem.getColor(1), 215), ColoringSystem.setAlpha(ColoringSystem.getColor(90), 215), ColoringSystem.setAlpha(ColoringSystem.getColor(180), 215), ColoringSystem.setAlpha(ColoringSystem.getColor(360), 215));
    }

    public static class IntColor {

        public static float[] rgb(final int color) {
            return new float[]{
                    (color >> 16 & 0xFF) / 255f,
                    (color >> 8 & 0xFF) / 255f,
                    (color & 0xFF) / 255f,
                    (color >> 24 & 0xFF) / 255f
            };
        }

        public static int rgba(final int r,
                               final int g,
                               final int b,
                               final int a) {
            return a << 24 | r << 16 | g << 8 | b;
        }

        public static int rgb(int r, int g, int b) {
            return 255 << 24 | r << 16 | g << 8 | b;
        }


        public static int getRed(final int hex) {
            return hex >> 16 & 255;
        }

        public static int getGreen(final int hex) {
            return hex >> 8 & 255;
        }

        public static int getBlue(final int hex) {
            return hex & 255;
        }

        public static int getAlpha(final int hex) {
            return hex >> 24 & 255;
        }
    }

    public Color random() {
        return new Color(Color.HSBtoRGB((float) Math.random(), (float) (0.75F + (Math.random() / 4F)), (float) (0.75F + (Math.random() / 4F))));
    }

    public int overCol(int c1, int c2, float pc01) {
        return getColor((float) red(c1) * (1 - pc01) + (float) red(c2) * pc01, (float) green(c1) * (1 - pc01) + (float) green(c2) * pc01, (float) blue(c1) * (1 - pc01) + (float) blue(c2) * pc01, (float) alpha(c1) * (1 - pc01) + (float) alpha(c2) * pc01);
    }

    public Color rainbowC(int speed, int index, float saturation, float brightness, float opacity) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        Color color = new Color(Color.HSBtoRGB(hue, saturation, brightness));
        return new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                Math.max(0, Math.min(255, (int) (opacity * 255)))
        );
    }

    public Color skyRainbowC(int speed, int index) {
        double angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        return Color.getHSBColor(
                ((angle %= 360) / 360.0) < 0.5 ? -((float) (angle / 360.0)) : (float) (angle / 360.0),
                0.5F,
                1.0F
        );
    }
    public static int gradient(int speed, int index, int... colors) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = (angle > 180 ? 360 - angle : angle) + 180;
        int colorIndex = (int) (angle / 360f * colors.length);
        if (colorIndex == colors.length) {
            colorIndex--;
        }
        int color1 = colors[colorIndex];
        int color2 = colors[colorIndex == colors.length - 1 ? 0 : colorIndex + 1];
        return interpolate(color1, color2, angle / 360f * colors.length - colorIndex);
    }
    public int skyRainbow(int speed, int index) {
        double angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        return Color.getHSBColor(
                ((angle %= 360) / 360.0) < 0.5 ? -((float) (angle / 360.0)) : (float) (angle / 360.0),
                0.5F,
                1.0F
        ).hashCode();
    }

    public int overCol(int c1, int c2) {
        return overCol(c1, c2, 0.5f);
    }

    public static int rgba(int r, int g, int b, int a) {
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static void setAlphaColor(final int color, final float alpha) {
        final float red = (float) (color >> 16 & 255) / 255.0F;
        final float green = (float) (color >> 8 & 255) / 255.0F;
        final float blue = (float) (color & 255) / 255.0F;
        RenderSystem.color4f(red, green, blue, alpha);
    }

    public int red(int c) {
        return c >> 16 & 0xFF;
    }

    public int green(int c) {
        return c >> 8 & 0xFF;
    }

    public int blue(int c) {
        return c & 0xFF;
    }

    public int alpha(int c) {
        return c >> 24 & 0xFF;
    }

    public float redf(int c) {
        return (float) red(c) / 255.F;
    }

    public float greenf(int c) {
        return (float) green(c) / 255.F;
    }

    public float bluef(int c) {
        return (float) blue(c) / 255.F;
    }

    public float alphaf(int c) {
        return (float) alpha(c) / 255.F;
    }

    public static void setColor(int color) {
        setAlphaColor(color, (float) (color >> 24 & 255) / 255.0F);
    }

    public static int toColor(String hexColor) {
        int argb = Integer.parseInt(hexColor.substring(1), 16);
        return setAlpha(argb, 255);
    }
    public static int setAlpha(int color, int alpha) {
        return (color & 0x00ffffff) | (alpha << 24);
    }

    public static float[] rgba(final int color) {
        return new float[] {
                (color >> 16 & 0xFF) / 255f,
                (color >> 8 & 0xFF) / 255f,
                (color & 0xFF) / 255f,
                (color >> 24 & 0xFF) / 255f
        };
    }

    public static int reAlphaInt(final int color,
                                 final int alpha) {
        return (MathHelper.clamp(alpha, 0, 255) << 24) | (color & 16777215);
    }

    public int getColor(double d, double e, double f, double g) {
        return new Color((int) d, (int) e, (int) f, (int) g).getRGB();
    }

    public int getColor(float r, float g, float b, float a) {
        return new Color((int) r, (int) g, (int) b, (int) a).getRGB();
    }

    public int multAlpha(int c, float apc) {
        return getColor(red(c), green(c), blue(c), (float) alpha(c) * apc);
    }

    public int astolfo(int speed, int index) {
        double angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        return Color.getHSBColor(
                ((angle %= 360) / 360.0) < 0.5 ? -((float) (angle / 360.0)) : (float) (angle / 360.0),
                0.5F,
                1.0F
        ).hashCode();
    }

    public int rainbow(int speed, int index) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f; // Преобразуем угол в диапазон [0, 1] для HSB
        return Color.HSBtoRGB(hue, 1.0f, 1.0f); // Максимальная насыщенность и яркость
    }


    public static int getOverallColorFrom(int color1, int color2, float percentTo2) {
        final int finalRed = (int) MathSystem.lerp(color1 >> 16 & 0xFF, color2 >> 16 & 0xFF, percentTo2),
                finalGreen = (int) MathSystem.lerp(color1 >> 8 & 0xFF, color2 >> 8 & 0xFF, percentTo2),
                finalBlue = (int) MathSystem.lerp(color1 & 0xFF, color2 & 0xFF, percentTo2),
                finalAlpha = (int) MathSystem.lerp(color1 >> 24 & 0xFF, color2 >> 24 & 0xFF, percentTo2);
        return getColor(finalRed, finalGreen, finalBlue, finalAlpha);
    }

    public static int gradient(int start, int end, int index, int speed) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = (angle > 180 ? 360 - angle : angle) + 180;
        int color = interpolate(start, end, MathHelper.clamp(angle / 180f - 1, 0, 1));
        float[] hs = rgba(color);
        float[] hsb = Color.RGBtoHSB((int) (hs[0] * 255), (int) (hs[1] * 255), (int) (hs[2] * 255), null);

        hsb[1] *= 1.5F;
        hsb[1] = Math.min(hsb[1], 1.0f);

        return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
    }


    public Color interpolate(Color color1, Color color2, double amount) {
        amount = 1F - amount;
        amount = (float) MathHelper.clamp(0, 1, amount);
        return new Color(
        		(int) MathSystem.lerp(color1.getRed(), color2.getRed(), amount),
        		(int) MathSystem.lerp(color1.getGreen(), color2.getGreen(), amount),
        		(int) MathSystem.lerp(color1.getBlue(), color2.getBlue(), amount),
        		(int) MathSystem.lerp(color1.getAlpha(), color2.getAlpha(), amount)
        		);
    }

    public int getColor(int r, int g, int b, int a) {
        return new Color(r, g, b, a).getRGB();
    }
    public int getColor(float red, float green, float blue) {
        return getColor(red, green, blue, 1.0F);
    }
    public int getColor(int r, int g, int b) {
        return new Color(r, g, b, 255).getRGB();
    }

    public int getColor(int br, int a) {
        return new Color(br, br, br, a).getRGB();
    }

    public int interpolate(int color1, int color2, double amount) {
        amount = (float) MathHelper.clamp(0, 1, amount);
        return getColor(
        		MathSystem.lerp(red(color1), red(color2), amount),
        		MathSystem.lerp(green(color1), green(color2), amount),
                MathSystem.lerp(blue(color1), blue(color2), amount),
                MathSystem.lerp(alpha(color1), alpha(color2), amount)
        );
    }

    public static int interpolateColor(int to, int from, float amount) {
        amount = Math.min(1.0f, Math.max(0.0f, amount));
        int red1 = red(to);
        int green1 = green(to);
        int blue1 = blue(to);
        int alpha1 = alpha(to);
        int red2 = red(from);
        int green2 = green(from);
        int blue2 = blue(from);
        int alpha2 = alpha(from);
        int interpolatedRed = interpolateInt(red1, red2, amount);
        int interpolatedGreen = interpolateInt(green1, green2, amount);
        int interpolatedBlue = interpolateInt(blue1, blue2, amount);
        int interpolatedAlpha = interpolateInt(alpha1, alpha2, amount);
        return interpolatedAlpha << 24 | interpolatedRed << 16 | interpolatedGreen << 8 | interpolatedBlue;
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return interpolate(oldValue, newValue, (float)interpolationValue);
    }

    public static int interpolate(int start, int end, float value) {
        float[] startColor = rgba(start);
        float[] endColor = rgba(end);

        return rgba((int) MathSystem.interpolate(startColor[0] * 255, endColor[0] * 255, value),
                (int) MathSystem.interpolate(startColor[1] * 255, endColor[1] * 255, value),
                (int) MathSystem.interpolate(startColor[2] * 255, endColor[2] * 255, value),
                (int) MathSystem.interpolate(startColor[3] * 255, endColor[3] * 255, value));
    }

    public static Color lerp(int speed, int index, Color start, Color end) {
        int angle = (int) (((System.currentTimeMillis()) / speed + index) % 360);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return interpolate(start, end, angle / 360f);
    }

    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) MathHelper.clamp(0, 255, alpha));
    }

}
