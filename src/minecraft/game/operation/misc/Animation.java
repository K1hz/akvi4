package minecraft.game.operation.misc;


import com.google.common.eventbus.Subscribe;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.transactions.EventUpdate;

@Defuse(name = "Animation", brand = Category.Visual, description = "Добавляет кастомные движение вашего персонажа")
public class Animation extends Module {

    public ModeSetting modes = new ModeSetting("Мод", "Дрочить", "Дрочить", "Наруто");
    public Animation() {
        addSettings(modes);
    }


    @Subscribe
    public boolean onUpdate(final EventUpdate update) {
        return false;
    }
}
