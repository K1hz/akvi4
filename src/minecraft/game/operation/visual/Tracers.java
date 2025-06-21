package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;

import minecraft.system.managers.friend.FriendManager;
import minecraft.game.transactions.EventRender3D;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.combat.DDBOT;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeListSetting;
import minecraft.game.advantage.luvvy.EntitySystem;
import minecraft.game.advantage.make.color.ColoringSystem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

import static org.lwjgl.opengl.GL11.*;

@Defuse(name = "Tracers",description = "Рисует линии к сущностям", brand = Category.Visual)
public class Tracers extends Module {

    public ModeListSetting targets = new ModeListSetting("Отображать",
            new CheckBoxSetting("Игроки", true),
            new CheckBoxSetting("Предметы", false),
            new CheckBoxSetting("Мобы", false)
    );

    public Tracers() {
        addSettings(targets);
    }

    @Subscribe
    public void onRender(EventRender3D e) {
        glPushMatrix();

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glEnable(GL_LINE_SMOOTH);

        glLineWidth(1);

        Vector3d cam = new Vector3d(0, 0, 150)
                .rotatePitch((float) -(Math.toRadians(mc.getRenderManager().info.getPitch())))
                .rotateYaw((float) -Math.toRadians(mc.getRenderManager().info.getYaw()));

        for (Entity entity : mc.world.getAllEntities()) {
            if (!(entity instanceof PlayerEntity && entity != mc.player && targets.is("Игроки").getValue()
                    || entity instanceof ItemEntity && targets.is("Предметы").getValue()
                    || (entity instanceof AnimalEntity || entity instanceof MobEntity) && targets.is("Мобы").getValue()
            )) continue;
            if (DDBOT.isBot(entity) || !entity.isAlive()) continue;

            Vector3d pos = EntitySystem.getInterpolatedPositionVec(entity).subtract(mc.getRenderManager().info.getProjectedView());

            ColoringSystem.setColor(FriendManager.isFriend(entity.getName().getString()) ? FriendManager.getColor() : -1);

            buffer.begin(1, DefaultVertexFormats.POSITION);

            buffer.pos(cam.x, cam.y, cam.z).endVertex();
            buffer.pos(pos.x, pos.y, pos.z).endVertex();


            tessellator.draw();
        }

        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);

        glPopMatrix();
    }
}
