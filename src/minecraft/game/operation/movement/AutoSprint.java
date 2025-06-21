package minecraft.game.operation.movement;

import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;

@Defuse(name = "Sprint",description = "Автоматически включает спринт", brand = Category.Movement)
public class AutoSprint extends Module {
    public CheckBoxSetting saveSprint = new CheckBoxSetting("Сохранять спринт", true);
    public AutoSprint() {
        addSettings(saveSprint);
    }
}
