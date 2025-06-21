package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;

import minecraft.system.AG;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.transactions.EventMoving;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.advantage.luvvy.InventoryOrigin;
import minecraft.game.advantage.luvvy.MovementSystem;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CEntityActionPacket.Action;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import static minecraft.game.advantage.luvvy.InventoryOrigin.getHotbarSlotOfItem;

@Defuse(name = "Elytra Flight",description = "Полет на элитре", brand = Category.Movement)
public class ElytraFly extends Module {
	private final TimeCounterSetting TimeCounterSetting = new TimeCounterSetting();
	private final TimeCounterSetting TimeCounterSetting1 = new TimeCounterSetting();
    public static long lastStartFalling;

    public ModeSetting mode = new ModeSetting("Мод", "CatFly Mode", "Matrix", "Matrix Glide", "Grim UP", "CatFly Mode");
	ItemStack currentStack = ItemStack.EMPTY;
    private SliderSetting motionY = new SliderSetting("Скорость Y", 0.2f, 0.1f, 0.5f, 0.01f).visibleIf(() -> mode.is("Matrix"));
    private SliderSetting motionX = new SliderSetting("Скорость XZ", 1.2f, 0.1f, 5f, 0.1f).visibleIf(() -> mode.is("Matrix"));
	private CheckBoxSetting autojump = new CheckBoxSetting("Авто прыжок", false).visibleIf(() -> mode.is("Matrix"));
	private CheckBoxSetting saveMe = new CheckBoxSetting("Спасать", false).visibleIf(() -> mode.is("Matrix"));
	private final SliderSetting timerStartFireWork = new SliderSetting("Таймер фейерверка", 400, 50, 1500, 10).visibleIf(() -> mode.is("CatFly Mode"));
	private final CheckBoxSetting onlyGrimBypass = new CheckBoxSetting("Обход Grim", true).visibleIf(() -> mode.is("CatFly Mode"));
	boolean launchRocket = true;
	public static boolean shackingcontroll;
	public ElytraFly() {
    	addSettings(mode, motionX, motionY, autojump, saveMe, timerStartFireWork, onlyGrimBypass);
    }

	@Subscribe
	public void onMoving(EventMoving e) {
		if (AG.getInst().getModuleManager().getDDFRCM().isEnabled()) return;

		if (mode.is("Matrix Glide")) {
			int elytra = InventoryOrigin.getSlotIDFromItem(Items.ELYTRA);
			if (elytra == -1) {
				return;
			}
			Vector3d motion = e.motion;
			if (System.currentTimeMillis() - lastStartFalling > 1000) {
				DDDSLB(elytra);
			}
			if (System.currentTimeMillis() - lastStartFalling < 800 && !mc.player.isSneaking()) {
				motion.y = motionY.getValue().doubleValue();
			} else {
				motion.y -= 0.05;
			}
			mc.player.jump();
			mc.player.motion.y = motion.y;
		}
	}

	@Override
	public void onDisable() {
		super.onDisable();
		TimeCounterSetting.reset();
		TimeCounterSetting1.reset();
	}

    @Subscribe
    public void onUpdate(EventUpdate e) {
		if (AG.getInst().getModuleManager().getDDFRCM().isEnabled()) return;
		if (mode.is("Grim UP")) {
			shackingcontroll = true;
			this.currentStack = ElytraFly.mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);
			if (this.currentStack.getItem() == Items.ELYTRA) {
				if (ElytraFly.mc.player.isOnGround()) {
					ElytraFly.mc.player.jump();
					ElytraFly.mc.player.rotationPitchHead = -90.0f;
				} else if (ElytraItem.isUsable(this.currentStack) && !ElytraFly.mc.player.isElytraFlying()) {
					ElytraFly.mc.player.startFallFlying();
					ElytraFly.mc.player.connection.sendPacket(new CEntityActionPacket(ElytraFly.mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
					ElytraFly.mc.player.rotationPitchHead = -90.0f;
				}
				ElytraFly.mc.player.rotationPitch = 0.0f;
				ElytraFly.mc.player.motion.y *= (double)1.08f;
			}
		}
		if (mode.is("CatFly Mode")) {

			long elytraSwapTime = 550;

			if (onlyGrimBypass.getValue()) {
				elytraSwapTime = 0;
				if (mc.player.isHandActive() && mc.player.getHeldItemMainhand().getUseAction() == UseAction.EAT) {
					launchRocket = false;
				} else {
					launchRocket = true;
				}
			} else {
				launchRocket = true;
			}

	        for (int i = 0; i < 9; i++) {
				if (mc.player.inventory.getStackInSlot(i).getItem() == Items.ELYTRA && mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY() - 0.01, mc.player.getPosZ())).getBlock() == Blocks.AIR && !mc.player.isOnGround() && !mc.player.isInWater() && !mc.player.isInLava() && !mc.player.isElytraFlying()) {
					if (TimeCounterSetting1.isReached(elytraSwapTime)) {
						mc.playerController.windowClick(0, 6, i, ClickType.SWAP, mc.player);
						mc.player.startFallFlying();
						mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
						mc.playerController.windowClick(0, 6, i, ClickType.SWAP, mc.player);
						TimeCounterSetting1.reset();
					}

					if (launchRocket) {
						if (mc.player.isElytraFlying()) {
							if (TimeCounterSetting.isReached(timerStartFireWork.getValue().longValue())) {
								InventoryOrigin.inventorySwapClick(Items.FIREWORK_ROCKET, false);
								TimeCounterSetting.reset();
							}
						}
					}
	            }
	        }
		}

    	if (mode.is("Matrix")) {
			int elytra = getHotbarSlotOfItem();
			if (MovementSystem.reason(false)) {
				return;
			}
			if (elytra == -1) {
				return;
			}
			if (mc.player.onGround) {
				if (autojump.getValue()) {
					mc.player.jump();
				}
				TimeCounterSetting.reset();
			} else if (TimeCounterSetting.isReached(350)) {

				if (mc.player.ticksExisted % 2 == 0) {
					DDDSLB(elytra);

				}

				mc.player.motion.y = mc.player.ticksExisted % 2 != 0 ? -0.25 : 0.25;

				if (saveMe.getValue()) {
					if ((!MovementSystem.isBlockUnder(4f) || mc.player.collidedHorizontally || mc.player.collidedVertically)) {
						mc.player.motion.y = motionY.getValue();
					}
				}

				if (!mc.player.isSneaking() && mc.gameSettings.keyBindJump.pressed) {
					mc.player.motion.y = motionY.getValue();
				}

				if (mc.gameSettings.keyBindSneak.isKeyDown()) {
					mc.player.motion.y = -motionY.getValue();
				}

				MovementSystem.setMotion(motionX.getValue());
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
