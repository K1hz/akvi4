package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.transactions.EventUpdate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.item.Items;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

@Defuse(name = "Creeper Farm",description = "123", brand = Category.Movement)
public class CreeperFarm extends Module {
    private long cpsLimit = 0;
    private boolean rotateDirection = true;
    private final SliderSetting distince = new SliderSetting("Дистанция", 15, 5, 50, 1F);
    private final TimeCounterSetting timerUtil = new TimeCounterSetting();

    public CreeperFarm() {
        addSettings(distince);
    }

    @Subscribe
    public boolean onEvent(EventUpdate event) {
            if (cpsLimit > System.currentTimeMillis()) {
                cpsLimit--;
            }
            if (!mc.gameSettings.keyBindForward.isKeyDown()) {
                mc.gameSettings.keyBindForward.setPressed(true);
            }
            if (!mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.gameSettings.keyBindJump.setPressed(true);
            }
            if (mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY) {
                if (whenFalling() && (cpsLimit <= System.currentTimeMillis())) {
                    cpsLimit = System.currentTimeMillis() + 550;
                    mc.playerController.attackEntity(mc.player, ((EntityRayTraceResult) mc.objectMouseOver).getEntity());
                    mc.player.swingArm(Hand.MAIN_HAND);
                }
            }
        try {
            if (mc == null || mc.world == null || mc.player == null) {
                return false;
            }

            Entity target = null;
            double closestDistance = Double.MAX_VALUE;

            for (ItemEntity item : mc.world.getEntitiesWithinAABB(ItemEntity.class, mc.player.getBoundingBox().grow(distince.getValue().longValue()))) {
                if (item != null && item.isAlive() && item.getItem().getItem() == Items.GUNPOWDER) {
                    double distance = mc.player.getDistance(item);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        target = item;
                    }
                }
            }

            if (target == null) {
                for (CreeperEntity creeper : mc.world.getEntitiesWithinAABB(CreeperEntity.class, mc.player.getBoundingBox().grow(distince.getValue().longValue()))) {
                    if (creeper != null && creeper.isAlive() && mc.player.canEntityBeSeen(creeper)) {
                        double distance = mc.player.getDistance(creeper);
                        if (distance < closestDistance) {
                            closestDistance = distance;
                            target = creeper;
                        }
                    }
                }
            }

            if (target != null) {
                faceEntity(target);
            } else if (mc.player.collidedHorizontally) {
                changeRotationDirection();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean whenFalling() {
        boolean critWater = mc.player.areEyesInFluid(FluidTags.WATER);

        final boolean reasonForCancelCritical = mc.player.isPotionActive(Effects.BLINDNESS)
                || mc.player.isOnLadder()
                || mc.player.isInWater() && critWater
                || mc.player.isRidingHorse()
                || mc.player.abilities.isFlying
                || mc.player.isElytraFlying();

        final boolean onSpace = mc.player.isOnGround();

        if (mc.player.getCooledAttackStrength(1.5F) < 0.92F)
            return false;

        if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY) {
            Entity target = ((EntityRayTraceResult) mc.objectMouseOver).getEntity();
            if (target instanceof CreeperEntity) {
                if (!reasonForCancelCritical) {
                    return onSpace || !mc.player.isOnGround() && mc.player.fallDistance > 0.0F;
                }
            }
        }
        return false;
    }

    private void changeRotationDirection() {
        rotateDirection = !rotateDirection;
        float yawChange = rotateDirection ? 49.0F : -50.0F;
        mc.player.rotationYaw += yawChange;
    }

    private void faceEntity(Entity entity) {
        try {
            if (entity == null || mc.player == null) {
                return;
            }

            double dx = entity.getPosX() - mc.player.getPosX();
            double dy = entity.getPosY() + (entity.getHeight() / 2.0) - (mc.player.getPosY() + mc.player.getEyeHeight());
            double dz = entity.getPosZ() - mc.player.getPosZ();

            double distanceXZ = Math.sqrt(dx * dx + dz * dz);

            float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, distanceXZ));

            mc.player.rotationYaw = yaw;
            mc.player.rotationPitch = pitch;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
