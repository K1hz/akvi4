package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.network.play.client.CEntityActionPacket;
import minecraft.game.transactions.EventPacket;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.advantage.luvvy.MovementSystem;

@Defuse(name = "Boat Fly",description = "Полёт на лодке", brand = Category.Movement)
public class BoatFly extends Module {
    final SliderSetting speed = new SliderSetting("Скорость", 10.f, 1.f, 20.f, 0.05f);
    final CheckBoxSetting noDismount = new CheckBoxSetting("Не вылезать", true);


    public BoatFly() {
        addSettings(speed, noDismount);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (mc.player.getRidingEntity() != null) {
            if (mc.player.getRidingEntity() instanceof BoatEntity) {

                mc.player.getRidingEntity().motion.y = 0;
                if (mc.player.isPassenger()) {
                    if (mc.gameSettings.keyBindJump.isKeyDown()) {
                        mc.player.getRidingEntity().motion.y = 1;
                    } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                        mc.player.getRidingEntity().motion.y = -1;
                    }


                    if (MovementSystem.isMoving()) {
                        final double yaw = MovementSystem.getDirection(true);

                        mc.player.getRidingEntity().motion.x = -Math.sin(yaw) * speed.getValue();
                        mc.player.getRidingEntity().motion.z = Math.cos(yaw) * speed.getValue();
                    } else {
                        mc.player.getRidingEntity().motion.x = 0;
                        mc.player.getRidingEntity().motion.z = 0;
                    }
                    if ((!MovementSystem.isBlockUnder(4f) || mc.player.collidedHorizontally || mc.player.collidedVertically)) {
                        mc.player.getRidingEntity().motion.y += 1;
                    }
                }
            }
        }
    }

    @Subscribe
    private void onPacket(EventPacket e) {


        if (e.getPacket() instanceof CEntityActionPacket actionPacket) {
            if (!noDismount.getValue() || !(mc.player.getRidingEntity() instanceof BoatEntity)) return;
            CEntityActionPacket.Action action = actionPacket.getAction();
            if (action == CEntityActionPacket.Action.START_SNEAKING || action == CEntityActionPacket.Action.STOP_SNEAKING)
                e.cancel();
        }
    }

    public boolean notStopRidding() {
        return this.isEnabled() && noDismount.getValue();
    }
}
