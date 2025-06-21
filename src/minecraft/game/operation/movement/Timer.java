package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;

import net.minecraft.network.play.server.SEntityDDNVLCPacket;
import minecraft.game.transactions.EventPacket;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import net.minecraft.network.play.client.CConfirmTransactionPacket;

@Defuse(name = "Timer",description = "Ускоряет игру", brand = Category.Movement)
public class Timer extends Module {

    public final SliderSetting speed = new SliderSetting("Скорость", 2f, 0.1f, 10f, 0.1f);

    public Timer() {
        addSettings(speed);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SEntityDDNVLCPacket p) {
            if (p.getEntityID() == mc.player.getEntityId()) {
                resetSpeed();
            }
        }

        if (e.getPacket() instanceof CConfirmTransactionPacket p) {
            e.cancel();
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        mc.timer.timerSpeed = this.speed.getValue();
    }

    private void reset() {
        mc.timer.timerSpeed = 1;
    }

    public void resetSpeed() {
        mc.timer.timerSpeed = 1.0F;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
    }
}
