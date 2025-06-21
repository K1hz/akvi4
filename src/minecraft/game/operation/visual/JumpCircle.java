package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.transactions.EventJump;
import minecraft.game.transactions.EventRender3D;
import minecraft.game.transactions.EventWorld;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.concurrent.CopyOnWriteArrayList;

@Defuse(name = "Jump Circle",description = "ASDD", brand = Category.Visual)
public class JumpCircle extends Module {

    private final CopyOnWriteArrayList<Circle> circles = new CopyOnWriteArrayList<>();
    public static final ModeSetting type = new ModeSetting(
            "Мод",

            "Обычный",
            "Обычный",
            "Старый",
            "Новый",
            "Кончал ебал"
    );
    public static final SliderSetting size = new SliderSetting("Размер", 10f, 1, 50f, 1f);
    @Subscribe
    private void onJump(EventJump e) {
        circles.add(new Circle(mc.player.getPositon(mc.getRenderPartialTicks()).add(0,0.05, 0)));
    }

    private ResourceLocation circle = new ResourceLocation("render/images/modules/circle.png");
    public JumpCircle() {
        this.addSettings(
                this.type, this.size);
    }

    @Subscribe
    private void onRender(EventRender3D e) {

        GlStateManager.pushMatrix();
        GlStateManager.shadeModel(7425);
        GlStateManager.blendFunc(770,771);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.disableCull();
        GlStateManager.translated(-mc.getRenderManager().info.getProjectedView().x, -mc.getRenderManager().info.getProjectedView().y,-mc.getRenderManager().info.getProjectedView().z);

        // render
        {



            for (Circle c : circles) {
                if (type.is("Новый")) {
                    circle = new ResourceLocation("render/images/modules/jumpcircles/glow.png");
                }
                if (type.is("Кончал ебал")) {
                    circle = new ResourceLocation("render/images/modules/jumpcircles/konchalebal.png");
                }
                if (type.is("Старый")) {
                    circle = new ResourceLocation("render/images/modules/jumpcircles/circle.png");
                }
                if (type.is("Обычный")) {
                    circle = new ResourceLocation("render/images/modules/jumpcircles/client.png");
                }

                mc.getTextureManager().bindTexture(circle);
                if (System.currentTimeMillis() - c.time > 2000) circles.remove(c);
                if (System.currentTimeMillis() - c.time > 1500 && !c.isBack) {
                    c.animation.animate(0, 0.5f, Easings.CIRC_OUT);
                    c.isBack = true;
                }

                c.animation.update();
                float rad = (float) c.animation.getValue();

                Vector3d vector3d = c.vector3d;

                vector3d = vector3d.add(-rad / 2f, 0 ,-rad / 2f);

                buffer.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
                int alpha = (int) (255 * MathHelper.clamp(rad, 0, 1));
                buffer.pos(vector3d.x, vector3d.y, vector3d.z).color(ColoringSystem.setAlpha(ColoringSystem.getColorBlack(0), alpha)).tex(0,0).endVertex();
                buffer.pos(vector3d.x + rad, vector3d.y, vector3d.z).color(ColoringSystem.setAlpha(ColoringSystem.getColorBlack(90), alpha)).tex(1,0).endVertex();
                buffer.pos(vector3d.x + rad, vector3d.y, vector3d.z + rad).color(ColoringSystem.setAlpha(ColoringSystem.getColorBlack(180), alpha)).tex(1,1).endVertex();
                buffer.pos(vector3d.x, vector3d.y, vector3d.z + rad).color(ColoringSystem.setAlpha(ColoringSystem.getColorBlack(360), alpha)).tex(0,1).endVertex();
                tessellator.draw();
            }

        }

        GlStateManager.disableBlend();
        GlStateManager.shadeModel(7424);
        GlStateManager.depthMask(true);
        GlStateManager.enableAlphaTest();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }


    private class Circle {

        private final Vector3d vector3d;

        private final long time;
        private final Animation animation = new Animation();
        private boolean isBack;

        public Circle(Vector3d vector3d) {
            this.vector3d = vector3d;
            time = System.currentTimeMillis();
            animation.animate(size.getValue() / 10, 0.5f, Easings.CIRC_OUT);
        }

    }

}
