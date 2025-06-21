package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;

import minecraft.game.advantage.luvvy.*;
import minecraft.system.AG;
import minecraft.game.transactions.*;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.combat.DDATTACK;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoulSandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Random;


@Defuse(name = "Strafe",description = "Добавляет возможность стрейфится в воздухе", brand = Category.Movement)
public class Strafe extends Module {
    private final ModeSetting mode = new ModeSetting("Обход", "Matrix Hard", "Matrix", "Matrix Hard");
    private final CheckBoxSetting elytra = new CheckBoxSetting("Буст с элитрой", false);
    private final SliderSetting setSpeed = new SliderSetting("Скорость", 1.5F, 0.5F, 2.5F, 0.1F).visibleIf(() -> elytra.getValue());
    private final CheckBoxSetting damageBoost = new CheckBoxSetting("Буст с дамагом", false);
    private final SliderSetting boostSpeed = new SliderSetting("Значение буста", 0.7f, 0.1F, 5.0f, 0.1F).visibleIf(() -> damageBoost.getValue());

    private final CheckBoxSetting onlyGround = new CheckBoxSetting("Только на земле", false).visibleIf(() -> mode.is("Matrix Hard"));
    private final CheckBoxSetting autoJump = new CheckBoxSetting("Прыгать", false);
    private final CheckBoxSetting moveDir = new CheckBoxSetting("Направление", true);
    private final CheckBoxSetting sprintType = new CheckBoxSetting("Использовать спринт", true).visibleIf(() -> mode.is("Matrix Hard"));;

    private final SelfDamagePlayer damageUtil = new SelfDamagePlayer();
    private final StrafeMovement strafeMovement = new StrafeMovement();
    private final TargetStrafe targetStrafe;
    private final DDATTACK DDATTACK;
    public static int waterTicks;

    public boolean check() {
        return AG.getInst().getModuleManager().getDDATTACK().getTarget() != null && AG.getInst().getModuleManager().getDDATTACK().isEnabled();
    }

    public Strafe(TargetStrafe targetStrafe, DDATTACK DDATTACK) {
        this.targetStrafe = targetStrafe;
        this.DDATTACK = DDATTACK;
        addSettings(mode, elytra, setSpeed, damageBoost, boostSpeed, onlyGround, autoJump, moveDir,sprintType);
    }

    @Subscribe
    private void onAction(ActionEvent e) {
        if (AG.getInst().getModuleManager().getDDFRCM().isEnabled()) return;
        handleEventAction(e);
    }

    @Subscribe
    private void onMoving(EventMoving e) {
        if (AG.getInst().getModuleManager().getDDFRCM().isEnabled()) return;
        handleEventMove(e);
    }

    @Subscribe
    private void onPostMove(EventPostMove e) {
        if (AG.getInst().getModuleManager().getDDFRCM().isEnabled()) return;
        handleEventPostMove(e);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        if (AG.getInst().getModuleManager().getDDFRCM().isEnabled()) return;
        handleEventPacket(e);
    }

    @Subscribe
    private void onDamage(EventDamageReceive e) {
        if (AG.getInst().getModuleManager().getDDFRCM().isEnabled()) return;
        handleDamageEvent(e);
    }

    @Subscribe
    public void onMotion(EventMotion e) {

        if (AG.getInst().getModuleManager().getDDFRCM().isEnabled()) return;

    }


    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (AG.getInst().getModuleManager().getDDFRCM().isEnabled()) return;

        if (autoJump.getValue()) {
            if (mc.player.isOnGround() && !mc.player.isInWater() && !mc.player.isInLava()) {
                mc.player.jump();
            }
        }

        if (!elytra.getValue()) return;
        int elytra = InventoryOrigin.getInstance().getHotbarSlotOfItem();

        if (mc.player.isInWater() || mc.player.isInLava() || waterTicks > 0 || elytra == -1)
            return;
        if (mc.player.fallDistance != 0 && mc.player.fallDistance < 0.1 && mc.player.motion.y < -0.1) {
            if (elytra != -2) {
                mc.playerController.windowClick(0, elytra, 1, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, mc.player);
            }
            mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
            mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));

            if (elytra != -2) {
                mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, elytra, 1, ClickType.PICKUP, mc.player);
            }
        }
    }

    private void handleDamageEvent(EventDamageReceive damage) {
        if (damageBoost.getValue()) {
            damageUtil.processDamage(damage);
        }
    }

    private void handleEventAction(ActionEvent action) {
        if  (mode.is("Matrix Hard")) {
            if (strafes()) {
                handleStrafesEventAction(action);
            }
            if (strafeMovement.isNeedSwap()) {
                handleNeedSwapEventAction(action);
            }
        }
    }

    private void handleEventMove(EventMoving eventMove) {
        int elytraSlot = InventoryOrigin.getInstance().getHotbarSlotOfItem();

        if (elytra.getValue() && elytraSlot != -1) {
            if (MovementSystem.isMoving() && !mc.player.onGround && mc.player.fallDistance >= 0.15 && eventMove.isToGround()) {
                MovementSystem.setMotion(setSpeed.getValue());
                strafeMovement.setOldSpeed(setSpeed.getValue() / 1.06);
            }
        }

        if (mc.player.isInWater() || mc.player.isInLava()) {
            waterTicks = 10;
        } else {
            waterTicks--;
        }
        if (mode.is("Matrix Hard")) {
            if (onlyGround.getValue())
                if (!mc.player.isOnGround()) return;

            if (strafes()) {
                handleStrafesEventMove(eventMove);
            } else {
                strafeMovement.setOldSpeed(0);
            }
        }

        if (mode.is("Matrix")) {
            if (waterTicks > 0) return;
            if (MovementSystem.isMoving() && MovementSystem.getMotion() <= 0.289385188) {
                if (!eventMove.isToGround()) {
                    MovementSystem.setStrafe(MovementSystem.reason(false) || mc.player.isHandActive() ? MovementSystem.getMotion() - 0.00001f : 0.245f - (new Random().nextFloat() * 0.000001f));
                }
            }
        }
    }

    private void handleEventPostMove(EventPostMove eventPostMove) {
        strafeMovement.postMove(eventPostMove.getHorizontalMove());
    }

    private void handleEventPacket(EventPacket packet) {

        if (packet.getType() == EventPacket.Type.RECEIVE) {
            if (damageBoost.getValue()) {
                damageUtil.onPacketEvent(packet);
            }
            handleReceivePacketEventPacket(packet);
        }
    }

    private void handleStrafesEventAction(ActionEvent action) {
        if (sprintType.getValue()) {
            if (CEntityActionPacket.lastUpdatedSprint != strafeMovement.isNeedSprintState()) {
                action.setSprintState(!CEntityActionPacket.lastUpdatedSprint);
            }
        }
    }

    private void handleStrafesEventMove(EventMoving eventMove) {
        if (targetStrafe.isEnabled() && (DDATTACK.isEnabled() && DDATTACK.getTarget() != null)) {
            return;
        }

        if (damageBoost.getValue())
            this.damageUtil.time(700L);

        final float damageSpeed = boostSpeed.getValue().floatValue() / 10.0F;
        final double speed = strafeMovement.calculateSpeed(eventMove, damageBoost.getValue(), damageUtil.isNormalDamage(), false, damageSpeed);

        MovementSystem.MoveEvent.setMoveMotion(eventMove, speed);
    }

    private void handleNeedSwapEventAction(ActionEvent action) {
        if (sprintType.getValue()) {
            action.setSprintState(!mc.player.serverSprintState);
            strafeMovement.setNeedSwap(false);
        }
    }

    private void handleReceivePacketEventPacket(EventPacket packet) {
        if (packet.getPacket() instanceof SPlayerPositionLookPacket) {
            strafeMovement.setOldSpeed(0);
        }

    }

    public boolean strafes() {
        if (isInvalidPlayerState()) {
            return false;
        }

        if (mc.player.isInWater() || waterTicks > 0) {
            return false;
        }

        BlockPos playerPosition = new BlockPos(mc.player.getPositionVec());
        BlockPos abovePosition = playerPosition.up();
        BlockPos belowPosition = playerPosition.down();

        if (isSurfaceLiquid(abovePosition, belowPosition)) {
            return false;
        }

        if (isPlayerInWebOrSoulSand(playerPosition)) {
            return false;
        }

        return isPlayerAbleToStrafe();
    }

    private boolean isInvalidPlayerState() {
        return mc.player == null || mc.world == null
                || mc.player.isSneaking()
                || mc.player.isElytraFlying()
                || mc.player.isInWater()
                || mc.player.isInLava();
    }

    private boolean isSurfaceLiquid(BlockPos abovePosition, BlockPos belowPosition) {
        Block aboveBlock = mc.world.getBlockState(abovePosition).getBlock();
        Block belowBlock = mc.world.getBlockState(belowPosition).getBlock();

        return aboveBlock instanceof AirBlock && belowBlock == Blocks.WATER;
    }

    private boolean isPlayerInWebOrSoulSand(BlockPos playerPosition) {
        Material playerMaterial = mc.world.getBlockState(playerPosition).getMaterial();
        Block oneBelowBlock = mc.world.getBlockState(playerPosition.down()).getBlock();

        return playerMaterial == Material.WEB || oneBelowBlock instanceof SoulSandBlock;
    }
    @Subscribe
    private void onWalking(EventMotion e) {

        float yaw = MovementSystem.moveYaw(mc.player.rotationYaw);

        if (this.moveDir.getValue()) {
            e.setYaw(yaw);
            DDATTACK.mc.player.rotationYawHead = yaw;
            mc.player.renderYawOffset = PlayerSettingsModule.calculateCorrectYawOffset(yaw);
        }
    }
    private boolean isPlayerAbleToStrafe() {
        return !mc.player.abilities.isFlying && !mc.player.isPotionActive(Effects.LEVITATION);
    }

    @Override
    public void onEnable() {
        strafeMovement.setOldSpeed(0);
        super.onEnable();
        betterComp(AG.getInst().getModuleManager().getSpeed());
    }
}