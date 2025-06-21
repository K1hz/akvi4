package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;
import minecraft.game.operation.combat.AntiTarget;
import minecraft.game.transactions.EventPacket;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.system.AG;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;

@Defuse(name = "Elytra Booster", description = "Увеличивает скорость феерверка", brand = Category.Movement)
public class ElytraBooster extends Module {
    public final ModeSetting mode = new ModeSetting("Мод", "Default", "Default", "BravoHVH");
    public final SliderSetting boostPower = new SliderSetting("Скорость буста", 1.63f, 1.55f, 3f, 0.01f).visibleIf(() -> mode.is("Default"));
    public double boosterSpeed;
    public boolean restart;
    public TimeCounterSetting TimeCounterSetting = new TimeCounterSetting();

    public ElytraBooster() {
        addSettings(mode, boostPower);
    }

    @Subscribe
    public void packet(EventPacket event) {
        if (event.getPacket() instanceof SPlayerPositionLookPacket packet && mode.is("BravoHVH")) {
            restart = false;
            boosterSpeed = 0.7;
        }
    }

    @Subscribe
    public void update(EventUpdate event) {
        AntiTarget antiTarget = AG.getInst().getModuleManager().getAntiTarget();
        if (mode.is("BravoHVH")) {
                if (!restart) {
                    boosterSpeed = 1.5;
                    if (TimeCounterSetting.isReached(1000)) {
                        restart = true;
                        TimeCounterSetting.reset();
                    }
                }
            if (antiTarget.isEnabled()) {
                if (restart) boosterSpeed = Math.min(boosterSpeed + 0.05, 2.00000000);
            }
            else {
                if (restart) boosterSpeed = Math.min(boosterSpeed + 0.05, 1.66800064);
            }
        } else {
            boosterSpeed = boostPower.getValue();
        }
    }
}
