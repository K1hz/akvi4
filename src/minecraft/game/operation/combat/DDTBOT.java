package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;

import minecraft.system.managers.friend.FriendManager;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.advantage.luvvy.AttackSystem;
import minecraft.game.advantage.luvvy.InventoryOrigin;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

@Defuse(name = "Triggеr Воt",description = "Автоматически бьет по сущности при наводке", brand = Category.Combat)
public class DDTBOT extends Module {

    private final CheckBoxSetting onlyCrit = new CheckBoxSetting("Только криты", true);
    private final CheckBoxSetting smartCrit = new CheckBoxSetting("Умные криты", true).visibleIf(() -> onlyCrit.getValue());
    private final CheckBoxSetting players = new CheckBoxSetting("Игроки", true);
    private final CheckBoxSetting mobs = new CheckBoxSetting("Мобы", false);
    private final CheckBoxSetting animals = new CheckBoxSetting("Животные", false);
    private final CheckBoxSetting shieldBreak = new CheckBoxSetting("Ломать щит", true);
    private final CheckBoxSetting tpsSync = new CheckBoxSetting("TPSSync", false);

    public DDTBOT() {
        addSettings(onlyCrit, smartCrit, players, mobs, animals, shieldBreak, tpsSync);
    }

    private final TimeCounterSetting TimeCounterSetting = new TimeCounterSetting();
    private final TimeCounterSetting timerForTarget = new TimeCounterSetting();
    @Getter
    LivingEntity target = null;


    @Subscribe
    public void onUpdate(EventUpdate e) {
        Entity entity = getValidEntity();

        target = (LivingEntity) entity;

        if (entity == null || mc.player == null) {
            return;
        }

        if (shouldAttack()) {
            TimeCounterSetting.setLastMS(500);
            attackEntity(entity);
        }
    }

    private boolean shouldAttack() {
        if (!this.smartCrit.getValue()) {
            return AttackSystem.isPlayerFalling(onlyCrit.getValue(), false, false) && (TimeCounterSetting.hasTimeElapsed());
        }
            return AttackSystem.isPlayerFalling(onlyCrit.getValue(), true, false) && (TimeCounterSetting.hasTimeElapsed());
    }

    private void attackEntity(Entity entity) {
        boolean shouldStopSprinting = false;
        mc.playerController.attackEntity(mc.player, entity);
        mc.player.swingArm(Hand.MAIN_HAND);
        if (shieldBreak.getValue() && entity instanceof PlayerEntity)
            breakShieldPlayer(entity);
    }

    private Entity getValidEntity() {
        if (mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY) {
            Entity entity = ((EntityRayTraceResult) mc.objectMouseOver).getEntity();
            if (checkEntity((LivingEntity) entity)) {
                return entity;
            }
        }
        return null;
    }

    public static void breakShieldPlayer(Entity entity) {
        if (((LivingEntity) entity).isBlocking()) {
            int invSlot = InventoryOrigin.getInstance().getAxeInInventory(false);
            int hotBarSlot = InventoryOrigin.getInstance().getAxeInInventory(true);

            if (hotBarSlot == -1 && invSlot != -1) {
                int bestSlot = InventoryOrigin.getInstance().findBestSlotInHotBar();
                mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, mc.player);

                mc.player.connection.sendPacket(new CHeldItemChangePacket(bestSlot));
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));

                mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, mc.player);
            }

            if (hotBarSlot != -1) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(hotBarSlot));
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            }
        }
    }

    private boolean checkEntity(LivingEntity entity) {
        if (FriendManager.isFriend(entity.getName().getString())) {
            return false;
        }

        AttackSystem entitySelector = new AttackSystem();

        if (players.getValue()) {
            entitySelector.apply(AttackSystem.EntityType.PLAYERS);
        }
        if (mobs.getValue()) {
            entitySelector.apply(AttackSystem.EntityType.MOBS);
        }
        if (animals.getValue()) {
            entitySelector.apply(AttackSystem.EntityType.ANIMALS);
        }
        return entitySelector.ofType(entity, entitySelector.build()) != null && entity.isAlive();
    }

    @Override
    public void onDisable() {
        TimeCounterSetting.reset();
        timerForTarget.reset();
        target = null;
        super.onDisable();
    }
}
