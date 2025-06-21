package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;
import minecraft.system.AG;
import minecraft.game.transactions.EventPacket;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.util.Hand;

@Defuse(name = "Packet Criticals",description = "Криты без прыжка", brand = Category.Combat)
public class DDCRIT extends Module {

    public static boolean cancelCrit;

    public static final ModeSetting mode = new ModeSetting("Обход", "NCP", "NCP", "OldNCP", "NCPUpdate", "Grim", "Matrix", "FunTime old");

    public DDCRIT() {
        addSettings(mode);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.isSend()) {
            if (e.getPacket() instanceof CUseEntityPacket packet) {
                if (packet.getAction() == CUseEntityPacket.Action.ATTACK) {
                    Entity entity = packet.getEntityFromWorld(mc.world);
                    if (entity == null || entity instanceof EnderCrystalEntity || cancelCrit)
                        return;
                    sendCrit();
                }
            }
        }
    }

    public void sendCrit() {
        if (mc.player == null || mc.world == null)
            return;
        if ((mc.player.isOnGround() || mc.player.abilities.isFlying || mode.is("Grim") && !mc.player.isInLava() && !mc.player.isInWater())) {
            if (mode.is("NCP")) {
                critPacket(0.0625D, false);
                critPacket(0., false);
            }
            if (mode.is("NCPUpdate")) {
                critPacket(0.000000271875, false);
                critPacket(0., false);
            }
            if (mode.is("OldNCP")) {
                critPacket(0.00001058293536, false);
                critPacket(0.00000916580235, false);
                critPacket(0.00000010371854, false);
            }
            if (mode.is("Grim") && !mc.player.isOnGround()) {
                critPacket(-0.000001, false);
            }
            if (mode.is("Matrix")) {
                critPacket(1.0E-6, false);
                critPacket(0., false);
            }
            if (mode.is("FunTime old")) {
                if (mc.player.isOnGround()) critPacket(1e-8, false);
                mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY() - 1e-9, mc.player.getPosZ(), ((ClientPlayerEntity) mc.player).lastReportedYaw, ((ClientPlayerEntity) mc.player).lastReportedPitch, false));
                mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY() - 1e-9, mc.player.getPosZ(), ((ClientPlayerEntity) mc.player).lastReportedYaw, ((ClientPlayerEntity) mc.player).lastReportedPitch, false));
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
            }
        }
    }

    public static boolean canUseDDCRIT() {
        if (!AG.getInst().getModuleManager().getDdcrit().isEnabled()) return false;

        if (mode.is("Grim") && mc.player.isOnGround()) {
            return false;
        }

        return true;
    }

    private void critPacket(double yDelta, boolean full) {
        if (full)
            mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + yDelta, mc.player.getPosZ(), false));
        else
            mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY() + yDelta, mc.player.getPosZ(), ((ClientPlayerEntity) mc.player).lastReportedYaw, ((ClientPlayerEntity) mc.player).lastReportedPitch, false));
    }

}
