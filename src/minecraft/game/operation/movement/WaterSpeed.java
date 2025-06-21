package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.transactions.EventUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CEntityActionPacket.Action;

@Defuse(
        name = "Water Speed",
        description = "D",
        brand = Category.Movement
)
public class WaterSpeed extends Module {
    final ModeSetting rejim = new ModeSetting("Режим", "FunTime", "FunTime");
    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (this.rejim.is("FunTime")) {
            if (mc.player != null && mc.player.isAlive() && mc.player.isSwimming()) {
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, Action.STOP_SNEAKING));
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, Action.START_SNEAKING));
                float acceletion = 1.04F;
                mc.player.setDDNVLC(mc.player.getMotion().x * (double) acceletion, mc.player.getMotion().y, mc.player.getMotion().z * (double) acceletion);
            }
        }
    }
}