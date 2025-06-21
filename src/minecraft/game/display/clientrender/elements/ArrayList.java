package minecraft.game.display.clientrender.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.List;
import java.util.stream.Collectors;

import minecraft.game.advantage.figures.StopWatch;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.font.Fonts;
import minecraft.game.advantage.words.MoreColorsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientrender.timeupdate.ElementRenderer;
import minecraft.game.display.clientrender.timeupdate.ElementUpdater;
import minecraft.game.operation.visual.Hud;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.transactions.EventRender2D;
import minecraft.game.transactions.EventUpdate;
import minecraft.system.AG;
import ru.hogoshi.Animation;

public class ArrayList implements ElementRenderer, ElementUpdater {
    private int lastIndex;
    private List<Module> list;
    private final StopWatch stopWatch = new StopWatch();

    public void update(EventUpdate e) {
        if (stopWatch.isReached(1000L)) {
            list = AG.getInst().getModuleManager().getSorted(Fonts.sfui, 7.5f).stream()
                    .filter(m -> m.getCategory() != Category.Visual && m.getCategory() != Category.Misc)
                    .collect(Collectors.toList());
            stopWatch.reset();
        }
    }

    public void render(EventRender2D pizda) {
        if (list == null || list.isEmpty()) return;

        MatrixStack ms = pizda.getMatrixStack();
        final float rounding = 3.0F, padding = 3.5F, posX = 6F;
        float posY = 19;

        int index = 0;
        for (Module f : list) {
            Animation anim = f.getAnimation();
            float value = (float) anim.getValue();
            if (value > 0.0F) {
                String text = f.getName();

                float textWidth = ClientFonts.clickGui[15].getWidth(text);
                float fontSize = 6.5F;

                GraphicsSystem.drawRectW(posX - 1.5f, posY + 0.5f, 2f, fontSize + padding * 1.1f, ColoringSystem.getColorBlack(1));
                GraphicsSystem.drawRoundedRect(posX + 1, posY, textWidth + padding * 1.2F, fontSize + padding * 1.3F, 1.5f,
                        ColoringSystem.rgba(20, 20, 20, (int) (255)));

                    ClientFonts.clickGui[15].drawString(ms, MoreColorsSystem.gradient(text, Hud.getColor(1, 25), ColoringSystem.rgb(25,25,25)), posX + padding, posY + padding, -1);

                posY += (5F + padding * 1.5F);
                index++;
            }
        }
        lastIndex = index - 1;
    }
}
