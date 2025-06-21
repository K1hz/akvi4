package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;
import minecraft.game.advantage.alacrity.AnimationManager;
import minecraft.game.advantage.alacrity.Direction;
import minecraft.game.advantage.alacrity.impl.DecelerateAnimation;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.Projection;
import minecraft.game.advantage.make.engine2d.RectanglesSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.operation.combat.DDATTACK;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.transactions.EventPreRender3D;
import minecraft.game.transactions.EventRender2D;
import minecraft.game.transactions.EventUpdate;
import minecraft.system.AG;

import java.util.ArrayList;
import java.util.List;

import static com.mojang.blaze3d.platform.GlStateManager.GL_QUADS;
import static com.mojang.blaze3d.systems.RenderSystem.depthMask;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR_TEX;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

@Defuse(name = "Target Esp",description = "Добавляет маркер на таргет", brand = Category.Visual)
public class TargetEsp extends Module {
    public ModeSetting mode = new ModeSetting("Мод", "Маркер", "Маркер", "Старый", "2 Меча", "Призраки", "Сковывающий", "Кругляшок");
    public ModeSetting attackColorMode = new ModeSetting("От удара", "Краснеть", "Краснеть", "Ничего");
    public ModeSetting anim = new ModeSetting("Анимация", "Статичный", "Статичный", "Динамический").visibleIf(() -> !this.mode.is("Призраки") && !this.mode.is("Новые призраки") && !this.mode.is("Кольцо") && !this.mode.is("Сковывающий") && !this.mode.is("Кругляшок") && !this.mode.is("Старый кругляшок"));;

    public ModeSetting type = new ModeSetting("Режим цвета", "Новый", "Новый", "Старый").visibleIf(() -> !this.mode.is("Призраки") && !this.mode.is("Новые призраки") && !this.mode.is("Сковывающий") && !this.mode.is("Кругляшок") && !this.mode.is("Старый кругляшок"));
    final ModeSetting aiming = new ModeSetting("Позиция рендера", "Торс", "Голова", "Шея", "Грудь", "Торс", "Ноги").visibleIf(() -> !this.mode.is("Призраки") && !this.mode.is("Сковывающий") && !this.mode.is("Новые призраки") && !this.mode.is("Кругляшок") && !this.mode.is("Старый кругляшок"));
    private final CheckBoxSetting downTest = new CheckBoxSetting("Сквозь стены", true).visibleIf(() -> this.mode.is("Призраки") || this.mode.is("Кругляшок") || this.mode.is("Сковывающий"));
    private final SliderSetting size = new SliderSetting("Размер", 15, 1, 30, 1f).visibleIf(() -> !this.mode.is("Призраки") && !this.mode.is("Новые призраки") && !this.mode.is("Сковывающий") && !this.mode.is("Кругляшок") && !this.mode.is("Старый кругляшок") && !this.mode.is("Квадратный") && !this.anim.is("Динамический"));
    private final AnimationManager alpha = new DecelerateAnimation(700, 255);
    private LivingEntity currentTarget;
    private long lastTime = System.currentTimeMillis();

    public TargetEsp() {
        addSettings(mode, type, anim, aiming, size, downTest, attackColorMode);
    }

    float length;

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (AG.getInst().getModuleManager().getDDATTACK().getTarget() != null) {
            currentTarget = AG.getInst().getModuleManager().getDDATTACK().getTarget();
        }

        this.alpha.setDirection(AG.getInst().getModuleManager().getDDATTACK().getTarget() != null ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    @Subscribe
    public void render(EventPreRender3D e) {
        if (this.alpha.finished(Direction.BACKWARDS)) {
            return;
        }

        if (mode.is("Кругляшок")) {
            drawCircleMarker(e.getMatrix(), e);
        }
        if (mode.is("Старый кругляшок")) {
            drawCircleMarker1(e.getMatrix(), e);
        }
        if (mode.is("Сковывающий")) {
            drawPizzdec(e.getMatrix(), e);
        }
        if (mode.is("Призраки")) {
            drawGhostNew(e.getMatrix(), e);
        }

    }

    @Subscribe
    public void onDisplay(EventRender2D e) {
        if (mc.world == null || e.getType() != EventRender2D.Type.PRE) {
            return;
        }
        if (this.alpha.finished(Direction.BACKWARDS)) {
            return;
        }

        if (mode.is("Маркер")) {
            drawImageMarker(e);
        }
        if (mode.is("Старый")) {
            drawImageMarker2(e);
        }

        if (mode.is("2 Меча")) {
            drawImageMarkerSword(e);
        }
    }
    public void glCenteredScale(final float x, final float y, final float w, final float h, final float f) {
        glTranslatef(x + w / 2, y + h / 2, 0);
        glScalef(f, f, 1);
        glTranslatef(-x - w / 2, -y - h / 2, 0);
    }

    public double getScale(Vector3d position, double size) {
        Vector3d cam = mc.getRenderManager().info.getProjectedView();
        double distance = cam.distanceTo(position);
        double fov = mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);

        return Math.max(10f, 1000 / distance) * (size / 30f) / (fov == 70 ? 1 : fov / 70.0f);
    }
    public void drawPizzdec(MatrixStack stack, EventPreRender3D e) {
        if (this.currentTarget != null && this.currentTarget != mc.player) {
            MatrixStack ms = stack;
            ms.push();

            RenderSystem.pushMatrix();
            RenderSystem.disableLighting();
            depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableCull();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFuncSeparate(770, 1, 0, 1);
            if (this.downTest.getValue()) {
                RenderSystem.disableDepthTest();
            }
            double x = currentTarget.getPosX();
            double y = currentTarget.getPosY() + (currentTarget.getHeight() / 2);
            double z = currentTarget.getPosZ();
            double radius = 0.54;
            float speed = 13;
            float size = 0.36f;
            double distance = 20;
            int length = 150;
            float colors = Hud.getColor(1);
            if (this.attackColorMode.is("Краснеть")) {
                if (currentTarget.hurtTime != 0) {
                    colors = ColoringSystem.rgb(255,0,0);
                }
            }
            int color = (int) colors;
            int maxAlpha = 255;
            int alphaFactor = 5;
            ActiveRenderInfo camera = mc.getRenderManager().info;
            ms.translate(-mc.getRenderManager().info.getProjectedView().getX(), -mc.getRenderManager().info.getProjectedView().getY(), -mc.getRenderManager().info.getProjectedView().getZ());
            Vector3d interpolated = MathSystem.interpolate(currentTarget.getPositionVec(), new Vector3d(currentTarget.lastTickPosX, currentTarget.lastTickPosY, currentTarget.lastTickPosZ), e.getPartialTicks());
            interpolated.y += 0.25 + currentTarget.getHeight() / 2;

            ms.translate(interpolated.x + 0.2, interpolated.y, interpolated.z);

            RectanglesSystem.bindTexture(new ResourceLocation("render/images/glow.png"));
            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle) * radius;
                double c = Math.cos(angle) * radius;
                ms.translate(s, (c), -c);
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                tessellator.draw();
                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                ms.translate(-(s), -(c), (c));
            }
            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle) * radius;
                double c = Math.cos(angle) * radius;
                ms.translate(-s, s, -c);
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                tessellator.draw();
                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                ms.translate((s), -(s), (c));
            }
            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle) * radius;
                double c = Math.cos(angle) * radius;
                ms.translate(-(s), -(s), (c));
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                tessellator.draw();
                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                ms.translate((s), (s), -(c));
            }
            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle) * radius;
                double c = Math.cos(angle) * radius;
                ms.translate(s, (c), -c);
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                tessellator.draw();
                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                ms.translate(-(s), -(c), (c));
            }
            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle) * radius;
                double c = Math.cos(angle) * radius;
                ms.translate(-s, s, -c);
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                tessellator.draw();
                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                ms.translate((s), -(s), (c));
            }
            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle) * radius;
                double c = Math.cos(angle) * radius;
                ms.translate(-(s), -(s), (c));
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                tessellator.draw();
                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                ms.translate((s), (s), -(c));
            }

            ms.translate(-x, -y, -z);
            if (this.downTest.getValue()) {
                RenderSystem.enableDepthTest();
            }
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableAlphaTest();
            depthMask(true);
            RenderSystem.popMatrix();
            ms.pop();
        }
    }
    public void drawGhostNew(MatrixStack stack, EventPreRender3D e) {
        if (this.currentTarget != null && this.currentTarget != mc.player) {
            MatrixStack ms = stack;
            ms.push();

            RenderSystem.pushMatrix();
            RenderSystem.disableLighting();
            depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableCull();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFuncSeparate(770, 1, 0, 1);
            if (this.downTest.getValue()) {
                RenderSystem.disableDepthTest();
            }
            double x = currentTarget.getPosX();
            double y = currentTarget.getPosY() + (currentTarget.getHeight() / 2);
            double z = currentTarget.getPosZ();
            double radius = 0.81;
            float speed = 25;
            float size = 0.41f;
            double distance = 15;
            int length = 13;
            float colors = Hud.getColor(1);
            if (this.attackColorMode.is("Краснеть")) {
                if (currentTarget.hurtTime != 0) {
                    colors = ColoringSystem.rgb(255,0,0);
                }
            }
            int color = (int) colors;
            int maxAlpha = 255;
            int alphaFactor = 18;
            ActiveRenderInfo camera = mc.getRenderManager().info;
            ms.translate(-mc.getRenderManager().info.getProjectedView().getX(), -mc.getRenderManager().info.getProjectedView().getY(), -mc.getRenderManager().info.getProjectedView().getZ());
            Vector3d interpolated = MathSystem.interpolate(currentTarget.getPositionVec(), new Vector3d(currentTarget.lastTickPosX, currentTarget.lastTickPosY, currentTarget.lastTickPosZ), e.getPartialTicks());
            interpolated.y += 0.25 + currentTarget.getHeight() / 2;

            ms.translate(interpolated.x + 0.2, interpolated.y, interpolated.z);

            RectanglesSystem.bindTexture(new ResourceLocation("render/images/glow.png"));
            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle) * radius;
                double c = Math.cos(angle) * radius;
                ms.translate(s, (c), -c);
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                tessellator.draw();
                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                ms.translate(-(s), -(c), (c));
            }
            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle) * radius;
                double c = Math.cos(angle) * radius;
                ms.translate(-s, s, -c);
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                tessellator.draw();
                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                ms.translate((s), -(s), (c));
            }
            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle) * radius;
                double c = Math.cos(angle) * radius;
                ms.translate(-(s), -(s), (c));
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                tessellator.draw();
                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                ms.translate((s), (s), -(c));
            }
            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle) * radius;
                double c = Math.cos(angle) * radius;
                ms.translate(s, (c), -c);
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                tessellator.draw();
                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                ms.translate(-(s), -(c), (c));
            }
            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle) * radius;
                double c = Math.cos(angle) * radius;
                ms.translate(-s, s, -c);
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                tessellator.draw();
                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                ms.translate((s), -(s), (c));
            }
            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle) * radius;
                double c = Math.cos(angle) * radius;
                ms.translate(-(s), -(s), (c));
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(GraphicsSystem.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                tessellator.draw();
                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);
                ms.translate((s), (s), -(c));
            }

            ms.translate(-x, -y, -z);
            if (this.downTest.getValue()) {
                RenderSystem.enableDepthTest();
            }
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableAlphaTest();
            depthMask(true);
            RenderSystem.popMatrix();
            ms.pop();
        }
    }

    public void drawImageMarker(EventRender2D e) {
        float aimingValue = 0;
        if (this.aiming.is("Шея")) {
            aimingValue = 1.595f;
        } else if (this.aiming.is("Голова")) {
            aimingValue = 1.2f;
        } else if (this.aiming.is("Грудь")) {
            aimingValue = 1.65f;
        } else if (this.aiming.is("Торс")) {
            aimingValue = 2.2f;
        } else if (this.aiming.is("Ноги")) {
            aimingValue = 3f;
        }
        if (this.currentTarget != null && this.currentTarget != mc.player) {
            double sin = Math.sin(System.currentTimeMillis() / 2300.0);
            double distance = mc.player.getDistance(currentTarget);
            float maxSize = (float) getScale(currentTarget.getPositionVec(), this.size.getValue());
            float pid1 = Math.max(maxSize - (float) distance, 5.0F);
            if (this.anim.is("Динамический")) {
                pid1 = Math.max(110 , 5.0F);
            }
            if (this.anim.is("Статичный")) {
                pid1 = Math.max(maxSize - (float) distance, 5.0F);
            }
            float size = pid1;
            Vector3d interpolated = currentTarget.getPositon(e.getPartialTicks());
            Vector2f pos = Projection.project(interpolated.x, interpolated.y + currentTarget.getHeight() / aimingValue, interpolated.z);
            GlStateManager.pushMatrix();
            GlStateManager.translatef(pos.x, pos.y, 0);
            GlStateManager.rotatef((float) sin * 460, 0, 0, 1);
            GlStateManager.translatef(-pos.x, -pos.y, 0);

            if (pos != null) {
                Vector4i colors = new Vector4i(
                        ColoringSystem.setAlpha(Hud.getColor(1), (int) (alpha.getOutput())),
                        ColoringSystem.setAlpha(Hud.getColor(180), (int) (alpha.getOutput())),
                        ColoringSystem.setAlpha(Hud.getColor(360), (int) (alpha.getOutput())),
                        ColoringSystem.setAlpha(Hud.getColor(960), (int) (alpha.getOutput()))
                );
                if (pos != null) {
                    if (this.type.is("Старый")) {
                        if (currentTarget.hurtTime == 0) {
                            GraphicsSystem.drawImage(new ResourceLocation("render/images/modules/marker.png"), pos.x - size / 2f, pos.y - size / 2f, size, size, colors);
                        }
                        if (this.attackColorMode.is("Краснеть")) {
                            if (currentTarget.hurtTime != 0) {
                                GraphicsSystem.drawImage(new ResourceLocation("render/images/modules/marker.png"), pos.x - size / 2f, pos.y - size / 2f, size, size, ColoringSystem.rgba(255, 0, 0, 215));
                            }
                        }
                    }
                    if (this.type.is("Новый")) {
                        if (currentTarget.hurtTime == 0) {
                            GraphicsSystem.drawImageAlpha(new ResourceLocation("render/images/modules/marker.png"), pos.x - size / 2f, pos.y - size / 2f, size, size, colors);
                        }

                        if (this.attackColorMode.is("Краснеть")) {
                            if (currentTarget.hurtTime != 0) {
                                GraphicsSystem.drawImageAlpha(new ResourceLocation("render/images/modules/marker.png"), pos.x - size / 2f, pos.y - size / 2f, size, size, ColoringSystem.rgba(255, 0, 0, 215));
                            }
                        }
                    }
                }
                GlStateManager.popMatrix();
            }
        }
    }
    public void drawImageMarker2(EventRender2D e) {
        float aimingValue = 0;
        if (this.aiming.is("Шея")) {
            aimingValue = 1.595f;
        } else if (this.aiming.is("Голова")) {
            aimingValue = 1.2f;
        } else if (this.aiming.is("Грудь")) {
            aimingValue = 1.65f;
        } else if (this.aiming.is("Торс")) {
            aimingValue = 2.2f;
        } else if (this.aiming.is("Ноги")) {
            aimingValue = 3f;
        }
        if (this.currentTarget != null && this.currentTarget != mc.player) {
            double sin = Math.sin(System.currentTimeMillis() / 3300.0);
            double distance = mc.player.getDistance(currentTarget);
            float maxSize = (float) getScale(currentTarget.getPositionVec(), this.size.getValue());
            float pid1 = Math.max(maxSize - (float) distance, 5.0F);
            if (this.anim.is("Динамический")) {
                pid1 = Math.max(130 , 5.0F);
            }
            if (this.anim.is("Статичный")) {
                pid1 = Math.max(maxSize - (float) distance, 5.0F);
            }
            float size = pid1;
            Vector3d interpolated = currentTarget.getPositon(e.getPartialTicks());
            Vector2f pos = Projection.project(interpolated.x, interpolated.y + currentTarget.getHeight() / aimingValue, interpolated.z);
            GlStateManager.pushMatrix();
            GlStateManager.translatef(pos.x, pos.y, 0);
            GlStateManager.rotatef((float) sin * 960, 0, 0, 1);
            GlStateManager.translatef(-pos.x, -pos.y, 0);
            Vector4i colors = new Vector4i(
                    ColoringSystem.setAlpha(Hud.getColor(1), (int) (alpha.getOutput())),
                    ColoringSystem.setAlpha(Hud.getColor(180), (int) (alpha.getOutput())),
                    ColoringSystem.setAlpha(Hud.getColor(360), (int) (alpha.getOutput())),
                    ColoringSystem.setAlpha(Hud.getColor(960), (int) (alpha.getOutput()))
            );

            if (pos != null) {
                if (this.type.is("Старый")) {
                    if (currentTarget.hurtTime == 0) {
                        GraphicsSystem.drawImage(new ResourceLocation("render/images/modules/target.png"), pos.x - size / 2f, pos.y - size / 2f, size, size, colors);
                    }
                    if (this.attackColorMode.is("Краснеть")) {
                        if (currentTarget.hurtTime != 0) {
                            GraphicsSystem.drawImage(new ResourceLocation("render/images/modules/target.png"), pos.x - size / 2f, pos.y - size / 2f, size, size, ColoringSystem.rgba(255, 0, 0, 215));
                        }
                    }
                }
                if (this.type.is("Новый")) {
                    if (currentTarget.hurtTime == 0) {
                        GraphicsSystem.drawImageAlpha(new ResourceLocation("render/images/modules/target.png"), pos.x - size / 2f, pos.y - size / 2f, size, size, colors);
                    }
                    if (this.attackColorMode.is("Краснеть")) {
                        if (currentTarget.hurtTime != 0) {
                            GraphicsSystem.drawImageAlpha(new ResourceLocation("render/images/modules/target.png"), pos.x - size / 2f, pos.y - size / 2f, size, size, ColoringSystem.rgba(255, 0, 0, 215));
                        }
                    }
                }
                GlStateManager.popMatrix();
            }
        }
    }
    public void drawImageMarkerSword(EventRender2D e) {
        float aimingValue = 0;
        if (this.aiming.is("Шея")) {
            aimingValue = 1.595f;
        } else if (this.aiming.is("Голова")) {
            aimingValue = 1.2f;
        } else if (this.aiming.is("Грудь")) {
            aimingValue = 1.65f;
        } else if (this.aiming.is("Торс")) {
            aimingValue = 2.2f;
        } else if (this.aiming.is("Ноги")) {
            aimingValue = 3f;
        }
        if (this.currentTarget != null && this.currentTarget != mc.player) {
            double sin = Math.sin(System.currentTimeMillis() / 3300);
            double distance = mc.player.getDistance(currentTarget);
            float maxSize = (float) getScale(currentTarget.getPositionVec(), this.size.getValue());
            float pid1 = Math.max(maxSize - (float) distance, 5.0F);
            if (this.anim.is("Динамический")) {
                pid1 = Math.max(130 , 5.0F);
            }
            if (this.anim.is("Статичный")) {
                pid1 = Math.max(maxSize - (float) distance, 5.0F);
            }
            float size = pid1;
            Vector3d interpolated = currentTarget.getPositon(e.getPartialTicks());
            Vector2f pos = Projection.project(interpolated.x, interpolated.y + currentTarget.getHeight() / aimingValue, interpolated.z);
            GlStateManager.pushMatrix();
            GlStateManager.translatef(pos.x, pos.y, 0);
            GlStateManager.rotatef(270, 0, 0, 1);
            GlStateManager.translatef(-pos.x, -pos.y, 0);

            if (pos != null) {
                Vector4i colors = new Vector4i(
                        ColoringSystem.setAlpha(Hud.getColor(1), (int) (alpha.getOutput())),
                        ColoringSystem.setAlpha(Hud.getColor(180), (int) (alpha.getOutput())),
                        ColoringSystem.setAlpha(Hud.getColor(360), (int) (alpha.getOutput())),
                        ColoringSystem.setAlpha(Hud.getColor(960), (int) (alpha.getOutput()))
                );
                if (pos != null) {
                    if (this.type.is("Старый")) {
                        if (currentTarget.hurtTime == 0) {
                            GraphicsSystem.drawImage(new ResourceLocation("render/images/modules/targetsword.png"), pos.x - size / 2f, pos.y - size / 2f, size, size, colors);
                        }
                        if (this.attackColorMode.is("Краснеть")) {
                            if (currentTarget.hurtTime != 0) {
                                GraphicsSystem.drawImage(new ResourceLocation("render/images/modules/targetsword.png"), pos.x - size / 2f, pos.y - size / 2f, size, size, ColoringSystem.rgba(255, 0, 0, 215));
                            }
                        }
                    }
                    if (this.type.is("Новый")) {
                        if (currentTarget.hurtTime == 0) {
                            GraphicsSystem.drawImageAlpha(new ResourceLocation("render/images/modules/targetsword.png"), pos.x - size / 2f, pos.y - size / 2f, size, size, colors);
                        }
                        if (this.attackColorMode.is("Краснеть")) {
                            if (currentTarget.hurtTime != 0) {
                                GraphicsSystem.drawImageAlpha(new ResourceLocation("render/images/modules/targetsword.png"), pos.x - size / 2f, pos.y - size / 2f, size, size, ColoringSystem.rgba(255, 0, 0, 215));
                            }
                        }
                    }
                }
                GlStateManager.popMatrix();
            }
        }
    }

    public void drawCircleMarker1(MatrixStack stack, EventPreRender3D e) {
        if (this.currentTarget != null && this.currentTarget != mc.player) {
            MatrixStack ms = stack;
            ms.push();
            RenderSystem.pushMatrix();
            RenderSystem.disableLighting();
            depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableCull();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFuncSeparate(770, 1, 0, 1);
            double radius = 0.5 + currentTarget.getWidth() / 2;
            float speed = 100;
            float size = 0.15f;
            double distance = 1000;
            int lenght = (int) (distance + currentTarget.getWidth());
            ActiveRenderInfo camera = mc.getRenderManager().info;
            ms.translate(-mc.getRenderManager().info.getProjectedView().getX(),
                    -mc.getRenderManager().info.getProjectedView().getY(),
                    -mc.getRenderManager().info.getProjectedView().getZ());

            Vector3d interpolated = MathSystem.interpolate(currentTarget.getPositionVec(), new Vector3d(currentTarget.lastTickPosX, currentTarget.lastTickPosY, currentTarget.lastTickPosZ), e.getPartialTicks());

            RectanglesSystem.bindTexture(new ResourceLocation("render/images/circle.png"));

            ms.push();
            ms.translate(interpolated.x + 0.1, interpolated.y - 0.65 + currentTarget.getHeight() / 2, interpolated.z);
            for (int j = 0; j < 1; j++) {
                for (int i = 0; i < lenght; i++) {
                    Quaternion r = camera.getRotation().copy();
                    buffer.begin(GL_QUADS, POSITION_COLOR_TEX);

                    double angle = 0.1f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);

                    double s = Math.sin(angle + j * (Math.PI / 1.5)) * radius;
                    double c = Math.cos(angle + j * (Math.PI / 1.5)) * radius;

                    // Убираем вертикальные колебания
                    double yOffset = 0;

                    ms.translate(0, yOffset, 0);
                    ms.translate(s, 0, -c);

                    ms.translate(-size / 2f, -size / 2f, 0);
                    ms.rotate(r);
                    ms.translate(size / 2f, size / 2f, 0);
                    int color = Hud.getColor(1);
                    if (this.attackColorMode.is("Краснеть")) {
                        if (currentTarget.hurtTime != 0) {
                            color = ColoringSystem.rgb(255, 0, 0);
                        }
                    }
                    int alpha = (int) (1 * this.alpha.getOutput());
                    buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(ColoringSystem.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(ColoringSystem.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(ColoringSystem.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(ColoringSystem.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                    tessellator.draw();

                    ms.translate(-size / 2f, -size / 2f, 0);
                    r.conjugate();
                    ms.rotate(r);
                    ms.translate(size / 2f, size / 2f, 0);
                    ms.translate(-s, 0, c);
                    ms.translate(0, -yOffset, 0);
                }
            }
            ms.pop();


            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableAlphaTest();
            depthMask(true);
            RenderSystem.popMatrix();
            ms.pop();
        }
    }


    public void drawCircleMarker(MatrixStack stack, EventPreRender3D e) {
        if (this.currentTarget != null && this.currentTarget != mc.player) {
            MatrixStack ms = stack;
            ms.push();
            RenderSystem.pushMatrix();
            RenderSystem.disableLighting();
            depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableCull();
            RenderSystem.disableAlphaTest();
            if (this.downTest.getValue()) {
                RenderSystem.disableDepthTest();
            }
            RenderSystem.blendFuncSeparate(770, 1, 0, 1);
            double radius = 0.24 + currentTarget.getWidth() / 2;
            float speed = 100;
            float size = 0.13f;
            double distance = 255;
            int lenght = (int) (distance + currentTarget.getWidth());
            ActiveRenderInfo camera = mc.getRenderManager().info;
            ms.translate(-mc.getRenderManager().info.getProjectedView().getX(),
                    -mc.getRenderManager().info.getProjectedView().getY(),
                    -mc.getRenderManager().info.getProjectedView().getZ());

            Vector3d interpolated = MathSystem.interpolate(currentTarget.getPositionVec(), new Vector3d(currentTarget.lastTickPosX, currentTarget.lastTickPosY, currentTarget.lastTickPosZ), e.getPartialTicks());
            ms.translate(interpolated.x + 0.15, interpolated.y + 0.3 + currentTarget.getHeight() / 2, interpolated.z);
            RectanglesSystem.bindTexture(new ResourceLocation("render/images/glow.png"));
            for (int j = 0; j < 1; j++) {
                for (int i = 0; i < lenght; i++) {
                    Quaternion r = camera.getRotation().copy();
                    buffer.begin(GL_QUADS, POSITION_COLOR_TEX);

                    double angle = 0.1f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);

                    double s = Math.sin(angle + j * (Math.PI / 1.5)) * radius;
                    double c = Math.cos(angle + j * (Math.PI / 1.5)) * radius;

                    double yOffset = Math.sin(System.currentTimeMillis() * 0.003 + j) * 0.8;

                    ms.translate(0, yOffset, 0);

                    ms.translate(s, 0, -c);

                    ms.translate(-size / 2f, -size / 2f, 0);
                    ms.rotate(r);
                    ms.translate(size / 2f, size / 2f, 0);
                    int color = Hud.getColor(1);
                    if (this.attackColorMode.is("Краснеть")) {
                        if (currentTarget.hurtTime != 0) {
                            color = ColoringSystem.rgb(255, 0, 0);
                        }
                    }
                    int alpha = (int) (1 * this.alpha.getOutput());
                    buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(ColoringSystem.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(ColoringSystem.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(ColoringSystem.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(ColoringSystem.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                    tessellator.draw();

                    ms.translate(-size / 2f, -size / 2f, 0);
                    r.conjugate();
                    ms.rotate(r);
                    ms.translate(size / 2f, size / 2f, 0);
                    ms.translate(-s, 0, c);
                    ms.translate(0, -yOffset, 0);
                }
            }
            if (this.downTest.getValue()) {
                RenderSystem.enableDepthTest();
            }
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableAlphaTest();
            depthMask(true);
            RenderSystem.popMatrix();
            ms.pop();
        }
    }

}
