package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;
import minecraft.system.AG;
import minecraft.game.transactions.EventMotion;
import minecraft.game.transactions.EventPacket;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.advantage.luvvy.MovementSystem;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;

@Defuse(name = "No Fall Damage",description = "ХУЙ", brand = Category.Movement)
public class NoFall extends Module {

    private ModeSetting mode = new ModeSetting("Обход", "FunTime", "FunTime", "FunTime New", "NCP", "MatrixOld", "Vanilla");

    public NoFall() {
        addSettings(mode);
    }

    boolean fall = false;

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (AG.getInst().getModuleManager().getDDFRCM().isEnabled()) return;

        if (mode.is("FunTime")) {
            if (mc.player.fallDistance > 2.4) {
                mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY() + 1e-6, mc.player.getPosZ(), ((ClientPlayerEntity) mc.player).lastReportedYaw, ((ClientPlayerEntity) mc.player).lastReportedPitch, false));
                mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY() + 1e-6, mc.player.getPosZ(), ((ClientPlayerEntity) mc.player).lastReportedYaw, ((ClientPlayerEntity) mc.player).lastReportedPitch, false));
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                mc.player.fallDistance = 0;
            }
        }

        if (mode.is("Vanilla")) {
            if (mc.player.fallDistance >= 3) {
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), true));
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), false));
                mc.player.fallDistance = 0.0F;
            }
        }

        if (mode.is("NCP")) {
            if (mc.player.fallDistance >= 3) {
                mc.player.onGround = false;
                mc.player.motion.y = 0.019999999552965164;

                for(int i = 0; i < 30; ++i) {
                    mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + 110000.0, mc.player.getPosZ(), false));
                    mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + 2.0, mc.player.getPosZ(), false));
                }

                mc.player.connection.sendPacket(new CPlayerPacket(true));
                mc.player.fallDistance = 0.0F;
            }
        }

        if (mode.is("MatrixOld")) {
            if (mc.player.fallDistance > (float) (mc.player.getHealth() > 6.0F ? 3 : 2)) {
                mc.player.fallDistance = (float) (Math.random() * 1.0E-12);
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), true));
                mc.player.jumpMovementFactor = 0.0F;
            }
        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {

    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (AG.getInst().getModuleManager().getDDFRCM().isEnabled()) return;

        if (mode.is("FunTime New")) {
            if (mc.player.fallDistance > 2.4 && !MovementSystem.isBlockUnder(2)) {
                double up = 0.0035000001080334187;
                mc.player.motion.y = up;
                mc.player.rotationYaw = 0;
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        fall = false;
    }
}
