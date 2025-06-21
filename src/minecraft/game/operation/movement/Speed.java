package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;

import minecraft.game.operation.combat.DDATTACK;
import minecraft.game.operation.combat.DDPOTION;
import minecraft.system.AG;
import minecraft.game.transactions.*;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.advantage.luvvy.InventoryOrigin;
import minecraft.game.advantage.luvvy.MovementSystem;
import minecraft.game.advantage.luvvy.StrafeMovement;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SEntityDDNVLCPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.system.CallbackI;

@Defuse(name = "Speed",description = "Изменяет скорость игрока", brand = Category.Movement)
public class Speed extends Module {

	private final ModeSetting mode = new ModeSetting("Обход", "Matrix_AC", "Vanilla", "Matrix_AC", "Grim_AC", "NCP", "Timer", "Vulcan", "Funtime", "AAC", "RAC");
	private final CheckBoxSetting autoJump = new CheckBoxSetting("Auto Jump", false).visibleIf(() -> mode.is("Matrix_AC") || mode.is("NCP") || mode.is("Vanilla"));
	// aac
	private final CheckBoxSetting longjump_aac = new CheckBoxSetting("LongJump", false).visibleIf(() -> mode.is("AAC"));
	private final CheckBoxSetting onground_aac = new CheckBoxSetting("OnGround", false).visibleIf(() -> mode.is("AAC"));

	// Matrix_AC
	private final CheckBoxSetting timerboost_Matrix_AC = new CheckBoxSetting("Timer", false).visibleIf(() -> mode.is("Matrix_AC"));
	private final CheckBoxSetting motionboost_Matrix_AC = new CheckBoxSetting("Motion", true).visibleIf(() -> mode.is("Matrix_AC"));
	private final CheckBoxSetting strafemotion_Matrix_AC = new CheckBoxSetting("Strafe", false).visibleIf(() -> mode.is("Matrix_AC") && motionboost_Matrix_AC.getValue());
	private final CheckBoxSetting damageboost_Matrix_AC = new CheckBoxSetting("DamageBoost", true).visibleIf(() -> mode.is("Matrix_AC"));
	private final CheckBoxSetting airboost_Matrix_AC = new CheckBoxSetting("AirBoost", false).visibleIf(() -> mode.is("Matrix_AC"));

	// Grim_AC
	private final CheckBoxSetting blockboost_Grim_AC = new CheckBoxSetting("BlockBoost", false).visibleIf(() -> mode.is("Grim_AC"));
	private final CheckBoxSetting entityboost_Grim_AC = new CheckBoxSetting("EntityBoost", true).visibleIf(() -> mode.is("Grim_AC"));
	private final CheckBoxSetting timerboost_Grim_AC = new CheckBoxSetting("Timer", false).visibleIf(() -> mode.is("Grim_AC"));
	private final CheckBoxSetting lastGrimCollision = new CheckBoxSetting("Fix Collision", false).visibleIf(() -> mode.is("Grim_AC") && this.entityboost_Grim_AC.getValue());private final SliderSetting speed_motion = new SliderSetting("Скорость", 8, 1, 25, 1f).visibleIf(() -> mode.is("Grim_AC") && this.entityboost_Grim_AC.getValue() && !this.lastGrimCollision.getValue());
	// ncp
	private final CheckBoxSetting timerboost_ncp = new CheckBoxSetting("Timer", true).visibleIf(() -> mode.is("NCP"));
	private final CheckBoxSetting yport_ncp = new CheckBoxSetting("YPort", true).visibleIf(() -> mode.is("NCP"));
	private final CheckBoxSetting bhop_ncp = new CheckBoxSetting("BHop", false).visibleIf(() -> mode.is("NCP"));
	private final CheckBoxSetting spoofJump = new CheckBoxSetting("Spoof", false).visibleIf(() -> mode.is("NCP") && autoJump.getValue() && bhop_ncp.getValue());


	// vanilla
	private final SliderSetting speed = new SliderSetting("Скорость", 1, 0.1f, 5, 0.1f).visibleIf(() -> mode.is("Vanilla"));

	private final CheckBoxSetting antiFlag = new CheckBoxSetting("AntiFlag", true);

	private final StrafeMovement strafeMovement = new StrafeMovement();
	private boolean enabled = false;
	public static int stage;
	public double less, stair, moveSpeed;
	public boolean slowDownHop, wasJumping, boosting, restart;
	private int prevSlot = -1;
	boolean isDDNVLC, damage, DDNVLC;
	int ticks;
	double motion;
	public TimeCounterSetting TimeCounterSetting = new TimeCounterSetting();
	public TimeCounterSetting racTimer = new TimeCounterSetting();

	public Speed() {
		addSettings(mode, speed,
				autoJump,
				blockboost_Grim_AC, entityboost_Grim_AC,lastGrimCollision, speed_motion,timerboost_Grim_AC,
				damageboost_Matrix_AC, motionboost_Matrix_AC, strafemotion_Matrix_AC, airboost_Matrix_AC, timerboost_Matrix_AC,
				timerboost_ncp, yport_ncp, bhop_ncp, spoofJump,
				longjump_aac, onground_aac, antiFlag);
	}

	@Override
	public void onDisable() {
		mc.timer.timerSpeed = 1;
		super.onDisable();
	}

	@Override
	public void onEnable() {
		super.onEnable();
	}

	@Subscribe
	public void onPacket(EventPacket e) {
		if (AG.getInst().getModuleManager().getDDFRCM().isEnabled()) return;

		if (antiFlag.getValue()) {
			if (e.getPacket() instanceof SPlayerPositionLookPacket p) {
				mc.player.setPacketCoordinates(p.getX(), p.getY(), p.getZ());
				mc.player.setRawPosition(p.getX(), p.getY(), p.getZ());
				toggle();
			}
		}

		if (mode.is("Grim_AC") && timerboost_Grim_AC.getValue()) {
			if (e.getPacket() instanceof CConfirmTransactionPacket p) {
				e.cancel();
			}
		}

		if (mode.is("Matrix_AC") && damageboost_Matrix_AC.getValue() && e.isReceive()) {
			if (e.getPacket() instanceof SEntityDDNVLCPacket) {
				if (((SEntityDDNVLCPacket) e.getPacket()).getMotionY() > 0) {
					isDDNVLC = true;
				}
				if ((((SEntityDDNVLCPacket) e.getPacket()).getMotionY() / 8000.0D) > 0.2) {
					motion = (((SEntityDDNVLCPacket) e.getPacket()).getMotionY() / 8000.0D);
					DDNVLC = true;
				}
			}
		}
	}

	@Subscribe
	public void onUpdate(EventUpdate e) {
		if (AG.getInst().getModuleManager().getDDFRCM().isEnabled()) return;

		switch (mode.getValue()) {
			case "Matrix_AC" -> {
				if (mc.player.isOnGround() && autoJump.getValue() && !mc.player.isInLava() && !mc.player.isInWater() && !airboost_Matrix_AC.getValue()) {
					mc.player.jump();
				}
			}

			case "NCP" -> {
				NCPSpeed(timerboost_ncp.getValue(), yport_ncp.getValue(), bhop_ncp.getValue());
			}

			case "Vanilla" -> {
				MovementSystem.setMotion(speed.getValue());

				if (autoJump.getValue() && mc.player.isOnGround() && !mc.player.isInWater() && !mc.player.isInLava()) mc.player.jump();
			}

			case "RAC" -> {
				if (racTimer.isReached(10)) {
					if (mc.player.onGround && !mc.player.isJumping) {
						MovementSystem.setSpeed((float) MathHelper.clamp(MovementSystem.getSpeed() * (mc.player.rayGround ? 1.8 : 0.8), 0.2, MovementSystem.w() && mc.player.isSprinting() ? 1.715499997138977 : 1.7450000047683716));
						mc.player.rayGround = mc.player.onGround;
					} else {
						mc.player.serverSprintState = true;
						MovementSystem.setSpeed((float) MathHelper.clamp(MovementSystem.getSpeed() * (!mc.player.onGround && !mc.player.rayGround ? 1.2 : 1.0), 0.195, 1.823585033416748), 0.12F);
						mc.player.rayGround = mc.player.onGround;
					}

					racTimer.reset();
				}
			}

			case "Funtime" -> {
				AxisAlignedBB aabb = mc.player.getBoundingBox().grow(0.1);
				int armorstans = mc.world.getEntitiesWithinAABB(ArmorStandEntity.class, aabb).size();
				boolean canBoost = armorstans > 1 || mc.world.getEntitiesWithinAABB(LivingEntity.class, aabb).size() > 1;
				if (canBoost && !mc.player.isOnGround()) {
					mc.player.jumpMovementFactor = armorstans > 1 ? 1.0f / (float) armorstans : 0.16f;
				}
			}

			case "Grim_AC" -> {
				if (timerboost_Grim_AC.getValue()) {
					if (TimeCounterSetting.isReached(1150)) {
						boosting = true;
					}
					if (TimeCounterSetting.isReached(7000)) {
						boosting = false;
						TimeCounterSetting.reset();
					}
					if (boosting) {
						if (mc.player.isOnGround() && !mc.gameSettings.keyBindJump.pressed) {
							mc.player.jump();
						}
						mc.timer.timerSpeed = (mc.player.ticksExisted % 2 == 0 ? 1.5f : 1.2f);
					} else {
						mc.timer.timerSpeed = (0.05f);
					}
				}
				if (this.lastGrimCollision.getValue()) {
						AxisAlignedBB aabb = mc.player.getBoundingBox().grow(0.3);
						int armorstans = mc.world.getEntitiesWithinAABB(ArmorStandEntity.class, aabb).size();
						boolean canBoost = armorstans > 1 || mc.world.getEntitiesWithinAABB(LivingEntity.class, aabb).size() > 1;
						if (canBoost && !mc.player.isOnGround()) {
							float speed = 0.105f;
							mc.player.jumpMovementFactor = armorstans > 0.5f ? speed / speed : speed;
						}
				}
				if (blockboost_Grim_AC.getValue()) {
					int block = InventoryOrigin.getInstance().getSlotInInventoryOrHotbar(Items.ICE, true);
					if (block == -1 || mc.player.isInWater()) {
						return;
					}
					if (mc.player.isOnGround()) {
						if (!wasJumping) {
							wasJumping = true;
							placeBlock();
							mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.rotationYaw, 90.0f, mc.player.isOnGround()));
						}
					} else {
						wasJumping = false;
						mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.rotationYaw, 90.0f, mc.player.isOnGround()));
					}
					if (mc.player.isOnGround()) {
						mc.player.jump();
					}
				}
			}

			case "Timer" -> {
				float timerValue = mc.player.fallDistance <= 0.25f ? 2.2f : (float) (mc.player.fallDistance != Math.ceil(mc.player.fallDistance) ? 0.4f : 1f);
				if (MovementSystem.isMoving()) {
					mc.timer.timerSpeed = timerValue;
					if (mc.player.onGround) {
						mc.player.jump();
					}
				} else {
					mc.timer.timerSpeed = 1.0f;
				}
			}


			case "Vulcan" -> {
				mc.player.jumpMovementFactor = 0.025f;
				if (mc.player.isOnGround() && MovementSystem.isMoving()) {
					if (mc.player.collidedHorizontally && mc.gameSettings.keyBindJump.pressed) {
						if (!mc.gameSettings.keyBindJump.pressed) {
							mc.player.jump();
						}
						return;
					}
					mc.player.jump();
					mc.player.motion.y = 0.1f;
				}
			}

			case "AAC" -> {
				boolean longHop = longjump_aac.getValue() && (mc.player.isJumping || mc.player.fallDistance != 0.0F);
				boolean onGround = onground_aac.getValue() && !mc.player.isJumping && mc.player.onGround && mc.player.collidedVertically && MovementSystem.getSpeed() < 0.9;
				mc.timer.timerSpeed = 1.2f;
				if (longHop) {
					mc.player.jumpMovementFactor = 0.17F;
					mc.player.multiplyMotionXZ(1.005F);
				}

				if (onGround) {
					mc.player.multiplyMotionXZ(1.212F);
				}
			}
		}
	}

	@Subscribe
	public void onMotion(EventMotion move) {
		if (AG.getInst().getModuleManager().getDDFRCM().isEnabled()) return;

		if (mode.is("Matrix_AC") && timerboost_Matrix_AC.getValue()) {
			if (mc.player.isOnGround()) {
				mc.timer.timerSpeed = (1.1f);
			}
			if (mc.player.fallDistance > 0.1 && mc.player.fallDistance < 1) {
				mc.timer.timerSpeed = (1 + (1F - Math.floorMod((long) 2.520, (long) 2.600)));
			}
			if (mc.player.fallDistance >= 1) {
				mc.timer.timerSpeed = (0.978F);
			}
		}

		if (mode.is("Matrix_AC") && damageboost_Matrix_AC.getValue()) {
			double radians = MovementSystem.getDirection();
			if (mc.player.hurtTime == 9) {
				damage = true;
			}
			if (damage && isDDNVLC) {
				if (DDNVLC) {
					if (mc.player.onGround && MovementSystem.isMoving()) {
						mc.player.addDDNVLC(-Math.sin(radians) * 8 / 24.5, 0, Math.cos(radians) * 8 / 24.5);
						MovementSystem.setStrafe(MovementSystem.getSpeed());
					}
					ticks++;
				}
				if (ticks >= Math.max(24, 30)) {
					isDDNVLC = false;
					DDNVLC = false;
					damage = false;
					toggle();
					ticks = 0;
				}
			}
		}

		if (mode.is("Matrix_AC") && airboost_Matrix_AC.getValue()) {
			if (mc.player.isOnGround()) {
				enabled = true;
			} else if (mc.player.fallDistance > 0f) {
				enabled = false;
			}

			if (!mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox().expand(0.5, 0, 0.5).offset(0, -1, 0)).toList().isEmpty() && mc.player.ticksExisted % 2 == 0) {
				if (!motionboost_Matrix_AC.getValue() && !autoJump.getValue()) {
					mc.player.fallDistance = 0;
					move.setOnGround(true);
					mc.player.onGround = true;
				}
				if (enabled && !mc.player.movementInput.jump && autoJump.getValue()) mc.player.jump();
				mc.player.jumpMovementFactor = 0.026523f;
			}
		}
	}

	@Subscribe
	public void onMove(EventMoving e) {
		if (AG.getInst().getModuleManager().getDDFRCM().isEnabled()) return;

		if (mode.is("Matrix_AC") && motionboost_Matrix_AC.getValue()) {
			if (!mc.player.isOnGround() && mc.player.fallDistance >= 0.5f && e.toGround) {
				double speed = 2f;
				if (strafemotion_Matrix_AC.getValue()) {
					double[] newSpeed = MovementSystem.getSpeed((Math.hypot(mc.player.motion.x, mc.player.motion.z) - 0.0001) * speed);
					e.motion.x = newSpeed[0];
					e.motion.z = newSpeed[1];
					mc.player.motion.x = e.motion.x;
					mc.player.motion.z = e.motion.z;
					return;
				}
				mc.player.motion.x *= speed;
				mc.player.motion.z *= speed;
				strafeMovement.setOldSpeed(speed);
			}
		}

		if (!this.lastGrimCollision.getValue()) {
			if (mode.is("Grim_AC") && entityboost_Grim_AC.getValue()) {
				for (Entity ent : mc.world.getAllEntities()) {
					int collisions = 0;
					if (ent != mc.player && (ent instanceof LivingEntity || ent instanceof BoatEntity) && mc.player.getBoundingBox().expand(0, 1.0, 0).intersects(ent.getBoundingBox())) collisions++;
					double[] motion = MovementSystem.forward(this.speed_motion.getValue() / 100 * collisions);
					mc.player.addDDNVLC(motion[0], 0.0, motion[1]);
				}
			}
		}
		if (this.lastGrimCollision.getValue()) {
			AxisAlignedBB aabb = mc.player.getBoundingBox().grow(0.1);
			int armorstans = mc.world.getEntitiesWithinAABB(ArmorStandEntity.class, aabb).size();
			boolean canBoost = armorstans > 1 || mc.world.getEntitiesWithinAABB(LivingEntity.class, aabb).size() > 1;
			if (canBoost && !mc.player.isOnGround()) {
				float speed = 0.1f;
				mc.player.jumpMovementFactor = armorstans > 0.5f ? speed / speed : speed;
			}
		}
	}

	public void NCPSpeed(boolean timer, boolean yPort, boolean bhop) {
		if (timer) {
			mc.timer.timerSpeed = 1.075f;
		}

		double speed = 0.0;

		if (yPort) {
			speed = MovementSystem.getSpeed();
			mc.player.speedInAir = mc.player.isPotionActive(Effects.SPEED) ? 0.06f : 0.05f;
			if (mc.player.onGround) {
				mc.player.jump();
				if (mc.player.isPotionActive(Effects.SPEED)) {
					mc.player.jump();
				}
				mc.player.motion.y /= 1.05;
			} else {
				if (!mc.player.collidedHorizontally) {
					mc.player.motion.y -= 1.0;
				}
				speed = mc.player.isPotionActive(Effects.SPEED) ? 0.45 : 0.32;
			}
			MovementSystem.setSpeed((float) speed);
		} else if (mc.player.speedInAir == 0.06f || mc.player.speedInAir == 0.05f) {
			mc.player.speedInAir = 0.02f;
		}

		if (bhop) {
			if (!autoJump.getValue() && !mc.gameSettings.keyBindJump.isKeyDown() && !yPort) {
				return;
			}

			mc.player.jumpMovementFactor = (float) ((double) mc.player.jumpMovementFactor * 1.04);
			boolean collided = mc.player.collidedHorizontally;

			if (collided) {
				stage = -1;
			}
			if (this.stair > 0.0) {
				this.stair -= 0.3;
			}
			this.less -= this.less > 1.0 ? 0.24 : 0.17;
			if (this.less < 0.0) {
				this.less = 0.0;
			}

			if (!mc.player.isInWater() && mc.player.onGround) {
				collided = mc.player.collidedHorizontally;
				if (stage >= 0 || collided) {
					stage = 0;
					float motY = 0.42f;
					if (spoofJump.getValue())
						mc.player.motion.y = motY;
					else
						mc.player.jump();
					this.less += 1.0;
					this.slowDownHop = this.less > 1.0 && !this.slowDownHop;
					if (this.less > 1.15) {
						this.less = 1.15;
					}
				}
			}
			this.moveSpeed = this.getCurrentSpeed(stage) + 0.0335;
			this.moveSpeed *= 0.85;
			if (this.stair > 0.0) {
				this.moveSpeed *= 1.0;
			}
			if (this.slowDownHop) {
				this.moveSpeed *= 0.8575;
			}
			if (mc.player.isInWater()) {
				this.moveSpeed = 0.351;
			}
			if (MovementSystem.isMoving()) {
				MovementSystem.setSpeed((float)moveSpeed);
			}
			++stage;
		}
	}

	public void placeBlock() {
		if (AG.getInst().getModuleManager().getDDATTACK().isEnabled() && AG.getInst().getModuleManager().getDDATTACK().getTarget() != null) {
			return;
		}
		BlockPos blockPos = new BlockPos(mc.player.getPosX(), mc.player.getPosY() - 0.6, mc.player.getPosZ());
		if (mc.world.getBlockState(blockPos).isAir()) {
			return;
		}
		int block = InventoryOrigin.findBlockInHotbar();
		if (block == -1) {
			return;
		}
		mc.player.connection.sendPacket(new CHeldItemChangePacket(block));
		mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
		Vector3d blockCenter = new Vector3d((double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5);
		mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, new BlockRayTraceResult(blockCenter, Direction.UP, blockPos, false)));
		mc.player.connection.sendPacket(new CHeldItemChangePacket(prevSlot));
		mc.world.setBlockState(blockPos, Blocks.ICE.getDefaultState());
		prevSlot = mc.player.inventory.currentItem;
	}

	public double getCurrentSpeed(int stage) {
		double speed = MovementSystem.getBaseSpeed() + 0.028 * (double) MovementSystem.getSpeedEffect() + (double) MovementSystem.getSpeedEffect() / 15.0;
		double initSpeed = 0.4145 + (double) MovementSystem.getSpeedEffect() / 12.5;
		double decrease = (double) stage / 500.0 * 1.87;
		if (stage == 0) {
			speed = 0.64 + ((double) MovementSystem.getSpeedEffect() + 0.028 * (double) MovementSystem.getSpeedEffect()) * 0.134;
		} else if (stage == 1) {
			speed = initSpeed;
		} else if (stage >= 2) {
			speed = initSpeed - decrease;
		}
		return Math.max(speed, this.slowDownHop ? speed : MovementSystem.getBaseSpeed() + 0.028 * (double) MovementSystem.getSpeedEffect());
	}
}
