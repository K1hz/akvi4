package minecraft.game.operation.misc;


import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;

@Defuse(name = "Aspect Ratio",description = "Растягивает экран", brand = Category.Visual)
public class AspectRatio extends Module {
    public SliderSetting width = new SliderSetting("Ширина", 1, 0.1f, 3f, 0.1f);
    public AspectRatio() {
        addSettings(width);
    }
    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}