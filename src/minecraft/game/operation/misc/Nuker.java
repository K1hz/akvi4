package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import minecraft.game.advantage.luvvy.MovementSystem;
import minecraft.game.advantage.luvvy.PlayerSettingsModule;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.operation.combat.DDATTACK;
import minecraft.game.operation.visual.Hud;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.transactions.EventInput;
import minecraft.game.transactions.EventMotion;
import minecraft.game.transactions.EventRender3D;
import minecraft.game.transactions.EventUpdate;
import minecraft.system.AG;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.OreBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.optifine.render.RenderUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import java.util.HashMap;
import java.util.Map;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Defuse(name = "Nuker", description = "123", brand = Category.Player)
public class Nuker extends Module {

    private final SliderSetting radiusXZ = new SliderSetting("Радиус XZ", 3, 1, 5, 1);
    private final SliderSetting radiusY = new SliderSetting("Радиус Y", 1, 1, 6, 1);
    private final CheckBoxSetting orePriority = new CheckBoxSetting("Приоритет на руды", true);
    private final CheckBoxSetting correctionMove = new CheckBoxSetting("Коррекция движения", true);
    private final CheckBoxSetting camera = new CheckBoxSetting("Корректировать камеру", false);
    private final CheckBoxSetting fastBreak = new CheckBoxSetting("ФастБрик", true);
    public ModeSetting modes = new ModeSetting("Копать", "Вверх", "Вверх", "Вниз");
    private AxisAlignedBB box;
    private BlockPos pos;
    private final Map<BlockPos, Integer> breakAttempts = new HashMap<>();

    @Subscribe
    public void onRender3D(EventRender3D e) {
        if (pos != null) {
            GraphicsSystem.drawBlockBox(pos, Hud.getColor(1));
        }
    }

    public Nuker() {
        addSettings(modes, radiusXZ, radiusY, orePriority, fastBreak, camera, correctionMove);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        updateNuker();
    }

    public void updateNuker() {
        pos = PlayerSettingsModule.getCube(mc.player.getPosition(), radiusXZ.getValue(), radiusY.getValue()).stream()
                .filter(this::validBlock)
                .filter(pos -> {
                    if (modes.getValue().equals("Вверх")) {
                        return pos.getY() >= mc.player.getPosition().getY();
                    }
                    return true;
                })
                .sorted(Comparator.comparingInt(pos -> {
                    boolean isOre = mc.world.getBlockState(pos).getBlock() instanceof OreBlock;
                    int priority = orePriority.getValue() && isOre ? 0 : 1;
                    int heightDiff = Math.abs(mc.player.getPosition().getY() - pos.getY());
                    return priority * 10 + heightDiff;
                }))
                .findFirst()
                .orElse(null);

        if (pos != null) {
            box = mc.world.getBlockState(pos).getShape(mc.world, pos).getBoundingBox().offset(pos);

            if (mc.playerController.onPlayerDamageBlock(pos, Direction.UP)) {
                breakAttempts.remove(pos);
            } else {
                breakAttempts.put(pos, breakAttempts.getOrDefault(pos, 0) + 1);
                if (breakAttempts.get(pos) >= 3) {
                    breakAttempts.remove(pos);
                    return;
                }
            }

            mc.player.swingArm(Hand.MAIN_HAND);
        }
    }

    private boolean validBlock(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        return !state.isAir() && state.getBlock() != Blocks.CAVE_AIR && state.getBlock() != Blocks.WATER && state.getBlock() != Blocks.LAVA && state.getBlock() != Blocks.BEDROCK && state.getBlock() != Blocks.BARRIER
                && breakAttempts.getOrDefault(pos, 0) < 3;
    }

    @Subscribe
    private void onWalking(EventMotion e) {
        if (pos == null) return;

        Vector3d target = Vector3d.copyCentered(pos);
        double diffX = target.x - mc.player.getPosX();
        double diffY = target.y - (mc.player.getPosY() + mc.player.getEyeHeight());
        double diffZ = target.z - mc.player.getPosZ();
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, dist));

        if (this.camera.getValue()) {
            e.setYaw(yaw);
            e.setPitch(pitch);
            DDATTACK.mc.player.rotationYawHead = yaw;
            mc.player.renderYawOffset = PlayerSettingsModule.calculateCorrectYawOffset(yaw);
            DDATTACK.mc.player.rotationPitchHead = pitch;
        }
    }
    @Subscribe
    public void onInput(EventInput eventInput) {
        if (pos == null) return;
        Vector3d target = Vector3d.copyCentered(pos);
        double diffX = target.x - mc.player.getPosX();
        double diffZ = target.z - mc.player.getPosZ();
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        if (this.correctionMove.getValue()) {
            MovementSystem.fixMovement(eventInput, yaw);
        }
    }

    private void lookAtBlock(BlockPos pos) {
        Vector3d target = Vector3d.copyCentered(pos);
        double diffX = target.x - mc.player.getPosX();
        double diffY = target.y - (mc.player.getPosY() + mc.player.getEyeHeight());
        double diffZ = target.z - mc.player.getPosZ();
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, dist));

        mc.player.rotationYaw = yaw;
        mc.player.rotationPitch = pitch;
    }
}

