package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;

import minecraft.game.transactions.*;
import minecraft.system.managers.Theme;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.advantage.advisee.SoundPlayer;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.SDestroyEntitiesPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.ArrayList;
import java.util.List;

@Defuse(name = "Death Effect",description = "Добавляет эффект при убийстве", brand = Category.Visual)
public class DeathEffect extends Module {
    private Animation animate = new Animation();
    private Animation animation = new Animation();
    private boolean useAnimation;

    private final CheckBoxSetting onlyPlayer = new CheckBoxSetting("Только на игроков", true);

    LivingEntity target;
    long time;
    public TimeCounterSetting TimeCounterSetting = new TimeCounterSetting();

    private float yaw, pitch;

    private final List<Vector3d> position = new ArrayList<>();

    private int current;

    private Vector3d setPosition;

    public DeathEffect() {
        addSettings(onlyPlayer);
    }

    @Subscribe
    public void onPacket(AttackEvent e) {
        if (mc.player == null || mc.world == null)
            return;

        // НЕ БЕЙТЕ X5
        if (onlyPlayer.getValue()) {
            if (e.entity instanceof PlayerEntity)
                target = (LivingEntity) e.entity;
        } else {
            target = (LivingEntity) e.entity;
        }
        time = System.currentTimeMillis();

    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player == null || mc.world == null)
            return;

        if (e.getPacket() instanceof SDestroyEntitiesPacket p) {
            for (int ids : p.getEntityIDs()) {
                if (target != null) {
                    if (ids == mc.player.getEntityId())
                        continue;

                    if (time + 400 >= System.currentTimeMillis() && target.getEntityId() == ids) {
                        if (((LivingEntity) mc.world.getEntityByID(ids)).getHealth() < 5) {
                            onKill(target);
                            target = null;
                        }
                    }
                }
            }
        }

    }

    public float back;

    @Subscribe
    public void onUpdate(EventMotion e) {
        if (mc.player == null || mc.world == null)
            return;

        if (useAnimation) {
            if (mc.player.ticksExisted % 5 == 0)
                current++;
            Vector3d player = new Vector3d(
                    MathSystem.interpolate(mc.player.getPosX(), mc.player.lastTickPosX, mc.getRenderPartialTicks()),
                    MathSystem.interpolate(mc.player.getPosY(), mc.player.lastTickPosY, mc.getRenderPartialTicks()),
                    MathSystem.interpolate(mc.player.getPosZ(), mc.player.lastTickPosZ, mc.getRenderPartialTicks()))
                    .add(0, mc.player.getEyeHeight(), 0);

            position.add(player);
        }

        if (target != null) {
            if (time + 1000 >= System.currentTimeMillis() && target.getHealth() <= 0f) {
                onKill(target);
                target = null;
            }

        }

        if (TimeCounterSetting.isReached(500)) {
            animate = animate.animate(0, 1f, Easings.CIRC_OUT);
            animation = animation.animate(0, 1f, Easings.EXPO_OUT);
        }
        if (TimeCounterSetting.isReached(1000)) {
            useAnimation = false;
            last = null;
        }
    }

    public Vector2f last;

    @Subscribe
    public void onCameraController(EventCamera e) {
        if (useAnimation) {
            mc.getRenderManager().info.setDirection(
                    (float) (yaw + (6 * animate.getValue())),
                    (float) (pitch + (6 * animate.getValue())));

            back = MathSystem.fast(back, TimeCounterSetting.isReached(700) ? 1 : 0, 10);
            Vector3d player = new Vector3d(
                    MathSystem.interpolate(mc.player.getPosX(), mc.player.lastTickPosX, mc.getRenderPartialTicks()),
                    MathSystem.interpolate(mc.player.getPosY(), mc.player.lastTickPosY, mc.getRenderPartialTicks()),
                    MathSystem.interpolate(mc.player.getPosZ(), mc.player.lastTickPosZ, mc.getRenderPartialTicks()))
                    .add(0, mc.player.getEyeHeight(), 0);

            if (setPosition != null) {
                mc.getRenderManager().info.setDirection(
                        (float) MathSystem.interpolate((float) (yaw + (6 * animate.getValue())), mc.player.getYaw(e.partialTicks), 1 - back),
                        (float) MathSystem.interpolate((float) (pitch + (6 * animate.getValue())), mc.player.getPitch(e.partialTicks), 1 - back));
                mc.getRenderManager().info.setPosition(MathSystem.interpolate(setPosition, player, 1 - back));
            }
            mc.getRenderManager().info.moveForward(2f * animate.getValue());
        }

    }

    @Subscribe
    public void onDisplay(EventRender2D e) {
        if (mc.player == null || mc.world == null || e.getType() != EventRender2D.Type.POST) {
            return;
        }
        animate.update();
        animation.update();
        float animateImage = (float) (200 - 200 * animation.getValue());
        if (useAnimation && setPosition != null && position.size() > 1) {
            setPosition = MathSystem.fast(setPosition, position.get(current), 1);
            GraphicsSystem.drawWhite((float) animate.getValue());
            GraphicsSystem.drawImage(new ResourceLocation("render/images/modules/frag.png"), 0 - animateImage, 0 - animateImage, window.getScaledWidth() + animateImage * 2, window.getScaledHeight() + animateImage * 2, ColoringSystem.reAlphaInt(Theme.mainRectColor, (int) (140 * animation.getValue())));
        }
    }

    public void onKill(LivingEntity entity) {
        position.clear();
        current = 0;
        animate = animate.animate(1, 1f, Easings.CIRC_OUT);
        animation = animation.animate(1, 1f, Easings.EXPO_OUT);
        useAnimation = true;
        TimeCounterSetting.reset();
        Vector3d player = new Vector3d(
                MathSystem.interpolate(mc.player.getPosX(), mc.player.lastTickPosX, mc.getRenderPartialTicks()),
                MathSystem.interpolate(mc.player.getPosY(), mc.player.lastTickPosY, mc.getRenderPartialTicks()),
                MathSystem.interpolate(mc.player.getPosZ(), mc.player.lastTickPosZ, mc.getRenderPartialTicks()))
                .add(0, mc.player.getEyeHeight(), 0);

        setPosition = player;
        SoundPlayer.playSound("frag.wav");
        yaw = mc.player.getYaw(mc.getRenderPartialTicks());
        pitch = mc.player.getPitch(mc.getRenderPartialTicks());
    }
}
