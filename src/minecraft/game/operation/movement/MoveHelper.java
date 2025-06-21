package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;
import minecraft.game.transactions.*;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.advantage.advisee.ClientReceive;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.advantage.luvvy.MovementSystem;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.gui.screen.inventory.AnvilScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.*;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SEntityDDNVLCPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

@Defuse(name = "Move Modules",description = "Мувмент хелпер", brand = Category.Movement)
public class MoveHelper extends Module {

    private final List<IPacket<?>> packet = new ArrayList<>();
    public TimeCounterSetting TimeCounterSettingInv = new TimeCounterSetting();
    boolean eActiveNoSlow = false;
    private int tiks;
    private boolean usedNoslow;

    public TimeCounterSetting timerUtil = new TimeCounterSetting();
    public TimeCounterSetting timerUtil1 = new TimeCounterSetting();
    boolean closeMe = false;

    public final CheckBoxSetting dragonFly = new CheckBoxSetting("DragonFly", false);
    public final CheckBoxSetting noSlow = new CheckBoxSetting("NoSlow", true);
    public final ModeSetting noSlowMode = new ModeSetting("NoSlow Mode", "Matrix", "Matrix", "Grim 1.7.4", "Grim Old", "Grim 2.3.70", "Grim Ground").visibleIf(() -> noSlow.getValue());

    public final CheckBoxSetting noWeb = new CheckBoxSetting("NoWeb", false);
    public ModeSetting noWebMode = new ModeSetting("NoWeb Mode", "Motion", "Motion", "Matrix", "Old Grim").visibleIf(() -> noWeb.getValue());
    public SliderSetting jumpMotion = new SliderSetting("Сила прыжка", 0, 0, 10, 0.5f).visibleIf(() -> noWebMode.is("Matrix"));
    public SliderSetting speed = new SliderSetting("Скорость", 1, 0.1f, 2, 0.2f).visibleIf(() -> noWeb.getValue());


    public final CheckBoxSetting levitationControl = new CheckBoxSetting("LevitationControl", false);
    public ModeSetting lcMode = new ModeSetting("LC Mode", "Control", "Remove", "Control").visibleIf(() -> levitationControl.getValue());
    public SliderSetting moveUp = new SliderSetting("Скорость вверх", 1, 1, 5, 0.1f).visibleIf(() -> lcMode.is("Control") && levitationControl.getValue());
    public SliderSetting moveDown = new SliderSetting("Скорость вниз", 1, 1, 5, 0.1f).visibleIf(() -> lcMode.is("Control") && levitationControl.getValue());

    public MoveHelper() {
        addSettings(dragonFly, noSlow, noSlowMode, noWeb, noWebMode, jumpMotion, speed, levitationControl, lcMode, moveUp, moveDown);
    }

    @Subscribe
    public void onEating(EventNoSlow e) {
        handleEventUpdate(e);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (noSlow.getValue()) {
            if (noSlowMode.is("Damage")) {
                if (e.getPacket() instanceof SEntityDDNVLCPacket && ((SEntityDDNVLCPacket)e.getPacket()).getEntityID() == mc.player.getEntityId()) {
                    closeMe = true;
                    timerUtil.reset();
                }

                if (closeMe && timerUtil.hasTimeElapsed(1600, false)) {
                    closeMe = false;
                    timerUtil.reset();
                }
            }
        }
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (levitationControl.getValue()) {
            if (lcMode.is("Control")) {
                if (mc.player.isPotionActive(Effects.LEVITATION)) {
                    int amplifier = mc.player.getActivePotionEffect(Effects.LEVITATION).getAmplifier();
                    if (mc.gameSettings.keyBindJump.pressed)
                        mc.player.motion.y = (((0.05D * (double) (amplifier + 1) - mc.player.motion.y) * 0.2D) * moveUp.getValue());
                    else if (mc.gameSettings.keyBindSneak.pressed)
                        mc.player.motion.y = (-(((0.05D * (double) (amplifier + 1) - mc.player.motion.y) * 0.2D) * moveDown.getValue()));
                    else mc.player.motion.y = (0);
                }
            }
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player.isSneaking()) {
            if (usedNoslow) {
                mc.player.setSneaking(false);
            }
        }
        if (noWeb.getValue()) {
            if (noWebMode.is("Motion")) {
                if (mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ())).getBlock() != Blocks.AIR && mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ())).getBlock() == Blocks.COBWEB) {
                    mc.player.motion.y = 0.0;
                    if (mc.gameSettings.keyBindJump.isKeyDown()) {
                        mc.player.motion.y = 1.2000000476837158;
                    }

                    if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                        mc.player.motion.y = -1.2000000476837158;
                    }

                    MovementSystem.setMotion(speed.getValue());
                }
            }

            if (noWebMode.is("Matrix")) {
                if (mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ())).getBlock() != Blocks.AIR && mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ())).getBlock() == Blocks.COBWEB) {
                    mc.player.motion.y += 2;
                } else if (mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ())).getBlock() == Blocks.COBWEB) {
                    mc.player.motion.y += jumpMotion.getValue();
                    MovementSystem.setSpeed(speed.getValue());
                    mc.gameSettings.keyBindJump.pressed = false;
                }
            }
        }

        if (levitationControl.getValue() && lcMode.is("Remove")) {
            if (mc.player.isPotionActive(Effects.LEVITATION)) {
                mc.player.removeActivePotionEffect(Effects.LEVITATION);
            }
        }
        if (mc.player.isHandActive()) {
            tiks++;
        }
        else {
            tiks = 0;
        }
        if (dragonFly.getValue()) {
            if (mc.player.abilities.isFlying) {
                MovementSystem.setMotion(1.0);
                mc.player.motion.y = 0.0;
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.motion.y = 0.25;
                    if (mc.player.moveForward == 0.0f && !mc.gameSettings.keyBindLeft.isKeyDown() && !mc.gameSettings.keyBindRight.isKeyDown()) {
                        mc.player.motion.y = 0.5;
                    }
                }

                if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.motion.y = -0.25;
                    if (mc.player.moveForward == 0.0f && !mc.gameSettings.keyBindLeft.isKeyDown() && !mc.gameSettings.keyBindRight.isKeyDown()) {
                        mc.player.motion.y = -0.5;
                    }
                }
            }
        }
    }

    private void updateKeyBindingState(KeyBinding[] keyBindings) {
        for (KeyBinding keyBinding : keyBindings) {
            boolean isKeyPressed = InputMappings.isKeyDown(mc.getMainWindow().getHandle(), keyBinding.getDefault().getKeyCode());
            keyBinding.setPressed(isKeyPressed);
        }
    }

    private void handleEventUpdate(EventNoSlow eventNoSlow) {
        if (mc.player.isHandActive() && noSlow.getValue()) {
            switch (noSlowMode.getValue()) {
                case "Grim 1.7.4" -> handleGrimMode(eventNoSlow);
                case "Matrix" -> handleMatrixMode(eventNoSlow);
                case "Grim Ground" -> handleGrimNewMode(eventNoSlow);
                case "Grim Old" -> handleGrimNew(eventNoSlow);
                case "Grim 2.3.70" -> handleGrimTest(eventNoSlow);
                case "FunTime Water" -> funtimWater(eventNoSlow);
                case "Grim Latest" -> handleHWMode(eventNoSlow);
                case "Damage" -> handleDamageMode(eventNoSlow);
            }
        } else {
            eActiveNoSlow = false;
            mc.timer.timerSpeed = 1f;
        }
    }

    private void handleDamageMode(EventNoSlow e) {
        if ((mc.player.isInWater() || closeMe) && mc.player.getActiveHand() == Hand.MAIN_HAND && mc.player.getHeldItemOffhand().getUseAction() == UseAction.NONE) {
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
            e.cancel();
        }

        if (mc.player.getActiveHand() == Hand.OFF_HAND && MovementSystem.isMoving()) {
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            e.cancel();
        }
    }

    private void handleHWMode(EventNoSlow event) {
        boolean mainHandActive;
        boolean offHandActive = mc.player.isHandActive() && mc.player.getActiveHand() == Hand.OFF_HAND;
        boolean bl = mainHandActive = mc.player.isHandActive() && mc.player.getActiveHand() == Hand.MAIN_HAND;
        if ((mc.player.getItemInUseCount() < 28 && mc.player.getItemInUseCount() > 4 || mc.player.getHeldItemOffhand().getItem() == Items.SHIELD) && mc.player.isHandActive() && !mc.player.isPassenger()) {
            mc.playerController.syncCurrentPlayItem();
            if (offHandActive && !mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemOffhand().getItem())) {
                int old = mc.player.inventory.currentItem;
                mc.player.connection.sendPacket(new CHeldItemChangePacket(old + 1 > 8 ? old - 1 : old + 1));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                mc.player.setSprinting(false);
                event.cancel();
            }
            if (mainHandActive && !mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemMainhand().getItem())) {
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                if (mc.player.getHeldItemOffhand().getUseAction().equals((Object)UseAction.NONE)) {
                    event.cancel();
                }
            }
            mc.playerController.syncCurrentPlayItem();
        }
    }

    private void handleMatrixMode(EventNoSlow eventNoSlow) {
        boolean isFalling = (double) mc.player.fallDistance > 0.725;
        float speedMultiplier;
        eventNoSlow.cancel();
        if (mc.player.isOnGround() && !mc.player.movementInput.jump) {
            if (mc.player.ticksExisted % 2 == 0) {
                boolean isNotStrafing = mc.player.moveStrafing == 0.0F;
                speedMultiplier = isNotStrafing ? 0.5F : 0.4F;
                mc.player.motion.x *= speedMultiplier;
                mc.player.motion.z *= speedMultiplier;
            }
        } else if (isFalling) {
            boolean isVeryFastFalling = (double) mc.player.fallDistance > 1.4;
            speedMultiplier = isVeryFastFalling ? 0.95F : 0.97F;
            mc.player.motion.x *= speedMultiplier;
            mc.player.motion.z *= speedMultiplier;
        }
    }

    private void handleGrimNewMode(EventNoSlow e) {
        if (mc.player.isOnGround()) {
            if (mc.player.isHandActive() && tiks < 5) {
                return;
            }
            if (mc.player.isSneaking()) {
                mc.gameSettings.keyBindSneak.pressed = false;
            }
            else {
                mc.gameSettings.keyBindSneak.pressed = true;
            }
            if ((mc.player.getHeldItemOffhand().getUseAction() != UseAction.BLOCK || mc.player.getActiveHand() != Hand.MAIN_HAND) && (mc.player.getHeldItemOffhand().getUseAction() != UseAction.EAT || mc.player.getActiveHand() != Hand.MAIN_HAND)) {
                if (mc.player.getActiveHand() == Hand.MAIN_HAND && tiks > 3) {
                    e.cancel();
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                } else if (tiks > 3) {
                    e.cancel();
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                }
            }
            usedNoslow = true;
        }
    }

    private void funtimWater(EventNoSlow event) {
        if(mc.player.isSwimming() && mc.player.isInWater()) {
            boolean isCurrentlyFalling = mc.player.fallDistance > 0.725;
            float multiplier;

            event.cancel();

            if (mc.player.isOnGround() && !mc.player.movementInput.jump) {
                if (mc.player.ticksExisted % 2 == 0) {
                    boolean isStrafing = mc.player.moveStrafing != 0.0F;
                    multiplier = isStrafing ? 0.4F : 0.5F;
                    mc.player.motion.x *= multiplier;
                    mc.player.motion.z *= multiplier;
                }
            } else if (isCurrentlyFalling) {
                boolean isSpeedyFalling = mc.player.fallDistance > 1.4;
                multiplier = isSpeedyFalling ? 0.95F : 0.97F;
                mc.player.motion.x *= multiplier;
                mc.player.motion.z *= multiplier;
            }
        }
    }

    private void handleGrimMode(EventNoSlow noSlow) {
        boolean offHandActive = mc.player.isHandActive() && mc.player.getActiveHand() == Hand.OFF_HAND;
        boolean mainHandActive = mc.player.isHandActive() && mc.player.getActiveHand() == Hand.MAIN_HAND;
        if (!(mc.player.getItemInUseCount() < 25 && mc.player.getItemInUseCount() > 4) && mc.player.getHeldItemOffhand().getItem() != Items.SHIELD) {
            return;
        }
        if (mc.player.isHandActive() && !mc.player.isPassenger()) {
            mc.playerController.syncCurrentPlayItem();
            if (offHandActive && !mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemOffhand().getItem())) {
                int old = mc.player.inventory.currentItem;
                mc.player.connection.sendPacket(new CHeldItemChangePacket(old + 1 > 8 ? old - 1 : old + 1));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                mc.player.setSprinting(false);
                noSlow.cancel();
            }

            if (mainHandActive && !mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemMainhand().getItem())) {
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));

                if (mc.player.getHeldItemOffhand().getUseAction().equals(UseAction.NONE)) {
                    noSlow.cancel();
                }
            }
            mc.playerController.syncCurrentPlayItem();
        }
    }
    private void handleGrimNew(EventNoSlow e) {
        if ((mc.player.getHeldItemOffhand().getUseAction() != UseAction.BLOCK || mc.player.getActiveHand() != Hand.MAIN_HAND) && (mc.player.getHeldItemOffhand().getUseAction() != UseAction.EAT || mc.player.getActiveHand() != Hand.MAIN_HAND)) {
            if (mc.player.getActiveHand() == Hand.MAIN_HAND) {
                e.cancel();
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
            } else {
                e.cancel();
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            }
        }
    }
    private void handleGrimTest(EventNoSlow e) {
        if (tiks > 1) {
            if ((mc.player.getHeldItemOffhand().getUseAction() != UseAction.BLOCK || mc.player.getActiveHand() != Hand.MAIN_HAND) && (mc.player.getHeldItemOffhand().getUseAction() != UseAction.EAT || mc.player.getActiveHand() != Hand.MAIN_HAND)) {
                if (mc.player.getActiveHand() == Hand.MAIN_HAND) {
                    e.cancel();
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                } else {
                    e.cancel();
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                }
            }
        }
        if (tiks < 1) {
            mc.gameSettings.keyBindSneak.pressed = true;
        }
        else {
            mc.gameSettings.keyBindSneak.pressed = false;
        }
    }

    private void sendItemChangePacket() {
        ItemStack stack;
        if (!MovementSystem.isMoving()) {
            return;
        }
        int slot = 0;
        do {
            if (++slot == mc.player.inventory.currentItem) {
                ++slot;
            }
            if (slot <= 8) continue;
            slot = -1;
            break;
        } while ((stack = mc.player.inventory.getStackInSlot(slot)).getItem().getUseAction(stack) == UseAction.NONE);
        if (slot == -1) {
            return;
        }
        mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
        mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
    }

    private boolean canCancel() {
        boolean isHandActive = mc.player.isHandActive();
        boolean isLevitation = mc.player.isPotionActive(Effects.LEVITATION);
        boolean isOnGround = mc.player.isOnGround();
        boolean isJumpPressed = mc.gameSettings.keyBindJump.pressed;
        boolean isElytraFlying = mc.player.isElytraFlying();

        if (isLevitation || isElytraFlying) {
            return false;
        }

        return (isOnGround || isJumpPressed) && isHandActive;
    }
}
