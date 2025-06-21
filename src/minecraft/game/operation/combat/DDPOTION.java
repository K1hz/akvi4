package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;

import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.transactions.EventMotion;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.advantage.luvvy.MovementSystem;
import minecraft.game.advantage.luvvy.PotionModeSettings;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.potion.*;
import net.minecraft.util.Hand;

@Defuse(name = "Potion Thrower",description = "Автоматически бросает зелья под игрока", brand = Category.Player)
public class DDPOTION extends Module {
    public CheckBoxSetting intave = new CheckBoxSetting("обход IntaveAC", true);
    private float previousPitch;
    public boolean isActive, isActivePotion;
    private int selectedSlot;
    private final TimeCounterSetting TimeCounterSetting = new TimeCounterSetting();
    private final PotionModeSettings PotionModeSettings = new PotionModeSettings();

    public DDPOTION() {
        this.addSettings(this.intave);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (this.isActive() || !MovementSystem.isBlockUnder(0.5f) || mc.player.isOnGround()) {
            for (PotionType potionType : PotionType.values()) {
                this.isActivePotion = potionType.isEnabled();
            }
        } else {
            this.isActivePotion = false;
        }
        if (this.isActive() && this.previousPitch == mc.player.getLastReportedPitch()) {
            int oldItem = mc.player.inventory.currentItem;
            this.selectedSlot = -1;
            for (PotionType potionType : PotionType.values()) {
                if (!potionType.isEnabled()) continue;
                int slot = this.findPotionSlot(potionType);
                if (this.selectedSlot == -1) {
                    this.selectedSlot = slot;
                }
                this.isActive = true;
            }
            if (this.selectedSlot > 8) {
                mc.playerController.pickItem(this.selectedSlot);
            }
            mc.player.connection.sendPacket(new CHeldItemChangePacket(oldItem));
        }
        if (this.TimeCounterSetting.isReached(500L)) {
            try {
                this.reset();
                this.selectedSlot = -2;
            } catch (Exception exception) {
            }
        }
        this.PotionModeSettings.changeItemSlot(this.selectedSlot == -2);
    }

    @Subscribe
    private void onMotion(EventMotion e) {
        if (!this.isActive() || MovementSystem.isBlockUnder(0.5f) || !mc.player.isOnGround()) {
            return;
        }
        float[] angles = new float[]{mc.player.rotationYaw, 90.0f};
        this.previousPitch = 90.0f;
        e.setYaw(angles[0]);
        e.setPitch(this.previousPitch);
        mc.player.rotationPitchHead = this.previousPitch;
        mc.player.rotationYawHead = angles[0];
        mc.player.renderYawOffset = angles[0];
    }

    private void reset() {
        for (PotionType potionType : PotionType.values()) {
            potionType.setEnabled(this.isPotionActive(potionType));
        }
    }

    private int findPotionSlot(PotionType type) {
        int hbSlot = this.getPotionIndexHb(type.getId());
        if (hbSlot != -1) {
            this.PotionModeSettings.setPreviousSlot(mc.player.inventory.currentItem);
            mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            PotionModeSettings.useItem(Hand.MAIN_HAND);
            type.setEnabled(false);
            this.TimeCounterSetting.reset();
            return hbSlot;
        }
        int invSlot = this.getPotionIndexInv(type.getId());
        if (invSlot != -1) {
            this.PotionModeSettings.setPreviousSlot(mc.player.inventory.currentItem);
            mc.playerController.pickItem(invSlot);
            PotionModeSettings.useItem(Hand.MAIN_HAND);
            mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            type.setEnabled(false);
            this.TimeCounterSetting.reset();
            return invSlot;
        }
        return -1;
    }

    public boolean isActive() {
        for (PotionType potionType : PotionType.values()) {
            if (!potionType.isEnabled()) continue;
            return true;
        }
        return false;
    }

    private boolean isPotionActive(PotionType type) {
        if (mc.player.isPotionActive(type.getPotion())) {
            this.isActive = false;
            return false;
        }
        return this.getPotionIndexInv(type.getId()) != -1 || this.getPotionIndexHb(type.getId()) != -1;
    }

    private int getPotionIndexHb(int id) {
        for (int i = 0; i < 9; ++i) {
            for (EffectInstance potion : PotionUtils.getEffectsFromStack(mc.player.inventory.getStackInSlot(i))) {
                if (potion.getPotion() != Effect.get(id) || mc.player.inventory.getStackInSlot(i).getItem() != Items.SPLASH_POTION) continue;
                return i;
            }
        }
        return -1;
    }

    private int getPotionIndexInv(int id) {
        for (int i = 9; i < 36; ++i) {
            for (EffectInstance potion : PotionUtils.getEffectsFromStack(mc.player.inventory.getStackInSlot(i))) {
                if (potion.getPotion() != Effect.get(id) || mc.player.inventory.getStackInSlot(i).getItem() != Items.SPLASH_POTION) continue;
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onDisable() {
        this.isActive = false;
        super.onDisable();
    }

    public enum PotionType {
        STRENGTH(Effects.STRENGTH, 5),
        SPEED(Effects.SPEED, 1),
        FIRE_RESIST(Effects.FIRE_RESISTANCE, 12);

        private final Effect potion;
        private final int id;
        private boolean enabled;

        private PotionType(Effect potion, int potionId) {
            this.potion = potion;
            this.id = potionId;
        }

        public Effect getPotion() {
            return this.potion;
        }

        public int getId() {
            return this.id;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean var1) {
            this.enabled = var1;
        }
    }
}
