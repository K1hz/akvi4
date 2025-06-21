package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.game.advantage.figures.StopWatch;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.transactions.EventUpdate;


@Defuse(name = "Tape Mouse", description = "PIZDA", brand = Category.Combat)
public class TapeMouse extends Module {

    public StopWatch delay = new StopWatch();
    static ModeSetting modeClick = new ModeSetting("Тип", "Левая кнопка", new String[]{"Левая кнопка", "Правая кнопка"});
    static SliderSetting delayForClick = new SliderSetting("Задержка", 1.0F, 1.0F, 15.0F, 0.1F);

    public TapeMouse() {
        this.addSettings(modeClick, delayForClick);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (modeClick.is("Левая кнопка")) {
            if (this.delay.isReached((long)(delayForClick.getValue() * 300.0F))) {
                mc.clickMouse();
                this.delay.reset();
            }
        } else if (modeClick.is("Правая кнопка") && this.delay.isReached((long)((Float)delayForClick.getValue() * 300f))) {
            mc.rightClickMouse();
            this.delay.reset();
        }

    }
}
