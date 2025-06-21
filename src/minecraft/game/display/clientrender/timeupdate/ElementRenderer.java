package minecraft.game.display.clientrender.timeupdate;

import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.words.MoreColorsSystem;
import minecraft.game.transactions.EventRender2D;
import minecraft.game.advantage.advisee.IMinecraft;
import net.minecraft.util.text.ITextComponent;

public interface ElementRenderer extends IMinecraft {
    void render(EventRender2D eventRender2D);

    static ITextComponent gradient(String clientName, String suffix) {
        return MoreColorsSystem.gradient(clientName + suffix, ColoringSystem.getColorTest4(1), ColoringSystem.rgb(11,11,11));
    }
    static ITextComponent gradient123(String clientName) {
        return MoreColorsSystem.gradient(clientName, ColoringSystem.getColorBlack(0), ColoringSystem.getColorBlack(90));
    }
}
