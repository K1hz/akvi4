package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.game.advantage.luvvy.MovementSystem;
import minecraft.game.advantage.luvvy.PlayerSettingsModule;
import minecraft.game.operation.combat.DDATTACK;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.transactions.EventInput;
import minecraft.game.transactions.EventMotion;
import minecraft.game.transactions.EventUpdate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.List;

@Defuse(name = "Container Aura", description = "Открывает за пизду", brand = Category.Misc)
public class ContainerAura extends Module {
    private final SliderSetting distance = new SliderSetting("Макс. дистанция", 3, 1F, 6F, 0.5F);
    private final ModeSetting container = new ModeSetting("Контейнер", "Сундук", "Сундук", "Зелье-варка", "Печь", "Верстак");

    private BlockPos targetBlock = null;

    public ContainerAura() {
        setEnabled(false, true);
        this.addSettings(this.distance, this.container);
    }

    @Subscribe
    private void onMotion(EventMotion e) {
        if (mc.currentScreen != null)
            return;

        targetBlock = null;

        for (BlockPos blockpos : getBlockPoses()) {
            if (isFitBlock(mc.world.getBlockState(blockpos))) {
                faceBlock(blockpos, e);
                targetBlock = blockpos;
                break;
            }
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (targetBlock == null || mc.currentScreen != null)
            return;

        double angle = Math.toDegrees(Math.atan2(targetBlock.getZ(), targetBlock.getX())) - 90D;
        BlockRayTraceResult result = new BlockRayTraceResult(Vector3d.ZERO, Direction.fromAngle(angle), targetBlock, false);

        mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, result);
        this.toggle();
        targetBlock = null;
    }

    private void faceBlock(BlockPos pos, EventMotion e) {
        double dx = pos.getX() + 0.5 - mc.player.getPosX();
        double dy = pos.getY() + 0.5 - (mc.player.getPosY() + mc.player.getEyeHeight());
        double dz = pos.getZ() + 0.5 - mc.player.getPosZ();

        double distance = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distance));

//        e.setYaw(yaw);
//        e.setPitch(pitch);
//        DDATTACK.mc.player.rotationYawHead = yaw;
//        mc.player.renderYawOffset = PlayerSettingsModule.calculateCorrectYawOffset(yaw);
//        DDATTACK.mc.player.rotationPitchHead = pitch;
    }

    @Subscribe
    public void onInput(EventInput eventInput) {
//        if (targetBlock == null)
//            return;
//
//        Vector3d target = Vector3d.copyCentered(targetBlock);
//        double diffX = target.x - mc.player.getPosX();
//        double diffZ = target.z - mc.player.getPosZ();
//        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
//
//        MovementSystem.fixMovement(eventInput, yaw);
    }

    private boolean isFitBlock(BlockState blockState) {
        Block block = blockState.getBlock();

        switch (container.getValue()) {
            case "Сундук" -> {
                return block == Blocks.CHEST
                        || block == Blocks.ENDER_CHEST
                        || block == Blocks.TRAPPED_CHEST;
            }
            case "Зелье-варка" -> {
                return block == Blocks.BREWING_STAND;
            }
            case "Печь" -> {
                return block == Blocks.FURNACE;
            }
            case "Верстак" -> {
                return block == Blocks.CRAFTING_TABLE;
            }
        }

        return false;
    }

    private List<BlockPos> getBlockPoses() {
        List<BlockPos> blockPoses = new ArrayList<>();
        BlockPos playerPos = mc.player.getPosition();
        double distance = (double) this.distance.getValue();

        for (double x = -distance; x < distance; x++) {
            for (double y = -distance; y < distance; y++) {
                for (double z = -distance; z < distance; z++) {
                    blockPoses.add(playerPos.add(x, y, z));
                }
            }
        }

        return blockPoses;
    }
}
