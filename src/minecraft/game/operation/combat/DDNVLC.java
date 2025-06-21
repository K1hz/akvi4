package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;

import minecraft.game.transactions.*;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.advantage.figures.TimeCounterSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import net.minecraft.network.play.server.SEntityDDNVLCPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

@Defuse(name = "No Velocity",description = "Убирает отталкивание", brand = Category.Combat)
public class  DDNVLC extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Cancel", "Cancel", "Grim Cancel", "Grim Skip", "Old Grim", "Grim Air", "1.18.2");

    private int skip = 0;
    private boolean cancel;
    boolean damaged;

    public DDNVLC() {
        addSettings(mode);
    }


    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player == null) return;

        if (e.isReceive()) {
            if (e.getPacket() instanceof SEntityDDNVLCPacket p && p.getEntityID() != mc.player.getEntityId()) return;
            switch (mode.getValue()) {
                case "Cancel" -> { // Cancel
                    if (e.getPacket() instanceof SEntityDDNVLCPacket) {
                        e.cancel();
                    }
                }

                case "Grim Skip" -> { // Grim Skip
                    if (e.getPacket() instanceof SEntityDDNVLCPacket) {
                        skip = 6;
                        e.cancel();
                    }

                    if (e.getPacket() instanceof CPlayerPacket) {
                        if (skip > 0) {
                            skip--;
                            e.cancel();
                        }
                    }
                }

                case "Grim Cancel" -> { // Grim Cancel
                    if (e.getPacket() instanceof SEntityDDNVLCPacket) {
                        e.cancel();
                        cancel = true;
                    }
                    if (e.getPacket() instanceof SPlayerPositionLookPacket) {
                        skip = 3;
                    }
                    if (mode.is("1.18.2")) {
                        if (e.getPacket() instanceof SEntityDDNVLCPacket p) {
                            e.cancel();
                        }
                    }

                    if (e.getPacket() instanceof CPlayerPacket) {
                        skip--;
                        if (cancel) {
                            if (skip <= 0) {
                                BlockPos blockPos = new BlockPos(mc.player.getPositionVec());
                                mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()));
                                mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
                            }
                            cancel = false;
                        }
                    }
                }

                case "Grim Air" -> {
                    if (mc.player.isElytraFlying() || mc.player.isInWater() || mc.player.isInLava()) {
                        return;
                    }

                    if (e.getPacket() instanceof SPlayerPositionLookPacket p) {
                        mc.player.func_242277_a(new Vector3d(p.getX(), p.getY(), p.getZ()));
                        mc.player.setRawPosition(p.getX(), p.getY(), p.getZ());
                        return;
                    }

                    if (mc.player.fallDistance < 15.0F) {
                        if (e.getPacket() instanceof SConfirmTransactionPacket && mc.player.fallDistance > 0.09) {
                            e.cancel();
                        }

                        if (e.getPacket() instanceof SEntityDDNVLCPacket && mc.player.fallDistance > 0.09 && ((SEntityDDNVLCPacket)e.getPacket()).getEntityID() == mc.player.getEntityId()) {
                            e.cancel();
                        }
                    }
                }

                case "Old Grim" -> {
                    if (e.getPacket() instanceof SEntityDDNVLCPacket p) {
                        if (skip >= 2) {
                            return;
                        }
                        if (p.getEntityID() != mc.player.getEntityId()) {
                            return;
                        }
                        e.cancel();
                        damaged = true;
                    }
                    if (e.getPacket() instanceof SPlayerPositionLookPacket) {
                        skip = 3;
                    }
                }
            }
        }
    }

    BlockPos blockPos;

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mode.is("Old Grim")) {
            skip--;
            if (damaged) {
                BlockPos blockPos = mc.player.getPosition();
                mc.player.connection.sendPacketWithoutEvent(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()));
                mc.player.connection.sendPacketWithoutEvent(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
                damaged = false;
            }
        }
        if (mode.is("1.18.2")) {
            skip--;
            if (damaged) {
                BlockPos blockPos = mc.player.getPosition();
                mc.player.connection.sendPacketWithoutEvent(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
                damaged = false;
            }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        skip = 0;
        cancel = false;
        damaged = false;
    }
}