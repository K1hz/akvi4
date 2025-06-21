package minecraft.game.advantage.words;


import minecraft.game.operation.visual.Hud;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import minecraft.game.advantage.make.color.ColoringSystem;

import static minecraft.game.advantage.make.color.ColoringSystem.IntColor.*;

public class MoreColorsSystem {

    public static StringTextComponent gradient(String message, int colorStart, int colorEnd) {
        StringTextComponent text = new StringTextComponent("");
        int length = message.length();
        long time = System.currentTimeMillis();
        int speed = 8;

        for (int i = 0; i < length; i++) {
            float progress = (float) (Math.sin(time * speed / 2000.0 + i * 0.5) * 0.5 + 0.5);
            int red = (int) (getRed(colorStart) * (1 - progress) + getRed(colorEnd) * progress);
            int green = (int) (getGreen(colorStart) * (1 - progress) + getGreen(colorEnd) * progress);
            int blue = (int) (getBlue(colorStart) * (1 - progress) + getBlue(colorEnd) * progress);
            int color = new java.awt.Color(red, green, blue).getRGB();

            text.append(new StringTextComponent(String.valueOf(message.charAt(i)))
                    .setStyle(Style.EMPTY.setColor(new net.minecraft.util.text.Color(color))));
        }

        return text;
    }

    public static StringTextComponent gradient(String message) {

        StringTextComponent text = new StringTextComponent("");
        for (int i = 0; i < message.length(); i++) {
            float colors = Hud.getColor(i);
            text.append(new StringTextComponent(String.valueOf(message.charAt(i))).setStyle(Style.EMPTY.setColor(new Color((int) colors))));
        }

        return text;

    }

}
