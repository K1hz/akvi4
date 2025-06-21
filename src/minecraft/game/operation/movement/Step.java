package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;
import minecraft.system.AG;
import minecraft.game.transactions.EventMoving;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.advantage.luvvy.MovementSystem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

@Defuse(name = "Step Block",description = "ХУЙ", brand = Category.Movement)
public class Step extends Module {

    public CheckBoxSetting anchoorHole = new CheckBoxSetting("AnchoorHole", true);
    public CheckBoxSetting reverseStep = new CheckBoxSetting("ReverseStep", false);
    public CheckBoxSetting stepHeight = new CheckBoxSetting("StepHeight", true);
    public ModeSetting stepHeightMode = new ModeSetting("StepMode", "Vanilla", "Vanilla", "Matrix").visibleIf(() -> stepHeight.getValue());

    public static boolean holeTick;
    public static int holeTicks;

    public Step() {
        addSettings(anchoorHole, reverseStep, stepHeight, stepHeightMode);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.player.stepHeight = 0.6f;
    }

    @Subscribe
    public void onMove(EventMoving e) {

        if (anchoorHole.getValue()) {
            holeTick = false;
            float w = mc.player.getWidth() / 2.0f;
            double x = mc.player.getPosX();
            double y = mc.player.getPosY() + (mc.player.lastTickPosY - mc.player.getPosY()) / 2.0;
            double z = mc.player.getPosZ();
            if ((this.isHoledPosFull(new BlockPos(x, y, z)) || this.isHoledPosFull(new BlockPos(x, y - 1.0, z)) || this.isHoledPosFull(new BlockPos(x, y - 1.3, z))) && getBlockFullWithExpand(w, x, y - 1.0, z, Blocks.AIR) && getBlockFullWithExpand(w, x, y - 1.3, z, Blocks.AIR)) {
                mc.player.jumpMovementFactor = 0.0f;
                MovementSystem.setSpeed(0.0f);
                MovementSystem.setCuttingSpeed(0.0);
                if (reverseStep.getValue() && stepHeight.getValue()) {
                    mc.player.motion.y = -3.0;
                    holeTicks = 0;
                } else {
                    holeTicks = -10;
                }
                holeTick = true;
            }
        }
        if (stepHeight.getValue() && stepHeightMode.is("Matrix") && !mc.player.isJumping && mc.player.collidedHorizontally && MovementSystem.isMoving()) {
            double moveYaw = MovementSystem.getMotionYaw();
            double offsetY = 1.001335979112147;
            double extendXZ = 1.0E-5;
            double sin = -Math.sin(Math.toRadians(moveYaw)) * extendXZ;
            double cos = Math.cos(Math.toRadians(moveYaw)) * extendXZ;
            AxisAlignedBB aabb = mc.player.getBoundingBox().offset(0.0, -0.42, 0.0);
            AxisAlignedBB aabbOff = mc.player.getBoundingBox().offset(sin, offsetY, cos);
            if (mc.world.getCollisionShapes(mc.player, aabbOff).toList().isEmpty() && !mc.world.getCollisionShapes(mc.player, aabb).toList().isEmpty()) {
                mc.player.onGround = true;
                mc.player.jump();
            }
        }
        if (reverseStep.getValue() && mc.player.onGround && mc.player.collidedVertically && mc.player.motion.y < 0.0 && !posBlock(mc.player.getPosX(), mc.player.getPosY() - 0.6, mc.player.getPosZ()) && getDistanceTofall() < 4.0 && !mc.player.isJumping) {
            mc.player.motion.y = -3.0;
            holeTicks = 0;
        }
        if (stepHeight.getValue() && stepHeightMode.is("Vanilla")) {
            if (!mc.player.isSneaking() && MovementSystem.moveKeysPressed()) {
                ++holeTicks;
            }
            if (holeTicks > 5 && !mc.player.isSneaking() && MovementSystem.moveKeysPressed()) {
                mc.player.stepHeight = 2.000121f;
            }
        } else if (mc.player.stepHeight == 2.000121f) {
            mc.player.stepHeight = 0.6f;
        }
    }

    public static boolean getBlockFullWithExpand(float expand, double x, double y, double z, Block block) {
        return mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == block && mc.world.getBlockState(new BlockPos(x + (double)expand, y, z + (double)expand)).getBlock() == block && mc.world.getBlockState(new BlockPos(x - (double)expand, y, z - (double)expand)).getBlock() == block && mc.world.getBlockState(new BlockPos(x + (double)expand, y, z - (double)expand)).getBlock() == block && mc.world.getBlockState(new BlockPos(x - (double)expand, y, z + (double)expand)).getBlock() == block && mc.world.getBlockState(new BlockPos(x + (double)expand, y, z)).getBlock() == block && mc.world.getBlockState(new BlockPos(x - (double)expand, y, z)).getBlock() == block && mc.world.getBlockState(new BlockPos(x, y, z + (double)expand)).getBlock() == block && mc.world.getBlockState(new BlockPos(x, y, z - (double)expand)).getBlock() == block;
    }

    public static double getDistanceTofall() {
        for (int i = 0; i < 500; ++i) {
            if (!posBlock(mc.player.getPosX(), mc.player.getPosY() - (double)i, mc.player.getPosZ())) continue;
            return i;
        }
        return 0.0;
    }

    public static boolean posBlock(double x, double y, double z) {
        return mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.AIR && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.WATER && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.LAVA && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.CAKE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.TALL_GRASS && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.STONE_BUTTON && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.FLOWER_POT && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.CHORUS_FLOWER && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.VINE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.ACACIA_FENCE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.ACACIA_FENCE_GATE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.BIRCH_FENCE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.BIRCH_FENCE_GATE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DARK_OAK_FENCE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DARK_OAK_FENCE_GATE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.JUNGLE_FENCE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.JUNGLE_FENCE_GATE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.NETHER_BRICK_FENCE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.OAK_FENCE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.OAK_FENCE_GATE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.SPRUCE_FENCE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.SPRUCE_FENCE_GATE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.ENCHANTING_TABLE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.END_PORTAL_FRAME && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DAYLIGHT_DETECTOR && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.PURPUR_SLAB && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.STONE_SLAB && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.REDSTONE_WIRE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.TORCH && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.REDSTONE_WIRE;
    }

    private Block getBlock(BlockPos position) {
        if (mc.world != null) {
            return mc.world.getBlockState(position).getBlock();
        }
        return Blocks.AIR;
    }

    private boolean isBedrock(BlockPos position) {
        Block state = Blocks.BEDROCK;
        return this.getBlock(position) == state;
    }

    private boolean isObsidian(BlockPos position) {
        Block state = Blocks.OBSIDIAN;
        return this.getBlock(position) == state;
    }

    private boolean isCurrentBlock(BlockPos position) {
        return this.isBedrock(position) || this.isObsidian(position);
    }

    private boolean isHoled(BlockPos position) {
        Block state = Blocks.AIR;
        return this.isCurrentBlock(position.add(1, 0, 0)) && this.isCurrentBlock(position.add(-1, 0, 0)) && this.isCurrentBlock(position.add(0, 0, 1)) && this.isCurrentBlock(position.add(0, 0, -1)) && posBlock(position.add(0, -1, 0).getX(), position.add(0, -1, 0).getY(), position.add(0, -1, 0).getZ()) && this.getBlock(position) == state && this.getBlock(position.add(0, 1, 0)) == state && this.getBlock(position.add(0, 2, 0)) == state;
    }

    private boolean isHoledPosFull(BlockPos pos) {
        return this.isHoled(new BlockPos(pos.getX(), pos.getY(), pos.getZ())) && !posBlock(pos.getX(), pos.getY(), pos.getZ());
    }
}
