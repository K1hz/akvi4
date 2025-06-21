package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;

import com.mojang.blaze3d.systems.RenderSystem;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.transactions.EventRender2D;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.system.managers.Theme;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import lombok.Getter;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.math.RayTraceResult.Type;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@Defuse(name = "Crosshair",description = "Изменяет прицел", brand = Category.Visual)
public class Crosshair extends Module {

    public final ModeSetting mode = new ModeSetting("Вид", "Орбиз", "Орбиз", "Классический", "Круг");
    final SliderSetting gap123 = new SliderSetting("Зазор", 1.5f, 0.1f, 6, 0.1f).visibleIf(() -> this.mode.is("Классический"));
    final SliderSetting gapAN = new SliderSetting("Сила расширения", 8.0f, 0.6f, 6, 0.1f).visibleIf(() -> this.mode.is("Классический"));;
    final SliderSetting lenght = new SliderSetting("Длина", 3f, 0.4f, 6, 0.1f).visibleIf(() -> this.mode.is("Классический"));;
    final SliderSetting thicknes = new SliderSetting("Ширина", 1f, 0.5f, 2, 0.1f).visibleIf(() -> this.mode.is("Классический"));;
    public final ModeSetting tochka = new ModeSetting("Точка в центре?", "Да", "Да", "Нет").visibleIf(() -> this.mode.is("Классический"));;
    public final CheckBoxSetting staticCrosshair = new CheckBoxSetting("Статический", false).visibleIf(() -> mode.is("Орбиз"));
    private float lastYaw;
    private float lastPitch;

    @Getter
    public float animatedYaw, x;
    @Getter
    public float animatedPitch, y;

    private float animation;
    private float animationSize;

    private final int outlineColor = Color.BLACK.getRGB();
    private final int entityColor = Color.RED.getRGB();

    public Crosshair() {
        addSettings(mode, staticCrosshair, gap123, gapAN, lenght, thicknes, tochka);
    }



    @Subscribe
    public void onDisplay(EventRender2D e) {
        if (mc.player == null || mc.world == null || e.getType() != EventRender2D.Type.POST) {
            return;
        }

        x = mc.getMainWindow().getScaledWidth() / 2f;
        y = mc.getMainWindow().getScaledHeight() / 2f;

        switch (mode.getIndex()) {
            case 0 -> {
                float size = 5;

                animatedYaw = MathSystem.fast(animatedYaw, ((lastYaw - mc.player.rotationYaw) + mc.player.moveStrafing) * size, 5);
                animatedPitch = MathSystem.fast(animatedPitch, ((lastPitch - mc.player.rotationPitch) + mc.player.moveForward) * size, 5);
                animation = MathSystem.fast(animation, mc.objectMouseOver.getType() == Type.ENTITY ? 1 : 0, 5);

                int color = ColoringSystem.interpolate(Hud.getColor(1), Hud.getColor(360), 1 - animation);

                if (!staticCrosshair.getValue()) {
                    x += animatedYaw;
                    y += animatedPitch;
                }

                animationSize = MathSystem.fast(animationSize, (1 - mc.player.getCooledAttackStrength(1)) * 5, 10);

                float radius = 3 + (staticCrosshair.getValue() ? 0 : animationSize);
                if (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
                    GraphicsSystem.drawShadowCircle(x, y, radius * 2, ColoringSystem.setAlpha(color, 64));
                    GraphicsSystem.drawCircle(x, y, radius, color);
                }
                lastYaw = mc.player.rotationYaw;
                lastPitch = mc.player.rotationPitch;
            }

            case 1 -> {
                if (mc.gameSettings.getPointOfView() != PointOfView.FIRST_PERSON) return;

                float cooldown = 1 - mc.player.getCooledAttackStrength(0);
                animationSize = MathSystem.fast(animationSize, (1 - mc.player.getCooledAttackStrength(0)), 10);
                float thickness = thicknes.getValue();
                float length = lenght.getValue();
                float gap = gap123.getValue() + gapAN.getValue() * animationSize;

                int color = mc.pointedEntity != null ? entityColor : -1;

                // Adjust the x, y to center the drawing
                float centerX = x;
                float centerY = y;

                // Вычисляем смещение для прямоугольников, чтобы они всегда оставались по центру
                float halfThickness = thickness / 2;
                float halfLength = length / 2;

                // Делаем прямоугольники выровненными по центру, с учетом их размеров
                drawOutlined(centerX - halfThickness, centerY - gap - length, thickness, length, color);

                if (this.tochka.is("Да")) {
                    // Точка всегда рисуется по центру экрана
                    drawOutlined(centerX - thickness / 2, centerY - 0.5F, 0.5F, 0.5F, color);
                }
                drawOutlined(centerX - halfThickness, centerY + gap, thickness, length, color);

                drawOutlined(centerX - gap - length, centerY - halfThickness, length, thickness, color);
                drawOutlined(centerX + gap, centerY - halfThickness, length, thickness, color);
            }





            case 2 -> {
                animationSize = MathSystem.fast(animationSize, (1 - mc.player.getCooledAttackStrength(1)) * 260, 10);
                if (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
                    GraphicsSystem.drawCircleWithFill(x, y, 0, 360, 3.8f, 3, false, ColoringSystem.rgb(23,21,21));
                    GraphicsSystem.drawCircleWithFill(x, y, animationSize, 360, 3.8f, 2, false, Hud.getColor(1));
                }
            }
        }
    }

    private void drawOutlined(
            final float x,
            final float y,
            final float w,
            final float h,
            final int hex
    ) {
        GraphicsSystem.drawRectW(x - 0.5, y - 0.5, w + 1, h + 1, outlineColor); // бля че за хуйня поч его хуярит салат что наделал
        GraphicsSystem.drawRectW(x, y, w, h, hex);
    }
}
