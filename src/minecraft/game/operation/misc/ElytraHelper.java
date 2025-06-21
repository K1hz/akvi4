package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.*;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.system.AG;
import minecraft.game.transactions.EventKey;
import minecraft.game.transactions.EventPacket;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.BindSetting;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.display.clientnotify.most.NotifyManager;
import minecraft.game.advantage.advisee.ClientReceive;
import minecraft.game.advantage.figures.StopWatch;
import minecraft.game.advantage.luvvy.InventoryOrigin;

import javax.management.Notification;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Defuse(name = "Elytra Helper",description = "Помогает с элитрой", brand = Category.Player)
public class ElytraHelper extends Module {

    final BindSetting swapChestKey = new BindSetting("Кнопка свапа", -1);
    final BindSetting fireWorkKey = new BindSetting("Кнопка феера", -1);
    final CheckBoxSetting autoFireWork = new CheckBoxSetting("Авто феерверк", true);
    final SliderSetting timerFireWork = new SliderSetting("Таймер феера", 400, 10, 2000, 10).visibleIf(() -> autoFireWork.getValue());
    final CheckBoxSetting autoFly = new CheckBoxSetting("Авто взлёт", true);
    final CheckBoxSetting onlyNotPVP = new CheckBoxSetting("Не использовать в PVP", true);

    final InventoryOrigin.Hand handUtil = new InventoryOrigin.Hand();

    public ElytraHelper() {
        addSettings(swapChestKey, fireWorkKey, autoFireWork, timerFireWork, autoFly, onlyNotPVP);
    }

    ItemStack currentStack = ItemStack.EMPTY;
    public static TimeCounterSetting fireWorkTimeCounterSetting = new TimeCounterSetting();
    public static StopWatch stopWatch = new StopWatch();
    public static StopWatch stopWatch1 = new StopWatch();
    long delay;
    boolean fireworkUsed;

    @Subscribe
    private void onEventKey(EventKey e) {

        if (isNotPvP()) {
            return;
        }


        if (e.getKey() == swapChestKey.getValue() && stopWatch.isReached(0)) {
            changeChestPlate(currentStack);
        }

        if (e.getKey() == fireWorkKey.getValue() && stopWatch.isReached(0L)) {
            fireworkUsed = true;
            stopWatch.reset();
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        ItemStack mouseStack = mc.player.inventory.getItemStack();



        this.currentStack = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);

        if (isNotPvP()) {
            return;
        }
        if (((Boolean)this.autoFly.getValue()).booleanValue() && this.currentStack.getItem() == Items.ELYTRA) {
            if (ElytraHelper.mc.player.isOnGround()) {
                ElytraHelper.mc.player.jump();
            } else if (ElytraItem.isUsable(this.currentStack) && !ElytraHelper.mc.player.isElytraFlying() && !ElytraHelper.mc.player.abilities.isFlying) {
                ElytraHelper.mc.player.startFallFlying();
                ElytraHelper.mc.player.connection.sendPacket(new CEntityActionPacket(ElytraHelper.mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
            }
        }

        if (mc.player.isElytraFlying() && autoFireWork.getValue() && !(mc.player.isHandActive() && mc.player.getHeldItemMainhand().getUseAction() == UseAction.EAT)) {
            if (fireWorkTimeCounterSetting.isReached(timerFireWork.getValue().longValue())) {
                InventoryOrigin.inventorySwapClick(Items.FIREWORK_ROCKET, false);
                fireWorkTimeCounterSetting.reset();
            }
        }

        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);

        if (fireworkUsed) {
            int hbSlot = InventoryOrigin.getInstance().getSlotInInventoryOrHotbar(Items.FIREWORK_ROCKET, true);
            int invSlot = InventoryOrigin.getInstance().getSlotInInventoryOrHotbar(Items.FIREWORK_ROCKET, false);


            if (invSlot == -1 && hbSlot == -1) {
                fireworkUsed = false;
                return;
            }

            int slot = findAndTrowItem(hbSlot, invSlot);
            if (slot > 8) {
                mc.playerController.pickItem(slot);
            }

            fireworkUsed = false;
        }
        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);

    }

    public boolean isNotPvP() {
        return onlyNotPVP.getValue() && ClientReceive.isPvP();
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        handUtil.onEventPacket(e);
    }

    private int findAndTrowItem(int hbSlot, int invSlot) {
        if (hbSlot != -1) {
            this.handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            if (hbSlot != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            }

            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));

            if (hbSlot != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            }
            this.delay = System.currentTimeMillis();
            return hbSlot;
        }
        if (invSlot != -1) {
            handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            mc.playerController.pickItem(invSlot);
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            this.delay = System.currentTimeMillis();
            return invSlot;
        }
        return -1;
    }


    private void changeChestPlate(ItemStack stack) {
        if (mc.currentScreen != null) {
            return;
        }

        if (stack.getItem() != Items.ELYTRA) {
            int elytraSlot = getItemSlot(Items.ELYTRA);
            if (elytraSlot >= 0) {
                InventoryOrigin.moveItem(elytraSlot, 6);
                return;
            } else {
            }
        }
        int armorSlot = getChestPlateSlot();
        if (armorSlot >= 0 && stack.getItem() == Items.ELYTRA) {
            InventoryOrigin.moveItem(armorSlot, 6);
        } else {
        }
    }


    private int getChestPlateSlot() {
        Item[] items = {Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.IRON_CHESTPLATE, Items.LEATHER_CHESTPLATE};

        for (Item item : items) {
            for (int i = 0; i < 36; ++i) {
                Item stack = mc.player.inventory.getStackInSlot(i).getItem();
                if (stack == item) {
                    if (i < 9) {
                        i += 36;
                    }
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void onDisable() {
        stopWatch.reset();
        super.onDisable();
    }

    private int getItemSlot(Item input) {
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() == input) {
                slot = i;
                break;
            }
        }
        if (slot < 9 && slot != -1) {
            slot = slot + 36;
        }
        return slot;
    }
}
