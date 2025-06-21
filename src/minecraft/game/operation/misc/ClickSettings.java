package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.game.operation.combat.DDATTACK;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.system.AG;
import net.minecraft.util.math.MathHelper;
import minecraft.game.transactions.EventMotion;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.StopWatch;

@Defuse(name = "Click Settings", description = "Отвечает за клики клиента",brand = Category.Movement)
public class ClickSettings extends Module {

    public static final ModeSetting sprintType = new ModeSetting("Режим спринта", "Пакетный", "Пакетный", "Легитный");
    public static final ModeSetting attackType = new ModeSetting("Режим удара", "Обычный", "Обычный", "Beta");
    public static final ModeSetting fallDistance = new ModeSetting("Момент удара", "Обычный", "Обычный", "Рандомный");
    public final SliderSetting customFallDistance = new SliderSetting("Момент удара по Y", 0.5f, 0f, 0.99f, 0.1f).visibleIf(() -> this.fallDistance.is("Кастомный"));

    public ClickSettings() {
        addSettings(sprintType, attackType, fallDistance, customFallDistance);
    }
    @Subscribe
    public void onUpdate(EventMotion e) {
        toggle();
        AG.getInst().getModuleManager().getDDATTACK();
        DDATTACK ddattack = null;
        if (ddattack.target !=null) {
            mc.player.setSprinting(false);
        }
    }
}
