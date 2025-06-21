package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;
import minecraft.game.transactions.EventChangeWorld;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.advantage.figures.MathSystem;
import net.minecraft.network.play.client.CPlayerPacket;

@Defuse(name = "KT Leave",description = "Ливает в кт", brand = Category.Movement)
public class KTLeave extends Module {

    // без сомнения, базовый модуль для целестиала (.bind add ktleave space)

    final ModeSetting mode = new ModeSetting("Способ телепорта", "Packet", "Vanilla", "Packet", "VClipSpam");

    public KTLeave() {
        addSettings(mode);
    }

    @Subscribe
    public void onChange(EventChangeWorld e) {
        toggle();
    }

    @Override
    public void onEnable() {
        int xCoord = MathSystem.randomInt(-500, 500);
        int zCoord = MathSystem.randomInt(-500, 500);
        if (mode.is("Packet")) {
            for (int i = 0; i < 12; ++i) {
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(xCoord, 180.0, zCoord, true));
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(xCoord, 180.0, zCoord, true));
            }
            for (int i = 0; i < 12; ++i) {
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), 180.0, mc.player.getPosZ(), true));
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), 180.0, mc.player.getPosZ(), true));
            }
        }

        if (mode.is("Vanilla")) {
            mc.player.setPositionAndUpdate(xCoord, 180, zCoord);
        }

        if (mode.is("VClipSpam")) {
            for (int j = 0; j < 5; ++j) {
                int packetsCount = Math.max((20 / 1000), 3);
                for (int i = 0; i < packetsCount; i++) {
                    mc.player.connection.sendPacket(new CPlayerPacket(mc.player.isOnGround()));
                }
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + 20, mc.player.getPosZ(), false));
                mc.player.setPosition(mc.player.getPosX(), mc.player.getPosY() + 20, mc.player.getPosZ());
            }
        }

        super.onEnable();
    }
}
