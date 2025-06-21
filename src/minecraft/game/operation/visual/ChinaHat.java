package minecraft.game.operation.visual;

import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;

import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;

@Defuse(name = "China Hat",description = "Pizdec", brand = Category.Visual)
public class ChinaHat extends minecraft.game.operation.wamost.api.Module {
    public static final ModeSetting type = new ModeSetting(
            "Мод",
            "Обычный",
            "Обычный",
            "Новый",
            "Кастомный"
    );
    public static final SliderSetting aaaa = new SliderSetting("Прозрачность", 255, 0f, 255, 1f).visibleIf(() -> type.is("Кастомный"));
    public static final SliderSetting толщина = new SliderSetting("Толщина линий", 1f, 0.1f, 5f, 0.01f).visibleIf(() -> type.is("Кастомный"));
    public static final SliderSetting ширина = new SliderSetting("Ширина", 6.5f, 1f, 15, 0.1f).visibleIf(() -> type.is("Кастомный"));
    public static final SliderSetting высота = new SliderSetting("Высота", 3f, 0.1f, 10f, 0.1f).visibleIf(() -> type.is("Кастомный"));
    public static final SliderSetting posY = new SliderSetting("Позиция по Y", 0, -10f, 10f, 0.1f).visibleIf(() -> type.is("Кастомный"));
    public static final SliderSetting posZ = new SliderSetting("Позиция по X", 0f, -1f, 1f, 0.01f).visibleIf(() -> type.is("Кастомный"));
    public static final SliderSetting posX = new SliderSetting("Позиция по Z", 0f, -1, 1f, 0.01f).visibleIf(() -> type.is("Кастомный"));
    public ChinaHat() {
        this.addSettings(
                this.type,this.aaaa, this.толщина,this.ширина, this.высота, this.posY, this.posX, this.posZ);
    }
}
