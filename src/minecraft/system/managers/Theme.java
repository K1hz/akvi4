package minecraft.system.managers;

import com.google.common.eventbus.Subscribe;
import net.minecraft.util.text.Color;
import minecraft.system.AG;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.visual.Hud;
import minecraft.game.advantage.make.color.ColoringSystem;

import java.util.HashMap;
import java.util.Map;

public class Theme {

    public static int textColor, darkTextColor, mainRectColor, darkMainRectColor, rectColor;

    private static final Map<String, int[]> THEME_COLORS = new HashMap<>();

    static {
        THEME_COLORS.put("Синий", new int[]{
                ColoringSystem.rgb(95, 112, 176),
                ColoringSystem.rgb(95, 112, 176),
                ColoringSystem.rgb(33, 37, 54),
                ColoringSystem.rgb(48, 57, 94),
                ColoringSystem.rgb(69, 101, 181)
        });
        THEME_COLORS.put("Розовый", new int[]{
                ColoringSystem.rgb(222, 124, 214),
                ColoringSystem.rgb(169, 95, 176),
                ColoringSystem.rgb(54, 33, 53),
                ColoringSystem.rgb(94, 48, 86),
                ColoringSystem.rgb(181, 69, 172)
        });
        THEME_COLORS.put("Фиолетовый", new int[]{
                ColoringSystem.rgb(165, 124, 222),
                ColoringSystem.rgb(129, 95, 176),
                ColoringSystem.rgb(45, 33, 54),
                ColoringSystem.rgb(76, 48, 94),
                ColoringSystem.rgb(119, 69, 181)
        });
        THEME_COLORS.put("Эстетичный", new int[]{
                ColoringSystem.rgb(124, 222, 209),
                ColoringSystem.rgb(95, 176, 153),
                ColoringSystem.rgb(33, 54, 49),
                ColoringSystem.rgb(48, 94, 79),
                ColoringSystem.rgb(69, 181, 146)
        });
        THEME_COLORS.put("Бирюзовый", new int[]{
                ColoringSystem.rgb(64, 216, 224),
                ColoringSystem.rgb(80, 208, 206),
                ColoringSystem.rgb(28, 57, 59),
                ColoringSystem.rgb(32, 102, 107),
                ColoringSystem.rgb(90, 225, 230)
        });
        THEME_COLORS.put("Красный", new int[]{
                ColoringSystem.rgb(222, 124, 124),
                ColoringSystem.rgb(176, 95, 100),
                ColoringSystem.rgb(54, 33, 33),
                ColoringSystem.rgb(94, 48, 48),
                ColoringSystem.rgb(181, 69, 69)
        });
        THEME_COLORS.put("Зеленый", new int[]{
                ColoringSystem.rgb(124, 222, 137),
                ColoringSystem.rgb(95, 176, 106),
                ColoringSystem.rgb(33, 54, 35),
                ColoringSystem.rgb(48, 94, 57),
                ColoringSystem.rgb(69, 181, 101)
        });
        THEME_COLORS.put("Темный", new int[]{
                ColoringSystem.rgb(255, 255, 255),
                ColoringSystem.rgb(100, 100, 100),
                ColoringSystem.rgb(20, 20, 20),
                ColoringSystem.rgb(48, 48, 48),
                ColoringSystem.rgb(61, 61, 61)
        });
    }


    private void setTemplateColors(String theme) {
        int[] colors = THEME_COLORS.get(theme);
        if (colors != null) {
            setColors(colors);
        }
    }

    private void setColors(int[] colors) {
        textColor = colors[0];
        darkTextColor = colors[1];
        darkMainRectColor = colors[2];
        mainRectColor = colors[3];
        rectColor = colors[4];
    }
}
