package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;

import minecraft.game.transactions.EventMotion;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.BindSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.operation.wamost.massa.elements.StringSetting;
import minecraft.game.advantage.advisee.KeyStorage;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.advantage.luvvy.PotionModeSettings;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

@Defuse(name = "Auto Fix",description = "Автоматически чинит броню", brand = Category.Misc)
public class DDFIX extends Module {

    public ModeSetting mode = new ModeSetting("Тип починки", "Пузырьки", "Пузырьки", "Команда");
    public StringSetting name = new StringSetting("Команда для починки", "/fix all", "Укажите текст для починки").visibleIf(() -> mode.is("Команда"));
    public BindSetting bind = new BindSetting("Кнопка", -1).visibleIf(() -> mode.is("Пузырьки"));
    public SliderSetting delay = new SliderSetting("Задержка", 50, 0, 500, 1).visibleIf(() -> mode.is("Пузырьки"));

    private final TimeCounterSetting TimeCounterSetting = new TimeCounterSetting();
    private final TimeCounterSetting throwDelay = new TimeCounterSetting();
    private float previousPitch;
    private int selectedSlot;
    private final PotionModeSettings PotionModeSettings = new PotionModeSettings();

    public DDFIX() {
        addSettings(mode, name, bind, delay);
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (!mode.is("Пузырьки")) return;

        if (mc.currentScreen != null) return;
        if (isNotPressed()) return;

        if (this.isNotThrowing()) {
            return;
        }
        if (checkFixInv().equals(ItemStack.EMPTY) || (getPotionIndexInv() == -1 && getPotionIndexHb() == -1)) {
            return;
        }

        this.previousPitch = 90.0F;
        e.setPitch(this.previousPitch);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mode.is("Команда")) {
            if (TimeCounterSetting.isReached(1000) && checkFix(mc.player.getHeldItemMainhand())) {
                print(name.getValue());
                TimeCounterSetting.reset();
            }
        }

        if (mode.is("Пузырьки")) {
            if (mc.currentScreen != null) return;
            if (isNotPressed()) return;
            if (!checkFixInv().equals(ItemStack.EMPTY)) {
                if (!this.isNotThrowing() && previousPitch == mc.player.lastReportedPitch) {
                    if (throwDelay.isReached(delay.getValue().intValue())) {
                        int oldItem = mc.player.inventory.currentItem;
                        this.selectedSlot = -1;

                        int slot = this.setSlotAndUseItem();
                        if (this.selectedSlot == -1) {
                            this.selectedSlot = slot;
                        }

                        if (this.selectedSlot > 8) {
                            mc.playerController.pickItem(this.selectedSlot);
                        }

                        mc.player.connection.sendPacket(new CHeldItemChangePacket(oldItem));
                        throwDelay.reset();
                    }
                }

                if (TimeCounterSetting.isReached(500L)) {
                    try {
                        this.selectedSlot = -2;
                    } catch (Exception ignored) {
                    }
                }

                this.PotionModeSettings.changeItemSlot(this.selectedSlot == -2);
            }
        }
    }

    private boolean isNotPressed() {
        return !KeyStorage.isKeyDown(bind.getValue());
    }

    private int setSlotAndUseItem() {
        int hbSlot = this.getPotionIndexHb();
        if (hbSlot != -1) {
            this.PotionModeSettings.setPreviousSlot(mc.player.inventory.currentItem);
            mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            PotionModeSettings.useItem(Hand.MAIN_HAND);
            TimeCounterSetting.reset();
            return hbSlot;
        } else {
            int invSlot = this.getPotionIndexInv();
            if (invSlot != -1) {
                this.PotionModeSettings.setPreviousSlot(mc.player.inventory.currentItem);
                mc.playerController.pickItem(invSlot);
                PotionModeSettings.useItem(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                TimeCounterSetting.reset();
                return invSlot;
            } else {
                return -1;
            }
        }
    }

    public boolean isNotThrowing() {
        return !(mc.player.isOnGround() || mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getBoundingBox().minY - 0.3f, mc.player.getPosZ())).isSolid()) || mc.player.isOnLadder() || mc.player.getRidingEntity() != null || mc.player.abilities.isFlying;
    }


    private int getPotionIndexHb() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.EXPERIENCE_BOTTLE) return i;
        }

        return -1;
    }

    private int getPotionIndexInv() {
        for (int i = 9; i < 36; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.EXPERIENCE_BOTTLE) return i;
        }

        return -1;
    }

    private boolean checkFix(ItemStack item) {
        return item.getMaxDamage() != 0 && ((item.getMaxDamage() - item.getDamage()) <= 3);
    }

    private ItemStack checkFixInv() {
        for (ItemStack stack : mc.player.getArmorInventoryList()) {
            if (stack.isEmpty()) continue;
            boolean mending = false;
            if (stack.isEnchanted()) {
                for (int j = 0; j < stack.getEnchantmentTagList().size(); j++) {
                    String stackItemEnchant = stack.getEnchantmentTagList().getCompound(j).getString("id").replaceAll("minecraft:", "");
                    if (stackItemEnchant.equalsIgnoreCase("mending")) {
                        mending = true;
                        break;
                    }
                }
            }
            if (stack.getMaxDamage() != 0 && stack.getDamage() != 0 && mending) return stack;
        }
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            boolean mending = false;
            if (stack.isEnchanted()) {
                for (int j = 0; j < stack.getEnchantmentTagList().size(); j++) {
                    String stackItemEnchant = stack.getEnchantmentTagList().getCompound(j).getString("id").replaceAll("minecraft:", "");
                    if (stackItemEnchant.equalsIgnoreCase("mending")) {
                        mending = true;
                        break;
                    }
                }
            }
            if (stack.getMaxDamage() != 0 && stack.getDamage() != 0 && mending) return stack;
        }
        return ItemStack.EMPTY;
    }
    
}
