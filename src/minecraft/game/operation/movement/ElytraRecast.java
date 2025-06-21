package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import minecraft.game.advantage.luvvy.MovementSystem;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.transactions.EventUpdate;

@Defuse(name = "Elytra Recast",description = "Баг элитры", brand = Category.Movement)
public class ElytraRecast extends Module {
    ModeSetting elytramod = new ModeSetting("Elytra Mode", "Matrix", "Matrix", "Grim","Grim New");
    public ElytraRecast() {

        addSettings(elytramod);
    }
    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (!mc.player.isElytraFlying() && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA && MovementSystem.isMoving() && !mc.player.isOnGround()) {
            mc.player.rotationPitch = 45;
            mc.player.startFallFlying();
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
          /*  mc.player.startFallFlying();
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
            */if (elytramod.is("Grim")) {
                mc.player.rotationPitch = 35;
            }
            //mc.player.rotationPitch = 35;
            if (elytramod.is("Matrix")) {
                mc.player.rotationPitch = 15;
                mc.player.motion.y = -0.90;
            }
            if (elytramod.is("Grim New")) {
                mc.player.rotationPitch = 75;
                mc.player.startFallFlying();
                //     mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
            }

            // mc.player.rotationPitch = 15;

        }

        if (mc.player.isOnGround() && mc.player.isElytraFlying()) {
            if (mc.gameSettings.keyBindJump.isKeyDown()) {

                // rotate pitch -45 (12 тиков)
            } else {
                mc.player.rotationPitch = 85;

                // rotate pitch 90 (2 тика)
                mc.player.jump();

                // mc.player.motion.y = 0.085;
            }
        }
    }
}