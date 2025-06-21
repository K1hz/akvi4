package minecraft.game.advantage.words;
import net.minecraft.util.text.*;
import minecraft.system.AG;
import minecraft.system.managers.Theme;
import minecraft.game.advantage.make.color.ColoringSystem;

public class GradientOld {

    public static StringTextComponent gradient(String message) {
        StringTextComponent text = new StringTextComponent("");
        for (int i = 0; i < message.length(); i++) {
            text.append(new StringTextComponent(String.valueOf(message.charAt(i))).setStyle(Style.EMPTY.setColor(new Color(Theme.rectColor))));
        }
        return text;
    }

    public static IFormattableTextComponent white(String message) {
        return new StringTextComponent(message).setStyle(Style.EMPTY.setColor(Color.fromHex("#FFFFFF")));
    }
    public static ITextComponent gray(String text) {
        return new StringTextComponent(text).setStyle(Style.EMPTY.setColor(Color.fromTextFormatting(TextFormatting.GRAY)));
    }
}
