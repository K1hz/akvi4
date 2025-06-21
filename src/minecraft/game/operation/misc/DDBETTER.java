package minecraft.game.operation.misc;

import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;

@Defuse(name = "Beautifully",description = "Делает игру приятнее", brand = Category.Visual)
public class DDBETTER extends Module {

    // Настройки для различных функций
    public final CheckBoxSetting smoothCamera = new CheckBoxSetting("Плавная камера", true);
    public final CheckBoxSetting smoothChat = new CheckBoxSetting("Плавный чат", true);
    public final CheckBoxSetting distancecamera1 = new CheckBoxSetting("Настройки камеры", true);
    public final CheckBoxSetting betterTab = new CheckBoxSetting("Улучшенный таб", true);
    public final CheckBoxSetting betterChat = new CheckBoxSetting("Улучшенный чат", true);
    public static final CheckBoxSetting invAnim = new CheckBoxSetting("Живой инвентарь", false);
    public final CheckBoxSetting fpsBoot = new CheckBoxSetting("Оптимизировать", false);  // Сделано нестатическим
    public final CheckBoxSetting gavno = new CheckBoxSetting("fff", false);
    public final SliderSetting distanceCamera = new SliderSetting("Дистанция камеры", 4f, 1f, 16f, 0.1f)
            .visibleIf(() -> distancecamera1.getValue());  // Делаем видимость зависимой от флага "Изменить дистанцию камеры"
    public final SliderSetting postionCamera = new SliderSetting("Позиция камеры", 0f, -5f, 5f, 0.1f)
            .visibleIf(() -> distancecamera1.getValue());
    public final SliderSetting upcamera = new SliderSetting("Высота камеры", 0f, -5f, 5f, 0.1f)
            .visibleIf(() -> distancecamera1.getValue());

    // Конструктор для инициализации настроек
    public DDBETTER() {
        addSettings(invAnim, betterTab, betterChat, smoothCamera, distancecamera1, smoothChat, fpsBoot, distanceCamera, postionCamera, upcamera);
    }

    public boolean isFpsMode() {
        return this.isEnabled() && fpsBoot.getValue();  // Проверка, активен ли модуль и включена ли оптимизация
    }
}
