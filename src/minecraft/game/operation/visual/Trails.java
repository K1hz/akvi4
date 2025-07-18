package minecraft.game.operation.visual;

import java.util.List;

import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.StopWatch;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.transactions.EventRender3D;
import minecraft.system.AG;
import minecraft.system.styles.StyleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import org.lwjgl.opengl.GL11;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;

import lombok.Getter;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

@Defuse(name = "Trails",description = "PENIS", brand = Category.Visual)
public class Trails extends Module {

    public final SliderSetting size = new SliderSetting("Длина", 5, 1, 15, 1);

    public Trails() {
        addSettings(size);
    }
    @Subscribe
    public void onRender(EventRender3D event) {

        float pidor = this.size.getValue() * 100;
        for (Entity entity : mc.world.getPlayers()) {
            entity.points.removeIf(p -> p.time.isReached((long) pidor));
            if (entity instanceof ClientPlayerEntity) {
                if (entity == mc.player && mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON)
                    continue;

                Vector3d player = new Vector3d(
                        MathSystem.interpolate(entity.getPosX(), entity.lastTickPosX, event.getPartialTicks()),
                        MathSystem.interpolate(entity.getPosY(), entity.lastTickPosY, event.getPartialTicks()),
                        MathSystem.interpolate(entity.getPosZ(), entity.lastTickPosZ, event.getPartialTicks()));

                entity.points.add(new Point(player));
            }
        }

        RenderSystem.pushMatrix();

        Vector3d projection = mc.getRenderManager().info.getProjectedView();
        RenderSystem.translated(-projection.x, -projection.y, -projection.z);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableTexture();
        RenderSystem.blendFunc(770, 771);
        RenderSystem.shadeModel(7425);
        RenderSystem.disableAlphaTest();
        RenderSystem.lineWidth(3);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        // render
        for (Entity entity : mc.world.getAllEntities()) {
            GL11.glBegin(GL11.GL_QUAD_STRIP);

            List<Point> points = entity.points;

            for (Point point : points) {

                float index = points.indexOf(point);

                StyleManager styleManager = AG.getInst().getStyleManager();
                float alpha = index / points.size();
                    ColoringSystem.setAlphaColor(ColoringSystem.getColorBlack(195 + points.indexOf(point)), alpha);
                GL11.glVertex3d(point.getPosition().x, point.getPosition().y, point.getPosition().z);
                GL11.glVertex3d(point.getPosition().x, point.getPosition().y + entity.getHeight(),
                        point.getPosition().z);
            }

            GL11.glEnd();

            GL11.glBegin(GL11.GL_LINE_STRIP);
            GL11.glEnd();

            GL11.glBegin(GL11.GL_LINE_STRIP);
            GL11.glEnd();

        }
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableCull();
        RenderSystem.shadeModel(7424);
        RenderSystem.depthMask(true);
        RenderSystem.popMatrix();
    }

    @Getter
    public static class Point {

        private final Vector3d position;
        private final StopWatch time = new StopWatch();

        public Point(Vector3d position) {
            this.position = position;
        }
    }

}
