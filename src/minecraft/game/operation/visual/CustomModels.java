package minecraft.game.operation.visual;

import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;

@Defuse(name = "Custom Models",  description = "Маму ебал", brand = Category.Misc)
public class CustomModels extends Module {
    public final ModeSetting mode = new ModeSetting("Модель","White Demon", "White Demon","Red Demon", "Crazy Rabbit", "Freddy Bear");
    public final CheckBoxSetting friends = new CheckBoxSetting("На друзей",true);
    public CustomModels() {
        addSettings(mode, friends);
    }
}