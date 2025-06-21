package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;

import minecraft.system.AG;
import minecraft.game.transactions.*;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.combat.DDCOLDWN;
import minecraft.game.operation.wamost.massa.elements.BindSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.advantage.advisee.ClientReceive;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.advantage.luvvy.InventoryOrigin;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Defuse(name = "Click Pearl",description = "Бросает пёрку по кнопке", brand = Category.Player)
public class DDCLCKPERL extends Module {
    final ModeSetting mode = new ModeSetting("Тип", "Обычный", "Обычный", "Легитный");
    final BindSetting pearlKey = new BindSetting("Кнопка", -98);
    final InventoryOrigin.Hand handUtil = new InventoryOrigin.Hand();
    final DDCOLDWN DDCOLDWN;
    long delay;
    final TimeCounterSetting waitMe = new TimeCounterSetting();
    final TimeCounterSetting TimeCounterSetting = new TimeCounterSetting();
    final TimeCounterSetting TimeCounterSetting2 = new TimeCounterSetting();
    public ActionType actionType = ActionType.START;
    Runnable runnableAction;
    int oldSlot = -1;

    public DDCLCKPERL(DDCOLDWN DDCOLDWN) {
        this.DDCOLDWN = DDCOLDWN;
        addSettings(mode, pearlKey);
    }

    @Subscribe
    public void onKey(EventKey e) {
        if (e.getKey() == pearlKey.getValue()) {
            if (!mc.player.getCooldownTracker().hasCooldown(Items.ENDER_PEARL)) {
                final KeyBinding[] pressedKeys = {mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump, mc.gameSettings.keyBindSprint};
                if (ClientReceive.isConnectedToServer("funtime")) {
                    if (!waitMe.isReached(400)) {
                        for (KeyBinding keyBinding : pressedKeys) {
                            keyBinding.setPressed(false);
                        }
                        return;
                    }
                }

                sendRotatePacket();

                oldSlot = mc.player.inventory.currentItem;

                if (mode.is("Обычный")) {
                    InventoryOrigin.inventorySwapClick(Items.ENDER_PEARL, true);
                } else {
                    if (runnableAction == null) {
                        actionType = ActionType.START;
                        runnableAction = () -> vebatSoli();
                        TimeCounterSetting.reset();
                        TimeCounterSetting2.reset();
                    }
                }
            } else {
                DDCOLDWN.ItemEnum itemEnum = minecraft.game.operation.combat.DDCOLDWN.ItemEnum.getItemEnum(Items.ENDER_PEARL);

                if (DDCOLDWN.isEnabled() && itemEnum != null && DDCOLDWN.isCurrentItem(itemEnum)) {
                    DDCOLDWN.lastUseItemTime.put(itemEnum.getItem(), System.currentTimeMillis());
                }
            }
        }
    }

    @Subscribe
    public void onTick(EventTick e) {
        if (runnableAction != null) {
            runnableAction.run();
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        this.handUtil.onEventPacket(e);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private void vebatSoli() {
        int slot = InventoryOrigin.getInstance().getSlotInInventoryOrHotbar(Items.ENDER_PEARL, true);
        Hand hand = mc.player.getHeldItemOffhand().getItem() instanceof EnderPearlItem ? Hand.OFF_HAND : Hand.MAIN_HAND;

        if (slot != -1) {
            interact(slot, hand);
        } else {
            runnableAction = null;
        }
    }

    private void swingAndSendPacket(Hand hand) {
        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(hand));
        mc.player.swingArm(hand);
    }

    private void interact(Integer slot, Hand hand) {
        if (actionType == ActionType.START) { // начало
            switchSlot(slot, hand);
            actionType = ActionType.WAIT;
        } else if (actionType == ActionType.WAIT && TimeCounterSetting.isReached(50L)) { // какая та хуйня
            actionType = ActionType.USE_ITEM;
        } else if (actionType == ActionType.USE_ITEM) {
            sendRotatePacket();
            swingAndSendPacket(hand);
            switchSlot(mc.player.inventory.currentItem, hand);
            actionType = ActionType.SWAP_BACK;
        } else if (actionType == ActionType.SWAP_BACK && TimeCounterSetting2.isReached(300L)) { // задержка на свап обратно
            mc.player.inventory.currentItem = oldSlot;
            runnableAction = null;
        }
    }

    private void switchSlot(int slot, Hand hand) {
        if (slot != mc.player.inventory.currentItem && hand != Hand.OFF_HAND) {
            mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
            mc.player.inventory.currentItem = slot;
        }
    }

    private void sendRotatePacket() {
        if (AG.getInst().getModuleManager().getDDATTACK().getTarget() != null) {
            mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()));
        }
    }

    public enum ActionType {
        START, WAIT, USE_ITEM, SWAP_BACK
    }
}
