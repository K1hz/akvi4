package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.operation.wamost.massa.elements.*;
import net.minecraft.util.text.Color;
import org.lwjgl.glfw.GLFW;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.transactions.EventUpdate;
import minecraft.system.AG;

@Defuse(name = "Click Gui",description = "Изменяет эту гуи", brand = Category.Visual)
public class ClickGui extends Module {
    public static final ModeSetting mode = new ModeSetting("Режим гуи", "Панельная", "Панельная", "Плиточная");
    public static SliderSetting sizeGui = new SliderSetting("Размер ГУИ", 0.9f, 0.7f, 1.5f, 0.01f).visibleIf(() -> mode.is("Панельная"));
    public static BindSetting bind = new BindSetting("Кнопка открытия", GLFW.GLFW_KEY_RIGHT_SHIFT);
    public static CheckBoxSetting gradient = new CheckBoxSetting("Градиент", false);
    public static CheckBoxSetting centerNAME = new CheckBoxSetting("Центрировать названия", false);
    public static CheckBoxSetting settings = new CheckBoxSetting("Отображать шестерни", true);
    public static CheckBoxSetting helpers = new CheckBoxSetting("Вспомогательные текста", false);
    public static CheckBoxSetting scrollingg = new CheckBoxSetting("Скролл", false);
    public static CheckBoxSetting searchick = new CheckBoxSetting("Поиск", true);
    public static CheckBoxSetting info = new CheckBoxSetting("Информация", false);

    public ClickGui() {
        addSettings(mode,bind, gradient, scrollingg, settings, centerNAME, helpers);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        toggle();
    }
}
