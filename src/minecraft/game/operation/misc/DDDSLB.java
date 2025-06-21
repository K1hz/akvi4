package minecraft.game.operation.misc;

import lombok.Setter;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.client.CEntityActionPacket.Action;

import com.google.common.eventbus.Subscribe;

import minecraft.game.transactions.*;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.advantage.luvvy.InventoryOrigin;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

@Defuse(name = "Disаblеr",description = "Убирает флаги от ач", brand = Category.Misc)
public class DDDSLB extends Module {
	
	public static long lastStartFalling;
	public static CheckBoxSetting matrixElytraSpoof = new CheckBoxSetting("MatrixElySpoofs", false);
	public static CheckBoxSetting ncpmove = new CheckBoxSetting("NCPMovement", false);
	public static CheckBoxSetting vulcanstrafe = new CheckBoxSetting("VulcanStrafe", true);
	public static CheckBoxSetting verusCombat = new CheckBoxSetting("VerusCombat", true);
	public static CheckBoxSetting kauri = new CheckBoxSetting("KauriAC", false);
	@Setter
	boolean canHackJesus;
	int confirmtranscounter = 0;

	public DDDSLB() {
		addSettings(matrixElytraSpoof, ncpmove, vulcanstrafe, verusCombat, kauri);
	}

	@Override
	public void onEnable() {
		if (kauri.getValue() && !mc.isSingleplayer()) {
			print(getName() + ": перезайдите на сервер");
		}
		super.onEnable();
	}

	@Subscribe
	public void onPacket(EventPacket e) {
		if (verusCombat.getValue()) {
			if (e.getPacket() instanceof CConfirmTransactionPacket) {
				if (!mc.player.isAlive()) {
					confirmtranscounter = 0;
				}
				if (confirmtranscounter != 0) e.cancel();
				confirmtranscounter++;
			} else if (e.getPacket() instanceof CEntityActionPacket) {
				e.cancel();
			}
		}

		if (kauri.getValue()) {
			if (e.getPacket() instanceof CConfirmTransactionPacket) e.cancel();
		}
	}
	
	@Subscribe
	public void onUpdate(EventMoving e) {
		if (vulcanstrafe.getValue()) {
			if (mc.player.ticksExisted % 11 == 7) {
				mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, BlockPos.ZERO.down(61), mc.player.getHorizontalFacing().getOpposite()));
			}

			setCanHackJesus(mc.player.ticksExisted > 8 && (!mc.playerController.getIsHittingBlock() || !(mc.playerController.curBlockDamageMP > 0.0F))); // TODO: make the vulcan jesus DDDSLB
		}

		if (matrixElytraSpoof.getValue()) {
			int elytra = InventoryOrigin.getSlotIDFromItem(Items.ELYTRA);
			if (elytra == -1) {
				return;
			}
			if ((System.currentTimeMillis() - lastStartFalling) > 150) {
				DDDSLB(elytra);
			}
		}
	}

	@Subscribe
	public void onMotion(EventMotion e) {
		ItemStack chestStack = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);
		if (ncpmove.getValue()) {
			if (!(chestStack.getItem() == Items.ELYTRA)) return;

			if (mc.player.isOnGround() && !mc.player.isElytraFlying()) {
				mc.player.jump();
			}

			if (!mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox().expand(1, 0, 1).offset(0, -1, 0)).toList().isEmpty() && mc.player.isElytraFlying()) {
				mc.player.getBoundingBox().expand(1, 0, 1).offset(0, -1, 0);
			}

			if (ElytraItem.isUsable(chestStack) && !mc.player.isElytraFlying() && !mc.player.abilities.isFlying && mc.player.fallDistance >= 0.2f) {
				mc.player.startFallFlying();
				mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
			}
		}
	}
	
	public static void DDDSLB(int elytra) {
		if (elytra != -2) {
			mc.playerController.windowClick(0, elytra, 1, ClickType.PICKUP, mc.player);
			mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, mc.player);
		}
		mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, Action.START_FALL_FLYING));
		mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, Action.START_FALL_FLYING));
		if (elytra != -2) {
			mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, mc.player);
			mc.playerController.windowClick(0, elytra, 1, ClickType.PICKUP, mc.player);
		}
		lastStartFalling = System.currentTimeMillis();
	}


}
