package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.system.AG;

@Defuse(name = "Anti Target", description = "Дает возможность сбежать от противника на элитре", brand = Category.Movement)
public class AntiTarget extends Module {
    public double boosterSpeed;
    public boolean restart;
    public TimeCounterSetting TimeCounterSetting = new TimeCounterSetting();

    public final SliderSetting pitch = new SliderSetting("Угол наклона",
            35f, 10f, 60f, 1f
    );
    public AntiTarget() {
        setEnabled(false, true);
        this.addSettings(this.pitch);
    }
    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player.isElytraFlying()) {
            mc.player.rotationPitch = -this.pitch.getValue();
        }
    }
}
