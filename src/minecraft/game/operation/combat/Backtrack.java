package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.operation.visual.Hud;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.BindSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.transactions.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.server.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.optifine.render.RenderUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

@Defuse(name = "Back Track",description = "Negri", brand = Category.Movement)
public class Backtrack extends Module {

    private final BindSetting skip = new BindSetting("Сбросить", 0);
    private final SliderSetting range = new SliderSetting("Дистанция", 3f, 3f, 15f, 0.1f);
    private final SliderSetting delay = new SliderSetting("Задержка", 500f, 100f, 5_000f, 50f);

    public Backtrack() {
        addSettings(skip, range, delay);
    }

    private final List<PacketData> queue = new LinkedList<>();

    private Entity target;
    private Vector3d realPos;
    private Vector3d interpolatedrealPos;

    @Subscribe
    private void onKey(EventKey e) {
        if (e.isKeyDown(skip.getValue())) {
            reset();
        }
    }

    @Subscribe
    private void onAttack(AttackEvent e) {
        if (e.entity == target || e.entity.isInvulnerable()) return;

        target = e.entity;
        realPos = target.getPositionVec();
        interpolatedrealPos = realPos;
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        if (e.isSend() || !shouldLagging() || mc.isSingleplayer()) {
            return;
        }

        IPacket<?> packet = e.getPacket();

        if (packet instanceof SPlaySoundEffectPacket || packet instanceof SEntityStatusPacket) {
            return;
        }

        if (packet instanceof SPlayerPositionLookPacket || packet instanceof SDisconnectPacket) {
            reset();
            return;
        }

        if (packet instanceof SEntityTeleportPacket etp && etp.getEntityId() == target.getEntityId()) {
            realPos = new Vector3d(etp.getX(), etp.getY(), etp.getZ());
        }

        if (packet instanceof SEntityPacket ep) {
            if (ep.entityId == target.getEntityId()) {
                realPos = realPos.add(new Vector3d(
                        ep.posX / 4096d,
                        ep.posY / 4096d,
                        ep.posZ / 4096d
                ));
            }
        }

        e.cancel();

        synchronized (queue) {
            if (e.isReceive())
                queue.add(new PacketData(packet, System.currentTimeMillis()));
        }
    }

    @Subscribe
    private void onTick(EventTick e) {
        if (queue.isEmpty() && isTargetNull() || mc.isSingleplayer()) {
            return;
        }

        if (shouldLagging()) {
            handle(false);
        } else {
            reset();
        }
    }

    @Subscribe
    private void onRender(EventRender3D e) { // салат говна навернул вкусно очень
        if (realPos == null || mc.isSingleplayer()) {
            return;
        }

        double half = target.getWidth() / 2;
        if (interpolatedrealPos == null || realPos.distanceTo(interpolatedrealPos) >= 2) {
            interpolatedrealPos = realPos;
        }
        interpolatedrealPos = MathSystem.fast(interpolatedrealPos, realPos, 15);

        glPushMatrix();

        Vector3d renderOffset = mc.getRenderManager().info.getProjectedView();

        glTranslated(-renderOffset.x, -renderOffset.y, -renderOffset.z);
        int hurtTime = 0;
        if (target instanceof LivingEntity l) {
            hurtTime = l.hurtTime;
        }
        RenderUtils.drawBox(new AxisAlignedBB(
                        interpolatedrealPos.getX() - half, interpolatedrealPos.getY(), interpolatedrealPos.getZ() - half,
                        interpolatedrealPos.getX() + half, interpolatedrealPos.getY() + target.getHeight(), interpolatedrealPos.getZ() + half),
                ColoringSystem.interpolate(-1, Hud.getColor(0), 1 - (hurtTime / 9f)));
        glPopMatrix();
    }

    private void handle(boolean all) {
        synchronized (queue) {
            Iterator<PacketData> iterator = queue.iterator();

            while (iterator.hasNext()) {
                PacketData packetData = iterator.next();

                double factor = mc.player.getPositionVec().distanceTo(realPos) / range.getValue();

                if (!all && packetData.timestamp() + delay.getValue().longValue() * factor > System.currentTimeMillis())
                    break;

                try {
                    NetworkManager.processPacket(packetData.packet(), mc.player.connection);
                } catch (ThreadQuickExitException ignored) {
                }

                iterator.remove();
            }
        }
    }

    private boolean isTargetNull() {
        return target == null && realPos == null;
    }

    private boolean shouldLagging() {
        return target != null && target.isAlive() && !target.isInvulnerable() && mc.player.getPositionVec().distanceTo(realPos) <= range.getValue();
    }

    private void reset() {
        handle(true);
        target = null;
        realPos = null;
        interpolatedrealPos = null;
    }

    private record PacketData(IPacket<?> packet, long timestamp) {
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.isSingleplayer()) {
            return;
        }
        reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.isSingleplayer()) {
            return;
        }
        reset();
    }
}
