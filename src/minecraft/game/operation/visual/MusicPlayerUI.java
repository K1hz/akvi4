package minecraft.game.operation.visual;


import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.BindSetting;

@Defuse(name = "Music Player",description = "Гуи музыки", brand = Category.Visual)
public class MusicPlayerUI extends Module {

    public BindSetting setting = new BindSetting("Кнопка открытия", -1);

    public MusicPlayerUI() {
        addSettings(setting);
    }
}